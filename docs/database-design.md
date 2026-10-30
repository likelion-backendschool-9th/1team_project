# 찍먹 데이터베이스 설계

대상 DB는 PostgreSQL 16이다. 테이블은 Spring Boot 배포 시 JPA 엔티티를 기준으로 생성·갱신한다. 컬럼 상세는 [테이블정의서](./테이블정의서.md), 관계 다이어그램은 [ERD](./erd.html)를 참조한다.

---

## 1. 범위와 결정

| 영역 | 포함 테이블 | 결정 |
| --- | --- | --- |
| 회원·인증 | users, social_accounts | 소셜 로그인만 지원한다 (GitHub, 카카오, 구글). 이메일·비밀번호 가입은 없다. 한 계정에 여러 소셜을 연동할 수 있다. `(provider, provider_id)` 유니크로 중복 연동을 막는다. |
| 미니홈피 | mini_homes, guestbooks | 회원 생성 시 미니홈피를 함께 만든다 (1:1). `today_count`는 매일 자정 리셋한다 (스케줄러). 같은 사람은 하루 1회만 집계한다 (서비스 로직). |
| 데모 | demos, tags, demo_tags | zip 업로드, 확장자/용량 검사 후 저장. `file_path`는 파일 저장소 경로다. `like_count`, `play_count`는 비정규화 — 좋아요/체험 시 트랜잭션으로 함께 갱신한다. 태그는 정규화해서 `tags` + `demo_tags` 중간 테이블로 관리한다. |
| 블로그 | posts | 데모와 연결 가능 (`demo_id` nullable FK). 유료 글은 `is_paid=true`이고 구독자 또는 도토리 결제자만 조회 가능하다 (서비스 로직). |
| 피드 | feeds, feed_votes, reposts | 짧은 글. 투표는 `poll_options` jsonb 배열 + `feed_votes` 테이블로 구현한다. `like_count`, `repost_count`는 비정규화. |
| 다형성 테이블 | comments, likes | `target_type` + `target_id`로 여러 엔티티를 대상으로 한다 (demo / post / feed). FK를 걸 수 없으므로 서비스 레이어에서 대상 존재 여부를 검증한다. |
| 소셜 | follows, blocks, bookmarks | 팔로우는 인스타그램 방식 (신청/수락 없음). 자기 자신 팔로우·차단은 CHECK 제약으로 막는다. |
| 결제·구독 | subscriptions, coin_transactions, settlements | 구독은 게시자별 월 구독료. `monthly_price`는 구독 시점 스냅샷이다. 도토리(가상화폐)는 `coin_transactions`로 이력을 남기고, `users.coin_balance`는 비정규화 잔액이다. 정산은 수수료 30%를 적용한다. |
| 알림·DM | notifications, dm_messages | 알림은 폴링 또는 WebSocket으로 읽는다. DM은 WebSocket(STOMP)으로 실시간 전송하고 DB에도 저장한다. |
| 관리자 | reports, user_bans | 신고는 `target_type` + `target_id` 다형성. 제재는 기간 유형별로 `expires_at`을 계산한다 (`permanent`이면 NULL). |

### 제외 항목

| 항목 | 사유 |
| --- | --- |
| 결제 PG 연동 테이블 | 실제 결제 연동 방식이 정해지면 설계한다 |
| 검색 인덱스 (벡터) | 벡터 검색 도입 시 별도 설계한다 |
| 파일 메타데이터 테이블 | 데모 zip은 `demos.file_path`로 충분. 이미지 등 추가 파일 관리가 필요하면 설계한다 |
| 뱃지·스킨 상세 테이블 | 꾸미기 기능은 우선순위 낮음. `mini_homes.skin_theme` varchar로 시작하고 필요 시 확장한다 |

---

## 2. ID 정책

- 모든 테이블의 PK는 `BIGINT GENERATED ALWAYS AS IDENTITY`를 기본으로 한다.
- API URL에는 숫자 ID를 사용한다 (닉네임 아님). 미니홈피 URL만 `userId`를 경로에 포함한다.
- 금액은 원 단위 정수이며 음수는 허용하지 않는다.
- 시각은 `TIMESTAMPTZ`로 저장하고 API에서 ISO 8601로 반환한다.

---

## 3. 비정규화 필드

성능을 위해 의도적으로 비정규화한 필드 목록이다. 원본 데이터와 불일치가 생기지 않도록 **반드시 트랜잭션 안에서 함께 갱신**한다.

| 테이블.컬럼 | 원본 | 갱신 시점 |
| --- | --- | --- |
| `users.coin_balance` | `coin_transactions` 합계 | 도토리 충전/사용/선물/환불 시 |
| `demos.like_count` | `likes` WHERE target_type='demo' 건수 | 좋아요 토글 시 |
| `demos.play_count` | 체험 이벤트 누적 | `POST demos/{id}/play` 시 |
| `posts.like_count` | `likes` WHERE target_type='post' 건수 | 좋아요 토글 시 |
| `feeds.like_count` | `likes` WHERE target_type='feed' 건수 | 좋아요 토글 시 |
| `feeds.repost_count` | `reposts` 건수 | 리포스트 시 |
| `mini_homes.today_count` | 방문 이벤트 (당일) | 방문 카운트 증가 시 |
| `mini_homes.total_count` | 방문 이벤트 (누적) | 방문 카운트 증가 시 |

---

## 4. 인덱스 전략

PK, UNIQUE, FK 인덱스 외에 추가로 필요한 인덱스다.

| 테이블 | 인덱스 | 용도 |
| --- | --- | --- |
| `users` | `(email)` UNIQUE | 로그인 시 이메일 조회 |
| `users` | `(nickname)` UNIQUE | 닉네임 중복 검사, 프로필 검색 |
| `social_accounts` | `(provider, provider_id)` UNIQUE | 소셜 로그인 시 기존 계정 조회 |
| `demos` | `(user_id, created_at DESC)` | 사용자별 데모 목록 |
| `demos` | `(like_count DESC, created_at DESC)` | 인기 데모 정렬 |
| `posts` | `(user_id, created_at DESC)` | 사용자별 블로그 목록 |
| `feeds` | `(created_at DESC)` | 최신 피드 목록 |
| `comments` | `(target_type, target_id, created_at)` | 대상별 댓글 목록 |
| `likes` | `(user_id, target_type, target_id)` UNIQUE | 중복 좋아요 방지 + 좋아요 여부 확인 |
| `follows` | `(follower_id)`, `(following_id)` | 팔로잉/팔로워 목록 |
| `notifications` | `(user_id, is_read, created_at DESC)` | 안 읽은 알림 우선 조회 |
| `dm_messages` | `(sender_id, receiver_id, created_at DESC)` | DM 대화 내역 조회 |
| `reports` | `(status, created_at DESC)` | 관리자 신고 목록 (대기 우선) |
| `subscriptions` | `(subscriber_id, target_user_id)` | 구독 여부 확인 |

---

## 5. 동시성 처리

| 상황 | 전략 |
| --- | --- |
| 좋아요 토글 | `likes` INSERT/DELETE + 대상 테이블 `like_count` 갱신을 한 트랜잭션으로 처리한다. UNIQUE 제약이 중복 좋아요를 막는다. |
| 도토리 사용 | `users.coin_balance >= amount` 조건 확인 후 차감 + `coin_transactions` INSERT를 한 트랜잭션으로 처리한다. 잔액 부족 시 롤백한다. `SELECT ... FOR UPDATE`로 잔액 행을 잠근다. |
| 팔로우/언팔로우 | UNIQUE 제약이 중복을 막는다. INSERT 실패 시 이미 팔로우 상태로 처리한다. |
| 데모 체험 카운트 | 정확한 집계보다 성능이 중요하다. `play_count`를 `UPDATE demos SET play_count = play_count + 1`로 갱신한다. |
| 방문자 카운트 | 하루 1회 제한은 서비스 레이어에서 캐시(Redis) 또는 별도 방문 기록 테이블로 처리한다. `today_count`와 `total_count`는 원자적으로 증가시킨다. |
| 구독 | 구독 생성 시 `subscriptions` INSERT + `coin_transactions` INSERT + `users.coin_balance` 차감을 한 트랜잭션으로 처리한다. |
| 신고 처리 | 관리자가 신고를 처리할 때 `reports.status`를 변경한다. 두 관리자가 동시에 같은 신고를 처리하는 것을 막으려면 `status='PENDING'` 조건 UPDATE를 사용한다. |

---

## 6. 구현 순서

Phase 1부터 순서대로 엔티티와 API를 작은 PR로 만든다.

### Phase 1 — 핵심 (MVP)

| 순서 | 엔티티 | 검증 항목 |
| --- | --- | --- |
| 1 | users, social_accounts | 소셜 로그인 → JWT 발급 → 프로필 조회. 이메일·닉네임 유니크 제약 |
| 2 | mini_homes | 회원 생성 시 1:1 자동 생성. 조회·수정 |
| 3 | demos, tags, demo_tags | zip 업로드 → 저장 → 상세 조회. 태그 검색·필터. 확장자/용량 제약 |
| 4 | posts | 글 CRUD. 데모 연결 (demo_id). 유료/무료 구분 |
| 5 | follows | 팔로우/언팔로우. 자기참조 CHECK. 팔로잉/팔로워 목록 |
| 6 | feeds, feed_votes, reposts | 피드 CRUD. 투표. 리포스트 |
| 7 | comments, likes, bookmarks | 다형성 댓글·좋아요. 북마크. 비정규화 카운트 동시성 검증 |

### Phase 2 — 소셜/소통

| 순서 | 엔티티 | 검증 항목 |
| --- | --- | --- |
| 8 | dm_messages | WebSocket 연결. DM 전송·수신·읽음. DB 저장 |
| 9 | guestbooks | 방명록 작성·조회. 비밀글은 주인만 |
| 10 | notifications | 알림 생성·조회·읽음. WebSocket 실시간 전달 |
| 11 | blocks | 차단/해제. 차단된 사용자 콘텐츠 필터링 |

### Phase 3 — 수익화/관리

| 순서 | 엔티티 | 검증 항목 |
| --- | --- | --- |
| 12 | subscriptions, coin_transactions | 구독 생성·해지. 도토리 충전·사용. 잔액 동시성 |
| 13 | settlements | 정산 계산 (수수료 30%). 기간별 집계 |
| 14 | reports, user_bans | 신고 접수·처리. 회원 제재. 관리자 대시보드 |

### 검증 공통

- 각 Phase에서 FK·UNIQUE·CHECK 제약, 트랜잭션, 권한을 검증한다.
- 비정규화 필드는 원본과 일치하는지 확인한다.
- 빈 DB에서 회원 생성 → 데모 업로드 → 피드 작성 → DM → 구독까지 흐름을 확인한다.
