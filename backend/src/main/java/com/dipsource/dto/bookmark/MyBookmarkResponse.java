package com.dipsource.dto.bookmark;

import java.time.LocalDateTime;

/**
 * 내 북마크 한 건 (프론트 Bookmark). 피드·블로그 글·데모 북마크를 한 목록으로 보여 준다.
 *
 * @param id      북마크한 대상의 ID (피드·글·데모 ID). 종류가 달라 겹칠 수 있으니 type과 함께 구분한다
 * @param type    "feed" / "post" / "demo"
 * @param title   제목. 피드는 본문 앞 80자
 * @param author  작성자 닉네임
 * @param href    프론트에서 열 주소
 * @param savedAt 북마크한 시각
 */
public record MyBookmarkResponse(
        Long id,
        String type,
        String title,
        String author,
        String href,
        LocalDateTime savedAt
) {
}
