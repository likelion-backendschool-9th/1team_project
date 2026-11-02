import { connection } from "next/server";
import { getMe } from "@/app/(user)/_lib/api";
import { LinkTabs } from "@/app/_components/ui/LinkTabs";
import {
  getFeeds,
  getHotDemos,
  getMyDemos,
  getServerTime,
  getTags,
  type FeedType,
} from "../_lib/api";
import { FeedCard } from "../../_components/FeedCard";
import { FeedComposer } from "./FeedComposer";
import { HotDemos } from "./HotDemos";
import { TagChips } from "./TagChips";
import styles from "./MainContent.module.css";

type Props = {
  searchParams: Promise<{ tag?: string | string[]; type?: string | string[] }>;
};

const TAG_PATTERN = /^[\w-]{1,30}$/;

function parseTag(value: string | string[] | undefined) {
  const tag = Array.isArray(value) ? value[0] : value;
  return tag && TAG_PATTERN.test(tag) ? tag : undefined;
}

export async function MainContent({ searchParams }: Props) {
  await connection();
  const params = await searchParams;
  const activeTag = parseTag(params.tag);
  const feedType: FeedType = params.type === "following" ? "following" : "recommended";
  const typeHref = (type: FeedType) => {
    const query = new URLSearchParams();
    if (activeTag) query.set("tag", activeTag);
    if (type === "following") query.set("type", type);
    const search = query.toString();
    return search ? `/main?${search}` : "/main";
  };
  const me = await getMe();
  const [tags, demos, feeds, now, myDemos] = await Promise.all([
    getTags(),
    getHotDemos(),
    getFeeds(activeTag, feedType),
    getServerTime(),
    getMyDemos(me.id),
  ]);

  return (
    <>
      <TagChips tags={tags} activeTag={activeTag} />
      <HotDemos demos={demos} />
      <FeedComposer nickname={me.nickname} myDemos={myDemos} />
      <section aria-labelledby="feed-list-title">
        <h2 id="feed-list-title" className="sr-only">
          피드 목록
        </h2>
        <div className={styles.tabs}>
          <LinkTabs
            label="피드 종류"
            variant="line"
            items={[
              { href: typeHref("recommended"), label: "추천", active: feedType === "recommended" },
              { href: typeHref("following"), label: "팔로잉", active: feedType === "following" },
            ]}
          />
        </div>
        {feeds.length === 0 ? (
          <p className={styles.empty}>
            {feedType === "following"
              ? "팔로잉한 개발자의 새 피드가 없어요."
              : "아직 피드가 없어요. 첫 소스를 공유해 보세요."}
          </p>
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
