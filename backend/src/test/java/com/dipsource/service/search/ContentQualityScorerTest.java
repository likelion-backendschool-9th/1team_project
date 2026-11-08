package com.dipsource.service.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.dipsource.global.config.SearchProperties;
import com.dipsource.service.search.index.ContentQualityScorer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContentQualityScorerTest {

    private final ContentQualityScorer scorer = new ContentQualityScorer(new SearchProperties());

    @Test
    @DisplayName("같은 단어 100회 반복은 감점한다")
    void wordStuffing() {
        assertThat(scorer.score("spring ".repeat(100))).isLessThan(1.0f);
    }

    @Test
    @DisplayName("같은 문장 반복은 감점한다")
    void sentenceRepetition() {
        assertThat(scorer.score("spring 최고의 프레임워크 추천 ".repeat(30) + "끝.")).isLessThan(1.0f);
    }

    @Test
    @DisplayName("기술 용어를 자주 쓴 정상 문서는 감점하지 않는다")
    void legitimateTechnicalDocument() {
        String text = """
                Spring 의 빈 생명주기를 정리한다. Spring 컨테이너는 빈을 생성하고 의존성을 주입한다.
                Spring 은 초기화 콜백을 호출한다. 애플리케이션 컨텍스트가 닫히면 Spring 은 소멸 콜백을 호출한다.
                스코프에 따라 Spring 이 관리하는 범위가 달라진다. 프로토타입 스코프는 초기화까지만 Spring 이 맡는다.
                테스트에서는 Spring 컨텍스트 캐시를 활용해 시간을 줄일 수 있다. 설정 클래스를 나누면 이해하기 쉽다.
                """;
        assertThat(scorer.score(text)).isEqualTo(1.0f);
    }

    @Test
    @DisplayName("같은 코드 줄이 반복된 문서만으로는 감점하지 않는다(신호 하나)")
    void repeatedCodeLinesAlone() {
        StringBuilder text = new StringBuilder("배열을 초기화하는 예제다 각 인덱스에 다른 값을 넣는 과정을 줄마다 보여 준다\n");
        for (int i = 0; i < 40; i++) {
            text.append("buffer put value").append(i).append(" offset").append(i).append(" length").append(i).append('\n');
        }
        assertThat(scorer.score(text.toString())).isEqualTo(1.0f);
    }

    @Test
    @DisplayName("짧은 글과 빈 글은 판단하지 않는다")
    void shortText() {
        assertThat(scorer.score("spring spring spring")).isEqualTo(1.0f);
        assertThat(scorer.score(null)).isEqualTo(1.0f);
    }
}
