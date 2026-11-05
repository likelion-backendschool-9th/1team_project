package com.dipsource.service.minihome.impl;

import com.dipsource.dto.minihome.MiniHomeCommentResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.minihome.Guestbook;
import com.dipsource.entity.minihome.MiniHome;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.minihome.GuestbookRepository;
import com.dipsource.service.minihome.GuestbookService;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link GuestbookService} 구현체. */
@Service
@RequiredArgsConstructor
public class GuestbookServiceImpl implements GuestbookService {

    private final MiniHomeLoader miniHomeLoader;
    private final GuestbookRepository guestbookRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MiniHomeCommentResponse> list(Long viewerId, Long ownerId, Pageable pageable) {
        Optional<MiniHome> found = miniHomeLoader.findByUserId(ownerId);
        if (found.isEmpty()) {
            return empty(pageable);
        }
        MiniHome home = found.get();
        Page<Guestbook> page = guestbookRepository.findByHome_IdAndDeletedAtIsNull(
                home.getId(), newest(pageable));
        Map<Long, UserSummaryResponse> authors = authors(page.getContent().stream()
                .map(Guestbook::getWriterId)
                .distinct()
                .toList());
        List<MiniHomeCommentResponse> content = page.getContent().stream()
                .map(entry -> toResponse(entry, visible(viewerId, home, entry), authors))
                .toList();
        return PageResponse.of(page, content);
    }

    @Override
    @Transactional
    public MiniHomeCommentResponse write(Long writerId, Long ownerId, String content, Boolean secret) {
        String text = requireText(content);
        MiniHome home = miniHomeLoader.loadOrCreate(ownerId);
        boolean isSecret = Boolean.TRUE.equals(secret);
        Guestbook saved = guestbookRepository.save(Guestbook.builder()
                .home(home)
                .writerId(writerId)
                .content(text)
                .secret(isSecret)
                .build());
        return toResponse(saved, true, userService.getSummaries(List.of(writerId)));
    }

    @Override
    @Transactional
    public void delete(Long actorId, Long guestbookId) {
        Guestbook entry = guestbookRepository.findById(guestbookId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "방명록을 찾을 수 없습니다"));
        if (!actorId.equals(entry.getWriterId()) && !actorId.equals(entry.getHome().getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "방명록을 삭제할 권한이 없어요.");
        }
        entry.delete();
    }

    private Map<Long, UserSummaryResponse> authors(List<Long> writerIds) {
        if (writerIds.isEmpty()) {
            return Map.of();
        }
        return userService.getSummaries(writerIds);
    }

    /** 비밀글은 작성자와 미니홈피 주인만 내용을 본다. */
    private static boolean visible(Long viewerId, MiniHome home, Guestbook entry) {
        if (!entry.isSecret()) {
            return true;
        }
        return viewerId.equals(entry.getWriterId()) || viewerId.equals(home.getUserId());
    }

    private static MiniHomeCommentResponse toResponse(
            Guestbook entry,
            boolean showContent,
            Map<Long, UserSummaryResponse> authors
    ) {
        UserSummaryResponse author = authors.get(entry.getWriterId());
        if (author == null) {
            author = new UserSummaryResponse(entry.getWriterId(), "삭제된 유저", null, List.of());
        }
        String content = showContent ? entry.getContent() : null;
        return new MiniHomeCommentResponse(
                entry.getId(), author, content, entry.getCreatedAt(), entry.isSecret());
    }

    private static String requireText(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "방명록 내용을 입력해 주세요.");
        }
        String text = raw.trim();
        if (text.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "방명록 내용을 입력해 주세요.");
        }
        if (text.length() > 500) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "방명록은 500자 이하로 입력해 주세요.");
        }
        return text;
    }

    private static PageResponse<MiniHomeCommentResponse> empty(Pageable pageable) {
        return new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0);
    }

    private static Pageable newest(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
