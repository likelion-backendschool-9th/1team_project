package com.dipsource.entity.coin;

/**
 * 칩 잔액 부족 시 던지는 도메인 예외.
 * GlobalExceptionHandler에서 400으로 매핑됨
 */
public class InsufficientChipsException extends RuntimeException {

    public InsufficientChipsException(int required, int actual) {
        super("칩이 부족해요. 필요: " + required + "개, 보유: " + actual + "개");
    }
}
