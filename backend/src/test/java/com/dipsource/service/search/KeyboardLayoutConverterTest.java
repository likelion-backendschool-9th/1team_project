package com.dipsource.service.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.dipsource.service.search.suggest.HangulJamo;
import com.dipsource.service.search.suggest.KeyboardLayoutConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KeyboardLayoutConverterTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            "네갸ㅜㅎ, spring",
            "ㅓㅁㅍㅁ, java",
            "ㄱㄷㅁㅊㅅ, react",
            "애ㅊㅏㄷㄱ, docker",
            "ㅜㄷㅌㅅ.ㅓㄴ, next.js",
            "ㅜㅐㅇㄷ.ㅓㄴ, node.js",
            "ㅔㅛ쇄ㅜ, python",
    })
    @DisplayName("한글 자판 입력을 같은 키의 영문으로 바꾼다")
    void hangulToLatin(String input, String expected) {
        assertThat(KeyboardLayoutConverter.toLatin(input)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({"tmvmfld, 스프링", "fldorxm, 리액트", "wkqk, 자바", "ehzj, 도커"})
    @DisplayName("영문 자판 입력을 같은 키의 한글로 조합한다")
    void latinToHangul(String input, String expected) {
        assertThat(KeyboardLayoutConverter.toHangul(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("한글이 없으면 영문 변환 대상이 아니고, 영문자 외 문자가 있으면 한글 변환 대상이 아니다")
    void notConvertible() {
        assertThat(KeyboardLayoutConverter.toLatin("spring")).isNull();
        assertThat(KeyboardLayoutConverter.toHangul("next.js")).isNull();
        assertThat(KeyboardLayoutConverter.toHangul("스프링")).isNull();
    }

    @Test
    @DisplayName("조합 중인 글자도 완성 단어의 자모 접두어가 된다")
    void composingIsJamoPrefix() {
        String word = HangulJamo.decompose("스프링");
        assertThat(word).startsWith(HangulJamo.decompose("스"));
        assertThat(word).startsWith(HangulJamo.decompose("슾"));
        assertThat(word).startsWith(HangulJamo.decompose("스프"));
        assertThat(word).startsWith(HangulJamo.decompose("스플"));
        assertThat(word).startsWith(HangulJamo.decompose("스프리"));
    }

    @Test
    @DisplayName("겹받침은 다음 글자로 넘어갈 수 있게 나눈다")
    void splitsDoubleFinal() {
        assertThat(HangulJamo.decompose("달걀")).startsWith(HangulJamo.decompose("닭"));
    }

    @Test
    @DisplayName("초성을 뽑고 자음만 입력했는지 판단한다")
    void initials() {
        assertThat(HangulJamo.initials("스프링")).isEqualTo("ㅅㅍㄹ");
        assertThat(HangulJamo.isConsonantsOnly("ㅅㅍㄹ")).isTrue();
        assertThat(HangulJamo.isConsonantsOnly("ㅅㅡ")).isFalse();
    }
}
