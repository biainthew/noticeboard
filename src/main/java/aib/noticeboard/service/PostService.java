package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.entity.Post;
import aib.noticeboard.domain.enums.CommentStatus;
import aib.noticeboard.domain.enums.PostStatus;
import aib.noticeboard.dto.request.PostRequestDto;
import aib.noticeboard.dto.response.PostResponseDto;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.repository.CommentRepository;
import aib.noticeboard.repository.LikeRepository;
import aib.noticeboard.repository.MemberRepository;
import aib.noticeboard.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ViewCountService viewCountService;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public PostResponseDto.Detail create (String email, PostRequestDto.Create request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = Post.builder()
                .member(member)
                .title(request.getTitle())
                .content(request.getContent())
                .status(PostStatus.ACTIVE)
                .build();

        return new PostResponseDto.Detail(postRepository.save(post), 0);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto.Summary> getList(Pageable pageable, String email) {
        Page<Post> posts = postRepository.findAllByStatus(PostStatus.ACTIVE, pageable);

        Member member = (email != null) ? memberRepository.findByEmail(email).orElse(null) : null;

        return posts.map(post -> {
            boolean liked = member != null && likeRepository.existsByMemberAndPost(member, post);
            int commentCount = (int) commentRepository.countByPostAndStatusAndParentIsNull(post, CommentStatus.ACTIVE);
            return PostResponseDto.Summary.from(post, liked, commentCount);
        });
    }

    @Transactional
    public PostResponseDto.Detail getDetail (Long postId, String email) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        viewCountService.increaseViewCount(postId);

        int redisViewCount = post.getViewCount() + viewCountService.getViewCount(postId);

        boolean liked = false;
        if (email != null) {
            Member member = memberRepository.findByEmail(email)
                    .orElse(null);
            if (member != null) {
                liked = likeRepository.existsByMemberAndPost(member, post);
            }
        }

        int commentCount = (int) commentRepository.countByPostAndStatusAndParentIsNull(post, CommentStatus.ACTIVE);

        return new PostResponseDto.Detail(post, redisViewCount, liked, commentCount);
    }

    @Transactional
    public PostResponseDto.Detail update (String email, Long postId, PostRequestDto.Update request) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getMember().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.POST_UNAUTHORIZED);
        }

        post.update(request.getTitle(), request.getContent());

        int commentCount = (int) commentRepository.countByPostAndStatusAndParentIsNull(post, CommentStatus.ACTIVE);

        return new PostResponseDto.Detail(post, commentCount);
    }

    @Transactional
    public void delete(String email, Long postId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getMember().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.POST_UNAUTHORIZED);
        }

        post.delete();
    }
}
