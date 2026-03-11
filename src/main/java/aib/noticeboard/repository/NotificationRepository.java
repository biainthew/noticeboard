package aib.noticeboard.repository;

import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByReceiverOrderByCreatedAtDesc(Member receiver);
    long countByReceiverAndIsReadFalse(Member receiver);
}
