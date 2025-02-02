package codestates.main007.board.service;

import codestates.main007.board.dto.BoardDto;
import codestates.main007.board.entity.Board;
import codestates.main007.board.mapper.BoardMapper;
import codestates.main007.board.repository.BoardRepository;
import codestates.main007.boardImage.entity.BoardImage;
import codestates.main007.boardImage.repository.BoardImageRepository;
import codestates.main007.boardImage.service.ImageHandler;
import codestates.main007.boardMember.entity.BoardMember;
import codestates.main007.boardMember.service.BoardMemberService;
import codestates.main007.boardNotice.service.BoardNoticeService;
import codestates.main007.comment.dto.CommentDto;
import codestates.main007.comment.mapper.CommentMapper;
import codestates.main007.exception.ExceptionCode;
import codestates.main007.member.entity.Member;
import codestates.main007.member.service.MemberService;
import codestates.main007.service.DistanceMeasuringService;
import codestates.main007.station.Station;
import codestates.main007.tag.entity.Tag;
import codestates.main007.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardImageRepository boardImageRepository;
    private final MemberService memberService;
    private final CommentMapper commentMapper;
    private final BoardMapper boardMapper;
    private final DistanceMeasuringService distanceService;
    private final BoardMemberService boardMemberService;
    private final BoardNoticeService boardNoticeService;

    private final TagService tagService;
    private final ImageHandler imageHandler;

    public void save(String accessToken, BoardDto.Input boardDto, List<MultipartFile> images) throws IOException {
        Station station = new Station(boardDto.getStationId().intValue());
        double startLat = station.getLatitude();
        double startLong = station.getLongitude();
        double endLat = boardDto.getLatitude();
        double endLong = boardDto.getLongitude();

        List<Tag> tags = tagService.findAll(boardDto.getTags());

        Board board = Board.builder()
                .title(boardDto.getTitle())
                .review(boardDto.getReview())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .categoryId(boardDto.getCategoryId())
                .stationId(boardDto.getStationId())
                .latitude(boardDto.getLatitude())
                .longitude(boardDto.getLongitude())
                .star(boardDto.getStar())
                .upScore(0)
                .downScore(0)
                .viewCount(0)
                .address(boardDto.getAddress())
                .writer(memberService.findByAccessToken(accessToken))
                .timeFromStation(distanceService.getTime(startLat, startLong, endLat, endLong))
                .tags(tags)
                .build();

        // image 핸들러에서 boardId 를 사용하기위해 한 번 저장
        boardRepository.save(board);

        List<BoardImage> list = imageHandler.saveImageOnS3(board, images);

        List<BoardImage> boardImages = new ArrayList<>();
        for (BoardImage tempImage : list) {
            boardImages.add(boardImageRepository.save(tempImage));
        }
        board.setImages(boardImages);

    }
    public void save(Board board){
        boardRepository.save(board);
    }

    public void update(String accessToken, long boardId, BoardDto.Patch patch, List<MultipartFile> images) throws IOException {
        Member member = memberService.findByAccessToken(accessToken);
        Board updatedBoard = find(boardId);
        Member writer = updatedBoard.getWriter();
        if (member.getMemberId()>5){
            if (member != writer) {
                throw new ResponseStatusException(ExceptionCode.MEMBER_UNAUTHORIZED.getStatus(), ExceptionCode.MEMBER_UNAUTHORIZED.getMessage(), new IllegalArgumentException());
            }
        }


        updatedBoard.patchBoard(patch.getTitle(),
                patch.getReview(),
                patch.getStar(),
                patch.getLatitude(),
                patch.getLongitude(),
                patch.getStationId(),
                patch.getCategoryId(),
                patch.getAddress());

        if (patch.getLatitude() != null || patch.getLongitude() != null || patch.getStationId() != null) {
            Station station = new Station(patch.getStationId().intValue());
            double startLat = station.getLatitude();
            double startLong = station.getLongitude();
            double endLat = updatedBoard.getLatitude();
            double endLong = updatedBoard.getLongitude();

            updatedBoard.updateTimeFromStation(distanceService.getTime(startLat, startLong, endLat, endLong));
        }

        List<Long> tagIds = patch.getTags();

        updatedBoard.setTags(tagService.findAll(tagIds));

        List<BoardImage> list = imageHandler.updateImages(updatedBoard, patch.getPriority(), images, patch.getUrls());

        List<BoardImage> boardImages = new ArrayList<>();
        for (BoardImage tempImage : list) {
            boardImages.add(boardImageRepository.save(tempImage));
        }
        updatedBoard.setImages(boardImages);

        boardRepository.save(updatedBoard);
    }

    public void delete(String accessToken, long boardId) {
        Member member = memberService.findByAccessToken(accessToken);
        Board board = find(boardId);
        Member writer = board.getWriter();
        if (member.getMemberId()>5){
            if (member != writer) {
                throw new ResponseStatusException(ExceptionCode.MEMBER_UNAUTHORIZED.getStatus(), ExceptionCode.MEMBER_UNAUTHORIZED.getMessage(), new IllegalArgumentException());
            }
        }

        boardRepository.deleteById(boardId);
    }

    public Board find(long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(ExceptionCode.BOARD_NOT_FOUND.getStatus(), ExceptionCode.BOARD_NOT_FOUND.getMessage(), new IllegalArgumentException()));
    }

    public BoardDto.DetailResponse getDetailPage(long boardId, String accessToken){
        Board board = find(boardId);
        BoardDto.DetailResponse detailResponse = BoardDto.DetailResponse.builder().build();

        // 식당,숙소의 경우 주소가 동일한 경우만 추출
        if (board.getCategoryId() == 1 || board.getCategoryId() == 3) {
            List<CommentDto.Response> comments = commentMapper.commentsToResponses(board.getComments());
            // 해당글 이미지 리스트
            List<String> imageUrls = findImageUrls(board);
            // 주변 가게 게시글 리스트
            List<Board> around = findByAddress(board.getAddress(), board.getStationId(), boardId, board.getCategoryId());// 근처 보드 정보

            boolean isDibs = false;
            int status = 0;
            List<Boolean> booleans = new ArrayList<>();
            for (int i = 0; i < around.size(); i++) {
                booleans.add(false);
            }
            // 로그인 시에만 바뀌는 정보
            if (accessToken != null) {
                // 해당글 찜 여부
                isDibs = checkDibs(accessToken, boardId);
                // 해당글 추천 여부
                Member member = memberService.findByAccessToken(accessToken);
                status = checkScoreStatus(member, board);
                // 주변 가게 찜 정보 리스트
                booleans = findAroundDibs(accessToken, around);
            }

            // 주변가게 DTO로 변경
            List<BoardDto.aroundResponse> aroundResponses = boardMapper.boardsToAround(around, booleans);

            detailResponse = boardMapper.boardToDetailResponseDto(board, isDibs, board.getWriter(), comments, imageUrls, status, aroundResponses);

            // 볼거리의 경우 근처애들 추출
        } else if (board.getCategoryId() == 2) {
            List<CommentDto.Response> comments = commentMapper.commentsToResponses(board.getComments());
            // 해당글 이미지 리스트
            List<String> imageUrls = findImageUrls(board);
            // 주변 가게 게시글 리스트
            List<Board> around = findByAddressViewCategory(board.getStationId(), board.getCategoryId(), boardId);// 근처 보드 정보

            boolean isDibs = false;
            int status = 0;
            List<Boolean> booleans = new ArrayList<>();
            for (int i = 0; i < around.size(); i++) {
                booleans.add(false);
            }

            // 로그인 시에만 바뀌는 정보
            if (accessToken != null) {
                // 해당글 찜 여부
                isDibs = checkDibs(accessToken, boardId);
                Member member = memberService.findByAccessToken(accessToken);
                // 해당글 추천 여부
                status = checkScoreStatus(member, board);
                // 주변 가게 찜 정보 리스트
                booleans = findAroundDibs(accessToken, around);
            }
            // 주변가게 DTO로 변경
            List<BoardDto.aroundResponse> aroundResponses = boardMapper.boardsToAround(around, booleans);

            detailResponse = boardMapper.boardToDetailResponseDto(board, isDibs, board.getWriter(), comments, imageUrls, status, aroundResponses);
        }
        return detailResponse;
    }

    public Page<Board> findBoardPage(long stationId, long categoryId, int page, int size, Sort sort) {
        // 전체 리스트 조회
        if (categoryId == 0) {
            return boardRepository.findByStationId(stationId, PageRequest.of(page, size, sort));
        } else {
            return boardRepository.findByStationIdAndCategoryId(stationId, categoryId,
                    PageRequest.of(page, size, sort));
        }
    }

    public Page<Board> findBoardPageByTag(long stationId, long categoryId, int page, int size, Sort sort, long tagId) {
        return boardRepository.findByStationIdAndCategoryIdAndTags(stationId, categoryId, tagService.find(tagId),
                PageRequest.of(page, size, sort));
    }

    public boolean checkDibs(String accessToken, long boardId) {
        Member member = memberService.findByAccessToken(accessToken);

        return boardMemberService.checkDibs(member, find(boardId));
    }

    public List<BoardDto.boardsResponse> listCheckDibs(String accessToken, List<BoardDto.boardsResponse> responses) {
        Member member = memberService.findByAccessToken(accessToken);

        List<BoardDto.boardsResponse> result = new ArrayList<>();
        for (BoardDto.boardsResponse dto : responses) {
            Board board = find(dto.getBoardId());
            dto.setDibs(boardMemberService.checkDibs(member, board));
            result.add(dto);
        }

        return result;
    }

    public Integer checkScoreStatus(Member member, Board board) {
        BoardMember boardMember = boardMemberService.getBoardMember(member, board);
        return boardMember.getScoreStatus();
    }

    public boolean dibs(String accessToken, long boardId) {
        Member member = memberService.findByAccessToken(accessToken);
        Board board = find(boardId);

        return boardMemberService.changeDibs(member, board);
    }

    public void report(String accessToken, long boardId, long reportId){
        Member member = memberService.findByAccessToken(accessToken);
        Board board = find(boardId);

        boardMemberService.report(member, board, reportId);
    }
    public Integer upVote(String accessToken, long boardId) {
        Board board = find(boardId);
        Member member = memberService.findByAccessToken(accessToken);
//        boardNoticeService.save(boardId, member, "UpVote");

        return boardMemberService.upVote(member, board);
    }

    public Integer downVote(String accessToken, long boardId) {
        Board board = find(boardId);
        Member member = memberService.findByAccessToken(accessToken);
//        boardNoticeService.save(boardId, member, "DownVote");

        return boardMemberService.downVote(member, board);
    }

    public List<String> findImageUrls(Board board) {
        List<String> imageUrls = new ArrayList<>();
        List<BoardImage> boardImages = boardImageRepository.findAllByBoard(board);
        for (BoardImage boardImage : boardImages) {
            imageUrls.add(boardImage.getStoredFilePath());
        }

        return imageUrls;
    }

    public List<Board> findByAddress(String address, long stationId, long boardId, long categoryId) {
        List<Board> boards = boardRepository.findByAddressAndStationIdAndCategoryId(address, stationId, categoryId);

        Board board = find(boardId);
        boards.remove(board);

        return boards;
    }

    public List<Board> findByAddressViewCategory(long stationId, long categoryId, long boardId) {
        Board thisBoard = find(boardId);

        List<Board> boards = boardRepository.findByStationIdAndCategoryId(stationId, categoryId);
        for (int i = 0; i < boards.size(); i++) {
            Board board = boards.get(i);
            if (board.getLongitude() < thisBoard.getLongitude() - 0.000005 || board.getLongitude() > thisBoard.getLongitude() + 0.000005) {
                boards.remove(board);
            } else if (board.getLatitude() < thisBoard.getLatitude() - 0.000005 || board.getLatitude() > thisBoard.getLatitude() + 0.000005) {
                boards.remove(board);
            }
        }
        boards.remove(thisBoard);

        return boards;
    }

    public List<Boolean> findAroundDibs(String access, List<Board> boards) {
        List<Boolean> booleans = new ArrayList<>();
        for (Board board : boards) {
            booleans.add(checkDibs(access, board.getBoardId()));
        }
        return booleans;
    }

    public List<Board> findAllBoards(){
        return boardRepository.findAll();
    }

    public Integer countByStationId(long stationId){
        return boardRepository.countByStationId(stationId);
    }

    public List<Board> findHighScoreBoard(int score){
        return boardRepository.findByScoreGreaterThan(score);
    }
    public List<Board> findLowScoreBoard(int score){
        return boardRepository.findByScoreLessThan(score);
    }

}
