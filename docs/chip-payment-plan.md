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
- [x] 구독료 설정 API — `PUT users/me/subscription` (내 블로그 화면의 구독료 설정, 0원이면 구독 안 받음, 상한·하한 없음)
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

> 브랜치: ① 조회 `feature/admin-payment-read` → ② 정산 → ③ 환불·이상 거래 (PR 3개로 나눔)

**정한 것 (2026-11-09)**
- 정산 수수료 **30%** (database-design.md 기준). 관리자 목업의 20%는 맞춰 고친다
- 정산은 **관리자 버튼**("지난달 정산 만들기")으로 작가별로 만든다. 대상은 판매 수익(BLOG_INCOME)·후원 수익(DONATION_INCOME) 칩. 매달 자동 배치는 나중에
- 관리자 환불은 **칩 충전 환불만**. 결제를 REFUNDED로 바꾸고 칩을 회수하며, 남은 칩이 모자라면 환불 불가. 회원 "환불 요청" 기능과 유료 글 24시간 환불은 만들지 않는다
- 이상 거래는 **간단한 규칙 2~3개**를 집계로 계산 (짧은 시간 충전 반복, 충전 후 환불 반복, 같은 사람끼리 후원 반복). 새 테이블 없음

**백엔드**
- [x] `GET /admin/coins/purchases?userId=&type=` — 충전 내역 조회 (①)
- [x] `PUT /admin/coins/purchases/{purchaseId}/refund` — 환불 처리 (③). 칩 충전만, 칩 회수 후 REFUNDED. 실제 송금은 PG 연동 후
- [x] `GET /admin/subscriptions` — 구독 목록 (①)
- [x] `GET /admin/revenue?from=&to=` — 수익 현황 (①). 대시보드 `revenueMonth`(지난달 매출)도 채움
- [ ] `GET /admin/settlements`, `PUT /admin/settlements/{id}/confirm` — 정산
- [x] `GET /admin/coins/suspicious` — 이상 거래 탐지 (③). 규칙 3개를 조회할 때 계산
- [ ] `백엔드-청사진.md` 상태 업데이트

**프론트**
- [ ] `/admin/coins` 페이지 — 충전 내역(① 완료) + 환불 버튼(③)
- [x] `/admin/subscriptions` 페이지 (①)
- [x] `/admin/revenue` 페이지 (①)
- [ ] `/admin/settlements` 페이지

---

## 주요 트레이드오프 메모

| 항목 | 선택 | 이유 |
|------|------|------|
| PG 연동 방식 | 즉시 충전 (no-op verify) | PG 미정, 나중에 `verifyPayment()` 교체 |
| 구독 결제 | 칩으로 임시 처리 | 현금 결제는 PG 연동 후 |
| 만료 구독 처리 | 조회 시 체크 (lazy) | 스케줄러 없이도 동작, 성능 이슈 시 배치로 전환 |
| 칩 차감 동시성 | `SELECT ... FOR UPDATE` | 잔액 음수 방지 |
