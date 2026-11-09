# 찍먹 프론트엔드 개발 문서

> 프론트엔드 작업의 기준 문서다.
>
> - **데이터·API:** 백엔드 실제 API/DTO 코드가 기준. [wbs.md](./wbs.md)는 작업 초안
> - **DB 구조:** [database-design.md](./database-design.md), [테이블정의서](./테이블정의서.md) 참조
> - **Git 규칙:** [AGENTS.md](../AGENTS.md)를 따른다

## 1. 개요

찍먹은 개발자들이 프로젝트를 데모로 공유하고, 미니홈피를 꾸미고, 서로의 코드를 체험할 수 있는 개발자 SNS다. Next.js 16 App Router, React 19, TypeScript `strict`를 쓴다.

| 역할 | 기능 |
| --- | --- |
| 비회원 | 데모·블로그·피드 조회, 미니홈피 방문, 검색, 순위 |
| 회원 | 데모 업로드·실행, 블로그 작성, 피드·댓글·좋아요, DM, 팔로우, 구독·결제, 미니홈피 꾸미기 |
| 관리자 | 회원·콘텐츠·신고·구독·정산·가상화폐 관리 |

---

## 2. 라우트 구조

### 2-1. 원칙

1. **작업 단위는 "페이지 ↔ 백엔드 API" 쌍이다.** 작업 분배, PR, 리뷰를 이 쌍 단위로 한다.
2. **URL을 추가·변경·삭제하면 대응 API와 함께 바꾼다.** 프론트 단독으로 바꾸지 않는다.
3. **기획서 5장의 API 명세에 없는 API는 가정하지 않는다.** 새 엔드포인트는 백엔드와 합의해 기획서에 먼저 반영한다.

### 2-2. 라우트 목록

| 페이지 URL | 주요 API (`/api/v1/` 생략) | 비고 |
| --- | --- | --- |
| `/` | 추천 피드, 인기 데모, 순위, 뉴스 | 통합 타임라인 |
| `/login` | `POST auth/login/{provider}` | 소셜 로그인 (GitHub, 카카오, 구글) |
| `/onboarding` | 명세 없음 (목) | 소셜 가입 직후 4단계 입력의 입구. 끝나지 않은 첫 단계로 리다이렉트 |
| `/onboarding/profile` | 명세 없음 (목) | 1단계 기본 정보 (닉네임 필수) |
| `/onboarding/stack` · `/fields` · `/topics` | 명세 없음 (목) | 2~4단계 고정 태그 선택 (기술 스택 10개, 관심분야 10개, 관심주제 7개 이하, 각각 2개 이상). API·태그 목록은 백엔드와 합의 후 기획서에 반영 |
| `/feeds` | `GET feeds`, `POST feeds` | 피드 전용 페이지 |
| `/home/[userId]` | `GET mini-home/{userId}` | 미니홈피 |
| `/home/[userId]/edit` | `PUT mini-home/{userId}` | 미니홈피 수정 (본인만) |
| `/home/[userId]/guestbook` | `GET/POST mini-home/{userId}/guestbook` | 방명록 |
| `/home/[userId]/demos` | `GET mini-home/{userId}/demos` | 미니홈피 내 데모 목록 |
| `/home/[userId]/blog` | `GET users/{userId}/posts` | 미니홈피 내 블로그 목록 |
| `/demos` | `GET demos` | 데모 탐색 (태그 필터, 정렬) |
| `/demos/upload` | `POST demos`, `POST demos/{id}/versions`, `POST demos/{id}/publish` | 데모 업로드. zip만 브라우저에서 API로 바로 보낸다 (Server Action 용량 제한) |
| `/demos/[demoId]/manage` | `PUT·DELETE demos/{id}`, `POST demos/{id}/versions`, `publish`, `unpublish`, `GET demos/{id}/versions`, `restore` | 데모 관리 (본인만) |
| `/demos/studio` | `GET demo-templates`, `POST demos/{id}/versions/editor`, `GET·POST demos/{id}/versions/{vid}/config` | 코드 편집·Mock API·템플릿 |
| `/mini-home/[userId]/demos/[demoId]` | `GET demos/{id}`, 댓글/좋아요/북마크 | 데모 상세·실행 (작성자 미니홈피 틀 안) |
| `/demos/[id]` | `GET demos/{id}` | 작성자의 미니홈피 데모 상세로 리다이렉트 |
| `/blog/write` | `POST posts` | 글 작성 |
| `/blog/[id]` | `GET posts/{id}`, 댓글 | 글 상세 |
| `/blog/[id]/edit` | `PUT posts/{id}` | 글 수정 |
| `/search` | `GET search` | 통합 검색 |
| `/news` | `GET search/news` | IT/개발 기사 (네이버 뉴스 API) |
| `/rankings` | `GET rankings` | 순위 (데모/블로그/팔로워/홈피) |
| (페이지 없음) DM 창 | `GET dm/rooms`, `GET dm/{userId}`, WebSocket | 오른쪽 아래 DM 버튼으로 여닫는 창 (`(user)/_components/DmWidget.tsx`) |
| `/my` | `GET users/{id}` | 마이페이지 |
| `/my/edit` | `PUT users/{id}` | 프로필 수정 |
| `/my/followings` | `GET follows/followings` | 팔로잉 목록 |
| `/my/followers` | `GET follows/followers` | 팔로워 목록 |
| `/my/bookmarks` | `GET users/me/bookmarks` | 북마크 목록 |
| `/my/subscriptions` | `GET subscriptions/me` | 내 구독 목록 |
| `/my/coins` | `GET coins/balance`, `POST coins/purchase` | 칩 충전/잔액 |
| `/notifications` | `GET notifications` | 알림 |
| `/settings` | `GET/PUT users/me/settings` | 설정 |
| `/admin` | `GET admin/dashboard` | 관리자 대시보드 |
| `/admin/users` | `GET admin/users` | 회원 관리 |
| `/admin/users/[userId]` | `GET admin/users/{userId}` | 회원 상세 |
| `/admin/demos` | `GET admin/demos` | 데모 관리 |
| `/admin/posts` | `GET admin/posts` | 블로그 관리 |
| `/admin/feeds` | `GET admin/feeds` | 피드 관리 |
| `/admin/comments` | `GET admin/comments` | 댓글 관리 |
| `/admin/reports` | `GET admin/reports` | 신고 관리 |
| `/admin/reports/[id]` | `PUT admin/reports/{reportId}` | 신고 처리 |
| `/admin/subscriptions` | `GET admin/subscriptions` | 구독 현황 |
| `/admin/revenue` | `GET admin/revenue` | 매출 통계 |
| `/admin/settlements` | `GET admin/settlements` | 정산 관리 |
| `/admin/coins` | `GET admin/coins` | 가상화폐 관리 |
| `/admin/coins/refunds` | `PUT admin/coins/refunds/{id}` | 환불 처리 |
| `/admin/coins/suspicious` | `GET admin/coins/suspicious` | 부정 사용 확인 |

---

## 3. 폴더와 코로케이션

```text
frontend/src/app/
├─ layout.tsx, error.tsx, loading.tsx, not-found.tsx, globals.css
├─ _components/        모든 역할이 공유하는 UI
│  └─ ui/              도메인을 모르는 공통 UI
├─ _lib/               공통 코드: api/, auth/, types/
├─ (public)/           메인, login, feeds, demos, blog, search, news, rankings
├─ (user)/             my, dm, notifications, settings, home/[userId]
└─ admin/              대시보드, users, demos, posts, feeds, comments, reports, subscriptions, revenue, settlements, coins
   └─ <route>/         page.tsx, loading.tsx, error.tsx, _components/, _hooks/, _lib/, _actions.ts
```

### 규칙

- **배치 단계:** 한 화면 → `page.tsx` 옆, 같은 기능 여러 화면 → 기능 폴더, 같은 역할 여러 기능 → `(역할)/_components`, 모든 역할 → `app/_components`·`app/_lib`.
- **위로 올리는 시점:** 처음에는 라우트 안에 만든다. 두 번째 사용처가 생기면 가장 가까운 공통 부모로 올린다.
- 다른 라우트의 `_components`·`_lib`는 import하지 않는다.
- 최상위 `components/`, `lib/`, `stores/`, `types/`, `features/` 폴더는 만들지 않는다.
- `page.tsx`는 조립만 한다. 조회는 `_lib/api.ts`, 변경은 `_actions.ts`(여러 파일이면 `_actions/`)에 둔다.
- 라우트별로 `loading.tsx`·`error.tsx`를 두고, 테스트는 대상 옆에 둔다.

---

## 4. 상태 관리

| 상태 | 위치 |
| --- | --- |
| 공유·북마크·뒤로 가기가 의미 있는 값 (필터, 정렬, 페이지, 탭, 검색어) | URL. page에서 파싱해 props로 내려준다 |
| 서버 데이터 | 서버 컴포넌트에서 조회 |
| 일시적 UI 상태 (입력 중인 값, 대화상자, 토글) | 클라이언트 `useState` |
| 여러 클라이언트 컴포넌트가 공유하는 UI 상태 | 가까운 부모의 React Context (최소화) |
| 실시간 데이터 (DM 메시지, 알림) | WebSocket + 클라이언트 상태 |

- TanStack Query와 Zustand는 쓰지 않는다. 서버 데이터를 클라이언트 전역 상태로 복사하지 않는다.
- URL 값을 `useState`로 중복해 들고 있지 않는다. 단, 입력 중인 값은 로컬 상태로 두고 제출할 때 URL에 반영한다.
- **URL 파라미터:**
  - 파싱·기본값·검증을 거친 뒤 쓴다. 기본값은 URL에서 생략한다.
  - 이름은 API 쿼리와 맞춘다.
  - URL `page`는 1부터 세고, 0부터 세는 API에는 `page-1`로 넘긴다. 필터를 바꾸면 `page`를 제거한다.
- **검색·필터는 제출할 때 한꺼번에 적용한다.** 기본은 서버 컴포넌트의 `<form method="get">`이다. 클라이언트에서 URL을 바꿔야 하면 `router.replace`를 쓴다. 페이지 이동 성격(페이지네이션·탭·상세)은 `Link`를 쓴다.
- `useSearchParams`를 쓰는 클라이언트 컴포넌트는 `<Suspense>`로 감싼다.

---

## 5. 컴포넌트

- **분리 기준:**
  - 서버/클라이언트 경계 (최우선), 독립된 책임 (`section`·`article` 단위), 반복 단위, 긴 분기 블록
  - 파일이 200줄을 넘으면 분리를 검토한다
  - 래퍼 컴포넌트나 "나중을 위한" 추상화는 만들지 않는다
- **공통 UI (`app/_components/ui/`):**
  - props만 안다. API·도메인 타입·URL에 의존하지 않는다.
  - 접근성을 내장하고, 네이티브 속성을 `...rest`로 전달한다.
- **`"use client"` 위치:**
  - 상태·이벤트·브라우저 API가 필요한 leaf에만 붙인다. page·layout에는 붙이지 않는다 (`error.tsx` 제외).
  - 서버 컴포넌트는 children으로 넘긴다.
  - `window`·`localStorage`는 `useEffect` 안에서만 쓴다. 서버 전용 파일에는 `import "server-only"`를 넣는다.
- **로직 분리:** 긴 상태 로직은 `_hooks/use*.ts`, 순수 함수는 `_lib/`에 둔다.
- **네이밍:**
  - 파일명 = 컴포넌트명 (PascalCase), 훅은 `use` camelCase
  - named export. Next 규약 파일만 default export
  - props 타입은 `Props`, 이벤트 props는 `onXxx`, 핸들러는 `handleXxx`
  - 배럴 `index.ts` 금지

---

## 6. 마크업과 접근성

- **랜드마크:**
  - 루트 layout에 본문 바로가기 (`<a href="#main-content">`)를 둔다.
  - `<main id="main-content" tabIndex={-1}>`는 **라우트 그룹 layout에 하나만** 둔다.
  - `nav`가 여럿이면 `aria-label`로 구분한다. 검색 폼은 `<search>` 또는 `role="search"`를 쓴다.
- **헤딩:** 페이지마다 `h1` 하나를 **`page.tsx`에서** 쓴다. 클라이언트 폼이나 공통 UI 안에 두지 않는다. 단계를 건너뛰지 않는다.
- **인터랙티브 요소:**
  - 동작은 `<button type>`, 이동은 `next/link`를 쓴다. `div onClick`은 금지한다.
  - 아이콘 버튼에는 `aria-label`을 둔다.
  - 상태에 맞게 `aria-pressed`·`aria-expanded`·`aria-checked`·`aria-current="page"`를 쓴다.
- **폼:**
  - 모든 입력에 화면에 보이는 `<label>`을 단다. placeholder로 대신하지 않는다.
  - 묶음은 `fieldset`+`legend`로 만든다.
  - 오류는 `aria-describedby`+`aria-invalid`로 연결하고, 실패하면 첫 오류 필드로 포커스를 옮긴다.
- **이미지:** 의미 있는 이미지는 내용을 설명하는 `alt`, 장식은 `alt=""`. "이미지"로 시작하지 않는다.
- **키보드:**
  - Tab만으로 모든 기능을 쓸 수 있어야 하고, `:focus-visible`을 유지한다. `tabIndex`는 0·-1만 쓴다.
  - 대화상자는 `<dialog>`+`showModal()`로 만들고, 닫히면 트리거로 포커스를 돌려준다.
- **동적 안내:** 결과·flash는 `role="status"`, 오류는 `role="alert"`로 알린다. live region은 미리 DOM에 둔다. 로딩 영역에는 `aria-busy`를 둔다.

---

## 7. SEO

- **루트 metadata:** `metadataBase`(`NEXT_PUBLIC_SITE_URL`), `title: { default: "찍먹", template: "%s | 찍먹" }`, description, `openGraph`(siteName·locale `ko_KR`·이미지)를 둔다. page에서는 `title: "데모 탐색"`처럼 이름만 쓴다.
- **색인 범위:** 공개 페이지(`/`, `/feeds`, `/demos`, `/demos/[id]`, `/blog/[id]`, `/home/[userId]`, `/search`, `/rankings`)만 title·description·canonical을 갖고 sitemap에 넣는다. 나머지는 `robots: { index: false }`로 처리하고, 회원·관리자는 그룹 layout에서 설정한다.
- **canonical:**
  - 목록은 필터·정렬·검색어를 빼고 `page`만 유지한다.
  - 상세는 자기 경로를 쓴다. 데이터가 없으면 `notFound()`를 부른다.
  - 상세는 `cache()`로 감싼 조회를 `generateMetadata`와 page가 함께 쓴다.
- **크롤링:** 핵심 콘텐츠는 서버에서 렌더한다. 이동과 페이지네이션은 `<a href>`(Link)로 만든다.
- **robots·sitemap:** `app/robots.ts`는 `/api/`를 disallow하고 sitemap 경로를 알린다. `app/sitemap.ts`에는 색인 대상만 넣는다.

---

## 8. 데이터·Server Actions·API

### 8-1. API 규약

- 엔드포인트·DTO 필드명·오류·페이지네이션은 기획서 API 명세와 백엔드 DTO 코드를 따른다. 기획서에 없는 API는 가정하지 않는다.
- 프론트 타입은 DTO와 이름·타입을 맞춘다.
- 금액은 원 단위 정수, 시각은 ISO 8601이다.

### 8-2. 조회

- 서버 컴포넌트에서 `_lib/api.ts`를 통해 조회한다. 공통 요청기는 `app/_lib/api/`(`server-only`)에 두고, 쿠키(JWT)를 헤더에 붙여 백엔드를 호출한다.
- 컴포넌트에서 `fetch`를 직접 호출하지 않는다.
- 응답은 `_lib/api.ts`에서 검증하고 화면에는 정규화된 타입만 넘긴다.

**공통 요청기**

```ts
// src/app/_lib/api/client.ts
import "server-only";
import { cookies } from "next/headers";

const API_ORIGIN = process.env.API_ORIGIN;

export type ApiError = { code: string; message: string };
export class ApiRequestError extends Error {
  constructor(public readonly status: number, public readonly error: ApiError) {
    super(error.message);
  }
}

export async function apiFetch<T>(
  path: `/api/v1/${string}`,
  init: RequestInit = {},
): Promise<T> {
  const response = await fetch(`${API_ORIGIN}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      cookie: (await cookies()).toString(),
      ...init.headers,
    },
    cache: "no-store",
  });
  if (!response.ok)
    throw new ApiRequestError(response.status, await response.json());
  return response.status === 204 ? (undefined as T) : response.json();
}
```

### 8-3. Server Actions

변경은 Server Action으로 하고, 흐름은 `권한 확인 → 입력 검증 → API 호출 → revalidatePath → redirect` 순서다.

1. **첫 단계에서 권한을 확인한다.** Server Action은 누구나 직접 호출할 수 있는 POST 엔드포인트다. 화면에서 버튼을 숨기는 것만으로는 막을 수 없다.
2. **필드 검증은 프론트에서도 한다.** 백엔드 오류 메시지는 사용자 친화적이지 않을 수 있으므로, 필드 옆 오류(`aria-describedby`)와 첫 오류 필드 포커스는 직접 만든다.
3. **실패하면 입력값(`values`)을 함께 돌려준다.** React 19의 action 폼은 제출 후 초기화되므로, 폼은 `defaultValue={state.values.x}`로 다시 채운다.
4. **`try`는 API 호출만 감싼다.** `redirect()`는 내부적으로 예외를 던져 동작하므로 `try` 안에 넣으면 이동이 막힌다.
5. **바뀐 데이터를 보여 주는 경로를 모두 `revalidatePath`로 갱신한다.**
6. **성공하면 `redirect`와 flash 쿼리로 결과를 알린다.**

```ts
// 예: src/app/(user)/my/edit/_actions.ts
"use server";

export async function updateProfile(
  _previous: ProfileFormState,
  formData: FormData,
): Promise<ProfileFormState> {
  const user = await requireAuth("/my/edit"); // 1
  const { state, request } = validate(formData); // 2
  if (!request) return state; // 3

  try {
    // 4
    await apiFetch(`/api/v1/users/${user.id}`, {
      method: "PUT",
      body: JSON.stringify(request),
    });
  } catch (error) {
    if (error instanceof ApiRequestError)
      return { ...state, message: error.error.message };
    throw error;
  }
  revalidatePath("/my"); // 5
  redirect("/my?updated=1"); // 6
}
```

클라이언트 폼은 `const [state, formAction, pending] = useActionState(action, initialState)`로 연결하고, `<form action={formAction}>`에서 `pending` 동안 제출 버튼을 막아 중복 제출을 방지한다.

---

## 9. 인증

찍먹은 JWT(Access + Refresh Token) 기반 인증을 사용한다.

- **토큰 저장:** httpOnly 쿠키에 둔다. localStorage에 두지 않는다.
- **소셜 로그인 흐름:** 소셜 제공자(GitHub/카카오/구글) → 백엔드에서 사용자 확인/생성 → JWT 발급 → Next.js 서버에서 httpOnly 쿠키로 저장
- **토큰 갱신:** Access Token 만료 시 Refresh Token으로 `POST auth/refresh` 호출
- **프록시:** 브라우저 호출은 `next.config.ts` rewrites로 프록시한다. 대상은 `/api/v1/:path*`만이다. 프록시 없이 호출하면 포트만 달라도 CORS 대상이다.
- **라우트 보호:** middleware 또는 layout에서 JWT 유효성을 확인하고, 인증이 필요한 페이지는 로그인으로 리다이렉트한다. 최종 권한 검증은 백엔드가 한다.
- **Server Action의 권한 확인은 계속 유지한다.** 미들웨어의 보호와 별개로, 각 Action에서 사용자 인증 상태를 검증한다.

---

## 10. 실시간 (WebSocket)

DM과 알림은 WebSocket + STOMP + SockJS로 실시간 통신한다.

- **연결:** 로그인한 사용자가 앱에 진입하면 WebSocket 연결을 맺는다. JWT를 연결 헤더에 포함한다.
- **구독:**
  - 알림: `/user/{userId}/queue/notifications`
  - DM: `/user/{userId}/queue/dm`
- **발행:** DM 전송은 `/app/dm.send`로 STOMP 메시지를 보낸다.
- **클라이언트 관리:**
  - WebSocket 연결은 `_lib/` 또는 최상위 Context에서 관리한다.
  - 재연결 로직을 포함한다 (SockJS가 자동 재연결을 지원하지 않으므로 직접 구현).
  - 페이지 전환 시 연결이 끊기지 않도록 layout 수준에서 유지한다.
- **서버 컴포넌트와의 경계:** 실시간 메시지 수신은 클라이언트 컴포넌트에서 처리한다. 초기 데이터(DM 대화 내역, 알림 목록)는 서버 컴포넌트에서 조회하고, 이후 수신되는 새 메시지만 클라이언트 상태에 추가한다.

---

## 11. 스타일 (CSS)

- **CSS Modules를 쓴다.** `globals.css`에 스타일을 몰아넣지 않고, 컴포넌트(페이지) 단위로 `*.module.css` 파일을 만든다.
- **`globals.css`에는 전역 스타일만 둔다.** CSS 변수(`:root`), 리셋(`*`, `body`), 공통 타이포그래피, 공통 유틸리티 클래스 등.
- **파일 위치:** 해당 컴포넌트 옆에 둔다. 예) `page.tsx` → `page.module.css`, `DemoCard.tsx` → `DemoCard.module.css`.
- **네이밍:** 클래스명은 camelCase로 쓴다. `styles.feedCard`, `styles.demoGrid` 등.
- **사용법:**
  ```tsx
  import styles from "./page.module.css";
  <div className={styles.feedCard}>...</div>
  ```
- **여러 클래스 조합:** 템플릿 리터럴을 쓴다. `` className={`${styles.btn} ${styles.active}`} ``

---

## 12. 기타

- 이미지는 `next/image`, 폰트는 `next/font`, 무거운 라이브러리는 `dynamic import`를 쓴다.
- 리스트 `key`에 index를 쓰지 않는다. `any`는 금지하고, 외부 입력은 검증한다.
- 브라우저에 노출하는 환경변수는 `NEXT_PUBLIC_`만 쓰고, `.env.local.example`을 커밋한다.
- 비밀번호·토큰은 action state·응답·브라우저 저장소에 넣지 않는다.

---

## 13. PR 체크리스트

- [ ] **매핑:** PR이 하나의 페이지-API 쌍을 다루고, 엔드포인트가 2장과 일치한다
- [ ] **URL 변경:** URL을 바꿨다면 라우트 목록과 기획서 API 명세를 함께 고쳤다
- [ ] **구조:** 라우트 전용 코드는 라우트 안에 있고, 공통 코드는 두 곳 이상에서 쓴다. 다른 라우트의 `_components`를 import하지 않고, page는 조립만 한다
- [ ] **컴포넌트:** `"use client"`는 leaf에만 있다. 공통 UI는 도메인·API·URL에 의존하지 않고, 래퍼나 미리 만든 추상화가 없으며, 네이밍·export 규칙을 따른다
- [ ] **데이터·API:** 엔드포인트·타입이 기획서·DTO와 일치하고, 없는 API를 가정하지 않았다
- [ ] **변경:** 권한을 확인하는 Server Action으로 하고, `revalidatePath`로 관련 경로를 모두 갱신한다
- [ ] **상태:** 공유 의미가 있는 상태는 URL에 두고 page에서 검증한다. URL과 `useState`에 중복으로 두지 않고, 서버 데이터를 전역 상태에 복사하지 않는다
- [ ] **랜드마크:** skip link가 있고, `main`은 그룹 layout에 하나, h1은 page에 하나이며 헤딩 순서를 지킨다
- [ ] **요소:** button·a를 의미대로 쓰고, 아이콘 버튼에 `aria-label`, 이미지에 `alt`가 있다. 보이는 label과 오류 연결이 있다
- [ ] **키보드·안내:** 키보드만으로 쓸 수 있고 포커스가 보이며, 대화상자 포커스를 관리한다. 동적 변경은 `status`·`alert`로 안내한다
- [ ] **SEO:** 공개 페이지에 고유한 title·description·canonical이 있고, 핵심 콘텐츠가 초기 HTML에 있으며, 이동은 Link다. 없는 데이터는 `notFound()`, 인증 페이지는 noindex
- [ ] **검증:** `npm run lint`, `npm run build`, Tab 키로 전체 흐름 확인
