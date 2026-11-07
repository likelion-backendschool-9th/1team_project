package com.dipsource.service.admin;

import com.dipsource.dto.admin.AdminReportResponse;
import java.util.List;

public interface AdminReportService {

    /** 관리자 신고 목록. type·status는 프론트 쿼리 값(feeds, pending 등). */
    List<AdminReportResponse> list(String type, String status);

    AdminReportResponse get(Long reportId);

    /** 처리 상태만 바꾼다. hide/sanction → RESOLVED, reject → REJECTED. */
    void handle(Long adminId, Long reportId, String decision, String memo);
}
