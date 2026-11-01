import kit from "@/app/_components/ui/kit.module.css";

const map: Record<string, { label: string; className: string }> = {
  active: { label: "정상", className: kit.pillSuccess },
  suspended: { label: "정지", className: kit.pillDanger },
  withdrawn: { label: "탈퇴", className: kit.pill },
  visible: { label: "공개", className: kit.pillSuccess },
  hidden: { label: "숨김", className: kit.pill },
  pending: { label: "대기", className: kit.pillWarn },
  resolved: { label: "처리 완료", className: kit.pillSuccess },
  rejected: { label: "기각", className: kit.pill },
  done: { label: "확정", className: kit.pillSuccess },
  canceled: { label: "해지", className: kit.pill },
  charge: { label: "충전", className: kit.pillAccent },
  "refund-request": { label: "환불 요청", className: kit.pillWarn },
  refunded: { label: "환불 완료", className: kit.pill },
  high: { label: "위험", className: kit.pillDanger },
  medium: { label: "주의", className: kit.pillWarn },
};

export function StatusPill({ status }: { status: string }) {
  const item = map[status] ?? { label: status, className: kit.pill };
  return <span className={item.className}>{item.label}</span>;
}
