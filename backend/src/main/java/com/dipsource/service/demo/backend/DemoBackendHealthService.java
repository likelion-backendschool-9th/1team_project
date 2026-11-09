package com.dipsource.service.demo.backend;

import com.dipsource.dto.demo.DemoBackendResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoTypeConfig;
import com.dipsource.global.config.DemoBackendProperties;
import com.dipsource.repository.demo.DemoTypeConfigRepository;
import com.dipsource.service.demo.DemoAccess;
import com.dipsource.service.demo.DemoResponseAssembler;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 외부 백엔드 상태 확인. 로그인한 사용자가 요청할 때만 보내고 주기적으로 돌리지 않는다.
 *
 * <p>요청 수는 데모별 최소 간격(DB 조건부 UPDATE)과 인스턴스별 동시 실행 수로 제한한다.
 * 제한에 걸리면 외부 요청 없이 마지막 상태를 돌려준다. 외부 응답을 기다리는 동안 DB 연결을 잡지 않는다.
 */
@Slf4j
@Service
public class DemoBackendHealthService {

    private final DemoAccess demoAccess;
    private final DemoTypeConfigRepository configRepository;
    private final DemoBackendUrlValidator urlValidator;
    private final DemoBackendProbe probe;
    private final DemoResponseAssembler assembler;
    private final DemoBackendProperties properties;
    private final Semaphore permits;

    public DemoBackendHealthService(DemoAccess demoAccess, DemoTypeConfigRepository configRepository,
                                    DemoBackendUrlValidator urlValidator, DemoBackendProbe probe,
                                    DemoResponseAssembler assembler, DemoBackendProperties properties) {
        this.demoAccess = demoAccess;
        this.configRepository = configRepository;
        this.urlValidator = urlValidator;
        this.probe = probe;
        this.assembler = assembler;
        this.properties = properties;
        this.permits = new Semaphore(properties.healthMaxConcurrent());
    }

    public DemoBackendResponse check(Long viewerId, Long demoId) {
        Demo demo = demoAccess.findVisible(demoId, viewerId);
        if (properties.healthCheckEnabled() && demo.getApiUrl() != null) {
            configRepository.findByDemoId(demoId)
                    .map(DemoTypeConfig::getHealthCheckUrl)
                    .filter(url -> urlValidator.isAllowedHealthCheckUrl(url, demo.getApiUrl()))
                    .ifPresent(url -> checkWithinLimits(demoId, url));
        }
        return assembler.toBackendResponse(demo, viewerId);
    }

    private void checkWithinLimits(Long demoId, String url) {
        // 동시 실행 한도에 걸린 요청이 최소 간격을 소모하지 않도록 먼저 자리를 얻는다
        if (!permits.tryAcquire()) {
            return;
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            if (configRepository.claimHealthCheck(demoId, url, now, now.minusSeconds(properties.healthCooldownSeconds())) == 1) {
                record(demoId, url, probeSafely(url));
            }
        } finally {
            permits.release();
        }
    }

    private DemoBackendProbe.Outcome probeSafely(String url) {
        try {
            return probe.probe(URI.create(url));
        } catch (RuntimeException e) {
            log.warn("외부 백엔드 상태 확인 실패", e);
            return DemoBackendProbe.Outcome.UNVERIFIABLE;
        }
    }

    private void record(Long demoId, String url, DemoBackendProbe.Outcome outcome) {
        if (outcome == DemoBackendProbe.Outcome.UNVERIFIABLE) {
            configRepository.clearHealthResult(demoId, url);
            return;
        }
        configRepository.recordHealthResult(demoId, url, outcome == DemoBackendProbe.Outcome.ONLINE, LocalDateTime.now());
    }
}
