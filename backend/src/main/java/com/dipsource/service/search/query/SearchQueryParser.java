package com.dipsource.service.search.query;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/** 검색어 전처리: 정규화, 길이 검증, 일반 검색어와 해시태그 분리. */
@Component
public class SearchQueryParser {

    /** 프론트 검색창 제한과 같다. 글자 수는 코드 포인트로 센다 */
    static final int MAX_QUERY_LENGTH = 50;
    /** 문장 검색까지 받아야 해서 PostgreSQL 경로(5개)보다 많다 */
    static final int MAX_KEYWORDS = 10;
    static final int MAX_HASHTAGS = 5;

    public ParsedQuery parse(String query) {
        String normalized = TextNormalizer.normalize(query);
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "검색어를 입력해 주세요");
        }
        if (TextNormalizer.codePointLength(normalized) > MAX_QUERY_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "검색어는 " + MAX_QUERY_LENGTH + "자 이하로 입력해 주세요");
        }

        Set<String> keywords = new LinkedHashSet<>();
        Set<String> hashtags = new LinkedHashSet<>();
        List<String> textWords = new ArrayList<>();
        for (String word : normalized.split(" ")) {
            String tag = hashtag(word);
            if (tag != null) {
                hashtags.add(tag);
            } else if (!isBareHash(word)) {
                textWords.add(word);
                keywords.add(word);
            }
        }
        if (hashtags.isEmpty() && textWords.isEmpty()) {
            // "#"만 입력한 경우
            throw new BusinessException(ErrorCode.BAD_REQUEST, "검색어를 입력해 주세요");
        }
        return new ParsedQuery(
                String.join(" ", textWords),
                keywords.stream().limit(MAX_KEYWORDS).toList(),
                hashtags.stream().limit(MAX_HASHTAGS).toList(),
                false);
    }

    /** "#spring"이면 태그 키, 해시태그가 아니면 null. "C#"처럼 뒤에 붙은 #은 해시태그가 아니다 */
    private static String hashtag(String word) {
        if (!word.startsWith("#") || isBareHash(word)) {
            return null;
        }
        String key = TextNormalizer.tagKey(word.replaceFirst("^#+", ""));
        return key.isEmpty() ? null : key;
    }

    private static boolean isBareHash(String word) {
        return word.chars().allMatch(ch -> ch == '#');
    }
}
