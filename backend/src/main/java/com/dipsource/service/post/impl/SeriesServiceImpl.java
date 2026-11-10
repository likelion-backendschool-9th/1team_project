package com.dipsource.service.post.impl;

import com.dipsource.dto.post.MySeriesPurchaseResponse;
import com.dipsource.dto.post.SeriesDetailResponse;
import com.dipsource.dto.post.SeriesIdResponse;
import com.dipsource.dto.post.SeriesSummaryResponse;
import com.dipsource.dto.post.SeriesWriteRequest;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.notification.NotificationType;
import com.dipsource.entity.post.PostSeries;
import com.dipsource.entity.post.PostStatus;
import com.dipsource.entity.post.SeriesPurchase;
import com.dipsource.entity.post.SeriesStatus;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.post.PostSeriesRepository;
import com.dipsource.repository.post.SeriesPurchaseRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.coin.CoinService;
import com.dipsource.service.notification.NotificationService;
import com.dipsource.service.post.SeriesService;
import com.dipsource.service.user.UserService;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link SeriesService} 구현체. */
@Service
@RequiredArgsConstructor
public class SeriesServiceImpl implements SeriesService {

    /** 인기 시리즈 한 번에 최대 몇 개까지 주는지 */
    private static final int MAX_POPULAR = 20;

    private final PostSeriesRepository seriesRepository;
    private final SeriesPurchaseRepository purchaseRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final BlockRepository blockRepository;
    private final CoinService coinService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public SeriesIdResponse create(Long userId, SeriesWriteRequest request) {
        if (!userService.isActiveUser(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "시리즈를 만들 수 없는 계정이에요.");
        }
        PostSeries series = seriesRepository.save(PostSeries.builder()
                .userId(userId)
                .title(request.title().strip())
                .description(description(request))
                .priceKrw(price(request))
                .build());
        return new SeriesIdResponse(series.getId());
    }

    @Override
    @Transactional
    public void update(Long userId, Long seriesId, SeriesWriteRequest request) {
        PostSeries series = findOwn(userId, seriesId);
        int price = price(request);
        // 무료 시리즈에는 유료 회차를 둘 수 없다. 이미 유료 회차가 있으면 아무도 열 수 없게 되므로 막는다
        if (price == 0 && postRepository.existsBySeriesIdAndPaidTrueAndStatusNot(seriesId, PostStatus.DELETED)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "유료 회차가 있어 0원으로 바꿀 수 없어요. 회차를 무료로 바꾼 뒤 다시 해 주세요.");
        }
        series.update(request.title().strip(), description(request), price, parseStatus(request.status()));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long seriesId) {
        PostSeries series = findOwn(userId, seriesId);
        if (postRepository.existsBySeriesIdAndStatusNot(seriesId, PostStatus.DELETED)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "회차가 남아 있어 지울 수 없어요. 글을 시리즈에서 빼거나 지운 뒤 다시 해 주세요.");
        }
        if (purchaseRepository.existsBySeriesId(seriesId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "산 사람이 있는 시리즈는 지울 수 없어요.");
        }
        seriesRepository.delete(series);
    }

    @Override
    @Transactional(readOnly = true)
    public SeriesDetailResponse getSeries(Long seriesId, Long viewerId) {
        PostSeries series = findVisible(seriesId, viewerId);
        boolean mine = series.getUserId().equals(viewerId);
        boolean purchased = mine || (viewerId != null && purchaseRepository.existsByUserIdAndSeriesId(viewerId, seriesId));
        List<SeriesDetailResponse.Episode> episodes = postRepository
                .findBySeriesIdAndStatusOrderBySeriesOrderAscIdAsc(seriesId, PostStatus.ACTIVE).stream()
                .map(post -> new SeriesDetailResponse.Episode(
                        post.getId(), post.getSeriesOrder(), post.getTitle(), post.isPaid(), post.getCreatedAt()))
                .toList();
        return new SeriesDetailResponse(summarize(List.of(series)).getFirst(), purchased, mine, episodes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeriesSummaryResponse> getUserSeries(Long userId, Long viewerId) {
        if (viewerId != null && !viewerId.equals(userId) && blockRepository.existsBetween(viewerId, userId)) {
            return List.of();
        }
        return summarize(seriesRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeriesSummaryResponse> getPopular(Long viewerId, int size) {
        int limit = Math.max(1, Math.min(size, MAX_POPULAR));
        return summarize(seriesRepository.findPopular(viewerId, PageRequest.of(0, limit)));
    }

    @Override
    @Transactional
    public void purchase(Long userId, Long seriesId) {
        // 사는 회원을 잠가서, 버튼을 연타해도 결제가 한 번만 생기게 한다
        User buyer = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요."));
        if (buyer.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "구매할 수 없는 계정이에요.");
        }
        PostSeries series = findVisible(seriesId, userId);
        if (series.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "내 시리즈는 사지 않아도 읽을 수 있어요.");
        }
        int price = series.getPriceKrw();
        if (price <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "무료 시리즈예요.");
        }
        Long authorId = series.getUserId();
        // 구매 기록을 먼저 넣어 중복 결제를 막는다. 이미 샀으면 다시 결제하지 않고 끝낸다 (멱등)
        if (purchaseRepository.insertIgnore(userId, seriesId, price) == 0) {
            return;
        }
        coinService.paySeries(userId, seriesId, price);
        notificationService.notify(authorId, userId, NotificationType.SERIES_PURCHASE,
                NotificationService.TARGET_SERIES, seriesId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MySeriesPurchaseResponse> getMyPurchases(Long userId) {
        List<SeriesPurchase> purchases = purchaseRepository.findByUserIdOrderByPurchasedAtDescIdDesc(userId);
        if (purchases.isEmpty()) {
            return List.of();
        }
        // 서로 차단한 작가의 시리즈는 보여 주지 않는다. 산 기록이라 지우지는 않고 화면에서만 숨긴다
        Set<Long> hidden = new HashSet<>(blockRepository.findHiddenUserIds(userId));
        Map<Long, PostSeries> seriesById = new HashMap<>();
        seriesRepository.findAllById(purchases.stream().map(SeriesPurchase::getSeriesId).toList())
                .forEach(series -> seriesById.put(series.getId(), series));
        List<PostSeries> visible = purchases.stream()
                .map(purchase -> seriesById.get(purchase.getSeriesId()))
                .filter(series -> series != null && !hidden.contains(series.getUserId()))
                .toList();
        Map<Long, SeriesSummaryResponse> summaries = new HashMap<>();
        summarize(visible).forEach(summary -> summaries.put(summary.id(), summary));
        return purchases.stream()
                .filter(purchase -> summaries.containsKey(purchase.getSeriesId()))
                .map(purchase -> new MySeriesPurchaseResponse(
                        summaries.get(purchase.getSeriesId()), purchase.getPaidPrice(), purchase.getPurchasedAt()))
                .toList();
    }

    /** 시리즈 목록에 작가·회차 수·구매 수를 붙인다. 탈퇴 등으로 작가 정보가 없으면 뺀다 */
    private List<SeriesSummaryResponse> summarize(List<PostSeries> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        List<Long> ids = list.stream().map(PostSeries::getId).toList();
        Map<Long, UserSummaryResponse> authors = userService.getSummaries(
                list.stream().map(PostSeries::getUserId).distinct().toList());
        Map<Long, SeriesSummaryResponse.Stats> stats = stats(ids);
        return list.stream()
                .filter(series -> authors.containsKey(series.getUserId()))
                .map(series -> SeriesSummaryResponse.of(series, authors.get(series.getUserId()),
                        stats.getOrDefault(series.getId(), SeriesSummaryResponse.Stats.EMPTY)))
                .toList();
    }

    private Map<Long, SeriesSummaryResponse.Stats> stats(Collection<Long> ids) {
        Map<Long, Long> purchases = new HashMap<>();
        for (Object[] row : purchaseRepository.countBySeriesIds(ids)) {
            purchases.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        Map<Long, SeriesSummaryResponse.Stats> result = new HashMap<>();
        for (Object[] row : postRepository.summarizeSeries(ids)) {
            Long id = ((Number) row[0]).longValue();
            result.put(id, new SeriesSummaryResponse.Stats(
                    ((Number) row[1]).intValue(),
                    ((Number) row[2]).intValue(),
                    row[3] == null ? null : ((Number) row[3]).longValue(),
                    purchases.getOrDefault(id, 0L)));
        }
        // 회차가 없지만 산 사람이 있는 시리즈
        purchases.forEach((id, count) -> result.putIfAbsent(id, new SeriesSummaryResponse.Stats(0, 0, null, count)));
        return result;
    }

    /** 작가가 활동 중이고 보는 사람과 차단 관계가 아닌 시리즈. 아니면 404 */
    private PostSeries findVisible(Long seriesId, Long viewerId) {
        PostSeries series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "시리즈를 찾을 수 없어요."));
        Long authorId = series.getUserId();
        boolean blocked = viewerId != null && !viewerId.equals(authorId) && blockRepository.existsBetween(viewerId, authorId);
        if (blocked || !userService.isActiveUser(authorId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "시리즈를 찾을 수 없어요.");
        }
        return series;
    }

    private PostSeries findOwn(Long userId, Long seriesId) {
        PostSeries series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "시리즈를 찾을 수 없어요."));
        if (!series.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "내 시리즈만 고칠 수 있어요.");
        }
        return series;
    }

    private static String description(SeriesWriteRequest request) {
        return request.description() == null ? "" : request.description().strip();
    }

    private static int price(SeriesWriteRequest request) {
        if (request.price() == null || request.price() < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "가격은 0원 이상으로 정해 주세요.");
        }
        return request.price();
    }

    private static SeriesStatus parseStatus(String status) {
        if (status == null || status.isBlank() || "ongoing".equals(status)) {
            return SeriesStatus.ONGOING;
        }
        if ("completed".equals(status)) {
            return SeriesStatus.COMPLETED;
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "연재 상태를 확인해 주세요.");
    }
}
