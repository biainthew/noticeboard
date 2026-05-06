package aib.noticeboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenStore {

    private static final String TOKEN_PREFIX    = "pwreset:token:";
    private static final String LATEST_PREFIX   = "pwreset:latest:";
    private static final String COOLDOWN_PREFIX = "pwreset:cooldown:";

    private static final long TOKEN_TTL_SECONDS    = 3600L;
    private static final long COOLDOWN_TTL_SECONDS = 60L;

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String issue(String email) {
        String prevToken = redisTemplate.opsForValue().get(LATEST_PREFIX + email);
        if (prevToken != null) {
            redisTemplate.delete(TOKEN_PREFIX + prevToken);
        }

        String token = generateToken();
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, email, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(LATEST_PREFIX + email, token, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(COOLDOWN_PREFIX + email, "1", COOLDOWN_TTL_SECONDS, TimeUnit.SECONDS);
        return token;
    }

    public boolean isOnCooldown(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(COOLDOWN_PREFIX + email));
    }

    public Optional<String> findEmailByToken(String token) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(TOKEN_PREFIX + token));
    }

    public void invalidate(String token, String email) {
        redisTemplate.delete(TOKEN_PREFIX + token);
        redisTemplate.delete(LATEST_PREFIX + email);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
