// 관리자 화면 목 데이터. 날짜는 화면이 흔들리지 않게 고정값을 쓴다.

export type UserStatus = "active" | "suspended" | "withdrawn";

export type AdminUser = {
  id: number;
  nickname: string;
  email: string;
  provider: "github" | "kakao" | "google";
  status: UserStatus;
  joinedAt: string;
  demoCount: number;
  postCount: number;
  reportCount: number;
  chipBalance: number;
};

export type Sanction = { id: number; period: string; reason: string; createdAt: string; admin: string };

// comments는 피드 댓글, post-comments는 블로그 댓글이다. 테이블이 달라 ID가 겹칠 수 있어 나눈다.
export type ContentKind = "demos" | "posts" | "feeds" | "comments" | "post-comments";

export type ContentRow = {
  id: number;
  title: string;
  author: string;
  createdAt: string;
  metric: string;
  reported: number;
  status: "visible" | "hidden";
};

export type ReportStatus = "pending" | "resolved" | "rejected";

export type Report = {
  id: number;
  targetType: "데모" | "블로그" | "피드" | "댓글" | "방명록" | "회원";
  targetTitle: string;
  targetHref: string;
  reason: string;
  detail: string;
  reporter: string;
  reported: string;
  createdAt: string;
  status: ReportStatus;
  handledBy?: string | null;
  adminNote?: string | null;
  handledAt?: string | null;
};

const day = (offset: number) => new Date(Date.UTC(2026, 10, 2) - offset * 86_400_000).toISOString();

const names = ["딥소스", "차재인", "곽다윗", "김승조", "박준하", "정시우", "윤세진", "김진엽", "이서연", "최민준", "한지우", "오하늘", "서도윤", "장예린", "임현우", "강소율", "조하람", "윤태오", "신유나", "배준서", "권다인", "황시온", "스팸봇123"];
const providers: AdminUser["provider"][] = ["github", "kakao", "google"];

export const mockAdminUsers: AdminUser[] = names.map((nickname, index) => ({
  id: index + 1,
  nickname,
  email: `user${index + 1}@example.com`,
  provider: providers[index % 3],
  status: nickname === "스팸봇123" ? "suspended" : index === 19 ? "withdrawn" : "active",
  joinedAt: day(400 - index * 15),
  demoCount: (index * 7) % 13,
  postCount: (index * 5) % 9,
  reportCount: nickname === "스팸봇123" ? 14 : index % 6 === 0 ? 1 : 0,
  chipBalance: 1250 - index * 37,
}));

export const mockSanctions: Sanction[] = [
  { id: 1, period: "7일", reason: "광고성 댓글 반복 작성", createdAt: day(30), admin: "운영자 A" },
  { id: 2, period: "영구", reason: "스팸 링크 대량 게시", createdAt: day(3), admin: "운영자 B" },
];

const contentSeed: Record<ContentKind, [string, string, string][]> = {
  demos: [["실시간 채팅앱", "차재인", "체험 318"], ["날씨 대시보드", "곽다윗", "체험 254"], ["오목 게임", "김승조", "체험 199"], ["무료 칩 받는 법 (클릭)", "스팸봇123", "체험 3"], ["CSS 로딩 컬렉션", "김진엽", "체험 402"], ["Spring Batch 모니터", "딥소스", "체험 210"]],
  posts: [["10만 동시접속 채팅 서버 구축기", "차재인", "조회 3,120"], ["라자냐 코드 회고", "곽다윗", "조회 1,840"], ["Unity WebGL 38MB → 9MB", "김승조", "조회 1,302"], ["JPA N+1 해결기", "딥소스", "조회 980"], ["칩 무한 충전 꿀팁", "스팸봇123", "조회 12"]],
  feeds: [["채팅앱 데모 올렸어요. 피드백 부탁드려요", "차재인", "좋아요 42"], ["라자냐 코드 발견함. 레이어 7겹", "곽다윗", "좋아요 18"], ["Unity WebGL 빌드 용량 줄인 후기", "김승조", "좋아요 27"], ["useEffect AbortController 질문", "박준하", "좋아요 15"], ["여기 들어오면 칩 드려요 → bit.ly/xxxx", "스팸봇123", "좋아요 0"]],
  comments: [["STOMP 구독 경로 나눈 이유가 궁금해요!", "곽다윗", "피드 #1"], ["모바일 사파리에서도 되나요?", "김진엽", "피드 #2"], ["하트비트 주기는 몇 초로 두셨어요?", "김진엽", "피드 #1"]],
  "post-comments": [["광고 문의는 DM 주세요", "스팸봇123", "글 #1"], ["측정 → 가설 → 검증 공감합니다", "박준하", "글 #21"]],
};

export const mockContent: Record<ContentKind, ContentRow[]> = Object.fromEntries(
  (Object.keys(contentSeed) as ContentKind[]).map((kind) => [
    kind,
    contentSeed[kind].map(([title, author, metric], index) => ({
      id: index + 1,
      title,
      author,
      createdAt: day(index * 2 + 1),
      metric,
      reported: author === "스팸봇123" ? 6 : 0,
      status: author === "스팸봇123" && index % 2 === 0 ? "hidden" : "visible",
    })),
  ]),
) as Record<ContentKind, ContentRow[]>;

export const mockReports: Report[] = [
  { id: 1, targetType: "피드", targetTitle: "여기 들어오면 칩 드려요 → bit.ly/xxxx", targetHref: "/feeds/5", reason: "스팸·광고", detail: "외부 피싱 링크로 유도합니다.", reporter: "곽다윗", reported: "스팸봇123", createdAt: day(0), status: "pending" },
  { id: 2, targetType: "데모", targetTitle: "무료 칩 받는 법 (클릭)", targetHref: "/demos/4", reason: "악성 코드 의심", detail: "실행하면 다른 사이트로 계속 리다이렉트돼요.", reporter: "김진엽", reported: "스팸봇123", createdAt: day(0), status: "pending" },
  { id: 3, targetType: "댓글", targetTitle: "광고 문의는 DM 주세요", targetHref: "/blog/posts/1", reason: "스팸·광고", detail: "여러 글에 같은 댓글을 남기고 있어요.", reporter: "차재인", reported: "스팸봇123", createdAt: day(1), status: "pending" },
  { id: 4, targetType: "블로그", targetTitle: "라자냐 코드 회고", targetHref: "/blog/posts/2", reason: "저작권 침해", detail: "제 글의 코드를 출처 없이 가져다 썼어요.", reporter: "한지우", reported: "곽다윗", createdAt: day(4), status: "rejected" },
  { id: 5, targetType: "방명록", targetTitle: "욕설이 포함된 방명록", targetHref: "/mini-home/6/guestbook", reason: "욕설·비하", detail: "비하 표현이 포함돼 있어요.", reporter: "정시우", reported: "오하늘", createdAt: day(6), status: "resolved" },
];

export const mockSignups = [32, 41, 38, 55, 47, 62, 71];
// subscription: 블로그 구독, plus: 딥소스 플러스 (둘 다 자동이체), chips: 칩 충전
export const mockRevenue = [
  { month: "2026-06", subscription: 1_820_000, plus: 640_000, chips: 2_410_000 },
  { month: "2026-07", subscription: 2_040_000, plus: 790_000, chips: 2_780_000 },
  { month: "2026-08", subscription: 2_350_000, plus: 960_000, chips: 3_120_000 },
  { month: "2026-09", subscription: 2_610_000, plus: 1_120_000, chips: 3_050_000 },
  { month: "2026-10", subscription: 3_020_000, plus: 1_340_000, chips: 3_640_000 },
  { month: "2026-11", subscription: 980_000, plus: 450_000, chips: 1_210_000 },
];

// 블로그 구독과 딥소스 플러스는 현금 월 자동이체(원)다.
export type AdminSubscription = {
  id: number;
  plan: "blog" | "plus";
  subscriber: string;
  author: string | null;
  price: number;
  startedAt: string;
  status: "active" | "canceled";
};
export const mockAdminSubscriptions: AdminSubscription[] = [
  { id: 1, plan: "plus", subscriber: "딥소스", author: null, price: 4900, startedAt: day(93), status: "active" },
  { id: 2, plan: "blog", subscriber: "딥소스", author: "차재인", price: 5000, startedAt: day(60), status: "active" },
  { id: 3, plan: "blog", subscriber: "딥소스", author: "윤세진", price: 3000, startedAt: day(32), status: "active" },
  { id: 4, plan: "plus", subscriber: "정시우", author: null, price: 4900, startedAt: day(150), status: "active" },
  { id: 5, plan: "blog", subscriber: "곽다윗", author: "차재인", price: 5000, startedAt: day(90), status: "active" },
  { id: 6, plan: "blog", subscriber: "김진엽", author: "정시우", price: 3000, startedAt: day(12), status: "active" },
  { id: 7, plan: "blog", subscriber: "박준하", author: "김승조", price: 3000, startedAt: day(120), status: "canceled" },
];

export type Settlement = { id: number; creator: string; gross: number; fee: number; status: "pending" | "done"; period: string };
export const mockSettlements: Settlement[] = [
  { id: 1, creator: "차재인", gross: 845_000, fee: 169_000, status: "pending", period: "2026-10" },
  { id: 2, creator: "윤세진", gross: 512_000, fee: 102_400, status: "pending", period: "2026-10" },
  { id: 3, creator: "정시우", gross: 388_000, fee: 77_600, status: "pending", period: "2026-10" },
  { id: 4, creator: "김승조", gross: 126_000, fee: 25_200, status: "done", period: "2026-10" },
  { id: 5, creator: "차재인", gross: 790_000, fee: 158_000, status: "done", period: "2026-09" },
];

export type CoinTx = { id: number; user: string; type: "charge" | "refund-request" | "refunded"; chips: number; createdAt: string; memo: string };
export const mockCoinTx: CoinTx[] = [
  { id: 1, user: "딥소스", type: "charge", chips: 300, createdAt: day(5), memo: "카드 결제" },
  { id: 2, user: "곽다윗", type: "refund-request", chips: 20, createdAt: day(1), memo: "품질 보장제: 코드 실행 불가" },
  { id: 3, user: "김진엽", type: "charge", chips: 50, createdAt: day(2), memo: "카카오페이" },
  { id: 4, user: "박준하", type: "refunded", chips: 10, createdAt: day(9), memo: "품질 보장제 환불 완료" },
  { id: 5, user: "한지우", type: "refund-request", chips: 10, createdAt: day(0), memo: "중복 결제" },
];

export type Suspicious = { id: number; userId: number; user: string; pattern: string; count: number; amount: number; detectedAt: string; level: "high" | "medium" };
export const mockSuspicious: Suspicious[] = [
  { id: 1, userId: 23, user: "스팸봇123", pattern: "1분 내 동일 카드 결제 시도 12회", count: 12, amount: 132_000, detectedAt: day(0), level: "high" },
  { id: 2, userId: 15, user: "임현우", pattern: "충전 직후 전액 환불 요청 반복", count: 4, amount: 44_000, detectedAt: day(2), level: "medium" },
  { id: 3, userId: 13, user: "서도윤", pattern: "다계정 간 칩 선물 순환", count: 7, amount: 15_400, detectedAt: day(3), level: "medium" },
];
