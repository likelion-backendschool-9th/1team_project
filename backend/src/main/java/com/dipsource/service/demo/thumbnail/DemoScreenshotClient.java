package com.dipsource.service.demo.thumbnail;

/** 격리된 원격 브라우저에서 URL을 찍어 이미지 바이트를 돌려준다. 서비스 서버에서 데모를 직접 실행하지 않는다. */
public interface DemoScreenshotClient {

    /** @throws RuntimeException 촬영 실패. 메시지는 실패 원인으로 저장되므로 비밀값을 넣지 않는다 */
    byte[] capture(String url);
}
