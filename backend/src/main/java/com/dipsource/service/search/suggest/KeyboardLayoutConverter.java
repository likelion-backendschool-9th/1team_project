package com.dipsource.service.search.suggest;

import java.util.HashMap;
import java.util.Map;

/**
 * 두벌식 자판과 QWERTY의 키 위치 대응. 발음 변환이 아니라 "같은 키를 눌렀을 때 나오는 글자"로 바꾼다.
 * 변환 결과가 맞는지는 판단하지 않는다. 사전에 있는 후보만 쓰는 것은 호출하는 쪽의 책임이다.
 */
public final class KeyboardLayoutConverter {

    private static final Map<String, String> JAMO_TO_KEY = new HashMap<>();
    private static final Map<Character, String> KEY_TO_JAMO = new HashMap<>();

    private static final String[][] KEYS = {
            {"ㅂ", "q"}, {"ㅈ", "w"}, {"ㄷ", "e"}, {"ㄱ", "r"}, {"ㅅ", "t"}, {"ㅛ", "y"}, {"ㅕ", "u"}, {"ㅑ", "i"}, {"ㅐ", "o"}, {"ㅔ", "p"},
            {"ㅁ", "a"}, {"ㄴ", "s"}, {"ㅇ", "d"}, {"ㄹ", "f"}, {"ㅎ", "g"}, {"ㅗ", "h"}, {"ㅓ", "j"}, {"ㅏ", "k"}, {"ㅣ", "l"},
            {"ㅋ", "z"}, {"ㅌ", "x"}, {"ㅊ", "c"}, {"ㅍ", "v"}, {"ㅠ", "b"}, {"ㅜ", "n"}, {"ㅡ", "m"},
            {"ㅃ", "Q"}, {"ㅉ", "W"}, {"ㄸ", "E"}, {"ㄲ", "R"}, {"ㅆ", "T"}, {"ㅒ", "O"}, {"ㅖ", "P"}};

    /** 한 글자로 합쳐지는 자모 입력의 키 조합 */
    private static final String[][] COMPOUND_JAMO = {
            {"ㅘ", "hk"}, {"ㅙ", "ho"}, {"ㅚ", "hl"}, {"ㅝ", "nj"}, {"ㅞ", "np"}, {"ㅟ", "nl"}, {"ㅢ", "ml"},
            {"ㄳ", "rt"}, {"ㄵ", "sw"}, {"ㄶ", "sg"}, {"ㄺ", "fr"}, {"ㄻ", "fa"}, {"ㄼ", "fq"}, {"ㄽ", "ft"}, {"ㄾ", "fx"}, {"ㄿ", "fv"}, {"ㅀ", "fg"}, {"ㅄ", "qt"}};

    static {
        for (String[] pair : KEYS) {
            JAMO_TO_KEY.put(pair[0], pair[1]);
            KEY_TO_JAMO.put(pair[1].charAt(0), pair[0]);
        }
        for (String[] pair : COMPOUND_JAMO) {
            JAMO_TO_KEY.put(pair[0], pair[1]);
        }
    }

    private KeyboardLayoutConverter() {
    }

    /** 한글 입력을 같은 키의 영문으로. 한글이 없으면 null */
    public static String toLatin(String text) {
        if (text.codePoints().noneMatch(cp -> HangulJamo.isSyllable(cp) || HangulJamo.isJamo(cp))) {
            return null;
        }
        StringBuilder latin = new StringBuilder();
        HangulJamo.decompose(text).codePoints().forEach(codePoint -> {
            String key = JAMO_TO_KEY.get(Character.toString(codePoint));
            if (key != null) {
                latin.append(key);
            } else {
                latin.appendCodePoint(codePoint);
            }
        });
        return latin.toString().toLowerCase(java.util.Locale.ROOT);
    }

    /** 영문 입력을 같은 키의 한글로 조합한다. 영문자만으로 된 입력이 아니면 null */
    public static String toHangul(String text) {
        if (text.isEmpty() || !text.chars().allMatch(ch -> ch < 128 && Character.isLetter(ch))) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        HangulComposer composer = new HangulComposer(result);
        for (char key : text.toCharArray()) {
            String jamo = KEY_TO_JAMO.get(key);
            if (jamo == null) {
                jamo = KEY_TO_JAMO.get(Character.toLowerCase(key));
            }
            if (jamo == null) {
                return null;
            }
            composer.feed(jamo);
        }
        composer.flush();
        return result.toString();
    }

    /** 자모를 순서대로 받아 음절로 조합한다(첫소리 → 가운뎃소리 → 받침, 다음 모음이 오면 받침을 넘긴다). */
    private static final class HangulComposer {
        private final StringBuilder out;
        private String initial;
        private String vowel;
        private String finalJamo;

        HangulComposer(StringBuilder out) {
            this.out = out;
        }

        void feed(String jamo) {
            if (isVowel(jamo)) {
                feedVowel(jamo);
            } else {
                feedConsonant(jamo);
            }
        }

        private void feedVowel(String jamo) {
            if (vowel == null) {
                if (initial == null) {
                    out.append(jamo);
                } else {
                    vowel = jamo;
                }
                return;
            }
            if (finalJamo != null) {
                // 받침이 다음 음절의 첫소리로 넘어간다
                String moved = finalJamo;
                finalJamo = null;
                flush();
                initial = moved;
                vowel = jamo;
                return;
            }
            String combined = combineVowel(vowel, jamo);
            if (combined != null) {
                vowel = combined;
            } else {
                flush();
                out.append(jamo);
            }
        }

        private void feedConsonant(String jamo) {
            if (initial == null) {
                initial = jamo;
            } else if (vowel == null) {
                out.append(initial);
                initial = jamo;
            } else if (finalJamo == null && indexOf(HangulJamo.FINALS, jamo) > 0) {
                finalJamo = jamo;
            } else {
                flush();
                initial = jamo;
            }
        }

        void flush() {
            if (initial != null && vowel != null) {
                int i = indexOf(HangulJamo.INITIALS, initial);
                int v = indexOf(VOWELS_COMBINED, vowel);
                int f = finalJamo == null ? 0 : indexOf(HangulJamo.FINALS, finalJamo);
                out.appendCodePoint(0xAC00 + (i * 21 + v) * 28 + f);
            } else if (initial != null) {
                out.append(initial);
            }
            initial = null;
            vowel = null;
            finalJamo = null;
        }

        private static final String[] VOWELS_COMBINED = {"ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"};

        private static boolean isVowel(String jamo) {
            return indexOf(VOWELS_COMBINED, jamo) >= 0;
        }

        private static String combineVowel(String first, String second) {
            return switch (first + second) {
                case "ㅗㅏ" -> "ㅘ";
                case "ㅗㅐ" -> "ㅙ";
                case "ㅗㅣ" -> "ㅚ";
                case "ㅜㅓ" -> "ㅝ";
                case "ㅜㅔ" -> "ㅞ";
                case "ㅜㅣ" -> "ㅟ";
                case "ㅡㅣ" -> "ㅢ";
                default -> null;
            };
        }

        private static int indexOf(String[] values, String value) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    return i;
                }
            }
            return -1;
        }
    }
}
