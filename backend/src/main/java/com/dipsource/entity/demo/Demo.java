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

/**
 * 데모. 테이블: demos
 * 실행·좋아요 수는 동시성 때문에 엔티티에서 더하지 않고 리포지토리의 UPDATE 쿼리로 갱신한다.
 */
@Getter
@Entity
@Table(name = "demos", indexes = {
        @Index(name = "idx_demos_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_demos_created", columnList = "created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Demo extends BaseEntity {

    /** 데모 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작성자 ID(users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 제목 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 설명 */
    @Column(length = 2000)
    private String description;

    /** 실행 방식 */
    @Enumerated(EnumType.STRING)
    @Column(name = "demo_type", nullable = false, length = 20)
    private DemoType demoType = DemoType.STATIC;

    /** 업로드 파일 경로. STATIC일 때 사용 */
    @Column(name = "file_path", length = 500)
    private String filePath;

    /** 외부 백엔드 API Base URL. 실행 방식(demoType)과 무관하며 없으면 연결하지 않은 데모다 */
    @Column(name = "api_url", length = 500)
    private String apiUrl;

    /** OpenAPI 명세 JSON. API일 때 사용 */
    @Column(name = "openapi_json", length = 50000)
    private String openapiJson;

    /** 실행 시나리오 JSON. API일 때 사용 */
    @Column(name = "scenario_json", length = 50000)
    private String scenarioJson;

    /** 외부 실행 URL. EXTERNAL일 때 사용 */
    @Column(name = "external_url", length = 500)
    private String externalUrl;

    /** 썸네일 이미지 URL */
    @Column(length = 500)
    private String thumbnail;

    /** 실행(찍먹) 수 */
    @Column(name = "play_count", nullable = false)
    private int playCount = 0;

    /** 좋아요 수 */
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    /** 피드백(댓글) 수 */
    @Column(name = "feedback_count", nullable = false)
    private int feedbackCount = 0;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DemoStatus status = DemoStatus.ACTIVE;

    /** 데모 버전. 다시 올릴 때마다 1씩 오른다 */
    @Column(nullable = false)
    private int version = 1;

    /** 삭제 시각. 삭제하지 않았으면 null */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** 현재 공개 중인 버전(demo_versions.id). 공개한 적 없으면 null */
    @Column(name = "current_version_id")
    private Long currentVersionId;

    /** 작성자가 직접 올린 썸네일이면 true. 자동 썸네일로 덮어쓰지 않는다 */
    @Column(name = "thumbnail_manual", nullable = false, columnDefinition = "boolean default false not null")
    private boolean thumbnailManual = false;

    @Builder
    private Demo(Long userId, String title, String description, DemoType demoType, String filePath,
                 String apiUrl, String openapiJson, String scenarioJson, String externalUrl, String thumbnail,
                 DemoStatus status) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.demoType = demoType == null ? DemoType.STATIC : demoType;
        this.filePath = filePath;
        this.apiUrl = apiUrl;
        this.openapiJson = openapiJson;
        this.scenarioJson = scenarioJson;
        this.externalUrl = externalUrl;
        this.thumbnail = thumbnail;
        this.status = status == null ? DemoStatus.ACTIVE : status;
    }

    public void update(String title, String description, String thumbnail) {
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    /** 새 파일로 다시 올린다. 버전이 1 오른다. */
    public void reupload(String filePath) {
        this.filePath = filePath;
        this.version++;
    }

    /** 버전을 공개한다. Worker가 읽는 공개 포인터와 함께 바뀌어야 한다. */
    public void publish(DemoVersion target) {
        this.currentVersionId = target.getId();
        this.filePath = target.getStoragePrefix();
        this.version = target.getNumber();
        this.status = DemoStatus.ACTIVE;
    }

    /** 자동 생성한 썸네일을 쓴다. 직접 올린 썸네일이 있으면 그대로 둔다. */
    public void applyAutoThumbnail(String url) {
        if (!this.thumbnailManual) {
            this.thumbnail = url;
        }
    }

    public void applyManualThumbnail(String url) {
        this.thumbnail = url;
        this.thumbnailManual = true;
    }

    public void connectBackend(String apiBaseUrl) {
        this.apiUrl = apiBaseUrl;
    }

    public void disconnectBackend() {
        this.apiUrl = null;
    }

    /** 공개를 내리고 초안으로 돌린다. 파일과 버전은 그대로 둔다. */
    public void unpublish() {
        this.currentVersionId = null;
        this.status = DemoStatus.DRAFT;
    }

    public void hide() {
        this.status = DemoStatus.HIDDEN;
    }

    public void delete() {
        this.status = DemoStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}
