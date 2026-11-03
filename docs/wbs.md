# 찍먹 WBS (Work Breakdown Structure)

> **기준:** 구글 시트 [응용프로젝트 1팀 WBS](https://docs.google.com/spreadsheets/d/1HRJKGv3sKu0TpdEOQmTYPxSIa9DtNrrpDJFNFDfLV2Q/edit?gid=95799491) 의 `WBS6(url수정)` 탭 (2026-11-03 동기화). 시트가 원본이고 이 문서는 사본이다. 시트가 바뀌면 이 문서도 맞춘다.

> **기능 범위·작업/API 초안:** 아래 목록은 구현 계약이 아닙니다. 서비스 방향은 [기획서.md](기획서.md), 문서 우선순위는 [README.md](README.md)를 확인하세요.

> 모든 API 기본 경로: `/api/v1/`. 작업 번호는 시트 번호를 그대로 쓴다 (중복·순서 문제는 맨 아래 "시트 확인 필요" 참고).

---

## 0. (공통) 설계·레이아웃

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 0.1 | ERD 설계 | - | - | - | |
| 0.2 | API 명세 작성 | - | - | - | |
| 0.3 | 인증 처리 (로그인 여부 확인, 미로그인 시 /login 이동) | 공통 | - | - | |
| 0.4 | 왼쪽 어사이드 레이아웃 (로고 · 피드/블로그/개발 뉴스/이벤트 메뉴 · 팔로잉/추천 탭) | 공통 | - | - | 메뉴 바로가기 링크 포함 |
| 0.5 | 오른쪽 어사이드 레이아웃 (프로필 카드 · 미니홈피/내정보 바로가기 · 알림 · 팔로잉 목록 · 설정) | 공통 | - | - | 데이터는 내 정보(1.3)·팔로잉 목록(3.4)·안 읽은 알림 수(8.2) API 재사용 |
| 0.6 | 미니홈피 헤더 메뉴바 (홈·데모·방명록·블로그·수정·메인 피드 바로가기) | /mini-home/[userId] 하위 공통 | - | - | |
| 0.7 | 검색창 (Ctrl K) | 공통 | - | - | |

---

## 1. (사용자) 회원

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 1.1 | 소셜 로그인 (github / kakao / google) | /login | GET | auth/login/{provider} | OAuth 리다이렉트 방식 → GET + 콜백 URL 필요 (Spring Security 기본값: /oauth2/authorization/{provider}) |
| 1.2 | 최초 로그인 추가 정보 입력 (닉네임·기술 스택) | /onboarding | POST | users/me/profile | 자체 회원가입 없음 → 이 단계가 필요한지 팀 확인 |
| 1.3 | 내 정보 조회 (팔로워·팔로잉·칩 수 포함) | /my, 공통(오른쪽 어사이드) | GET | users/me | 오른쪽 프로필 카드도 이 API 재사용 |
| 1.4 | 내 정보 수정 | /my | PUT | users/me | |
| 1.5 | 회원 탈퇴 | /my | DELETE | users/me | |
| 1.6 | 다른 사용자 프로필 조회 | /mini-home/[userId] | GET | users/{userId} | 미니홈피 주인 정보 등 |
| 1.7 | 설정 조회 | /my/settings | GET | users/me/settings | |
| 1.8 | 설정 수정 | /my/settings | PUT | users/me/settings | |
| 1.9 | 로그아웃 | /main | POST | auth/logout | 버튼 위치 확정 필요 (/my 또는 오른쪽 어사이드) |
| 1.10 | 닉네임 중복 확인 | /onboarding | GET | nicknames/available | 현재 구현은 `users/nicknames/available` |

---

## 2. (사용자) 피드

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 2.1 | 피드 목록 (팔로잉) | /main | GET | feeds?type=following | 응답에 좋아요·댓글·리포스트 수 포함 (feeds 집계 칸) |
| 2.2 | 피드 목록 (추천) | /main | GET | feeds?type=recommended | 응답에 좋아요·댓글·리포스트 수 포함 (feeds 집계 칸) |
| 2.3 | 피드 작성 | /main | POST | feeds | 500자 제한 |
| 2.4 | 태그 목록 | /main | (미정) | tags | |
| 2.5 | 피드 상세 | /feeds/[feedId] | GET | feeds/{feedId} | 응답에 좋아요·댓글·리포스트 수 포함 (feeds 집계 칸) |
| 2.6 | 피드 수정 | /feeds/[feedId] | PUT | feeds/{feedId} | |
| 2.7 | 피드 삭제 | /feeds/[feedId] | DELETE | feeds/{feedId} | |
| 2.8 | 댓글/대댓글 목록 | /feeds/[feedId] | GET | feeds/{feedId}/comments | |
| 2.9 | 댓글/대댓글 작성 | /feeds/[feedId] | POST | feeds/{feedId}/comments | |
| 2.10 | 댓글/대댓글 수정 | /feeds/[feedId] | PUT | feeds/comments/{commentId} | 경로 형식 팀 논의 중 (`comments/{commentId}` 또는 `feeds/{feedId}/comments/{commentId}` 의견 있음) |
| 2.11 | 댓글/대댓글 삭제 | /feeds/[feedId] | DELETE | feeds/comments/{commentId} | |
| 2.12 | 좋아요 | /main, /feeds/[feedId] | POST | feeds/{feedId}/like | |
| 2.13 | 좋아요 취소 | /main, /feeds/[feedId] | DELETE | feeds/{feedId}/like | |
| 2.14 | 리포스트 | /main, /feeds/[feedId] | POST | feeds/{feedId}/repost | |
| 2.15 | 리포스트 취소 | /main, /feeds/[feedId] | DELETE | feeds/{feedId}/repost | |
| 2.16 | 피드 신고 | /main, /feeds/[feedId] | POST | feeds/{feedId}/report | |
| 2.13 | 카톡 공유 | /main, /feeds/[feedId] | - | - | 카카오 SDK로 프론트에서 처리 |
| 2.17 | 피드 북마크 | /main, /feeds/[feedId] | POST | feeds/{feedId}/bookmark | |
| 2.18 | 피드 북마크 취소 | /main, /feeds/[feedId] | DELETE | feeds/{feedId}/bookmark | |
| 2.19 | 내 북마크 목록 | /my/bookmarks | GET | users/me/bookmarks?type= | type: feed / post / demo. 피드·블로그·데모 북마크를 한 페이지에서 모아봄 (페이지 위치 팀 확인) |

---

## 3. (사용자) 소셜 (팔로우·차단·DM)

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 3.1 | 팔로우 | /main, /feeds/[feedId], /mini-home/[userId] | POST | follows/{userId} | |
| 3.2 | 언팔로우 | /main, /feeds/[feedId], /mini-home/[userId] | DELETE | follows/{userId} | |
| 3.3 | 사용자 차단 | /main, /feeds/[feedId] | POST | blocks/{userId} | |
| 3.4 | 팔로잉 목록 | /my/follows, 공통(오른쪽 어사이드) | GET | follows/followings | |
| 3.5 | 팔로워 목록 | /my/follows | GET | follows/followers | |
| 3.6 | 차단 해제 | /my/settings | DELETE | blocks/{userId} | |
| 3.7 | DM 대화방 목록 | /dm | GET | dm/rooms | |
| 3.8 | DM 대화 내역 | /dm/[userId] | GET | dm/{userId} | |
| 3.9 | DM 보내기 | /dm/[userId] | - | dm | WebSocket(STOMP) |
| 3.10 | 팔로잉 접속 상태 조회 | 공통(오른쪽 어사이드) | GET | follows/followings/presence | 팔로잉 목록(3.4) 응답에 접속 여부를 포함해도 됨 |
| 3.11 | 내 접속 상태 갱신 | 공통 | PUT | users/me/presence | 일정 주기로 호출(하트비트)하거나 DM WebSocket 연결 여부로 대체 가능 |

---

## 4. (사용자) 검색

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 4.1 | 통합 검색 결과 | /search?q= | GET | search?q=&type= | type 파라미터로 블로그만 검색 등 가능 |
| 4.2 | 인기 검색어 | /search?q= | GET | search/popular | 검색창 드롭다운으로 보여주면 페이지 URL → 공통 |

---

## 5. (사용자) 블로그

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 5.1 | 블로그 메인 (전체 글 목록) | /blog | GET | posts/list | |
| 5.2 | 사용자 블로그 글 목록 | /blog/[userId] | GET | users/{userId}/posts | |
| 5.4 | 글 작성 | /blog/write | POST | posts | |
| 5.3 | 글 조회 | /blog/posts/[postId] | GET | posts/{postId} | |
| 5.5 | 글 수정 | /blog/posts/[postId]/edit | PUT | posts/{postId} | |
| 5.6 | 글 삭제 | /blog/posts/[postId] | DELETE | posts/{postId} | |
| 5.7 | 글 신고 | /blog/posts/[postId] | POST | posts/{postId}/report | |
| 5.8 | 댓글/대댓글 목록 | /blog/posts/[postId] | GET | posts/{postId}/comments | |
| 5.9 | 댓글/대댓글 작성 | /blog/posts/[postId] | POST | posts/{postId}/comments | |
| 5.10 | 댓글/대댓글 수정 | /blog/posts/[postId] | PUT | posts/comments/{commentId} | |
| 5.11 | 댓글/대댓글 삭제 | /blog/posts/[postId] | DELETE | posts/comments/{commentId} | |
| 5.12 | 좋아요 | /blog/posts/[postId] | POST | posts/{postId}/like | |
| 5.13 | 좋아요 취소 | /blog/posts/[postId] | DELETE | posts/{postId}/like | |
| 5.14 | 블로그 글 북마크 | /blog/posts/[postId] | POST | posts/{postId}/bookmark | |
| 5.15 | 블로그 글 북마크 취소 | /blog/posts/[postId] | DELETE | posts/{postId}/bookmark | |

---

## 6. (사용자) 미니홈피

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 6.1 | 방문자 통계 조회 | /mini-home/[userId] | GET | mini-homes/{userId}/visitors | |
| 6.2 | 방문 카운트 증가 | /mini-home/[userId] | POST | mini-homes/{userId}/visits | 방문한 수만큼 증가 (기존 비고). 하루 1회 제한 여부 팀 확인 |
| 6.3 | 파도타기 (랜덤 방문) | /mini-home/[userId] | GET | mini-homes/random | |
| 6.4 | 깃허브 잔디 조회 | /mini-home/[userId] | GET | users/{userId}/github/contributions | |
| 6.5 | 깃허브 대표 레포 조회 | /mini-home/[userId] | GET | users/{userId}/github/repos | |
| 6.6 | 언어 사용 통계 조회 | /mini-home/[userId] | GET | users/{userId}/github/languages | |
| 6.7 | 일촌평 목록 | /mini-home/[userId] | GET | mini-homes/{userId}/comments | 일촌평과 방명록 차이 팀 확인 |
| 6.8 | 일촌평 작성 | /mini-home/[userId] | POST | mini-homes/{userId}/comments | |
| 6.9 | 방명록 목록 | /mini-home/[userId]/guestbook | GET | mini-homes/{userId}/guestbook | |
| 6.10 | 방명록 작성 | /mini-home/[userId]/guestbook | POST | mini-homes/{userId}/guestbook | |
| 6.11 | 방명록 삭제 | /mini-home/[userId]/guestbook | DELETE | mini-homes/guestbook/{guestbookId} | |
| 6.12 | 미니홈피 수정 (본인만) | /mini-home/[userId]/edit | PUT | mini-homes/me | |
| 6.13 | 대표 데모 설정 | /mini-home/[userId]/edit | PUT | mini-homes/me/featured-demo | |
| 6.14 | 사용자 데모 목록 | /mini-home/[userId]/demos | GET | demos?userId={userId} | 데모 목록(7.2) API 재사용 |

---

## 7. (사용자) 데모

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 7.1 | 지금 뜨는 데모 | /main | GET | demos?sort=trending | 데모 목록(7.2) 재사용, '뜨는' 기준 정의 필요 |
| 7.2 | 데모 목록 | /mini-home/demos | GET | demos?tag=&sort=&userId= | |
| 7.3 | 데모 상세 | /mini-home/demos/[demoId] | GET | demos/{demoId} | |
| 7.7 | 데모 수정 | /demos/[demoId] | PUT | demos/{demoId} | |
| 7.4 | 데모 삭제 | /mini-home/demos/[demoId] | DELETE | demos/{demoId} | |
| 7.5 | 좋아요 | /mini-home/demos/[demoId] | POST | demos/{demoId}/like | |
| 7.6 | 좋아요 취소 | /mini-home/demos/[demoId] | DELETE | demos/{demoId}/like | |
| 7.7 | 데모 신고 | /mini-home/demos/[demoId] | POST | demos/{demoId}/report | |
| 7.8 | 댓글/대댓글 목록 | /mini-home/demos/[demoId] | GET | demos/{demoId}/comments | |
| 7.9 | 댓글/대댓글 작성 | /mini-home/demos/[demoId] | POST | demos/{demoId}/comments | |
| 7.10 | 댓글/대댓글 수정 | /mini-home/demos/[demoId] | PUT | demos/comments/{commentId} | |
| 7.11 | 댓글/대댓글 삭제 | /mini-home/demos/[demoId] | DELETE | demos/comments/{commentId} | |
| 7.12 | 데모 북마크 | /mini-home/demos/[demoId] | POST | demos/{demoId}/bookmark | |
| 7.13 | 데모 북마크 취소 | /mini-home/demos/[demoId] | DELETE | demos/{demoId}/bookmark | |
| 7.14 | 찍먹하기 (체험 카운트) | /main, /demos/[demoId] | POST | demos/{demoId}/play | |
| 7.15 | 데모 업로드 (zip) | /demos/upload | POST | demos | |
| 7.16 | 데모 파일 검사 (서버 비동기 처리) | - | - | - | 업로드 후 서버 내부 작업 (API 아님) |
| 7.17 | 파일 검사 진행률 조회 | /demos/upload | GET | demos/{demoId}/status | 폴링 / 웹소켓 방식 결정 필요 |

---

## 8. (사용자) 알림

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 8.1 | 알림 목록 | /notifications | GET | notifications | 드롭다운으로 할 경우 페이지 URL → 공통 |
| 8.3 | 알림 읽음 처리 | /notifications | PUT | notifications/{notificationId}/read | |
| 8.2 | 안 읽은 알림 수 | 공통(오른쪽 어사이드) | GET | notifications/unread-count | |

---

## 9. (사용자) 뉴스·순위·이벤트

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 9.1 | 개발 뉴스 (네이버 뉴스 API 프록시) | /news | GET | search/news?q= | |
| 9.2 | 순위 (demo / blog / follower / home) | /rankings | GET | rankings?type= | |
| 9.3 | 주간 순위 위젯 | 공통(왼쪽 어사이드) | GET | rankings?type=&period=weekly | 9.2 재사용 |
| 9.4 | 이벤트 목록 | /events | GET | events?status= | status: ONGOING / ENDED |
| 9.5 | 이벤트 상세 | /events/[eventId] | GET | events/{eventId} | |

---

## 10. (사용자) 칩·구독 결제

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 9.6 | 칩 충전 | /my/coins | POST | coins/purchases | PG 연동 필요, 기획 확인 |
| 9.7 | 칩 충전·사용 내역 | /my/coins | GET | coins/history | |
| 9.8 | 칩 사용 | - | POST | (기획 후 결정) | 사용처 기획 필요 |
| 9.9 | 구독 결제 | /my/subscription | POST | subscriptions | PG 연동 필요 |
| 9.10 | 구독 해지 | /my/subscription | DELETE | subscriptions/me | |

---

## 10.0 (공통) 테스트·배포

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 10.1 | 통합 테스트 | - | - | - | |
| 10.2 | 서버 배포 | - | - | - | |

---

## 12. (관리자) 공통

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 12.1 | 관리자 로그인·권한 확인 (ROLE_ADMIN) | /admin 하위 공통 | - | - | |
| 10.3 | 관리자 헤더 메뉴바 (회원·콘텐츠·신고·구독·가상화폐 바로가기) | 공통 | - | - | |
| 12.3 | 대시보드 통계 | /admin | GET | admin/dashboard | |

---

## 13. (관리자) 회원 관리

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 10.4 | 회원 목록 / 검색 | /admin/users | GET | admin/users?q=&status= | |
| 10.5 | 회원 상세 | /admin/users/[userId] | GET | admin/users/{userId} | |
| 10.6 | 회원 제재 | /admin/users/[userId] | PUT | admin/users/{userId}/ban | |
| 10.7 | 제재 해제 | /admin/users/[userId] | DELETE | admin/users/{userId}/ban | |

---

## 11.0 (관리자) 콘텐츠 관리

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 11.1 | 데모 목록 | /admin/demos | GET | admin/demos | |
| 11.2 | 데모 삭제 | /admin/demos | DELETE | admin/demos/{demoId} | 신고된 데모 삭제도 이 API 재사용 |
| 11.3 | 블로그 글 목록 | /admin/posts | GET | admin/posts | |
| 11.4 | 블로그 글 삭제 | /admin/posts | DELETE | admin/posts/{postId} | 신고된 글 삭제도 이 API 재사용 |
| 11.5 | 피드 목록 | /admin/feeds | GET | admin/feeds | |
| 11.6 | 피드 삭제 | /admin/feeds | DELETE | admin/feeds/{feedId} | 신고된 피드 삭제도 이 API 재사용 |
| 11.7 | 댓글 목록 | /admin/comments | GET | admin/comments | |
| 11.8 | 댓글 삭제 | /admin/comments | DELETE | admin/comments/{commentId} | 신고된 댓글 삭제도 이 API 재사용 |
| 11.9 | 이벤트 등록 | /admin/events | POST | admin/events | 이벤트 메뉴(9.4)에 보일 내용을 관리자가 등록 |
| 11.10 | 이벤트 수정 | /admin/events | PUT | admin/events/{eventId} | |
| 11.11 | 이벤트 삭제 | /admin/events | DELETE | admin/events/{eventId} | |

---

## 12.0 (관리자) 신고 관리

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 12.1 | 신고 목록 (유형별 필터) | /admin/reports | GET | admin/reports?type=&status= | type: demo / post / feed / comment |
| 12.2 | 신고 처리 (상태 변경) | /admin/reports/[reportId] | PUT | admin/reports/{reportId} | 콘텐츠 삭제는 콘텐츠 관리(11.x) 삭제 API 재사용 |

---

## 13.0 (관리자) 구독·매출

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 13.1 | 구독 현황 | /admin/subscriptions | GET | admin/subscriptions | |
| 13.2 | 매출 통계 | /admin/revenue | GET | admin/revenue?from=&to= | |
| 13.3 | 정산 목록 | /admin/settlements | GET | admin/settlements | |
| 13.4 | 정산 확정 | /admin/settlements | PUT | admin/settlements/{settlementId}/confirm | |

---

## 14.0 (관리자) 칩(가상화폐) 관리

| # | 작업명 | 페이지 URL | Method | API URL | 비고 |
|---|--------|-----------|--------|---------|------|
| 14.1 | 충전 내역 조회 | /admin/coins | GET | admin/coins/purchases?userId=&type= | |
| 14.2 | 환불 처리 | /admin/coins | PUT | admin/coins/purchases/{purchaseId}/refund | |
| 14.3 | 부정 사용 목록 | /admin/coins/suspicious | GET | admin/coins/suspicious | |

---

## 시트 확인 필요

시트를 옮기면서 발견한 점. 시트에서 정리되면 이 문서도 맞춘다.

| 항목 | 내용 |
|------|------|
| 중복 번호 | 2.13(좋아요 취소·카톡 공유), 7.7(데모 수정·데모 신고), 12.1(관리자 권한 확인·신고 목록) |
| 섹션과 번호 불일치 | 칩·구독 결제(10장) 항목이 9.6~9.10, 관리자 회원 관리(13장) 항목이 10.4~10.7, 관리자 헤더 메뉴바가 10.3 |
| 순서 | 5.4가 5.3보다 앞, 7.7(데모 수정)이 7.3 다음, 8.3이 8.2보다 앞 |
| 1.10 닉네임 중복 확인 | 시트는 `nicknames/available`, 현재 구현은 `users/nicknames/available` |
| 2.4 태그 목록 | HTTP Method 비어 있음 |
| 5.1 블로그 전체 글 목록 | 시트는 `posts/list`, 현재 프론트는 `posts?...`로 호출 |
| 비고의 옛 번호 | 시트 비고가 옛 번호를 가리키던 곳은 이 문서에서 현재 번호로 고쳤다 (0.5, 3.10, 6.14, 7.1, 12.2) |
