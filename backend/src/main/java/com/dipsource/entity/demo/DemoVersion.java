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

    @Builder
    private DemoVersion(Long demoId, int number, String entryPath, int fileCount, long totalBytes) {
        this.demoId = demoId;
        this.number = number;
        this.entryPath = entryPath;
        this.fileCount = fileCount;
        this.totalBytes = totalBytes;
    }

    /** id가 정해진 뒤에 경로를 붙인다. */
    public void assignStoragePrefix(String storagePrefix) {
        this.storagePrefix = storagePrefix;
    }

    public void markReady() {
        this.status = DemoVersionStatus.READY;
    }

    public void markFailed() {
        this.status = DemoVersionStatus.FAILED;
    }

    public void markDeleted() {
        this.status = DemoVersionStatus.DELETED;
    }
}
