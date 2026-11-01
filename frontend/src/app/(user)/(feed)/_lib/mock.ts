import { mockUsers } from "@/app/(user)/_lib/mock";
import type { DemoSummary } from "@/app/_lib/types/demo";
import type { Feed, FeedComment, Tag } from "@/app/_lib/types/feed";
import type { UserSummary } from "@/app/_lib/types/user";

export const mockTags: Tag[] = ["spring", "java", "react", "unity", "game", "code-review"].map(
  (name, index) => ({ id: index + 1, name }),
);

export const mockHotDemos: DemoSummary[] = [
  {
    id: 1,
    title: "실시간 채팅앱",
    description: "WebSocket 기반 실시간 채팅 데모입니다.",
    thumbnail: null,
    techTags: ["websocket"],
    author: mockUsers.cha,
    likeCount: 42,
  },
  {
    id: 2,
    title: "날씨 대시보드",
    description: "공공 API로 만든 날씨 대시보드입니다.",
    thumbnail: null,
    techTags: ["react"],
    author: mockUsers.kwak,
    likeCount: 31,
  },
  {
    id: 3,
    title: "오목 게임",
    description: "WebGL로 만든 오목 게임입니다.",
    thumbnail: null,
    techTags: ["unity webgl"],
    author: mockUsers.seungjo,
    likeCount: 27,
  },
  {
    id: 4,
    title: "투두 리스트",
    description: "바닐라 JS로 만든 투두 리스트입니다.",
    thumbnail: null,
    techTags: ["vanilla js"],
    author: mockUsers.junha,
    likeCount: 19,
  },
  {
    id: 5,
    title: "픽셀 아트 캔버스",
    description: "Canvas API로 만든 32x32 픽셀 드로잉 툴입니다.",
    thumbnail: null,
    techTags: ["canvas"],
    author: mockUsers.jinyeop,
    likeCount: 17,
  },
  {
    id: 6,
    title: "타자 연습 게임",
    description: "코드 스니펫으로 타자 속도를 재는 게임입니다.",
    thumbnail: null,
    techTags: ["typescript"],
    author: mockUsers.seungjo,
    likeCount: 15,
  },
  {
    id: 7,
    title: "Three.js 태양계",
    description: "행성 궤도를 3D로 돌려보는 Three.js 데모입니다.",
    thumbnail: null,
    techTags: ["three.js"],
    author: mockUsers.cha,
    likeCount: 12,
  },
];

const HOUR = 3_600_000;

export function createMockFeeds(now: number): Feed[] {
  return [
    {
      id: 1,
      author: mockUsers.cha,
      content:
        "채팅앱 데모 올렸어요. 웹소켓 + STOMP로 만들었는데 찍먹해보고 피드백 부탁드려요.",
      createdAt: new Date(now - 2 * HOUR).toISOString(),
      demo: {
        ...mockHotDemos[0],
        techTags: ["html", "css", "js"],
      },
      likeCount: 42,
      commentCount: 12,
      repostCount: 5,
      liked: false,
      reposted: false,
      following: false,
    },
    {
      id: 2,
      author: mockUsers.kwak,
      content: "오늘 리팩토링하다가 라자냐 코드 발견함. 레이어가 7겹이었다.",
      createdAt: new Date(now - 5 * HOUR).toISOString(),
      demo: null,
      likeCount: 18,
      commentCount: 4,
      repostCount: 1,
      liked: false,
      reposted: false,
      following: false,
    },
    {
      id: 3,
      author: mockUsers.seungjo,
      content:
        "Unity WebGL 빌드 용량 38MB → 9MB로 줄였어요. Brotli 압축 + 텍스처 ASTC 변환 + 안 쓰는 패키지 정리가 핵심이었습니다. 오목 게임으로 로딩 속도 체감해 보세요.",
      createdAt: new Date(now - 8 * HOUR).toISOString(),
      demo: {
        ...mockHotDemos[2],
        techTags: ["unity", "webgl", "c#"],
      },
      likeCount: 27,
      commentCount: 9,
      repostCount: 3,
      liked: false,
      reposted: false,
      following: true,
    },
    {
      id: 4,
      author: mockUsers.junha,
      content:
        "코드리뷰 질문! useEffect 안에서 fetch할 때 AbortController 꼭 써야 하나요? cleanup에서 ignore 플래그만 바꾸고 있었는데 리뷰에서 요청 자체를 취소하라고 지적받았어요.",
      createdAt: new Date(now - 12 * HOUR).toISOString(),
      demo: null,
      likeCount: 15,
      commentCount: 21,
      repostCount: 2,
      liked: false,
      reposted: false,
      following: false,
    },
    {
      id: 5,
      author: mockUsers.kwak,
      content:
        "날씨 대시보드에 React 19 use() + Suspense 적용해 봤어요. 로딩 스피너 분기 코드가 절반으로 줄었고, 공공 API 호출을 서버 컴포넌트로 옮기니 키 노출 걱정도 끝.",
      createdAt: new Date(now - 26 * HOUR).toISOString(),
      demo: {
        ...mockHotDemos[1],
        techTags: ["react", "typescript", "next.js"],
      },
      likeCount: 31,
      commentCount: 7,
      repostCount: 6,
      liked: false,
      reposted: false,
      following: false,
    },
  ];
}

const MINUTE = 60_000;

export function createMockFeedComments(feedId: number, now: number): FeedComment[] {
  const rows: [UserSummary, string, number, number][] = [
    [mockUsers.kwak, "STOMP 구독 경로를 /topic/room.{id}로 나눈 이유가 궁금해요!", 20, 4],
    [mockUsers.seungjo, "찍먹해봤는데 재접속할 때 메시지 유실 없이 잘 붙네요 👍", 45, 2],
    [mockUsers.jinyeop, "하트비트 주기는 몇 초로 두셨어요?", 90, 1],
  ];
  return rows.map(([author, content, minutesAgo, likeCount], index) => ({
    id: feedId * 100 + index,
    author,
    content,
    createdAt: new Date(now - minutesAgo * MINUTE).toISOString(),
    likeCount,
  }));
}
