package codestates.main007.comments;

import codestates.main007.board.Board;
import codestates.main007.board.BoardService;
import codestates.main007.exception.ExceptionCode;
import codestates.main007.member.Member;
import codestates.main007.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    private final MemberService memberService;

    private final BoardService boardService;

    public void save(String accessToken, long boardId, Comment comment) {
        Member writer = memberService.findByAccessToken(accessToken);
        Board board = boardService.find(boardId);

        comment.setWriterAndBoard(writer, board);

        commentRepository.save(comment);
    }

    public void update(String accessToken, CommentDto.Input patchDto, long commentId) {
        Member member = memberService.findByAccessToken(accessToken);
        Comment comment = find(commentId);
        Member writer = comment.getWriter();
        if (member.getMemberId()>5){
            if (member != writer) {
                throw new ResponseStatusException(ExceptionCode.MEMBER_UNAUTHORIZED.getStatus(), ExceptionCode.MEMBER_UNAUTHORIZED.getMessage(), new IllegalArgumentException());
            }
        }

        comment.patchComment(patchDto.getComment(), accessToken);
        commentRepository.save(comment);
    }

    public Comment find(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(ExceptionCode.COMMENT_NOT_FOUND.getStatus(), ExceptionCode.COMMENT_NOT_FOUND.getMessage(), new IllegalArgumentException()));
    }

    public void delete(String accessToken, long commentId) {
        Member member = memberService.findByAccessToken(accessToken);
        Comment comment = find(commentId);
        Member writer = comment.getWriter();
        if (member.getMemberId()>5){
            if (member != writer) {
                throw new ResponseStatusException(ExceptionCode.MEMBER_UNAUTHORIZED.getStatus(), ExceptionCode.MEMBER_UNAUTHORIZED.getMessage(), new IllegalArgumentException());
            }
        }

        commentRepository.deleteById(commentId);
    }
}

