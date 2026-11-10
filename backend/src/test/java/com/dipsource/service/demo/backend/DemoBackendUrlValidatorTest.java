package com.dipsource.service.demo.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dipsource.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DemoBackendUrlValidatorTest {

    private final DemoBackendUrlValidator validator =
            new DemoBackendUrlValidator("https://runner.workers.dev", "https://dipsource.com");

    @ParameterizedTest
    @CsvSource({
            "https://example.com/api, https://example.com/api",
            "https://example.com/api/, https://example.com/api",
            "HTTPS://API.Example.COM/v1, https://api.example.com/v1",
            "https://example.com, https://example.com",
            "https://example.com:443/api, https://example.com/api",
            "https://example.com:9000/api, https://example.com:9000/api",
            "https://my-app.onrender.com/api/v1, https://my-app.onrender.com/api/v1",
            "https://xn--9n2bp8q.xn--3e0b707e/api, https://xn--9n2bp8q.xn--3e0b707e/api",
    })
    void API_주소를_정규화한다(String input, String expected) {
        assertThat(validator.normalizeApiBaseUrl(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "   ", "example.com", "http://example.com", "ftp://example.com", "javascript:alert(1)",
            "file:///etc/passwd", "data:text/html,x", "//example.com/api",
            "https://user:pw@example.com", "https://example.com@evil.org", "https://example.com/api?token=abc",
            "https://example.com/api#x", "https://example.com/a%2e%2e/b", "https://example.com\\@evil.org",
            "https://example.com/a b", "https://example.com/\r\nHost: evil", "https://example.com/../admin",
            "https://example.com/a/./b", "https://example.com//a",
            "https://localhost", "https://localhost:8080/api", "https://foo.localhost", "https://app.local",
            "https://db.internal", "https://svc.corp", "https://x.test", "https://x.invalid", "https://x.onion",
            "https://1.0.0.127.in-addr.arpa", "https://127.0.0.1.nip.io", "https://a.sslip.io",
            "https://127.0.0.1", "https://10.0.0.1/api", "https://192.168.0.1", "https://169.254.169.254/latest",
            "https://[::1]", "https://[::ffff:127.0.0.1]", "https://2130706433", "https://0x7f000001",
            "https://0x7f.0.0.1", "https://017700000001", "https://127.1", "https://example",
            "https://-bad.example.com", "https://exa_mple.com", "https://example.com.", "https://한글.com",
            "https://runner.workers.dev", "https://sub.runner.workers.dev/api", "https://dipsource.com/api",
            "https://www.dipsource.com",
            "https://example.com:0/api", "https://example.com:65536", "https://example.com:99999/api",
    })
    void 허용하지_않는_주소는_거부한다(String input) {
        assertThatThrownBy(() -> validator.normalizeApiBaseUrl(input)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 지나치게_긴_주소는_거부한다() {
        String input = "https://example.com/" + "a".repeat(500);
        assertThatThrownBy(() -> validator.normalizeApiBaseUrl(input)).isInstanceOf(BusinessException.class);
    }

    @Test
    void null도_거부한다() {
        assertThatThrownBy(() -> validator.normalizeApiBaseUrl(null)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("헬스체크 주소는 API와 같은 출처만 허용한다")
    void sameOriginOnly() {
        assertThat(validator.normalizeHealthCheckUrl("https://example.com/health/", "https://example.com/api"))
                .isEqualTo("https://example.com/health");
        assertThatThrownBy(() -> validator.normalizeHealthCheckUrl("https://other.com/health", "https://example.com/api"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> validator.normalizeHealthCheckUrl("https://api.example.com/health", "https://example.com/api"))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> validator.normalizeHealthCheckUrl("https://example.com:8443/health", "https://example.com/api"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("헬스체크는 임의 포트를 쓸 수 없다. 서버를 포트 스캐너로 쓰지 못하게 한다")
    void healthPorts() {
        assertThat(validator.normalizeHealthCheckUrl("https://example.com:8443/health", "https://example.com:8443/api"))
                .isEqualTo("https://example.com:8443/health");
        assertThatThrownBy(() -> validator.normalizeHealthCheckUrl("https://example.com:22/health", "https://example.com:22/api"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 저장된_주소를_다시_검사한다() {
        assertThat(validator.isAllowedHealthCheckUrl("https://example.com/health", "https://example.com/api")).isTrue();
        assertThat(validator.isAllowedHealthCheckUrl("https://localhost/health", "https://localhost/api")).isFalse();
    }
}
