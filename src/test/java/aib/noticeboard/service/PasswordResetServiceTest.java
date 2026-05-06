package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.domain.enums.MemberRole;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Mock private MemberRepository memberRepository;
    @Mock private PasswordResetTokenStore tokenStore;
    @Mock private MailService mailService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private PasswordEncoder passwordEncoder;

    private Member member;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:5173");
        member = Member.builder()
                .email("user@test.com")
                .password("encodedOld")
                .nickname("유저")
                .role(MemberRole.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("forgot - 회원이 존재하지 않으면 메일 발송하지 않고 정상 종료")
    void forgot_memberNotFound_silentNoOp() {
        given(memberRepository.findByEmail("ghost@test.com")).willReturn(Optional.empty());

        passwordResetService.forgot("ghost@test.com");

        verify(tokenStore, never()).issue(anyString());
        verify(mailService, never()).sendPasswordResetMail(anyString(), anyString());
    }

    @Test
    @DisplayName("forgot - 쿨다운 중이면 메일 발송하지 않음")
    void forgot_onCooldown_skipsSend() {
        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.of(member));
        given(tokenStore.isOnCooldown("user@test.com")).willReturn(true);

        passwordResetService.forgot("user@test.com");

        verify(tokenStore, never()).issue(anyString());
        verify(mailService, never()).sendPasswordResetMail(anyString(), anyString());
    }

    @Test
    @DisplayName("forgot - 정상 흐름: 토큰 발급 후 재설정 링크가 담긴 메일 발송")
    void forgot_success_sendsMailWithLink() {
        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.of(member));
        given(tokenStore.isOnCooldown("user@test.com")).willReturn(false);
        given(tokenStore.issue("user@test.com")).willReturn("token123");

        passwordResetService.forgot("user@test.com");

        verify(mailService).sendPasswordResetMail(
                eq("user@test.com"),
                eq("http://localhost:5173/reset-password?token=token123")
        );
    }

    @Test
    @DisplayName("reset - 잘못된 토큰이면 INVALID_PASSWORD_RESET_TOKEN")
    void reset_invalidToken_throws() {
        given(tokenStore.findEmailByToken("bad")).willReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.reset("bad", "newpass1234"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_PASSWORD_RESET_TOKEN.getMessage());

        verify(refreshTokenService, never()).delete(anyString());
        verify(tokenStore, never()).invalidate(anyString(), anyString());
    }

    @Test
    @DisplayName("reset - 정상 흐름: 비밀번호 인코딩 / RefreshToken 삭제 / 토큰 무효화")
    void reset_success_invalidatesTokenAndRefresh() {
        given(tokenStore.findEmailByToken("token123")).willReturn(Optional.of("user@test.com"));
        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.of(member));
        given(passwordEncoder.encode("newpass1234")).willReturn("encodedNew");

        passwordResetService.reset("token123", "newpass1234");

        verify(passwordEncoder).encode("newpass1234");
        verify(refreshTokenService).delete("user@test.com");
        verify(tokenStore).invalidate("token123", "user@test.com");
    }

    @Test
    @DisplayName("reset - 토큰은 유효한데 그 사이 회원 탈퇴된 경우 MEMBER_NOT_FOUND")
    void reset_memberDeleted_throws() {
        given(tokenStore.findEmailByToken("token123")).willReturn(Optional.of("user@test.com"));
        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.reset("token123", "newpass1234"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
