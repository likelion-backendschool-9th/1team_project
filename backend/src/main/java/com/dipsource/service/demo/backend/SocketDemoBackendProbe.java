package com.dipsource.service.demo.backend;

import com.dipsource.global.config.DemoBackendProperties;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 소켓을 직접 열어 상태 코드 한 줄만 읽는다.
 *
 * <p>일반 HTTP 클라이언트는 URL 검사 후 연결할 때 DNS를 다시 조회하므로 DNS 재바인딩을 막지 못한다.
 * 여기서는 한 번 조회해 모든 IP를 검사하고, 검사한 IP에 그대로 연결한다. 인증서는 원래 도메인으로 검증한다.
 * 리다이렉트를 따라가지 않고 본문을 읽지 않으며, 전체 시간은 바깥 Future가 끊는다.
 */
@Component
public class SocketDemoBackendProbe implements DemoBackendProbe {

    private static final int MAX_STATUS_LINE_BYTES = 256;
    private static final int MAX_ADDRESSES = 3;
    private static final Pattern SUCCESS = Pattern.compile("HTTP/1\\.[01] 2\\d\\d( .*)?");

    interface Resolver {
        InetAddress[] resolve(String host) throws UnknownHostException;
    }

    private final Resolver resolver;
    private final Predicate<InetAddress> addressPolicy;
    private final SSLSocketFactory tlsFactory;
    private final long timeoutMs;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Autowired
    public SocketDemoBackendProbe(DemoBackendProperties properties) {
        this(InetAddress::getAllByName, DemoBackendAddressPolicy::isPublic,
                (SSLSocketFactory) SSLSocketFactory.getDefault(), properties.healthTimeoutMs());
    }

    SocketDemoBackendProbe(Resolver resolver, Predicate<InetAddress> addressPolicy, SSLSocketFactory tlsFactory,
                           long timeoutMs) {
        this.resolver = resolver;
        this.addressPolicy = addressPolicy;
        this.tlsFactory = tlsFactory;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public Outcome probe(URI url) {
        Future<Outcome> attempt = executor.submit(() -> attempt(url));
        try {
            return attempt.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | ExecutionException e) {
            // 가상 스레드를 인터럽트하면 막혀 있던 소켓이 닫힌다
            attempt.cancel(true);
            return Outcome.OFFLINE;
        } catch (InterruptedException e) {
            attempt.cancel(true);
            Thread.currentThread().interrupt();
            return Outcome.UNVERIFIABLE;
        }
    }

    private Outcome attempt(URI url) throws IOException {
        InetAddress[] addresses;
        try {
            addresses = resolver.resolve(url.getHost());
        } catch (UnknownHostException e) {
            return Outcome.OFFLINE;
        }
        if (addresses.length == 0) {
            return Outcome.OFFLINE;
        }
        // 하나라도 내부 주소면 전부 거부한다. 공인 IP와 사설 IP를 섞어 응답하는 우회를 막는다
        for (InetAddress address : addresses) {
            if (!addressPolicy.test(address)) {
                return Outcome.UNVERIFIABLE;
            }
        }
        int port = url.getPort() == -1 ? 443 : url.getPort();
        for (int i = 0; i < Math.min(addresses.length, MAX_ADDRESSES); i++) {
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(addresses[i], port), (int) timeoutMs);
            } catch (IOException e) {
                socket.close();
                continue;
            }
            return exchange(socket, url, port);
        }
        return Outcome.OFFLINE;
    }

    private Outcome exchange(Socket socket, URI url, int port) throws IOException {
        String host = url.getHost();
        try (socket; SSLSocket tls = (SSLSocket) tlsFactory.createSocket(socket, host, port, true)) {
            socket.setSoTimeout((int) timeoutMs);
            SSLParameters parameters = tls.getSSLParameters();
            parameters.setEndpointIdentificationAlgorithm("HTTPS");
            parameters.setServerNames(List.of(new SNIHostName(host)));
            tls.setSSLParameters(parameters);
            tls.startHandshake();

            OutputStream out = tls.getOutputStream();
            out.write(request(url, host, port).getBytes(StandardCharsets.US_ASCII));
            out.flush();
            return SUCCESS.matcher(readStatusLine(tls.getInputStream())).matches() ? Outcome.ONLINE : Outcome.OFFLINE;
        } catch (IOException | IllegalArgumentException e) {
            return Outcome.OFFLINE;
        }
    }

    /** 주소는 검증기의 허용 문자만 통과했으므로 헤더 주입이 불가능하다. 쿠키나 인증 헤더는 보내지 않는다. */
    private static String request(URI url, String host, int port) {
        String path = url.getRawPath().isEmpty() ? "/" : url.getRawPath();
        String hostHeader = port == 443 ? host : host + ":" + port;
        return "GET " + path + " HTTP/1.1\r\nHost: " + hostHeader
                + "\r\nUser-Agent: DipSource-HealthCheck/1.0\r\nAccept: */*\r\nConnection: close\r\n\r\n";
    }

    private static String readStatusLine(InputStream in) throws IOException {
        byte[] line = new byte[MAX_STATUS_LINE_BYTES];
        int length = 0;
        int next;
        while (length < line.length && (next = in.read()) != -1 && next != '\n') {
            line[length++] = (byte) next;
        }
        if (length > 0 && line[length - 1] == '\r') {
            length--;
        }
        return new String(line, 0, length, StandardCharsets.US_ASCII);
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }
}
