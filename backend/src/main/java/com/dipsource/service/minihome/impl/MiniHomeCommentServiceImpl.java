package com.dipsource.service.minihome.impl;

import com.dipsource.dto.minihome.MiniHomeCommentResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.minihome.MiniHome;
import com.dipsource.entity.minihome.MiniHomeComment;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.minihome.MiniHomeCommentRepository;
import com.dipsource.service.follow.FollowService;
import com.dipsource.service.minihome.MiniHomeCommentService;
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

/** {@link MiniHomeCommentService} 구현체. */
@Service
@RequiredArgsConstructor
public class MiniHomeCommentServiceImpl implements MiniHomeCommentService {

    private final MiniHomeLoader miniHomeLoader;
    private final MiniHomeCommentRepository commentRepository;
    private final UserService userService;
    private final FollowService followService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MiniHomeCommentResponse> list(Long ownerId, Pageable pageable) {
        Optional<MiniHome> home = miniHomeLoader.findByUserId(ownerId);
        if (home.isEmpty()) {
            return empty(pageable);
        }
        Page<MiniHomeComment> page = commentRepository.findByHome_IdAndDeletedAtIsNull(
                home.get().getId(), newest(pageable, "updatedAt"));
        Map<Long, UserSummaryResponse> authors = authors(page.getContent().stream()
                .map(MiniHomeComment::getWriterId)
                .distinct()
                .toList());
        List<MiniHomeCommentResponse> content = page.getContent().stream()
                .map(comment -> toResponse(comment, false, comment.getContent(), authors))
                .toList();
        return PageResponse.of(page, content);
    }

    @Override
    @Transactional
    public MiniHomeCommentResponse write(Long writerId, Long ownerId, String content) {
        String text = requireText(content, 100, "일촌평을 입력해 주세요.", "일촌평은 100자 이하로 입력해 주세요.");
        MiniHome home = miniHomeLoader.loadOrCreate(ownerId);
        if (writerId.equals(home.getUserId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "내 미니홈피에는 일촌평을 쓸 수 없어요.");
        }
        if (!followService.isMutualFollow(writerId, ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "맞팔한 사람만 일촌평을 쓸 수 있어요");
        }
        MiniHomeComment comment = commentRepository.findByHome_IdAndWriterId(home.getId(), writerId)
                .orElseGet(() -> MiniHomeComment.builder()
                        .home(home)
                        .writerId(writerId)
                        .content(text)
                        .build());
        comment.rewrite(text);
        MiniHomeComment saved = commentRepository.save(comment);
        return toResponse(saved, false, saved.getContent(), userService.getSummaries(List.of(writerId)));
    }

    @Override
    @Transactional
    public void delete(Long actorId, Long commentId) {
        MiniHomeComment comment = commentRepository.findById(commentId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "일촌평을 찾을 수 없습니다"));
        if (!actorId.equals(comment.getWriterId()) && !actorId.equals(comment.getHome().getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "일촌평을 삭제할 권한이 없어요.");
        }
        comment.delete();
    }

    private Map<Long, UserSummaryResponse> authors(List<Long> writerIds) {
        if (writerIds.isEmpty()) {
            return Map.of();
        }
        return userService.getSummaries(writerIds);
    }

    private static MiniHomeCommentResponse toResponse(
            MiniHomeComment comment,
            boolean secret,
            String content,
            Map<Long, UserSummaryResponse> authors
    ) {
        UserSummaryResponse author = authors.get(comment.getWriterId());
        if (author == null) {
            author = new UserSummaryResponse(comment.getWriterId(), "삭제된 유저", null, List.of());
        }
        return new MiniHomeCommentResponse(comment.getId(), author, content, comment.getUpdatedAt(), secret);
    }

    private static String requireText(String raw, int max, String emptyMessage, String lengthMessage) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, emptyMessage);
        }
        String text = raw.trim();
        if (text.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, emptyMessage);
        }
        if (text.length() > max) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, lengthMessage);
        }
        return text;
    }

    private static PageResponse<MiniHomeCommentResponse> empty(Pageable pageable) {
        return new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0);
    }

    private static Pageable newest(Pageable pageable, String property) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, property));
    }
}
