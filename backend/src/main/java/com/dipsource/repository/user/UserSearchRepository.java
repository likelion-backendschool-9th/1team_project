package com.dipsource.repository.user;

import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchOperator;
import com.dipsource.dto.search.SearchSort;
import com.dipsource.entity.user.User;
import com.dipsource.global.common.LikePattern;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * 사람 검색 전용 조회. 검색 방식을 바꿀 때는 이 파일만 바꾼다.
 */
public interface UserSearchRepository extends Repository<User, Long> {

    /** 2단계: 단어별 LIKE + 관련도 점수 (PostgreSQL native SQL) */
    default Page<User> search(SearchCondition condition, Pageable pageable) {
        return searchByLikePatterns(
                condition.keywords().stream().map(LikePattern::contains).toArray(String[]::new),
                condition.phrase().toLowerCase(Locale.ROOT),
                LikePattern.contains(condition.phrase().toLowerCase(Locale.ROOT)),
                condition.operator() == SearchOperator.AND,
                condition.sort() == SearchSort.RELEVANCE,
                condition.viewerId(),
                pageable);
    }

    /**
     * 활동 중(ACTIVE)인 회원 중 닉네임, 자기소개, 기술 스택에 단어가 들어 있는 회원. 최신순은 최근 가입순이다.
     * 정지·탈퇴 회원은 사람 검색에 나오지 않는다.
     * 기술 스택은 jsonb 배열을 문자열로 바꿔 비교한다(1단계부터 쓰는 임시 방식).
     * 관련도 점수 = 닉네임이 검색어와 같음 100 + 닉네임에 검색어 전체 30 + 닉네임 단어당 10
     * + 자기소개·기술 스택 단어당 3.
     * 파라미터와 m(LATERAL)의 뜻은 {@code PostSearchRepository}와 같다.
     */
    @Query(nativeQuery = true, value = """
            SELECT u.* FROM users u
            CROSS JOIN LATERAL (
                SELECT count(*) FILTER (WHERE lower(u.nickname) LIKE t.k
                                           OR lower(u.bio) LIKE t.k
                                           OR lower(CAST(u.tech_stacks AS text)) LIKE t.k) AS matched,
                       count(*) FILTER (WHERE lower(u.nickname) LIKE t.k) AS title_hits,
                       count(*) FILTER (WHERE lower(u.bio) LIKE t.k
                                           OR lower(CAST(u.tech_stacks AS text)) LIKE t.k) AS body_hits
                FROM unnest(CAST(:patterns AS text[])) AS t(k)
            ) m
            WHERE u.status = 'ACTIVE'
            AND (:viewerId IS NULL OR u.id NOT IN (
                SELECT blocked_id FROM blocks WHERE blocker_id = :viewerId
                UNION
                SELECT blocker_id FROM blocks WHERE blocked_id = :viewerId
            ))
              AND m.matched >= CASE WHEN :matchAll THEN cardinality(CAST(:patterns AS text[])) ELSE 1 END
            ORDER BY CASE WHEN :relevance THEN
                         CASE WHEN lower(u.nickname) = :phrase THEN 100 ELSE 0 END
                       + CASE WHEN lower(u.nickname) LIKE :phrasePattern THEN 30 ELSE 0 END
                       + m.title_hits * 10
                       + m.body_hits * 3
                     END DESC,
                     u.created_at DESC, u.id DESC
            """,
            countQuery = """
            SELECT count(*) FROM users u
            CROSS JOIN LATERAL (
                SELECT count(*) FILTER (WHERE lower(u.nickname) LIKE t.k
                                           OR lower(u.bio) LIKE t.k
                                           OR lower(CAST(u.tech_stacks AS text)) LIKE t.k) AS matched
                FROM unnest(CAST(:patterns AS text[])) AS t(k)
            ) m
            WHERE u.status = 'ACTIVE'
            AND (:viewerId IS NULL OR u.id NOT IN (
                SELECT blocked_id FROM blocks WHERE blocker_id = :viewerId
                UNION
                SELECT blocker_id FROM blocks WHERE blocked_id = :viewerId
            ))
              AND m.matched >= CASE WHEN :matchAll THEN cardinality(CAST(:patterns AS text[])) ELSE 1 END
            """)
    Page<User> searchByLikePatterns(
            @Param("patterns") String[] patterns,
            @Param("phrase") String phrase,
            @Param("phrasePattern") String phrasePattern,
            @Param("matchAll") boolean matchAll,
            @Param("relevance") boolean relevance,
            @Param("viewerId") Long viewerId,
            Pageable pageable);
}
