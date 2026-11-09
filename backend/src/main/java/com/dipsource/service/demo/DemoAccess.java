package com.dipsource.service.demo;

import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.demo.DemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 데모 조회 권한 규칙을 한곳에 모은다. */
@Component
@RequiredArgsConstructor
public class DemoAccess {

    private static final String NOT_FOUND = "데모를 찾을 수 없습니다";

    private final DemoRepository demoRepository;
    private final BlockRepository blockRepository;

    /** 조회 가능한 데모. 초안·숨김은 작성자에게만 보이고, 그 외에는 없는 것처럼 404를 준다. */
    public Demo findVisible(Long demoId, Long viewerId) {
        Demo demo = findLive(demoId);
        boolean owner = demo.getUserId().equals(viewerId);
        if (!owner && (demo.getStatus() != DemoStatus.ACTIVE || isBlocked(viewerId, demo.getUserId()))) {
            throw new BusinessException(ErrorCode.NOT_FOUND, NOT_FOUND);
        }
        return demo;
    }

    /** 좋아요·댓글처럼 공개 중인 데모에만 할 수 있는 행동의 대상. */
    public Demo findActive(Long demoId, Long viewerId) {
        Demo demo = findLive(demoId);
        if (demo.getStatus() != DemoStatus.ACTIVE || isBlocked(viewerId, demo.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, NOT_FOUND);
        }
        return demo;
    }

    /** 작성자만 다룰 수 있는 데모. */
    public Demo findOwned(Long userId, Long demoId) {
        Demo demo = findLive(demoId);
        requireOwner(demo, userId);
        return demo;
    }

    /** 공개·버전 변경용. 행을 잠그고 가져온다. */
    public Demo findOwnedForUpdate(Long userId, Long demoId) {
        Demo demo = demoRepository.findByIdForUpdate(demoId)
                .filter(d -> d.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, NOT_FOUND));
        requireOwner(demo, userId);
        return demo;
    }

    private Demo findLive(Long demoId) {
        return demoRepository.findById(demoId)
                .filter(d -> d.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, NOT_FOUND));
    }

    private void requireOwner(Demo demo, Long userId) {
        if (demo.getUserId().equals(userId)) {
            return;
        }
        // 초안은 존재 자체를 숨긴다
        if (demo.getStatus() != DemoStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.NOT_FOUND, NOT_FOUND);
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 데모만 변경할 수 있습니다");
    }

    private boolean isBlocked(Long viewerId, Long authorId) {
        return viewerId != null && blockRepository.existsBetween(viewerId, authorId);
    }
}
