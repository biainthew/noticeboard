package aib.noticeboard.dto.response;

import aib.noticeboard.domain.entity.Member;
import lombok.Getter;

public class MemberResponseDto {

    @Getter
    public static class Info {
        private final Long id;
        private final String email;
        private final String nickname;

        public Info(Member member) {
            this.id = member.getId();
            this.email = member.getEmail();
            this.nickname = member.getNickname();
        }
    }

    @Getter
    public static class Token {
        private final String accessToken;
        private final String refreshToken;

        public Token(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}
