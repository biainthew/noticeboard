package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Like;
import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.entity.Post;
import aib.noticeboard.domain.enums.MemberRole;
import aib.noticeboard.domain.enums.PostStatus;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.repository.LikeRepository;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    private Member member;
    private Post post;
    private Like like;

    @BeforeEach
    void setup() {
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

        like = Like.builder()
                .member(member)
                .post(post)
                .build();
    }

    @Test
    @DisplayName("좋아요 성공")
    void like_success() {
        // given
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(postRepository.findByIdAndStatus(1L, PostStatus.ACTIVE)).willReturn(Optional.of(post));
        given(likeRepository.existsByMemberAndPost(member, post)).willReturn(false);

        // when
        likeService.like(member.getEmail(), 1L);

        // then
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("좋아요 실패 - 이미 좋아요를 누른 게시글")
    void like_fail_already_post() {
        // given
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(postRepository.findByIdAndStatus(1L, PostStatus.ACTIVE)).willReturn(Optional.of(post));
        given(likeRepository.existsByMemberAndPost(member, post)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> likeService.like(member.getEmail(), 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.LIKE_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("좋아요 취소 성공")
    void unlike_success() {
        // given
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(postRepository.findByIdAndStatus(1L, PostStatus.ACTIVE)).willReturn(Optional.of(post));
        given(likeRepository.findByMemberAndPost(member, post)).willReturn(Optional.of(like));

        // when
        likeService.unlike(member.getEmail(), 1L);

        // then
        verify(likeRepository).delete(like);
    }

    @Test
    @DisplayName("좋아요 취소 실패 - 좋아요를 누르지 않은 게시글")
    void unlike_fail_notFound() {
        // given
        given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
        given(postRepository.findByIdAndStatus(1L, PostStatus.ACTIVE)).willReturn(Optional.of(post));
        given(likeRepository.findByMemberAndPost(member, post)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.unlike(member.getEmail(), 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.LIKE_NOT_FOUND.getMessage());
    }
}
