package com.dipsource.search.eval;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** 0단계 기준선: 기존 PostgreSQL LIKE 검색. */
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret-0000",
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test"
})
class PostgresBaselineEvalTest extends AbstractSearchEval {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Override
    String profile() {
        return "0-postgres-like";
    }

    /** 튜닝에 쓰지 않은 신규 평가셋을 PostgreSQL 경로로 잰다 */
    @org.junit.jupiter.api.Nested
    class Holdout extends AbstractSearchEval {
        @Override
        String profile() {
            return "holdout-postgres";
        }

        @Override
        String corpusResource() {
            return "search-eval/holdout-corpus.json";
        }

        @Override
        String queriesResource() {
            return "search-eval/holdout-queries.json";
        }
    }

    @org.junit.jupiter.api.Nested
    class SingleChar extends AbstractSearchEval {
        @Override
        String profile() {
            return "singlechar-postgres";
        }

        @Override
        String corpusResource() {
            return "search-eval/holdout-corpus.json";
        }

        @Override
        String queriesResource() {
            return "search-eval/single-char-queries.json";
        }
    }
}
