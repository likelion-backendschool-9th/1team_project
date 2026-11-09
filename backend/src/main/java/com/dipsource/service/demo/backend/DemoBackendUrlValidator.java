package com.dipsource.service.demo.backend;

import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 외부 백엔드 주소의 형식 규칙.
 *
 * <p>여기서는 문자열만 본다. 도메인이 실제로 어느 IP로 가는지는 알 수 없으므로
 * 서버가 요청을 보내는 쪽의 SSRF 방어는 {@link SocketDemoBackendProbe}가 맡는다.
 */
@Component
public class DemoBackendUrlValidator {

    private static final int MAX_LENGTH = 500;
    private static final int MAX_PORT = 65535;
    /** 쿼리·프래그먼트·계정 정보·퍼센트 인코딩·역슬래시를 문자 단계에서 뺀다 */
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("[A-Za-z0-9:/._~-]+");
    private static final Pattern LABEL = Pattern.compile("[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?");
    /** 마지막 라벨이 문자여야 하므로 10진수·8진수·16진수로 쓴 IP가 걸러진다 */
    private static final Pattern TOP_LABEL = Pattern.compile("[a-z]{2,63}|xn--[a-z0-9-]{1,59}");
    private static final Set<String> RESERVED_SUFFIXES = Set.of(
            "localhost", "local", "localdomain", "internal", "intranet", "lan", "home", "corp", "private",
            "test", "invalid", "example", "onion", "arpa",
            // 이름에 적은 IP로 응답하는 공용 DNS
            "nip.io", "sslip.io", "xip.io", "localtest.me", "lvh.me");
    private static final Set<Integer> HEALTH_PORTS = Set.of(-1, 443, 8443);

    private final Set<String> ownHosts = new HashSet<>();

    @Autowired
    public DemoBackendUrlValidator(DemoProperties properties, @Value("${app.frontend-base-url}") String frontendBaseUrl) {
        this(properties.workerBaseUrl(), frontendBaseUrl);
    }

    DemoBackendUrlValidator(String... ownUrls) {
        Arrays.stream(ownUrls).map(DemoBackendUrlValidator::hostOf).filter(host -> host != null).forEach(ownHosts::add);
    }

    /** @return 끝의 / 와 기본 포트를 뺀 주소 */
    public String normalizeApiBaseUrl(String raw) {
        URI uri = parse(raw);
        return originOf(uri) + normalizedPath(uri);
    }

    /** 서버가 요청을 보내는 주소라 API Base URL과 같은 출처, 443·8443 포트만 허용한다. */
    public String normalizeHealthCheckUrl(String raw, String apiBaseUrl) {
        URI uri = parse(raw);
        if (!originOf(uri).equals(originOf(URI.create(apiBaseUrl)))) {
            throw invalid("상태 확인 주소는 API Base URL과 같은 도메인이어야 해요");
        }
        if (!HEALTH_PORTS.contains(uri.getPort())) {
            throw invalid("상태 확인 주소는 443 또는 8443 포트만 쓸 수 있어요");
        }
        return originOf(uri) + normalizedPath(uri);
    }

    /** 저장된 주소를 다시 검사한다. 규칙이 바뀐 뒤 남아 있는 값으로 요청을 보내지 않기 위한 것이다. */
    public boolean isAllowedHealthCheckUrl(String url, String apiBaseUrl) {
        try {
            return normalizeHealthCheckUrl(url, normalizeApiBaseUrl(apiBaseUrl)).equals(url);
        } catch (BusinessException e) {
            return false;
        }
    }

    private URI parse(String raw) {
        String value = raw == null ? "" : raw.strip();
        if (value.isEmpty() || value.length() > MAX_LENGTH) {
            throw invalid("주소는 1~500자로 입력해 주세요");
        }
        if (!value.toLowerCase(Locale.ROOT).startsWith("https://")) {
            throw invalid("https:// 주소만 등록할 수 있어요. https 페이지에서 http API는 브라우저가 차단해요");
        }
        if (!ALLOWED_CHARACTERS.matcher(value).matches()) {
            throw invalid("주소에는 영문, 숫자와 : / . _ ~ - 만 쓸 수 있어요. 쿼리(?)나 계정 정보(@)는 넣을 수 없어요");
        }
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            throw invalid("주소 형식이 올바르지 않아요");
        }
        requirePublicHostName(uri.getHost());
        // URI는 포트 범위를 검사하지 않는다
        if (uri.getPort() == 0 || uri.getPort() > MAX_PORT) {
            throw invalid("포트 번호가 올바르지 않아요");
        }
        return uri;
    }

    private void requirePublicHostName(String rawHost) {
        if (rawHost == null) {
            throw invalid("주소 형식이 올바르지 않아요");
        }
        String host = rawHost.toLowerCase(Locale.ROOT);
        String[] labels = host.split("\\.", -1);
        boolean wellFormed = labels.length >= 2 && host.length() <= 253
                && Arrays.stream(labels).allMatch(label -> LABEL.matcher(label).matches())
                && TOP_LABEL.matcher(labels[labels.length - 1]).matches();
        if (!wellFormed) {
            throw invalid("IP 주소가 아닌 공개 도메인을 입력해 주세요");
        }
        if (RESERVED_SUFFIXES.stream().anyMatch(suffix -> host.equals(suffix) || host.endsWith("." + suffix))) {
            throw invalid("내부망·예약 도메인은 등록할 수 없어요");
        }
        if (ownHosts.stream().anyMatch(own -> host.equals(own) || host.endsWith("." + own))) {
            throw invalid("찍먹 서비스 주소는 등록할 수 없어요");
        }
    }

    private String normalizedPath(URI uri) {
        String path = uri.getRawPath();
        for (String segment : path.split("/", -1)) {
            if (segment.equals(".") || segment.equals("..")) {
                throw invalid("주소에 . 이나 .. 경로는 쓸 수 없어요");
            }
        }
        if (path.contains("//")) {
            throw invalid("주소 형식이 올바르지 않아요");
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private static String originOf(URI uri) {
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        int port = uri.getPort();
        return "https://" + host + (port == -1 || port == 443 ? "" : ":" + port);
    }

    private static String hostOf(String url) {
        try {
            String host = url == null ? null : new URI(url.strip()).getHost();
            return host == null ? null : host.toLowerCase(Locale.ROOT);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.BAD_REQUEST, message);
    }
}
