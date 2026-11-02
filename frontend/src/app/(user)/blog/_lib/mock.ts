import { mockUsers } from "@/app/(user)/_lib/mock";
import type {
  ArticleRanking,
  BlogOwner,
  PostComment,
  PostDetail,
  PostSeries,
  PostSummary,
  SubscribedAuthor,
} from "@/app/_lib/types/post";
import type { UserSummary } from "@/app/_lib/types/user";

export const mockSeries: PostSeries[] = [
  {
    id: 1,
    author: mockUsers.cha,
    authorTitle: "시니어 아키텍트",
    title: "대규모 트래픽을 견디는 이벤트 드리븐 시스템 디자인 가이드",
    description:
      "Kafka 파티셔닝 전략부터 Outbox 패턴, 데드레터 큐 재처리 로직까지 프로덕션 환경의 실전 설계를 다룹니다.",
    tags: ["Kafka", "Spring Boot", "AWS"],
    status: "completed",
    episodeCount: 12,
    schedule: null,
    freeEpisodes: 3,
    price: 15,
    firstPostId: 101,
  },
  {
    id: 2,
    author: mockUsers.yoon,
    authorTitle: "프론트엔드 리드",
    title: "React 19 & Next.js 15: 풀스택 렌더링 최적화 딥다이브",
    description:
      "RSC(React Server Components), Server Actions 내부 원리와 캐싱 라이프사이클을 심층 분석합니다.",
    tags: ["React 19", "Next.js", "Web Performance"],
    status: "ongoing",
    episodeCount: 15,
    schedule: "매주 화·금",
    freeEpisodes: 2,
    price: 10,
    firstPostId: 201,
  },
];

const HOUR = 3_600_000;

export function createMockPosts(now: number): PostSummary[] {
  return [
    {
      id: 1,
      author: mockUsers.cha,
      title: "STOMP + Spring WebSocket으로 10만 동시접속 채팅 서버 구축하기 (오답노트 포함)",
      excerpt:
        "단일 인스턴스 톰캣 환경에서 메모리 릭을 경험하고, Redis Pub/Sub 및 Netty 기반 소켓 분리로 전환한 실제 리팩토링 일지입니다. 서버 아키텍처 다이어그램 및 부하 테스트 결과까지 공개합니다.",
      category: "backend",
      createdAt: new Date(now - 2 * HOUR).toISOString(),
      price: 20,
      readMinutes: 6,
      demo: {
        id: 1,
        title: "부하 테스트 코드 및 STOMP 채널 인터셉터 소스",
        description: "k6 스크립트 + WebSocketHandler 소스코드 패키지 포함",
      },
      likeCount: 42,
      commentCount: 12,
      repostCount: 5,
      liked: false,
    },
    {
      id: 2,
      author: mockUsers.kwak,
      title: "오늘 리팩토링하다가 라자냐 코드 발견함: 레이어가 7겹이었던 사연",
      excerpt:
        "클린 아키텍처를 과도하게 적용한 나머지 DTO 변환만 5번 거치는 보일러플레이트 지옥을 어떻게 단 2개의 단순 계층으로 압축했는지 후기를 남깁니다. 오버 엔지니어링을 피하는 실용주의 접근법.",
      category: "frontend",
      createdAt: new Date(now - 5 * HOUR).toISOString(),
      price: 0,
      readMinutes: 3,
      demo: null,
      likeCount: 18,
      commentCount: 4,
      repostCount: 1,
      liked: false,
    },
  ];
}

export const mockArticleRanking: ArticleRanking = {
  views: [
    { rank: 1, postId: 11, title: "MSA 분산 트랜잭션과 Saga 패턴 실전", score: 1420 },
    { rank: 2, postId: 12, title: "CSS Grid & Subgrid 레이아웃 완전 정복", score: 980 },
    { rank: 3, postId: 13, title: "Spring Boot 3.3 AOT 빌드로 기동 시간 줄이기", score: 742 },
  ],
  likes: [
    { rank: 1, postId: 12, title: "CSS Grid & Subgrid 레이아웃 완전 정복", score: 312 },
    { rank: 2, postId: 1, title: "STOMP + Spring WebSocket 10만 동시접속", score: 254 },
    { rank: 3, postId: 11, title: "MSA 분산 트랜잭션과 Saga 패턴 실전", score: 198 },
  ],
  follows: [
    { rank: 1, postId: 13, title: "Spring Boot 3.3 AOT 빌드로 기동 시간 줄이기", score: 86 },
    { rank: 2, postId: 11, title: "MSA 분산 트랜잭션과 Saga 패턴 실전", score: 64 },
    { rank: 3, postId: 2, title: "라자냐 코드: 레이어가 7겹이었던 사연", score: 41 },
  ],
};

export const mockSubscribedAuthors: SubscribedAuthor[] = [
  { user: mockUsers.cha, seriesLabel: "대규모 트래픽 연재 12화", online: true },
  { user: mockUsers.yoon, seriesLabel: "Next.js 15 아키텍처 연재", online: true },
  { user: mockUsers.seungjo, seriesLabel: "DevOps CI/CD 파이프라인", online: false },
];

// 작성자별 블로그·글 상세에서 쓰는 추가 글. 블로그 홈 목록에는 위 두 글만 보여 준다.
const extraPosts: [number, keyof typeof mockUsers, string, string, PostSummary["category"], number, number][] = [
  [11, "cha", "MSA 분산 트랜잭션과 Saga 패턴 실전", "오케스트레이션과 코레오그래피 중 무엇을 골랐는지, 보상 트랜잭션 설계까지 정리했어요.", "backend", 15, 30],
  [12, "kwak", "CSS Grid & Subgrid 레이아웃 완전 정복", "카드 높이 맞추기부터 복잡한 대시보드 레이아웃까지 Subgrid로 해결한 사례 모음.", "frontend", 0, 52],
  [13, "seungjo", "Spring Boot 3.3 AOT 빌드로 기동 시간 줄이기", "네이티브 이미지 없이 AOT 처리만으로 기동 시간을 40% 줄인 과정.", "backend", 0, 75],
  [21, "me", "JPA N+1 해결기: fetch join vs EntityGraph vs BatchSize", "실제 피드 조회 API에서 쿼리 41개를 3개로 줄인 과정을 비교합니다.", "backend", 0, 20],
  [22, "me", "Spring Batch 모니터 데모 제작기", "배치 잡 실행 이력을 실시간 대시보드로 만든 과정과 SSE 선택 이유.", "backend", 10, 96],
  [101, "cha", "[이벤트 드리븐 1화] 왜 이벤트 드리븐인가", "동기 호출이 장애를 전파하는 구조를 그림으로 보고, 이벤트로 끊는 방법을 소개해요.", "backend", 0, 200],
  [201, "yoon", "[렌더링 딥다이브 1화] RSC는 무엇을 서버로 옮기는가", "서버 컴포넌트 페이로드를 직접 열어 보며 렌더링 경계를 이해해 봅니다.", "frontend", 0, 210],
  [301, "jung", "Three.js 셰이더 파이프라인에서 최적화 팁 10가지", "드로우콜 줄이기부터 유니폼 업데이트 비용까지, 실전 최적화 체크리스트.", "frontend", 15, 40],
  [302, "jung", "웹 워커를 이용한 프레임 드랍 없는 60fps 파티클", "OffscreenCanvas와 워커로 메인 스레드를 비워 60fps를 지킨 방법.", "frontend", 15, 60],
  [303, "jung", "오늘 리팩토링하다가 라자냐 코드 발견함. 7겹 레이어 회고", "계층을 과하게 나누면 생기는 문제와 줄이는 기준을 정리했어요.", "frontend", 0, 90],
];

const body = (title: string) => `## 들어가며

「${title}」를 정리하게 된 계기는 단순했어요. 같은 문제를 세 번째 만났을 때, 이번엔 기록으로 남겨야겠다고 생각했습니다.

## 문제 상황

- 트래픽이 몰리는 시간에 응답이 2초 이상 늘어졌어요.
- 로그만으로는 어디서 막히는지 알기 어려웠어요.
- 재현 환경을 만드는 데만 하루가 걸렸어요.

## 해결 과정

먼저 병목을 숫자로 확인했어요. 측정 없이 고치면 같은 실수를 반복하게 됩니다.

\`\`\`java
@Transactional(readOnly = true)
public List<FeedResponse> getFeeds(Long userId) {
    return feedRepository.findAllWithAuthor(userId).stream()
        .map(FeedResponse::from)
        .toList();
}
\`\`\`

조회 쿼리를 하나로 묶고, 응답 DTO 변환을 한 곳으로 모았더니 코드도 성능도 같이 좋아졌어요.

## 마치며

정답은 상황마다 다르지만, 측정 → 가설 → 검증 순서만 지키면 크게 틀리지 않더라고요. 궁금한 점은 댓글로 남겨 주세요!`;

export function createMockPostDetail(postId: number, now: number): PostDetail | null {
  const base = createMockPosts(now).find((post) => post.id === postId);
  const extra = extraPosts.find(([id]) => id === postId);
  let summary: PostSummary;
  if (base) {
    summary = base;
  } else if (extra) {
    const [id, author, title, excerpt, category, price, hoursAgo] = extra;
    summary = {
      id,
      author: mockUsers[author],
      title,
      excerpt,
      category,
      createdAt: new Date(now - hoursAgo * HOUR).toISOString(),
      price,
      readMinutes: 5,
      demo: null,
      likeCount: 12 + (id % 30),
      commentCount: 2 + (id % 5),
      repostCount: id % 4,
      liked: false,
    };
  } else {
    return null;
  }
  return {
    ...summary,
    content: body(summary.title),
    tags: summary.category === "backend" ? ["Spring", "JPA", "성능"] : ["React", "CSS", "성능"],
    purchased: false,
    seriesTitle: postId === 101 ? mockSeries[0].title : postId === 201 ? mockSeries[1].title : null,
    viewCount: 300 + postId * 7,
  };
}

export function createMockUserPosts(userId: number, now: number): PostSummary[] {
  return [...createMockPosts(now).map((post) => post.id), ...extraPosts.map(([id]) => id)]
    .map((id) => createMockPostDetail(id, now))
    .filter((post): post is PostDetail => post !== null && post.author.id === userId);
}

export function createMockPostComments(postId: number, now: number): PostComment[] {
  const rows: [UserSummary, string, number][] = [
    [mockUsers.junha, "측정 → 가설 → 검증 순서, 저도 팀에 공유해야겠어요 👍", 25],
    [mockUsers.kwak, "fetch join이랑 페이징 같이 쓸 때는 어떻게 하셨어요?", 70],
    [mockUsers.me, "잘 읽었습니다. 다음 편도 기대할게요!", 140],
  ];
  const comments: PostComment[] = rows.map(([author, content, minutesAgo], index) => ({
    id: postId * 100 + index,
    author,
    content,
    createdAt: new Date(now - minutesAgo * 60_000).toISOString(),
    likeCount: Math.max(0, 3 - index),
    liked: index === 0,
  }));
  comments[1].replies = [
    {
      id: postId * 100 + 50,
      author: mockUsers.seungjo,
      content: "@곽다윗 컬렉션 fetch join은 페이징이랑 같이 못 써서 batch size로 풀었어요.",
      createdAt: new Date(now - 40 * 60_000).toISOString(),
      likeCount: 1,
      liked: false,
    },
  ];
  return comments;
}

export function findMockBlogOwner(userId: number): BlogOwner | null {
  const user = Object.values(mockUsers).find((item) => item.id === userId);
  if (!user) return null;
  return {
    user,
    bio: `${user.techStacks[0] ?? "개발"} 이야기를 꾸준히 기록하는 ${user.nickname}의 개발 블로그예요.`,
    subscriberCount: 120 + userId * 37,
    postCount: createMockUserPosts(userId, 0).length,
    totalViews: 5000 + userId * 1234,
    // 블로그 구독은 현금 월 자동이체(원)
    subscriptionPrice: [3000, 5000, 3000][userId % 3],
    subscribed: userId === 2 || userId === 7,
  };
}

export const mockDemoOptions = [
  { id: 1, title: "실시간 채팅앱" },
  { id: 9, title: "Spring Batch 모니터" },
  { id: 8, title: "마크다운 에디터" },
];
