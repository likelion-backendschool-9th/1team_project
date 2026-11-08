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

    /** 2단계: 필드 가중치 + 구문·정확 일치 + 인기도 재정렬 + 스팸 감점. 다국어·기술 용어는 아직 끔 */
    @AutoConfigureMockMvc
    @SpringBootTest(properties = {COMMON_JWT, GITHUB_ID, GITHUB_SECRET, ES_ON, ENGINE, NO_FALLBACK,
            "search.analysis.language-fields=false",
            "search.analysis.tech-terms=false",
            "search.analysis.synonyms=false"})
    static class Stage2RankedEval extends AbstractEsEval {
        @Override
        String profile() {
            return "2-es-ranked";
        }
    }

    /** 2단계 실험: 블로그 해시태그 가중치를 요구사항 초기값(4)으로 올렸을 때 */
    @AutoConfigureMockMvc
    @SpringBootTest(properties = {COMMON_JWT, GITHUB_ID, GITHUB_SECRET, ES_ON, ENGINE, NO_FALLBACK,
            "search.analysis.language-fields=false",
            "search.analysis.tech-terms=false",
            "search.analysis.synonyms=false",
            "search.ranking.post.keyword-boost=4"})
    static class Stage2TagBoost4Eval extends AbstractEsEval {
        @Override
        String profile() {
            return "2x-post-tag-boost-4";
        }
    }

    /** 2단계 실험: 인기도를 끄면(최대 가산 0) 어떻게 되는지 */
    @AutoConfigureMockMvc
    @SpringBootTest(properties = {COMMON_JWT, GITHUB_ID, GITHUB_SECRET, ES_ON, ENGINE, NO_FALLBACK,
            "search.analysis.language-fields=false",
            "search.analysis.tech-terms=false",
            "search.analysis.synonyms=false",
            "search.ranking.post.popularity.max-boost=0",
            "search.ranking.demo.popularity.max-boost=0",
            "search.ranking.feed.popularity.max-boost=0",
            "search.ranking.user.popularity.max-boost=0"})
    static class Stage2NoPopularityEval extends AbstractEsEval {
        @Override
        String profile() {
            return "2x-no-popularity";
        }
    }
}
