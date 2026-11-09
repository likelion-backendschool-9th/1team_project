package com.dipsource.global.exception;

import com.dipsource.entity.coin.InsufficientChipsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** 예외를 공통 에러 JSON으로 변환한다. */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
                ErrorCode code = ex.getErrorCode();
                return ResponseEntity.status(code.getStatus())
                                .body(ErrorResponse.of(code.getCode(), ex.getMessage()));
        }

        /** 칩 잔액 부족 → 400 */
        @ExceptionHandler(InsufficientChipsException.class)
        public ResponseEntity<ErrorResponse> handleInsufficientChips(InsufficientChipsException ex) {
                return ResponseEntity.badRequest()
                                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
                String message = ex.getBindingResult().getFieldErrors().stream()
                                .findFirst()
                                .map(err -> err.getDefaultMessage() == null ? ErrorCode.BAD_REQUEST.getDefaultMessage()
                                                : err.getDefaultMessage())
                                .orElse(ErrorCode.BAD_REQUEST.getDefaultMessage());
                return ResponseEntity.badRequest()
                                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST.getCode(), message));
        }


        /** JSON 파싱 실패, 파라미터 타입 불일치, 필수 파라미터 누락 → 400 */
        @ExceptionHandler({
                HttpMessageNotReadableException.class,
                MethodArgumentTypeMismatchException.class,
                MissingServletRequestParameterException.class
        })
        public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
                return ResponseEntity.badRequest()
                                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getDefaultMessage()));
        }

        /** 위에서 잡지 못한 모든 예외 → 500 반환. 스택 트레이스는 서버 로그에만 기록한다. */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
                log.error("예상치 못한 에러 발생", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of("INTERNAL_ERROR", "서버 에러가 발생했습니다"));
        }
}
