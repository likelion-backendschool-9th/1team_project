package com.dipsource.service.bookmark;

import com.dipsource.dto.bookmark.MyBookmarkResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * 내 북마크 목록. 피드(feed_bookmarks)·블로그 글(post_bookmarks)·데모(bookmarks) 북마크를 합쳐 최근 저장 순으로 보여 준다.
 * 세 테이블을 UNION ALL 한 번으로 읽는다. 삭제·숨김된 대상은 빼고 보여 준다.
 */
@Service
@RequiredArgsConstructor
public class MyBookmarkService {

    private static final List<String> TYPES = List.of("feed", "post", "demo");

    private static final Map<String, String> BRANCHES = Map.of(
            "feed", """
                    SELECT 'feed' AS type, f.id AS target_id, LEFT(f.content, 80) AS title,
                           u.nickname AS author, fb.created_at AS saved_at
                    FROM feed_bookmarks fb
                    JOIN feeds f ON f.id = fb.feed_id
                    JOIN users u ON u.id = f.user_id
                    WHERE fb.user_id = :userId AND f.status = 'ACTIVE'
                    """,
            "post", """
                    SELECT 'post' AS type, p.id AS target_id, p.title AS title,
                           u.nickname AS author, pb.created_at AS saved_at
                    FROM post_bookmarks pb
                    JOIN posts p ON p.id = pb.post_id
                    JOIN users u ON u.id = p.user_id
                    WHERE pb.user_id = :userId AND p.status = 'ACTIVE'
                    """,
            "demo", """
                    SELECT 'demo' AS type, d.id AS target_id, d.title AS title,
                           u.nickname AS author, b.created_at AS saved_at
                    FROM bookmarks b
                    JOIN demos d ON d.id = b.demo_id
                    JOIN users u ON u.id = d.user_id
                    WHERE b.user_id = :userId AND d.status = 'ACTIVE'
                    """
    );

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * @param type null이면 전체, 아니면 feed / post / demo 중 하나만
     */
    public PageResponse<MyBookmarkResponse> getMine(Long userId, @Nullable String type, int page, int size) {
        if (type != null && !TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "북마크 종류는 feed, post, demo 중 하나예요.");
        }
        List<String> selected = type == null ? TYPES : List.of(type);
        String union = String.join(" UNION ALL ", selected.stream().map(BRANCHES::get).toList());

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("size", size)
                .addValue("offset", (long) page * size);

        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM (" + union + ") b", params, Long.class);
        List<MyBookmarkResponse> content = jdbc.query(
                "SELECT type, target_id, title, author, saved_at FROM (" + union + ") b"
                        + " ORDER BY saved_at DESC, target_id DESC LIMIT :size OFFSET :offset",
                params,
                (rs, rowNum) -> {
                    String rowType = rs.getString("type");
                    long id = rs.getLong("target_id");
                    return new MyBookmarkResponse(
                            id,
                            rowType,
                            rs.getString("title"),
                            rs.getString("author"),
                            href(rowType, id),
                            rs.getTimestamp("saved_at").toLocalDateTime());
                });

        long totalElements = total == null ? 0 : total;
        int totalPages = (int) ((totalElements + size - 1) / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }

    private static String href(String type, long id) {
        return switch (type) {
            case "feed" -> "/feeds/" + id;
            case "post" -> "/blog/posts/" + id;
            default -> "/demos/" + id;
        };
    }
}
