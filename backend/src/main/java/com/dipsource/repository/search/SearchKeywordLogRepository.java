package com.dipsource.repository.search;

import com.dipsource.entity.search.SearchKeywordLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** search_keyword_logs 테이블 접근. */
public interface SearchKeywordLogRepository extends JpaRepository<SearchKeywordLog, Long> {

    boolean existsByUserIdAndKeywordAndSearchedAtGreaterThanEqual(Long userId, String keyword, LocalDateTime searchedAt);

    /** keyword, 횟수, 마지막 검색 시각. 정렬은 서비스에서 한다. */
    @Query("""
            SELECT l.keyword, COUNT(l), MAX(l.searchedAt)
            FROM SearchKeywordLog l
            WHERE l.searchedAt >= :since
            GROUP BY l.keyword
            """)
    List<Object[]> countSince(@Param("since") LocalDateTime since);
}
