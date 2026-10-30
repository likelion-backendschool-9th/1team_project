# 찍먹 (Dip Source)

> 개발자의 소스를 맛보다

개발자들이 자신의 프로젝트를 데모로 공유하고, 미니홈피를 꾸미고, 서로의 코드를 체험할 수 있는 개발자 SNS 플랫폼

**GitHub:** https://github.com/likelion-backendschool-9th/1team_project

## 기술 스택

| 구분 | 스택 |
|------|------|
| Backend | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| Frontend | Next.js (App Router), TypeScript, React |
| Database | PostgreSQL |
| Infra | Docker, Docker Compose, Nginx |

## 프로젝트 구조

```
team_project/
├── backend/          # Spring Boot API 서버
├── frontend/         # Next.js 클라이언트
└── docs/             # 기획 문서
```

## 실행 방법

### Backend

```bash
cd backend
./gradlew bootRun
```

- 기본 포트: `8080`
- PostgreSQL 필요 (`localhost:5432/dipsource`)

### Frontend

```bash
cd frontend
npm install
npm run dev
```

- 기본 포트: `3000`

## 팀원

| 이름 | 역할 |
|------|------|
| 승조 | |
| 다윗 | |
| 진엽 | |
| 준하 | |
| 재인 | |
