package com.dipsource.service.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dipsource.dto.ranking.RankingResponse;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoLikeRepository;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.repository.minihome.MiniHomeRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.ranking.impl.RankingServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RankingServiceImplTest {

    @Mock
    private DemoLikeRepository demoLikeRepository;
    @Mock
    private DemoRepository demoRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private MiniHomeRepository miniHomeRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private RankingServiceImpl service;

    @Test
    void 데모는_기간_좋아요가_많고_같으면_실행수가_많은_순이다() {
        when(demoLikeRepository.rankByRecentLikes(any())).thenReturn(List.of(
                row(9L, 1L, "낮음", 1, 100),
                row(3L, 2L, "높음", 5, 10),
                row(4L, 3L, "동점", 5, 0)));
        when(userRepository.findAllById(any())).thenReturn(List.of(
                user(1L, "하나", UserStatus.ACTIVE),
                user(2L, "둘", UserStatus.ACTIVE),
                user(3L, "셋", UserStatus.ACTIVE)));

        List<RankingResponse> result = service.rank("demo", "weekly");

        assertThat(result).extracting(RankingResponse::title).containsExactly("높음", "동점", "낮음");
        assertThat(result).extracting(RankingResponse::rank).containsExactly(1, 2, 3);
        assertThat(result).extracting(RankingResponse::score).containsExactly(5L, 5L, 1L);
        assertThat(result).allMatch(row -> row.change() == 0);
    }

    @Test
    void 점수와_동점값이_같으면_id가_작은_쪽이_앞이다() {
        when(demoLikeRepository.rankByRecentLikes(any())).thenReturn(List.of(
                row(8L, 1L, "나중", 3, 1),
                row(2L, 2L, "먼저", 3, 1)));
        when(userRepository.findAllById(any())).thenReturn(List.of(
                user(1L, "하나", UserStatus.ACTIVE),
                user(2L, "둘", UserStatus.ACTIVE)));

        assertThat(service.rank("demo", "daily")).extracting(RankingResponse::title)
                .containsExactly("먼저", "나중");
    }

    @Test
    void 정지된_회원은_순위에서_빠진다() {
        when(demoLikeRepository.rankByRecentLikes(any())).thenReturn(List.of(
                row(1L, 5L, "정지", 9, 0),
                row(2L, 6L, "활동", 2, 0)));
        when(userRepository.findAllById(any())).thenReturn(List.of(
                user(5L, "정지회원", UserStatus.SUSPENDED),
                user(6L, "활동회원", UserStatus.ACTIVE)));

        List<RankingResponse> result = service.rank("demo", "monthly");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).user().nickname()).isEqualTo("활동회원");
        assertThat(result.get(0).rank()).isEqualTo(1);
    }

    @Test
    void 점수가_0이거나_결과가_없으면_빈_목록이다() {
        when(postRepository.rankByViews()).thenReturn(List.<Object[]>of(row(1L, 1L, "없음", 0, 3)));
        assertThat(service.rank("blog", "weekly")).isEmpty();

        when(miniHomeRepository.rankByVisits()).thenReturn(List.of());
        assertThat(service.rank("home", "weekly")).isEmpty();
    }

    @Test
    void 팔로워수가_같으면_데모_좋아요_합이_많은_쪽이_앞이다() {
        when(followRepository.countNewFollowers(any())).thenReturn(List.of(
                new Object[]{10L, 4L},
                new Object[]{11L, 4L}));
        when(demoRepository.sumActiveLikesByUserIds(any())).thenReturn(List.of(
                new Object[]{10L, 1L},
                new Object[]{11L, 9L}));
        when(userRepository.findAllById(any())).thenReturn(List.of(
                user(10L, "적음", UserStatus.ACTIVE),
                user(11L, "많음", UserStatus.ACTIVE)));

        assertThat(service.rank("follower", "weekly")).extracting(row -> row.user().nickname())
                .containsExactly("많음", "적음");
    }

    @Test
    void 이번주는_최근_7일을_기준으로_조회한다() {
        when(demoLikeRepository.rankByRecentLikes(any())).thenReturn(List.of());
        ArgumentCaptor<LocalDateTime> since = ArgumentCaptor.forClass(LocalDateTime.class);
        service.rank("demo", "weekly");
        org.mockito.Mockito.verify(demoLikeRepository).rankByRecentLikes(since.capture());
        LocalDateTime now = LocalDateTime.now();
        assertThat(since.getValue()).isBetween(now.minusDays(8), now.minusDays(6));
    }

    @Test
    void 알_수_없는_종류는_거절한다() {
        assertThatThrownBy(() -> service.rank("feed", "weekly"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    private static Object[] row(long subjectId, long userId, String title, int score, int tie) {
        return new Object[]{subjectId, userId, title, score, tie};
    }

    private static User user(long id, String nickname, UserStatus status) {
        User user = User.builder().email(id + "@test.com").nickname(nickname).build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "status", status);
        return user;
    }
}
