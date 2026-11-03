package com.dipsource.entity.minihome;

import com.dipsource.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 미니홈피. 회원 한 명에 하나. 테이블: mini_homes
 * 방문자 수는 동시성 때문에 엔티티에서 더하지 않고 리포지토리의 UPDATE 쿼리로 갱신한다.
 */
@Getter
@Entity
@Table(name = "mini_homes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MiniHome extends BaseEntity {

    /** 미니홈피 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 주인 회원 ID(users.id) */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** 대표 데모 1 ID(demos.id) */
    @Column(name = "showcase_demo1_id")
    private Long showcaseDemo1Id;

    /** 대표 데모 2 ID(demos.id) */
    @Column(name = "showcase_demo2_id")
    private Long showcaseDemo2Id;

    /** 대표 데모 3 ID(demos.id) */
    @Column(name = "showcase_demo3_id")
    private Long showcaseDemo3Id;

    /** 미니홈피 제목 */
    @Column(length = 50)
    private String title;

    /** 소개글 */
    @Column(length = 300)
    private String intro;

    /** 스킨 이름. default 등 기본 스킨 이름이고, custom이면 custom_skin_json을 읽는다 */
    @Column(name = "skin_theme", length = 50)
    private String skinTheme = "default";

    /** 커스텀 스킨 JSON. 배경색·글자색·배경 이미지 URL 등. skin_theme이 custom일 때 사용 */
    @Column(name = "custom_skin_json", length = 5000)
    private String customSkinJson;

    /** 오늘 방문자 수 */
    @Column(name = "today_count", nullable = false)
    private int todayCount = 0;

    /** 전체 방문자 수 */
    @Column(name = "total_count", nullable = false)
    private int totalCount = 0;

    @Builder
    private MiniHome(Long userId, String title, String intro) {
        this.userId = userId;
        this.title = title;
        this.intro = intro;
    }

    public void update(String title, String intro) {
        this.title = title;
        this.intro = intro;
    }

    public void changeSkin(String skinTheme, String customSkinJson) {
        this.skinTheme = skinTheme;
        this.customSkinJson = customSkinJson;
    }

    /** 대표 데모 3칸을 한 번에 바꾼다. 빈 칸은 null. */
    public void changeShowcase(Long demo1Id, Long demo2Id, Long demo3Id) {
        this.showcaseDemo1Id = demo1Id;
        this.showcaseDemo2Id = demo2Id;
        this.showcaseDemo3Id = demo3Id;
    }
}
