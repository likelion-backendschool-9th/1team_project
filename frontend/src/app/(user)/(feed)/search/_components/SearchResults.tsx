import { Avatar } from "@/app/_components/ui/Avatar";
import kit from "@/app/_components/ui/kit.module.css";
import type { SearchResult, SearchType } from "../_lib/api";
import { SearchResultRow } from "./SearchResultRow";

type Props = {
  result: SearchResult;
  type: SearchType;
  query: string;
  canHide: boolean;
};

/** 대상별 검색 결과 묶음. 대상끼리 점수를 섞지 않는다 */
export function SearchResults({ result, type, query, canHide }: Props) {
  const show = (target: SearchType) => (type === "all" || type === target);
  const row = { query, canHide };

  return (
    <>
      {show("post") && result.posts.items.length > 0 && (
        <section className={kit.card} aria-labelledby="result-posts">
          <h2 id="result-posts" className={kit.cardTitle}>
            블로그
            {result.posts.relaxed && <span className={kit.pill} style={{ marginLeft: 8 }}>일부 일치</span>}
          </h2>
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
          <h2 id="result-demos" className={kit.cardTitle}>
            데모
            {result.demos.relaxed && <span className={kit.pill} style={{ marginLeft: 8 }}>일부 일치</span>}
          </h2>
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
          <h2 id="result-users" className={kit.cardTitle}>
            사용자
            {result.users.relaxed && <span className={kit.pill} style={{ marginLeft: 8 }}>일부 일치</span>}
          </h2>
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
          <h2 id="result-feeds" className={kit.cardTitle}>
            피드
            {result.feeds.relaxed && <span className={kit.pill} style={{ marginLeft: 8 }}>일부 일치</span>}
          </h2>
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
