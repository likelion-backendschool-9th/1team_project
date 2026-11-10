package com.dipsource.service.demo.backend;

import com.dipsource.dto.demo.DemoBackendRequest;
import com.dipsource.dto.demo.DemoResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoStatus;
import com.dipsource.entity.demo.DemoTypeConfig;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoTypeConfigRepository;
import com.dipsource.service.demo.DemoAccess;
import com.dipsource.service.demo.DemoPublicationService;
import com.dipsource.service.demo.DemoResponseAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 외부 백엔드 연결 정보를 관리한다. 실행 방식(DemoType)과 독립적이라 정적 데모에도, Mock을 쓰는 데모에도 붙일 수 있다.
 * 업로더의 백엔드 코드는 실행하지 않고 주소만 저장한다.
 */
@Service
@RequiredArgsConstructor
public class DemoBackendService {

    private final DemoAccess demoAccess;
    private final DemoTypeConfigRepository configRepository;
    private final DemoBackendUrlValidator urlValidator;
    private final DemoPublicationService publicationService;
    private final DemoResponseAssembler assembler;

    @Transactional
    public DemoResponse connect(Long userId, Long demoId, DemoBackendRequest request) {
        Demo demo = findEditable(userId, demoId);
        String apiBaseUrl = urlValidator.normalizeApiBaseUrl(request.apiBaseUrl());
        String healthCheckUrl = request.healthCheckUrl() == null || request.healthCheckUrl().isBlank()
                ? null : urlValidator.normalizeHealthCheckUrl(request.healthCheckUrl(), apiBaseUrl);
        demo.connectBackend(apiBaseUrl);
        applyHealthCheckUrl(demo, healthCheckUrl);
        publicationService.refreshBackend(demo);
        return assembler.toResponse(demo, userId);
    }

    @Transactional
    public void disconnect(Long userId, Long demoId) {
        Demo demo = findEditable(userId, demoId);
        demo.disconnectBackend();
        applyHealthCheckUrl(demo, null);
        publicationService.refreshBackend(demo);
    }

    private Demo findEditable(Long userId, Long demoId) {
        Demo demo = demoAccess.findOwnedForUpdate(userId, demoId);
        if (demo.getStatus() == DemoStatus.HIDDEN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자가 숨긴 데모는 변경할 수 없습니다");
        }
        return demo;
    }

    /** 연결을 끊어도 행을 남긴다. 지웠다 다시 만들어 헬스체크 최소 간격을 건너뛰지 못하게 하기 위해서다. */
    private void applyHealthCheckUrl(Demo demo, String url) {
        configRepository.findByDemoId(demo.getId()).ifPresentOrElse(
                config -> config.changeHealthCheckUrl(url),
                () -> {
                    if (url != null) {
                        configRepository.save(DemoTypeConfig.builder().demo(demo).healthCheckUrl(url).build());
                    }
                });
    }
}
