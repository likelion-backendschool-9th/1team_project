import { formatCount } from "@/app/_lib/format";
import type {
  GithubContributions,
  GithubLanguage,
  GithubRepo,
  MiniHomeProfile,
} from "@/app/_lib/types/miniHome";
import { UserBadges } from "@/app/(user)/_components/UserBadges";
import { BgmPlayer } from "./BgmPlayer";
import styles from "./ProfilePanel.module.css";

// 잔디는 GitHub처럼 세로 7칸(일~토) · 가로 주 단위로 그린다. 패널 폭에 맞게 최근 20주만 보여 준다.
const GRASS_WEEKS = 20;

type GrassCell = { date: string; level: number } | null;

/** 최근 GRASS_WEEKS주를 일요일부터 시작하도록 앞을 빈칸으로 채운다. (날짜는 YYYY-MM-DD) */
function toGrassCells(days: GithubContributions["days"]): GrassCell[] {
  if (days.length === 0) return [];
  const lastDay = new Date(`${days[days.length - 1].date}T00:00:00Z`).getUTCDay();
  // 마지막 주는 오늘 요일까지만 있다. 그 앞 (GRASS_WEEKS - 1)주는 꽉 채운다.
  const recent = days.slice(-((GRASS_WEEKS - 1) * 7 + lastDay + 1));
  const firstDay = new Date(`${recent[0].date}T00:00:00Z`).getUTCDay();
  return [...Array.from({ length: firstDay }, () => null), ...recent];
}

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
        {profile.profileImage ? (
          // 소셜·R2 등 여러 곳의 주소라 next/image 대신 img를 쓴다. 바로 아래에 닉네임이 나와서 alt는 비운다
          // eslint-disable-next-line @next/next/no-img-element
          <img src={profile.profileImage} alt="" className={styles.avatarImage} referrerPolicy="no-referrer" />
        ) : (
          <span className={styles.avatarName}>{profile.nickname}</span>
        )}
      </div>
      <h2 className={styles.name}>{profile.nickname}</h2>
      {(profile.plus || profile.company) && (
        <p className={styles.idTags}>
          {profile.plus && <span className={styles.plusTag}>PLUS</span>}
          {profile.company && <span className={styles.company}>🏢 {profile.company}</span>}
        </p>
      )}
      <UserBadges badges={profile.badges} align="center" />

      {profile.plus && profile.bgm && <BgmPlayer trackId={profile.bgm} />}

      {profile.quote && <blockquote className={styles.quote}>{profile.quote}</blockquote>}

      <h3 className={styles.label}>STACK TAGS</h3>
      {profile.stackTags.length === 0 ? (
        <p className={styles.emptyNote}>아직 등록한 기술 스택이 없어요.</p>
      ) : (
        <ul className={styles.tags}>
          {profile.stackTags.map((tag) => (
            <li key={tag}>#{tag}</li>
          ))}
        </ul>
      )}

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
      {contributions.days.length === 0 ? (
        // GitHub 계정이 없거나 활동을 못 불러오면 빈 칸 대신 안내만 보여 준다.
        <p className={styles.emptyNote}>GitHub 활동이 아직 없어요. GitHub으로 로그인하면 잔디가 보여요.</p>
      ) : (
        <div
          className={styles.grass}
          role="img"
          aria-label={`최근 ${GRASS_WEEKS}주 활동, 총 ${contributions.totalCommits}커밋`}
        >
          {toGrassCells(contributions.days).map((day, index) =>
            day ? (
              <span key={day.date} className={styles[`level${day.level}`]} title={day.date} />
            ) : (
              <span key={`empty-${index}`} className={styles.grassEmpty} />
            ),
          )}
        </div>
      )}

      {repos.length > 0 && (
        <>
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
        </>
      )}
    </section>
  );
}
