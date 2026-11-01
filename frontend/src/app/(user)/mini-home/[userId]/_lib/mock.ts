import { mockBadges, mockUsers } from "@/app/(user)/_lib/mock";
import type { UserSummary } from "@/app/_lib/types/user";
import type {
  GithubContributions,
  GithubLanguage,
  GithubRepo,
  MiniHomeComment,
  MiniHomeDemo,
  MiniHomePost,
  MiniHomeProfile,
  VisitorStats,
} from "@/app/_lib/types/miniHome";

const siwooProfile: MiniHomeProfile = {
  ...mockUsers.jung,
  badges: mockBadges[6],
  headline: "Interactivity Explorer",
  domain: "siwoo.dev",
  bgm: "NewJeans - Ditto (8-bit lofi remix)",
  quote: "“픽셀은 거짓말을 안 한다.\n프레임레이트는 감동이다.” ⚡",
  stackTags: ["WebGL", "ThreeJS", "TypeScript", "React", "GLSL"],
  featuredDemoId: 11,
  featuredPostId: 302,
  following: false,
};

// 목 모드에서는 정시우 홈피를 기준으로, 다른 회원은 이름과 소개만 바꿔 보여준다.
export function findMockProfile(userId: number): MiniHomeProfile | null {
  if (userId === siwooProfile.id) return siwooProfile;
  const user = Object.values(mockUsers).find((item) => item.id === userId);
  if (!user) return null;
  return {
    ...siwooProfile,
    ...user,
    badges: mockBadges[user.id] ?? [],
    headline: "Dip Source Maker",
    domain: null,
  };
}

export const mockVisitors: VisitorStats = { today: 142, total: 4892 };

export const mockLanguages: GithubLanguage[] = [
  { name: "TS", percent: 72 },
  { name: "GLSL", percent: 12 },
  { name: "기타", percent: 16 },
];

const grassLevels = [
  2, 3, 3, 0, 2, 3, 4, 2, 2, 3,
  0, 1, 3, 4, 3, 0, 2, 3, 4, 2,
  2, 3, 4, 3, 0, 2, 3, 4, 3, 1,
];

export const mockContributions: GithubContributions = {
  totalCommits: 942,
  days: grassLevels.map((level, index) => ({
    date: new Date(Date.UTC(2026, 9, 4 + index)).toISOString().slice(0, 10),
    level,
  })),
};

export const mockRepos: GithubRepo[] = [
  { name: "three-fluid-simulation", stars: 328 },
  { name: "canvas-dipsource-kit", stars: 142 },
];

export const mockDemos: MiniHomeDemo[] = [
  {
    id: 11,
    title: "Interactive Neon Particle Swarm",
    description:
      "마우스 반발력과 물리 기반 가속도를 시뮬레이션한 파티클 인터랙션 데모입니다.",
    thumbnail: null,
    techTags: ["canvas", "webgl"],
    author: mockUsers.jung,
    likeCount: 42,
    engine: "WebGL & Canvas 2D Engine",
    playCount: 318,
    commentCount: 12,
  },
  {
    id: 12,
    title: "실시간 WebAudio 스펙트럼 뷰어",
    description: "마이크 입력 주파수를 파티클로 전환하는 시각화 도구",
    thumbnail: null,
    techTags: ["webaudio"],
    author: mockUsers.jung,
    likeCount: 29,
    engine: "Audio Spectrum API",
    playCount: 214,
    commentCount: 8,
  },
  {
    id: 13,
    title: "초경량 글래스모피즘 컴포넌트",
    description: "Zero-dependency 반응형 CSS 블러 디자인 킷",
    thumbnail: null,
    techTags: ["css"],
    author: mockUsers.jung,
    likeCount: 53,
    engine: "Glassmorphism UI",
    playCount: 502,
    commentCount: 5,
  },
  {
    id: 14,
    title: "GLSL 노이즈 셰이더 놀이터",
    description: "Perlin·Simplex 노이즈 파라미터를 실시간으로 조절해 보는 셰이더 데모",
    thumbnail: null,
    techTags: ["glsl"],
    author: mockUsers.jung,
    likeCount: 21,
    engine: "WebGL Fragment Shader",
    playCount: 156,
    commentCount: 3,
  },
  {
    id: 15,
    title: "드래그 앤 드롭 칸반 보드",
    description: "의존성 없이 Pointer Events로 만든 칸반 보드",
    thumbnail: null,
    techTags: ["typescript"],
    author: mockUsers.jung,
    likeCount: 18,
    engine: "Pointer Events",
    playCount: 133,
    commentCount: 2,
  },
  {
    id: 16,
    title: "CSS 3D 카드 플립 갤러리",
    description: "transform-style: preserve-3d로 만든 카드 갤러리",
    thumbnail: null,
    techTags: ["css"],
    author: mockUsers.jung,
    likeCount: 14,
    engine: "CSS 3D Transforms",
    playCount: 97,
    commentCount: 1,
  },
];

export const mockPosts: MiniHomePost[] = [
  { id: 301, title: "Three.js 셰이더 파이프라인에서 최적화 팁 10가지", membership: true },
  { id: 302, title: "웹 워커를 이용한 프레임 드랍 없는 60fps 파티클", membership: true },
  { id: 303, title: "오늘 리팩토링하다가 라자냐 코드 발견함. 7겹 레이어 회고", membership: false },
];

const MINUTE = 60_000;

export function createMockComments(now: number): { items: MiniHomeComment[]; total: number } {
  return {
    total: 28,
    items: [
      {
        id: 1,
        author: mockUsers.cha,
        content: "파티클 렌더링 물리반응 예술이네요 ㄷㄷ",
        createdAt: new Date(now - 10 * MINUTE).toISOString(),
        secret: false,
      },
      {
        id: 2,
        author: mockUsers.seungjo,
        content: "혹시 Three.js 모듈 소스 코드 공유 가능한가요?",
        createdAt: new Date(now - 32 * MINUTE).toISOString(),
        secret: true,
      },
      {
        id: 3,
        author: mockUsers.jinyeop,
        content: "파도타기 타고 놀러왔습니다! 일촌 신청했어요~ 데모 너무 멋져요",
        createdAt: new Date(now - 60 * MINUTE).toISOString(),
        secret: false,
      },
    ],
  };
}

const HOUR = 60 * MINUTE;

export function createMockGuestbook(now: number): MiniHomeComment[] {
  const entries: [UserSummary, string, number, boolean][] = [
    [mockUsers.cha, "홈피 BGM 선곡 무엇 ㅋㅋ 파티클 데모 보고 감탄하고 갑니다!", 0.2, false],
    [mockUsers.seungjo, "웹 워커 글 잘 읽었어요. 혹시 OffscreenCanvas도 써보셨나요?", 2, true],
    [mockUsers.jinyeop, "파도타기 타고 왔어요~ 일촌 신청했습니다 🙌", 5, false],
    [mockUsers.kwak, "글래스모피즘 컴포넌트 저희 사이드 프로젝트에 써도 될까요?", 26, false],
    [mockUsers.junha, "스펙트럼 뷰어 마이크 권한 안내 문구 넣으면 더 좋을 것 같아요!", 50, false],
    [mockUsers.me, "다음 데모도 기대할게요. 찍먹 최고 👍", 72, false],
  ];
  return entries.map(([author, content, hoursAgo, secret], index) => ({
    id: 100 + index,
    author,
    content,
    createdAt: new Date(now - hoursAgo * HOUR).toISOString(),
    secret,
  }));
}

export function pickRandomMockUserId(exceptId: number): number {
  const ids = Object.values(mockUsers)
    .map((user) => user.id)
    .filter((id) => id !== exceptId);
  return ids[Math.floor(Math.random() * ids.length)];
}
