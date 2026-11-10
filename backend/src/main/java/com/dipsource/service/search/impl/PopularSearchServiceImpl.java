package com.dipsource.service.search.impl;

import com.dipsource.entity.search.SearchKeywordLog;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.search.SearchKeywordLogRepository;
import com.dipsource.repository.tag.TagRepository;
import com.dipsource.service.search.PopularSearchService;
import com.dipsource.service.search.query.TextNormalizer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PopularSearchServiceImpl implements PopularSearchService {

    static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 50;
    private static final int MAX_LIMIT = 20;
    private static final int DEDUP_MINUTES = 10;
    private static final int WINDOW_DAYS = 7;

    private final SearchKeywordLogRepository searchKeywordLogRepository;
    private final TagRepository tagRepository;
    private final Clock clock;

    @Autowired
    public PopularSearchServiceImpl(SearchKeywordLogRepository searchKeywordLogRepository, TagRepository tagRepository) {
        this(searchKeywordLogRepository, tagRepository, Clock.system(ZONE));
    }

    PopularSearchServiceImpl(SearchKeywordLogRepository searchKeywordLogRepository, TagRepository tagRepository, Clock clock) {
        this.searchKeywordLogRepository = searchKeywordLogRepository;
        this.tagRepository = tagRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void record(String query, Long userId) {
        String keyword = TextNormalizer.normalize(query);
        int length = TextNormalizer.codePointLength(keyword);
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (userId != null && searchKeywordLogRepository.existsByUserIdAndKeywordAndSearchedAtGreaterThanEqual(
                userId, keyword, now.minusMinutes(DEDUP_MINUTES))) {
            return;
        }
        searchKeywordLogRepository.save(SearchKeywordLog.create(keyword, userId, now));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> popular(int limit) {
        int size = size(limit);
        List<String> keywords = new ArrayList<>(searchKeywordLogRepository.countSince(LocalDateTime.now(clock).minusDays(WINDOW_DAYS))
                .stream()
                .map(row -> new Score((String) row[0], ((Number) row[1]).longValue(), (LocalDateTime) row[2]))
                .sorted(Comparator.comparingLong(Score::count).reversed()
                        .thenComparing(Score::latest, Comparator.reverseOrder()))
                .limit(size)
                .map(Score::keyword)
                .toList());
        if (keywords.size() >= size) {
            return List.copyOf(keywords);
        }
        Set<String> seen = new HashSet<>();
        keywords.forEach(keyword -> seen.add(matchKey(keyword)));
        for (String tag : tagRepository.findPopularNames(size + keywords.size())) {
            if (keywords.size() >= size) {
                break;
            }
            String key = matchKey(tag);
            if (key.isEmpty() || !seen.add(key)) {
                continue;
            }
            keywords.add(tag);
        }
        return List.copyOf(keywords);
    }

    /** 이미 넣은 검색어와 태그를 비교한다. 앞뒤 공백과 대소문자는 구분하지 않는다. */
    private static String matchKey(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static int size(int limit) {
        if (limit < 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "개수는 1 이상이어야 해요.");
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private record Score(String keyword, long count, LocalDateTime latest) {
    }
}
