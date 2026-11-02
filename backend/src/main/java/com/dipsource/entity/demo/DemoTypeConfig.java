package com.dipsource.entity.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** API·외부 데모의 상태 확인 설정. 데모 하나에 하나. 테이블: demo_type_config */
@Getter
@Entity
@Table(name = "demo_type_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoTypeConfig {

    /** 설정 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 대상 데모 */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demo_id", nullable = false, unique = true)
    private Demo demo;

    /** 상태 확인(health check) URL */
    @Column(name = "health_check_url", length = 500)
    private String healthCheckUrl;

    /** 마지막 상태 확인 시각 */
    @Column(name = "last_health_at")
    private LocalDateTime lastHealthAt;

    /** 정상 동작 여부 */
    @Column(name = "is_healthy", nullable = false)
    private boolean healthy = true;

    /** 동작하지 않을 때 녹화본(리플레이)으로 대신 보여 줄지 여부 */
    @Column(name = "fallback_to_replay", nullable = false)
    private boolean fallbackToReplay = false;

    @Builder
    private DemoTypeConfig(Demo demo, String healthCheckUrl, boolean fallbackToReplay) {
        this.demo = demo;
        this.healthCheckUrl = healthCheckUrl;
        this.fallbackToReplay = fallbackToReplay;
    }

    public void recordHealth(boolean healthy) {
        this.healthy = healthy;
        this.lastHealthAt = LocalDateTime.now();
    }
}
