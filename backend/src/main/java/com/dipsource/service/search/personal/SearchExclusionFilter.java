package com.dipsource.service.search.personal;

import com.dipsource.dto.demo.DemoSearchItemResponse;
import com.dipsource.dto.feed.FeedSearchItemResponse;
import com.dipsource.dto.post.PostSearchItemResponse;
import com.dipsource.dto.search.SearchAllResponse;
import com.dipsource.dto.search.SearchPageResponse;
import com.dipsource.dto.search.SearchSection;
import com.dipsource.dto.search.SearchType;
import com.dipsource.dto.user.UserSearchItemResponse;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PostgreSQL 대체 경로용 숨김·차단 필터. 장애 중에도 차단한 사람이 보이면 안 되므로 응답에서 뺀다.
 * 페이지를 채운 뒤 거르기 때문에 이 경로에서만 페이지가 조금 짧아질 수 있다.
 */
@Component
@RequiredArgsConstructor
public class SearchExclusionFilter {

    private final SearchPersonalizationService personalization;

    @SuppressWarnings("unchecked")
    public SearchPageResponse<?> filter(SearchPageResponse<?> response, SearchType type, Long viewerId) {
        if (viewerId == null) {
            return response;
        }
        Set<Long> blocked = personalization.blockedUsers(viewerId);
        Set<Long> hidden = personalization.hiddenIds(viewerId, type);
        List<Object> content = ((List<Object>) response.content()).stream()
                .filter(item -> !hidden.contains(idOf(item)) && !blocked.contains(authorOf(item)))
                .toList();
        return new SearchPageResponse<>(content, response.page(), response.size(), response.totalElements(),
                response.totalPages(), response.correctedQuery(), response.suggestedQuery(), response.suggestionType(),
                response.relaxedMatch());
    }

    public SearchAllResponse filter(SearchAllResponse response, Long viewerId) {
        if (viewerId == null) {
            return response;
        }
        Set<Long> blocked = personalization.blockedUsers(viewerId);
        return new SearchAllResponse(
                section(response.posts(), PostSearchItemResponse::id, PostSearchItemResponse::authorId, viewerId, SearchType.POST, blocked),
                section(response.feeds(), FeedSearchItemResponse::id, FeedSearchItemResponse::authorId, viewerId, SearchType.FEED, blocked),
                section(response.demos(), DemoSearchItemResponse::id, DemoSearchItemResponse::authorId, viewerId, SearchType.DEMO, blocked),
                section(response.users(), UserSearchItemResponse::id, UserSearchItemResponse::id, viewerId, SearchType.USER, blocked),
                response.correctedQuery(), response.suggestedQuery(), response.suggestionType());
    }

    private <T> SearchSection<T> section(SearchSection<T> section, Function<T, Long> id, Function<T, Long> author, Long viewerId,
                                         SearchType type, Set<Long> blocked) {
        Set<Long> hidden = personalization.hiddenIds(viewerId, type);
        return new SearchSection<>(section.content().stream()
                .filter(item -> !hidden.contains(id.apply(item)) && !blocked.contains(author.apply(item)))
                .toList(), section.totalElements());
    }

    private static Long idOf(Object item) {
        return switch (item) {
            case PostSearchItemResponse post -> post.id();
            case FeedSearchItemResponse feed -> feed.id();
            case DemoSearchItemResponse demo -> demo.id();
            case UserSearchItemResponse user -> user.id();
            default -> throw new IllegalArgumentException("알 수 없는 검색 결과 항목: " + item);
        };
    }

    private static Long authorOf(Object item) {
        return switch (item) {
            case PostSearchItemResponse post -> post.authorId();
            case FeedSearchItemResponse feed -> feed.authorId();
            case DemoSearchItemResponse demo -> demo.authorId();
            case UserSearchItemResponse user -> user.id();
            default -> throw new IllegalArgumentException("알 수 없는 검색 결과 항목: " + item);
        };
    }
}
