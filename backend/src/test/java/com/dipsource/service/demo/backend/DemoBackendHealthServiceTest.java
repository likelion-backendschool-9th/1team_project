package com.dipsource.service.demo.backend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.entity.demo.Demo;
import com.dipsource.global.config.DemoBackendProperties;
import com.dipsource.repository.demo.DemoTypeConfigRepository;
import com.dipsource.service.demo.DemoAccess;
import com.dipsource.service.demo.DemoResponseAssembler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DemoBackendHealthServiceTest {

    @Test
    @DisplayName("기능이 꺼져 있으면 외부 요청을 보내지 않는다")
    void disabledNeverProbes() {
        DemoAccess access = mock(DemoAccess.class);
        DemoTypeConfigRepository repository = mock(DemoTypeConfigRepository.class);
        DemoBackendProbe probe = mock(DemoBackendProbe.class);
        Demo demo = Demo.builder().userId(1L).title("t").apiUrl("https://example.com/api").build();
        when(access.findVisible(7L, 1L)).thenReturn(demo);

        new DemoBackendHealthService(access, repository, new DemoBackendUrlValidator(new String[0]), probe,
                mock(DemoResponseAssembler.class), new DemoBackendProperties(false, 60, 30, 4, 5000)).check(1L, 7L);

        verify(probe, never()).probe(any());
        verify(repository, never()).findByDemoId(any());
    }
}
