// 서비스 정책 값 (설정 정리 2026-11-02). 바뀌면 이 파일만 고친다.

/** 1칩 가격(원) */
export const CHIP_PRICE_WON = 200;

/** 기본 데모 인벤토리 칸 수 */
export const DEMO_INVENTORY_BASE = 6;
/** 인벤토리 1칸 추가 비용(칩) */
export const INVENTORY_SLOT_CHIPS = 10;
/** 딥소스 플러스 인벤토리 추가 칸 */
export const PLUS_INVENTORY_BONUS = 2;

/** 미니홈피에 보여 주는 데모 수 (일반 / 플러스) */
export const SHOWCASE_BASE = 3;
export const SHOWCASE_PLUS = 6;

/** 딥소스 플러스 월 요금(원). 금액은 아직 정해지지 않은 임시값이다. */
export const PLUS_PRICE_WON = 4900;

export const PLUS_BENEFITS = [
  `미니홈피에 보여 주는 데모 ${SHOWCASE_BASE}개 → ${SHOWCASE_PLUS}개`,
  "미니홈피 스킨 커스텀",
  "미니홈피 BGM",
  "프로필 고급 배지",
  `데모 인벤토리 +${PLUS_INVENTORY_BONUS}칸`,
];

export function chipsToWon(chips: number) {
  return chips * CHIP_PRICE_WON;
}

export function showcaseLimit(plus: boolean) {
  return plus ? SHOWCASE_PLUS : SHOWCASE_BASE;
}

export function inventoryCapacity(plus: boolean, extraSlots: number) {
  return DEMO_INVENTORY_BASE + (plus ? PLUS_INVENTORY_BONUS : 0) + extraSlots;
}
