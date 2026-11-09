package com.dipsource.dto.admin;

/**
 * 이상 거래 한 건 (프론트 Suspicious). 저장하지 않고 조회할 때마다 계산한다.
 *
 * @param id         목록 안에서만 쓰는 번호
 * @param userId     의심 회원 (계정 정지 대상)
 * @param pattern    무엇이 이상한지 한 줄 설명
 * @param count      발생 횟수
 * @param amount     관련 금액(원)
 * @param detectedAt 마지막으로 발생한 시각
 * @param level      "high" / "medium"
 */
public record AdminSuspiciousResponse(
        Long id,
        Long userId,
        String user,
        String pattern,
        int count,
        long amount,
        String detectedAt,
        String level
) {
}
