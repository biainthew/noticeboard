package aib.noticeboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60L; // 7일

    public void save(String email, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + email,
                refreshToken,
                REFRESH_TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );
    }

    public String get(String email) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + email);
    }

    public void delete(String email) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + email);
    }

    public boolean isValid(String email, String refreshToken) {
        String saved = get(email);
        return saved != null && saved.equals(refreshToken);
    }
}
