# DM 확인 스크립트

로컬 백엔드의 DM 기능(WebSocket 실시간 전송 + 대화방 목록·대화 내역·읽음 API)이 동작하는지 명령 한 줄로 확인한다.
화면(프론트) 없이 백엔드만 확인하는 용도다.

## 하는 일

1. 테스트 회원 2명(`dm-check-a@test.local`, `dm-check-b@test.local`)을 DB에서 찾고, 없으면 만든다.
2. 백엔드와 같은 `JWT_SECRET`으로 두 회원의 로그인 토큰을 만든다(GitHub 로그인 불필요).
3. 두 회원이 WebSocket으로 연결해 메시지를 주고받는다.
4. 대화방 목록, 대화 내역, 안 읽은 수, 읽음 표시, 오류 처리, 비로그인 401을 확인한다.

## 실행

```bash
# 1) 백엔드 실행 (JWT_SECRET은 아래 3번과 같은 값)
cd backend
GITHUB_CLIENT_ID=test GITHUB_CLIENT_SECRET=test \
JWT_SECRET=local-dev-secret-at-least-32-characters-long \
FRONTEND_BASE_URL=http://localhost:3000 \
./gradlew bootRun

# 2) 처음 한 번만
cd backend/scripts/dm-check
npm install

# 3) 확인
JWT_SECRET=local-dev-secret-at-least-32-characters-long npm run check
```

모두 통과하면 `모두 통과했어요.`가 나온다. 실패한 항목은 ❌와 이유가 함께 나온다.

## 설정 (환경변수, 모두 선택)

| 이름 | 기본값 | 설명 |
| --- | --- | --- |
| `JWT_SECRET` | (필수) | 백엔드를 띄울 때 쓴 값 |
| `API_BASE` | `http://localhost:8080` | 백엔드 주소 |
| `ORIGIN` | `http://localhost:3000` | 백엔드 `FRONTEND_BASE_URL`과 같아야 한다 |
| `DATABASE_URL` | `postgres://dipsource:dipsource@localhost:5432/dipsource` | 테스트 회원을 넣을 DB |

테스트 회원과 테스트 메시지는 지우지 않고 남는다(다시 실행하면 같은 회원을 쓴다). 로컬 DB에서만 실행한다.

## 화면으로 확인 (더미 데이터)

`npm run preview`로 더미 회원 4명과 대화방 3개를 넣고 로그인 토큰을 받는다. 자세한 방법은 [docs/임시-목업-현황.md](../../../docs/임시-목업-현황.md)를 본다.
