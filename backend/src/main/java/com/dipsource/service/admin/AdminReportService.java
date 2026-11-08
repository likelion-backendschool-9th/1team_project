package com.dipsource.service.admin;

import com.dipsource.dto.admin.AdminReportResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AdminReportService {

    /** 관리자 신고 목록. type·status는 프론트 쿼리 값(feeds, pending 등). */
    PageResponse<AdminReportResponse> list(String type, String status, Pageable pageable);

    AdminReportResponse get(Long reportId);

    /** 처리 상태만 바꾼다. hide/sanction → RESOLVED, reject → REJECTED. 콘텐츠 삭제·회원 제재는 각 관리자 API에서 따로 한다. */
    void handle(Long adminId, Long reportId, String decision, String memo);
}
