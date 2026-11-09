# 칩·결제 시스템 구현 계획

> 작업하면서 체크 채워나가기.

---

## 설계 원칙

- **지금**: `POST /coins/purchases` 한 방에 처리 (PG 없이 즉시 충전)
- **나중에**: `verifyPayment()` 메서드에 PG 검증 로직만 꽂으면 됨
- 칩 차감은 모두 `SELECT ... FOR UPDATE`로 비관적 잠금 (race condition 방지)
- 모든 칩 증감은 `chip_transactions`에 기록 → `users.chip_balance`와 항상 동기화

---

## 구현 순서

### 1단계 — 칩 충전 + 거래 내역

> 브랜치: `feature/chip-charge`

**백엔드**
- [x] `Payment` 엔티티 + `PaymentRepository`
- [x] `ChipTransaction` 엔티티 + `ChipTransactionRepository`
- [x] `CoinService` — `charge(userId, packageId)` + `verifyPayment()` (현재는 no-op)
- [x] `CoinController` — `POST /coins/purchases`, `GET /coins/history`
- [x] `백엔드-청사진.md` 상태 업데이트

**프론트**
- [x] `/my/coins` 페이지 — 잔액 표시 + 충전 버튼 (기존 UI 활용)
- [x] 충전 금액 선택 UI (패키지 고정값)
- [x] 거래 내역 목록 (실 API 연결)
- [x] 충전 후 `revalidatePath` 추가
- [x] 거래 내역 무한 스크롤 페이지네이션

---

### 2단계 — 유료 글 구매

> 브랜치: `feature/post-purchase`

**백엔드**
- [x] `PostPurchase` 엔티티 확인 (이미 있음) — `PostPurchaseRepository.insertIgnore()`로 중복 구매 방지
- [x] `CoinService.payForPost(buyerId, authorId, postId, title, chips)` — 두 회원을 ID 순서로 잠금 → 구매자 차감(BLOG_PURCHASE) → 글쓴이 수익(BLOG_INCOME). 수수료는 정산(5단계)에서 뗀다
- [x] `POST /posts/{postId}/purchase` 엔드포인트 — `PostService.purchase()`가 글 검사 후 `post_purchases` 저장 → `payForPost()` 호출
- [x] `백엔드-청사진.md` 상태 업데이트

**프론트**
- [x] `unlockPost` Server Action 실 연동
- [x] 구매 후 본문 전체 표시 (revalidatePath)
- [x] 잔액 부족 시 에러 처리 (백엔드 메시지 "칩이 부족해요. 필요: N개, 보유: M개"를 그대로 보여 줌)

---

### 3단계 — 블로그 구독

> 브랜치: `feature/blog-subscription`
> 결제 수단: 칩이 아니라 **현금(원)**으로 정했다 (팀 결정). 테이블정의서(`payments.payment_type = BLOG_SUBSCRIPTION`, 구독료 `blog_subscription_price` 원)와 화면 문구("칩은 차감되지 않아요")에 맞춘 것이다.

**백엔드**
- [x] `BlogSubscription` 엔티티 확인
- [x] `BlogSubscriptionService.subscribe(subscriberId, blogOwnerId)` — 구독 레코드 생성(한 달) + `CoinService.payBlogSubscription()`으로 `payments`에 기록(PG no-op 즉시 PAID) + 블로그 주인에게 SUBSCRIBE 알림. 이미 구독 중이면 그대로, 해지했지만 만료 전이면 결제 없이 다시 이어 줌
- [x] `BlogSubscriptionService.cancel(subscriberId, subscriptionId)` — CANCELED로. 만료일까지는 유료 글 열람 가능
- [x] `POST /subscriptions`, `DELETE /subscriptions/me`, `GET /subscriptions/me`(내 목록), `GET /users/{userId}/subscription`(블로그 화면용)
- [x] 만료된 구독 처리 — 조회 시 체크(`expires_at > now`). EXPIRED로 바꾸는 배치는 아직 없음
- [ ] 자동 갱신(매달 결제) — PG 연동 후
- [ ] 구독료 설정 API — 아직 없음 (시드 데이터의 `blog_subscription_price`로 확인)
- [x] `백엔드-청사진.md` 상태 업데이트

**프론트**
- [x] 구독 버튼 → 실 API 연동, 구독료가 0인 블로그는 버튼 숨김
- [x] `/my/subscription` 페이지 — 현재 구독 상태 + 해지 버튼 (딥소스 플러스 영역은 아직 목업)

---

### 4단계 — 후원

> 브랜치: `feature/donation`

**백엔드**
- [ ] `Donation` 엔티티 + `DonationRepository`
- [ ] `DonationService.donate(senderId, miniHomeId, chips)` — 잔액 체크 → 차감 → 수신자 증가
- [ ] `POST /mini-homes/{miniHomeId}/donations`
- [ ] `백엔드-청사진.md` 상태 업데이트

**프론트**
- [ ] 미니홈피 후원 버튼 + 칩 입력 UI
- [ ] 후원 성공/실패 처리

---

### 5단계 — 관리자

> 브랜치: `feature/admin-coins`

**백엔드**
- [ ] `GET /admin/coins/purchases?userId=&type=` — 충전 내역 조회
- [ ] `PUT /admin/coins/purchases/{purchaseId}/refund` — 환불 처리
- [ ] `GET /admin/subscriptions` — 구독 목록
- [ ] `GET /admin/revenue?from=&to=` — 수익 현황
- [x] `GET /admin/settlements`, `POST /admin/settlements?period=YYYY-MM`(한 달 치 생성), `PUT /admin/settlements/{id}/confirm` — 정산. 월별 수익 칩(BLOG_INCOME·DONATION_INCOME) 합계 × 200원, 수수료 30%. 확정 시 회원 칩 차감(SETTLEMENT) + 관리자 기록
- [ ] `GET /admin/coins/suspicious` — 이상 거래 탐지
- [ ] `백엔드-청사진.md` 상태 업데이트

**프론트**
- [ ] `/admin/coins` 페이지 — 충전 내역 + 환불 버튼
- [ ] `/admin/subscriptions` 페이지
- [ ] `/admin/revenue` 페이지
- [x] `/admin/settlements` 페이지 — 정산 만들기(월 선택) + 확정 버튼

---

## 주요 트레이드오프 메모

| 항목 | 선택 | 이유 |
|------|------|------|
| PG 연동 방식 | 즉시 충전 (no-op verify) | PG 미정, 나중에 `verifyPayment()` 교체 |
| 구독 결제 | 칩으로 임시 처리 | 현금 결제는 PG 연동 후 |
| 만료 구독 처리 | 조회 시 체크 (lazy) | 스케줄러 없이도 동작, 성능 이슈 시 배치로 전환 |
| 칩 차감 동시성 | `SELECT ... FOR UPDATE` | 잔액 음수 방지 |
