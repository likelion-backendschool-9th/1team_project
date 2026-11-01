import { formatCount } from "@/app/_lib/format";
import type {
  GithubContributions,
  GithubLanguage,
  GithubRepo,
  MiniHomeProfile,
} from "@/app/_lib/types/miniHome";
import { UserBadges } from "@/app/(user)/_components/UserBadges";
import styles from "./ProfilePanel.module.css";

type Props = {
  profile: MiniHomeProfile;
  languages: GithubLanguage[];
  contributions: GithubContributions;
  repos: GithubRepo[];
};

export function ProfilePanel({ profile, languages, contributions, repos }: Props) {
  const top = languages[0];

  return (
    <section className={styles.panel} aria-label={`${profile.nickname} 프로필`}>
      <div className={styles.avatar}>
        <span className={styles.avatarName}>{profile.nickname}</span>
      </div>
      <h2 className={styles.name}>{profile.nickname}</h2>
      <UserBadges badges={profile.badges} align="center" />

      {profile.bgm && (
        <p className={styles.bgm}>
          <span className={styles.note} aria-hidden="true">
            ♬
          </span>
          <span className={styles.bgmTitle}>
            <span className="sr-only">배경음악 </span>
            {profile.bgm}
          </span>
          <span className={styles.pause} aria-hidden="true" />
        </p>
      )}

      {profile.quote && <blockquote className={styles.quote}>{profile.quote}</blockquote>}

      <h3 className={styles.label}>STACK TAGS</h3>
      <ul className={styles.tags}>
        {profile.stackTags.map((tag) => (
          <li key={tag}>#{tag}</li>
        ))}
      </ul>

      {top && (
        <>
          <div className={styles.row}>
            <h3 className={styles.rowLabel}>언어 사용 통계</h3>
            <span className={styles.rowValue}>
              {top.name} {top.percent}%
            </span>
          </div>
          <div
            className={styles.bar}
            role="img"
            aria-label={languages.map((lang) => `${lang.name} ${lang.percent}%`).join(", ")}
          >
            {languages.map((lang, index) => (
              <span
                key={lang.name}
                className={styles[`bar${Math.min(index, 2)}`]}
                style={{ width: `${lang.percent}%` }}
              />
            ))}
          </div>
        </>
      )}

      <div className={styles.row}>
        <h3 className={styles.rowLabel}>깃허브 잔디 (스트릭)</h3>
        <span className={styles.commits}>
          {formatCount(contributions.totalCommits)} COMMITS
        </span>
      </div>
      <div
        className={styles.grass}
        role="img"
        aria-label={`최근 ${contributions.days.length}일 활동, 총 ${contributions.totalCommits}커밋`}
      >
        {contributions.days.map((day) => (
          <span key={day.date} className={styles[`level${day.level}`]} />
        ))}
      </div>

      <h3 className={`${styles.rowLabel} ${styles.repoLabel}`}>대표 레포지토리</h3>
      <ul className={styles.repos}>
        {repos.map((repo) => (
          <li key={repo.name}>
            <span className={styles.repoName}>{repo.name}</span>
            <span className={styles.stars}>
              <span aria-hidden="true">★</span>
              <span className="sr-only">스타</span> {formatCount(repo.stars)}
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}
