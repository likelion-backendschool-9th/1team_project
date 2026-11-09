package com.dipsource.service.search.suggest;

/** 한글 음절을 호환 자모로 분해한다. 입력 중 조합 상태(슾, 스플)도 분해하면 완성 단어의 접두어가 된다. */
public final class HangulJamo {

    private static final int SYLLABLE_FIRST = 0xAC00;
    private static final int SYLLABLE_LAST = 0xD7A3;
    private static final int VOWEL_COUNT = 21;
    private static final int FINAL_COUNT = 28;

    static final String[] INITIALS = {"ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};
    /** 겹모음은 입력 순서대로 나눈다: ㅘ → ㅗㅏ */
    static final String[] VOWELS = {"ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅗㅏ", "ㅗㅐ", "ㅗㅣ", "ㅛ", "ㅜ", "ㅜㅓ", "ㅜㅔ", "ㅜㅣ", "ㅠ", "ㅡ", "ㅡㅣ", "ㅣ"};
    /** 겹받침도 나눈다: ㄺ → ㄹㄱ. 그래야 닭이 달걀의 접두어가 된다 */
    static final String[] FINALS = {"", "ㄱ", "ㄲ", "ㄱㅅ", "ㄴ", "ㄴㅈ", "ㄴㅎ", "ㄷ", "ㄹ", "ㄹㄱ", "ㄹㅁ", "ㄹㅂ", "ㄹㅅ", "ㄹㅌ", "ㄹㅍ", "ㄹㅎ", "ㅁ", "ㅂ", "ㅂㅅ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};

    private HangulJamo() {
    }

    public static boolean isSyllable(int codePoint) {
        return codePoint >= SYLLABLE_FIRST && codePoint <= SYLLABLE_LAST;
    }

    public static boolean isJamo(int codePoint) {
        return codePoint >= 0x3131 && codePoint <= 0x3163;
    }

    /** 음절만 자모로 풀고 나머지 글자는 그대로 둔다 */
    public static String decompose(String text) {
        StringBuilder jamo = new StringBuilder(text.length() * 3);
        text.codePoints().forEach(codePoint -> {
            if (isSyllable(codePoint)) {
                int index = codePoint - SYLLABLE_FIRST;
                jamo.append(INITIALS[index / (VOWEL_COUNT * FINAL_COUNT)]);
                jamo.append(VOWELS[(index / FINAL_COUNT) % VOWEL_COUNT]);
                jamo.append(FINALS[index % FINAL_COUNT]);
            } else {
                jamo.appendCodePoint(codePoint);
            }
        });
        return jamo.toString();
    }

    /** 초성만 뽑는다. 한글 음절이 없으면 빈 문자열 */
    public static String initials(String text) {
        StringBuilder result = new StringBuilder();
        text.codePoints().filter(HangulJamo::isSyllable)
                .forEach(codePoint -> result.append(INITIALS[(codePoint - SYLLABLE_FIRST) / (VOWEL_COUNT * FINAL_COUNT)]));
        return result.toString();
    }

    /** 모두 자음(초성 입력)인지 */
    public static boolean isConsonantsOnly(String text) {
        return !text.isEmpty() && text.codePoints().allMatch(cp -> isJamo(cp) && cp <= 0x314E);
    }
}
