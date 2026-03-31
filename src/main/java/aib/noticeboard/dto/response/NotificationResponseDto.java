package aib.noticeboard.dto.response;

import aib.noticeboard.domain.entity.Notification;
import lombok.Getter;

import java.time.LocalDateTime;

public class NotificationResponseDto {

    @Getter
    public static class Detail {
        private final Long id;
        private final String senderNickname;
        private final String type;
        private final Long postId;
        private final String postTitle;
        private final String commentContent;
        private final boolean isRead;
        private final LocalDateTime createdAt;

        public Detail(Notification notification) {
            this.id = notification.getId();
            this.senderNickname = notification.getSender().getNickname();
            this.type = notification.getType().name();
            this.postId = notification.getPost().getId();
            this.postTitle = notification.getPost().getTitle();
            this.commentContent = notification.getComment() != null
                    ? notification.getComment().getContent()
                    : null;
            this.isRead = notification.isRead();
            this.createdAt = notification.getCreatedAt();
        }
    }
}
