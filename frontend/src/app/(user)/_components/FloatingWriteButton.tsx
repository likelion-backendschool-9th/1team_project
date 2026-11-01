import Link from "next/link";
import { PlusIcon } from "@/app/_components/ui/Icons";
import styles from "./FloatingWriteButton.module.css";

export function FloatingWriteButton() {
  return (
    <Link href="/main#feed-composer" className={styles.button}>
      <PlusIcon />
      피드 쓰기
    </Link>
  );
}
