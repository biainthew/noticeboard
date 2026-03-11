package aib.noticeboard.repository;

import aib.noticeboard.domain.entity.Post;
import aib.noticeboard.domain.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);
    Optional<Post> findByIdAndStatus(Long id, PostStatus status);
}
