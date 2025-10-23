package io.hhplus.tdd;

import lombok.Getter;

@Getter
public class ErrorException extends RuntimeException {

    private final ErrorCode errorCode;

    // 기본 메시지
    public ErrorException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 추가 메시지
    public ErrorException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}