package aib.noticeboard.repository;

import aib.noticeboard.domain.entity.Like;
import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByMemberAndPost(Member member, Post post);
    boolean existsByMemberAndPost(Member member, Post post);
}
