package com.dipsource.service.search.impl;

import com.dipsource.dto.search.AutocompletePrefix;
import com.dipsource.dto.search.AutocompleteResponse;
import com.dipsource.dto.search.AutocompleteType;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.demo.DemoSearchService;
import com.dipsource.service.post.PostSearchService;
import com.dipsource.service.search.AutocompleteService;
import com.dipsource.service.tag.TagSearchService;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@link AutocompleteService} 구현체. 후보 출처와 순서만 정하고, 접두어로 찾는 방법은 각 기능 서비스(Repository)가 맡는다.
 *
 * <p>단어 사전은 3단계 오타 교정과 같다(태그 이름, 공개 글 제목, 공개 데모 제목). 유료 글 본문과 숨김·삭제 글은 쓰지 않는다.
 * 사전은 요청할 때마다 계산한다. 5단계에서 속도를 재고, 6단계에서 미리 만든 사전과 인덱스로 바꾸는 것이 목표다.
 */
@Service
@RequiredArgsConstructor
public class AutocompleteServiceImpl implements AutocompleteService {

    /** 후보 최대 개수 */
    static final int MAX_SUGGESTIONS = 8;
    /** 태그 후보 최대 개수. 나머지 자리는 단어 후보로 채운다 */
    static final int MAX_TAGS = 4;

    private final TagSearchService tagSearchService;
    private final PostSearchService postSearchService;
    private final DemoSearchService demoSearchService;

    @Override
    public List<AutocompleteResponse> autocomplete(String query) {
        String stripped = query == null ? "" : query.strip();
        if (stripped.isEmpty()) {
            // 입력창을 지우는 순간에도 호출되므로 오류가 아니라 빈 목록이다
            return List.of();
        }
        if (stripped.length() > SearchServiceImpl.MAX_QUERY_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "검색어는 " + SearchServiceImpl.MAX_QUERY_LENGTH + "자 이하로 입력해 주세요");
        }
        String input = stripped.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        // "spring b" → 앞 단어 "spring "은 그대로 두고 마지막 단어 "b"만 완성한다
        int lastSpace = input.lastIndexOf(' ');
        String leading = input.substring(0, lastSpace + 1);
        String lastWord = input.substring(lastSpace + 1);

        Map<String, AutocompleteResponse> suggestions = new LinkedHashMap<>();
        Stream.concat(tags(input), words(leading, lastWord))
                // 입력과 똑같은 후보를 맨 앞으로. 나머지는 태그 → 단어 순서를 그대로 둔다(안정 정렬)
                .sorted(Comparator.comparing(suggestion -> !suggestion.text().toLowerCase(Locale.ROOT).equals(input)))
                // 태그 Spring Boot와 단어 spring boot처럼 같은 검색어는 앞의 것(태그)만 남긴다
                .forEach(suggestion -> suggestions.putIfAbsent(suggestion.text().toLowerCase(Locale.ROOT), suggestion));
        return suggestions.values().stream().limit(MAX_SUGGESTIONS).toList();
    }

    /** 입력 전체로 시작하는 태그 이름 */
    private Stream<AutocompleteResponse> tags(String input) {
        return tagSearchService.findNamesByPrefix(AutocompletePrefix.of(input), MAX_TAGS).stream()
                .map(name -> new AutocompleteResponse(name, AutocompleteType.TAG));
    }

    /** 마지막 단어를 사전 단어로 완성한 검색어. 세 사전의 등장 횟수를 더해서 자주 나온 순 → 짧은 순 → 사전순 */
    private Stream<AutocompleteResponse> words(String leading, String lastWord) {
        AutocompletePrefix prefix = AutocompletePrefix.of(lastWord);
        Map<String, Long> frequencies = new HashMap<>();
        Stream.of(
                        tagSearchService.findTermsByPrefix(prefix, MAX_SUGGESTIONS),
                        postSearchService.findTermsByPrefix(prefix, MAX_SUGGESTIONS),
                        demoSearchService.findTermsByPrefix(prefix, MAX_SUGGESTIONS))
                .flatMap(List::stream)
                .forEach(term -> frequencies.merge(term.getTerm(), term.getFrequency(), Long::sum));
        return frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(entry -> entry.getKey().length())
                        .thenComparing(Map.Entry.<String, Long>comparingByKey()))
                .map(entry -> new AutocompleteResponse(leading + entry.getKey(), AutocompleteType.WORD));
    }
}
