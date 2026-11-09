package com.dipsource.entity.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자별 데모 사용량. 동시 요청에서도 정확하도록 항상 조건부 UPDATE로만 바꾼다. 테이블: demo_usages */
@Getter
@Entity
@Table(name = "demo_usages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoUsage {

    /** 회원 ID(users.id) */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /** 저장이 끝난 크기(바이트) */
    @Column(name = "used_bytes", nullable = false)
    private long usedBytes;

    /** 업로드 중이라 예약해 둔 크기(바이트) */
    @Column(name = "reserved_bytes", nullable = false)
    private long reservedBytes;

    /** 삭제하지 않은 데모 수 */
    @Column(name = "demo_count", nullable = false)
    private int demoCount;
}
