import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ArrowRightIcon } from "@/app/_components/ui/Icons";
import { formatRelativeTime } from "@/app/_lib/format";
import type { Feed } from "@/app/_lib/types/feed";
import { DemoThumb } from "./DemoThumb";
import { FeedActions } from "./FeedActions";
import { FeedMenu } from "./FeedMenu";
import { FollowButton } from "./FollowButton";
import styles from "./FeedCard.module.css";

type Props = {
  feed: Feed;
  myId: number;
  now: number;
};

export function FeedCard({ feed, myId, now }: Props) {
  const { author, demo } = feed;

  return (
    <article className={styles.card} aria-label={`${author.nickname}의 피드`}>
      <header className={styles.header}>
        <Link href={`/mini-home/${author.id}`} aria-label={`${author.nickname} 미니홈피`}>
          <Avatar name={author.nickname} size={52} />
        </Link>
        <div className={styles.author}>
          <Link href={`/mini-home/${author.id}`} className={styles.name}>
            {author.nickname}
          </Link>
          <p className={styles.meta}>
            {author.techStacks[0]} ·{" "}
            <time dateTime={feed.createdAt}>
              {formatRelativeTime(feed.createdAt, now)}
            </time>
          </p>
        </div>
        {author.id !== myId && (
          <FollowButton
            userId={author.id}
            nickname={author.nickname}
            following={feed.following}
          />
        )}
        <FeedMenu
          feedId={feed.id}
          authorId={author.id}
          authorName={author.nickname}
          mine={author.id === myId}
        />
      </header>

      <p className={styles.content}>{feed.content}</p>

      {demo && (
        <div className={styles.demo}>
          <DemoThumb seed={demo.id - 1} thumbnail={demo.thumbnail} className={styles.demoThumb} />
          <div className={styles.demoInfo}>
            <h3 className={styles.demoTitle}>{demo.title}</h3>
            <p className={styles.demoDesc}>{demo.description}</p>
            <ul className={styles.tags}>
              {demo.techTags.map((tag) => (
                <li key={tag}>{tag}</li>
              ))}
            </ul>
          </div>
          <Link href={`/demos/${demo.id}`} className={styles.play}>
            찍먹하기
            <ArrowRightIcon />
          </Link>
        </div>
      )}

      <FeedActions
        feedId={feed.id}
        likeCount={feed.likeCount}
        commentCount={feed.commentCount}
        repostCount={feed.repostCount}
        liked={feed.liked}
        reposted={feed.reposted}
        bookmarked={feed.bookmarked}
      />
    </article>
  );
}
