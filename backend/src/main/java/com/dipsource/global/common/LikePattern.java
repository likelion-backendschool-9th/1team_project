package com.dipsource.global.common;

/**
 * LIKE 부분 일치 검색에 넣을 패턴을 만든다. 쿼리에는 반드시 ESCAPE '\' 를 함께 쓴다.
 * 예: "100%" → "%100\%%" (앞뒤 %는 부분 일치, 가운데 \%는 진짜 % 글자)
 */
public final class LikePattern {

    /** 이스케이프 문자. 쿼리의 ESCAPE '\' 와 같아야 한다. */
    public static final char ESCAPE = '\\';

    private LikePattern() {
    }

    /** 검색어가 어디든 들어 있으면 일치하는 패턴 */
    public static String contains(String keyword) {
        return "%" + escape(keyword) + "%";
    }

    /** 검색어로 시작하면 일치하는 패턴 */
    public static String startsWith(String keyword) {
        return escape(keyword) + "%";
    }

    /** %, _, \ 를 글자 그대로 비교하도록 앞에 \ 를 붙인다. */
    static String escape(String keyword) {
        StringBuilder escaped = new StringBuilder(keyword.length());
        for (char c : keyword.toCharArray()) {
            if (c == ESCAPE || c == '%' || c == '_') {
                escaped.append(ESCAPE);
            }
            escaped.append(c);
        }
        return escaped.toString();
    }
}
