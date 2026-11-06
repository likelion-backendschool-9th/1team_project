package com.dipsource.service.feed;

import com.dipsource.dto.feed.FeedCreateRequest;
import com.dipsource.dto.feed.FeedCursorResponse;
import com.dipsource.dto.feed.FeedResponse;
import com.dipsource.dto.feed.FeedUpdateRequest;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedStatus;
import com.dipsource.entity.feed.FeedTag;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.entity.user.User;
import com.dipsource.repository.feed.FeedBookmarkRepository;
import com.dipsource.repository.feed.FeedLikeRepository;
import com.dipsource.repository.feed.FeedRepostRepository;
import com.dipsource.repository.feed.FeedRepository;
import com.dipsource.repository.feed.FeedTagRepository;
import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.repository.tag.TagRepository;
import com.dipsource.repository.user.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 피드 CRUD 비즈니스 로직
 * 추천/트렌딩 조회는 FeedRecommendationService가 담당함
 */
@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedTagRepository feedTagRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedRepostRepository feedRepostRepository;
    private final FeedBookmarkRepository feedBookmarkRepository;
    private final FollowRepository followRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    /**
     * 피드 생성
     * 피드를 저장한 뒤 태그가 있으면 feed_tags에도 함께 저장함
     *
     * @param userId  작성자 ID (JWT에서 추출)
     * @param request 생성 요청 (content, demoId, tagIds, pollOptions)
     * @return 생성된 피드 응답
     */
    @Transactional
    public FeedResponse create(Long userId, FeedCreateRequest request) {
        Feed feed = Feed.builder()
                .userId(userId)
                .content(request.content())
                .demoId(request.demoId())
                .imageUrls(request.imageUrls())
                .linkUrl(request.linkUrl())
                .pollOptions(request.pollOptions())
                .build();
        feedRepository.save(feed);

        // 태그가 있으면 존재 여부 확인 후 feed_tags 테이블에 저장
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            long existCount = tagRepository.countByIdIn(request.tagIds());
            if (existCount != request.tagIds().size()) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 태그가 포함되어 있습니다");
            }
            List<FeedTag> tags = request.tagIds().stream()
                    .map(tagId -> new FeedTag(feed, tagId))
                    .toList();
            feedTagRepository.saveAll(tags);
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다"));
        return FeedResponse.from(feed, author);
    }

    /**
     * 피드 단건 조회
     * ACTIVE 상태인 피드만 조회 가능. 삭제/숨김 피드는 404
     * userId가 null이면 비로그인 (liked/bookmarked/following 전부 false)
     */
    @Transactional(readOnly = true)
    public FeedResponse getById(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .filter(f -> f.getStatus() == FeedStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "피드를 찾을 수 없습니다"));
        User author = userRepository.findById(feed.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다"));

        if (userId == null) {
            return FeedResponse.from(feed, author);
        }

        boolean liked = feedLikeRepository.existsByIdUserIdAndIdFeedId(userId, feedId);
        boolean reposted = feedRepostRepository.existsByIdUserIdAndIdFeedId(userId, feedId);
        boolean bookmarked = feedBookmarkRepository.existsByIdUserIdAndIdFeedId(userId, feedId);
        boolean following = followRepository.existsByIdFollowerIdAndIdFollowingId(userId, feed.getUserId());
        return FeedResponse.from(feed, author, liked, reposted, bookmarked, following);
    }

    /**
     * 피드 수정
     * 본인이 작성한 ACTIVE 피드만 수정 가능. 본문(content)만 변경됨
     *
     * @param userId  요청자 ID (JWT에서 추출)
     * @param feedId  수정할 피드 ID
     * @param request 수정 요청 (content)
     * @return 수정된 피드 응답
     * @throws BusinessException NOT_FOUND — 피드가 없거나 삭제됨
     * @throws BusinessException UNAUTHORIZED — 본인 피드가 아님
     */
    @Transactional
    public FeedResponse update(Long userId, Long feedId, FeedUpdateRequest request) {
        Feed feed = findOwnActiveFeed(userId, feedId);
        feed.update(request.content());
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다"));
        return FeedResponse.from(feed, author);
    }

    /**
     * 피드 삭제 (소프트 삭제)
     * status를 DELETED로 변경하고 deletedAt을 기록함. DB에서 실제로 지우지 않음
     *
     * @param userId 요청자 ID (JWT에서 추출)
     * @param feedId 삭제할 피드 ID
     * @throws BusinessException NOT_FOUND — 피드가 없거나 이미 삭제됨
     * @throws BusinessException UNAUTHORIZED — 본인 피드가 아님
     */
    @Transactional
    public void delete(Long userId, Long feedId) {
        Feed feed = findOwnActiveFeed(userId, feedId);
        feed.delete();
    }

    /**
     * 내가 쓴 피드 목록 조회 (커서 페이지네이션)
     */
    @Transactional(readOnly = true)
    public FeedCursorResponse getMyFeeds(Long userId, Long cursor, int size) {
        List<Feed> feeds = feedRepository.findMyFeeds(userId, cursor, size);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다"));
        if (feeds.isEmpty()) {
            return FeedCursorResponse.of(List.of(), size);
        }
        List<Long> feedIds = feeds.stream().map(Feed::getId).toList();
        Set<Long> likedIds = new HashSet<>(feedLikeRepository.findLikedFeedIds(userId, feedIds));
        Set<Long> bookmarkedIds = new HashSet<>(feedBookmarkRepository.findBookmarkedFeedIds(userId, feedIds));
        // 내 글이므로 reposted는 의미 없음(false), following도 본인이므로 false
        List<FeedResponse> responses = feeds.stream()
                .map(feed -> FeedResponse.from(
                        feed, author,
                        likedIds.contains(feed.getId()),
                        false,
                        bookmarkedIds.contains(feed.getId()),
                        false
                ))
                .toList();
        return FeedCursorResponse.of(responses, size);
    }

    /**
     * 내가 리포스트한 피드 목록 조회 (커서 페이지네이션)
     */
    @Transactional(readOnly = true)
    public FeedCursorResponse getMyReposts(Long userId, Long cursor, int size) {
        List<Feed> feeds = feedRepository.findMyReposts(userId, cursor, size);
        if (feeds.isEmpty()) {
            return FeedCursorResponse.of(List.of(), size);
        }
        List<Long> feedIds = feeds.stream().map(Feed::getId).toList();
        List<Long> authorIds = feeds.stream().map(Feed::getUserId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Set<Long> likedIds = new HashSet<>(feedLikeRepository.findLikedFeedIds(userId, feedIds));
        Set<Long> bookmarkedIds = new HashSet<>(feedBookmarkRepository.findBookmarkedFeedIds(userId, feedIds));
        Set<Long> followingAuthorIds = new HashSet<>(followRepository.findFollowingIdsIn(userId, authorIds));
        List<FeedResponse> responses = feeds.stream()
                .map(feed -> {
                    User author = userMap.get(feed.getUserId());
                    if (author == null) return null;
                    return FeedResponse.from(
                            feed, author,
                            likedIds.contains(feed.getId()),
                            true, // 이 목록의 피드는 모두 내가 리포스트한 것
                            bookmarkedIds.contains(feed.getId()),
                            followingAuthorIds.contains(feed.getUserId())
                    );
                })
                .filter(r -> r != null)
                .toList();
        return FeedCursorResponse.of(responses, size);
    }

    /**
     * 본인이 작성한 ACTIVE 피드를 찾음
     * 수정/삭제에서 공통으로 사용하는 내부 메서드
     *
     * @param userId 요청자 ID
     * @param feedId 대상 피드 ID
     * @return 본인의 ACTIVE 피드
     * @throws BusinessException NOT_FOUND — 피드 없음 / UNAUTHORIZED — 본인 아님
     */
    private Feed findOwnActiveFeed(Long userId, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .filter(f -> f.getStatus() == FeedStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "피드를 찾을 수 없습니다"));
        if (!feed.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 피드만 수정/삭제할 수 있습니다");
        }
        return feed;
    }
}
