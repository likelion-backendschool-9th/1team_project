package com.dipsource.dto.post;

import java.util.Arrays;
import java.util.stream.Collectors;

/** 본문 마크다운에서 요약·미리보기·읽는 시간을 만든다. */
final class PostText {

    /** 요약 최대 길이 */
    private static final int EXCERPT_LENGTH = 120;

    /** 1분에 읽는 글자 수 (한국어 기준 대략) */
    private static final int CHARS_PER_MINUTE = 500;

    /** 잠긴 유료 글에 보여 줄 문단 수 (프론트 잠금 화면과 같다) */
    private static final int PREVIEW_PARAGRAPHS = 3;

    private PostText() {
    }

    /** 마크다운 기호를 걷어낸 본문 앞부분 */
    static String excerpt(String content) {
        String plain = content
                .replaceAll("```[\\s\\S]*?```", " ")      // 코드 블록
                .replaceAll("!\\[[^]]*]\\([^)]*\\)", " ")  // 이미지
                .replaceAll("\\[([^]]*)]\\([^)]*\\)", "$1") // 링크는 글자만
                .replaceAll("(?m)^\\s{0,3}(#{1,6}|>|[-*+]|\\d+\\.)\\s+", "") // 제목·인용·목록 기호
                .replaceAll("[*_`~]", "")
                .replaceAll("\\s+", " ")
                .strip();
        return plain.length() <= EXCERPT_LENGTH ? plain : plain.substring(0, EXCERPT_LENGTH).strip() + "…";
    }

    /** 잠긴 유료 글: 앞 몇 문단만 */
    static String preview(String content) {
        return Arrays.stream(content.strip().split("\\n\\s*\\n"))
                .limit(PREVIEW_PARAGRAPHS)
                .collect(Collectors.joining("\n\n"));
    }

    static int readMinutes(String content) {
        return Math.max(1, (int) Math.ceil(content.length() / (double) CHARS_PER_MINUTE));
    }
}
