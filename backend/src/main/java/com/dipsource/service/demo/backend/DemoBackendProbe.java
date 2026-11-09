package com.dipsource.service.demo.backend;

import java.net.URI;

/** 외부 백엔드에 실제 상태 확인 요청을 보낸다. */
public interface DemoBackendProbe {

    enum Outcome {
        /** 2xx 응답 */
        ONLINE,
        /** 연결 실패, 시간 초과, TLS 오류, 2xx가 아닌 응답(리다이렉트 포함) */
        OFFLINE,
        /** 허용하지 않는 IP로 해석되어 요청을 보내지 않음 */
        UNVERIFIABLE
    }

    Outcome probe(URI url);
}
