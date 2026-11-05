package com.dipsource.repository.tag;

import com.dipsource.dto.search.SearchTermCandidate;
import com.dipsource.entity.tag.Tag;
import com.dipsource.global.common.LikePattern;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * 통합 검색에서 쓰는 태그 조회. 태그 CRUD용 리포지토리와 섞이지 않도록 따로 둔다.
 */
public interface TagSearchRepository extends Repository<Tag, Long> {

    /**
     * 3단계: 오타 교정 후보. 태그 이름에서 검색 단어와 가까운 단어 두 개(가까운 순).
     * 태그는 이름만 있고 공개 범위가 없어서 모두 사전에 넣는다. "Spring Boot"처럼 띄어 쓴 태그는 단어로 나눈다.
     * 쿼리 설명은 {@code PostSearchRepository#findClosestTitleTerms}과 같다.
     *
     * @param word        소문자 검색 단어
     * @param maxDistance 허용하는 최대 편집 거리
     */
    default List<SearchTermCandidate> findClosestTerms(String word, int maxDistance) {
        return findClosestNameTerms(word, LikePattern.contains(word), maxDistance);
    }

    @Query(nativeQuery = true, value = """
            SELECT w.term AS term,
                   w.term LIKE :pattern AS contained,
                   levenshtein(:word, w.term) AS distance,
                   w.frequency AS frequency,
                   CAST(similarity(:word, w.term) AS double precision) AS similarity
            FROM (
                SELECT s.term, count(*) AS frequency
                FROM tags t
                CROSS JOIN LATERAL regexp_split_to_table(lower(t.name), '[^a-z0-9가-힣_.#+-]+') AS s(term)
                GROUP BY s.term
            ) w
            WHERE length(w.term) BETWEEN 1 AND 50
              AND (w.term LIKE :pattern
                   OR (abs(length(w.term) - length(:word)) <= :maxDistance
                       AND levenshtein_less_equal(:word, w.term, :maxDistance) <= :maxDistance))
            ORDER BY contained DESC, distance, frequency DESC, similarity DESC, term
            LIMIT 2
            """)
    List<SearchTermCandidate> findClosestNameTerms(
            @Param("word") String word,
            @Param("pattern") String pattern,
            @Param("maxDistance") int maxDistance);
}
