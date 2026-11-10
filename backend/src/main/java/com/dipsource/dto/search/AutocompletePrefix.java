package com.dipsource.dto.search;

import com.dipsource.global.common.LikePattern;

/**
 * 자동완성 접두어 조건. 입력한 글자로 시작하는 단어를 찾는다.
 *
 * <p>한글은 입력 중에 마지막 글자가 아직 조합 중이라 접두어가 맞지 않을 때가 있다.
 * "스프링"을 치면 입력창은 스 → 슾 → 스프 → 스플 → 스프리 → 스프링 순서로 바뀌는데, 슾·스플은 스프링의 접두어가 아니다.
 * 그래서 마지막 글자가 아직 바뀔 수 있으면, "앞부분(stem)으로 시작하고 그다음 글자가 이 범위 안"인 단어도 찾는다.
 * <ul>
 *   <li>받침 없음 스프리: 리에 받침이 붙을 수 있다 → "스프" 다음 글자가 리~맇(리, 릭, 린 … 링) → 스프링</li>
 *   <li>받침 있음 스플: 받침 ㄹ이 다음 글자 첫소리로 넘어갈 수 있다 → "스프" 다음 글자가 ㄹ로 시작(라~맇) → 스프링</li>
 *   <li>겹받침 닭(ㄺ): 뒤 자음 ㄱ이 넘어간다 → "달" 다음 글자가 ㄱ으로 시작(가~깋) → 달걀</li>
 *   <li>자음만 스ㅍ: "스" 다음 글자가 ㅍ으로 시작(파~핗)</li>
 * </ul>
 *
 * @param pattern     입력 그대로의 접두어 LIKE 패턴
 * @param composing   마지막 글자가 조합 중일 수 있어서 아래 범위로 한 번 더 찾을지
 * @param stemPattern 마지막 글자를 뺀(받침이 넘어가면 받침을 뗀) 앞부분의 접두어 LIKE 패턴
 * @param stemLength  앞부분의 글자 수. 그다음 글자를 범위로 비교한다
 * @param nextFrom    다음 글자 범위의 시작(예: 라)
 * @param nextTo      다음 글자 범위의 끝(예: 맇)
 */
public record AutocompletePrefix(
        String pattern,
        boolean composing,
        String stemPattern,
        int stemLength,
        String nextFrom,
        String nextTo
) {

    /** 한글 음절 '가'. 음절 = 가 + (첫소리 × 21 + 가운뎃소리) × 28 + 받침 */
    private static final int SYLLABLE_FIRST = 0xAC00;
    private static final int SYLLABLE_LAST = 0xD7A3;
    private static final int FINAL_COUNT = 28;
    /** 첫소리 하나에 속한 음절 수(21 × 28) */
    private static final int INITIAL_BLOCK = 588;

    /** 받침 번호별로, 다음 글자로 넘기고 남는 받침. 겹받침은 앞 자음이 남는다(ㄺ → ㄹ) */
    private static final int[] REMAINING_FINAL = {
            0, 0, 0, 1, 0, 4, 4, 0, 0, 8, 8, 8, 8, 8, 8, 8, 0, 0, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    /** 받침 번호별로, 다음 글자의 첫소리가 되는 자음 번호. 겹받침은 뒤 자음이 넘어간다(ㄺ → ㄱ) */
    private static final int[] MOVED_INITIAL = {
            -1, 0, 1, 9, 2, 12, 18, 3, 5, 0, 6, 7, 9, 16, 17, 18, 6, 7, 9, 9, 10, 11, 12, 14, 15, 16, 17, 18};

    /** 자음만 입력했을 때(ㄱ U+3131 ~ ㅎ U+314E) 첫소리 번호. 겹자음(ㄳ 등)은 첫소리가 될 수 없어서 -1 */
    private static final int JAMO_FIRST = 0x3131;
    private static final int[] JAMO_INITIAL = {
            0, 1, -1, 2, -1, -1, 3, 4, 5, -1, -1, -1, -1, -1, -1, -1, 6, 7, 8, -1, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};

    /** @param prefix 소문자로 바꾼, 비어 있지 않은 입력 */
    public static AutocompletePrefix of(String prefix) {
        String pattern = LikePattern.startsWith(prefix);
        int last = prefix.codePointBefore(prefix.length());
        String head = prefix.substring(0, prefix.length() - Character.charCount(last));

        if (last >= SYLLABLE_FIRST && last <= SYLLABLE_LAST) {
            int finalIndex = (last - SYLLABLE_FIRST) % FINAL_COUNT;
            if (finalIndex == 0) {
                // 받침이 붙을 수 있다: 같은 글자 + 받침 27가지
                return composing(pattern, head, last, last + FINAL_COUNT - 1);
            }
            // 받침이 다음 글자 첫소리로 넘어갈 수 있다
            int first = SYLLABLE_FIRST + MOVED_INITIAL[finalIndex] * INITIAL_BLOCK;
            String stem = head + Character.toString(last - finalIndex + REMAINING_FINAL[finalIndex]);
            return composing(pattern, stem, first, first + INITIAL_BLOCK - 1);
        }
        if (last >= JAMO_FIRST && last < JAMO_FIRST + JAMO_INITIAL.length && JAMO_INITIAL[last - JAMO_FIRST] >= 0) {
            // 자음만 입력했다: 그 자음으로 시작하는 글자
            int first = SYLLABLE_FIRST + JAMO_INITIAL[last - JAMO_FIRST] * INITIAL_BLOCK;
            return composing(pattern, head, first, first + INITIAL_BLOCK - 1);
        }
        return new AutocompletePrefix(pattern, false, pattern, 0, "", "");
    }

    private static AutocompletePrefix composing(String pattern, String stem, int nextFrom, int nextTo) {
        return new AutocompletePrefix(pattern, true, LikePattern.startsWith(stem), stem.codePointCount(0, stem.length()),
                Character.toString(nextFrom), Character.toString(nextTo));
    }
}
