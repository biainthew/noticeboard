package aib.noticeboard.controller;

import aib.noticeboard.config.SecurityConfig;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.security.JwtTokenProvider;
import aib.noticeboard.service.MemberService;
import aib.noticeboard.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private MemberService memberService;
    @MockitoBean private PasswordResetService passwordResetService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /api/auth/password/forgot - 정상 요청 시 200")
    void forgot_success() throws Exception {
        String body = """
                { "email": "user@test.com" }
                """;

        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(passwordResetService).forgot("user@test.com");
    }

    @Test
    @DisplayName("POST /api/auth/password/forgot - 잘못된 이메일 형식이면 400")
    void forgot_invalidEmail() throws Exception {
        String body = """
                { "email": "not-an-email" }
                """;

        mockMvc.perform(post("/api/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/password/reset - 정상 요청 시 200")
    void reset_success() throws Exception {
        String body = """
                { "token": "abc123", "newPassword": "newpass1234" }
                """;

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(passwordResetService).reset("abc123", "newpass1234");
    }

    @Test
    @DisplayName("POST /api/auth/password/reset - 잘못된 토큰이면 400")
    void reset_invalidToken() throws Exception {
        doThrow(new CustomException(ErrorCode.INVALID_PASSWORD_RESET_TOKEN))
                .when(passwordResetService).reset(any(), any());

        String body = """
                { "token": "bad", "newPassword": "newpass1234" }
                """;

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_PASSWORD_RESET_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("POST /api/auth/password/reset - 짧은 비밀번호면 400")
    void reset_shortPassword() throws Exception {
        String body = """
                { "token": "abc123", "newPassword": "short" }
                """;

        mockMvc.perform(post("/api/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
