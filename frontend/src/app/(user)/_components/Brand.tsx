import { Black_Han_Sans, JetBrains_Mono } from "next/font/google";
import Link from "next/link";
import { LogoMark } from "@/app/_components/ui/LogoMark";
import styles from "./Brand.module.css";

const display = Black_Han_Sans({ weight: "400", subsets: ["latin"] });
const mono = JetBrains_Mono({ weight: "700", subsets: ["latin"] });

type Props = {
  caps?: boolean;
};

export function Brand({ caps = false }: Props) {
  return (
    <Link href="/main" className={styles.logo} aria-label="찍먹 dipsource 홈">
      <LogoMark size={44} className={styles.mark} />
      <span className={`${styles.brand} ${caps ? styles.caps : ""}`}>
        <strong className={display.className}>찍먹</strong>
        <span className={caps ? undefined : mono.className}>dipsource</span>
      </span>
    </Link>
  );
}
