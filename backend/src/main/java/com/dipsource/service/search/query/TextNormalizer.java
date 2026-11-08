package com.dipsource.service.search.query;

import java.text.Normalizer;
import java.util.Locale;

/** 검색어와 색인 값에 똑같이 적용하는 정규화. 어느 쪽이든 이 클래스를 거쳐야 정확 일치가 맞는다. */
public final class TextNormalizer {

    private static final int FULLWIDTH_FIRST = 0xFF01;
    private static final int FULLWIDTH_LAST = 0xFF5E;
    private static final int FULLWIDTH_OFFSET = 0xFEE0;
    private static final int IDEOGRAPHIC_SPACE = 0x3000;

    private TextNormalizer() {
    }

    /**
     * NFC로 합치고 전각 ASCII를 반각으로 바꾼 뒤, 공백을 한 칸으로 줄이고 소문자로 만든다.
     * NFKC를 쓰지 않는 이유: NFKC는 호환 자모(ㄴ, ㅜ)를 첫가끝 자모로 바꿔서 자모 입력 처리가 깨진다.
     */
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder folded = new StringBuilder(text.length());
        text.codePoints().forEach(codePoint -> folded.appendCodePoint(foldFullwidth(codePoint)));
        return Normalizer.normalize(folded, Normalizer.Form.NFC)
                .replaceAll("\\s+", " ")
                .strip()
                .toLowerCase(Locale.ROOT);
    }

    /** 태그 비교 키. "Spring Boot", "spring-boot", "spring_boot"를 같은 태그로 본다 */
    public static String tagKey(String tag) {
        return normalize(tag).replaceAll("[\\s_-]+", "");
    }

    public static int codePointLength(String text) {
        return text.codePointCount(0, text.length());
    }

    private static int foldFullwidth(int codePoint) {
        if (codePoint >= FULLWIDTH_FIRST && codePoint <= FULLWIDTH_LAST) {
            return codePoint - FULLWIDTH_OFFSET;
        }
        return codePoint == IDEOGRAPHIC_SPACE ? ' ' : codePoint;
    }
}
