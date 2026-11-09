import Link from "next/link";
import type { ReactNode } from "react";
import styles from "./FeedText.module.css";

// 백엔드 FeedService.HASHTAG와 같은 규칙. 단어 중간의 #(C#, a#b)이나 URL 조각(#section)은 태그로 보지 않는다.
const HASHTAG = /(?<![\p{L}\p{N}_&/#])#([\p{L}\p{N}_][\p{L}\p{N}_.+-]{0,49})/gu;

type Props = {
  text: string;
};

// 피드 본문의 #해시태그를 그 태그 피드(/main?tag=)로 가는 링크로 바꾼다.
export function FeedText({ text }: Props) {
  const nodes: ReactNode[] = [];
  let cursor = 0;
  for (const match of text.matchAll(HASHTAG)) {
    const start = match.index ?? 0;
    const name = match[1].replace(/[.+-]+$/, "");
    if (!name) continue;
    if (start > cursor) nodes.push(text.slice(cursor, start));
    nodes.push(
      <Link key={`${start}-${name}`} href={`/main?tag=${encodeURIComponent(name.toLowerCase())}`} className={styles.tag}>
        #{name}
      </Link>,
    );
    cursor = start + 1 + name.length;
  }
  if (cursor < text.length) nodes.push(text.slice(cursor));
  return <>{nodes}</>;
}
