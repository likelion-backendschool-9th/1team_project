import type { MiniHomeDemo, MiniHomeSkin } from "./types/miniHome";
import type { UserSummary } from "./types/user";

// 회원마다 다른 미니홈피를 보여 주기 위한 목 데이터 (미니홈피·데모 목록·피드 작성창이 같이 쓴다).
// 썸네일은 이모지 마스코트를 그린 SVG 이미지다. 실제로는 업로드한 데모의 썸네일 URL이 온다.

type Theme = {
  skin: MiniHomeSkin;
  headline: string;
  quote: string;
  colors: [string, string];
  demos: [emoji: string, title: string, engine: string, description: string][];
};

export const userThemes: Record<number, Theme> = {
  2: {
    skin: "mint",
    headline: "Cat Lover Backend",
    quote: "“고양이처럼\n유연한 서버.” 🐾",
    colors: ["#dff8ec", "#7fe0b5"],
    demos: [
      ["🐱", "고양이 채팅방", "WebSocket + STOMP", "고양이 아바타로 대화하는 실시간 채팅 데모예요."],
      ["💬", "말풍선 애니메이션", "CSS Animations", "메시지가 통통 튀며 쌓이는 말풍선 컴포넌트예요."],
      ["🧶", "털실 물리 엔진", "Matter.js", "털실 공을 굴리고 당겨 보는 물리 놀이터예요."],
    ],
  },
  3: {
    skin: "cherry",
    headline: "Pastel UI Crafter",
    quote: "“예쁜 UI는\n기분을 바꾼다.” 🌸",
    colors: ["#ffe4ef", "#ff9cc2"],
    demos: [
      ["🐰", "토끼 투두 리스트", "React", "할 일을 끝내면 토끼가 당근을 먹는 투두 앱이에요."],
      ["🥕", "당근 수확 클리커", "Vanilla JS", "클릭할수록 당근 밭이 넓어지는 클리커 게임이에요."],
      ["🌸", "벚꽃 파티클", "Canvas 2D", "마우스를 따라 벚꽃잎이 흩날리는 배경 효과예요."],
    ],
  },
  4: {
    skin: "sky",
    headline: "Indie Game Dev",
    quote: "“버그도 게임의\n일부라면…” 🎮",
    colors: ["#dff0ff", "#8ec9ff"],
    demos: [
      ["🐧", "펭귄 점프 게임", "Unity WebGL", "얼음 위를 미끄러지며 장애물을 뛰어넘는 펭귄 게임이에요."],
      ["🧊", "빙판 슬라이드 퍼즐", "Unity WebGL", "한 번 밀면 끝까지 미끄러지는 얼음 퍼즐이에요."],
      ["🐟", "물고기 잡기", "Phaser", "타이밍을 맞춰 물고기를 낚는 캐주얼 게임이에요."],
    ],
  },
  5: {
    skin: "cream",
    headline: "Frontend Explorer",
    quote: "“오늘도\n한 커밋.” 🐶",
    colors: ["#fff0dc", "#ffc58a"],
    demos: [
      ["🐶", "강아지 산책 타이머", "Vanilla JS", "산책 시간을 재면 강아지가 꼬리를 흔드는 타이머예요."],
      ["🦴", "뼈다귀 던지기", "Canvas 2D", "포물선을 계산해 뼈다귀를 던지는 미니 게임이에요."],
      ["🎾", "공 튀기기 물리", "Canvas 2D", "중력과 탄성을 바꿔 보는 공 튀기기 데모예요."],
    ],
  },
  7: {
    skin: "lavender",
    headline: "RSC Enthusiast",
    quote: "“서버에서 그리고\n클라이언트에서 꿈꾼다.” 🌙",
    colors: ["#efe7ff", "#b9a2ff"],
    demos: [
      ["🦊", "여우 날씨 위젯", "Next.js", "날씨에 따라 여우가 옷을 갈아입는 위젯이에요."],
      ["🍂", "낙엽 스크롤 효과", "Scroll-driven Animations", "스크롤하면 낙엽이 떨어지는 랜딩 페이지예요."],
      ["🌙", "달 위상 계산기", "TypeScript", "날짜를 고르면 그날의 달 모양을 보여 줘요."],
    ],
  },
  8: {
    skin: "peach",
    headline: "Kotlin Bear",
    quote: "“꾸준함이\n곧 실력.” 🍯",
    colors: ["#ffe9dc", "#ffb48a"],
    demos: [
      ["🐻", "곰돌이 습관 트래커", "Kotlin/JS", "습관을 지킬수록 곰돌이가 자라는 트래커예요."],
      ["🍯", "꿀단지 카운터", "Compose for Web", "누를 때마다 꿀이 차오르는 카운터예요."],
      ["🌲", "숲속 BGM 믹서", "Web Audio", "새소리·바람 소리를 섞어 나만의 배경음을 만들어요."],
    ],
  },
  9: {
    skin: "forest",
    headline: "Lecture & Code",
    quote: "“설명할 수 있어야\n아는 것이다.” 🦉",
    colors: ["#dcefe2", "#6fbf8e"],
    demos: [
      ["🦉", "부엉이 강의 노트", "Vue", "강의를 들으며 타임스탬프 메모를 남기는 노트예요."],
      ["📚", "책장 3D 뷰어", "Three.js", "읽은 책을 3D 책장에 꽂아 보는 뷰어예요."],
      ["✏️", "코드 필기 연습", "Vanilla JS", "예제 코드를 따라 치며 타자 속도를 재요."],
    ],
  },
  10: {
    skin: "midnight",
    headline: "Like a Lion",
    quote: "“나만의 것을\n만들자.” 🦁",
    colors: ["#3a2f1d", "#ffb020"],
    demos: [
      ["🦁", "멋사 사자 키우기", "Go + WASM", "코드를 커밋할수록 사자가 자라는 육성 게임이에요."],
      ["🦒", "기린 키 재기 차트", "D3.js", "동물들의 키를 비교해 보는 막대 차트예요."],
      ["🌴", "사바나 타일맵", "Canvas 2D", "타일을 칠해 나만의 사바나를 꾸며요."],
    ],
  },
  11: {
    skin: "sunset",
    headline: "Mars Bound",
    quote: "“다음 배포는\n화성에서.” 🚀",
    colors: ["#ffe1d1", "#ff8a65"],
    demos: [
      ["🚀", "로켓 발사 시뮬레이터", "Flutter Web", "연료와 각도를 정해 로켓을 쏘아 올려요."],
      ["🪐", "행성 궤도 계산기", "Canvas 2D", "중력을 바꾸면 궤도가 어떻게 변하는지 보여 줘요."],
      ["🛰️", "위성 추적 대시보드", "React", "가상의 위성 위치를 지도 위에 실시간으로 그려요."],
    ],
  },
};

function cuteThumbnail(emoji: string, [from, to]: [string, string]) {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 200">
<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="${from}"/><stop offset="1" stop-color="${to}"/></linearGradient></defs>
<rect width="320" height="200" fill="url(#g)"/>
<circle cx="40" cy="36" r="10" fill="#fff" opacity=".55"/><circle cx="282" cy="160" r="16" fill="#fff" opacity=".4"/>
<circle cx="268" cy="40" r="6" fill="#fff" opacity=".6"/><circle cx="58" cy="166" r="7" fill="#fff" opacity=".5"/>
<ellipse cx="160" cy="166" rx="54" ry="9" fill="#000" opacity=".08"/>
<text x="160" y="132" font-size="92" text-anchor="middle">${emoji}</text>
</svg>`;
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`;
}

/** 딥소스(1)·정시우(6)를 뺀 회원의 데모. id는 회원 id × 1000 + 순번이라 다른 목 데이터와 겹치지 않는다. */
export function createUserDemos(user: UserSummary): MiniHomeDemo[] {
  const theme = userThemes[user.id];
  if (!theme) return [];
  return theme.demos.map(([emoji, title, engine, description], index) => ({
    id: user.id * 1000 + index + 1,
    title,
    description,
    thumbnail: cuteThumbnail(emoji, theme.colors),
    techTags: [engine.split(" ")[0].toLowerCase()],
    author: user,
    likeCount: 12 + ((user.id * 7 + index * 5) % 40),
    engine,
    playCount: 80 + ((user.id * 31 + index * 17) % 300),
    commentCount: 2 + ((user.id + index) % 6),
  }));
}
