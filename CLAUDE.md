# 찍먹 (Dip Source) — 개발자 SNS

Git 규칙(브랜치, 커밋, PR)은 AGENTS.md를 따른다. Codex와 같은 규칙을 공유하기 위해 내용은 AGENTS.md 한 곳에서만 관리한다.

@AGENTS.md

## 프로젝트 구조

```
backend/     Spring Boot 3 + JPA + PostgreSQL
frontend/    Next.js 16 App Router (src/app/)
docs/        기획서, ERD, 테이블정의서, 개발 문서
```

## 주요 문서

| 문서 | 역할 |
|------|------|
| [AGENTS.md](AGENTS.md) | Git 협업 규칙 (브랜치, 커밋, PR) |
| [docs/기획서.md](docs/기획서.md) | 기능, API 명세, WBS |
| [docs/테이블정의서.md](docs/테이블정의서.md) | 23개 테이블 컬럼·제약조건 |
| [docs/database-design.md](docs/database-design.md) | DB 설계 결정, 인덱스, 동시성, 구현 순서 |
| [docs/FRONTEND.md](docs/FRONTEND.md) | 프론트엔드 개발 규칙 (라우트, 폴더, 상태, 컴포넌트, 접근성) |
| [docs/frontend-청사진.md](docs/frontend-청사진.md) | 사용자 흐름, 개발 단계 체크리스트 |

## 기술 스택

- **Backend:** Java 21, Spring Boot 3, Spring Security, JPA, JWT, PostgreSQL 16
- **Frontend:** Next.js 16, React 19, TypeScript strict
- **Infra:** Docker Compose, Nginx
- **Auth:** 소셜 로그인 (GitHub, 카카오, 구글) + JWT (Access + Refresh)
- **실시간:** WebSocket + STOMP + SockJS (DM, 알림)

## 규칙 요약

- 기획서 API 명세에 없는 API를 가정하지 않는다
- 테이블정의서와 database-design.md가 DB의 기준이다
- 프론트엔드는 FRONTEND.md의 규칙을 따른다
- `.env`, 키, 토큰 등 비밀정보를 커밋하지 않는다
