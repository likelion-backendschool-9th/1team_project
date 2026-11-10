package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoUsageResponse;
import com.dipsource.entity.demo.DemoUsage;
import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자별 용량 관리.
 *
 * <p>업로드는 R2 전송 전에 크기만큼 예약하고, 끝나면 사용량으로 옮기거나 돌려준다.
 * 예약이 조건부 UPDATE 한 문장이라 동시 업로드가 겹쳐도 한도를 넘을 수 없다.
 */
@Service
@RequiredArgsConstructor
public class DemoQuotaService {

    private final DemoUsageRepository usageRepository;
    private final DemoProperties properties;

    @Transactional
    public void reserveDemoSlot(Long userId) {
        usageRepository.ensureRow(userId);
        if (usageRepository.tryIncrementDemoCount(userId, properties.quota().maxDemos()) == 0) {
            throw new BusinessException(ErrorCode.DEMO_QUOTA_EXCEEDED,
                    "데모는 " + properties.quota().maxDemos() + "개까지 만들 수 있습니다");
        }
    }

    @Transactional
    public void releaseDemoSlot(Long userId) {
        usageRepository.decrementDemoCount(userId);
    }

    /** @throws BusinessException 남은 용량이 부족하면 */
    @Transactional
    public void reserveBytes(Long userId, long bytes) {
        usageRepository.ensureRow(userId);
        if (usageRepository.tryReserve(userId, bytes, properties.quota().maxStorageBytes()) == 0) {
            throw new BusinessException(ErrorCode.DEMO_QUOTA_EXCEEDED,
                    "저장 용량이 부족합니다. 사용하지 않는 버전이나 데모를 지워 주세요");
        }
    }

    @Transactional
    public void commitBytes(Long userId, long bytes) {
        usageRepository.commitReservation(userId, bytes);
    }

    @Transactional
    public void releaseReservedBytes(Long userId, long bytes) {
        usageRepository.releaseReservation(userId, bytes);
    }

    @Transactional
    public void releaseUsedBytes(Long userId, long bytes) {
        usageRepository.releaseUsed(userId, bytes);
    }

    @Transactional
    public DemoUsageResponse getUsage(Long userId) {
        usageRepository.ensureRow(userId);
        DemoUsage usage = usageRepository.findById(userId).orElseThrow();
        long limit = properties.quota().maxStorageBytes();
        long used = usage.getUsedBytes() + usage.getReservedBytes();
        return new DemoUsageResponse(used, limit, Math.max(limit - used, 0), usage.getDemoCount(), properties.quota().maxDemos());
    }
}
