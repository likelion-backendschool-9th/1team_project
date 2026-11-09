-- 블로그 글·데모·북마크·구독 시연용 시드 데이터
-- seed-data.sql(회원·피드·팔로우·태그)과 seed-minihome.sql을 먼저 넣은 뒤 실행한다. 여러 번 실행해도 중복되지 않는다.
-- 사용법: psql -h localhost -U dipsource -d dipsource -f backend/scripts/dev-seed/seed-content.sql
--
-- 넣는 것
--   블로그 글 24개(id 1001~1024, 마크다운 본문, 일부 유료), 글 태그·좋아요
--   데모 12개(id 1001~1012, 목록·미니홈피 표시용). 업로드한 파일이 없어서 '실행'은 되지 않는다.
--     실행되는 데모는 화면의 '데모 올리기'로 직접 올려야 한다(파일이 R2에 있어야 함).
--   팀 계정(시드팀01~05, id 21~25)의 피드·글·데모 북마크와 블로그 구독, 미니홈피 대표 글
-- 글·데모 id는 1001부터 쓰고, 마지막에 시퀀스를 최댓값으로 맞춰 앱에서 새로 만들 때 id가 겹치지 않게 한다.

SET client_encoding = 'UTF8';

BEGIN;

-- 태그 시퀀스가 시드의 고정 id보다 뒤처져 있으면 새 태그 INSERT가 충돌하므로 먼저 맞춘다.
SELECT setval(pg_get_serial_sequence('tags', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM tags), 1));

-- ========== tags (없는 것만) ==========
INSERT INTO tags (name) VALUES ('accessibility'), ('ai'), ('aws'), ('c#'), ('canvas'), ('career'), ('css'), ('dart'), ('design-system'), ('devops'), ('docker'), ('elasticsearch'), ('figma'), ('flutter'), ('frontend'), ('game'), ('java'), ('javascript'), ('jpa'), ('kafka'), ('next-js'), ('postgresql'), ('python'), ('react'), ('redis'), ('security'), ('spring-boot'), ('typescript'), ('unity'), ('webgl'), ('websocket'), ('찍먹') ON CONFLICT (name) DO NOTHING;

-- ========== posts ==========
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1001, 21, NULL, 'JPA N+1 문제, fetch join·EntityGraph·BatchSize로 비교해 보기', 'BACKEND', $md$# JPA N+1 문제, fetch join·EntityGraph·BatchSize로 비교해 보기

피드 목록 API를 만들다 쿼리가 41개 나가는 걸 보고 정리한 글이에요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

세 방법 모두 쿼리 수는 줄지만 페이징과 함께 쓸 때 차이가 커요.

```java
@Query("select f from Feed f join fetch f.author")
List<Feed> findAllWithAuthor();
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 원인 | 로그와 지표로 확인 |
| 해결 | 코드와 설정 변경 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #java
$md$, NULL, false, 0, 'ACTIVE', 416, 0, 0, NULL, NOW() - INTERVAL '24 days', NOW() - INTERVAL '24 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1002, 22, NULL, 'Next.js App Router에서 Server Action으로 폼 다루기', 'FRONTEND', $md$# Next.js App Router에서 Server Action으로 폼 다루기

API 라우트 없이 폼을 처리하는 방법을 실제 화면 기준으로 정리했어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

`useActionState`로 검증 오류와 성공 메시지를 한 번에 다룰 수 있어요.

```tsx
const [state, action, pending] = useActionState(save, initialState);
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 원인 | 로그와 지표로 확인 |
| 결과 | 전후 수치 비교 |
| 해결 | 코드와 설정 변경 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #next-js
$md$, NULL, false, 0, 'ACTIVE', 1307, 0, 0, NULL, NOW() - INTERVAL '23 days', NOW() - INTERVAL '23 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1003, 23, NULL, 'Unity WebGL 빌드 용량 38MB를 9MB로 줄인 과정', 'GAME', $md$# Unity WebGL 빌드 용량 38MB를 9MB로 줄인 과정

브라우저에서 바로 플레이되는 데모를 만들려면 용량이 가장 큰 적이었어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

텍스처 압축과 코드 스트리핑, Brotli 압축 순서로 효과가 컸어요.

```bash
# Player Settings > Publishing
# Compression Format: Brotli
# Strip Engine Code: on
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 원인 | 로그와 지표로 확인 |
| 결과 | 전후 수치 비교 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #unity
$md$, NULL, true, 10, 'ACTIVE', 413, 0, 0, NULL, NOW() - INTERVAL '22 days', NOW() - INTERVAL '22 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1004, 24, NULL, 'Redis 분산 락으로 칩 결제 중복 차감 막기', 'BACKEND', $md$# Redis 분산 락으로 칩 결제 중복 차감 막기

같은 결제 요청이 두 번 들어와 칩이 두 번 빠지는 버그를 고친 기록이에요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

락 획득 실패 시 재시도보다 멱등 키가 더 단순한 해법일 때도 있어요.

```java
RLock lock = redisson.getLock("chip:" + userId);
if (lock.tryLock(1, 3, TimeUnit.SECONDS)) { /* 차감 */ }
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 결과 | 전후 수치 비교 |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 원인 | 로그와 지표로 확인 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #redis
$md$, NULL, true, 15, 'ACTIVE', 460, 0, 0, NULL, NOW() - INTERVAL '21 days', NOW() - INTERVAL '21 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1005, 25, NULL, '디자인 토큰으로 다크 모드 한 번에 맞추기', 'UXUI', $md$# 디자인 토큰으로 다크 모드 한 번에 맞추기

색을 컴포넌트마다 고치다 지쳐서 토큰부터 다시 잡았어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

의미 이름(surface, ink, accent)으로 묶으면 테마 전환이 쉬워져요.

```css
:root { --surface: #fffaf0; --ink: #2b2118; }
[data-theme=dark] { --surface: #1f1b17; --ink: #f5efe3; }
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 해결 | 코드와 설정 변경 |
| 결과 | 전후 수치 비교 |
| 원인 | 로그와 지표로 확인 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #css
$md$, NULL, false, 0, 'ACTIVE', 370, 0, 0, NULL, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1006, 1, NULL, 'Spring Boot 3 가상 스레드, 정말 빨라질까?', 'BACKEND', $md$# Spring Boot 3 가상 스레드, 정말 빨라질까?

설정 한 줄로 켤 수 있다길래 부하 테스트로 직접 확인해 봤어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

I/O 대기가 긴 API에서는 효과가 컸지만 CPU 위주 작업은 차이가 없었어요.

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 해결 | 코드와 설정 변경 |
| 원인 | 로그와 지표로 확인 |
| 결과 | 전후 수치 비교 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #spring-boot
$md$, NULL, false, 0, 'ACTIVE', 370, 0, 0, NULL, NOW() - INTERVAL '19 days', NOW() - INTERVAL '19 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1007, 2, NULL, 'React 19 use()로 데이터 불러오기 정리', 'FRONTEND', $md$# React 19 use()로 데이터 불러오기 정리

Suspense와 함께 쓰는 새로운 데이터 패턴을 예제로 정리했어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

Promise를 props로 내려 주고 자식에서 use()로 꺼내 쓰는 흐름이에요.

```tsx
function Profile({ user }: { user: Promise<User> }) {
  const data = use(user);
  return <p>{data.name}</p>;
}
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 결과 | 전후 수치 비교 |
| 해결 | 코드와 설정 변경 |
| 문제 상황 | 재현 방법을 먼저 정리 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #react
$md$, NULL, false, 0, 'ACTIVE', 485, 0, 0, NULL, NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1008, 3, NULL, 'PostgreSQL 인덱스, EXPLAIN ANALYZE로 읽는 법', 'BACKEND', $md$# PostgreSQL 인덱스, EXPLAIN ANALYZE로 읽는 법

느린 쿼리를 만나면 가장 먼저 보는 실행 계획 읽는 법을 정리했어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

Seq Scan이 무조건 나쁜 건 아니에요. 행 수 추정치와 실제 값을 비교하세요.

```sql
EXPLAIN ANALYZE
SELECT * FROM feeds WHERE user_id = 21 ORDER BY id DESC LIMIT 20;
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 원인 | 로그와 지표로 확인 |
| 해결 | 코드와 설정 변경 |
| 결과 | 전후 수치 비교 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #postgresql
$md$, NULL, true, 20, 'ACTIVE', 424, 0, 0, NULL, NOW() - INTERVAL '17 days', NOW() - INTERVAL '17 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1009, 4, NULL, 'LLM API 응답을 스트리밍으로 보여 주기', 'AI', $md$# LLM API 응답을 스트리밍으로 보여 주기

답변이 다 만들어질 때까지 기다리게 하지 않으려고 SSE로 바꿨어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

서버에서는 청크 단위로 흘려보내고, 화면에서는 이어 붙이기만 하면 돼요.

```python
for chunk in client.stream(prompt):
    yield f"data: {chunk}\n\n"
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 결과 | 전후 수치 비교 |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 원인 | 로그와 지표로 확인 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #python
$md$, NULL, false, 0, 'ACTIVE', 365, 0, 0, NULL, NOW() - INTERVAL '16 days', NOW() - INTERVAL '16 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1010, 5, NULL, 'STOMP + Spring WebSocket으로 1:1 DM 만들기', 'BACKEND', $md$# STOMP + Spring WebSocket으로 1:1 DM 만들기

찍먹 DM 기능을 만들면서 겪은 연결·인증 문제를 정리했어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

핸드셰이크 단계에서 쿠키로 인증하고, 구독 경로는 사용자별로 나눴어요.

```java
registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 해결 | 코드와 설정 변경 |
| 결과 | 전후 수치 비교 |
| 원인 | 로그와 지표로 확인 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #spring-boot
$md$, NULL, false, 0, 'ACTIVE', 688, 0, 0, NULL, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1011, 6, NULL, 'Docker Compose로 로컬 개발 환경 한 번에 띄우기', 'BACKEND', $md$# Docker Compose로 로컬 개발 환경 한 번에 띄우기

새 팀원이 첫날 환경 설정으로 하루를 쓰지 않게 만든 구성이에요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

DB, 검색엔진, 백엔드, 프론트를 한 파일로 묶고 healthcheck로 순서를 맞췄어요.

```yaml
services:
  db:
    image: postgres:16
    healthcheck:
      test: ["CMD", "pg_isready"]
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 결과 | 전후 수치 비교 |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 해결 | 코드와 설정 변경 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #docker
$md$, NULL, false, 0, 'ACTIVE', 405, 0, 0, NULL, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1012, 7, NULL, 'CSS Grid와 Subgrid로 카드 높이 맞추기', 'FRONTEND', $md$# CSS Grid와 Subgrid로 카드 높이 맞추기

카드마다 제목 길이가 달라 버튼 위치가 들쭉날쭉하던 문제를 해결했어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

부모 그리드의 행을 subgrid로 물려받으면 카드끼리 줄이 맞아요.

```css
.card { display: grid; grid-row: span 3; grid-template-rows: subgrid; }
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 결과 | 전후 수치 비교 |
| 해결 | 코드와 설정 변경 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #css
$md$, NULL, false, 0, 'ACTIVE', 720, 0, 0, NULL, NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1013, 8, NULL, '트랜잭션 전파 REQUIRES_NEW, 언제 써야 할까', 'BACKEND', $md$# 트랜잭션 전파 REQUIRES_NEW, 언제 써야 할까

알림 저장 실패가 결제 전체를 롤백시키는 사고 뒤에 정리한 글이에요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

부가 기능은 별도 트랜잭션이나 이벤트로 떼어 내는 게 안전했어요.

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveNotification(...) { }
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 원인 | 로그와 지표로 확인 |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 결과 | 전후 수치 비교 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #spring-boot
$md$, NULL, true, 10, 'ACTIVE', 1294, 0, 0, NULL, NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1014, 9, NULL, 'Flutter로 만든 앱을 웹 데모로 올리기', 'MOBILE', $md$# Flutter로 만든 앱을 웹 데모로 올리기

모바일 앱도 찍먹할 수 있게 웹 빌드로 데모를 올려 봤어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

렌더러 선택과 첫 로딩 크기가 체감 속도를 좌우했어요.

```bash
flutter build web --release --web-renderer canvaskit
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 원인 | 로그와 지표로 확인 |
| 결과 | 전후 수치 비교 |
| 문제 상황 | 재현 방법을 먼저 정리 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #flutter
$md$, NULL, false, 0, 'ACTIVE', 567, 0, 0, NULL, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1015, 10, NULL, 'AWS S3 Presigned URL로 이미지 업로드', 'BACKEND', $md$# AWS S3 Presigned URL로 이미지 업로드

서버를 거치지 않고 브라우저에서 바로 업로드하게 바꾼 과정이에요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

URL 유효 시간과 Content-Type 제한을 꼭 같이 걸어 두세요.

```java
PresignedPutObjectRequest req = presigner.presignPutObject(r -> r.signatureDuration(Duration.ofMinutes(5)));
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 결과 | 전후 수치 비교 |
| 해결 | 코드와 설정 변경 |
| 원인 | 로그와 지표로 확인 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #aws
$md$, NULL, false, 0, 'ACTIVE', 1382, 0, 0, NULL, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1016, 12, NULL, '대규모 트래픽을 견디는 이벤트 드리븐 설계 1편', 'BACKEND', $md$# 대규모 트래픽을 견디는 이벤트 드리븐 설계 1편

주문·결제·알림을 이벤트로 나누면서 얻은 것과 잃은 것을 정리하는 연재의 첫 편이에요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

Outbox 패턴으로 DB 저장과 이벤트 발행을 한 트랜잭션처럼 묶었어요.

```java
outboxRepository.save(new OutboxEvent("order.created", payload));
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 결과 | 전후 수치 비교 |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 원인 | 로그와 지표로 확인 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #kafka
$md$, NULL, true, 30, 'ACTIVE', 1424, 0, 0, NULL, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1017, 15, NULL, 'next/image 없이 외부 이미지 안전하게 보여 주기', 'FRONTEND', $md$# next/image 없이 외부 이미지 안전하게 보여 주기

사용자가 넣은 외부 이미지는 크기를 모를 때가 많아서 고민했던 내용이에요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

lazy 로딩과 max-width, alt 처리만 지켜도 레이아웃이 깨지지 않아요.

```tsx
<img src={url} alt={alt} loading="lazy" style={{ maxWidth: "100%" }} />
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 원인 | 로그와 지표로 확인 |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 해결 | 코드와 설정 변경 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #next-js
$md$, NULL, false, 0, 'ACTIVE', 1071, 0, 0, NULL, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1018, 17, NULL, '부트캠프 6개월, 팀 프로젝트에서 배운 협업 규칙', 'CAREER', $md$# 부트캠프 6개월, 팀 프로젝트에서 배운 협업 규칙

코드보다 더 어려웠던 건 같이 일하는 방법이었어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

브랜치 규칙, 리뷰 기준, 회의록 세 가지만 잘 지켜도 충돌이 확 줄었어요.

```text
feature/기능-이름 → dev → main
리뷰 1명 이상 승인 후 머지
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 결과 | 전후 수치 비교 |
| 해결 | 코드와 설정 변경 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #career
$md$, NULL, false, 0, 'ACTIVE', 41, 0, 0, NULL, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1019, 19, NULL, 'GitHub Actions로 테스트·빌드·배포 자동화', 'BACKEND', $md$# GitHub Actions로 테스트·빌드·배포 자동화

PR마다 테스트가 돌고, 머지되면 서버에 배포되도록 만들었어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

시크릿은 저장소 설정에 두고 워크플로 파일에는 이름만 써요.

```yaml
on:
  push:
    branches: [main]
jobs:
  build:
    runs-on: ubuntu-latest
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 원인 | 로그와 지표로 확인 |
| 결과 | 전후 수치 비교 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #docker
$md$, NULL, false, 0, 'ACTIVE', 279, 0, 0, NULL, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1020, 21, NULL, 'Spring Security 소셜 로그인과 context-path 함정', 'BACKEND', $md$# Spring Security 소셜 로그인과 context-path 함정

context-path를 붙인 뒤 소셜 로그인이 모두 깨졌던 이유를 정리했어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

리다이렉트 주소에 context-path가 빠지는지 꼭 확인하세요.

```java
response.sendRedirect(request.getContextPath() + "/oauth2/authorization/" + provider);
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 결과 | 전후 수치 비교 |
| 문제 상황 | 재현 방법을 먼저 정리 |
| 해결 | 코드와 설정 변경 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #spring-boot
$md$, NULL, false, 0, 'ACTIVE', 179, 0, 0, NULL, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1021, 22, NULL, '마크다운 렌더러를 라이브러리 없이 만들어 보기', 'FRONTEND', $md$# 마크다운 렌더러를 라이브러리 없이 만들어 보기

블로그 본문을 안전하게 그리려고 작은 마크다운 해석기를 직접 만들었어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

HTML 문자열 대신 React 요소로 만들면 XSS 걱정을 크게 덜 수 있어요.

```ts
const INLINE = /(`[^`]+`)|(\*\*[^*]+\*\*)/g;
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 해결 | 코드와 설정 변경 |
| 결과 | 전후 수치 비교 |
| 원인 | 로그와 지표로 확인 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #react
$md$, NULL, true, 5, 'ACTIVE', 53, 0, 0, NULL, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1022, 23, NULL, '브라우저 게임에서 requestAnimationFrame 루프 다루기', 'GAME', $md$# 브라우저 게임에서 requestAnimationFrame 루프 다루기

탭을 바꿨다 돌아오면 캐릭터가 순간이동하던 버그를 고친 기록이에요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

프레임 사이 시간(delta)을 상한으로 잘라 주면 튀는 현상이 사라져요.

```js
const dt = Math.min((now - last) / 1000, 0.05);
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 해결 | 코드와 설정 변경 |
| 결과 | 전후 수치 비교 |
| 원인 | 로그와 지표로 확인 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #javascript
$md$, NULL, false, 0, 'ACTIVE', 889, 0, 0, NULL, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1023, 24, NULL, 'Elasticsearch 한글 검색, nori로 형태소 분석하기', 'BACKEND', $md$# Elasticsearch 한글 검색, nori로 형태소 분석하기

'스프링부트'로 검색하면 '스프링 부트' 글이 안 나오던 문제를 해결했어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

사용자 사전과 동의어 필터를 같이 쓰면 검색 품질이 꽤 좋아져요.

```json
{ "tokenizer": "nori_tokenizer", "filter": ["lowercase", "synonym"] }
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 원인 | 로그와 지표로 확인 |
| 해결 | 코드와 설정 변경 |
| 결과 | 전후 수치 비교 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #elasticsearch
$md$, NULL, false, 0, 'ACTIVE', 1383, 0, 0, NULL, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO posts (id, user_id, demo_id, title, category, content, thumbnail_url, is_paid, price_chips, status, view_count, like_count, comment_count, deleted_at, created_at, updated_at) VALUES (1024, 25, NULL, '접근성 체크리스트: 키보드만으로 서비스 써 보기', 'UXUI', $md$# 접근성 체크리스트: 키보드만으로 서비스 써 보기

마우스를 치우고 하루 동안 우리 서비스를 써 봤어요.

## 배경

처음에는 **단순한 문제**라고 생각했어요. 그런데 실제 데이터로 확인해 보니 생각보다 영향이 컸어요.

- 재현 조건을 먼저 정리했어요
- 측정 지표를 정하고 *전후를 비교*했어요
- 팀원과 리뷰하며 방향을 맞췄어요

## 핵심 정리

포커스 표시, 탭 순서, 모달 포커스 가두기가 가장 많이 걸렸어요.

```css
:focus-visible { outline: 3px solid #ffc21a; outline-offset: 2px; }
```

> 정답이 하나인 문제는 드물어요. 우리 상황에 맞는 선택지를 고르는 게 중요했어요.

## 진행 순서

1. 문제를 작게 재현하기
2. 원인 후보를 세 가지로 좁히기
3. 하나씩 바꿔 보며 측정하기

| 단계 | 한 일 |
| --- | --- |
| 원인 | 로그와 지표로 확인 |
| 해결 | 코드와 설정 변경 |
| 결과 | 전후 수치 비교 |

## 마치며

비슷한 문제를 겪는 분께 도움이 되면 좋겠어요. 궁금한 점은 댓글로 남겨 주세요. #찍먹 #accessibility
$md$, NULL, false, 0, 'ACTIVE', 587, 0, 0, NULL, NOW() - INTERVAL '1 days', NOW() - INTERVAL '1 days') ON CONFLICT (id) DO NOTHING;

-- ========== post_tags ==========
INSERT INTO post_tags (post_id, tag_id) SELECT 1001, id FROM tags WHERE name = 'java' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1001, id FROM tags WHERE name = 'jpa' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1001, id FROM tags WHERE name = 'spring-boot' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1001, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1002, id FROM tags WHERE name = 'next-js' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1002, id FROM tags WHERE name = 'react' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1002, id FROM tags WHERE name = 'typescript' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1002, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1003, id FROM tags WHERE name = 'unity' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1003, id FROM tags WHERE name = 'webgl' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1003, id FROM tags WHERE name = 'c#' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1003, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1004, id FROM tags WHERE name = 'redis' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1004, id FROM tags WHERE name = 'spring-boot' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1004, id FROM tags WHERE name = 'java' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1004, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1005, id FROM tags WHERE name = 'css' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1005, id FROM tags WHERE name = 'figma' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1005, id FROM tags WHERE name = 'design-system' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1005, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1006, id FROM tags WHERE name = 'spring-boot' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1006, id FROM tags WHERE name = 'java' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1006, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1007, id FROM tags WHERE name = 'react' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1007, id FROM tags WHERE name = 'typescript' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1007, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1008, id FROM tags WHERE name = 'postgresql' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1008, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1009, id FROM tags WHERE name = 'python' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1009, id FROM tags WHERE name = 'ai' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1009, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1010, id FROM tags WHERE name = 'spring-boot' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1010, id FROM tags WHERE name = 'websocket' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1010, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1011, id FROM tags WHERE name = 'docker' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1011, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1012, id FROM tags WHERE name = 'css' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1012, id FROM tags WHERE name = 'frontend' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1012, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1013, id FROM tags WHERE name = 'spring-boot' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1013, id FROM tags WHERE name = 'java' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1013, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1014, id FROM tags WHERE name = 'flutter' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1014, id FROM tags WHERE name = 'dart' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1014, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1015, id FROM tags WHERE name = 'aws' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1015, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1016, id FROM tags WHERE name = 'kafka' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1016, id FROM tags WHERE name = 'spring-boot' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1016, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1017, id FROM tags WHERE name = 'next-js' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1017, id FROM tags WHERE name = 'react' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1017, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1018, id FROM tags WHERE name = 'career' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1018, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1019, id FROM tags WHERE name = 'docker' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1019, id FROM tags WHERE name = 'devops' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1019, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1020, id FROM tags WHERE name = 'spring-boot' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1020, id FROM tags WHERE name = 'security' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1020, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1021, id FROM tags WHERE name = 'react' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1021, id FROM tags WHERE name = 'typescript' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1021, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1022, id FROM tags WHERE name = 'javascript' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1022, id FROM tags WHERE name = 'game' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1022, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1023, id FROM tags WHERE name = 'elasticsearch' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1023, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1024, id FROM tags WHERE name = 'accessibility' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1024, id FROM tags WHERE name = 'css' ON CONFLICT DO NOTHING;
INSERT INTO post_tags (post_id, tag_id) SELECT 1024, id FROM tags WHERE name = '찍먹' ON CONFLICT DO NOTHING;

-- ========== post_likes (회원 1~32 중 무작위) ==========
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (2, 1001, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (6, 1001, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (11, 1001, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (18, 1001, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (23, 1001, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (27, 1001, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (29, 1001, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (30, 1001, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (31, 1001, NOW() - INTERVAL '13 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (8, 1002, NOW() - INTERVAL '13 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (12, 1002, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (14, 1002, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (13, 1003, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (16, 1003, NOW() - INTERVAL '7 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (14, 1004, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (29, 1004, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (6, 1005, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (12, 1005, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (13, 1005, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (14, 1005, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (17, 1005, NOW() - INTERVAL '7 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (18, 1005, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (19, 1005, NOW() - INTERVAL '7 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (27, 1005, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (30, 1005, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (31, 1005, NOW() - INTERVAL '7 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (2, 1006, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (9, 1006, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (14, 1006, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (16, 1006, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (17, 1006, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (18, 1006, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (19, 1006, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (20, 1006, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (21, 1006, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (25, 1006, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (26, 1006, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (31, 1006, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (3, 1007, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (6, 1007, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (9, 1007, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (30, 1007, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (31, 1007, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (1, 1008, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (2, 1008, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (9, 1008, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (11, 1008, NOW() - INTERVAL '5 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (13, 1008, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (14, 1008, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (15, 1008, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (21, 1008, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (24, 1008, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (25, 1008, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (26, 1008, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (27, 1008, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (2, 1009, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (12, 1009, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (13, 1009, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (21, 1009, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (25, 1009, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (31, 1009, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (4, 1010, NOW() - INTERVAL '13 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (6, 1010, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (27, 1010, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (30, 1010, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (1, 1011, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (5, 1011, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (7, 1011, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (8, 1011, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (17, 1011, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (18, 1011, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (21, 1011, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (28, 1011, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (31, 1011, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (2, 1012, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (6, 1012, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (10, 1012, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (18, 1012, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (24, 1012, NOW() - INTERVAL '5 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (25, 1012, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (1, 1013, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (9, 1013, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (14, 1013, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (20, 1013, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (25, 1013, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (26, 1013, NOW() - INTERVAL '13 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (27, 1013, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (29, 1013, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (30, 1013, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (31, 1013, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (1, 1014, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (8, 1014, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (10, 1014, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (12, 1014, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (21, 1014, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (25, 1014, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (30, 1014, NOW() - INTERVAL '5 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (32, 1014, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (1, 1015, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (5, 1015, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (6, 1015, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (7, 1015, NOW() - INTERVAL '7 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (8, 1015, NOW() - INTERVAL '7 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (12, 1015, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (14, 1015, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (17, 1015, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (20, 1015, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (22, 1015, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (23, 1015, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (27, 1015, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (30, 1015, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (32, 1015, NOW() - INTERVAL '13 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (1, 1016, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (5, 1016, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (9, 1016, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (11, 1016, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (13, 1016, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (15, 1016, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (22, 1016, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (25, 1016, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (27, 1016, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (32, 1016, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (2, 1017, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (12, 1017, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (17, 1017, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (23, 1017, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (27, 1017, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (1, 1018, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (2, 1018, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (6, 1018, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (7, 1018, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (8, 1018, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (11, 1018, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (13, 1018, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (14, 1018, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (20, 1018, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (28, 1018, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (2, 1019, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (5, 1019, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (6, 1019, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (13, 1019, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (16, 1019, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (18, 1019, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (20, 1019, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (22, 1019, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (27, 1019, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (28, 1019, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (29, 1019, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (7, 1020, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (12, 1020, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (13, 1020, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (14, 1020, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (30, 1020, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (2, 1021, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (4, 1021, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (5, 1021, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (8, 1021, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (11, 1021, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (12, 1021, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (16, 1021, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (18, 1021, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (3, 1022, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (4, 1022, NOW() - INTERVAL '5 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (5, 1022, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (6, 1022, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (9, 1022, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (12, 1022, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (14, 1022, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (16, 1022, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (20, 1022, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (25, 1022, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (27, 1022, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (1, 1023, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (4, 1023, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (14, 1023, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (16, 1023, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (25, 1023, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (28, 1023, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (5, 1024, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (6, 1024, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (8, 1024, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (13, 1024, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (16, 1024, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (18, 1024, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (20, 1024, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO post_likes (user_id, post_id, created_at) VALUES (30, 1024, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
UPDATE posts p SET like_count = (SELECT COUNT(*) FROM post_likes l WHERE l.post_id = p.id) WHERE p.id BETWEEN 1001 AND 1024;

-- ========== demos (파일 없음: 목록·미니홈피 표시용) ==========
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1001, 21, 'Spring Batch 모니터', '배치 잡 실행 이력을 실시간 대시보드로 보여 주는 데모예요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 695, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1002, 22, '마우스에 반응하는 파티클', '마우스를 움직이면 입자들이 따라오고 흩어지는 인터랙션 데모예요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 831, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1003, 23, '픽셀 러너', '점프 한 번으로 장애물을 피하는 브라우저 미니 게임이에요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 792, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1004, 24, '칩 결제 동시성 실험실', '같은 결제를 동시에 100번 보냈을 때 락이 있는 경우와 없는 경우를 비교해요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 680, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1005, 25, '다크 모드 토큰 놀이터', '디자인 토큰을 바꾸면 컴포넌트 전체 색이 바로 바뀌는 데모예요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 543, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1006, 2, '리액트 칸반 보드', '카드를 끌어다 놓아 할 일을 옮기는 칸반 보드예요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 653, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1007, 3, '쿼리 실행 계획 시각화', 'EXPLAIN 결과를 트리로 그려 주는 도구예요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 119, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1008, 5, '실시간 채팅앱', 'STOMP로 만든 1:1 채팅을 바로 체험해 보세요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 269, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1009, 7, 'CSS Subgrid 갤러리', '제목 길이가 달라도 줄이 맞는 카드 갤러리예요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 751, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1010, 9, '플러터 할 일 앱', 'Flutter 웹으로 빌드한 할 일 관리 앱이에요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 319, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1011, 15, '마크다운 에디터', '작성하면서 바로 미리보기가 되는 마크다운 편집기예요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 26, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days') ON CONFLICT (id) DO NOTHING;
INSERT INTO demos (id, user_id, title, description, demo_type, file_path, api_url, openapi_json, scenario_json, external_url, thumbnail, play_count, like_count, feedback_count, status, version, deleted_at, current_version_id, thumbnail_manual, created_at, updated_at) VALUES (1012, 22, '타이핑 속도 측정기', '코드 한 줄을 얼마나 빨리 치는지 재 보는 데모예요.', 'STATIC', NULL, NULL, NULL, NULL, NULL, NULL, 505, 0, 0, 'ACTIVE', 1, NULL, NULL, false, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days') ON CONFLICT (id) DO NOTHING;

INSERT INTO demo_tags (demo_id, tag_id) SELECT 1001, id FROM tags WHERE name = 'spring-boot' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1001, id FROM tags WHERE name = 'java' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1002, id FROM tags WHERE name = 'javascript' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1002, id FROM tags WHERE name = 'canvas' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1003, id FROM tags WHERE name = 'javascript' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1003, id FROM tags WHERE name = 'game' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1004, id FROM tags WHERE name = 'redis' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1004, id FROM tags WHERE name = 'spring-boot' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1005, id FROM tags WHERE name = 'css' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1005, id FROM tags WHERE name = 'design-system' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1006, id FROM tags WHERE name = 'react' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1006, id FROM tags WHERE name = 'typescript' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1007, id FROM tags WHERE name = 'postgresql' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1008, id FROM tags WHERE name = 'websocket' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1008, id FROM tags WHERE name = 'react' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1009, id FROM tags WHERE name = 'css' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1010, id FROM tags WHERE name = 'flutter' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1010, id FROM tags WHERE name = 'dart' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1011, id FROM tags WHERE name = 'react' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1011, id FROM tags WHERE name = 'next-js' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1012, id FROM tags WHERE name = 'typescript' ON CONFLICT DO NOTHING;
INSERT INTO demo_tags (demo_id, tag_id) SELECT 1012, id FROM tags WHERE name = 'react' ON CONFLICT DO NOTHING;

INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (1, 1001, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (4, 1001, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (6, 1001, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (7, 1001, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (13, 1001, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (15, 1001, NOW() - INTERVAL '5 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (19, 1001, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (26, 1001, NOW() - INTERVAL '7 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (27, 1001, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (29, 1001, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (30, 1001, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (6, 1002, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (9, 1002, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (12, 1002, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (16, 1002, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (23, 1002, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (24, 1002, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (25, 1002, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (30, 1002, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (2, 1003, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (18, 1003, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (24, 1003, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (26, 1003, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (28, 1003, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (10, 1004, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (15, 1004, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (16, 1004, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (17, 1004, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (25, 1004, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (27, 1004, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (1, 1005, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (2, 1005, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (4, 1005, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (7, 1005, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (10, 1005, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (13, 1005, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (14, 1005, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (18, 1005, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (21, 1005, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (27, 1005, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (30, 1005, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (31, 1005, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (5, 1006, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (14, 1006, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (16, 1006, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (17, 1006, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (4, 1007, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (10, 1007, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (12, 1007, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (13, 1007, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (17, 1007, NOW() - INTERVAL '12 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (18, 1007, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (26, 1007, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (29, 1007, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (30, 1007, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (31, 1007, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (1, 1008, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (3, 1008, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (4, 1008, NOW() - INTERVAL '16 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (8, 1008, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (10, 1008, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (11, 1008, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (14, 1008, NOW() - INTERVAL '17 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (18, 1008, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (20, 1008, NOW() - INTERVAL '11 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (21, 1008, NOW() - INTERVAL '13 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (2, 1009, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (11, 1009, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (13, 1009, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (15, 1009, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (17, 1009, NOW() - INTERVAL '10 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (19, 1009, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (21, 1009, NOW() - INTERVAL '7 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (24, 1009, NOW() - INTERVAL '13 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (27, 1009, NOW() - INTERVAL '5 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (28, 1009, NOW() - INTERVAL '15 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (10, 1010, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (11, 1010, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (14, 1010, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (15, 1010, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (16, 1010, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (19, 1010, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (20, 1010, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (22, 1010, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (26, 1010, NOW() - INTERVAL '14 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (31, 1010, NOW() - INTERVAL '20 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (5, 1011, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (24, 1011, NOW() - INTERVAL '13 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (25, 1011, NOW() - INTERVAL '19 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (31, 1011, NOW() - INTERVAL '18 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (32, 1011, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (8, 1012, NOW() - INTERVAL '13 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (21, 1012, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO demo_likes (user_id, demo_id, created_at) VALUES (28, 1012, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
UPDATE demos d SET like_count = (SELECT COUNT(*) FROM demo_likes l WHERE l.demo_id = d.id) WHERE d.id BETWEEN 1001 AND 1012;

-- ========== 팀 계정 북마크 (내 북마크 화면 확인용) ==========
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 21, id, NOW() - INTERVAL '3 days' FROM feeds WHERE id = 48 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 21, id, NOW() - INTERVAL '2 days' FROM feeds WHERE id = 125 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 21, id, NOW() - INTERVAL '1 days' FROM feeds WHERE id = 146 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 21, id, NOW() - INTERVAL '10 days' FROM feeds WHERE id = 194 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (21, 1006, NOW() - INTERVAL '9 days') ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (21, 1021, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (21, 1024, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO bookmarks (user_id, demo_id, created_at) VALUES (21, 1009, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO bookmarks (user_id, demo_id, created_at) VALUES (21, 1012, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 22, id, NOW() - INTERVAL '6 days' FROM feeds WHERE id = 5 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 22, id, NOW() - INTERVAL '0 days' FROM feeds WHERE id = 8 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 22, id, NOW() - INTERVAL '10 days' FROM feeds WHERE id = 53 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 22, id, NOW() - INTERVAL '3 days' FROM feeds WHERE id = 144 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (22, 1018, NOW() - INTERVAL '5 days') ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (22, 1019, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (22, 1007, NOW() - INTERVAL '8 days') ON CONFLICT DO NOTHING;
INSERT INTO bookmarks (user_id, demo_id, created_at) VALUES (22, 1009, NOW() - INTERVAL '7 days') ON CONFLICT DO NOTHING;
INSERT INTO bookmarks (user_id, demo_id, created_at) VALUES (22, 1001, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 23, id, NOW() - INTERVAL '3 days' FROM feeds WHERE id = 6 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 23, id, NOW() - INTERVAL '5 days' FROM feeds WHERE id = 18 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 23, id, NOW() - INTERVAL '1 days' FROM feeds WHERE id = 47 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 23, id, NOW() - INTERVAL '1 days' FROM feeds WHERE id = 98 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (23, 1006, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (23, 1012, NOW() - INTERVAL '5 days') ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (23, 1023, NOW() - INTERVAL '4 days') ON CONFLICT DO NOTHING;
INSERT INTO bookmarks (user_id, demo_id, created_at) VALUES (23, 1006, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO bookmarks (user_id, demo_id, created_at) VALUES (23, 1002, NOW() - INTERVAL '7 days') ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 24, id, NOW() - INTERVAL '5 days' FROM feeds WHERE id = 55 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 24, id, NOW() - INTERVAL '9 days' FROM feeds WHERE id = 94 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 24, id, NOW() - INTERVAL '4 days' FROM feeds WHERE id = 96 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 24, id, NOW() - INTERVAL '6 days' FROM feeds WHERE id = 156 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (24, 1008, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (24, 1002, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (24, 1001, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO bookmarks (user_id, demo_id, created_at) VALUES (24, 1008, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO bookmarks (user_id, demo_id, created_at) VALUES (24, 1009, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 25, id, NOW() - INTERVAL '5 days' FROM feeds WHERE id = 53 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 25, id, NOW() - INTERVAL '7 days' FROM feeds WHERE id = 130 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 25, id, NOW() - INTERVAL '9 days' FROM feeds WHERE id = 143 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO feed_bookmarks (user_id, feed_id, created_at) SELECT 25, id, NOW() - INTERVAL '6 days' FROM feeds WHERE id = 151 AND status = 'ACTIVE' ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (25, 1023, NOW() - INTERVAL '3 days') ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (25, 1013, NOW() - INTERVAL '1 days') ON CONFLICT DO NOTHING;
INSERT INTO post_bookmarks (user_id, post_id, created_at) VALUES (25, 1010, NOW() - INTERVAL '2 days') ON CONFLICT DO NOTHING;
INSERT INTO bookmarks (user_id, demo_id, created_at) VALUES (25, 1007, NOW() - INTERVAL '6 days') ON CONFLICT DO NOTHING;
INSERT INTO bookmarks (user_id, demo_id, created_at) VALUES (25, 1012, NOW() - INTERVAL '0 days') ON CONFLICT DO NOTHING;

-- ========== 블로그 구독 (팀 계정끼리, 구독 중인 연재 작가 확인용) ==========
INSERT INTO blog_subscriptions (subscriber_id, blog_owner_id, paid_price, status, started_at, expires_at)
SELECT 21, 22, 500, 'ACTIVE', NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM blog_subscriptions WHERE subscriber_id = 21 AND blog_owner_id = 22);
INSERT INTO blog_subscriptions (subscriber_id, blog_owner_id, paid_price, status, started_at, expires_at)
SELECT 21, 23, 500, 'ACTIVE', NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM blog_subscriptions WHERE subscriber_id = 21 AND blog_owner_id = 23);
INSERT INTO blog_subscriptions (subscriber_id, blog_owner_id, paid_price, status, started_at, expires_at)
SELECT 22, 21, 500, 'ACTIVE', NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM blog_subscriptions WHERE subscriber_id = 22 AND blog_owner_id = 21);
INSERT INTO blog_subscriptions (subscriber_id, blog_owner_id, paid_price, status, started_at, expires_at)
SELECT 22, 23, 500, 'ACTIVE', NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM blog_subscriptions WHERE subscriber_id = 22 AND blog_owner_id = 23);
INSERT INTO blog_subscriptions (subscriber_id, blog_owner_id, paid_price, status, started_at, expires_at)
SELECT 23, 21, 500, 'ACTIVE', NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM blog_subscriptions WHERE subscriber_id = 23 AND blog_owner_id = 21);
INSERT INTO blog_subscriptions (subscriber_id, blog_owner_id, paid_price, status, started_at, expires_at)
SELECT 23, 22, 500, 'ACTIVE', NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM blog_subscriptions WHERE subscriber_id = 23 AND blog_owner_id = 22);
INSERT INTO blog_subscriptions (subscriber_id, blog_owner_id, paid_price, status, started_at, expires_at)
SELECT 24, 21, 500, 'ACTIVE', NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM blog_subscriptions WHERE subscriber_id = 24 AND blog_owner_id = 21);
INSERT INTO blog_subscriptions (subscriber_id, blog_owner_id, paid_price, status, started_at, expires_at)
SELECT 24, 22, 500, 'ACTIVE', NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM blog_subscriptions WHERE subscriber_id = 24 AND blog_owner_id = 22);
INSERT INTO blog_subscriptions (subscriber_id, blog_owner_id, paid_price, status, started_at, expires_at)
SELECT 25, 21, 500, 'ACTIVE', NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM blog_subscriptions WHERE subscriber_id = 25 AND blog_owner_id = 21);
INSERT INTO blog_subscriptions (subscriber_id, blog_owner_id, paid_price, status, started_at, expires_at)
SELECT 25, 22, 500, 'ACTIVE', NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM blog_subscriptions WHERE subscriber_id = 25 AND blog_owner_id = 22);

-- ========== 미니홈피 대표 글 (비어 있을 때만) ==========
UPDATE mini_homes SET featured_post_id = 1001 WHERE user_id = 21 AND featured_post_id IS NULL;
UPDATE mini_homes SET featured_post_id = 1002 WHERE user_id = 22 AND featured_post_id IS NULL;
UPDATE mini_homes SET featured_post_id = 1003 WHERE user_id = 23 AND featured_post_id IS NULL;
UPDATE mini_homes SET featured_post_id = 1004 WHERE user_id = 24 AND featured_post_id IS NULL;
UPDATE mini_homes SET featured_post_id = 1005 WHERE user_id = 25 AND featured_post_id IS NULL;

-- ========== 시퀀스 맞추기 ==========
SELECT setval(pg_get_serial_sequence('posts', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM posts), 1));
SELECT setval(pg_get_serial_sequence('demos', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM demos), 1));
SELECT setval(pg_get_serial_sequence('blog_subscriptions', 'id'), GREATEST((SELECT COALESCE(MAX(id), 0) FROM blog_subscriptions), 1));

COMMIT;
