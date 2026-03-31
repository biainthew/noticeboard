package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Comment;
import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.entity.Post;
import aib.noticeboard.domain.enums.CommentStatus;
import aib.noticeboard.domain.enums.NotificationType;
import aib.noticeboard.domain.enums.PostStatus;
import aib.noticeboard.dto.request.CommentRequestDto;
import aib.noticeboard.dto.response.CommentResponseDto;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.repository.CommentRepository;
import aib.noticeboard.repository.MemberRepository;
import aib.noticeboard.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponseDto.Detail create(String email, Long postId, CommentRequestDto.Create request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        }

        Comment comment = Comment.builder()
                .member(member)
                .post(post)
                .parent(parent)
                .content(request.getContent())
                .status(CommentStatus.ACTIVE)
                .build();

        Comment saved = commentRepository.save(comment);

        // 게시글 작성자에게 알림 전송
        notificationService.send(post.getMember(), member, post, comment, NotificationType.COMMENT);

        return new CommentResponseDto.Detail(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto.Detail> getList(Long postId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return commentRepository.findAllByPostAndStatusAndParentIsNull(post, CommentStatus.ACTIVE)
                .stream()
                .map(CommentResponseDto.Detail::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDto.Detail update(String email, Long commentId, CommentRequestDto.Update request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMember().getEmail().equals(email)) {
           throw new CustomException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.update(request.getContent());

        return new CommentResponseDto.Detail(comment);
    }

    @Transactional
    public void delete(String email, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMember().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.delete();
    }
}
