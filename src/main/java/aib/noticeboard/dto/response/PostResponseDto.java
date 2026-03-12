package aib.noticeboard.dto.response;

import aib.noticeboard.domain.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class PostResponseDto {

    @Getter
    public static class Summary {
        private final Long id;
        private final String title;
        private final String nickname;
        private final int viewCount;
        private final int likeCount;
        private final LocalDateTime createdAt;

        public Summary(Post post) {
            this.id = post.getId();
            this.title = post.getTitle();
            this.nickname = post.getMember().getNickname();
            this.viewCount = post.getViewCount();
            this.likeCount = post.getLikeCount();
            this.createdAt = post.getCreatedAt();
        }
    }

    @Getter
    public static class Detail {
        private final Long id;
        private final String title;
        private final String content;
        private final String nickname;
        private final int viewCount;
        private final int likeCount;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        public Detail(Post post) {
            this.id = post.getId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.nickname = post.getMember().getNickname();
            this.viewCount = post.getViewCount();
            this.likeCount = post.getLikeCount();
            this.createdAt = post.getCreatedAt();
            this.updatedAt = post.getUpdatedAt();
        }

        public Detail(Post post, int viewCount) {
            this.id = post.getId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.nickname = post.getMember().getNickname();
            this.viewCount = viewCount;
            this.likeCount = post.getLikeCount();
            this.createdAt = post.getCreatedAt();
            this.updatedAt = post.getUpdatedAt();
        }

        // 테스트 코드용 생성자
        @Builder
        public Detail(Long id, String title, String content, String nickname,
                      int viewCount, int likeCount,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.nickname = nickname;
            this.viewCount = viewCount;
            this.likeCount = likeCount;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }
}
