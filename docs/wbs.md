# 찍먹 WBS (Work Breakdown Structure)

> **기능 범위·작업/API 초안:** 아래 목록은 구현 계약이 아닙니다. 서비스 방향은 [기획서.md](기획서.md), 문서 우선순위는 [README.md](README.md)를 확인하세요.

> 모든 API 기본 경로: `/api/v1/`

---

## 1. 회원

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 1.1 | 소셜 로그인 | /login | POST | auth/login/{provider} | github / kakao / google |
| 1.2 | 로그아웃 | (레이아웃) | POST | auth/logout | |
| 1.3 | 내 정보 조회 | /my | GET | users/me | |
| 1.4 | 프로필 수정 | /my/edit | PUT | users/me | |
| 1.5 | 회원 탈퇴 | /my/edit | DELETE | users/me | |
| 1.6 | 설정 조회 | /settings | GET | users/me/settings | |
| 1.7 | 설정 수정 | /settings | PUT | users/me/settings | |
| 1.8 | 알림 목록 | /notifications | GET | notifications | |
| 1.9 | 알림 읽음 처리 | /notifications | PUT | notifications/{id}/read | |

---

## 2. 미니홈피

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 2.1 | 미니홈피 조회 | /home/[userId] | GET | mini-home/{userId} | |
| 2.2 | 미니홈피 수정 | /home/[userId]/edit | PUT | mini-home/{userId} | 본인만. 스킨/설정 등 |
| 2.3 | 대표 데모 설정 | /home/[userId]/edit | PUT | mini-home/{userId}/featured-demo | 본인만 |
| 2.4 | 방문자 통계 | /home/[userId] | GET | mini-home/{userId}/visitors | TODAY/TOTAL |
| 2.5 | 방문 카운트 증가 | /home/[userId] | POST | mini-home/{userId}/visit | 같은 사람 하루 1회 |
| 2.6 | 방명록 목록 | /home/[userId]/guestbook | GET | mini-home/{userId}/guestbook | 비밀글은 주인만 |
| 2.7 | 방명록 작성 | /home/[userId]/guestbook | POST | mini-home/{userId}/guestbook | |
| 2.8 | 일촌평 목록 | /home/[userId] | GET | mini-home/{userId}/comments | |
| 2.9 | 일촌평 작성 | /home/[userId] | POST | mini-home/{userId}/comments | |
| 2.10 | 미니홈피 내 데모 목록 | /home/[userId]/demos | GET | mini-home/{userId}/demos | |
| 2.11 | 파도타기 | - | GET | mini-home/random | 랜덤 미니홈피 이동 |

---

## 3. 깃허브 연동

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 3.1 | 깃허브 잔디 조회 | /home/[userId] | GET | users/{userId}/github/contributions | |
| 3.2 | 깃허브 대표 레포 | /home/[userId] | GET | users/{userId}/github/repos | |
| 3.3 | 언어 사용 통계 | /home/[userId] | GET | users/{userId}/github/languages | |

---

## 4. 데모

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 4.1 | 데모 업로드 | /demos/upload | POST | demos | zip, 확장자/용량 검사 |
| 4.2 | 데모 파일 검사 | - | POST | 외부 API (VirusTotal) | |
| 4.3 | 데모 파일 검사 진행률 | - | GET | 외부 API (VirusTotal) | |
| 4.4 | 데모 목록 | /demos | GET | demos?tag=&sort= | 태그 필터, 정렬 |
| 4.5 | 데모 상세 조회 | /demos/[id] | GET | demos/{id} | |
| 4.6 | 데모 체험 카운트 | /demos/[id] | POST | demos/{id}/play | |
| 4.7 | 데모 좋아요 토글 | /demos/[id] | POST | demos/{id}/like | |
| 4.8 | 데모 북마크 토글 | /demos/[id] | POST | demos/{id}/bookmark | |
| 4.9 | 내 북마크 목록 | /my/bookmarks | GET | users/me/bookmarks | |
| 4.10 | 데모 삭제 | /demos/[id] | DELETE | demos/{id} | 본인만 |

---

## 5. 데모 댓글

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 5.1 | 댓글 목록 | /demos/[id] | GET | demos/{demoId}/comments | |
| 5.2 | 댓글 작성 | /demos/[id] | POST | demos/{demoId}/comments | |
| 5.3 | 댓글 수정 | /demos/[id] | PUT | comments/{commentId} | |
| 5.4 | 댓글 삭제 | /demos/[id] | DELETE | comments/{commentId} | |

---

## 6. 블로그

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 6.1 | 글 작성 | /blog/write | POST | posts | 마크다운, 데모 연결 가능 |
| 6.2 | 글 조회 | /blog/[id] | GET | posts/{id} | 유료 글은 구독자/결제자만 |
| 6.3 | 사용자 블로그 글 목록 | /home/[userId]/blog | GET | users/{userId}/posts | |
| 6.4 | 글 수정 | /blog/[id]/edit | PUT | posts/{id} | 본인만 |
| 6.5 | 글 삭제 | /blog/[id] | DELETE | posts/{id} | 본인만 |
| 6.6 | 댓글 작성 | /blog/[id] | POST | posts/{id}/comments | |
| 6.7 | 댓글 수정 | /blog/[id] | PUT | comments/{commentId} | |
| 6.8 | 댓글 삭제 | /blog/[id] | DELETE | comments/{commentId} | |
| 6.9 | 글 신고 | /blog/[id] | POST | reports | body에 targetType, targetId |

---

## 7. 피드

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 7.1 | 피드 작성 | /feeds | POST | feeds | 짧은 글 + 투표(선택) |
| 7.2 | 피드 목록 (팔로잉) | / 또는 /feeds | GET | feeds?type=following | |
| 7.3 | 피드 목록 (추천) | / 또는 /feeds | GET | feeds?type=recommended | |
| 7.4 | 좋아요 | /feeds/[id] | POST | feeds/{feedId}/like | |
| 7.5 | 좋아요 취소 | /feeds/[id] | DELETE | feeds/{feedId}/like | |
| 7.6 | 리포스트 | /feeds/[id] | POST | feeds/{feedId}/repost | |
| 7.7 | 리포스트 취소 | /feeds/[id] | DELETE | feeds/{feedId}/repost | |
| 7.8 | 투표 | /feeds/[id] | POST | feeds/{feedId}/vote | |
| 7.9 | 댓글 목록 | /feeds/[id] | GET | feeds/{feedId}/comments | |
| 7.10 | 댓글 작성 | /feeds/[id] | POST | feeds/{feedId}/comments | |
| 7.11 | 댓글 수정 | /feeds/[id] | PUT | comments/{commentId} | |
| 7.12 | 댓글 삭제 | /feeds/[id] | DELETE | comments/{commentId} | |
| 7.13 | 피드 신고 | /feeds/[id] | POST | reports | body에 targetType, targetId |

---

## 8. 소셜

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 8.1 | 팔로우 | (미니홈피/피드 등) | POST | follows/{userId} | |
| 8.2 | 언팔로우 | (미니홈피/피드 등) | DELETE | follows/{userId} | |
| 8.3 | 팔로잉 목록 | /my/followings | GET | follows/followings | |
| 8.4 | 팔로워 목록 | /my/followers | GET | follows/followers | |
| 8.5 | DM 보내기 | /dm/[userId] | WEBSOCKET | /ws (STOMP) | 실시간 |
| 8.6 | DM 대화 내역 | /dm/[userId] | GET | dm/{userId} | |
| 8.7 | 사용자 차단 | (프로필 등) | POST | blocks/{userId} | |
| 8.8 | 사용자 차단 해제 | /my/blocks | DELETE | blocks/{userId} | |
| 8.9 | 차단 목록 | /my/blocks | GET | users/me/blocks | |
| 8.10 | 카톡 공유 | - | - | 카카오 SDK | 클라이언트에서 처리 |

---

## 9. 결제 / 구독

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 9.1 | 구독 | (미니홈피) | POST | subscriptions/{userId} | 칩 차감 |
| 9.2 | 구독 해지 | /my/subscriptions | DELETE | subscriptions/{userId} | |
| 9.3 | 내 구독 목록 | /my/subscriptions | GET | subscriptions/me | |
| 9.4 | 칩 충전 | /my/coins | POST | coins/purchase | 경로는 API 설계 확정 시 조정 |
| 9.5 | 칩 사용 (단건 결제) | - | POST | coins/spend | 유료 콘텐츠 개별 결제 |
| 9.6 | 잔액 조회 | /my/coins | GET | coins/balance | |

---

## 10. 검색 / 순위

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 10.1 | 통합 검색 | /search?q= | GET | search?q= | 문장형 지원 |
| 10.2 | 인기 검색어 | /search | GET | search/popular | |
| 10.3 | 태그 목록 | / | GET | tags | |
| 10.4 | 네이버 뉴스 프록시 | /news | GET | search/news?q= | |
| 10.5 | 순위 | /rankings | GET | rankings?type= | demo/blog/follower/home |

---

## 11. 관리자 — 대시보드

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 11.1 | 대시보드 통계 | /admin | GET | admin/dashboard | 가입자, 데모, 신고, 매출 |

---

## 12. 관리자 — 회원 관리

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 12.1 | 회원 목록/검색 | /admin/users | GET | admin/users?q=&status= | |
| 12.2 | 회원 상세 | /admin/users/[userId] | GET | admin/users/{userId} | |
| 12.3 | 회원 제재 | /admin/users/[userId] | PUT | admin/users/{userId}/ban | 일/주/월/영구 |
| 12.4 | 제재 해제 | /admin/users/[userId] | DELETE | admin/users/{userId}/ban | |

---

## 13. 관리자 — 콘텐츠 관리

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 13.1 | 데모 목록 | /admin/demos | GET | admin/demos | |
| 13.2 | 데모 삭제 | /admin/demos | DELETE | admin/demos/{demoId} | |
| 13.3 | 블로그 목록 | /admin/posts | GET | admin/posts | |
| 13.4 | 블로그 삭제 | /admin/posts | DELETE | admin/posts/{postId} | |
| 13.5 | 피드 목록 | /admin/feeds | GET | admin/feeds | |
| 13.6 | 피드 삭제 | /admin/feeds | DELETE | admin/feeds/{feedId} | |
| 13.7 | 댓글 관리 | /admin/comments | GET | admin/comments | 신고된 댓글 우선 |
| 13.8 | 댓글 삭제 | /admin/comments | DELETE | admin/comments/{commentId} | |

---

## 14. 관리자 — 신고 관리

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 14.1 | 신고 목록 | /admin/reports | GET | admin/reports?type=&status= | type으로 유형별 필터 |
| 14.2 | 신고 처리 | /admin/reports/[id] | PUT | admin/reports/{reportId} | 숨김/제재/기각 + 사유 |

> 신고된 콘텐츠 삭제는 13장(콘텐츠 관리)의 기존 삭제 API를 사용한다. 별도 `admin/reported/*` API 불필요.

---

## 15. 관리자 — 구독/정산

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 15.1 | 구독 현황 | /admin/subscriptions | GET | admin/subscriptions | |
| 15.2 | 매출 통계 | /admin/revenue | GET | admin/revenue?from=&to= | |
| 15.3 | 정산 관리 | /admin/settlements | GET | admin/settlements | |
| 15.4 | 정산 확정 | /admin/settlements | PUT | admin/settlements/{id}/confirm | |

---

## 16. 관리자 — 가상화폐 관리

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 16.1 | 충전 내역 조회 | /admin/coins | GET | admin/coins?userId=&type= | |
| 16.2 | 환불 처리 | /admin/coins/refunds | PUT | admin/coins/refunds/{id} | 승인/거절 + 사유 |
| 16.3 | 부정 사용 확인 | /admin/coins/suspicious | GET | admin/coins/suspicious | |

---

## 변경 요약

| 변경 | 내용 |
|------|------|
| 번호 체계 | 중복 제거. 1~16 섹션으로 정리 |
| 데모 API | `mini-homes/demos/...` → `demos/...` (독립 리소스) |
| mini-home 단수 통일 | `mini-homes/random` → `mini-home/random` |
| {userId} 통일 | 본인 수정 API도 `mini-home/{userId}` 사용. 서버에서 본인 검증 |
| 회원 탈퇴 | `PUT` → `DELETE users/me` |
| 알림 | `users/notifications` → `notifications` (독립 리소스) |
| 방문 카운트 | `visit-count` → `visit` |
| 차단 | `block/{userId}` → `blocks/{userId}` (복수형 통일) |
| 환불/부정사용 | `admin/{purchaseId}/...` → `admin/coins/...` |
| 신고 관리 | 유형별 별도 API 12개 → `admin/reports?type=` 필터 2개로 통합 |
| 블로그 페이지 URL | `/[id]/postwrite` → `/blog/write`, `/posts/[userid]` → `/home/[userId]/blog` |
| 댓글 수정/삭제 | 공통 `comments/{commentId}`로 통일 (데모·블로그·피드 공통) |
| 신고 API | 각 리소스별 `{resource}/{id}/report` → 공통 `POST reports` (body로 구분) |
| followings 오타 | `follwings` → `followings`, `follwers` → `followers` |
| comments 슬래시 | `posts/comments{commentId}` → `comments/{commentId}` |
