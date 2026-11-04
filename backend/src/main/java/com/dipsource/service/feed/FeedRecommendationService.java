package com.dipsource.service.feed;

import com.dipsource.dto.feed.FeedCursorResponse;
import com.dipsource.dto.feed.FeedResponse;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.user.User;
import com.dipsource.repository.feed.FeedRepository;
import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.repository.user.UserInterestTagRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.repository.user.UserTagProfileRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedRecommendationService {

    private final FeedRepository feedRepository;
    private final FollowRepository followRepository;
    private final UserInterestTagRepository userInterestTagRepository;
    private final UserTagProfileRepository userTagProfileRepository;
    private final UserRepository userRepository;

    /**
     * 로그인 사용자용 추천 피드.
     * 활동 데이터가 있으면 점수 기반, 없으면 지식 기반(관심 태그)으로 추천.
     */
    public FeedCursorResponse getRecommendedFeeds(Long userId, Long cursorId, int size) {
        boolean hasActivity = userTagProfileRepository.existsByIdUserId(userId);

        if (hasActivity) {
            return getScoreBasedFeeds(userId, cursorId, size);
        } else {
            return getKnowledgeBasedFeeds(userId, cursorId, size);
        }
    }

    /**
     * 점수 기반 추천 (2단계).
     * 점수 기반 50% + 트렌딩 50%.
     */
    private FeedCursorResponse getScoreBasedFeeds(Long userId, Long cursorId, int size) {
        List<Long> followingIds = followRepository.findFollowingIdsByUserId(userId);
        Long[] followingArray = followingIds.toArray(Long[]::new);

        int half = size / 2;

        List<Feed> scoredFeeds = feedRepository.findRecommendedFeeds(userId, followingArray, cursorId, half);
        List<Feed> trendingFeeds = feedRepository.findTrendingFeeds(cursorId, half);

        List<Feed> merged = mergeAndDeduplicate(scoredFeeds, trendingFeeds);

        return toResponse(merged, size);
    }

    /**
     * 지식 기반 추천 (1단계).
     * 관심 태그에 해당하는 피드 50% + 트렌딩 50%.
     */
    private FeedCursorResponse getKnowledgeBasedFeeds(Long userId, Long cursorId, int size) {
        List<Long> interestTagIds = userInterestTagRepository.findTagIdsByUserId(userId);

        int half = size / 2;
        List<Feed> tagFeeds;

        if (interestTagIds.isEmpty()) {
            // 관심 태그도 없으면 최신 피드로 대체
            tagFeeds = feedRepository.findLatestFeeds(PageRequest.of(0, half));
        } else {
            Long[] tagArray = interestTagIds.toArray(Long[]::new);
            tagFeeds = feedRepository.findByInterestTags(userId, tagArray, cursorId, half);
        }

        List<Feed> trendingFeeds = feedRepository.findTrendingFeeds(cursorId, half);

        List<Feed> merged = mergeAndDeduplicate(tagFeeds, trendingFeeds);

        return toResponse(merged, size);
    }

    /**
     * 팔로잉 피드.
     * 내가 팔로잉한 사람들의 글만 최신순으로.
     */
    public FeedCursorResponse getFollowingFeeds(Long userId, Long cursorId, int size) {
        List<Long> followingIds = followRepository.findFollowingIdsByUserId(userId);
        Long[] followingArray = followingIds.toArray(Long[]::new);

        List<Feed> feeds = feedRepository.findFollowingFeeds(followingArray, cursorId, size);

        return toResponse(feeds, size);
    }

    /**
     * 비로그인 사용자용 피드.
     * 트렌딩 50% + 최신 50%.
     */
    public FeedCursorResponse getGuestFeeds(Long cursorId, int size) {
        int half = size / 2;

        List<Feed> trendingFeeds = feedRepository.findTrendingFeeds(cursorId, half);
        List<Feed> latestFeeds = feedRepository.findLatestFeeds(PageRequest.of(0, half));

        List<Feed> merged = mergeAndDeduplicate(trendingFeeds, latestFeeds);

        return toResponse(merged, size);
    }

    private FeedCursorResponse toResponse(List<Feed> feeds, int size) {
        // 유저 정보를 한 번에 조회 (N+1 방지)
        List<Long> userIds = feeds.stream().map(Feed::getUserId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<FeedResponse> responses = feeds.stream()
                .map(feed -> FeedResponse.from(feed, userMap.get(feed.getUserId())))
                .toList();
        return FeedCursorResponse.of(responses, size);
    }

    private List<Feed> mergeAndDeduplicate(List<Feed> primary, List<Feed> secondary) {
        List<Feed> merged = new ArrayList<>(primary);
        List<Long> primaryIds = primary.stream().map(Feed::getId).toList();

        for (Feed feed : secondary) {
            if (!primaryIds.contains(feed.getId())) {
                merged.add(feed);
            }
        }
        return merged;
    }
}
