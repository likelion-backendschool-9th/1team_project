# GitHub OAuth 사용 설명서

찍먹 GitHub 소셜 로그인용 **로컬 환경변수 적용** 안내다.  
OAuth App은 이미 등록되어 있으니, 팀원은 공유받은 Client ID / Secret을 `.env`에 넣기만 하면 된다.

관련 문서: [환경설정.md](./환경설정.md)

---

## 1. `.env` 만들기

저장소 루트에서:

```bash
cp .env.example .env
```

| 파일 | 역할 |
|------|------|
| `.env.example` | 필요한 키 이름 템플릿 (Git에 포함) |
| `.env` | 실제 비밀값 (Git에 올리지 않음) |

---

## 2. 채울 값

```env
POSTGRES_DB=dipsource
POSTGRES_USER=dipsource
POSTGRES_PASSWORD=dipsource

JWT_SECRET=
GITHUB_CLIENT_ID=
GITHUB_CLIENT_SECRET=
```

| 키 | 어디서 구하나 |
|----|----------------|
| `GITHUB_CLIENT_ID` | 팀에서 공유받은 값 |
| `GITHUB_CLIENT_SECRET` | 팀에서 공유받은 값 |
| `JWT_SECRET` | 본인이 생성 (아래) |

### JWT_SECRET 생성

GitHub 값이 아니다. 우리 서버 JWT 서명용이다. 로컬에서는 각자 만들어도 된다.

```bash
openssl rand -base64 48
```

나온 문자열을 `JWT_SECRET=` 뒤에 붙인다.

---

## 3. 주의

- Client ID / Secret / JWT_SECRET을 **커밋·PR·공개 채널에 올리지 않는다**
- IDE에서 백엔드만 실행하면 `.env`가 자동으로 안 먹을 수 있다 → Run Configuration 환경변수에 같은 키를 넣거나, 터미널에서 export 후 실행
- DB·서버 실행은 [환경설정.md](./환경설정.md)를 따른다

---

## 4. 로그인 흐름 (참고)

```text
/login → GitHub 인가 → 백엔드 콜백 → JWT 쿠키 발급 → /main 또는 /onboarding
```

Callback URL(앱에 이미 등록됨): `http://localhost:8080/login/oauth2/code/github`

---

## 5. 미니홈피용 GitHub API (선택)

잔디·레포·언어는 **공개 API**로 조회한다. 로그인 OAuth Client Secret과 다른 값이다.

| 키 | 설명 |
|----|------|
| `GITHUB_TOKEN` | (선택) Personal Access Token. 없으면 동작하지만 rate limit이 낮다 |

```env
GITHUB_TOKEN=
```
