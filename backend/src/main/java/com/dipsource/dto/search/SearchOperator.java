package com.dipsource.dto.search;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.util.Arrays;

/**
 * 검색어가 여러 단어일 때 단어끼리 묶는 방식. API의 op 파라미터 값과 1:1로 대응한다.
 * 예: "spring jpa"
 * - and: spring과 jpa가 모두 들어 있는 결과
 * - or: spring이나 jpa 중 하나라도 들어 있는 결과
 */
public enum SearchOperator {
    AND("and"),
    OR("or");

    private final String value;

    SearchOperator(String value) {
        this.value = value;
    }

    /** op를 보내지 않으면 and다. */
    public static SearchOperator from(String value) {
        if (value == null) {
            return AND;
        }
        return Arrays.stream(values())
                .filter(op -> op.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST,
                        "지원하지 않는 검색 방식이에요. op는 and, or 중 하나예요"));
    }
}
