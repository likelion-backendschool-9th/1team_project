package com.dipsource.global.common;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** 목록 응답 공통 형식. { content, page, size, totalElements, totalPages } */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return of(page, page.getContent().stream().map(mapper).toList());
    }

    /** 내용을 따로 가공했을 때(정렬 뒤집기 등) 쓴다. */
    public static <T> PageResponse<T> of(Page<?> page, List<T> content) {
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
