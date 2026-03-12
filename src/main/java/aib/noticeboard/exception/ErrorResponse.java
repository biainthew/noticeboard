package aib.noticeboard.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ErrorResponse {
    private final String message;
    private final int status;
    private final Map<String, String> errors;

    public ErrorResponse(ErrorCode errorCode) {
        this.message = errorCode.getMessage();
        this.status = errorCode.getStatus().value();
        this.errors = null;
    }

    public ErrorResponse(ErrorCode errorCode, Map<String, String> errors) {
        this.message = errorCode.getMessage();
        this.status = errorCode.getStatus().value();
        this.errors = errors;
    }
}
