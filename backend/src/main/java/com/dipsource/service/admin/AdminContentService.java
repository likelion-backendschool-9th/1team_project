package com.dipsource.service.admin;

import com.dipsource.dto.admin.AdminContentResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AdminContentService {

    /** 관리자 피드 목록. q는 본문·작성자 닉네임. */
    PageResponse<AdminContentResponse> listFeeds(String q, Pageable pageable);

    /** 관리자 피드 삭제. 소프트 삭제 후 작업 기록을 남긴다. */
    void deleteFeed(Long adminId, Long feedId);
}