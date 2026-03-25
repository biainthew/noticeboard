package aib.noticeboard.controller;

import aib.noticeboard.config.SecurityConfig;
import aib.noticeboard.dto.request.PostRequestDto;
import aib.noticeboard.dto.response.PostResponseDto;
import aib.noticeboard.exception.CustomException;
import aib.noticeboard.exception.ErrorCode;
import aib.noticeboard.security.JwtTokenProvider;
import aib.noticeboard.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = PostController.class)
@Import(SecurityConfig.class)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ErrorCode errorCode;

    @Test
    @DisplayName("게시글 작성 성공")
    @WithMockUser(username = "lba0507@gmail.com")
    void create_success() throws Exception {
        // given
        PostRequestDto.Create request = new PostRequestDto.Create();
        request.setTitle("테스트 제목");
        request.setContent("테스트 내용");
        PostResponseDto.Detail response = PostResponseDto.Detail.builder()
                .id(1L)
                .title("테스트 제목")
                .content("테스트 내용")
                .nickname("테스터")
                .viewCount(0)
                .likeCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postService.create(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                        .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("테스트 제목"));
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    @WithMockUser
    void getList_success() throws Exception {
        // given
        given(postService.getList(any(Pageable.class))).willReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    @WithMockUser
    void getDetail_success() throws Exception {
        // given
        PostResponseDto.Detail response = PostResponseDto.Detail.builder()
                .id(1L)
                .title("테스트 제목")
                .content("테스트 내용")
                .nickname("테스터")
                .viewCount(0)
                .likeCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        given(postService.getDetail(1L, "lba0507@gmail.com")).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 제목"));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    @WithMockUser
    void delete_fail_unauthorized() throws Exception {
        // given
        doThrow(new CustomException(ErrorCode.POST_UNAUTHORIZED))
                .when(postService).delete(any(), any());

        // when & then
        mockMvc.perform(delete("/api/posts/1").with(csrf()))
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(CustomException.class));
    }
}
