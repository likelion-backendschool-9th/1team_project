package com.dipsource.service.minihome.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dipsource.dto.minihome.UpdateMiniHomeRequest;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.minihome.MiniHome;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.minihome.MiniHomeRepository;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MiniHomeServiceImplTest {

    private MiniHomeRepository repository;
    private UserService userService;
    private MiniHomeServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(MiniHomeRepository.class);
        userService = mock(UserService.class);
        service = new MiniHomeServiceImpl(new MiniHomeLoader(repository, userService), repository);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.isActiveUser(1L)).thenReturn(true);
        when(userService.getSummaries(List.of(1L))).thenReturn(Map.of(
                1L, new UserSummaryResponse(1L, "닉네임", null, List.of("React"))
        ));
    }

    @Test
    void createsHomeWithNicknameAndCreamSkin() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());

        var response = service.getOrCreate(1L);

        assertThat(response.headline()).isEqualTo("닉네임");
        assertThat(response.skin()).isEqualTo("cream");
        assertThat(response.showcaseDemoIds()).isEmpty();
        assertThat(response.featuredDemoId()).isNull();
        assertThat(response.stackTags()).isEmpty();
        assertThat(response.featuredPostId()).isNull();
    }

    @Test
    void readsStoredDefaultSkinAsCream() {
        MiniHome home = home();
        home.changeSkin("default", null);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        assertThat(service.getOrCreate(1L).skin()).isEqualTo("cream");
    }

    @Test
    void movesFeaturedDemoToFirstSlotAndKeepsTheRest() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        var response = service.update(1L, request(List.of(10L, 20L, 30L), "20"));

        assertThat(response.showcaseDemoIds()).containsExactly(20L, 10L, 30L);
        assertThat(response.featuredDemoId()).isEqualTo(20L);
        assertThat(response.headline()).isEqualTo("한 줄");
        assertThat(response.quote()).isEqualTo("한마디");
        assertThat(response.stackTags()).containsExactly("React", "TypeScript");
        assertThat(response.featuredPostId()).isNull();
    }

    @Test
    void keepsOrderWhenFeaturedIsEmpty() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        var response = service.update(1L, request(List.of(10L, 30L), ""));

        assertThat(response.showcaseDemoIds()).containsExactly(10L, 30L);
        assertThat(response.featuredDemoId()).isEqualTo(10L);
    }

    @Test
    void savesWithoutAnyDemo() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        var response = service.update(1L, request(List.of(), ""));

        assertThat(response.showcaseDemoIds()).isEmpty();
        assertThat(response.featuredDemoId()).isNull();
        assertThat(response.headline()).isEqualTo("한 줄");
    }

    @Test
    void rejectsMoreThanThreeDemos() {
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home()));

        assertThatThrownBy(() -> service.update(1L, request(List.of(1L, 2L, 3L, 4L), "")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void rejectsFeaturedOutsideShowcase() {
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home()));

        assertThatThrownBy(() -> service.update(1L, request(List.of(10L, 20L), "99")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void movesExistingShowcaseDemoToFirstSlot() {
        MiniHome home = home();
        home.changeShowcase(10L, 20L, 30L);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        var response = service.updateFeaturedDemo(1L, 30L);

        assertThat(response.showcaseDemoIds()).containsExactly(30L, 10L, 20L);
        assertThat(response.featuredDemoId()).isEqualTo(30L);
    }

    @Test
    void rejectsFeaturedDemoThatIsNotShown() {
        MiniHome home = home();
        home.changeShowcase(10L, 20L, null);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        assertThatThrownBy(() -> service.updateFeaturedDemo(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void storesTrimmedStackTags() {
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home()));

        var response = service.update(1L, request(List.of(10L), "", " React, , TypeScript "));

        assertThat(response.stackTags()).containsExactly("React", "TypeScript");
    }

    @Test
    void rejectsTooManyStackTags() {
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home()));

        assertThatThrownBy(() -> service.update(1L, request(List.of(10L), "", "a,b,c,d,e,f")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void missingUserIsNotFound() {
        when(userService.isActiveUser(9L)).thenReturn(false);

        assertThatThrownBy(() -> service.getOrCreate(9L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    private static MiniHome home() {
        return MiniHome.builder().userId(1L).title("옛 제목").intro("옛 소개").build();
    }

    private static UpdateMiniHomeRequest request(List<Long> showcase, String featured) {
        return request(showcase, featured, "React, TypeScript");
    }

    private static UpdateMiniHomeRequest request(List<Long> showcase, String featured, String stackTags) {
        return new UpdateMiniHomeRequest(
                "한 줄", "한마디", "mint", "example.dev", "track", stackTags, showcase, featured);
    }
}
