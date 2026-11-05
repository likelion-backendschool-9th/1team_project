package com.dipsource.dto.minihome;

import com.dipsource.dto.user.UserSummaryResponse;
import java.time.LocalDateTime;

/** 일촌평·방명록 한 건. 비밀글이고 볼 수 없으면 content는 null. */
public record MiniHomeCommentResponse(
        Long id,
        UserSummaryResponse author,
        String content,
        LocalDateTime createdAt,
        boolean secret
) {
}
