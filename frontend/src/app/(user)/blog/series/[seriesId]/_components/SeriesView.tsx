import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ConfirmButton } from "@/app/_components/ui/ConfirmButton";
import kit from "@/app/_components/ui/kit.module.css";
import { formatCount, formatDate, formatWon } from "@/app/_lib/format";
import { getMe } from "@/app/(user)/_lib/api";
import { deleteSeries, purchaseSeries, updateSeries } from "../../../_actions";
import { SeriesForm } from "../../../_components/SeriesForm";
import { getSeries } from "../../../_lib/api";
import styles from "./SeriesView.module.css";

type Props = {
  params: Promise<{ seriesId: string }>;
};

/** 시리즈 화면. 시리즈 정보·구매 버튼·회차 목록. 작가 본인이면 수정·삭제도 한다 */
export async function SeriesView({ params }: Props) {
  await connection();
  const seriesId = Number((await params).seriesId);
  const detail = Number.isSafeInteger(seriesId) && seriesId > 0 ? await getSeries(seriesId) : null;
  if (!detail) notFound();

  const me = await getMe();
  const { series, episodes, purchased, mine } = detail;
  const paidCount = episodes.filter((episode) => episode.paid).length;

  return (
    <div className={kit.stack}>
      <Link href={`/blog/${series.author.id}`} className={kit.more}>
        ← {series.author.nickname}의 블로그
      </Link>

      <section className={`${kit.card} ${styles.hero}`} aria-labelledby="series-name">
        <div className={kit.row}>
          <span className={kit.pillWarn}>{series.status === "completed" ? "완결" : "연재 중"}</span>
          <span className={kit.pill}>총 {episodes.length}화</span>
          {series.price > 0 && <span className={kit.pillSuccess}>무료 {episodes.length - paidCount}화</span>}
        </div>
        <h2 id="series-name" className={styles.title}>
          {series.title}
        </h2>
        {series.description && <p className={kit.muted}>{series.description}</p>}
        <Link href={`/blog/${series.author.id}`} className={styles.author}>
          <Avatar name={series.author.nickname} size={32} />
          <span>{series.author.nickname}</span>
        </Link>

        <div className={styles.buy}>
          {series.price <= 0 ? (
            <p className={kit.strong}>무료 시리즈예요. 모든 회차를 누구나 읽을 수 있어요.</p>
          ) : mine ? (
            <p className={kit.strong}>
              판매 가격 {formatWon(series.price)} · 산 사람 {formatCount(series.purchaseCount ?? 0)}명
            </p>
          ) : purchased ? (
            <p className={kit.pillSuccess}>✓ 구매한 시리즈 · 모든 회차를 읽을 수 있어요</p>
          ) : me ? (
            <>
              <p className={kit.strong}>
                {formatWon(series.price)} · 한 번 사면 이후 올라오는 회차까지 계속 읽어요
              </p>
              <ConfirmButton
                label={`시리즈 구매 · ${formatWon(series.price)}`}
                title={`「${series.title}」 시리즈를 살까요?`}
                description={`${formatWon(series.price)}이 결제돼요. 칩은 차감되지 않아요. 유료 회차 ${paidCount}편과 앞으로 올라올 회차를 모두 읽을 수 있어요.`}
                confirmLabel="구매하기"
                className={kit.btnPrimary}
                onConfirm={purchaseSeries.bind(null, series.id)}
              />
            </>
          ) : (
            <Link href="/login" className={kit.btnPrimary}>
              로그인하고 시리즈 구매하기
            </Link>
          )}
        </div>
      </section>

      <section className={kit.card} aria-labelledby="episodes-title">
        <h2 id="episodes-title" className={kit.cardTitle}>
          회차 {episodes.length}
        </h2>
        {episodes.length === 0 ? (
          <p className={kit.empty}>
            {mine ? "아직 회차가 없어요. 글쓰기에서 이 시리즈를 고르면 회차로 붙어요." : "아직 올라온 회차가 없어요."}
          </p>
        ) : (
          <ol className={kit.list}>
            {episodes.map((episode) => {
              const locked = episode.paid && !purchased;
              return (
                <li key={episode.postId} className={kit.listItem}>
                  <Link href={`/blog/posts/${episode.postId}`} className={styles.episode}>
                    <span className={styles.order}>{episode.order}화</span>
                    <span className={kit.grow}>
                      <strong>{episode.title}</strong>
                      <time dateTime={episode.createdAt} className={kit.faint} style={{ display: "block" }}>
                        {formatDate(episode.createdAt)}
                      </time>
                    </span>
                    {locked ? (
                      <span className={kit.pillWarn}>
                        <span aria-hidden="true">🔒</span> 구매자 전용
                      </span>
                    ) : (
                      !episode.paid && <span className={kit.pillSuccess}>무료</span>
                    )}
                  </Link>
                </li>
              );
            })}
          </ol>
        )}
        {mine && (
          <Link href="/blog/write" className={kit.more} style={{ marginTop: 12 }}>
            + 다음 회차 쓰기
          </Link>
        )}
      </section>

      {mine && (
        <section className={kit.card} aria-labelledby="series-edit-title">
          <h2 id="series-edit-title" className={kit.cardTitle}>
            시리즈 수정
          </h2>
          <SeriesForm
            action={updateSeries.bind(null, series.id)}
            editing
            submitLabel="저장"
            initial={{
              title: series.title,
              description: series.description,
              price: String(series.price),
              status: series.status,
            }}
          />
          <div className={styles.danger}>
            <ConfirmButton
              label="시리즈 삭제"
              title="시리즈를 지울까요?"
              description="회차가 남아 있거나 산 사람이 있으면 지울 수 없어요."
              confirmLabel="삭제"
              danger
              className={`${kit.btnGhost} ${kit.sm}`}
              onConfirm={deleteSeries.bind(null, series.id, series.author.id)}
            />
          </div>
        </section>
      )}
    </div>
  );
}
