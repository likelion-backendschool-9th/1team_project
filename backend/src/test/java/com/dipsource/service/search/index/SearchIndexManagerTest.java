package com.dipsource.service.search.index;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchIndexManagerTest {

    @Test
    @DisplayName("서버·인덱스 상태로 인한 실패는 재시도 횟수를 쓰지 않는 쪽으로 분류한다")
    void unavailable() {
        assertThat(SearchIndexManager.isUnavailable(503, "unavailable_shards_exception")).isTrue();
        assertThat(SearchIndexManager.isUnavailable(429, "es_rejected_execution_exception")).isTrue();
        assertThat(SearchIndexManager.isUnavailable(400, "index_closed_exception")).isTrue();
    }

    @Test
    @DisplayName("문서 자체가 거절된 실패는 재시도 횟수를 쓴다")
    void documentRejected() {
        assertThat(SearchIndexManager.isUnavailable(400, "mapper_parsing_exception")).isFalse();
        assertThat(SearchIndexManager.isUnavailable(400, "strict_dynamic_mapping_exception")).isFalse();
    }
}
