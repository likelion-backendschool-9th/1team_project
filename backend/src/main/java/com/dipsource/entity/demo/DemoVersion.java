package com.dipsource.entity.demo;

import com.dipsource.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 데모의 정적 파일 묶음 하나. 올릴 때마다 새로 생기고, 파일은 버전별 경로에 그대로 보관한다. 테이블: demo_versions */
@Getter
@Entity
@Table(name = "demo_versions", indexes = {
        @Index(name = "idx_demo_versions_demo", columnList = "demo_id, id"),
        @Index(name = "idx_demo_versions_status_updated", columnList = "status, updated_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoVersion extends BaseEntity {

    /** 버전 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 대상 데모(demos.id) */
    @Column(name = "demo_id", nullable = false)
    private Long demoId;

    /** 데모 안에서의 순번 (v1, v2 …) */
    @Column(nullable = false)
    private int number;

    /** R2 저장 경로 접두사. demos/{demoId}/versions/{id} */
    @Column(name = "storage_prefix", nullable = false, length = 200)
    private String storagePrefix = "";

    /** 진입 파일 경로 */
    @Column(name = "entry_path", nullable = false, length = 255)
    private String entryPath;

    /** 파일 수 */
    @Column(name = "file_count", nullable = false)
    private int fileCount;

    /** 압축 해제 후 총 크기(바이트) */
    @Column(name = "total_bytes", nullable = false)
    private long totalBytes;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DemoVersionStatus status = DemoVersionStatus.UPLOADING;

    /** 사용자 용량에 잡힌 크기(바이트) */
    @Column(name = "charged_bytes", nullable = false, columnDefinition = "bigint default 0 not null")
    private long chargedBytes;

    /** 자동 썸네일 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "thumbnail_status", nullable = false, length = 20)
    private DemoThumbnailStatus thumbnailStatus = DemoThumbnailStatus.SKIPPED;

    /** 자동 썸네일 주소 */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /** 촬영 시도 횟수 */
    @Column(name = "thumbnail_attempts", nullable = false, columnDefinition = "integer default 0 not null")
    private int thumbnailAttempts;

    /** 다음 촬영 시각 */
    @Column(name = "thumbnail_next_at")
    private LocalDateTime thumbnailNextAt;

    /** 마지막 실패 원인 */
    @Column(name = "thumbnail_error", length = 300)
    private String thumbnailError;

    /** 정적 파일의 용량을 이 버전이 부담하는지. Mock 설정만 바꾼 버전은 파일을 공유하므로 false */
    @Column(name = "owns_files", nullable = false, columnDefinition = "boolean default true not null")
    private boolean ownsFiles = true;

    /** Mock API 설정 원문(JSON). 없으면 null */
    @Column(name = "mock_config", columnDefinition = "text")
    private String mockConfig;

    /** 샘플 데이터 원문(JSON). 없으면 null */
    @Column(name = "sample_data", columnDefinition = "text")
    private String sampleData;

    /** Worker가 읽는 컴파일된 Mock의 R2 키. 없으면 null */
    @Column(name = "mock_key", length = 200)
    private String mockKey;

    /** 코드 편집기로 만든 버전의 원본 코드(JSON). ZIP으로 올렸으면 null */
    @Column(name = "editor_source", columnDefinition = "text")
    private String editorSource;

    @Builder
    private DemoVersion(Long demoId, int number, String entryPath, int fileCount, long totalBytes, long chargedBytes,
                        Boolean ownsFiles, String mockConfig, String sampleData, String editorSource) {
        this.demoId = demoId;
        this.number = number;
        this.entryPath = entryPath;
        this.fileCount = fileCount;
        this.totalBytes = totalBytes;
        this.chargedBytes = chargedBytes;
        this.ownsFiles = ownsFiles == null || ownsFiles;
        this.mockConfig = mockConfig;
        this.sampleData = sampleData;
        this.editorSource = editorSource;
    }

    /** id가 정해진 뒤에 경로를 붙인다. */
    public void assignStoragePrefix(String storagePrefix) {
        this.storagePrefix = storagePrefix;
    }

    public void assignMockKey(String mockKey) {
        this.mockKey = mockKey;
    }

    /** 파일을 함께 쓰던 버전이 지워질 때 파일 용량을 넘겨받는다. */
    public void adoptFiles() {
        this.ownsFiles = true;
        this.chargedBytes += this.totalBytes;
    }

    public void markReady() {
        this.status = DemoVersionStatus.READY;
    }

    public void markFailed() {
        this.status = DemoVersionStatus.FAILED;
    }

    /** 작성자가 지운 버전. 파일은 정리 작업이 지운다. */
    public void markDeleting() {
        this.status = DemoVersionStatus.DELETING;
    }

    /** 썸네일 촬영을 예약한다. */
    public void scheduleThumbnail() {
        this.thumbnailStatus = DemoThumbnailStatus.PENDING;
        this.thumbnailNextAt = LocalDateTime.now();
    }

    public void completeThumbnail(String url) {
        this.thumbnailStatus = DemoThumbnailStatus.READY;
        this.thumbnailUrl = url;
        this.thumbnailError = null;
        this.thumbnailNextAt = null;
    }

    /** 실패를 기록한다. 횟수가 남으면 뒤로 미뤄 다시 시도하고, 다 쓰면 기본 썸네일로 둔다. */
    public void failThumbnail(String error, int maxAttempts, long baseDelayMinutes) {
        this.thumbnailAttempts++;
        this.thumbnailError = error == null || error.length() <= 300 ? error : error.substring(0, 300);
        if (this.thumbnailAttempts >= maxAttempts) {
            this.thumbnailStatus = DemoThumbnailStatus.FAILED;
            this.thumbnailNextAt = null;
            return;
        }
        this.thumbnailNextAt = LocalDateTime.now().plusMinutes(baseDelayMinutes << (this.thumbnailAttempts - 1));
    }

    public void skipThumbnail() {
        this.thumbnailStatus = DemoThumbnailStatus.SKIPPED;
        this.thumbnailNextAt = null;
    }

    public void markDeleted() {
        this.status = DemoVersionStatus.DELETED;
    }
}
