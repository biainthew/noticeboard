package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Comment;
import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.entity.Post;
import aib.noticeboard.domain.enums.CommentStatus;
import aib.noticeboard.domain.enums.MemberRole;
import aib.noticeboard.domain.enums.PostStatus;
import aib.noticeboard.dto.request.CommentRequestDto;
import aib.noticeboard.dto.response.CommentResponseDto;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.repository.CommentRepository;
import aib.noticeboard.repository.MemberRepository;
import aib.noticeboard.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    private Member member;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("test@test.com")
                .password("password123")
                .nickname("테스터")
                .role(MemberRole.ROLE_USER)
                .build();

        post = Post.builder()
                .member(member)
                .title("테스트 제목")
                .content("테스트 내용")
                .status(PostStatus.ACTIVE)
                .build();

        comment = Comment.builder()
                .member(member)
                .post(post)
                .parent(null)
                .content("테스트 댓글")
                .status(CommentStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("댓글 작성 성공")
    void create_success() {
        // given
        CommentRequestDto.Create request = new CommentRequestDto.Create();
        request.setContent("테스트 댓글");
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(postRepository.findByIdAndStatus(1L, PostStatus.ACTIVE)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        CommentResponseDto.Detail result = commentService.create(member.getEmail(), 1L, request);

        // then
        assertThat(result.getContent()).isEqualTo(comment.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 존재하지 않는 게시글")
    void create_fail_postNotFound() {
        // given
        CommentRequestDto.Create request = new CommentRequestDto.Create();
        request.setContent("테스트 댓글");
        given(memberRepository.findByEmail(any())).willReturn(Optional.of(member));
        given(postRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.create(member.getEmail(), 999L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void update_success() {
        // given
        CommentRequestDto.Update request = new CommentRequestDto.Update();
        request.setContent("수정된 댓글");
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when
        CommentResponseDto.Detail result = commentService.update(member.getEmail(), 1L, request);

        // then
        assertThat(result.getContent()).isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 권한 없음")
    void update_fail_unauthorized() {
        // given
        CommentRequestDto.Update request = new CommentRequestDto.Update();
        request.setContent("수정된 댓글");
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.update("other@test.com", 1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.COMMENT_UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void delete_success() {
        // given
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when
        commentService.delete(member.getEmail(), 1L);

        // then
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없음")
    void delete_fail_unauthorized() {
        // given
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.delete("other@test.com", 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.COMMENT_UNAUTHORIZED.getMessage());
    }
}
