package aib.noticeboard.service;

import aib.noticeboard.domain.entity.Member;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final MemberRepository memberRepository;
    private final PasswordResetTokenStore tokenStore;
    private final MailService mailService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void forgot(String email) {
        memberRepository.findByEmail(email).ifPresent(member -> {
            if (tokenStore.isOnCooldown(email)) return;
            String token = tokenStore.issue(email);
            mailService.sendPasswordResetMail(email, frontendUrl + "/reset-password?token=" + token);
        });
    }

    @Transactional
    public void reset(String token, String newPassword) {
        String email = tokenStore.findEmailByToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_PASSWORD_RESET_TOKEN));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        member.updatePassword(passwordEncoder.encode(newPassword));

        refreshTokenService.delete(email);
        tokenStore.invalidate(token, email);
    }
}
