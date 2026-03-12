package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.entity.Post;
import aib.noticeboard.domain.enums.PostStatus;
import aib.noticeboard.dto.request.PostRequestDto;
import aib.noticeboard.dto.response.PostResponseDto;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
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

        return new PostResponseDto.Detail(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto.Summary> getList (Pageable pageable) {
        return postRepository.findAllByStatus(PostStatus.ACTIVE, pageable)
                .map(PostResponseDto.Summary::new);
    }

    @Transactional
    public PostResponseDto.Detail getDetail (Long postId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        viewCountService.increaseViewCount(postId);

        int redisViewCount = post.getViewCount() + viewCountService.getViewCount(postId);

        return new PostResponseDto.Detail(post, redisViewCount);
    }

    @Transactional
    public PostResponseDto.Detail update (String email, Long postId, PostRequestDto.Update request) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getMember().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.POST_UNAUTHORIZED);
        }

        post.update(request.getTitle(), request.getContent());

        return new PostResponseDto.Detail(post);
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
