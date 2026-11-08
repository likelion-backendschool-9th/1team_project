package com.dipsource.service.search.sync;

import com.dipsource.dto.search.SearchType;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** 색인 대기열 조회와 갱신. 여러 서버가 돌아도 SKIP LOCKED로 같은 행을 동시에 처리하지 않는다. */
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SearchOutboxRepository {

    public record Event(long id, SearchType type, long targetId, LocalDateTime createdAt) {
    }

    public record Status(long pending, long dead, Double oldestPendingSeconds) {
    }

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;

    public List<Event> lockPending(int limit, int maxAttempts) {
        return jdbc.query("""
                SELECT id, target_type, target_id, created_at FROM search_outbox
                WHERE processed_at IS NULL AND next_attempt_at <= now() AND attempts < ?
                ORDER BY id
                LIMIT ?
                FOR UPDATE SKIP LOCKED
                """, (rs, n) -> new Event(rs.getLong("id"), SearchType.valueOf(rs.getString("target_type")),
                rs.getLong("target_id"), rs.getTimestamp("created_at").toLocalDateTime()), maxAttempts, limit);
    }

    public void markProcessed(Collection<Long> ids) {
        if (!ids.isEmpty()) {
            named.update("UPDATE search_outbox SET processed_at = now(), last_error = NULL WHERE id IN (:ids)",
                    new MapSqlParameterSource("ids", ids));
        }
    }

    public void markFailed(Collection<Long> ids, String error, long backoffSeconds) {
        if (!ids.isEmpty()) {
            named.update("""
                    UPDATE search_outbox
                    SET attempts = attempts + 1,
                        next_attempt_at = now() + (:backoff * interval '1 second') * power(2, LEAST(attempts, 7)),
                        last_error = :error
                    WHERE id IN (:ids)
                    """, new MapSqlParameterSource("ids", ids)
                    .addValue("error", error.length() > 500 ? error.substring(0, 500) : error)
                    .addValue("backoff", backoffSeconds));
        }
    }

    public void enqueue(SearchType type, Collection<Long> targetIds) {
        List<Object[]> rows = new ArrayList<>(targetIds.size());
        targetIds.forEach(id -> rows.add(new Object[] {type.name(), id}));
        jdbc.batchUpdate("""
                INSERT INTO search_outbox(target_type, target_id, created_at, attempts, next_attempt_at)
                VALUES (?, ?, now(), 0, now())
                """, rows);
    }

    /** 재색인 중에 처리된 변경을 새 인덱스에 다시 적용하도록 되돌린다 */
    public int replaySince(SearchType type, LocalDateTime since) {
        return jdbc.update("""
                UPDATE search_outbox SET processed_at = NULL, attempts = 0, next_attempt_at = now()
                WHERE target_type = ? AND created_at >= ?
                """, type.name(), Timestamp.valueOf(since));
    }

    /** 재시도 한도를 넘긴 행을 다시 시도한다 */
    public int retryDead(int maxAttempts) {
        return jdbc.update("""
                UPDATE search_outbox SET attempts = 0, next_attempt_at = now()
                WHERE processed_at IS NULL AND attempts >= ?
                """, maxAttempts);
    }

    public int deleteProcessedBefore(LocalDateTime before) {
        return jdbc.update("DELETE FROM search_outbox WHERE processed_at < ?", Timestamp.valueOf(before));
    }

    public Status status(int maxAttempts) {
        Map<String, Object> row = jdbc.queryForMap("""
                SELECT count(*) FILTER (WHERE attempts < ?) AS pending,
                       count(*) FILTER (WHERE attempts >= ?) AS dead,
                       CAST(extract(epoch FROM now() - min(created_at)) AS double precision) AS oldest
                FROM search_outbox WHERE processed_at IS NULL
                """, maxAttempts, maxAttempts);
        return new Status(((Number) row.get("pending")).longValue(), ((Number) row.get("dead")).longValue(),
                row.get("oldest") == null ? null : ((Number) row.get("oldest")).doubleValue());
    }

    /** 게시물에 첨부된 데모가 바뀌면 그 피드의 색인도 바뀐다 */
    public List<Long> findFeedIdsByDemoIds(Collection<Long> demoIds) {
        return named.queryForList("SELECT id FROM feeds WHERE demo_id IN (:ids)", new MapSqlParameterSource("ids", demoIds), Long.class);
    }

    /** 탈퇴한 회원의 숨김·방문 기록을 지운다 */
    public void purgeWithdrawnUsers(Collection<Long> userIds) {
        MapSqlParameterSource params = new MapSqlParameterSource("ids", userIds);
        named.update("DELETE FROM search_hidden_results WHERE user_id IN (SELECT id FROM users WHERE id IN (:ids) AND status = 'DELETED')", params);
        named.update("DELETE FROM search_visits WHERE user_id IN (SELECT id FROM users WHERE id IN (:ids) AND status = 'DELETED')", params);
    }
}
