package aib.noticeboard.repository;

import aib.noticeboard.domain.entity.Comment;
import aib.noticeboard.domain.entity.Post;
import aib.noticeboard.domain.enums.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostAndStatusAndParentIsNull(Post post, CommentStatus status);
}
