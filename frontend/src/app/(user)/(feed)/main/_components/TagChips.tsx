import Link from "next/link";
import { DotsIcon } from "@/app/_components/ui/Icons";
import type { Tag } from "@/app/_lib/types/feed";
import styles from "./TagChips.module.css";

type Props = {
  tags: Tag[];
  activeTag: string | undefined;
};

export function TagChips({ tags, activeTag }: Props) {
  return (
    <nav aria-label="태그 필터">
      <ul className={styles.list}>
        <li>
          <Link
            href="/"
            className={`${styles.chip} ${!activeTag ? styles.active : ""}`}
            aria-current={!activeTag ? "page" : undefined}
          >
            전체
          </Link>
        </li>
        {tags.map((tag) => (
          <li key={tag.id}>
            <Link
              href={`/?tag=${encodeURIComponent(tag.name)}`}
              className={`${styles.chip} ${activeTag === tag.name ? styles.active : ""}`}
              aria-current={activeTag === tag.name ? "page" : undefined}
            >
              #{tag.name}
            </Link>
          </li>
        ))}
        <li>
          <button type="button" className={styles.chip} aria-label="태그 더보기" disabled>
            <DotsIcon />
          </button>
        </li>
      </ul>
    </nav>
  );
}
