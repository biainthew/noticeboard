package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.entity.Post;
import aib.noticeboard.domain.enums.MemberRole;
import aib.noticeboard.domain.enums.PostStatus;
import aib.noticeboard.dto.request.PostRequestDto;
import aib.noticeboard.dto.response.PostResponseDto;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ViewCountService viewCountService;

    private Member member;
    private Post post;

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
    }

    @Test
    @DisplayName("게시글 작성 성공")
    void create_success() {
        // given
        PostRequestDto.Create request = new PostRequestDto.Create();
        request.setTitle("테스트 제목");
        request.setContent("테스트 내용");
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(postRepository.save(any(Post.class))).willReturn(post);

        // when
        PostResponseDto.Detail result = postService.create(member.getEmail(), request);

        // then
        assertThat(result.getTitle()).isEqualTo(post.getTitle());
        assertThat(result.getContent()).isEqualTo(post.getContent());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 작성 실패 - 존재하지 않는 회원")
    void create_fail_memberNotFound() {
        // given
        PostRequestDto.Create request = new PostRequestDto.Create();
        request.setTitle("테스트 제목");
        request.setContent("테스트 내용");
        given(memberRepository.findByEmail(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.create("no@test.com", request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getDetail_success() {
        // given
        given(postRepository.findByIdAndStatus(1L, PostStatus.ACTIVE)).willReturn(Optional.of(post));
        given(viewCountService.getViewCount(1L)).willReturn(0);

        // when
        PostResponseDto.Detail result = postService.getDetail(1L);

        // then
        assertThat(result.getTitle()).isEqualTo(post.getTitle());
        verify(viewCountService).increaseViewCount(1L);
    }

    @Test
    @DisplayName("게시글 상세 조회 실패 - 존재하지 않는 게시글")
    void getDetail_fail_postNotFound() {
        // given
        given(postRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getDetail(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void update_success() {
        // given
        PostRequestDto.Update request = new PostRequestDto.Update();
        request.setTitle("테스트 제목 수정");
        request.setContent("테스트 내용 수정");
        given(postRepository.findByIdAndStatus(1L, PostStatus.ACTIVE)).willReturn(Optional.of(post));

        // when
        PostResponseDto.Detail result = postService.update(member.getEmail(), 1L, request);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("게시글 수정 실패 - 권한 없음")
    void update_fail_unauthorized() {
        // given
        PostRequestDto.Update request = new PostRequestDto.Update();
        request.setTitle("테스트 제목 수정");
        request.setContent("테스트 내용 수정");
        given(postRepository.findByIdAndStatus(1L, PostStatus.ACTIVE)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.update("no@test.com", 1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.POST_UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void delete_success() {
        // given
        given(postRepository.findByIdAndStatus(1L, PostStatus.ACTIVE)).willReturn(Optional.of(post));

        // when
        postService.delete(member.getEmail(), 1L);

        // then
        assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void delete_fail_unauthorized() {
        // given
        given(postRepository.findByIdAndStatus(1L, PostStatus.ACTIVE)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.delete("no@test.com", 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.POST_UNAUTHORIZED.getMessage());
    }
}
