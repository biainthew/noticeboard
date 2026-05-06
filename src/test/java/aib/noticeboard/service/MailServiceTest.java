package aib.noticeboard.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @InjectMocks
    private MailService mailService;

    @Mock
    private JavaMailSender mailSender;

    @Test
    @DisplayName("비밀번호 재설정 메일 발송 시 JavaMailSender.send 가 호출된다")
    void sendPasswordResetMail_callsSend() {
        ReflectionTestUtils.setField(mailService, "from", "noreply@test.com");
        ReflectionTestUtils.setField(mailService, "fromName", "Communeio");
        MimeMessage mime = Mockito.mock(MimeMessage.class);
        given(mailSender.createMimeMessage()).willReturn(mime);

        mailService.sendPasswordResetMail("user@test.com", "https://example.com/reset?token=abc");

        verify(mailSender).send(mime);
    }

    @Test
    @DisplayName("메일 발송 실패 시 예외를 swallow 한다")
    void sendPasswordResetMail_swallowsException() {
        ReflectionTestUtils.setField(mailService, "from", "noreply@test.com");
        ReflectionTestUtils.setField(mailService, "fromName", "Communeio");
        MimeMessage mime = Mockito.mock(MimeMessage.class);
        given(mailSender.createMimeMessage()).willReturn(mime);
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() ->
                mailService.sendPasswordResetMail("user@test.com", "https://example.com/reset?token=abc")
        ).doesNotThrowAnyException();
    }
}
