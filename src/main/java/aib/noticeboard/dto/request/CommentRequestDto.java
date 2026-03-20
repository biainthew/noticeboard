package aib.noticeboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class CommentRequestDto {

    @Getter
    @Setter
    public static class Create {
        @NotBlank(message = "내용을 입력해 주세요.")
        private String content;

        private Long parentId;
    }

    @Getter
    @Setter
    public static class Update {
        @NotBlank(message = "내용을 입력해 주세요.")
        private String content;
    }
}
