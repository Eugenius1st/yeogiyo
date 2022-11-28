package codestates.main007.member;


import codestates.main007.auth.jwt.JwtTokenizer;
import codestates.main007.auth.util.CustomAuthorityUtils;
import codestates.main007.board.Board;
import codestates.main007.board.BoardRepository;
import codestates.main007.boardImage.ImageHandler;
import codestates.main007.boardMember.BoardMember;
import codestates.main007.boardMember.BoardMemberRepository;
import codestates.main007.comments.Comment;
import codestates.main007.comments.CommentRepository;
import codestates.main007.exception.BusinessLogicException;
import codestates.main007.exception.ExceptionCode;
import codestates.main007.member.query.MemberScore;
import codestates.main007.member.query.MemberStation;
import codestates.main007.service.RandomAvatarService;
import codestates.main007.service.RandomNamingService;
import codestates.main007.service.RandomPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final CustomAuthorityUtils authorityUtils;
    private final RandomNamingService namingService;
    private final RandomAvatarService avatarService;
    private final RandomPasswordService randomPasswordService;
    private final ImageHandler imageHandler;
    private final JwtTokenizer jwtTokenizer;

    public Member save(MemberDto.Signup signupDto) {
        verifyExistEmail(signupDto.getEmail());
        String encryptedPassword = passwordEncoder().encode(signupDto.getPassword());
        List<String> roles = authorityUtils.createRoles(signupDto.getEmail());

        Member createdMember = Member.builder()
                .email(signupDto.getEmail())
                .name(namingService.genName())
                .avatar(avatarService.genAvatar())
                .password(encryptedPassword)
                .roles(roles)
                .build();

        return memberRepository.save(createdMember);
    }

    public void saveOAuthMember(String name, String email, String avatar) {
        Member oAuthMember = Member.builder()
                .name(name)
                .email(email)
                .avatar(avatar)
                .build();
        memberRepository.save(oAuthMember);
    }

    public void update(String accessToken, MemberDto.Patch patchDto) {
        Member member = findByAccessToken(accessToken);
        if (memberRepository.countByName(patchDto.getName()) != 0) {
            throw new ResponseStatusException(ExceptionCode.MEMBER_EXISTS.getStatus(), ExceptionCode.MEMBER_EXISTS.getMessage(), new IllegalArgumentException());
        }
        member.patchMember(patchDto.getName(), patchDto.getPassword(), passwordEncoder());
        memberRepository.save(member);
    }

    public void updateAvatar(String accessToken, MultipartFile image) throws IOException {
        Member member = findByAccessToken(accessToken);
        String avatarUrl = imageHandler.updateAvatar(image, member);

        member.patchAvatar(avatarUrl);

        memberRepository.save(member);
    }

    public Member find(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(ExceptionCode.MEMBER_NOT_FOUND.getStatus(), ExceptionCode.MEMBER_NOT_FOUND.getMessage(), new IllegalArgumentException()));
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(ExceptionCode.MEMBER_NOT_FOUND.getStatus(), ExceptionCode.MEMBER_NOT_FOUND.getMessage(), new IllegalArgumentException()));
    }

    public int countByName(String name) {
        return memberRepository.countByName(name);
    }

    public int countByEmail(String email) {
        return memberRepository.countByEmail(email);
    }


    private void verifyExistEmail(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent())
            throw new ResponseStatusException(ExceptionCode.MEMBER_EXISTS.getStatus(), ExceptionCode.MEMBER_EXISTS.getMessage(), new IllegalArgumentException());
    }

    public Member findByAccessToken(String accessToken) {
        long userId = jwtTokenizer.getUserId(accessToken);
        return find(userId);
    }

    public String findPassword(String email) {
        Member member = findByEmail(email);

        String password = randomPasswordService.genPassword();

        member.resetPassword(passwordEncoder().encode(password));
        memberRepository.save(member);

        return password;
    }

    @SneakyThrows
    public void verifyPassword(String accessToken, String password) {
        Member member = findByAccessToken(accessToken);

        if (!passwordEncoder().matches(password, member.getPassword())) {
            throw new ResponseStatusException(ExceptionCode.MEMBER_UNAUTHORIZED.getStatus(), ExceptionCode.MEMBER_UNAUTHORIZED.getMessage(), new IllegalArgumentException());
        }
    }


    public Page<Comment> findMyComments(String accessToken, int page, int size, Sort sort) {
        Member member = findByAccessToken(accessToken);

        return commentRepository.findByWriter(member, PageRequest.of(page, size, sort));
    }

    public int findMyScore(Member member) {
        int totalScore = 0;

        List<MemberScore> scores = boardRepository.findScoreByWriter(member);
        for (MemberScore s : scores) {
            totalScore += s.getScore();
        }
        return totalScore;
    }

    public List<Long> findMyStations(Member member) {
        List<Long> myStations = new ArrayList<>();

        List<MemberStation> stations = boardRepository.findStationIdByWriter(member);
        for (MemberStation ms : stations) {
            if (!myStations.contains(ms.getStationId())) {
                myStations.add(ms.getStationId());
            }
        }
        return myStations;
    }

    public List<Board> findMyDibsByStation(String accessToken, long stationId) {
        Member member = findByAccessToken(accessToken);
        List<BoardMember> boardMembers = boardMemberRepository.findByMemberAndDibsTrue(member);
        List<Long> boardIds = new ArrayList<>();

        for (BoardMember boardMember : boardMembers) {
            Board board = boardMember.getBoard();
            if (board.getStationId() == stationId) {
                boardIds.add(board.getBoardId());
            }
        }

        return boardRepository.findAllByBoardIdIn(boardIds);
    }

    public List<Board> findMyDibs(String accessToken) {
        Member member = findByAccessToken(accessToken);
        List<BoardMember> boardMembers = boardMemberRepository.findByMemberAndDibsTrue(member);
        List<Long> boardIds = new ArrayList<>();

        for (BoardMember boardMember : boardMembers) {
            Board board = boardMember.getBoard();
            boardIds.add(board.getBoardId());
        }

        return boardRepository.findAllByBoardIdIn(boardIds);
    }

    public List<MemberDto.MyPage> setIsDibsToMyPage(String accessToken, List<MemberDto.MyPage> memberDtos) {
        Member member = findByAccessToken(accessToken);
        for (MemberDto.MyPage myPage : memberDtos) {
            Board board = boardRepository.findById(myPage.getBoardId()).get();
            boolean isDibs = false;
            Optional<BoardMember> boardMember = boardMemberRepository.findByMemberAndBoard(member, board);
            if (boardMember.isPresent()) {
                isDibs = boardMember.get().isDibs();
            }
            myPage.setDibs(isDibs);
        }
        return memberDtos;
    }

    public void deleteMember(String accessToken) {
        Member member = findByAccessToken(accessToken);

        memberRepository.delete(member);
    }

    public List<Member> findAllMembers(){
        return memberRepository.findAll();
    }

    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
