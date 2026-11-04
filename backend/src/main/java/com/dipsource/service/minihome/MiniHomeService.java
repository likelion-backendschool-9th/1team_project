package com.dipsource.service.minihome;

import com.dipsource.dto.minihome.MiniHomeResponse;
import com.dipsource.dto.minihome.UpdateMiniHomeRequest;

/** 미니홈피 조회·수정. */
public interface MiniHomeService {

    /** 없으면 기본값으로 만든다. 없는 회원이면 예외. */
    MiniHomeResponse getOrCreate(Long userId);

    /** 내 미니홈피 수정. stackTags는 저장하지 않는다. */
    MiniHomeResponse update(Long userId, UpdateMiniHomeRequest request);

    /** 공개 중인 데모를 대표(1번 칸)로 올린다. */
    MiniHomeResponse updateFeaturedDemo(Long userId, Long demoId);
}
