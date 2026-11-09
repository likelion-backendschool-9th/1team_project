package com.dipsource.service.demo.backend;

import static org.assertj.core.api.Assertions.assertThat;

import com.dipsource.service.demo.backend.DemoBackendProbe.Outcome;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** 실제 TLS 서버를 띄워 검증한다. 테스트에서만 루프백을 허용하고, 이름은 고정 해석기로 대신한다. */
class SocketDemoBackendProbeTest {

    private static final String HOST = "api.demo.example";
    private static final char[] PASSWORD = "changeit".toCharArray();
    private static final Predicate<InetAddress> LOOPBACK_ONLY = InetAddress::isLoopbackAddress;

    private static SSLContext tls;

    @TempDir
    static Path directory;

    private SSLServerSocket server;
    private final AtomicInteger connections = new AtomicInteger();
    private final List<String> requests = Collections.synchronizedList(new ArrayList<>());

    interface Handler {
        void handle(Socket socket) throws Exception;
    }

    @BeforeAll
    static void createCertificate() throws Exception {
        Path store = directory.resolve("server.p12");
        Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "keytool").toString(),
                "-genkeypair", "-alias", "server", "-keyalg", "RSA", "-keysize", "2048", "-validity", "2",
                "-dname", "CN=" + HOST, "-ext", "san=dns:" + HOST, "-storetype", "PKCS12",
                "-keystore", store.toString(), "-storepass", "changeit", "-keypass", "changeit")
                .redirectErrorStream(true).start();
        process.getInputStream().readAllBytes();
        assertThat(process.waitFor()).isZero();

        KeyStore keys = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(store)) {
            keys.load(in, PASSWORD);
        }
        KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagers.init(keys, PASSWORD);
        TrustManagerFactory trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagers.init(keys);
        tls = SSLContext.getInstance("TLS");
        tls.init(keyManagers.getKeyManagers(), trustManagers.getTrustManagers(), null);
    }

    @AfterEach
    void close() throws IOException {
        if (server != null) {
            server.close();
        }
    }

    private int serve(Handler handler) throws IOException {
        server = (SSLServerSocket) tls.getServerSocketFactory().createServerSocket(0, 10, InetAddress.getLoopbackAddress());
        Thread.startVirtualThread(() -> {
            while (!server.isClosed()) {
                try {
                    Socket socket = server.accept();
                    connections.incrementAndGet();
                    Thread.startVirtualThread(() -> {
                        try (socket) {
                            handler.handle(socket);
                        } catch (Exception ignored) {
                            // 클라이언트가 먼저 끊는 경우
                        }
                    });
                } catch (IOException e) {
                    return;
                }
            }
        });
        return server.getLocalPort();
    }

    private Handler respond(String head) {
        return socket -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            StringBuilder request = new StringBuilder();
            for (String line = reader.readLine(); line != null && !line.isEmpty(); line = reader.readLine()) {
                request.append(line).append('\n');
            }
            requests.add(request.toString());
            socket.getOutputStream().write((head + "\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
        };
    }

    private SocketDemoBackendProbe probe(SocketDemoBackendProbe.Resolver resolver, Predicate<InetAddress> policy, long timeoutMs) {
        SSLSocketFactory factory = tls.getSocketFactory();
        return new SocketDemoBackendProbe(resolver, policy, factory, timeoutMs);
    }

    private SocketDemoBackendProbe probe() {
        return probe(host -> new InetAddress[] {InetAddress.getLoopbackAddress()}, LOOPBACK_ONLY, 3000);
    }

    private static URI url(int port) {
        return URI.create("https://" + HOST + ":" + port + "/health");
    }

    @Test
    void 응답이_2xx면_ONLINE이다() throws Exception {
        int port = serve(respond("HTTP/1.1 204 No Content"));
        assertThat(probe().probe(url(port))).isEqualTo(Outcome.ONLINE);
        assertThat(requests.get(0))
                .startsWith("GET /health HTTP/1.1\n")
                .contains("Host: " + HOST + ":" + port)
                .doesNotContainIgnoringCase("cookie")
                .doesNotContainIgnoringCase("authorization");
    }

    @Test
    void 서버_오류는_OFFLINE이다() throws Exception {
        int port = serve(respond("HTTP/1.1 503 Service Unavailable"));
        assertThat(probe().probe(url(port))).isEqualTo(Outcome.OFFLINE);
    }

    @Test
    void 리다이렉트를_따라가지_않는다() throws Exception {
        int port = serve(respond("HTTP/1.1 302 Found\r\nLocation: https://169.254.169.254/latest/meta-data"));
        assertThat(probe().probe(url(port))).isEqualTo(Outcome.OFFLINE);
        assertThat(connections).hasValue(1);
    }

    @Test
    void 인증서의_도메인이_다르면_OFFLINE이다() throws Exception {
        int port = serve(respond("HTTP/1.1 200 OK"));
        URI other = URI.create("https://wrong.demo.example:" + port + "/health");
        assertThat(probe().probe(other)).isEqualTo(Outcome.OFFLINE);
        assertThat(requests).isEmpty();
    }

    @Test
    void 사설_IP로_해석되면_연결하지_않는다() throws Exception {
        int port = serve(respond("HTTP/1.1 200 OK"));
        SocketDemoBackendProbe real = probe(host -> new InetAddress[] {InetAddress.getLoopbackAddress()},
                DemoBackendAddressPolicy::isPublic, 3000);
        assertThat(real.probe(url(port))).isEqualTo(Outcome.UNVERIFIABLE);
        assertThat(connections).hasValue(0);
    }

    @Test
    void 공인_IP와_사설_IP가_섞이면_전부_거부한다() throws Exception {
        int port = serve(respond("HTTP/1.1 200 OK"));
        SocketDemoBackendProbe real = probe(
                host -> new InetAddress[] {InetAddress.getByName("8.8.8.8"), InetAddress.getLoopbackAddress()},
                DemoBackendAddressPolicy::isPublic, 3000);
        assertThat(real.probe(url(port))).isEqualTo(Outcome.UNVERIFIABLE);
        assertThat(connections).hasValue(0);
    }

    @Test
    @DisplayName("DNS 재바인딩: 한 번 조회해 검사한 IP로만 연결하고 다시 조회하지 않는다")
    void dnsRebinding() throws Exception {
        int port = serve(respond("HTTP/1.1 200 OK"));
        AtomicInteger lookups = new AtomicInteger();
        SocketDemoBackendProbe rebinding = probe(host -> {
            if (lookups.getAndIncrement() == 0) {
                return new InetAddress[] {InetAddress.getLoopbackAddress()};
            }
            throw new AssertionError("두 번째 DNS 조회가 발생했습니다");
        }, LOOPBACK_ONLY, 3000);
        assertThat(rebinding.probe(url(port))).isEqualTo(Outcome.ONLINE);
        assertThat(lookups).hasValue(1);
    }

    @Test
    void 응답이_없으면_제한_시간에_끊고_소켓을_닫는다() throws Exception {
        CountDownLatch peerClosed = new CountDownLatch(1);
        int port = serve(socket -> {
            InputStream in = socket.getInputStream();
            try {
                while (in.read() != -1) {
                    // 요청을 읽고 응답하지 않는다
                }
            } finally {
                peerClosed.countDown();
            }
        });
        SocketDemoBackendProbe slow = probe(host -> new InetAddress[] {InetAddress.getLoopbackAddress()}, LOOPBACK_ONLY, 400);
        long started = System.nanoTime();
        assertThat(slow.probe(url(port))).isEqualTo(Outcome.OFFLINE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)).isLessThan(2000);
        assertThat(peerClosed.await(3, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void 한_글자씩_보내는_서버도_전체_제한_시간에_끊는다() throws Exception {
        int port = serve(socket -> {
            socket.getInputStream().read();
            for (int i = 0; i < 200; i++) {
                socket.getOutputStream().write('H');
                socket.getOutputStream().flush();
                Thread.sleep(100);
            }
        });
        SocketDemoBackendProbe slow = probe(host -> new InetAddress[] {InetAddress.getLoopbackAddress()}, LOOPBACK_ONLY, 400);
        long started = System.nanoTime();
        assertThat(slow.probe(url(port))).isEqualTo(Outcome.OFFLINE);
        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)).isLessThan(2000);
    }

    @Test
    void 끝없는_응답은_일정_크기만_읽고_끊는다() throws Exception {
        AtomicInteger written = new AtomicInteger();
        CountDownLatch finished = new CountDownLatch(1);
        int port = serve(socket -> {
            socket.getInputStream().read();
            byte[] chunk = new byte[64 * 1024];
            java.util.Arrays.fill(chunk, (byte) 'x');
            try {
                for (int i = 0; i < 16_000; i++) {
                    socket.getOutputStream().write(chunk);
                    written.addAndGet(chunk.length);
                }
            } finally {
                finished.countDown();
            }
        });
        assertThat(probe().probe(url(port))).isEqualTo(Outcome.OFFLINE);
        assertThat(finished.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(written.get()).isLessThan(16_000 * 64 * 1024);
    }

    @Test
    void DNS_실패는_OFFLINE이다() {
        SocketDemoBackendProbe missing = probe(host -> {
            throw new UnknownHostException(host);
        }, LOOPBACK_ONLY, 1000);
        assertThat(missing.probe(url(443))).isEqualTo(Outcome.OFFLINE);
    }

    @Test
    void 닫힌_포트는_OFFLINE이다() throws Exception {
        int closed;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            closed = socket.getLocalPort();
        }
        assertThat(probe().probe(url(closed))).isEqualTo(Outcome.OFFLINE);
    }
}
