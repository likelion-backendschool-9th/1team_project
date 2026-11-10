package com.dipsource.service.search.index;

import com.dipsource.dto.search.SearchType;
import com.dipsource.service.search.query.TextNormalizer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 원본 행을 색인 문서로 바꾼다. 좋아요·조회수처럼 자주 바뀌는 값은 넣지 않고 재정렬 때 PostgreSQL에서 읽는다. */
@Component
@RequiredArgsConstructor
public class SearchDocumentFactory {

    /** 문서 하나에 색인하는 태그 수. 태그를 많이 달아 여러 검색어에 걸리는 것을 막는다 */
    static final int MAX_TAGS = 20;

    private final ContentQualityScorer qualityScorer;

    @SuppressWarnings("unchecked")
    public Map<String, Object> create(SearchType type, long id, Map<String, Object> row) {
        Map<String, Object> doc = new LinkedHashMap<>(row);
        doc.put("doc_id", id);
        doc.put("created_at", SearchDocumentRepository.iso((Timestamp) row.get("created_at")));
        switch (type) {
            case POST -> {
                doc.put("title_norm", TextNormalizer.normalize((String) row.get("title")));
                putTags(doc, (List<String>) row.get("tags"));
                doc.put("quality", qualityScorer.score((String) row.get("content")));
            }
            case DEMO -> {
                doc.put("title_norm", TextNormalizer.normalize((String) row.get("title")));
                putTags(doc, (List<String>) row.get("tags"));
                doc.put("quality", qualityScorer.score((String) row.get("description")));
            }
            case FEED -> {
                putTags(doc, (List<String>) row.get("tags"));
                doc.put("quality", qualityScorer.score((String) row.get("content")));
            }
            case USER -> {
                doc.put("nickname_norm", TextNormalizer.normalize((String) row.get("nickname")));
                List<String> stacks = (List<String>) row.get("tech_stacks");
                doc.put("tech_stacks", tagKeys(stacks));
                doc.put("tech_text", String.join(" ", stacks));
                doc.put("quality", 1.0f);
            }
            case ALL -> throw new IllegalArgumentException("ALL은 인덱스가 아니다");
        }
        doc.put("content_hash", contentHash(doc));
        return doc;
    }

    /** 원본과 색인의 내용이 같은지 비교하는 값. 태그 순서는 조회마다 달라질 수 있어서 정렬해서 계산한다 */
    private static String contentHash(Map<String, Object> doc) {
        StringBuilder canonical = new StringBuilder();
        new TreeMap<>(doc).forEach((field, value) -> {
            Object stable = value instanceof List<?> list ? list.stream().map(String::valueOf).sorted().toList() : value;
            canonical.append(field).append('=').append(stable).append('\0');
        });
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void putTags(Map<String, Object> doc, List<String> tags) {
        doc.put("tags", tagKeys(tags));
        doc.put("tags_text", String.join(" ", distinctLimited(tags)));
    }

    private static List<String> tagKeys(List<String> tags) {
        Set<String> keys = new LinkedHashSet<>();
        for (String tag : tags) {
            String key = TextNormalizer.tagKey(tag);
            if (!key.isEmpty()) {
                keys.add(key);
            }
            if (keys.size() == MAX_TAGS) {
                break;
            }
        }
        return new ArrayList<>(keys);
    }

    /** 대소문자만 다른 태그의 중복을 없애서 tags_text에서 빈도가 부풀지 않게 한다 */
    private static List<String> distinctLimited(List<String> tags) {
        Map<String, String> byKey = new LinkedHashMap<>();
        for (String tag : tags) {
            if (byKey.size() == MAX_TAGS) {
                break;
            }
            byKey.putIfAbsent(TextNormalizer.tagKey(tag), tag);
        }
        return new ArrayList<>(byKey.values());
    }
}
