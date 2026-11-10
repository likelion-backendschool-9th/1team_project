package com.dipsource.service.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.service.search.query.ParsedQuery;
import com.dipsource.service.search.query.SearchQueryParser;
import com.dipsource.service.search.query.TextNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchQueryParserTest {

    private final SearchQueryParser parser = new SearchQueryParser();

    @Test
    @DisplayName("공백을 정리하고 소문자로 바꾸고 전각 문자를 접는다")
    void normalizes() {
        assertThat(parser.parse("  Ｓpring   BOOT ").phrase()).isEqualTo("spring boot");
    }

    @Test
    @DisplayName("조합형(NFD) 한글을 완성형으로 합치고, 호환 자모는 그대로 둔다")
    void normalizesHangul() {
        assertThat(TextNormalizer.normalize("스프링").length()).isEqualTo(3);
        assertThat(TextNormalizer.normalize("네갸ㅜㅎ")).isEqualTo("네갸ㅜㅎ");
    }

    @Test
    @DisplayName("일반 검색어와 해시태그를 분리한다. 뒤에 붙은 #은 해시태그가 아니다")
    void separatesHashtags() {
        ParsedQuery query = parser.parse("spring #Java C#");
        assertThat(query.keywords()).containsExactly("spring", "c#");
        assertThat(query.hashtags()).containsExactly("java");
    }

    @Test
    @DisplayName("해시태그는 표기 차이를 합치고 중복을 없애고 개수를 제한한다")
    void hashtagAbuse() {
        assertThat(parser.parse("#Spring #SPRING #spring").hashtags()).containsExactly("spring");
        assertThat(parser.parse("#spring-boot #spring_boot #SpringBoot").hashtags()).containsExactly("springboot");
        assertThat(parser.parse("#a1 #a2 #a3 #a4 #a5 #a6 #a7").hashtags()).hasSize(5);
    }

    @Test
    @DisplayName("#만 입력하면 검색어가 없는 것으로 본다")
    void bareHash() {
        assertThatThrownBy(() -> parser.parse("#")).isInstanceOf(BusinessException.class);
    }
}
