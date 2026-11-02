import { mockUsers } from "@/app/(user)/_lib/mock";
import type { DemoComment, DemoDetail } from "@/app/_lib/types/demo";
import { createUserDemos } from "@/app/_lib/mockMiniDemos";
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

function toDetail(
  [title, description, techTags, author, likeCount, playCount, hoursAgo]: Row,
  id: number,
): DemoDetail {
  const runtime: DemoDetail["runtime"] = techTags.includes("unity") ? "unity" : "web";
  const index = id - 1;
  return {
    id,
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
}

// 정시우 미니홈피 데모. 미니홈피 목 데이터와 같은 id(101~)를 쓴다.
const siwooRows: Row[] = [
  ["Interactive Neon Particle Swarm", "마우스 반발력과 물리 기반 가속도를 시뮬레이션한 파티클 인터랙션 데모입니다.", ["canvas", "webgl"], mockUsers.jung, 42, 318, 70],
  ["실시간 WebAudio 스펙트럼 뷰어", "마이크 입력 주파수를 파티클로 전환하는 시각화 도구", ["webaudio"], mockUsers.jung, 29, 214, 120],
  ["초경량 글래스모피즘 컴포넌트", "Zero-dependency 반응형 CSS 블러 디자인 킷", ["css"], mockUsers.jung, 53, 502, 200],
  ["GLSL 노이즈 셰이더 놀이터", "Perlin·Simplex 노이즈 파라미터를 실시간으로 조절해 보는 셰이더 데모", ["glsl"], mockUsers.jung, 21, 156, 260],
  ["드래그 앤 드롭 칸반 보드", "의존성 없이 Pointer Events로 만든 칸반 보드", ["typescript"], mockUsers.jung, 18, 133, 300],
  ["CSS 3D 카드 플립 갤러리", "transform-style: preserve-3d로 만든 카드 갤러리", ["css"], mockUsers.jung, 14, 97, 340],
];

// 내 미니홈피 데모. 정시우 데모를 본뜬 것으로, 미니홈피 목 데이터와 같은 id(201~)를 쓴다.
const myRows: Row[] = siwooRows.map(([title, description, techTags, , likeCount, playCount, hoursAgo]) => [
  title,
  description,
  techTags,
  mockUsers.me,
  likeCount,
  playCount,
  hoursAgo,
]);

// 다른 회원들의 미니홈피 마스코트 데모 (id: 회원 id × 1000 + 순번)
const memberDemos: DemoDetail[] = Object.values(mockUsers)
  .flatMap((user) => createUserDemos(user))
  .map((demo, index) => ({
    ...demo,
    createdAt: new Date(NOW - (80 + index * 9) * HOUR).toISOString(),
    runtime: demo.engine.startsWith("Unity") ? "unity" : "web",
    sizeMb: 1.4 + (index % 5) * 0.5,
    liked: false,
    bookmarked: false,
  }));

export const mockDemos: DemoDetail[] = [
  ...rows.map((row, index) => toDetail(row, index + 1)),
  ...siwooRows.map((row, index) => toDetail(row, 101 + index)),
  ...myRows.map((row, index) => toDetail(row, 201 + index)),
  ...memberDemos,
];

export const mockDemoTags = ["spring", "react", "typescript", "unity", "game", "canvas", "css", "webaudio"];

export function createMockDemoComments(demoId: number, now: number): DemoComment[] {
  const rows: [UserSummary, DemoComment["type"], string, number, number][] = [
    [mockUsers.kwak, "praise", "인터랙션이 정말 부드러워요. 코드 구조도 궁금합니다!", 30, 6],
    [mockUsers.jinyeop, "question", "모바일 사파리에서도 동작하나요?", 75, 2],
    [mockUsers.junha, "bug", "창 크기를 줄이면 오른쪽 패널이 잘려 보여요.", 180, 1],
    [mockUsers.me, "question", "Unity 버전은 몇 쓰셨어요?", 240, 0],
  ];
  const comments: DemoComment[] = rows.map(([author, type, content, minutesAgo, likeCount], index) => ({
    id: demoId * 100 + index,
    author,
    type,
    content,
    createdAt: new Date(now - minutesAgo * 60_000).toISOString(),
    likeCount,
    liked: index === 0,
  }));
  comments[1].replies = [
    {
      id: demoId * 100 + 50,
      author: mockUsers.seungjo,
      type: "praise",
      content: "@김진엽 iOS 17 사파리에서 확인했어요. 터치 이벤트도 잘 받아요!",
      createdAt: new Date(now - 60 * 60_000).toISOString(),
      likeCount: 1,
      liked: false,
    },
  ];
  return comments;
}
