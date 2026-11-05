package com.dipsource.service.minihome;

import com.dipsource.dto.minihome.MiniHomeCommentResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 미니홈피 일촌평. */
public interface MiniHomeCommentService {

    /** 삭제되지 않은 일촌평. updated_at 최신순. 미니홈피가 없으면 빈 목록. */
    PageResponse<MiniHomeCommentResponse> list(Long ownerId, Pageable pageable);

    /** 없으면 만들고, 있으면 내용을 고친다. 지워진 글이면 다시 살린다. */
    MiniHomeCommentResponse write(Long writerId, Long ownerId, String content);

    /** 작성자 또는 미니홈피 주인이 소프트 삭제한다. */
    void delete(Long actorId, Long commentId);
}
