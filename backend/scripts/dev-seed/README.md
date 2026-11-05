# 로컬 통합 테스트 시드 (dev-seed)

로컬 Postgres에만 넣는 더미 데이터다. **CI/CD·배포 서버에서는 실행하지 않는다.**

소셜 로그인 없이 팀원 5명이 JWT 쿠키로 들어가, 피드 알고리즘·DM·미니홈피·블로그·데모 등을 같이 확인할 때 쓴다.

## 들어가는 데이터

| 구분 | 내용 |
| --- | --- |
| 팀원 계정 5 | `seed-team-01@dev.local` ~ `seed-team-05@dev.local` (로그인용) |
| 데이터 계정 45 | `seed-user-06@dev.local` ~ `seed-user-50@dev.local` |
| 미니홈피 | 회원 50명 전원 + 방명록 |
| 피드 | 회원당 2개(~100) + 태그·좋아요·북마크·리포스트·투표·댓글·대댓글 |
| 데모 | ~30개 + 좋아요·북마크·피드백·대댓글 |
| 블로그 | ~40개 + 좋아요·북마크·댓글·대댓글 |
| 팔로우 | 팀원 서로 + 팀원↔유저 + 유저끼리 일부 |
| DM | 시드팀01↔02, 시드팀01↔03 (안 읽음 케이스 포함) |

## 실행 (이게 전부)

루트 `.env`의 DB·`JWT_SECRET`을 읽는다. `npm install` 필요 없음.

```bash
cd backend/scripts/dev-seed
./up.sh          # 시드 + 팀원 5명 토큰 출력
```

브라우저 쿠키 `access_token`에 토큰 붙여넣고 새로고침.

프론트 `.env.local`:

```env
API_ORIGIN=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws
```

루트 `.env`에 `DEV_LOGIN_ENABLED=true` 후 백엔드 재시작.

로그인: `http://localhost:3000/login/dev` 에서 시드 계정 클릭.
(소셜 로그인 페이지에도 "로컬 시드 계정으로 로그인 (임시)" 링크가 있다. TEMP-DEV-LOGIN)

`MOCK_EXCEPT_DM`은 끄기. 백엔드·프론트는 평소처럼 `bootRun` / `npm run dev`.

## 지우기

```bash
./cleanup.sh
```

## 절대 하지 말 것

- 배포 DB / 스테이징 DB에서 실행
- CI 워크플로에 `npm run preview` / `seed.sql` 연결
- `src/main/resources/data.sql`로 옮기거나 Spring `sql.init`에 연결
- 운영 `JWT_SECRET`으로 토큰 발급
