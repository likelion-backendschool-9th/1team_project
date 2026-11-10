package com.dipsource.service.post.impl;

import com.dipsource.dto.post.BlogStatsResponse;
import com.dipsource.dto.post.PostDetailResponse;
import com.dipsource.dto.post.PostIdResponse;
import com.dipsource.dto.post.PostSummaryResponse;
import com.dipsource.dto.post.WritePostRequest;
import com.dipsource.dto.search.SearchTargetChangedEvent;
import com.dipsource.dto.search.SearchType;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostCategory;
import com.dipsource.entity.post.PostSeries;
import com.dipsource.entity.post.PostStatus;
import com.dipsource.entity.post.PostTag;
import com.dipsource.entity.post.PostUserId;
import com.dipsource.entity.tag.Tag;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.common.LikePattern;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.post.BlogSubscriptionRepository;
import com.dipsource.repository.post.PostBookmarkRepository;
import com.dipsource.repository.post.PostLikeRepository;
import com.dipsource.repository.post.PostPurchaseRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.post.PostSeriesRepository;
import com.dipsource.repository.post.SeriesPurchaseRepository;
import com.dipsource.repository.post.PostTagRepository;
import com.dipsource.repository.tag.TagRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.coin.CoinService;
import com.dipsource.service.post.PostService;
import com.dipsource.service.user.UserService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link PostService} 구현체. */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    /** 목록 한 번에 최대 몇 개까지 주는지 */
    private static final int MAX_PAGE_SIZE = 50;

    /** 검색어 최대 길이 */
    private static final int MAX_QUERY_LENGTH = 50;

    /** 칩으로 파는 유료 글 본문 최소 글자 수. 시리즈 회차는 제외 */
    static final int PAID_MIN_CONTENT_LENGTH = 1000;

    private static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    private static final Sort POPULAR = Sort.by(
            Sort.Order.desc("likeCount"), Sort.Order.desc("viewCount"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    private static final Sort VIEWS = Sort.by(
            Sort.Order.desc("viewCount"), Sort.Order.desc("likeCount"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostBookmarkRepository postBookmarkRepository;
    private final PostTagRepository postTagRepository;
    private final PostPurchaseRepository postPurchaseRepository;
    private final BlogSubscriptionRepository blogSubscriptionRepository;
    private final PostSeriesRepository postSeriesRepository;
    private final SeriesPurchaseRepository seriesPurchaseRepository;
    private final TagRepository tagRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final BlockRepository blockRepository;
    private final PostViewCounter postViewCounter;
    private final CoinService coinService;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getPosts(
            String category, String q, String sort, Long viewerId, Pageable pageable) {
        Set<PostCategory> categories = (category == null || category.isBlank())
                ? EnumSet.allOf(PostCategory.class)
                : EnumSet.of(parseCategory(category));
        Sort order = switch (sort == null ? "" : sort) {
            case "popular" -> POPULAR;
            case "views" -> VIEWS;
            default -> LATEST;
        };
        Page<Post> page = postRepository.findList(
                PostStatus.ACTIVE, categories, pattern(q), viewerId, limit(pageable, order));
        return toSummaries(page, viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getUserPosts(Long userId, Long viewerId, Pageable pageable) {
        if (hiddenFrom(viewerId, userId)) {
            return PageResponse.of(Page.empty(limit(pageable, LATEST)), List.of());
        }
        return toSummaries(postRepository.findByUserIdAndStatus(userId, PostStatus.ACTIVE, limit(pageable, LATEST)), viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogStatsResponse getBlogStats(Long userId, Long viewerId) {
        User owner = userRepository.findById(userId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "블로그를 찾을 수 없어요."));
        if (hiddenFrom(viewerId, userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "블로그를 찾을 수 없어요.");
        }
        return new BlogStatsResponse(
                owner.getBio() == null ? "" : owner.getBio(),
                postRepository.countByUserIdAndStatus(userId, PostStatus.ACTIVE),
                postRepository.sumViewCountByUserIdAndStatus(userId, PostStatus.ACTIVE));
    }

    @Override
    @Transactional
    public PostDetailResponse getPost(Long postId, Long viewerId) {
        Post post = findActive(postId);
        if (hiddenFrom(viewerId, post.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "글을 찾을 수 없습니다");
        }
        boolean mine = post.getUserId().equals(viewerId);
        // 작성자 본인은 세지 않고, 로그인 회원은 같은 글을 한 시간에 한 번만 센다.
        if (!mine && postViewCounter.shouldCount(postId, viewerId)) {
            postRepository.incrementViewCount(postId);
            post = findActive(postId); // 올라간 조회수로 다시 읽는다
        }
        UserSummaryResponse author = userService.getSummaries(List.of(post.getUserId())).get(post.getUserId());
        List<String> tags = postTagRepository.findTagNames(postId);
        boolean liked = viewerId != null && !postLikeRepository.findLikedPostIds(viewerId, List.of(postId)).isEmpty();
        boolean bookmarked = viewerId != null && postBookmarkRepository.existsById(new PostUserId(viewerId, postId));
        String seriesTitle = post.getSeriesId() == null ? null
                : postSeriesRepository.findById(post.getSeriesId()).map(PostSeries::getTitle).orElse(null);
        return PostDetailResponse.of(post, author, tags, canReadAll(post, viewerId), liked, bookmarked, seriesTitle);
    }

    @Override
    @Transactional
    public PostIdResponse create(Long userId, WritePostRequest request) {
        if (!userService.isActiveUser(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "글을 쓸 수 없는 계정이에요.");
        }
        PostSeries series = findOwnSeries(userId, request.seriesId());
        boolean paid = isPaid(request, series);
        requirePaidLength(paid, series, request.content());
        Post post = Post.builder()
                .userId(userId)
                .demoId(request.demoId())
                .title(request.title().strip())
                .category(parseCategory(request.category()))
                .content(request.content())
                .paid(paid)
                .priceChips(request.price())
                .build();
        if (series != null) {
            post.placeInSeries(series.getId(), postRepository.findMaxSeriesOrder(series.getId()) + 1);
        }
        post = postRepository.save(post);
        attachTags(post, request.tags());
        return new PostIdResponse(post.getId());
    }

    @Override
    @Transactional
    public void update(Long userId, Long postId, WritePostRequest request) {
        Post post = findOwn(userId, postId);
        PostSeries series = findOwnSeries(userId, request.seriesId());
        boolean paid = isPaid(request, series);
        requirePaidLength(paid, series, request.content());
        post.update(request.title().strip(), parseCategory(request.category()), request.content(),
                post.getThumbnailUrl(), request.demoId(), paid, request.price());
        Long seriesId = series == null ? null : series.getId();
        if (!Objects.equals(post.getSeriesId(), seriesId)) {
            // 다른 시리즈로 옮기면 그 시리즈의 마지막 회차 뒤에 붙는다
            post.placeInSeries(seriesId, seriesId == null ? null : postRepository.findMaxSeriesOrder(seriesId) + 1);
        } else if (seriesId != null) {
            // 같은 시리즈면 회차 번호는 그대로 두고 칩 가격만 지운다
            post.placeInSeries(seriesId, post.getSeriesOrder());
        }
        // 태그는 지우고 다시 붙인다.
        postTagRepository.deleteByPostId(postId);
        attachTags(postRepository.getReferenceById(postId), request.tags());
        // 태그 삭제는 벌크 쿼리라 검색 색인 동기화에 잡히지 않는다
        eventPublisher.publishEvent(new SearchTargetChangedEvent(SearchType.POST, postId));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long postId) {
        findOwn(userId, postId).delete();
    }

    @Override
    @Transactional
    public void deleteByAdmin(Long postId) {
        Post post = postRepository.findById(postId)
                .filter(p -> p.getStatus() != PostStatus.DELETED)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "블로그 글을 찾을 수 없습니다"));
        post.delete();
    }

    @Override
    @Transactional
    public void like(Long userId, Long postId) {
        Post post = findActive(postId);
        if (post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "본인 글에는 좋아요할 수 없어요.");
        }
        int inserted = postLikeRepository.insertIgnore(userId, postId);
        if (inserted > 0) {
            postRepository.incrementLikeCount(postId);
        }
    }

    @Override
    @Transactional
    public void unlike(Long userId, Long postId) {
        findActive(postId);
        int deleted = postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        if (deleted > 0) {
            postRepository.decrementLikeCount(postId);
        }
    }

    @Override
    @Transactional
    public void purchase(Long userId, Long postId) {
        Post post = findActive(postId);
        if (hiddenFrom(userId, post.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "글을 찾을 수 없어요.");
        }
        // 시리즈 안 유료 글은 한 편씩 팔지 않는다 (팀 결정)
        if (post.getSeriesId() != null && post.isPaid()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "시리즈를 사면 읽을 수 있어요.");
        }
        if (!post.isPaid() || post.getPriceChips() <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "무료 글이에요.");
        }
        if (post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "내 글은 구매하지 않아도 읽을 수 있어요.");
        }
        // 구독자는 유료 글을 무료로 읽는다. 구독하지 않은 회원만 단건 구매한다 (테이블정의서 posts.is_paid)
        if (isSubscriber(userId, post.getUserId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "구독 중이라 이미 읽을 수 있어요.");
        }
        // insertIgnore는 영속성 컨텍스트를 비워(clearAutomatically) 이후 post는 detached가 된다.
        // 지연 로딩 필드를 건드리지 않도록 필요한 값은 여기서 미리 읽어 둔다.
        Long authorId = post.getUserId();
        String title = post.getTitle();
        int price = post.getPriceChips();
        // 구매 기록을 먼저 넣어 중복 결제를 막는다. 이미 샀으면 칩을 다시 빼지 않고 끝낸다 (멱등)
        int inserted = postPurchaseRepository.insertIgnore(userId, postId, price);
        if (inserted == 0) {
            return;
        }
        // 잔액 부족·글쓴이 정지면 예외가 나서 위의 구매 기록도 함께 롤백된다
        coinService.payForPost(userId, authorId, postId, title, price);
    }

    @Override
    @Transactional
    public void bookmark(Long userId, Long postId) {
        Post post = findActive(postId);
        // 상세에서 404인 글(차단 관계)은 북마크도 404로 맞춘다.
        if (hiddenFrom(userId, post.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "글을 찾을 수 없어요.");
        }
        postBookmarkRepository.insertIgnore(userId, postId);
    }

    @Override
    @Transactional
    public void unbookmark(Long userId, Long postId) {
        findActive(postId);
        // 차단 관계여도 취소는 막지 않는다. 차단할 때 북마크를 이미 지우므로 보통은 지울 게 없고, 남아 있으면 정리되는 쪽이 낫다.
        postBookmarkRepository.deleteByUserIdAndPostId(userId, postId);
    }

    /** 유료 글 본문 전체를 볼 수 있는지: 무료 글, 작성자, 시리즈 안 글은 시리즈 구매자, 시리즈 밖 글은 구매자·구독자 */
    private boolean canReadAll(Post post, Long viewerId) {
        if (!post.isPaid()) {
            return true;
        }
        if (viewerId == null) {
            return false;
        }
        if (post.getUserId().equals(viewerId)) {
            return true;
        }
        if (post.getSeriesId() != null) {
            return seriesPurchaseRepository.existsByUserIdAndSeriesId(viewerId, post.getSeriesId());
        }
        return postPurchaseRepository.existsById(new PostUserId(viewerId, post.getId()))
                || isSubscriber(viewerId, post.getUserId());
    }

    /** 유료 글을 읽을 수 있는 구독자인지. 해지(CANCELED)한 구독도 만료일까지는 읽을 수 있다 */
    private boolean isSubscriber(Long userId, Long blogOwnerId) {
        return blogSubscriptionRepository.existsReadable(userId, blogOwnerId, LocalDateTime.now());
    }

    /** 이름으로 태그를 찾고, 없으면 만든다. 이름은 소문자로 맞추고 중복은 하나로 친다. */
    private void attachTags(Post post, List<String> names) {
        if (names == null) {
            return;
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                normalized.add(name.strip().toLowerCase(Locale.ROOT));
            }
        }
        for (String name : normalized) {
            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
            postTagRepository.save(new PostTag(post, tag.getId()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActivePostOf(Long postId, Long userId) {
        if (postId == null || userId == null) {
            return false;
        }
        return postRepository.existsByIdAndUserIdAndStatus(postId, userId, PostStatus.ACTIVE);
    }

    private PageResponse<PostSummaryResponse> toSummaries(Page<Post> page, Long viewerId) {
        List<Long> authorIds = page.getContent().stream().map(Post::getUserId).distinct().toList();
        Map<Long, UserSummaryResponse> authors = userService.getSummaries(authorIds);
        List<Long> postIds = page.getContent().stream().map(Post::getId).toList();
        Set<Long> likedIds = viewerId != null && !postIds.isEmpty()
                ? new java.util.HashSet<>(postLikeRepository.findLikedPostIds(viewerId, postIds))
                : Collections.emptySet();
        return PageResponse.of(page, post -> PostSummaryResponse.of(post, authors.get(post.getUserId()), likedIds.contains(post.getId())));
    }

    private boolean hiddenFrom(Long viewerId, Long authorId) {
        return viewerId != null && !viewerId.equals(authorId) && blockRepository.existsBetween(viewerId, authorId);
    }

    private Post findActive(Long postId) {
        return postRepository.findById(postId)
                .filter(post -> post.getStatus() == PostStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "글을 찾을 수 없어요."));
    }

    /**
     * 칩으로 한 편씩 파는 유료 글은 돈을 내고 읽을 만큼 본문이 있어야 한다. 앞뒤 공백은 세지 않는다.
     * 시리즈 회차는 시리즈 전체를 사는 것이라 길이 제한이 없다 (팀 결정).
     */
    private static void requirePaidLength(boolean paid, PostSeries series, String content) {
        if (paid && series == null && content.strip().length() < PAID_MIN_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "유료 글은 본문을 " + PAID_MIN_CONTENT_LENGTH + "자 이상 써 주세요.");
        }
    }

    /** 글을 넣을 내 시리즈. seriesId가 null이면 null, 없거나 남의 시리즈면 400 */
    private PostSeries findOwnSeries(Long userId, Long seriesId) {
        if (seriesId == null) {
            return null;
        }
        return postSeriesRepository.findById(seriesId)
                .filter(series -> series.getUserId().equals(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "내 시리즈를 골라 주세요."));
    }

    /**
     * 잠글 글인지. 시리즈 밖 글은 칩 가격이 있으면 유료,
     * 시리즈 안 글은 paid로 정하고(없으면 가격으로), 무료 시리즈에는 유료 회차를 둘 수 없다.
     */
    private static boolean isPaid(WritePostRequest request, PostSeries series) {
        if (series == null) {
            return request.price() > 0;
        }
        boolean paid = request.paid() != null ? request.paid() : request.price() > 0;
        if (paid && series.getPriceKrw() <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "무료 시리즈에는 유료 회차를 넣을 수 없어요. 시리즈 가격을 먼저 정해 주세요.");
        }
        return paid;
    }

    private Post findOwn(Long userId, Long postId) {
        Post post = findActive(postId);
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "내 글만 수정·삭제할 수 있어요.");
        }
        return post;
    }

    private static PostCategory parseCategory(String code) {
        return PostCategory.fromCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "분류를 확인해 주세요."));
    }

    private static String pattern(String q) {
        if (q == null || q.isBlank()) {
            return "%";
        }
        String keyword = q.strip().toLowerCase(Locale.ROOT);
        return LikePattern.contains(keyword.length() > MAX_QUERY_LENGTH ? keyword.substring(0, MAX_QUERY_LENGTH) : keyword);
    }

    private static Pageable limit(Pageable pageable, Sort sort) {
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE), sort);
    }
}
