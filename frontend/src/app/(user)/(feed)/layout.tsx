import type { ReactNode } from "react";
import { LeftNav } from "../_components/LeftNav";
import { PresenceHeartbeat } from "../_components/PresenceHeartbeat";
import { RightAside } from "../_components/RightAside";
import { SearchBar } from "../_components/SearchBar";
import styles from "./layout.module.css";

export default function UserLayout({ children }: { children: ReactNode }) {
  return (
    <div className={styles.shell}>
      <LeftNav />
      <div className={styles.center}>
        <SearchBar />
        <main id="main-content" tabIndex={-1} className={styles.main}>
          {children}
        </main>
      </div>
      <RightAside />
      <PresenceHeartbeat />
    </div>
  );
}
