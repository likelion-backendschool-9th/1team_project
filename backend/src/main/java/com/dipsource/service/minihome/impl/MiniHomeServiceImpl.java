package com.dipsource.service.minihome.impl;

import com.dipsource.dto.minihome.MiniHomeResponse;
import com.dipsource.dto.minihome.RandomMiniHomeResponse;
import com.dipsource.dto.minihome.UpdateMiniHomeRequest;
import com.dipsource.dto.minihome.VisitorStatsResponse;
import com.dipsource.entity.minihome.MiniHome;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.minihome.MiniHomeRepository;
import com.dipsource.service.minihome.MiniHomeService;
import com.dipsource.service.post.PostService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link MiniHomeService} 구현체. */
@Service
@RequiredArgsConstructor
public class MiniHomeServiceImpl implements MiniHomeService {

    private static final String DEFAULT_SKIN = "cream";
    /** 기술 스택 최대 개수. 데모·피드 태그와 같은 10개로 맞춘다. */
    private static final int MAX_STACK_TAGS = 10;
    /** 프론트 MiniHomeSkin과 같은 9종. 스킨을 추가하면 양쪽을 같이 바꾼다. */
    private static final Set<String> SKINS = Set.of(
            "cream", "midnight", "mint", "cherry", "sky", "lavender", "peach", "forest", "sunset");
    /** 프론트 BGM_TRACKS(_lib/bgm.ts)의 id와 같다. 곡을 추가하면 양쪽을 같이 바꾼다. */
    private static final Set<String> BGMS = Set.of(
            "lofi-coding", "rainy-commit", "deploy-friday");
    /** example.dev, blog.my-site.co.kr 같은 호스트 이름. 각 칸은 영문·숫자·하이픈 1~63자, 끝은 영문 2자 이상. */
    private static final Pattern DOMAIN = Pattern.compile(
            "^(?=.{4,100}$)([a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}$");

    private final MiniHomeLoader miniHomeLoader;
    private final MiniHomeRepository miniHomeRepository;
    private final PostService postService;
    private final BlockRepository blockRepository;

    @Override
    @Transactional
    public MiniHomeResponse get(Long userId, Long viewerId) {
        if (viewerId != null && !viewerId.equals(userId) && blockRepository.existsBetween(viewerId, userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "미니홈피를 찾을 수 없습니다");
        }
        // 구경만 해서는 행을 만들지 않는다. 처음 저장(PUT)할 때 만들어진다.
        MiniHome home = miniHomeLoader.loadOrDraft(userId);
        // 대표 글이 삭제·숨김됐으면 연결을 끊어서 깨진 링크를 내려주지 않는다.
        Long postId = home.getFeaturedPostId();
        if (postId != null && !postService.isActivePostOf(postId, userId)) {
            home.changeFeaturedPost(null);
        }
        return toResponse(home);
    }

    @Override
    @Transactional
    public MiniHomeResponse update(Long userId, UpdateMiniHomeRequest request) {
        MiniHome home = miniHomeLoader.loadOrCreate(userId);
        String headline = requireHeadline(request.headline());
        String quote = optional(request.quote(), 80, "오늘의 한마디는 80자 이하로 입력해 주세요.");
        String domain = normalizeDomain(request.domain());
        String bgm = normalizeBgm(request.bgm());
        List<Long> ordered = arrange(request.showcaseDemoIds(), parseFeatured(request.featuredDemoId()));
        home.update(headline, quote, domain, bgm);
        home.changeStackTags(parseStackTags(request.stackTags()));
        home.changeSkin(normalizeSkin(request.skin()), home.getCustomSkinJson());
        applyShowcase(home, ordered);
        return toResponse(miniHomeRepository.save(home));
    }

    @Override
    @Transactional
    public MiniHomeResponse updateFeaturedDemo(Long userId, Long demoId) {
        if (demoId == null || demoId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "대표 데모를 골라 주세요.");
        }
        MiniHome home = miniHomeLoader.loadOrCreate(userId);
        List<Long> current = showcaseIds(home);
        if (!current.contains(demoId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "대표 데모는 공개할 데모 중에서 골라 주세요.");
        }
        applyShowcase(home, arrange(current, demoId));
        return toResponse(miniHomeRepository.save(home));
    }

    @Override
    @Transactional
    public MiniHomeResponse updateFeaturedPost(Long userId, Long postId) {
        if (postId != null && postId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "대표 글을 다시 골라 주세요.");
        }
        if (postId != null && !postService.isActivePostOf(postId, userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "내 블로그 글만 대표 글로 고를 수 있어요.");
        }
        MiniHome home = miniHomeLoader.loadOrCreate(userId);
        home.changeFeaturedPost(postId);
        return toResponse(miniHomeRepository.save(home));
    }

    @Override
    @Transactional(readOnly = true)
    public VisitorStatsResponse getVisitors(Long userId) {
        return miniHomeLoader.findByUserId(userId)
                .map(MiniHomeServiceImpl::stats)
                .orElse(new VisitorStatsResponse(0, 0));
    }

    @Override
    @Transactional
    public VisitorStatsResponse recordVisit(Long viewerId, Long ownerId) {
        // 내 홈 방문은 세지 않으니 홈을 만들 필요도 없다.
        if (viewerId.equals(ownerId)) {
            return stats(miniHomeLoader.loadOrDraft(ownerId));
        }
        miniHomeLoader.loadOrCreate(ownerId);
        miniHomeRepository.incrementVisitCounts(ownerId);
        return miniHomeRepository.findByUserId(ownerId)
                .map(MiniHomeServiceImpl::stats)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "미니홈피를 찾을 수 없습니다"));
    }

    @Override
    @Transactional(readOnly = true)
    public RandomMiniHomeResponse pickRandom(Long viewerId, Long excludeUserId) {
        Long excludeId = excludeUserId == null ? viewerId : excludeUserId;
        return miniHomeRepository.findRandomUserId(viewerId, excludeId)
                .map(RandomMiniHomeResponse::new)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "이동할 미니홈피가 없습니다"));
    }

    @Override
    @Transactional
    public void resetTodayCount() {
        miniHomeRepository.resetTodayCount();
    }

    @Override
    @Transactional
    public Long getOrCreateMiniHomeId(Long userId) {
        return miniHomeLoader.loadOrCreate(userId).getId();
    }

    private static String requireHeadline(String headline) {
        if (headline == null || headline.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "한 줄 소개를 입력해 주세요.");
        }
        String value = headline.trim();
        if (value.length() > 30) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "한 줄 소개는 30자 이하로 입력해 주세요.");
        }
        return value;
    }

    private static String optional(String value, int max, String message) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return trimmed;
    }

    /** 쉼표로 나누고, 앞뒤 공백과 빈 값은 버린 뒤 개수·길이를 검사한다. */
    private static List<String> parseStackTags(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<String> tags = new ArrayList<>();
        for (String part : raw.split(",")) {
            String tag = part.trim();
            if (tag.isEmpty()) {
                continue;
            }
            if (tag.length() > 20) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "기술 스택은 20자 이하로 입력해 주세요.");
            }
            tags.add(tag);
        }
        if (tags.size() > MAX_STACK_TAGS) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "기술 스택은 " + MAX_STACK_TAGS + "개까지만 적을 수 있어요.");
        }
        return List.copyOf(tags);
    }

    private static String normalizeSkin(String skin) {
        if (skin == null || skin.isBlank() || "default".equals(skin)) {
            return DEFAULT_SKIN;
        }
        String value = skin.trim();
        if (!SKINS.contains(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "목록에 있는 스킨만 고를 수 있어요.");
        }
        return value;
    }

    /** 비어 있으면 도메인 없음(null). https://나 끝의 /는 떼고 소문자로 저장한다. */
    private static String normalizeDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return null;
        }
        String value = domain.trim().toLowerCase(Locale.ROOT)
                .replaceFirst("^https?://", "")
                .replaceFirst("/+$", "");
        if (!DOMAIN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "도메인은 example.dev 같은 형식으로 입력해 주세요.");
        }
        return value;
    }

    /** 비어 있으면 BGM 없음(null). 목록에 없는 곡 id는 거절한다. */
    private static String normalizeBgm(String bgm) {
        if (bgm == null || bgm.isBlank()) {
            return null;
        }
        String value = bgm.trim();
        if (!BGMS.contains(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "목록에 있는 곡만 고를 수 있어요.");
        }
        return value;
    }

    private static Long parseFeatured(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            long id = Long.parseLong(raw.trim());
            if (id <= 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "대표 데모는 공개할 데모 중에서 골라 주세요.");
            }
            return id;
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "대표 데모는 공개할 데모 중에서 골라 주세요.");
        }
    }

    /** featured가 있으면 1번으로 옮기고 나머지 순서는 유지한다. */
    private static List<Long> arrange(List<Long> showcase, Long featured) {
        List<Long> source = showcase == null ? List.of() : showcase;
        if (source.size() > 3) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "공개할 데모는 3개까지 고를 수 있어요.");
        }
        List<Long> ids = new ArrayList<>();
        for (Long id : source) {
            if (id == null || id <= 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "공개할 데모를 다시 골라 주세요.");
            }
            ids.add(id);
        }
        if (featured != null && !ids.contains(featured)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "대표 데모는 공개할 데모 중에서 골라 주세요.");
        }
        List<Long> ordered = new ArrayList<>();
        if (featured != null) {
            ordered.add(featured);
        }
        for (Long id : ids) {
            if (featured != null && featured.equals(id)) {
                continue;
            }
            ordered.add(id);
        }
        return List.copyOf(ordered);
    }

    private static void applyShowcase(MiniHome home, List<Long> ordered) {
        home.changeShowcase(
                ordered.isEmpty() ? null : ordered.get(0),
                ordered.size() > 1 ? ordered.get(1) : null,
                ordered.size() > 2 ? ordered.get(2) : null
        );
    }

    private static MiniHomeResponse toResponse(MiniHome home) {
        return new MiniHomeResponse(
                home.getTitle(),
                home.getIntro(),
                displaySkin(home.getSkinTheme()),
                home.getDomain(),
                displayBgm(home.getBgm()),
                showcaseIds(home),
                home.getShowcaseDemo1Id(),
                home.getStackTags() == null ? List.of() : List.copyOf(home.getStackTags()),
                home.getFeaturedPostId()
        );
    }

    private static List<Long> showcaseIds(MiniHome home) {
        List<Long> ids = new ArrayList<>();
        if (home.getShowcaseDemo1Id() != null) {
            ids.add(home.getShowcaseDemo1Id());
        }
        if (home.getShowcaseDemo2Id() != null) {
            ids.add(home.getShowcaseDemo2Id());
        }
        if (home.getShowcaseDemo3Id() != null) {
            ids.add(home.getShowcaseDemo3Id());
        }
        return List.copyOf(ids);
    }

    private static VisitorStatsResponse stats(MiniHome home) {
        return new VisitorStatsResponse(home.getTodayCount(), home.getTotalCount());
    }

    /** 예전에 저장된 목록에 없는 곡 id는 BGM 없음으로 보여 준다. */
    private static String displayBgm(String bgm) {
        return bgm != null && BGMS.contains(bgm) ? bgm : null;
    }

    /** 예전에 저장된 "default"나 목록에 없는 값은 cream으로 보여 준다. */
    private static String displaySkin(String skin) {
        if (skin == null || !SKINS.contains(skin)) {
            return DEFAULT_SKIN;
        }
        return skin;
    }
}
