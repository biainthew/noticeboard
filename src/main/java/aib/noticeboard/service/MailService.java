package aib.noticeboard.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Async
    public void sendPasswordResetMail(String to, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(from, fromName);
            helper.setTo(to);
            helper.setSubject("[Communeio] 비밀번호 재설정 안내");
            helper.setText(buildHtmlBody(resetLink), true);

            mailSender.send(message);
            log.info("비밀번호 재설정 메일 발송 완료: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("비밀번호 재설정 메일 빌드 실패: to={}, msg={}", to, e.getMessage());
        } catch (Exception e) {
            log.error("비밀번호 재설정 메일 발송 실패: to={}, msg={}", to, e.getMessage());
        }
    }

    private String buildHtmlBody(String resetLink) {
        return """
                <div style="font-family: 'Apple SD Gothic Neo', sans-serif; max-width: 480px; margin: 0 auto; padding: 32px 24px; color: #222;">
                  <h2 style="margin: 0 0 16px;">비밀번호 재설정 안내</h2>
                  <p style="line-height: 1.6;">아래 버튼을 클릭하여 새 비밀번호를 설정해주세요.</p>
                  <p style="text-align: center; margin: 32px 0;">
                    <a href="%s" style="display: inline-block; padding: 12px 24px; background: #2b6cb0; color: #fff; text-decoration: none; border-radius: 6px;">비밀번호 재설정</a>
                  </p>
                  <p style="font-size: 13px; color: #666; line-height: 1.6;">이 링크는 발송 시점으로부터 1시간 후 만료됩니다.<br/>본인이 요청하지 않았다면 이 메일을 무시해주세요.</p>
                  <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;"/>
                  <p style="font-size: 11px; color: #999;">본 메일은 자동 발송된 메일이므로 회신할 수 없습니다.</p>
                </div>
                """.formatted(resetLink);
    }
}
