import { mockUsers } from "@/app/(user)/_lib/mock";
import type { DemoComment, DemoDetail } from "@/app/_lib/types/demo";
import type { UserSummary } from "@/app/_lib/types/user";

// 제목, 설명, 태그, 작성자, 좋아요, 실행 수, 몇 시간 전
type Row = [string, string, string[], UserSummary, number, number, number];

const rows: Row[] = [
  ["실시간 채팅앱", "WebSocket + STOMP 기반 실시간 채팅 데모입니다. 방 만들기와 입장 알림까지 지원해요.", ["websocket", "spring"], mockUsers.cha, 42, 318, 2],
  ["날씨 대시보드", "공공 API와 React 19 Suspense로 만든 날씨 대시보드입니다.", ["react", "typescript"], mockUsers.kwak, 31, 254, 5],
  ["오목 게임", "Unity WebGL로 만든 2인 오목 게임. 빌드 용량을 9MB로 줄였어요.", ["unity", "game"], mockUsers.seungjo, 27, 199, 8],
  ["투두 리스트", "바닐라 JS로 만든 드래그 정렬 투두 리스트입니다.", ["javascript"], mockUsers.junha, 19, 142, 12],
  ["픽셀 아트 캔버스", "Canvas API로 만든 32x32 픽셀 드로잉 툴입니다.", ["canvas"], mockUsers.jinyeop, 17, 120, 20],
  ["타자 연습 게임", "코드 스니펫으로 타자 속도를 재는 게임입니다.", ["typescript", "game"], mockUsers.seungjo, 15, 133, 26],
  ["Three.js 태양계", "행성 궤도를 3D로 돌려보는 Three.js 데모입니다.", ["three.js"], mockUsers.cha, 12, 88, 30],
  ["마크다운 에디터", "실시간 미리보기와 코드 하이라이트가 되는 에디터입니다.", ["react"], mockUsers.kwak, 24, 176, 34],
  ["Spring Batch 모니터", "배치 잡 실행 현황을 시각화한 대시보드 데모입니다.", ["spring", "java"], mockUsers.me, 33, 210, 40],
  ["사운드 시퀀서", "Web Audio API로 만든 16스텝 드럼 시퀀서입니다.", ["webaudio"], mockUsers.junha, 21, 160, 48],
  ["플랫포머 미니게임", "Unity로 만든 3스테이지 플랫포머 게임입니다.", ["unity", "game"], mockUsers.seungjo, 29, 230, 55],
  ["CSS 로딩 컬렉션", "순수 CSS 로딩 애니메이션 30종 모음입니다.", ["css"], mockUsers.jinyeop, 38, 402, 62],
];

const NOW = Date.UTC(2026, 10, 2, 9);
const HOUR = 3_600_000;

export const mockDemos: DemoDetail[] = rows.map(
  ([title, description, techTags, author, likeCount, playCount, hoursAgo], index) => {
    const runtime: DemoDetail["runtime"] = techTags.includes("unity") ? "unity" : "web";
    return {
      id: index + 1,
      title,
      description,
      thumbnail: null,
      techTags,
      author,
      likeCount,
      playCount,
      commentCount: (likeCount % 7) + 3,
      createdAt: new Date(NOW - hoursAgo * HOUR).toISOString(),
      runtime,
      sizeMb: runtime === "unity" ? 9.4 : 1.2 + (index % 4) * 0.6,
      liked: false,
      bookmarked: false,
    };
  },
);

export const mockDemoTags = ["spring", "react", "typescript", "unity", "game", "canvas", "css", "webaudio"];

export function createMockDemoComments(demoId: number, now: number): DemoComment[] {
  const rows: [UserSummary, DemoComment["type"], string, number, number][] = [
    [mockUsers.kwak, "praise", "인터랙션이 정말 부드러워요. 코드 구조도 궁금합니다!", 30, 6],
    [mockUsers.jinyeop, "question", "모바일 사파리에서도 동작하나요?", 75, 2],
    [mockUsers.junha, "bug", "창 크기를 줄이면 오른쪽 패널이 잘려 보여요.", 180, 1],
  ];
  return rows.map(([author, type, content, minutesAgo, likeCount], index) => ({
    id: demoId * 100 + index,
    author,
    type,
    content,
    createdAt: new Date(now - minutesAgo * 60_000).toISOString(),
    likeCount,
  }));
}
