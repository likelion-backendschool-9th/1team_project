import Link from "next/link";
import { Avatar } from "@/app/_components/ui/Avatar";
import { ArrowRightIcon } from "@/app/_components/ui/Icons";
import { RelativeTime } from "@/app/_components/ui/RelativeTime";
import type { Feed } from "@/app/_lib/types/feed";
import { DemoThumb } from "./DemoThumb";
import { FeedActions } from "./FeedActions";
import { FeedMenu } from "./FeedMenu";
import { FollowButton } from "./FollowButton";
import { FeedText } from "./FeedText";
import styles from "./FeedCard.module.css";

type Props = {
  feed: Feed;
  myId?: number;
  now: number;
};

function imageLayoutClass(count: number) {
  const layouts = [styles.count1, styles.count2, styles.count3, styles.count4, styles.count5];
  return layouts[Math.min(Math.max(count, 1), layouts.length) - 1];
}

export function FeedCard({ feed, myId, now }: Props) {
  const { author, demo } = feed;

  return (
    <article className={styles.card} aria-label={`${author.nickname}의 피드`}>
      <header className={styles.header}>
        <Link href={`/mini-home/${author.id}`} aria-label={`${author.nickname} 미니홈피`}>
          <Avatar name={author.nickname} src={author.profileImage} size={40} />
        </Link>
        <div className={styles.author}>
          <Link href={`/mini-home/${author.id}`} className={styles.name}>
            {author.nickname}
          </Link>
          <p className={styles.meta}>
            {author.techStacks[0]} ·{" "}
            <RelativeTime iso={feed.createdAt} now={now} />
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

      <p className={styles.content}>
        <FeedText text={feed.content} />
      </p>

      {feed.imageUrls && feed.imageUrls.length > 0 && (
        <ul
          className={`${styles.images} ${imageLayoutClass(feed.imageUrls.length)}`}
          aria-label="첨부 이미지"
        >
          {feed.imageUrls.map((url, index) => (
            <li key={url}>
              <a href={url} target="_blank" rel="noopener noreferrer" className={styles.imageLink}>
                {/* 업로드 이미지(R2) 크기를 미리 알 수 없어 next/image 대신 img를 쓴다. */}
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src={url}
                  alt={`첨부 이미지 ${index + 1}`}
                  loading="lazy"
                  referrerPolicy="no-referrer"
                  className={styles.image}
                />
              </a>
            </li>
          ))}
        </ul>
      )}

      {feed.linkUrl && (
        <a href={feed.linkUrl} target="_blank" rel="noopener noreferrer nofollow" className={styles.link}>
          🔗 {feed.linkUrl}
        </a>
      )}

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
