package com.dipsource.service.demo.storage;

/** R2 객체 키 규칙. Worker(workers/demo-runner)와 같은 규칙을 쓴다. */
public final class DemoStorageKeys {

    private DemoStorageKeys() {
    }

    public static String demoPrefix(Long demoId) {
        return "demos/" + demoId + "/";
    }

    /** Worker가 읽는 공개 포인터. 이 객체가 가리키는 버전만 외부에 나간다 */
    public static String publication(Long demoId) {
        return demoPrefix(demoId) + "publication.json";
    }

    public static String versionPrefix(Long demoId, Long versionId) {
        return "demos/" + demoId + "/versions/" + versionId;
    }

    public static String filesPrefix(String storagePrefix) {
        return storagePrefix + "/files/";
    }
}
