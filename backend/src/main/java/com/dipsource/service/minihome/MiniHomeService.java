package com.dipsource.service.minihome;

import com.dipsource.dto.minihome.MiniHomeResponse;
import com.dipsource.dto.minihome.RandomMiniHomeResponse;
import com.dipsource.dto.minihome.UpdateMiniHomeRequest;
import com.dipsource.dto.minihome.VisitorStatsResponse;

/** 미니홈피 조회·수정. */
public interface MiniHomeService {

    /** 없으면 기본값으로 만든다. 없는 회원이면 예외. */
    MiniHomeResponse getOrCreate(Long userId);

    /** 내 미니홈피 수정. stackTags는 저장하지 않는다. */
    MiniHomeResponse update(Long userId, UpdateMiniHomeRequest request);

    /** 공개 중인 데모를 대표(1번 칸)로 올린다. */
    MiniHomeResponse updateFeaturedDemo(Long userId, Long demoId);

    /** 방문자 수. 없는 회원이면 예외. 홈이 없으면 0. */
    VisitorStatsResponse getVisitors(Long userId);

    /** 방문 기록. 본인 방문은 올리지 않는다. */
    VisitorStatsResponse recordVisit(Long viewerId, Long ownerId);

    /** 본인과 exclude를 뺀 미니홈피 하나. 없으면 예외. */
    RandomMiniHomeResponse pickRandom(Long viewerId, Long excludeUserId);

    /** 모든 미니홈피의 오늘 방문자 수를 0으로 만든다. */
    void resetTodayCount();
}
