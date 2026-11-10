package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoCreateRequest;
import com.dipsource.dto.demo.DemoCursorResponse;
import com.dipsource.dto.demo.DemoResponse;
import com.dipsource.dto.search.SearchTargetChangedEvent;
import com.dipsource.dto.search.SearchType;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoStatus;
import com.dipsource.entity.demo.DemoTag;
import com.dipsource.entity.tag.Tag;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.demo.DemoBookmarkRepository;
import com.dipsource.repository.demo.DemoLikeRepository;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.demo.DemoTagRepository;
import com.dipsource.repository.tag.TagRepository;
import com.dipsource.service.user.UserService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 데모 등록·조회·수정·삭제와 좋아요·북마크·실행 수. */
@Service
@RequiredArgsConstructor
public class DemoService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> SORTS = Set.of("trending", "latest", "popular");

    private final DemoRepository demoRepository;
    private final DemoTagRepository demoTagRepository;
    private final DemoLikeRepository likeRepository;
    private final DemoBookmarkRepository bookmarkRepository;
    private final TagRepository tagRepository;
    private final BlockRepository blockRepository;
    private final UserService userService;
    private final DemoAccess demoAccess;
    private final DemoResponseAssembler assembler;
    private final DemoPublicationService publicationService;
    private final DemoQuotaService quotaService;
    private final ApplicationEventPublisher eventPublisher;

    /** 초안으로 만든다. 파일을 올리고 공개해야 다른 사람에게 보인다. */
    @Transactional
    public DemoResponse create(Long userId, DemoCreateRequest request) {
        if (!userService.isActiveUser(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "데모를 올릴 수 없는 계정이에요.");
        }
        quotaService.reserveDemoSlot(userId);
        Demo demo = demoRepository.save(Demo.builder()
                .userId(userId)
                .title(request.title().strip())
                .description(request.description())
                .status(DemoStatus.DRAFT)
                .build());
        attachTags(demo, request.tags());
        return assembler.toResponse(demo, userId);
    }

    /**
     * 커서 목록.
     *
     * @param cursor 이전 응답의 nextCursor ("정렬값:id")
     */
    @Transactional(readOnly = true)
    public DemoCursorResponse list(Long viewerId, Long userId, String tag, String sort, String cursor, int size) {
        String safeSort = sort != null && SORTS.contains(sort) ? sort : "trending";
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Long[] position = parseCursor(cursor);
        Long[] hidden = viewerId == null
                ? new Long[0] : blockRepository.findHiddenUserIds(viewerId).toArray(Long[]::new);

        List<Demo> fetched = demoRepository.findPage(viewerId, userId, hidden,
                tag == null || tag.isBlank() ? null : tag.strip().toLowerCase(Locale.ROOT),
                safeSort, position[0], position[1], safeSize + 1);
        boolean hasNext = fetched.size() > safeSize;
        List<Demo> page = hasNext ? fetched.subList(0, safeSize) : fetched;

        String nextCursor = null;
        if (hasNext) {
            Demo last = page.get(page.size() - 1);
            nextCursor = sortValue(last, safeSort) + ":" + last.getId();
        }
        return new DemoCursorResponse(assembler.toResponses(page, viewerId), nextCursor, hasNext, page.size());
    }

    @Transactional(readOnly = true)
    public DemoResponse get(Long viewerId, Long demoId) {
        return assembler.toResponse(demoAccess.findVisible(demoId, viewerId), viewerId);
    }

    @Transactional
    public DemoResponse update(Long userId, Long demoId, DemoCreateRequest request) {
        Demo demo = demoAccess.findOwned(userId, demoId);
        demo.update(request.title().strip(), request.description(), demo.getThumbnail());
        demoTagRepository.deleteByDemoId(demoId);
        attachTags(demoRepository.getReferenceById(demoId), request.tags());
        // 태그 삭제는 벌크 쿼리라 검색 색인 동기화에 잡히지 않는다
        eventPublisher.publishEvent(new SearchTargetChangedEvent(SearchType.DEMO, demoId));
        return assembler.toResponse(demoRepository.findById(demoId).orElseThrow(), userId);
    }

    @Transactional
    public void delete(Long userId, Long demoId) {
        publicationService.revokeAndDelete(demoAccess.findOwnedForUpdate(userId, demoId));
    }

    @Transactional
    public DemoResponse publish(Long userId, Long demoId, Long versionId, Long expectedCurrentId) {
        return assembler.toResponse(publicationService.publish(userId, demoId, versionId, expectedCurrentId), userId);
    }

    @Transactional
    public DemoResponse unpublish(Long userId, Long demoId) {
        return assembler.toResponse(publicationService.unpublish(userId, demoId), userId);
    }

    @Transactional
    public void play(Long viewerId, Long demoId) {
        demoAccess.findActive(demoId, viewerId);
        demoRepository.incrementPlayCount(demoId);
    }

    @Transactional
    public void like(Long userId, Long demoId) {
        Demo demo = demoAccess.findActive(demoId, userId);
        if (demo.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "본인 데모에는 좋아요할 수 없어요.");
        }
        if (likeRepository.insertIgnore(userId, demoId) > 0) {
            demoRepository.incrementLikeCount(demoId);
        }
    }

    @Transactional
    public void unlike(Long userId, Long demoId) {
        demoAccess.findActive(demoId, userId);
        if (likeRepository.deleteByUserIdAndDemoId(userId, demoId) > 0) {
            demoRepository.decrementLikeCount(demoId);
        }
    }

    @Transactional
    public void bookmark(Long userId, Long demoId) {
        demoAccess.findActive(demoId, userId);
        bookmarkRepository.insertIgnore(userId, demoId);
    }

    @Transactional
    public void unbookmark(Long userId, Long demoId) {
        demoAccess.findActive(demoId, userId);
        bookmarkRepository.deleteByUserIdAndDemoId(userId, demoId);
    }

    private void attachTags(Demo demo, List<String> names) {
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
            demoTagRepository.save(new DemoTag(demo, tag.getId()));
        }
    }

    private long sortValue(Demo demo, String sort) {
        return switch (sort) {
            case "latest" -> demo.getId();
            case "popular" -> demo.getLikeCount();
            default -> demo.getPlayCount();
        };
    }

    /** @return [정렬값, id]. 커서가 없으면 둘 다 null */
    private Long[] parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return new Long[] {null, null};
        }
        String[] parts = cursor.split(":", -1);
        try {
            if (parts.length != 2) {
                throw new NumberFormatException();
            }
            return new Long[] {Long.valueOf(parts[0]), Long.valueOf(parts[1])};
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "잘못된 커서입니다");
        }
    }
}
