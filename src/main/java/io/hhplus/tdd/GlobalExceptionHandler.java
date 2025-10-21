package io.hhplus.tdd;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(ErrorException ex) {
        
        ErrorCode errorCode = ex.getErrorCode();

        ErrorResponse response = new ErrorResponse(
            errorCode.getCode(), 
            ex.getMessage()
        );
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }
    
    /**
     * 예상치 못한 모든 예외를 처리하는 핸들러.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        
        ErrorResponse response = new ErrorResponse(
            errorCode.getCode(), 
            errorCode.getMessage()
        );
        
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }
}