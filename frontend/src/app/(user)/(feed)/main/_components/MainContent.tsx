import { connection } from "next/server";
import { getMe } from "@/app/(user)/_lib/api";
import { getFeeds, getHotDemos, getServerTime, getTags } from "../_lib/api";
import { FeedCard } from "../../_components/FeedCard";
import { FeedComposer } from "./FeedComposer";
import { HotDemos } from "./HotDemos";
import { TagChips } from "./TagChips";
import styles from "./MainContent.module.css";

type Props = {
  searchParams: Promise<{ tag?: string | string[] }>;
};

const TAG_PATTERN = /^[\w-]{1,30}$/;

function parseTag(value: string | string[] | undefined) {
  const tag = Array.isArray(value) ? value[0] : value;
  return tag && TAG_PATTERN.test(tag) ? tag : undefined;
}

export async function MainContent({ searchParams }: Props) {
  await connection();
  const activeTag = parseTag((await searchParams).tag);
  const [me, tags, demos, feeds, now] = await Promise.all([
    getMe(),
    getTags(),
    getHotDemos(),
    getFeeds(activeTag),
    getServerTime(),
  ]);

  return (
    <>
      <TagChips tags={tags} activeTag={activeTag} />
      <HotDemos demos={demos} />
      <FeedComposer nickname={me.nickname} />
      <section aria-labelledby="feed-list-title">
        <h2 id="feed-list-title" className="sr-only">
          피드 목록
        </h2>
        {feeds.length === 0 ? (
          <p className={styles.empty}>아직 피드가 없어요. 첫 소스를 공유해 보세요.</p>
        ) : (
          <ul className={styles.list}>
            {feeds.map((feed) => (
              <li key={feed.id}>
                <FeedCard feed={feed} myId={me.id} now={now} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
