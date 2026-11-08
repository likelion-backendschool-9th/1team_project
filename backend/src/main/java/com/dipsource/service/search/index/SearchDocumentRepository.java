package com.dipsource.service.search.index;

import com.dipsource.dto.search.SearchType;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 색인할 원본 행을 PostgreSQL에서 읽는다. 공개 상태인 문서만 돌려주므로, 여기서 안 나오는 ID는 인덱스에서 지운다.
 * 태그는 집계해서 한 번에 읽어 문서 수만큼 쿼리가 늘지 않게 한다.
 * 유료 글은 본문을 읽지 않는다. 색인에 들어가면 본문 내용이 검색으로 새기 때문이다.
 */
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SearchDocumentRepository {

    /** 색인할 원본 한 건. 값은 필드 이름 그대로다 */
    public record SourceRow(long id, Map<String, Object> values) {
    }

    private static final String POST_SQL = """
            SELECT p.id, p.user_id, p.title, CASE WHEN p.is_paid THEN NULL ELSE p.content END AS content, p.created_at,
                   COALESCE(array_agg(t.name) FILTER (WHERE t.id IS NOT NULL), '{}') AS tags
            FROM posts p
            LEFT JOIN post_tags pt ON pt.post_id = p.id
            LEFT JOIN tags t ON t.id = pt.tag_id
            WHERE p.status = 'ACTIVE' AND p.deleted_at IS NULL AND %s
            GROUP BY p.id ORDER BY p.id LIMIT :limit
            """;
    private static final String DEMO_SQL = """
            SELECT d.id, d.user_id, d.title, d.description, d.created_at,
                   COALESCE(array_agg(t.name) FILTER (WHERE t.id IS NOT NULL), '{}') AS tags
            FROM demos d
            LEFT JOIN demo_tags dt ON dt.demo_id = d.id
            LEFT JOIN tags t ON t.id = dt.tag_id
            WHERE d.status = 'ACTIVE' AND d.deleted_at IS NULL AND %s
            GROUP BY d.id ORDER BY d.id LIMIT :limit
            """;
    private static final String FEED_SQL = """
            SELECT f.id, f.user_id, f.content, f.created_at,
                   CASE WHEN d.id IS NOT NULL THEN concat_ws(' ', d.title, d.description) END AS demo_text,
                   COALESCE(array_agg(t.name) FILTER (WHERE t.id IS NOT NULL), '{}') AS tags
            FROM feeds f
            LEFT JOIN demos d ON d.id = f.demo_id AND d.status = 'ACTIVE' AND d.deleted_at IS NULL
            LEFT JOIN feed_tags ft ON ft.feed_id = f.id
            LEFT JOIN tags t ON t.id = ft.tag_id
            WHERE f.status = 'ACTIVE' AND f.deleted_at IS NULL AND %s
            GROUP BY f.id, d.id ORDER BY f.id LIMIT :limit
            """;
    private static final String USER_SQL = """
            SELECT u.id, u.nickname, u.bio, u.github_username, u.created_at,
                   ARRAY(SELECT jsonb_array_elements_text(COALESCE(u.tech_stacks, '[]'::jsonb))) AS tech_stacks
            FROM users u
            WHERE u.status = 'ACTIVE' AND %s
            ORDER BY u.id LIMIT :limit
            """;

    private final NamedParameterJdbcTemplate jdbc;

    /** ID 순서로 이어서 읽는다. 재색인 중에 데이터가 늘어도 OFFSET처럼 밀리지 않는다 */
    public List<SourceRow> findAfter(SearchType type, long afterId, int limit) {
        return query(type, alias(type) + ".id > :afterId",
                new MapSqlParameterSource().addValue("afterId", afterId).addValue("limit", limit));
    }

    public List<SourceRow> findByIds(SearchType type, Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return query(type, alias(type) + ".id IN (:ids)",
                new MapSqlParameterSource().addValue("ids", ids).addValue("limit", ids.size()));
    }

    /** 공개 상태인 ID만 ID 순서로. 인덱스와 비교할 때 쓴다 */
    public List<Long> findActiveIdsAfter(SearchType type, long afterId, int limit) {
        String sql = switch (type) {
            case POST -> "SELECT id FROM posts WHERE status = 'ACTIVE' AND deleted_at IS NULL AND id > :afterId ORDER BY id LIMIT :limit";
            case DEMO -> "SELECT id FROM demos WHERE status = 'ACTIVE' AND deleted_at IS NULL AND id > :afterId ORDER BY id LIMIT :limit";
            case FEED -> "SELECT id FROM feeds WHERE status = 'ACTIVE' AND deleted_at IS NULL AND id > :afterId ORDER BY id LIMIT :limit";
            case USER -> "SELECT id FROM users WHERE status = 'ACTIVE' AND id > :afterId ORDER BY id LIMIT :limit";
            case ALL -> throw new IllegalArgumentException("ALL은 인덱스가 아니다");
        };
        return jdbc.queryForList(sql, new MapSqlParameterSource().addValue("afterId", afterId).addValue("limit", limit),
                Long.class);
    }

    private List<SourceRow> query(SearchType type, String condition, MapSqlParameterSource params) {
        String sql = switch (type) {
            case POST -> POST_SQL.formatted(condition);
            case DEMO -> DEMO_SQL.formatted(condition);
            case FEED -> FEED_SQL.formatted(condition);
            case USER -> USER_SQL.formatted(condition);
            case ALL -> throw new IllegalArgumentException("ALL은 인덱스가 아니다");
        };
        return jdbc.query(sql, params, (rs, rowNum) -> toRow(type, rs));
    }

    private static String alias(SearchType type) {
        return switch (type) {
            case POST -> "p";
            case DEMO -> "d";
            case FEED -> "f";
            case USER -> "u";
            case ALL -> throw new IllegalArgumentException("ALL은 인덱스가 아니다");
        };
    }

    private static SourceRow toRow(SearchType type, ResultSet rs) throws SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("created_at", rs.getTimestamp("created_at"));
        switch (type) {
            case POST -> {
                values.put("author_id", rs.getLong("user_id"));
                values.put("title", rs.getString("title"));
                values.put("content", rs.getString("content"));
                values.put("tags", strings(rs.getArray("tags")));
            }
            case DEMO -> {
                values.put("author_id", rs.getLong("user_id"));
                values.put("title", rs.getString("title"));
                values.put("description", rs.getString("description"));
                values.put("tags", strings(rs.getArray("tags")));
            }
            case FEED -> {
                values.put("author_id", rs.getLong("user_id"));
                values.put("content", rs.getString("content"));
                values.put("demo_text", rs.getString("demo_text"));
                values.put("tags", strings(rs.getArray("tags")));
            }
            case USER -> {
                values.put("nickname", rs.getString("nickname"));
                values.put("bio", rs.getString("bio"));
                values.put("github_username", rs.getString("github_username"));
                values.put("tech_stacks", strings(rs.getArray("tech_stacks")));
            }
            case ALL -> throw new IllegalArgumentException("ALL은 인덱스가 아니다");
        }
        return new SourceRow(rs.getLong("id"), values);
    }

    private static List<String> strings(Array array) throws SQLException {
        if (array == null) {
            return new ArrayList<>();
        }
        return Arrays.asList((String[]) array.getArray());
    }

    static String iso(Timestamp timestamp) {
        return timestamp.toLocalDateTime().toString();
    }
}
