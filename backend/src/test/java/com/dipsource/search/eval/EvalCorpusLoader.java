package com.dipsource.search.eval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 고정 평가 말뭉치를 PostgreSQL에 넣는다. 문서 key와 DB id를 오간다.
 * IDF가 실제에 가깝도록 주제가 다른 배경 글을 함께 넣는다. 배경 글은 평가 검색어를 포함하지 않는다.
 */
final class EvalCorpusLoader {

    private static final int FILLER_POSTS = 120;
    private static final String[] FILLER_TOPICS = {
            "회의록 작성 요령", "재택근무 장비 후기", "사이드 프로젝트 회고", "면접 준비 기록", "연봉 협상 경험담",
            "온보딩 문서 개선", "코드 리뷰 문화 만들기", "번아웃 극복기", "기술 블로그 운영기", "팀 회식 장소 추천"};
    private static final String[] FILLER_BODIES = {
            "업무 효율을 높이기 위해 시도한 것들을 적었다. 작은 습관이 쌓이면 큰 차이를 만든다.",
            "동료들과 나눈 이야기를 바탕으로 정리했다. 정답은 없지만 참고가 되길 바란다.",
            "지난 분기를 돌아보며 잘한 점과 아쉬운 점을 나누어 기록했다."};

    private final JdbcTemplate jdbc;
    private final Map<String, Long> ids = new HashMap<>();
    private final Map<Long, String> postKeys = new HashMap<>();
    private final Map<Long, String> demoKeys = new HashMap<>();
    private final Map<Long, String> feedKeys = new HashMap<>();
    private final Map<Long, String> userKeys = new HashMap<>();
    private final Map<String, Long> tagIds = new HashMap<>();
    private Long firstUserId;

    EvalCorpusLoader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    void load(String corpusResource) throws IOException {
        JsonNode corpus = new ObjectMapper().readTree(new ClassPathResource(corpusResource).getInputStream());
        jdbc.execute("TRUNCATE posts, feeds, demos, users, tags, follows, blocks, post_tags, demo_tags, feed_tags "
                + "RESTART IDENTITY CASCADE");
        for (JsonNode user : corpus.get("users")) {
            insertUser(user);
        }
        for (JsonNode post : corpus.get("posts")) {
            insertPost(post);
        }
        for (JsonNode demo : corpus.get("demos")) {
            insertDemo(demo);
        }
        for (JsonNode feed : corpus.get("feeds")) {
            insertFeed(feed);
        }
        insertFiller();
        insertFollows(corpus);
        for (JsonNode block : corpus.get("blocks")) {
            jdbc.update("INSERT INTO blocks(blocker_id, blocked_id, created_at) VALUES (?, ?, now())",
                    ids.get(block.get(0).asText()), ids.get(block.get(1).asText()));
        }
        if (hasTable("search_hidden_results")) {
            jdbc.execute("TRUNCATE search_hidden_results, search_visits RESTART IDENTITY");
            for (JsonNode hidden : corpus.get("hidden")) {
                jdbc.update("INSERT INTO search_hidden_results(user_id, target_type, target_id, created_at) VALUES (?, ?, ?, now())",
                        ids.get(hidden.get("user").asText()), hidden.get("type").asText(), ids.get(hidden.get("target").asText()));
            }
            for (JsonNode visit : corpus.get("visits")) {
                jdbc.update("""
                        INSERT INTO search_visits(user_id, target_type, target_id, query_norm, visit_count, last_visited_at)
                        VALUES (?, ?, ?, ?, ?, now() - (? * interval '1 day'))
                        """, ids.get(visit.get("user").asText()), visit.get("type").asText(), ids.get(visit.get("target").asText()),
                        visit.get("query").asText(), visit.get("count").asInt(), visit.get("daysAgo").asInt());
            }
        }
    }

    private boolean hasTable(String table) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT to_regclass(?) IS NOT NULL", Boolean.class, table));
    }

    Long id(String key) {
        return ids.get(key);
    }

    String keyOf(String type, long id) {
        Map<Long, String> keys = switch (type) {
            case "post" -> postKeys;
            case "demo" -> demoKeys;
            case "feed" -> feedKeys;
            case "user" -> userKeys;
            default -> throw new IllegalArgumentException(type);
        };
        return keys.getOrDefault(id, "filler-" + id);
    }

    private void insertUser(JsonNode user) {
        String key = user.get("key").asText();
        long id = jdbc.queryForObject("""
                INSERT INTO users(email, nickname, bio, tech_stacks, github_username, role, status, chip_balance,
                                  blog_subscription_price, created_at, updated_at)
                VALUES (?, ?, ?, CAST(? AS jsonb), ?, 'USER', ?, 0, 0, now() - interval '100 day', now())
                RETURNING id
                """, Long.class, key + "@eval.local", user.get("nickname").asText(), text(user, "bio"),
                user.get("techStacks").toString(), text(user, "githubUsername"), status(user));
        ids.put(key, id);
        userKeys.put(id, key);
        if (firstUserId == null) {
            firstUserId = id;
        }
    }

    private void insertPost(JsonNode post) {
        String content = post.has("contentRepeat")
                ? post.get("contentRepeat").get("text").asText().repeat(post.get("contentRepeat").get("times").asInt())
                : post.get("content").asText();
        boolean paid = post.path("paid").asBoolean(false);
        String status = status(post);
        long id = jdbc.queryForObject("""
                INSERT INTO posts(user_id, title, content, is_paid, price_chips, status, view_count, like_count,
                                  comment_count, created_at, updated_at, deleted_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?, now(), ?)
                RETURNING id
                """, Long.class, ids.get(post.get("author").asText()), post.get("title").asText(), content, paid,
                post.path("price").asInt(0), status, post.path("views").asInt(0), post.path("likes").asInt(0),
                created(post), deletedAt(status));
        postKeys.put(id, post.get("key").asText());
        ids.put(post.get("key").asText(), id);
        insertTags("post_tags", "post_id", id, post);
    }

    private void insertDemo(JsonNode demo) {
        String status = status(demo);
        long id = jdbc.queryForObject("""
                INSERT INTO demos(user_id, title, description, demo_type, status, play_count, like_count,
                                  feedback_count, version, created_at, updated_at, deleted_at)
                VALUES (?, ?, ?, 'STATIC', ?, ?, ?, 0, 1, ?, now(), ?)
                RETURNING id
                """, Long.class, ids.get(demo.get("author").asText()), demo.get("title").asText(),
                demo.get("description").asText(), status, demo.path("plays").asInt(0), demo.path("likes").asInt(0),
                created(demo), deletedAt(status));
        demoKeys.put(id, demo.get("key").asText());
        ids.put(demo.get("key").asText(), id);
        insertTags("demo_tags", "demo_id", id, demo);
    }

    private void insertFeed(JsonNode feed) {
        String status = status(feed);
        long id = jdbc.queryForObject("""
                INSERT INTO feeds(user_id, content, demo_id, status, like_count, comment_count, repost_count,
                                  created_at, updated_at, deleted_at)
                VALUES (?, ?, ?, ?, ?, 0, 0, ?, now(), ?)
                RETURNING id
                """, Long.class, ids.get(feed.get("author").asText()), feed.get("content").asText(),
                feed.has("demo") ? ids.get(feed.get("demo").asText()) : null, status, feed.path("likes").asInt(0),
                created(feed), deletedAt(status));
        feedKeys.put(id, feed.get("key").asText());
        ids.put(feed.get("key").asText(), id);
        insertTags("feed_tags", "feed_id", id, feed);
    }

    private void insertTags(String table, String column, long targetId, JsonNode doc) {
        Set<String> names = new LinkedHashSet<>();
        doc.path("tags").forEach(tag -> names.add(tag.asText()));
        for (String name : names) {
            long tagId = tagIds.computeIfAbsent(name, n -> jdbc.queryForObject(
                    "INSERT INTO tags(name) VALUES (?) RETURNING id", Long.class, n));
            jdbc.update("INSERT INTO " + table + "(" + column + ", tag_id) VALUES (?, ?)", targetId, tagId);
        }
    }

    private void insertFiller() {
        Long author = firstUserId;
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < FILLER_POSTS; i++) {
            rows.add(new Object[] {author, FILLER_TOPICS[i % FILLER_TOPICS.length] + " " + i,
                    FILLER_BODIES[i % FILLER_BODIES.length], i % 7, i % 90});
        }
        jdbc.batchUpdate("""
                INSERT INTO posts(user_id, title, content, is_paid, price_chips, status, view_count, like_count,
                                  comment_count, created_at, updated_at)
                VALUES (?, ?, ?, false, 0, 'ACTIVE', 0, ?, 0, now() - (? * interval '1 day'), now())
                """, rows);
    }

    /** 코퍼스에 적은 followers 수만큼 팔로워 전용 회원을 만들어 팔로우 관계를 넣는다 */
    private void insertFollows(JsonNode corpus) {
        int maxFollowers = 0;
        for (JsonNode user : corpus.get("users")) {
            maxFollowers = Math.max(maxFollowers, user.path("followers").asInt(0));
        }
        List<Object[]> followerRows = new ArrayList<>();
        for (int i = 0; i < maxFollowers; i++) {
            followerRows.add(new Object[] {"follower" + i + "@eval.local", "eval_follower_" + i});
        }
        jdbc.batchUpdate("""
                INSERT INTO users(email, nickname, role, status, chip_balance, blog_subscription_price, tech_stacks,
                                  created_at, updated_at)
                VALUES (?, ?, 'USER', 'ACTIVE', 0, 0, '[]', now(), now())
                """, followerRows);
        List<Long> followerIds = jdbc.queryForList(
                "SELECT id FROM users WHERE email LIKE 'follower%@eval.local' ORDER BY id", Long.class);

        List<Object[]> follows = new ArrayList<>();
        for (JsonNode user : corpus.get("users")) {
            int count = user.path("followers").asInt(0);
            Long following = ids.get(user.get("key").asText());
            for (int i = 0; i < count; i++) {
                follows.add(new Object[] {followerIds.get(i), following});
            }
        }
        for (JsonNode follow : corpus.get("follows")) {
            follows.add(new Object[] {ids.get(follow.get(0).asText()), ids.get(follow.get(1).asText())});
        }
        jdbc.batchUpdate("INSERT INTO follows(follower_id, following_id, created_at) VALUES (?, ?, now())", follows);
    }

    private static String text(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    private static String status(JsonNode node) {
        return node.path("status").asText("ACTIVE");
    }

    private static Timestamp created(JsonNode node) {
        return Timestamp.valueOf(LocalDateTime.now().minusDays(node.path("daysAgo").asInt(1)));
    }

    private static Timestamp deletedAt(String status) {
        return "DELETED".equals(status) ? Timestamp.valueOf(LocalDateTime.now()) : null;
    }
}
