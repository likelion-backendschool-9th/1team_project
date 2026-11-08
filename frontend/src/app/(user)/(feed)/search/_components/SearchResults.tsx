import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import kit from "@/app/_components/ui/kit.module.css";
import type { SearchResult, SearchType } from "../_lib/api";
import { SearchResultRow } from "./SearchResultRow";
import styles from "./SearchContent.module.css";

type Props = {
  result: SearchResult;
  type: SearchType;
  query: string;
  canHide: boolean;
  /** 대상 탭 주소 */
  tabHref: (type: SearchType) => string;
};

type HeadingProps = {
  id: string;
  label: string;
  relaxed?: boolean;
  /** 전체 탭에서만 준다 */
  moreHref?: string;
};

function SectionHeading({ id, label, relaxed, moreHref }: HeadingProps) {
  return (
    <div className={styles.sectionHead}>
      <h2 id={id} className={kit.cardTitle}>
        {label}
        {relaxed && <span className={kit.pill} style={{ marginLeft: 8 }}>일부 일치</span>}
      </h2>
      {moreHref && (
        <Link href={moreHref} className={kit.more}>
          {label} 전체 검색결과 보기 →
        </Link>
      )}
    </div>
  );
}

/** 대상별 검색 결과 묶음. 대상끼리 점수를 섞지 않는다 */
export function SearchResults({ result, type, query, canHide, tabHref }: Props) {
  const show = (target: SearchType) => (type === "all" || type === target);
  const row = { query, canHide };
  const more = (target: SearchType) => (type === "all" ? tabHref(target) : undefined);

  return (
    <>
      {show("post") && result.posts.items.length > 0 && (
        <section className={kit.card} aria-labelledby="result-posts">
          <SectionHeading id="result-posts" label="블로그" relaxed={result.posts.relaxed} moreHref={more("post")} />
          <ul className={kit.list}>
            {result.posts.items.map((post) => (
              <SearchResultRow key={post.id} href={`/blog/posts/${post.id}`} type="post" id={post.id} {...row}>
                <span aria-hidden="true">📝</span>
                <span className={kit.grow}>
                  <strong>{post.title}</strong>
                  <span className={`${kit.muted} ${kit.ellipsis}`} style={{ display: "block", fontSize: 14 }}>
                    {post.author}
                    {post.excerpt ? ` · ${post.excerpt}` : ""}
                  </span>
                </span>
                <span className={post.price > 0 ? kit.pillAccent : kit.pill}>
                  {post.price > 0 ? `🔒 ${post.price}칩` : "무료"}
                </span>
              </SearchResultRow>
            ))}
          </ul>
        </section>
      )}
      {show("demo") && result.demos.items.length > 0 && (
        <section className={kit.card} aria-labelledby="result-demos">
          <SectionHeading id="result-demos" label="데모" relaxed={result.demos.relaxed} moreHref={more("demo")} />
          <ul className={kit.list}>
            {result.demos.items.map((demo) => (
              <SearchResultRow key={demo.id} href={`/demos/${demo.id}`} type="demo" id={demo.id} {...row}>
                <span aria-hidden="true">🧪</span>
                <span className={kit.grow}>
                  <strong>{demo.title}</strong>
                  <span className={kit.faint}> · {demo.author}</span>
                  <span className={`${kit.muted} ${kit.ellipsis}`} style={{ display: "block", fontSize: 14 }}>
                    {demo.description}
                  </span>
                </span>
                <span className={kit.pill}>♥ {demo.likeCount}</span>
              </SearchResultRow>
            ))}
          </ul>
        </section>
      )}
      {show("user") && result.users.items.length > 0 && (
        <section className={kit.card} aria-labelledby="result-users">
          <SectionHeading id="result-users" label="사용자" relaxed={result.users.relaxed} moreHref={more("user")} />
          <ul className={kit.list}>
            {result.users.items.map((user) => (
              <SearchResultRow key={user.id} href={`/mini-home/${user.id}`} type="user" id={user.id} {...row}>
                <Avatar name={user.nickname} size={40} />
                <span className={kit.grow}>
                  <strong>{user.nickname}</strong>
                  <span className={kit.faint}> · {user.techStacks.join(", ")}</span>
                </span>
                <span className={kit.more}>미니홈피 →</span>
              </SearchResultRow>
            ))}
          </ul>
        </section>
      )}
      {show("feed") && result.feeds.items.length > 0 && (
        <section className={kit.card} aria-labelledby="result-feeds">
          <SectionHeading id="result-feeds" label="피드" relaxed={result.feeds.relaxed} moreHref={more("feed")} />
          <ul className={kit.list}>
            {result.feeds.items.map((feed) => (
              <SearchResultRow key={feed.id} href={`/feeds/${feed.id}`} type="feed" id={feed.id} {...row}>
                <span aria-hidden="true">💬</span>
                <span className={kit.grow}>
                  <span className={kit.ellipsis} style={{ display: "block" }}>{feed.content}</span>
                  <span className={kit.faint}>{feed.author}</span>
                </span>
                <span className={kit.pill}>♥ {feed.likeCount}</span>
              </SearchResultRow>
            ))}
          </ul>
        </section>
      )}
    </>
  );
}
