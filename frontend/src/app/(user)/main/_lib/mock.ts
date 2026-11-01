import { mockUsers } from "@/app/(user)/_lib/mock";
import type { DemoSummary } from "@/app/_lib/types/demo";
import type { Feed, Tag } from "@/app/_lib/types/feed";

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
    author: mockUsers.kim,
    likeCount: 42,
  },
  {
    id: 2,
    title: "날씨 대시보드",
    description: "공공 API로 만든 날씨 대시보드입니다.",
    thumbnail: null,
    techTags: ["react"],
    author: mockUsers.park,
    likeCount: 31,
  },
  {
    id: 3,
    title: "오목 게임",
    description: "WebGL로 만든 오목 게임입니다.",
    thumbnail: null,
    techTags: ["unity webgl"],
    author: mockUsers.lee,
    likeCount: 27,
  },
  {
    id: 4,
    title: "투두 리스트",
    description: "바닐라 JS로 만든 투두 리스트입니다.",
    thumbnail: null,
    techTags: ["vanilla js"],
    author: mockUsers.choi,
    likeCount: 19,
  },
];

const HOUR = 3_600_000;

export function createMockFeeds(now: number): Feed[] {
  return [
    {
      id: 1,
      author: mockUsers.kim,
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
      author: mockUsers.park,
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
  ];
}
