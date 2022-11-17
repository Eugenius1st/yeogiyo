package codestates.main007.member;

import codestates.main007.board.Board;
import codestates.main007.boardMember.BoardMember;
import codestates.main007.comments.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long memberId;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String password;

    @Column
    private String avatar;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @OneToMany(mappedBy = "writer", cascade = CascadeType.REMOVE)
    private final List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "writer", cascade = CascadeType.REMOVE)
    private final List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private final List<BoardMember> boardMembers = new ArrayList<>();

    public void patchMember(String name, String avatar, String password) {
        if (name != null) {
            this.name = name;
        }
        if (avatar != null) {
            this.avatar = avatar;
        }
        if (password != null) {
            this.password = password;
        }
    }
    public void setUserDetails(long memberId, String email, String password){
        this.memberId = memberId;
        this.email = email;
        this.password = password;
    }
    public void resetPassword(String password){
        this.password = password;
    }
}
