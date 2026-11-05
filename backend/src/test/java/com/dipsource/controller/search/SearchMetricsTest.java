package com.dipsource.controller.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** 검색 성능 측정(5단계) 설정 테스트. 측정값이 쌓이는지, 측정 주소와 측정값으로 정보가 새지 않는지 확인한다. */
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test"
})
class SearchMetricsTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MeterRegistry meterRegistry;

    @Test
    @DisplayName("검색 기능 서비스의 메서드별 실행 시간을 search.query로 잰다")
    void recordsSearchQueryTimer() throws Exception {
        mockMvc.perform(get("/api/v1/search/autocomplete").param("q", "spr")).andExpect(status().isOk());

        Timer timer = meterRegistry.find("search.query")
                .tag("class", "com.dipsource.service.post.impl.PostSearchServiceImpl")
                .tag("method", "findTermsByPrefix")
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isPositive();
    }

    @Test
    @DisplayName("HTTP 요청 시간은 경로 이름으로 묶는다")
    void recordsRequestByUriTemplate() throws Exception {
        mockMvc.perform(get("/api/v1/search/autocomplete").param("q", "spr")).andExpect(status().isOk());

        assertThat(meterRegistry.find("http.server.requests").tag("uri", "/api/v1/search/autocomplete").timer())
                .isNotNull();
    }

    @Test
    @DisplayName("[보안] 검색어 원문은 측정값 어디에도 남지 않는다")
    void queryNotInTags() throws Exception {
        mockMvc.perform(get("/api/v1/search").param("q", "secretkeyword")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/search/autocomplete").param("q", "secretkeyword")).andExpect(status().isOk());

        assertThat(meterRegistry.getMeters())
                .flatMap(meter -> meter.getId().getTags())
                .extracting(Tag::getValue)
                .noneMatch(value -> value.contains("secretkeyword"));
    }

    @Test
    @DisplayName("[보안] metrics 주소는 기본으로 닫혀 있다")
    void metricsClosedByDefault() throws Exception {
        mockMvc.perform(get("/actuator/metrics")).andExpect(status().isNotFound());
        mockMvc.perform(get("/actuator/metrics/search.query")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[보안] health는 상태만 주고 DB 같은 세부 정보는 주지 않는다")
    void healthWithoutDetails() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    @DisplayName("[보안] 다른 actuator 주소(env, beans)는 열리지 않는다")
    void otherEndpointsClosed() throws Exception {
        mockMvc.perform(get("/actuator/env")).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/actuator/beans")).andExpect(status().is4xxClientError());
    }
}
