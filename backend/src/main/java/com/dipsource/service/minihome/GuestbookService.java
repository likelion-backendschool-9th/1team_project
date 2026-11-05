package com.dipsource.service.minihome;

import com.dipsource.dto.minihome.MiniHomeCommentResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 미니홈피 방명록. */
public interface GuestbookService {

    /** 삭제되지 않은 방명록. 작성 시각 최신순. 비밀글은 작성자·주인만 내용을 본다. 미니홈피가 없으면 빈 목록. */
    PageResponse<MiniHomeCommentResponse> list(Long viewerId, Long ownerId, Pageable pageable);

    /** 방명록을 추가한다. 주인 포함 누구나, 여러 번 쓸 수 있다. */
    MiniHomeCommentResponse write(Long writerId, Long ownerId, String content, Boolean secret);

    /** 작성자 또는 미니홈피 주인이 소프트 삭제한다. */
    void delete(Long actorId, Long guestbookId);
}
