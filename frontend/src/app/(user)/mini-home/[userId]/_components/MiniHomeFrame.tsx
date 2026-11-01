import { notFound } from "next/navigation";
import { connection } from "next/server";
import type { ReactNode } from "react";
import {
  getContributions,
  getLanguages,
  getProfile,
  getRepos,
  getVisitors,
} from "../_lib/api";
import { parseUserId } from "../_lib/parse";
import { MiniHomeHeader } from "./MiniHomeHeader";
import { MiniHomeTabs } from "./MiniHomeTabs";
import { ProfilePanel } from "./ProfilePanel";
import styles from "./MiniHomeFrame.module.css";

type Props = {
  params: Promise<{ userId: string }>;
  children: ReactNode;
};

// 미니홈피의 모든 탭이 같이 쓰는 틀: 방문자 헤더, 왼쪽 프로필, 오른쪽 탭 메뉴.
export async function MiniHomeFrame({ params, children }: Props) {
  await connection();
  const userId = parseUserId((await params).userId);
  if (!userId) notFound();

  const profile = await getProfile(userId);
  if (!profile) notFound();

  const [visitors, languages, contributions, repos] = await Promise.all([
    getVisitors(userId),
    getLanguages(userId),
    getContributions(userId),
    getRepos(userId),
  ]);

  return (
    <div className={styles.wrap}>
      <MiniHomeTabs userId={userId} />
      <div className={styles.frame}>
        <MiniHomeHeader profile={profile} visitors={visitors} />
        <div className={styles.grid}>
          <ProfilePanel
            profile={profile}
            languages={languages}
            contributions={contributions}
            repos={repos}
          />
          <div className={styles.content}>{children}</div>
        </div>
      </div>
    </div>
  );
}
