package com.dipsource.service.demo;

import com.dipsource.entity.demo.DemoVersion;
import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 비공개 경로를 제한된 시간 동안 Worker에서 열 수 있는 서명 URL을 만든다. Worker의 verifySignature와 짝이다. */
@Component
@RequiredArgsConstructor
public class DemoUrlSigner {

    private static final String ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final DemoProperties properties;
    private final ObjectMapper objectMapper;

    /** 버전의 파일과 Mock을 읽을 수 있는 권한 */
    public Map<String, String> grantOf(DemoVersion version) {
        return version.getMockKey() == null
                ? Map.of("p", version.getStoragePrefix())
                : Map.of("p", version.getStoragePrefix(), "m", version.getMockKey());
    }

    /** 작성자 미리보기용. 공개 전에도 등록한 외부 백엔드를 호출해 볼 수 있다. */
    public Map<String, String> grantOf(DemoVersion version, String apiBaseUrl) {
        Map<String, String> grant = new LinkedHashMap<>(grantOf(version));
        if (apiBaseUrl != null) {
            grant.put("b", apiBaseUrl);
        }
        return grant;
    }

    /**
     * @param grant     Worker에 넘길 권한. p: 파일 경로 접두사
     * @param entryPath 처음 열 파일
     * @param expiresAt 만료 시각
     */
    public String sign(Map<String, String> grant, String entryPath, Instant expiresAt) {
        String secret = properties.signingSecret();
        if (secret == null || secret.isBlank()) {
            throw new BusinessException(ErrorCode.DEMO_STORAGE_UNAVAILABLE, "미리보기 서명 키가 설정되지 않았습니다");
        }
        String scope = encodeGrant(grant);
        long expires = expiresAt.getEpochSecond();
        return properties.workerBaseUrl() + "/p/" + scope + "/" + expires + "." + hmac(secret, scope + "." + expires)
                + "/" + entryPath;
    }

    private String encodeGrant(Map<String, String> grant) {
        try {
            return ENCODER.encodeToString(objectMapper.writeValueAsBytes(grant));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String hmac(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return ENCODER.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
