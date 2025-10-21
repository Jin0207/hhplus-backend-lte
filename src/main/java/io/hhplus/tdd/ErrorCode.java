package io.hhplus.tdd;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //Point 관련 예외처리
    CHARGE_LESS_THAN_ZERO("E001", "충전 금액은 0보다 작을 수 없습니다.", HttpStatus.BAD_REQUEST),
    
    // 500에러
    INTERNAL_SERVER_ERROR("G999", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}