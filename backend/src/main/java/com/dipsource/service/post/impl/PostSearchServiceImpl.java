package com.dipsource.service.post.impl;

import com.dipsource.dto.post.PostSearchItemResponse;
import com.dipsource.dto.search.AutocompletePrefix;
import com.dipsource.dto.search.SearchCondition;
import com.dipsource.dto.search.SearchTermCandidate;
import com.dipsource.dto.search.SearchTermFrequency;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.Post;
import com.dipsource.global.common.PageResponse;
import com.dipsource.repository.post.PostSearchRepository;
import com.dipsource.service.post.PostSearchService;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link PostSearchService} 구현체. */
@Service
@RequiredArgsConstructor
public class PostSearchServiceImpl implements PostSearchService {

    /** 검색 결과 미리보기 최대 글자 수 */
    private static final int EXCERPT_LENGTH = 120;

    private final PostSearchRepository postSearchRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostSearchItemResponse> search(SearchCondition condition, Pageable pageable) {
        Page<Post> posts = postSearchRepository.search(condition, pageable);
        Map<Long, UserSummaryResponse> authors = userService.getSummaries(
                posts.getContent().stream().map(Post::getUserId).distinct().toList());
        return PageResponse.of(posts, post -> toSearchItem(post, authors.get(post.getUserId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchTermCandidate> findClosestTerms(String word, int maxDistance) {
        return postSearchRepository.findClosestTerms(word, maxDistance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchTermFrequency> findTermsByPrefix(AutocompletePrefix prefix, int limit) {
        return postSearchRepository.findTermsByPrefix(prefix, limit);
    }

    private PostSearchItemResponse toSearchItem(Post post, UserSummaryResponse author) {
        return new PostSearchItemResponse(
                post.getId(),
                post.getTitle(),
                post.isPaid() ? null : excerpt(post.getContent()),
                post.getUserId(),
                author == null ? null : author.nickname(),
                post.getPriceChips(),
                post.getCreatedAt()
        );
    }

    /** 줄바꿈·연속 공백을 한 칸으로 줄이고 앞부분만 자른다. 마크다운 기호는 아직 그대로 둔다. */
    private static String excerpt(String content) {
        String flat = content.replaceAll("\\s+", " ").strip();
        return flat.length() <= EXCERPT_LENGTH ? flat : flat.substring(0, EXCERPT_LENGTH) + "…";
    }
}
