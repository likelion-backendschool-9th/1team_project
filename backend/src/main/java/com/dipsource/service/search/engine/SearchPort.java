package com.dipsource.service.search.engine;

import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.dto.search.SearchType;
import com.dipsource.service.search.query.ParsedQuery;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 후보 검색 경계. 지금은 Elasticsearch BM25 구현 하나지만, 벡터·하이브리드 검색은 이 인터페이스의 구현으로 붙인다.
 * 랭킹·권한·응답 조립은 이 경계 밖에 있어서 검색 기술이 바뀌어도 그대로다.
 */
public interface SearchPort {

    /**
     * @param excludedIds     결과에서 뺄 문서 ID(사용자가 숨긴 결과)
     * @param excludedAuthors 결과에서 뺄 작성자 ID(차단 관계). 사용자 검색에서는 사용자 ID 자체를 뺀다
     * @param createdBefore   이 시각 이후에 만들어진 문서를 뺀다. 페이지를 넘기는 동안 새 글로 순서가 밀리지 않게 한다. 없으면 null
     */
    record Request(SearchType type, ParsedQuery query, SearchOperator operator, SearchSort sort, int from, int size,
                   Collection<Long> excludedIds, Collection<Long> excludedAuthors, LocalDateTime createdBefore) {
    }

    /**
     * @param score      검색어 관련도. 검색어마다 범위가 달라서 절대값으로 비교하면 안 된다
     * @param exactMatch 제목·닉네임 전체가 검색어와 같다
     */
    record Hit(long id, double score, boolean exactMatch) {
    }

    record Result(List<Hit> hits, long total) {
    }

    Result retrieve(Request request);
}
