package aib.noticeboard.dto.response;

import aib.noticeboard.domain.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommentResponseDto {

    @Getter
    public static class Detail {
        private final Long id;
        private final String content;
        private final String nickname;
        private final Long parentId;
        private final List<Detail> children;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        public Detail(Comment comment) {
            this.id = comment.getId();
            this.content = comment.getContent();
            this.nickname = comment.getMember().getNickname();
            this.parentId = comment.getParent() != null ? comment.getParent().getId() : null;
            this.children = comment.getChildren() != null
                    ? comment.getChildren().stream()
                    .map(Detail::new)
                    .collect(Collectors.toList())
                    : List.of();
            this.createdAt = comment.getCreatedAt();
            this.updatedAt = comment.getUpdatedAt();
        }
    }
}
