package com.dipsource.service.minihome.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.minihome.UpdateMiniHomeRequest;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.minihome.MiniHome;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.minihome.MiniHomeRepository;
import com.dipsource.service.post.PostService;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MiniHomeServiceImplTest {

    private MiniHomeRepository repository;
    private UserService userService;
    private PostService postService;
    private MiniHomeServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(MiniHomeRepository.class);
        userService = mock(UserService.class);
        postService = mock(PostService.class);
        service = new MiniHomeServiceImpl(
                new MiniHomeLoader(repository, userService), repository, postService, mock(BlockRepository.class));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.isActiveUser(1L)).thenReturn(true);
        when(userService.getSummaries(List.of(1L))).thenReturn(Map.of(
                1L, new UserSummaryResponse(1L, "닉네임", null, List.of("React"))
        ));
    }

    @Test
    void showsDefaultHomeWithoutSavingIt() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());

        var response = service.get(1L, null);

        verify(repository, never()).save(any());

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

        assertThat(service.get(1L, null).skin()).isEqualTo("cream");
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
    void setsAndClearsFeaturedPost() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));
        when(postService.isActivePostOf(302L, 1L)).thenReturn(true);

        assertThat(service.updateFeaturedPost(1L, 302L).featuredPostId()).isEqualTo(302L);
        assertThat(service.updateFeaturedPost(1L, null).featuredPostId()).isNull();
    }

    @Test
    void rejectsSomeoneElsesPostAsFeatured() {
        when(postService.isActivePostOf(999L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.updateFeaturedPost(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void clearsFeaturedPostWhenPostIsGone() {
        MiniHome home = home();
        home.changeFeaturedPost(302L);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));
        when(postService.isActivePostOf(302L, 1L)).thenReturn(false);

        assertThat(service.get(1L, null).featuredPostId()).isNull();
    }

    @Test
    void rejectsInvalidFeaturedPostId() {
        assertThatThrownBy(() -> service.updateFeaturedPost(1L, 0L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void savesKnownSkin() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        assertThat(service.update(1L, requestWithSkin("forest")).skin()).isEqualTo("forest");
        assertThat(home.getSkinTheme()).isEqualTo("forest");
    }

    @Test
    void savesDefaultOrBlankSkinAsCream() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        service.update(1L, requestWithSkin("default"));
        assertThat(home.getSkinTheme()).isEqualTo("cream");

        service.update(1L, requestWithSkin(""));
        assertThat(home.getSkinTheme()).isEqualTo("cream");
    }

    @Test
    void readsStoredUnknownSkinAsCream() {
        MiniHome home = home();
        home.changeSkin("abc", null);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        assertThat(service.get(1L, null).skin()).isEqualTo("cream");
    }

    @Test
    void rejectsUnknownSkin() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));
        assertThatThrownBy(() -> service.update(1L, requestWithSkin("abc")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void savesDomainWithoutSchemeInLowerCase() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        service.update(1L, requestWithDomain(" https://Blog.My-Site.co.kr/ "));
        assertThat(home.getDomain()).isEqualTo("blog.my-site.co.kr");

        service.update(1L, requestWithDomain(""));
        assertThat(home.getDomain()).isNull();
    }

    @Test
    void rejectsMalformedDomain() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        for (String domain : List.of("abc", "-bad.com", "ex ample.dev", "javascript:alert(1)")) {
            assertThatThrownBy(() -> service.update(1L, requestWithDomain(domain)))
                    .as(domain)
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.BAD_REQUEST);
        }
    }

    @Test
    void savesKnownBgm() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        assertThat(service.update(1L, requestWithBgm(" rainy-commit ")).bgm()).isEqualTo("rainy-commit");
        assertThat(home.getBgm()).isEqualTo("rainy-commit");
    }

    @Test
    void savesBlankBgmAsNone() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        service.update(1L, requestWithBgm(""));
        assertThat(home.getBgm()).isNull();

        service.update(1L, requestWithBgm(null));
        assertThat(home.getBgm()).isNull();
    }

    @Test
    void rejectsUnknownBgm() {
        MiniHome home = home();
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        assertThatThrownBy(() -> service.update(1L, requestWithBgm("track")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void readsStoredUnknownBgmAsNone() {
        MiniHome home = home();
        home.update("한 줄", "한마디", "example.dev", "old-track");
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home));

        assertThat(service.get(1L, null).bgm()).isNull();
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
    void acceptsTenStackTags() {
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home()));

        var response = service.update(1L, request(List.of(10L), "", "a,b,c,d,e,f,g,h,i,j"));

        assertThat(response.stackTags()).hasSize(10);
    }

    @Test
    void rejectsTooManyStackTags() {
        when(repository.findByUserId(1L)).thenReturn(Optional.of(home()));

        assertThatThrownBy(() -> service.update(1L, request(List.of(10L), "", "a,b,c,d,e,f,g,h,i,j,k")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void ownVisitDoesNotCreateHome() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());

        var stats = service.recordVisit(1L, 1L);

        assertThat(stats.today()).isZero();
        verify(repository, never()).save(any());
    }

    @Test
    void otherVisitCreatesHomeAndCounts() {
        MiniHome saved = home();
        when(userService.isActiveUser(2L)).thenReturn(true);
        when(userService.getSummaries(List.of(2L))).thenReturn(Map.of(
                2L, new UserSummaryResponse(2L, "주인", null, List.of())
        ));
        when(repository.findByUserId(2L)).thenReturn(Optional.empty(), Optional.of(saved));

        service.recordVisit(1L, 2L);

        verify(repository).save(any());
        verify(repository).incrementVisitCounts(2L);
    }

    @Test
    void missingUserIsNotFound() {
        when(userService.isActiveUser(9L)).thenReturn(false);

        assertThatThrownBy(() -> service.get(9L, null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    private static MiniHome home() {
        return MiniHome.builder().userId(1L).title("옛 제목").intro("옛 소개").build();
    }

    private static UpdateMiniHomeRequest requestWithSkin(String skin) {
        return new UpdateMiniHomeRequest(
                "한 줄", "한마디", skin, "example.dev", "lofi-coding", "React", List.of(), "");
    }

    private static UpdateMiniHomeRequest requestWithDomain(String domain) {
        return new UpdateMiniHomeRequest(
                "한 줄", "한마디", "cream", domain, "lofi-coding", "React", List.of(), "");
    }

    private static UpdateMiniHomeRequest requestWithBgm(String bgm) {
        return new UpdateMiniHomeRequest(
                "한 줄", "한마디", "cream", "example.dev", bgm, "React", List.of(), "");
    }

    private static UpdateMiniHomeRequest request(List<Long> showcase, String featured) {
        return request(showcase, featured, "React, TypeScript");
    }

    private static UpdateMiniHomeRequest request(List<Long> showcase, String featured, String stackTags) {
        return new UpdateMiniHomeRequest(
                "한 줄", "한마디", "mint", "example.dev", "lofi-coding", stackTags, showcase, featured);
    }
}
