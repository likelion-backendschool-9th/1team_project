package com.dipsource.service.post.impl;

import com.dipsource.dto.post.BlogSubscriptionInfoResponse;
import com.dipsource.dto.post.MySubscriptionResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.notification.NotificationType;
import com.dipsource.entity.post.BlogSubscription;
import com.dipsource.entity.post.SubscriptionStatus;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.post.BlogSubscriptionRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.coin.CoinService;
import com.dipsource.service.notification.NotificationService;
import com.dipsource.service.post.BlogSubscriptionService;
import com.dipsource.service.user.UserService;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link BlogSubscriptionService} 구현체. */
@Service
@RequiredArgsConstructor
public class BlogSubscriptionServiceImpl implements BlogSubscriptionService {

    private final BlogSubscriptionRepository blogSubscriptionRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final BlockRepository blockRepository;
    private final CoinService coinService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void subscribe(Long subscriberId, Long blogOwnerId) {
        if (blogOwnerId == null || blogOwnerId.equals(subscriberId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "내 블로그는 구독할 수 없어요.");
        }
        // 구독하는 회원을 잠가서, 버튼을 연타해도 구독·결제가 한 번만 생기게 한다
        User subscriber = userRepository.findByIdForUpdate(subscriberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요."));
        if (subscriber.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "구독할 수 없는 계정이에요.");
        }
        User owner = findVisibleOwner(blogOwnerId, subscriberId);
        int price = owner.getBlogSubscriptionPrice();
        if (price <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "구독을 받지 않는 블로그예요.");
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<BlogSubscription> current = blogSubscriptionRepository.findCurrent(subscriberId, blogOwnerId, now);
        if (current.isPresent()) {
            // 이미 낸 기간이 남아 있으면 새로 결제하지 않는다. 해지했던 구독은 다시 이어 준다
            BlogSubscription subscription = current.get();
            if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
                subscription.renew(subscription.getExpiresAt());
            }
            return;
        }

        BlogSubscription subscription = blogSubscriptionRepository.save(BlogSubscription.builder()
                .subscriberId(subscriberId)
                .blogOwnerId(blogOwnerId)
                .paidPrice(price)
                .startedAt(now)
                .expiresAt(now.plusMonths(1))
                .build());
        coinService.payBlogSubscription(subscriberId, subscription.getId(), price);
        notificationService.notify(blogOwnerId, subscriberId, NotificationType.SUBSCRIBE, null, null);
    }

    @Override
    @Transactional
    public void cancel(Long subscriberId, Long subscriptionId) {
        if (subscriptionId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "해지할 구독을 골라 주세요.");
        }
        BlogSubscription subscription = blogSubscriptionRepository.findById(subscriptionId)
                .filter(s -> s.getSubscriberId().equals(subscriberId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "구독을 찾을 수 없어요."));
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            subscription.cancel();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MySubscriptionResponse> getMine(Long subscriberId) {
        // 서로 차단한 블로그는 보여 주지 않는다. 결제한 구독이라 지우지는 않고 화면에서만 숨긴다
        Set<Long> hidden = new HashSet<>(blockRepository.findHiddenUserIds(subscriberId));
        List<BlogSubscription> subscriptions =
                blogSubscriptionRepository.findCurrentBySubscriber(subscriberId, LocalDateTime.now()).stream()
                        .filter(s -> !hidden.contains(s.getBlogOwnerId()))
                        .toList();
        if (subscriptions.isEmpty()) {
            return List.of();
        }
        Map<Long, UserSummaryResponse> owners = userService.getSummaries(
                subscriptions.stream().map(BlogSubscription::getBlogOwnerId).distinct().toList());
        return subscriptions.stream()
                .filter(s -> owners.containsKey(s.getBlogOwnerId()))
                .map(s -> MySubscriptionResponse.of(s, owners.get(s.getBlogOwnerId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BlogSubscriptionInfoResponse getInfo(Long blogOwnerId, Long viewerId) {
        User owner = findVisibleOwner(blogOwnerId, viewerId);
        LocalDateTime now = LocalDateTime.now();
        boolean subscribed = viewerId != null && !viewerId.equals(blogOwnerId)
                && blogSubscriptionRepository.existsBySubscriberIdAndBlogOwnerIdAndStatusAndExpiresAtAfter(
                        viewerId, blogOwnerId, SubscriptionStatus.ACTIVE, now);
        long subscriberCount = blogSubscriptionRepository.countByBlogOwnerIdAndStatusAndExpiresAtAfter(
                blogOwnerId, SubscriptionStatus.ACTIVE, now);
        return new BlogSubscriptionInfoResponse(owner.getBlogSubscriptionPrice(), subscribed, subscriberCount);
    }

    @Override
    @Transactional
    public void changePrice(Long userId, int price) {
        if (price < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "구독료는 0원 이상으로 정해 주세요.");
        }
        User user = userRepository.findById(userId)
                .filter(found -> found.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "구독료를 바꿀 수 없는 계정이에요."));
        user.changeBlogSubscriptionPrice(price);
    }

    /** 활동 중이고, 보는 사람과 차단 관계가 아닌 블로그 주인. 아니면 404 */
    private User findVisibleOwner(Long blogOwnerId, Long viewerId) {
        User owner = userRepository.findById(blogOwnerId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "블로그를 찾을 수 없어요."));
        if (viewerId != null && !viewerId.equals(blogOwnerId) && blockRepository.existsBetween(viewerId, blogOwnerId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "블로그를 찾을 수 없어요.");
        }
        return owner;
    }
}
