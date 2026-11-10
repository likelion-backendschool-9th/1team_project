package com.dipsource.service.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.post.SeriesDetailResponse;
import com.dipsource.dto.post.SeriesWriteRequest;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.notification.NotificationType;
import com.dipsource.entity.post.PostSeries;
import com.dipsource.entity.post.PostStatus;
import com.dipsource.entity.post.SeriesStatus;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.post.PostSeriesRepository;
import com.dipsource.repository.post.SeriesPurchaseRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.coin.CoinService;
import com.dipsource.service.notification.NotificationService;
import com.dipsource.service.post.impl.SeriesServiceImpl;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SeriesServiceImplTest {

    private static final Long AUTHOR = 1L;
    private static final Long BUYER = 2L;
    private static final Long SERIES = 3L;

    private PostSeriesRepository seriesRepository;
    private SeriesPurchaseRepository purchaseRepository;
    private PostRepository postRepository;
    private UserRepository userRepository;
    private UserService userService;
    private BlockRepository blockRepository;
    private CoinService coinService;
    private NotificationService notificationService;
    private SeriesServiceImpl service;

    @BeforeEach
    void setUp() {
        seriesRepository = mock(PostSeriesRepository.class);
        purchaseRepository = mock(SeriesPurchaseRepository.class);
        postRepository = mock(PostRepository.class);
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        blockRepository = mock(BlockRepository.class);
        coinService = mock(CoinService.class);
        notificationService = mock(NotificationService.class);
        service = new SeriesServiceImpl(seriesRepository, purchaseRepository, postRepository, userRepository,
                userService, blockRepository, coinService, notificationService);
        when(userService.isActiveUser(AUTHOR)).thenReturn(true);
        when(userService.getSummaries(any())).thenReturn(
                Map.of(AUTHOR, new UserSummaryResponse(AUTHOR, "작가", null, List.of())));
    }

    @Nested
    class Purchase {

        @BeforeEach
        void buyer() {
            User buyer = User.builder().email("b@test.local").nickname("독자").build();
            when(userRepository.findByIdForUpdate(BUYER)).thenReturn(Optional.of(buyer));
        }

        @Test
        void paysOnceAndTellsAuthor() {
            when(seriesRepository.findById(SERIES)).thenReturn(Optional.of(series(9900)));
            when(purchaseRepository.insertIgnore(BUYER, SERIES, 9900)).thenReturn(1);

            service.purchase(BUYER, SERIES);

            verify(coinService).paySeries(BUYER, SERIES, 9900);
            verify(notificationService).notify(AUTHOR, BUYER, NotificationType.SERIES_PURCHASE,
                    NotificationService.TARGET_SERIES, SERIES);
        }

        @Test
        void alreadyBoughtDoesNotPayAgain() {
            when(seriesRepository.findById(SERIES)).thenReturn(Optional.of(series(9900)));
            when(purchaseRepository.insertIgnore(BUYER, SERIES, 9900)).thenReturn(0);

            service.purchase(BUYER, SERIES);

            verify(coinService, never()).paySeries(anyLong(), anyLong(), anyInt());
        }

        @Test
        void freeSeriesCannotBeBought() {
            when(seriesRepository.findById(SERIES)).thenReturn(Optional.of(series(0)));

            assertBadRequest(() -> service.purchase(BUYER, SERIES));
            verify(purchaseRepository, never()).insertIgnore(anyLong(), anyLong(), anyInt());
        }

        @Test
        void blockedAuthorsSeriesIsNotFound() {
            when(seriesRepository.findById(SERIES)).thenReturn(Optional.of(series(9900)));
            when(blockRepository.existsBetween(BUYER, AUTHOR)).thenReturn(true);

            assertThatThrownBy(() -> service.purchase(BUYER, SERIES))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.NOT_FOUND);
        }
    }

    @Test
    void ownSeriesCannotBeBought() {
        User author = User.builder().email("a@test.local").nickname("작가").build();
        when(userRepository.findByIdForUpdate(AUTHOR)).thenReturn(Optional.of(author));
        when(seriesRepository.findById(SERIES)).thenReturn(Optional.of(series(9900)));

        assertBadRequest(() -> service.purchase(AUTHOR, SERIES));
    }

    @Test
    void cannotMakeSeriesFreeWhilePaidEpisodesRemain() {
        when(seriesRepository.findById(SERIES)).thenReturn(Optional.of(series(9900)));
        when(postRepository.existsBySeriesIdAndPaidTrueAndStatusNot(SERIES, PostStatus.DELETED)).thenReturn(true);

        assertBadRequest(() -> service.update(AUTHOR, SERIES, new SeriesWriteRequest("제목", "", 0, "ongoing")));
    }

    @Test
    void updateChangesPriceAndStatus() {
        PostSeries series = series(9900);
        when(seriesRepository.findById(SERIES)).thenReturn(Optional.of(series));

        service.update(AUTHOR, SERIES, new SeriesWriteRequest(" 새 제목 ", null, 15000, "completed"));

        assertThat(series.getTitle()).isEqualTo("새 제목");
        assertThat(series.getPriceKrw()).isEqualTo(15000);
        assertThat(series.getStatus()).isEqualTo(SeriesStatus.COMPLETED);
    }

    @Test
    void othersCannotUpdate() {
        when(seriesRepository.findById(SERIES)).thenReturn(Optional.of(series(9900)));

        assertThatThrownBy(() -> service.update(BUYER, SERIES, new SeriesWriteRequest("제목", "", 1, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void cannotDeleteWithEpisodesOrBuyers() {
        PostSeries series = series(9900);
        when(seriesRepository.findById(SERIES)).thenReturn(Optional.of(series));

        when(postRepository.existsBySeriesIdAndStatusNot(SERIES, PostStatus.DELETED)).thenReturn(true);
        assertBadRequest(() -> service.delete(AUTHOR, SERIES));

        when(postRepository.existsBySeriesIdAndStatusNot(SERIES, PostStatus.DELETED)).thenReturn(false);
        when(purchaseRepository.existsBySeriesId(SERIES)).thenReturn(true);
        assertBadRequest(() -> service.delete(AUTHOR, SERIES));

        verify(seriesRepository, never()).delete(any());
    }

    @Test
    void detailTellsWhetherViewerBought() {
        when(seriesRepository.findById(SERIES)).thenReturn(Optional.of(series(9900)));
        when(postRepository.findBySeriesIdAndStatusOrderBySeriesOrderAscIdAsc(SERIES, PostStatus.ACTIVE)).thenReturn(List.of());

        SeriesDetailResponse guest = service.getSeries(SERIES, null);
        assertThat(guest.purchased()).isFalse();
        assertThat(guest.series().price()).isEqualTo(9900);

        when(purchaseRepository.existsByUserIdAndSeriesId(BUYER, SERIES)).thenReturn(true);
        assertThat(service.getSeries(SERIES, BUYER).purchased()).isTrue();
        assertThat(service.getSeries(SERIES, AUTHOR).mine()).isTrue();
    }

    private static PostSeries series(int price) {
        PostSeries series = PostSeries.builder().userId(AUTHOR).title("스프링 정복").description("").priceKrw(price).build();
        ReflectionTestUtils.setField(series, "id", SERIES);
        return series;
    }

    private static void assertBadRequest(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }
}
