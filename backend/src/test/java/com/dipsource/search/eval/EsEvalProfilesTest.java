package com.dipsource.search.eval;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/** 단계별 평가 프로필. 같은 말뭉치와 정답으로 설정만 바꿔 비교한다. */
final class EsEvalProfilesTest {

    private static final String COMMON_JWT = "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000";
    private static final String GITHUB_ID = "spring.security.oauth2.client.registration.github.client-id=test";
    private static final String GITHUB_SECRET = "spring.security.oauth2.client.registration.github.client-secret=test";
    private static final String ES_ON = "search.elasticsearch.enabled=true";
    private static final String ENGINE = "search.engine=elasticsearch";
    private static final String NO_FALLBACK = "search.fallback-to-postgres=false";

    private EsEvalProfilesTest() {
    }

    /** 1단계: 순수 BM25. 필드 가중치·언어 분석·기술 용어·재정렬 없음 */
    @AutoConfigureMockMvc
    @SpringBootTest(properties = {COMMON_JWT, GITHUB_ID, GITHUB_SECRET, ES_ON, ENGINE, NO_FALLBACK,
            "search.ranking.flat=true",
            "search.analysis.language-fields=false",
            "search.analysis.tech-terms=false",
            "search.analysis.synonyms=false"})
    static class Stage1Bm25Eval extends AbstractEsEval {
        @Override
        String profile() {
            return "1-es-bm25";
        }
    }
}
