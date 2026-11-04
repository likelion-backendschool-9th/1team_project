package com.dipsource.dto.search;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.util.Arrays;

/** 검색 결과 정렬. API의 sort 파라미터 값과 1:1로 대응한다. */
public enum SearchSort {
    /** 관련도순. 점수가 같으면 최신순 */
    RELEVANCE("relevance"),
    /** 최신순 */
    LATEST("latest");

    private final String value;

    SearchSort(String value) {
        this.value = value;
    }

    /** sort를 보내지 않으면 관련도순이다. */
    public static SearchSort from(String value) {
        if (value == null) {
            return RELEVANCE;
        }
        return Arrays.stream(values())
                .filter(sort -> sort.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST,
                        "지원하지 않는 정렬이에요. sort는 relevance, latest 중 하나예요"));
    }
}
