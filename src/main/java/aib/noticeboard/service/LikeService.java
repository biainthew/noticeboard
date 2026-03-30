package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Like;
import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.entity.Post;
import aib.noticeboard.domain.enums.NotificationType;
import aib.noticeboard.domain.enums.PostStatus;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.repository.LikeRepository;
import aib.noticeboard.repository.MemberRepository;
import aib.noticeboard.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Transactional
    public void like(String email, Long postId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (likeRepository.existsByMemberAndPost(member, post)) {
            throw new CustomException(ErrorCode.LIKE_ALREADY_EXISTS);
        }

        likeRepository.save(Like.builder()
                .member(member)
                .post(post)
                .build());

        post.increaseLikeCount();

        // 게시글 작성자에게 알림 전송
        notificationService.send(post.getMember(), member, post, NotificationType.LIKE);
    }

    @Transactional
    public void unlike(String email, Long postId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Like like = likeRepository.findByMemberAndPost(member, post)
                .orElseThrow(() -> new CustomException(ErrorCode.LIKE_NOT_FOUND));

        likeRepository.delete(like);
        post.decreaseLikeCount();
    }
}
