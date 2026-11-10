"use client";

import { useState, useTransition, type FormEvent } from "react";
import kit from "@/app/_components/ui/kit.module.css";
import { formatWon } from "@/app/_lib/format";
import { changeSubscriptionPrice } from "../../_actions";
import styles from "./AuthorBlog.module.css";

type Props = {
  ownerId: number;
  price: number;
};

/** 내 블로그에서만 보이는 월 구독료 설정. 0원이면 구독 버튼이 숨겨진다. */
export function SubscriptionPriceForm({ ownerId, price }: Props) {
  const [draft, setDraft] = useState(String(price));
  const [result, setResult] = useState<{ ok: boolean; message: string } | null>(null);
  const [pending, startTransition] = useTransition();

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const next = Number(draft);
    startTransition(async () => {
      setResult(await changeSubscriptionPrice(ownerId, next));
    });
  }

  return (
    <section className={kit.card} aria-labelledby="subscription-price-title">
      <h2 id="subscription-price-title" className={kit.cardTitle}>
        구독료 설정
      </h2>
      <p className={kit.faint}>
        지금 {price > 0 ? `월 ${formatWon(price)}` : "구독을 받지 않아요"} · 0원으로 두면 구독하기 버튼이 보이지 않아요.
      </p>
      <form className={styles.priceForm} onSubmit={handleSubmit}>
        <label htmlFor="subscription-price" className="sr-only">
          월 구독료 (원)
        </label>
        <input
          id="subscription-price"
          type="number"
          inputMode="numeric"
          min={0}
          step={1}
          required
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
          className={`${kit.input} ${styles.priceInput}`}
          aria-describedby="subscription-price-result"
        />
        <span className={kit.muted}>원 / 월</span>
        <button type="submit" className={`${kit.btnPrimary} ${kit.sm}`} disabled={pending || draft === String(price)}>
          {pending ? "저장 중…" : "저장"}
        </button>
      </form>
      <p id="subscription-price-result" role="status" className={result?.ok === false ? kit.error : kit.faint}>
        {result?.message}
      </p>
    </section>
  );
}
