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
| [docs/테이블정의서.md](docs/테이블정의서.md) | 46개 테이블 컬럼·제약조건 |
| [docs/database-design.md](docs/database-design.md) | DB 설계 결정, 인덱스, 동시성, 구현 순서 |
| [docs/FRONTEND.md](docs/FRONTEND.md) | 프론트엔드 개발 규칙 (라우트, 폴더, 상태, 컴포넌트, 접근성) |
| [docs/frontend-청사진.md](docs/frontend-청사진.md) | 사용자 흐름, 개발 단계 체크리스트 |
| [docs/임시-목업-현황.md](docs/임시-목업-현황.md) | 아직 없는 백엔드 API를 대신하는 목 데이터, DM 확인용 더미 데이터·스위치(`MOCK_EXCEPT_DM`) |
| [docs/작업-템플릿.md](docs/작업-템플릿.md) | 기능·API 작업 사이클 (구현 → 해석 → 보안·테스트 → 확인). Cursor 규칙이 작업 전 읽기를 강제한다 |

## 기술 스택

- **Backend:** Java 21, Spring Boot 3, Spring Security, JPA, JWT, PostgreSQL 16
- **Frontend:** Next.js 16, React 19, TypeScript strict
- **Infra:** Docker Compose, Nginx
- **Auth:** 소셜 로그인 (GitHub, 카카오, 구글) + JWT (Access + Refresh)
- **실시간:** WebSocket + STOMP + SockJS (DM, 알림)

## 작업 흐름

**기능·API 작업을 시작하기 전에** [`docs/작업-템플릿.md`](docs/작업-템플릿.md)를 읽고, **청사진**도 함께 본다.

1. **할 일 확인**: `docs/백엔드-청사진.md` 4장(API 전체)에서 ❌ 미구현인 항목을 확인한다
2. **한 단계 구현**: 엔티티 → Repository → Service → Controller+DTO (템플릿: 해석·보안·테스트·사용자 확인 후 다음 단계)
3. **상태 업데이트**: 구현이 끝나면 청사진의 해당 API 상태를 직접 수정한다
   - `❌ 미구현` → `✅ 완료` (구현 + 동작 확인)
   - `🔲 스텁` → `✅ 완료` (스텁을 실제 구현으로 교체한 경우)
   - 6장 구현 순서 테이블도 함께 갱신한다
4. **커밋에 포함**: 청사진 상태 변경을 코드 커밋에 함께 포함한다

### 청사진 상태 범례

| 표시 | 의미 |
|------|------|
| ✅ 완료 | 실제 구현 완료, 프론트 연동 가능 |
| 🔄 PR 리뷰 중 | 코드 작성 완료, 머지 대기 |
| 🔲 스텁 | 빈 응답만 반환 (StubController), 프론트 에러 방지용 |
| ❌ 미구현 | 아직 코드 없음 |

### 참조 문서

| 작업 대상 | 읽을 문서 |
|-----------|-----------|
| 백엔드 API 구현 | `docs/백엔드-청사진.md` → 4장 API 전체 (상태 확인), 5장 도메인과 테이블 |
| 프론트엔드 구현 | `docs/프론트엔드-청사진.md` |
| DB 스키마 | `docs/테이블정의서.md`, `docs/database-design.md` |
| 기능 요구사항 | `docs/기획서.md` |

## 핸드오프 (Cursor ↔ Claude)

- 상태 파일: `docs/_local/handoff.md` (gitignore, 로컬 전용)
- **이어받기**: 새 세션 시작 시 `docs/_local/handoff.md`를 읽고 중단된 작업을 이어간다
- **저장하기**: 사용자가 "핸드오프" 또는 "상태 저장"이라고 하면 `docs/_local/handoff.md`를 현재 상태로 갱신한다
- 형식: 지금(브랜치·기능·단계) → 한 일 → 안 한 일 → 열어볼 파일

## 규칙 요약

- 기획서 API 명세에 없는 API를 가정하지 않는다
- 테이블정의서와 database-design.md가 DB의 기준이다
- 프론트엔드는 FRONTEND.md의 규칙을 따른다
- `.env`, 키, 토큰 등 비밀정보를 커밋하지 않는다
