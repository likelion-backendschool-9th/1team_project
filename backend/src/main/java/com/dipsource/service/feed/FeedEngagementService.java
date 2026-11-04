package com.dipsource.service.feed;

import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedStatus;
import com.dipsource.entity.feed.FeedUserId;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.feed.FeedBookmarkRepository;
import com.dipsource.repository.feed.FeedLikeRepository;
import com.dipsource.repository.feed.FeedRepository;
import com.dipsource.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 피드 상호작용(좋아요·북마크·리포스트).
 * like_count 등은 동시성을 위해 리포지토리 UPDATE로만 갱신한다.
 * 북마크는 feeds에 카운트 컬럼이 없어 feed_bookmarks 행 + 태그 가중치만 갱신한다.
 */
@Service
@RequiredArgsConstructor
public class FeedEngagementService {

    /** 좋아요/북마크 시 추천용 태그 가중치 증감량 (피드-추천-알고리즘.md) */
    private static final int ENGAGEMENT_TAG_WEIGHT = 1;

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedBookmarkRepository feedBookmarkRepository;
    private final UserRepository userRepository;
    private final TagWeightService tagWeightService;

    /**
     * 좋아요. 이미 눌렀으면 no-op (멱등).
     * <p>
     * INSERT는 {@code ON CONFLICT DO NOTHING}으로 동시 중복을 막고,
     * 실제로 삽입된 경우에만 like_count·태그 가중치를 올린다.
     */
    @Transactional
    public void like(Long userId, Long feedId) {
        findActiveFeed(feedId);

        int inserted = feedLikeRepository.insertIgnore(userId, feedId);
        if (inserted == 0) {
            return;
        }

        feedRepository.incrementLikeCount(feedId);
        tagWeightService.increaseWeight(requireUser(userId), feedId, ENGAGEMENT_TAG_WEIGHT);
    }

    /**
     * 좋아요 취소. 없으면 no-op (멱등).
     */
    @Transactional
    public void unlike(Long userId, Long feedId) {
        findActiveFeed(feedId);
        FeedUserId id = new FeedUserId(userId, feedId);
        if (!feedLikeRepository.existsById(id)) {
            return;
        }

        feedLikeRepository.deleteById(id);
        feedRepository.decrementLikeCount(feedId);
        tagWeightService.decreaseWeight(requireUser(userId), feedId, ENGAGEMENT_TAG_WEIGHT);
    }

    /**
     * 북마크. 이미 저장했으면 no-op (멱등).
     * feed_bookmarks INSERT + 태그 가중치 +1. (feeds에 bookmark_count 컬럼 없음)
     */
    @Transactional
    public void bookmark(Long userId, Long feedId) {
        findActiveFeed(feedId);

        int inserted = feedBookmarkRepository.insertIgnore(userId, feedId);
        if (inserted == 0) {
            return;
        }

        tagWeightService.increaseWeight(requireUser(userId), feedId, ENGAGEMENT_TAG_WEIGHT);
    }

    /**
     * 북마크 취소. 없으면 no-op (멱등).
     */
    @Transactional
    public void unbookmark(Long userId, Long feedId) {
        findActiveFeed(feedId);
        FeedUserId id = new FeedUserId(userId, feedId);
        if (!feedBookmarkRepository.existsById(id)) {
            return;
        }

        feedBookmarkRepository.deleteById(id);
        tagWeightService.decreaseWeight(requireUser(userId), feedId, ENGAGEMENT_TAG_WEIGHT);
    }

    private Feed findActiveFeed(Long feedId) {
        return feedRepository.findById(feedId)
                .filter(feed -> feed.getStatus() == FeedStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "피드를 찾을 수 없습니다"));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다"));
    }
}
