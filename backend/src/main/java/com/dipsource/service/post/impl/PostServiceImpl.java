package com.dipsource.service.post.impl;

import com.dipsource.dto.post.PostDetailResponse;
import com.dipsource.dto.post.PostIdResponse;
import com.dipsource.dto.post.PostSummaryResponse;
import com.dipsource.dto.post.WritePostRequest;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostCategory;
import com.dipsource.entity.post.PostStatus;
import com.dipsource.entity.post.PostTag;
import com.dipsource.entity.post.PostUserId;
import com.dipsource.entity.post.SubscriptionStatus;
import com.dipsource.entity.tag.Tag;
import com.dipsource.global.common.LikePattern;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.post.BlogSubscriptionRepository;
import com.dipsource.repository.post.PostBookmarkRepository;
import com.dipsource.repository.post.PostLikeRepository;
import com.dipsource.repository.post.PostPurchaseRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.post.PostTagRepository;
import com.dipsource.repository.tag.TagRepository;
import com.dipsource.service.post.PostService;
import com.dipsource.service.user.UserService;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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

    private static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    private static final Sort POPULAR = Sort.by(
            Sort.Order.desc("likeCount"), Sort.Order.desc("viewCount"), Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostBookmarkRepository postBookmarkRepository;
    private final PostPurchaseRepository postPurchaseRepository;
    private final BlogSubscriptionRepository blogSubscriptionRepository;
    private final TagRepository tagRepository;
    private final UserService userService;
    private final PostViewCounter postViewCounter;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getPosts(
            String category, String q, String sort, Long viewerId, Pageable pageable) {
        Set<PostCategory> categories = (category == null || category.isBlank())
                ? EnumSet.allOf(PostCategory.class)
                : EnumSet.of(parseCategory(category));
        Sort order = "popular".equals(sort) ? POPULAR : LATEST;
        Page<Post> page = postRepository.findList(
                PostStatus.ACTIVE, categories, pattern(q), limit(pageable, order));
        return toSummaries(page, viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getUserPosts(Long userId, Long viewerId, Pageable pageable) {
        // 공개 글만 → 삭제한 글은 작성자 블로그 목록에서도 빠진다.
        return toSummaries(
                postRepository.findByUserIdAndStatus(userId, PostStatus.ACTIVE, limit(pageable, LATEST)), viewerId);
    }

    @Override
    @Transactional
    public PostDetailResponse getPost(Long postId, Long viewerId) {
        Post post = findActive(postId);
        boolean mine = post.getUserId().equals(viewerId);
        // 작성자 본인은 세지 않고, 로그인 회원은 같은 글을 한 시간에 한 번만 센다.
        if (!mine && postViewCounter.shouldCount(postId, viewerId)) {
            postRepository.incrementViewCount(postId);
            post = findActive(postId); // 올라간 조회수로 다시 읽는다
        }
        UserSummaryResponse author = userService.getSummaries(List.of(post.getUserId())).get(post.getUserId());
        List<String> tags = postTagRepository.findTagNames(postId);
        boolean liked = viewerId != null && postLikeRepository.existsById(new PostUserId(viewerId, postId));
        boolean bookmarked = viewerId != null && postBookmarkRepository.existsById(new PostUserId(viewerId, postId));
        return PostDetailResponse.of(post, author, tags, canReadAll(post, viewerId), liked, bookmarked);
    }

    @Override
    @Transactional
    public PostIdResponse create(Long userId, WritePostRequest request) {
        if (!userService.isActiveUser(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "글을 쓸 수 없는 계정이에요.");
        }
        int price = request.price();
        Post post = postRepository.save(Post.builder()
                .userId(userId)
                .demoId(request.demoId())
                .title(request.title().strip())
                .category(parseCategory(request.category()))
                .content(request.content())
                .paid(price > 0)
                .priceChips(price)
                .build());
        attachTags(post, request.tags());
        return new PostIdResponse(post.getId());
    }

    @Override
    @Transactional
    public void update(Long userId, Long postId, WritePostRequest request) {
        Post post = findOwn(userId, postId);
        int price = request.price();
        post.update(request.title().strip(), parseCategory(request.category()), request.content(),
                post.getThumbnailUrl(), request.demoId(), price > 0, price);
        // 태그는 지우고 다시 붙인다.
        postTagRepository.deleteByPostId(postId);
        attachTags(postRepository.getReferenceById(postId), request.tags());
    }

    @Override
    @Transactional
    public void delete(Long userId, Long postId) {
        findOwn(userId, postId).delete();
    }

    @Override
    @Transactional
    public void like(Long userId, Long postId) {
        findActive(postId); // 삭제·숨김 글은 404
        if (postLikeRepository.insertIgnore(userId, postId) > 0) {
            postRepository.incrementLikeCount(postId);
        }
    }

    @Override
    @Transactional
    public void unlike(Long userId, Long postId) {
        findActive(postId);
        if (postLikeRepository.deleteByUserIdAndPostId(userId, postId) > 0) {
            postRepository.decrementLikeCount(postId);
        }
    }

    @Override
    @Transactional
    public void bookmark(Long userId, Long postId) {
        findActive(postId); // 삭제·숨김 글은 404
        postBookmarkRepository.insertIgnore(userId, postId);
    }

    @Override
    @Transactional
    public void unbookmark(Long userId, Long postId) {
        findActive(postId);
        postBookmarkRepository.deleteByUserIdAndPostId(userId, postId);
    }

    /** 유료 글 본문 전체를 볼 수 있는지: 무료 글, 작성자, 구매자, 구독자 */
    private boolean canReadAll(Post post, Long viewerId) {
        if (!post.isPaid()) {
            return true;
        }
        if (viewerId == null) {
            return false;
        }
        return post.getUserId().equals(viewerId)
                || postPurchaseRepository.existsById(new PostUserId(viewerId, post.getId()))
                || blogSubscriptionRepository.existsBySubscriberIdAndBlogOwnerIdAndStatusAndExpiresAtAfter(
                        viewerId, post.getUserId(), SubscriptionStatus.ACTIVE, LocalDateTime.now());
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

    private PageResponse<PostSummaryResponse> toSummaries(Page<Post> page, Long viewerId) {
        List<Long> authorIds = page.getContent().stream().map(Post::getUserId).distinct().toList();
        Map<Long, UserSummaryResponse> authors = userService.getSummaries(authorIds);
        List<Long> postIds = page.getContent().stream().map(Post::getId).toList();
        Set<Long> liked = viewerId == null || postIds.isEmpty()
                ? Set.of()
                : new HashSet<>(postLikeRepository.findLikedPostIds(viewerId, postIds));
        return PageResponse.of(page, post ->
                PostSummaryResponse.of(post, authors.get(post.getUserId()), liked.contains(post.getId())));
    }

    private Post findActive(Long postId) {
        return postRepository.findById(postId)
                .filter(post -> post.getStatus() == PostStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "글을 찾을 수 없어요."));
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
