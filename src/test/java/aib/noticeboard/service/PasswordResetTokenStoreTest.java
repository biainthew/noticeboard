package aib.noticeboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenStoreTest {

    @InjectMocks
    private PasswordResetTokenStore store;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    @DisplayName("신규 토큰 저장 시 3종 키 모두 set 된다")
    void issue_setsAllThreeKeys() {
        String token = store.issue("user@test.com");

        assertThat(token).isNotBlank();
        verify(valueOps).set(eq("pwreset:token:" + token), eq("user@test.com"), eq(3600L), eq(TimeUnit.SECONDS));
        verify(valueOps).set(eq("pwreset:latest:user@test.com"), eq(token), eq(3600L), eq(TimeUnit.SECONDS));
        verify(valueOps).set(eq("pwreset:cooldown:user@test.com"), eq("1"), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("이전 토큰이 있으면 새 토큰 발급 시 이전 토큰 키가 삭제된다")
    void issue_deletesPreviousToken() {
        given(valueOps.get("pwreset:latest:user@test.com")).willReturn("oldToken");

        store.issue("user@test.com");

        verify(redisTemplate).delete(eq("pwreset:token:oldToken"));
    }

    @Test
    @DisplayName("쿨다운 키가 존재하면 isOnCooldown 은 true")
    void isOnCooldown_true() {
        given(redisTemplate.hasKey("pwreset:cooldown:user@test.com")).willReturn(true);
        assertThat(store.isOnCooldown("user@test.com")).isTrue();
    }

    @Test
    @DisplayName("쿨다운 키가 없으면 isOnCooldown 은 false")
    void isOnCooldown_false() {
        given(redisTemplate.hasKey("pwreset:cooldown:user@test.com")).willReturn(false);
        assertThat(store.isOnCooldown("user@test.com")).isFalse();
    }

    @Test
    @DisplayName("토큰으로 이메일을 조회한다")
    void findEmailByToken_returnsEmail() {
        given(valueOps.get("pwreset:token:abc")).willReturn("user@test.com");
        assertThat(store.findEmailByToken("abc")).contains("user@test.com");
    }

    @Test
    @DisplayName("invalidate 호출 시 토큰 키와 latest 키가 삭제된다")
    void invalidate_deletesTokenAndLatest() {
        store.invalidate("abc", "user@test.com");

        verify(redisTemplate).delete("pwreset:token:abc");
        verify(redisTemplate).delete("pwreset:latest:user@test.com");
    }
}
