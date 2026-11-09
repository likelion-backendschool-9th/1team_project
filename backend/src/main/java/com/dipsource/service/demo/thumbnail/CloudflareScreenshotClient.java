package com.dipsource.service.demo.thumbnail;

import com.dipsource.global.config.DemoProperties;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Cloudflare Browser Run(구 Browser Rendering)의 screenshot 엔드포인트. */
@Component
@ConditionalOnProperty(name = "demo.thumbnail.enabled", havingValue = "true")
public class CloudflareScreenshotClient implements DemoScreenshotClient {

    private static final String BASE_URL = "https://api.cloudflare.com/client/v4/accounts/";
    private static final int CONNECT_TIMEOUT_MS = 5_000;
    /** Browser Run의 브라우저 제한 시간(60초)보다 길게 기다린다 */
    private static final int READ_TIMEOUT_MS = 70_000;

    private final RestClient restClient;
    private final String path;

    public CloudflareScreenshotClient(DemoProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.thumbnail().apiToken())
                .build();
        this.path = properties.thumbnail().accountId() + "/browser-rendering/screenshot";
    }

    @Override
    public byte[] capture(String url) {
        return restClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "url", url,
                        "viewport", Map.of("width", 1280, "height", 800),
                        "screenshotOptions", Map.of("type", "jpeg", "quality", 80),
                        "gotoOptions", Map.of("waitUntil", "networkidle2", "timeout", 30_000)))
                .exchange((request, response) -> {
                    MediaType type = response.getHeaders().getContentType();
                    if (!response.getStatusCode().is2xxSuccessful() || type == null || !"image".equals(type.getType())) {
                        // 응답 본문이나 요청 헤더는 토큰이 섞일 수 있어 원인에 남기지 않는다
                        throw new IllegalStateException("Browser Run 응답 " + response.getStatusCode().value());
                    }
                    return response.getBody().readAllBytes();
                });
    }
}
