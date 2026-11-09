package com.dipsource.controller.demo;

import com.dipsource.dto.demo.DemoBackendRequest;
import com.dipsource.dto.demo.DemoBackendResponse;
import com.dipsource.dto.demo.DemoCreateRequest;
import com.dipsource.dto.demo.DemoCursorResponse;
import com.dipsource.dto.demo.DemoEditorSaveRequest;
import com.dipsource.dto.demo.DemoPreviewResponse;
import com.dipsource.dto.demo.DemoPublishRequest;
import com.dipsource.dto.demo.DemoResponse;
import com.dipsource.dto.demo.DemoUsageResponse;
import com.dipsource.dto.demo.DemoVersionConfigRequest;
import com.dipsource.dto.demo.DemoVersionConfigResponse;
import com.dipsource.dto.demo.DemoVersionResponse;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.demo.DemoPreviewService;
import com.dipsource.service.demo.DemoQuotaService;
import com.dipsource.service.demo.DemoService;
import com.dipsource.service.demo.DemoVersionService;
import com.dipsource.service.demo.backend.DemoBackendHealthService;
import com.dipsource.service.demo.backend.DemoBackendService;
import com.dipsource.service.demo.thumbnail.DemoThumbnailService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 데모 컨트롤러
 *
 * GET    /demos                       — 목록 (커서, 비로그인 가능)
 * POST   /demos                       — 초안 등록
 * GET    /demos/{demoId}              — 상세 (비로그인 가능)
 * PUT    /demos/{demoId}              — 수정 (본인만)
 * DELETE /demos/{demoId}              — 삭제 (본인만)
 * POST   /demos/{demoId}/versions     — ZIP 업로드 (본인만)
 * POST   /demos/{demoId}/publish      — 공개 (본인만)
 * POST   /demos/{demoId}/unpublish    — 공개 해제 (본인만)
 * POST   /demos/{demoId}/play         — 실행 수 +1
 * POST·DELETE /demos/{demoId}/like     — 좋아요
 * POST·DELETE /demos/{demoId}/bookmark — 나중에 해보기
 * GET    /demos/usage                 — 내 사용량
 * POST   /demos/previews              — 임시 미리보기
 * GET    /demos/{demoId}/versions     — 버전 목록 (본인만)
 * DELETE /demos/{demoId}/versions/{versionId} — 버전 삭제 (본인만)
 * POST   /demos/{demoId}/versions/{versionId}/preview — 버전 미리보기 URL (본인만)
 * POST   /demos/{demoId}/versions/{versionId}/restore — 이전 버전 복구 (본인만)
 * POST   /demos/{demoId}/thumbnail    — 썸네일 직접 올리기 (본인만)
 * POST   /demos/{demoId}/versions/editor — 코드 편집 결과 저장 (본인만)
 * GET·POST /demos/{demoId}/versions/{versionId}/config — Mock·샘플 데이터 (본인만)
 * PUT·DELETE /demos/{demoId}/backend  — 외부 백엔드 연결·해제 (본인만)
 * POST   /demos/{demoId}/backend/health-check — 외부 백엔드 상태 확인 (로그인)
 */
@RestController
@RequestMapping("/demos")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;
    private final DemoVersionService versionService;
    private final DemoPreviewService previewService;
    private final DemoQuotaService quotaService;
    private final DemoThumbnailService thumbnailService;
    private final DemoBackendService backendService;
    private final DemoBackendHealthService backendHealthService;

    @GetMapping
    public ResponseEntity<ApiResponse<DemoCursorResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.of(
                demoService.list(viewerId(principal), userId, tag, sort, cursor, size)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DemoResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DemoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(demoService.create(requireUserId(principal), request)));
    }

    @GetMapping("/{demoId}")
    public ResponseEntity<ApiResponse<DemoResponse>> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId) {
        return ResponseEntity.ok(ApiResponse.of(demoService.get(viewerId(principal), demoId)));
    }

    @PutMapping("/{demoId}")
    public ResponseEntity<ApiResponse<DemoResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @Valid @RequestBody DemoCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.of(demoService.update(requireUserId(principal), demoId, request)));
    }

    @DeleteMapping("/{demoId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId) {
        demoService.delete(requireUserId(principal), demoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{demoId}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DemoVersionResponse>> uploadVersion(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(versionService.upload(requireUserId(principal), demoId, file)));
    }

    @PostMapping("/{demoId}/publish")
    public ResponseEntity<ApiResponse<DemoResponse>> publish(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @RequestBody(required = false) DemoPublishRequest request) {
        Long versionId = request == null ? null : request.versionId();
        Long expected = request == null ? null : request.expectedCurrentVersionId();
        return ResponseEntity.ok(ApiResponse.of(demoService.publish(requireUserId(principal), demoId, versionId, expected)));
    }

    /** 내 데모 사용량·남은 용량 */
    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<DemoUsageResponse>> usage(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(quotaService.getUsage(requireUserId(principal))));
    }

    /** 등록 전 임시 미리보기. 파일이 서버로 전송되고 만료되면 지워진다 */
    @PostMapping(value = "/previews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DemoPreviewResponse>> createPreview(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(previewService.create(requireUserId(principal), file)));
    }

    @GetMapping("/{demoId}/versions")
    public ResponseEntity<ApiResponse<List<DemoVersionResponse>>> versions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId) {
        return ResponseEntity.ok(ApiResponse.of(versionService.list(requireUserId(principal), demoId)));
    }

    @DeleteMapping("/{demoId}/versions/{versionId}")
    public ResponseEntity<Void> deleteVersion(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @PathVariable Long versionId) {
        versionService.delete(requireUserId(principal), demoId, versionId);
        return ResponseEntity.noContent().build();
    }

    /** 공개 전 버전을 작성자가 확인할 임시 URL */
    @PostMapping("/{demoId}/versions/{versionId}/preview")
    public ResponseEntity<ApiResponse<DemoPreviewResponse>> previewVersion(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.of(versionService.previewUrl(requireUserId(principal), demoId, versionId)));
    }

    /** 보관된 이전 버전을 다시 공개한다 */
    @PostMapping("/{demoId}/versions/{versionId}/restore")
    public ResponseEntity<ApiResponse<DemoResponse>> restore(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @PathVariable Long versionId,
            @RequestBody(required = false) DemoPublishRequest request) {
        Long expected = request == null ? null : request.expectedCurrentVersionId();
        return ResponseEntity.ok(ApiResponse.of(demoService.publish(requireUserId(principal), demoId, versionId, expected)));
    }

    /** 코드 편집기 내용을 새 버전으로 저장 */
    @PostMapping("/{demoId}/versions/editor")
    public ResponseEntity<ApiResponse<DemoVersionResponse>> saveEditor(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @Valid @RequestBody DemoEditorSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(versionService.saveEditor(requireUserId(principal), demoId, request)));
    }

    /** 버전의 Mock·샘플 데이터·편집기 코드 (본인만) */
    @GetMapping("/{demoId}/versions/{versionId}/config")
    public ResponseEntity<ApiResponse<DemoVersionConfigResponse>> getConfig(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.of(versionService.getConfig(requireUserId(principal), demoId, versionId)));
    }

    /** Mock·샘플 데이터를 바꾼 새 버전을 만든다 */
    @PostMapping("/{demoId}/versions/{versionId}/config")
    public ResponseEntity<ApiResponse<DemoVersionResponse>> saveConfig(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @PathVariable Long versionId,
            @Valid @RequestBody DemoVersionConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(versionService.saveConfig(requireUserId(principal), demoId, versionId, request)));
    }

    /** 외부 백엔드 주소 등록·수정 */
    @PutMapping("/{demoId}/backend")
    public ResponseEntity<ApiResponse<DemoResponse>> connectBackend(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @Valid @RequestBody DemoBackendRequest request) {
        return ResponseEntity.ok(ApiResponse.of(backendService.connect(requireUserId(principal), demoId, request)));
    }

    @DeleteMapping("/{demoId}/backend")
    public ResponseEntity<Void> disconnectBackend(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId) {
        backendService.disconnect(requireUserId(principal), demoId);
        return ResponseEntity.noContent().build();
    }

    /** 한도에 걸리거나 꺼져 있으면 외부 요청 없이 마지막 상태를 돌려준다 */
    @PostMapping("/{demoId}/backend/health-check")
    public ResponseEntity<ApiResponse<DemoBackendResponse>> checkBackendHealth(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId) {
        return ResponseEntity.ok(ApiResponse.of(backendHealthService.check(requireUserId(principal), demoId)));
    }

    /** 썸네일 직접 올리기 */
    @PostMapping(value = "/{demoId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadThumbnail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.of(thumbnailService.uploadManual(requireUserId(principal), demoId, file)));
    }

    @PostMapping("/{demoId}/unpublish")
    public ResponseEntity<ApiResponse<DemoResponse>> unpublish(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId) {
        return ResponseEntity.ok(ApiResponse.of(demoService.unpublish(requireUserId(principal), demoId)));
    }

    @PostMapping("/{demoId}/play")
    public ResponseEntity<Void> play(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId) {
        demoService.play(requireUserId(principal), demoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{demoId}/like")
    public ResponseEntity<Void> like(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId) {
        demoService.like(requireUserId(principal), demoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{demoId}/like")
    public ResponseEntity<Void> unlike(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId) {
        demoService.unlike(requireUserId(principal), demoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{demoId}/bookmark")
    public ResponseEntity<Void> bookmark(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId) {
        demoService.bookmark(requireUserId(principal), demoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{demoId}/bookmark")
    public ResponseEntity<Void> unbookmark(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long demoId) {
        demoService.unbookmark(requireUserId(principal), demoId);
        return ResponseEntity.noContent().build();
    }

    private static Long viewerId(UserPrincipal principal) {
        return principal == null ? null : principal.getUserId();
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
