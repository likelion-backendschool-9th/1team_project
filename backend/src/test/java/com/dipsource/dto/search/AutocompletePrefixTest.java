package com.dipsource.dto.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** 자동완성 접두어 조건. 한글 조합 중인 마지막 글자(받침이 붙거나 다음 글자로 넘어가는 경우)의 범위 계산을 검증한다. */
class AutocompletePrefixTest {

    @ParameterizedTest(name = "{0} → {1} 다음 글자 {2}~{3}")
    @CsvSource({
            "스프리, 스프, 리, 맇", // 받침 없음 → 같은 글자에 받침이 붙을 수 있음(리~맇)
            "스, '', 스, 슿",      // 첫 글자도 같다
            "스플, 스프, 라, 맇",   // 홑받침 ㄹ → 다음 글자 ㄹ
            "슾, 스, 파, 핗",      // 홑받침 ㅍ → 다음 글자 ㅍ
            "닭, 달, 가, 깋",      // 겹받침 ㄺ → ㄹ은 남고 ㄱ이 넘어감
            "없, 업, 사, 싷",      // 겹받침 ㅄ → ㅂ은 남고 ㅅ이 넘어감
            "있, 이, 싸, 앃",      // 쌍받침 ㅆ은 통째로 넘어감
            "ㅅ, '', 사, 싷",      // 자음만 입력
            "스ㅍ, 스, 파, 핗"      // 앞 글자 뒤에 자음만 입력
    })
    @DisplayName("마지막 한글 글자가 조합 중일 수 있으면 다음 글자 범위로 한 번 더 찾는다")
    void composing(String input, String stem, String from, String to) {
        AutocompletePrefix prefix = AutocompletePrefix.of(input);

        assertThat(prefix.pattern()).isEqualTo(input + "%");
        assertThat(prefix.composing()).isTrue();
        assertThat(prefix.stemPattern()).isEqualTo(stem + "%");
        assertThat(prefix.stemLength()).isEqualTo(stem.length());
        assertThat(prefix.nextFrom()).isEqualTo(from);
        assertThat(prefix.nextTo()).isEqualTo(to);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"spr", "스프a", "ㄳ", "ㅏ", "c#", "2"})
    @DisplayName("마지막 글자가 한글 음절이나 첫소리 자음이 아니면 입력 그대로만 찾는다")
    void notComposing(String input) {
        AutocompletePrefix prefix = AutocompletePrefix.of(input);

        assertThat(prefix.composing()).isFalse();
        assertThat(prefix.pattern()).isEqualTo(input + "%");
    }

    @Test
    @DisplayName("LIKE 특수문자(%, _)는 글자 그대로 비교하도록 이스케이프한다")
    void escapesLikeCharacters() {
        assertThat(AutocompletePrefix.of("100%").pattern()).isEqualTo("100\\%%");
        assertThat(AutocompletePrefix.of("a_b").pattern()).isEqualTo("a\\_b%");
    }

    @Test
    @DisplayName("앞부분 글자 수는 코드 포인트로 센다 (DB substr과 같은 기준)")
    void stemLengthInCodePoints() {
        AutocompletePrefix prefix = AutocompletePrefix.of("😀스플");

        assertThat(prefix.stemLength()).isEqualTo(3);
    }
}
