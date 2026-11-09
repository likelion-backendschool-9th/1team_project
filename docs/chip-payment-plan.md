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
- [ ] `백엔드-청사진.md` 상태 업데이트

**프론트**
- [x] `/my/coins` 페이지 — 잔액 표시 + 충전 버튼 (기존 UI 활용)
- [x] 충전 금액 선택 UI (패키지 고정값)
- [x] 거래 내역 목록 (실 API 연결)
- [x] 충전 후 `revalidatePath` 추가

---

### 2단계 — 유료 글 구매

> 브랜치: `feature/post-purchase`

**백엔드**
- [ ] `PostPurchase` 엔티티 확인 (이미 있음)
- [ ] `CoinService.useForPostPurchase(userId, postId, chips)` — 잔액 체크 → 차감 → `post_purchases` 저장
- [ ] `POST /posts/{postId}/purchase` 엔드포인트
- [ ] `백엔드-청사진.md` 상태 업데이트

**프론트**
- [ ] `unlockPost` Server Action 실 연동 (현재 목업)
- [ ] 구매 후 본문 전체 표시 (revalidatePath)
- [ ] 잔액 부족 시 에러 처리

---

### 3단계 — 블로그 구독

> 브랜치: `feature/blog-subscription`
> 현재 `subscribeBlog` 액션은 연동되어 있음 — 백엔드 실 구현 필요

**백엔드**
- [ ] `BlogSubscription` 엔티티 확인
- [ ] `SubscriptionService.subscribe(subscriberId, blogOwnerId)` — 칩 차감 + 구독 레코드 생성
- [ ] `SubscriptionService.unsubscribe(subscriberId)` — 구독 취소 (CANCELED 상태로)
- [ ] `POST /subscriptions`, `DELETE /subscriptions/me`
- [ ] 만료된 구독 자동 처리 (스케줄러 or 조회 시 체크)
- [ ] `백엔드-청사진.md` 상태 업데이트

**프론트**
- [ ] 구독 버튼 → 실 API 연동
- [ ] `/my/subscription` 페이지 — 현재 구독 상태 + 해지 버튼

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
- [ ] `GET /admin/settlements`, `PUT /admin/settlements/{id}/confirm` — 정산
- [ ] `GET /admin/coins/suspicious` — 이상 거래 탐지
- [ ] `백엔드-청사진.md` 상태 업데이트

**프론트**
- [ ] `/admin/coins` 페이지 — 충전 내역 + 환불 버튼
- [ ] `/admin/subscriptions` 페이지
- [ ] `/admin/revenue` 페이지
- [ ] `/admin/settlements` 페이지

---

## 주요 트레이드오프 메모

| 항목 | 선택 | 이유 |
|------|------|------|
| PG 연동 방식 | 즉시 충전 (no-op verify) | PG 미정, 나중에 `verifyPayment()` 교체 |
| 구독 결제 | 칩으로 임시 처리 | 현금 결제는 PG 연동 후 |
| 만료 구독 처리 | 조회 시 체크 (lazy) | 스케줄러 없이도 동작, 성능 이슈 시 배치로 전환 |
| 칩 차감 동시성 | `SELECT ... FOR UPDATE` | 잔액 음수 방지 |
