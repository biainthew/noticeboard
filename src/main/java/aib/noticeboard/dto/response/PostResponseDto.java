package aib.noticeboard.dto.response;

import aib.noticeboard.domain.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class PostResponseDto {

    @Getter
    @Builder
    public static class Summary {
        private final Long id;
        private final String title;
        private final String nickname;
        private final int viewCount;
        private final int likeCount;
        private final boolean liked;
        private final LocalDateTime createdAt;

        public static Summary from(Post post, boolean liked) {
            return Summary.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .nickname(post.getMember().getNickname())
                    .viewCount(post.getViewCount())
                    .likeCount(post.getLikeCount())
                    .liked(liked)
                    .createdAt(post.getCreatedAt())
                    .build();
        }
    }

    @Getter
    public static class Detail {
        private final Long id;
        private final String title;
        private final String content;
        private final String nickname;
        private final String email;
        private final int viewCount;
        private final int likeCount;
        private final boolean liked;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        public Detail(Post post) {
            this.id = post.getId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.nickname = post.getMember().getNickname();
            this.email = post.getMember().getEmail();
            this.viewCount = post.getViewCount();
            this.likeCount = post.getLikeCount();
            this.liked = false;
            this.createdAt = post.getCreatedAt();
            this.updatedAt = post.getUpdatedAt();
        }

        public Detail(Post post, int viewCount, boolean liked) {
            this.id = post.getId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.nickname = post.getMember().getNickname();
            this.email = post.getMember().getEmail();
            this.viewCount = viewCount;
            this.likeCount = post.getLikeCount();
            this.liked = liked;
            this.createdAt = post.getCreatedAt();
            this.updatedAt = post.getUpdatedAt();
        }

        // 테스트 코드용 생성자
        @Builder
        public Detail(Long id, String title, String content, String nickname,
                      int viewCount, int likeCount, boolean liked,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.nickname = nickname;
            this.email = "test@test.com";
            this.viewCount = viewCount;
            this.likeCount = likeCount;
            this.liked = liked;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }
}
