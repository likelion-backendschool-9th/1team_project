import Image from "next/image";

type Props = {
  size?: number;
  className?: string;
};

// 찍먹 화폐(칩) 아이콘: 한쪽이 말린 감자칩. 그림은 public/images/chip.png (배경 투명, 128px).
// 장식용이라 스크린리더에서는 숨기고, 옆에 "칩" 글자를 함께 둔다.
export function ChipIcon({ size = 20, className }: Props) {
  return (
    <Image
      src="/images/chip.png"
      width={size}
      height={size}
      alt=""
      aria-hidden="true"
      className={className}
      style={{ display: "inline-block", flex: "none", verticalAlign: "-0.2em" }}
    />
  );
}

// 충전 상품 그림: 99칩까지 감자칩 1장, 299칩까지 과자 봉지, 그 이상은 칩을 가득 담은 박스.
const PACK_IMAGES = ["/images/chip.png", "/images/chip-bag.png", "/images/chip-box.png"] as const;

export function chipPackLevel(chips: number) {
  if (chips <= 99) return 0;
  if (chips <= 299) return 1;
  return 2;
}

type PackProps = {
  chips: number;
  size?: number;
};

export function ChipPack({ chips, size = 56 }: PackProps) {
  return (
    <Image
      src={PACK_IMAGES[chipPackLevel(chips)]}
      width={size}
      height={size}
      alt=""
      aria-hidden="true"
      style={{ display: "block", flex: "none" }}
    />
  );
}
