-- 로컬 개발용 시드 데이터
-- 사용법: psql -h localhost -p {포트} -U dipsource -d dipsource -f seed-data.sql
-- 또는: backend/scripts/dev-seed/up.sh
-- 지우기: cleanup.sql

BEGIN;

-- ========== users (70명) ==========
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (1, 'user1@test.com', '코딩하는곰돌이', 'ACTIVE', 'USER', NULL, 'dev-bear-coder', '["spring-boot", "java", "docker", "postgresql"]'::jsonb, 3200, 1500, '2026-08-10 17:48:58.338876', '2026-10-24 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (2, 'user2@test.com', '리액트장인', 'ACTIVE', 'USER', NULL, 'react-craftsman', '["react", "typescript", "next-js"]'::jsonb, 1750, 2000, '2026-08-23 17:48:58.338876', '2026-10-29 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (3, 'user3@test.com', '백엔드탐험가', 'ACTIVE', 'USER', NULL, 'backend-explorer', '["spring-boot", "redis", "aws"]'::jsonb, 500, 0, '2026-09-04 17:48:58.338876', '2026-10-31 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (4, 'user4@test.com', '파이썬마법사', 'ACTIVE', 'USER', NULL, 'py-wizard', '["python", "docker", "kubernetes"]'::jsonb, 4800, 3000, '2026-09-09 17:48:58.338876', '2026-11-02 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (5, 'user5@test.com', '풀스택개발자K', 'ACTIVE', 'USER', NULL, 'fullstack-k', '["react", "spring-boot", "postgresql"]'::jsonb, 2100, 0, '2026-09-16 17:48:58.338876', '2026-10-27 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (6, 'user6@test.com', '도커달인', 'ACTIVE', 'USER', NULL, 'docker-master99', '["docker", "kubernetes", "aws", "go"]'::jsonb, 900, 1000, '2026-09-23 17:48:58.338876', '2026-11-01 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (7, 'user7@test.com', '타입스크립트러버', 'ACTIVE', 'USER', NULL, 'ts-lover-dev', '["typescript", "react", "next-js", "vue"]'::jsonb, 3700, 0, '2026-09-27 17:48:58.338876', '2026-10-30 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (8, 'user8@test.com', '자바의신', 'ACTIVE', 'USER', NULL, 'java-god-kr', '["java", "spring-boot", "redis"]'::jsonb, 5000, 2500, '2026-10-01 17:48:58.338876', '2026-10-28 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (9, 'user9@test.com', '플러터개발자', 'ACTIVE', 'USER', NULL, 'flutter-devKim', '["flutter", "dart", "firebase"]'::jsonb, 150, 0, '2026-10-05 17:48:58.338876', '2026-11-02 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (10, 'user10@test.com', 'AWS클라우드맨', 'ACTIVE', 'USER', NULL, 'aws-cloud-man', '["aws", "docker", "kubernetes", "python"]'::jsonb, 2900, 1000, '2026-10-08 17:48:58.338876', '2026-10-31 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (11, 'user11@test.com', '뷰JS전도사', 'ACTIVE', 'USER', NULL, 'vue-evangelist', '["vue", "typescript", "python"]'::jsonb, 680, 0, '2026-10-11 17:48:58.338876', '2026-11-01 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (12, 'user12@test.com', 'DB최적화장인', 'ACTIVE', 'USER', NULL, 'db-optimizer-kr', '["postgresql", "redis", "java", "spring-boot"]'::jsonb, 4100, 3000, '2026-10-13 17:48:58.338876', '2026-10-26 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (13, 'user13@test.com', '고랭고수', 'ACTIVE', 'USER', NULL, 'golang-pro', '["go", "docker", "kubernetes"]'::jsonb, 1300, 0, '2026-10-15 17:48:58.338876', '2026-11-02 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (14, 'user14@test.com', '러스트입문자', 'ACTIVE', 'USER', NULL, 'rust-newbie-kr', '["rust", "go", "docker"]'::jsonb, 80, 0, '2026-10-17 17:48:58.338876', '2026-11-01 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (15, 'user15@test.com', '넥스트JS빌더', 'ACTIVE', 'USER', NULL, 'nextjs-builder', '["next-js", "react", "typescript", "aws"]'::jsonb, 2250, 1500, '2026-10-19 17:48:58.338876', '2026-11-02 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (16, 'user16@test.com', '쿠버네티스왕', 'ACTIVE', 'USER', NULL, 'k8s-king-dev', '["kubernetes", "docker", "go", "aws"]'::jsonb, 3600, 0, '2026-10-21 17:48:58.338876', '2026-10-31 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (17, 'user17@test.com', '스프링부트러버', 'ACTIVE', 'USER', NULL, 'spring-lover17', '["spring-boot", "java", "postgresql"]'::jsonb, 420, 2000, '2026-10-23 17:48:58.338876', '2026-11-02 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (18, 'user18@test.com', '프론트엔드妖精', 'ACTIVE', 'USER', NULL, 'frontend-fairy', '["react", "vue", "typescript", "next-js"]'::jsonb, 1950, 0, '2026-10-25 17:48:58.338876', '2026-11-01 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (19, 'user19@test.com', '데브옵스닌자', 'ACTIVE', 'USER', NULL, 'devops-ninja-kr', '["docker", "kubernetes", "aws", "python"]'::jsonb, 4400, 1000, '2026-10-28 17:48:58.338876', '2026-11-02 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (20, 'user20@test.com', '오픈소스기여자', 'ACTIVE', 'USER', NULL, 'oss-contributor', '["rust", "go", "python", "docker"]'::jsonb, 750, 0, '2026-10-31 17:48:58.338876', '2026-11-02 17:48:58.338876') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (21, 'seed-team-01@dev.local', '시드팀01', 'ACTIVE', 'USER', '팀원 로그인용 계정 1. 로컬 테스트 전용.', 'seed-team-01', '["Java", "Spring Boot", "JPA"]'::jsonb, 100, 500, '2026-09-06 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (22, 'seed-team-02@dev.local', '시드팀02', 'ACTIVE', 'USER', '팀원 로그인용 계정 2. 로컬 테스트 전용.', 'seed-team-02', '["React", "TypeScript", "Next.js"]'::jsonb, 200, 500, '2026-09-07 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (23, 'seed-team-03@dev.local', '시드팀03', 'ACTIVE', 'USER', '팀원 로그인용 계정 3. 로컬 테스트 전용.', 'seed-team-03', '["Python", "Docker", "AWS"]'::jsonb, 300, 0, '2026-09-08 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (24, 'seed-team-04@dev.local', '시드팀04', 'ACTIVE', 'USER', '팀원 로그인용 계정 4. 로컬 테스트 전용.', 'seed-team-04', '["Node.js", "TypeScript", "Redis"]'::jsonb, 400, 0, '2026-09-09 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (25, 'seed-team-05@dev.local', '시드팀05', 'ACTIVE', 'USER', '팀원 로그인용 계정 5. 로컬 테스트 전용.', 'seed-team-05', '["Unity", "C#"]'::jsonb, 500, 0, '2026-09-10 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (26, 'seed-user-06@dev.local', '시드유저06', 'ACTIVE', 'USER', '데이터용 계정 6. 피드·블로그·데모 작성자.', 'seed-user-06', '["Spring Boot", "PostgreSQL", "Redis"]'::jsonb, 300, 0, '2026-08-12 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (27, 'seed-user-07@dev.local', '시드유저07', 'ACTIVE', 'USER', '데이터용 계정 7. 피드·블로그·데모 작성자.', 'seed-user-07', '["React", "WebSocket", "Node.js"]'::jsonb, 0, 0, '2026-08-13 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (28, 'seed-user-08@dev.local', '시드유저08', 'ACTIVE', 'USER', '데이터용 계정 8. 피드·블로그·데모 작성자.', 'seed-user-08', '["Kubernetes", "Docker", "AWS"]'::jsonb, 50, 0, '2026-08-14 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (29, 'seed-user-09@dev.local', '시드유저09', 'ACTIVE', 'USER', '데이터용 계정 9. 피드·블로그·데모 작성자.', 'seed-user-09', '["Java", "Spring Boot", "JPA"]'::jsonb, 100, 0, '2026-08-15 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (30, 'seed-user-10@dev.local', '시드유저10', 'ACTIVE', 'USER', '데이터용 계정 10. 피드·블로그·데모 작성자.', 'seed-user-10', '["React", "TypeScript", "Next.js"]'::jsonb, 150, 300, '2026-08-16 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (31, 'seed-user-11@dev.local', '시드유저11', 'ACTIVE', 'USER', '데이터용 계정 11. 피드·블로그·데모 작성자.', 'seed-user-11', '["Python", "Docker", "AWS"]'::jsonb, 200, 0, '2026-08-17 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (32, 'seed-user-12@dev.local', '시드유저12', 'ACTIVE', 'USER', '데이터용 계정 12. 피드·블로그·데모 작성자.', 'seed-user-12', '["Node.js", "TypeScript", "Redis"]'::jsonb, 250, 0, '2026-08-18 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (33, 'seed-user-13@dev.local', '시드유저13', 'ACTIVE', 'USER', '데이터용 계정 13. 피드·블로그·데모 작성자.', 'seed-user-13', '["Unity", "C#"]'::jsonb, 300, 0, '2026-08-19 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (34, 'seed-user-14@dev.local', '시드유저14', 'ACTIVE', 'USER', '데이터용 계정 14. 피드·블로그·데모 작성자.', 'seed-user-14', '["Spring Boot", "PostgreSQL", "Redis"]'::jsonb, 0, 0, '2026-08-20 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (35, 'seed-user-15@dev.local', '시드유저15', 'ACTIVE', 'USER', '데이터용 계정 15. 피드·블로그·데모 작성자.', 'seed-user-15', '["React", "WebSocket", "Node.js"]'::jsonb, 50, 0, '2026-08-21 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (36, 'seed-user-16@dev.local', '시드유저16', 'ACTIVE', 'USER', '데이터용 계정 16. 피드·블로그·데모 작성자.', 'seed-user-16', '["Kubernetes", "Docker", "AWS"]'::jsonb, 100, 0, '2026-08-22 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (37, 'seed-user-17@dev.local', '시드유저17', 'ACTIVE', 'USER', '데이터용 계정 17. 피드·블로그·데모 작성자.', 'seed-user-17', '["Java", "Spring Boot", "JPA"]'::jsonb, 150, 0, '2026-08-23 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (38, 'seed-user-18@dev.local', '시드유저18', 'ACTIVE', 'USER', '데이터용 계정 18. 피드·블로그·데모 작성자.', 'seed-user-18', '["React", "TypeScript", "Next.js"]'::jsonb, 200, 0, '2026-08-24 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (39, 'seed-user-19@dev.local', '시드유저19', 'ACTIVE', 'USER', '데이터용 계정 19. 피드·블로그·데모 작성자.', 'seed-user-19', '["Python", "Docker", "AWS"]'::jsonb, 250, 0, '2026-08-25 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (40, 'seed-user-20@dev.local', '시드유저20', 'ACTIVE', 'USER', '데이터용 계정 20. 피드·블로그·데모 작성자.', 'seed-user-20', '["Node.js", "TypeScript", "Redis"]'::jsonb, 300, 300, '2026-08-26 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (41, 'seed-user-21@dev.local', '시드유저21', 'ACTIVE', 'USER', '데이터용 계정 21. 피드·블로그·데모 작성자.', 'seed-user-21', '["Unity", "C#"]'::jsonb, 0, 0, '2026-08-27 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (42, 'seed-user-22@dev.local', '시드유저22', 'ACTIVE', 'USER', '데이터용 계정 22. 피드·블로그·데모 작성자.', 'seed-user-22', '["Spring Boot", "PostgreSQL", "Redis"]'::jsonb, 50, 0, '2026-08-28 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (43, 'seed-user-23@dev.local', '시드유저23', 'ACTIVE', 'USER', '데이터용 계정 23. 피드·블로그·데모 작성자.', 'seed-user-23', '["React", "WebSocket", "Node.js"]'::jsonb, 100, 0, '2026-08-29 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (44, 'seed-user-24@dev.local', '시드유저24', 'ACTIVE', 'USER', '데이터용 계정 24. 피드·블로그·데모 작성자.', 'seed-user-24', '["Kubernetes", "Docker", "AWS"]'::jsonb, 150, 0, '2026-08-30 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (45, 'seed-user-25@dev.local', '시드유저25', 'ACTIVE', 'USER', '데이터용 계정 25. 피드·블로그·데모 작성자.', 'seed-user-25', '["Java", "Spring Boot", "JPA"]'::jsonb, 200, 0, '2026-08-31 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (46, 'seed-user-26@dev.local', '시드유저26', 'ACTIVE', 'USER', '데이터용 계정 26. 피드·블로그·데모 작성자.', 'seed-user-26', '["React", "TypeScript", "Next.js"]'::jsonb, 250, 0, '2026-09-01 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (47, 'seed-user-27@dev.local', '시드유저27', 'ACTIVE', 'USER', '데이터용 계정 27. 피드·블로그·데모 작성자.', 'seed-user-27', '["Python", "Docker", "AWS"]'::jsonb, 300, 0, '2026-09-02 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (48, 'seed-user-28@dev.local', '시드유저28', 'ACTIVE', 'USER', '데이터용 계정 28. 피드·블로그·데모 작성자.', 'seed-user-28', '["Node.js", "TypeScript", "Redis"]'::jsonb, 0, 0, '2026-09-03 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (49, 'seed-user-29@dev.local', '시드유저29', 'ACTIVE', 'USER', '데이터용 계정 29. 피드·블로그·데모 작성자.', 'seed-user-29', '["Unity", "C#"]'::jsonb, 50, 0, '2026-09-04 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (50, 'seed-user-30@dev.local', '시드유저30', 'ACTIVE', 'USER', '데이터용 계정 30. 피드·블로그·데모 작성자.', 'seed-user-30', '["Spring Boot", "PostgreSQL", "Redis"]'::jsonb, 100, 300, '2026-09-05 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (51, 'seed-user-31@dev.local', '시드유저31', 'ACTIVE', 'USER', '데이터용 계정 31. 피드·블로그·데모 작성자.', 'seed-user-31', '["React", "WebSocket", "Node.js"]'::jsonb, 150, 0, '2026-09-06 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (52, 'seed-user-32@dev.local', '시드유저32', 'ACTIVE', 'USER', '데이터용 계정 32. 피드·블로그·데모 작성자.', 'seed-user-32', '["Kubernetes", "Docker", "AWS"]'::jsonb, 200, 0, '2026-09-07 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (53, 'seed-user-33@dev.local', '시드유저33', 'ACTIVE', 'USER', '데이터용 계정 33. 피드·블로그·데모 작성자.', 'seed-user-33', '["Java", "Spring Boot", "JPA"]'::jsonb, 250, 0, '2026-09-08 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (54, 'seed-user-34@dev.local', '시드유저34', 'ACTIVE', 'USER', '데이터용 계정 34. 피드·블로그·데모 작성자.', 'seed-user-34', '["React", "TypeScript", "Next.js"]'::jsonb, 300, 0, '2026-09-09 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (55, 'seed-user-35@dev.local', '시드유저35', 'ACTIVE', 'USER', '데이터용 계정 35. 피드·블로그·데모 작성자.', 'seed-user-35', '["Python", "Docker", "AWS"]'::jsonb, 0, 0, '2026-09-10 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (56, 'seed-user-36@dev.local', '시드유저36', 'ACTIVE', 'USER', '데이터용 계정 36. 피드·블로그·데모 작성자.', 'seed-user-36', '["Node.js", "TypeScript", "Redis"]'::jsonb, 50, 0, '2026-09-11 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (57, 'seed-user-37@dev.local', '시드유저37', 'ACTIVE', 'USER', '데이터용 계정 37. 피드·블로그·데모 작성자.', 'seed-user-37', '["Unity", "C#"]'::jsonb, 100, 0, '2026-09-12 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (58, 'seed-user-38@dev.local', '시드유저38', 'ACTIVE', 'USER', '데이터용 계정 38. 피드·블로그·데모 작성자.', 'seed-user-38', '["Spring Boot", "PostgreSQL", "Redis"]'::jsonb, 150, 0, '2026-09-13 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (59, 'seed-user-39@dev.local', '시드유저39', 'ACTIVE', 'USER', '데이터용 계정 39. 피드·블로그·데모 작성자.', 'seed-user-39', '["React", "WebSocket", "Node.js"]'::jsonb, 200, 0, '2026-09-14 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (60, 'seed-user-40@dev.local', '시드유저40', 'ACTIVE', 'USER', '데이터용 계정 40. 피드·블로그·데모 작성자.', 'seed-user-40', '["Kubernetes", "Docker", "AWS"]'::jsonb, 250, 300, '2026-09-15 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (61, 'seed-user-41@dev.local', '시드유저41', 'ACTIVE', 'USER', '데이터용 계정 41. 피드·블로그·데모 작성자.', 'seed-user-41', '["Java", "Spring Boot", "JPA"]'::jsonb, 300, 0, '2026-09-16 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (62, 'seed-user-42@dev.local', '시드유저42', 'ACTIVE', 'USER', '데이터용 계정 42. 피드·블로그·데모 작성자.', 'seed-user-42', '["React", "TypeScript", "Next.js"]'::jsonb, 0, 0, '2026-09-17 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (63, 'seed-user-43@dev.local', '시드유저43', 'ACTIVE', 'USER', '데이터용 계정 43. 피드·블로그·데모 작성자.', 'seed-user-43', '["Python", "Docker", "AWS"]'::jsonb, 50, 0, '2026-09-18 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (64, 'seed-user-44@dev.local', '시드유저44', 'ACTIVE', 'USER', '데이터용 계정 44. 피드·블로그·데모 작성자.', 'seed-user-44', '["Node.js", "TypeScript", "Redis"]'::jsonb, 100, 0, '2026-09-19 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (65, 'seed-user-45@dev.local', '시드유저45', 'ACTIVE', 'USER', '데이터용 계정 45. 피드·블로그·데모 작성자.', 'seed-user-45', '["Unity", "C#"]'::jsonb, 150, 0, '2026-09-20 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (66, 'seed-user-46@dev.local', '시드유저46', 'ACTIVE', 'USER', '데이터용 계정 46. 피드·블로그·데모 작성자.', 'seed-user-46', '["Spring Boot", "PostgreSQL", "Redis"]'::jsonb, 200, 0, '2026-09-21 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (67, 'seed-user-47@dev.local', '시드유저47', 'ACTIVE', 'USER', '데이터용 계정 47. 피드·블로그·데모 작성자.', 'seed-user-47', '["React", "WebSocket", "Node.js"]'::jsonb, 250, 0, '2026-09-22 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (68, 'seed-user-48@dev.local', '시드유저48', 'ACTIVE', 'USER', '데이터용 계정 48. 피드·블로그·데모 작성자.', 'seed-user-48', '["Kubernetes", "Docker", "AWS"]'::jsonb, 300, 0, '2026-09-23 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (69, 'seed-user-49@dev.local', '시드유저49', 'ACTIVE', 'USER', '데이터용 계정 49. 피드·블로그·데모 작성자.', 'seed-user-49', '["Java", "Spring Boot", "JPA"]'::jsonb, 0, 0, '2026-09-24 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO users (id, email, nickname, status, role, bio, github_username, tech_stacks, chip_balance, blog_subscription_price, created_at, updated_at) VALUES (70, 'seed-user-50@dev.local', '시드유저50', 'ACTIVE', 'USER', '데이터용 계정 50. 피드·블로그·데모 작성자.', 'seed-user-50', '["React", "TypeScript", "Next.js"]'::jsonb, 50, 300, '2026-09-25 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;

-- 시퀀스 리셋
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 0) FROM users));

-- ========== tags (30개) ==========
INSERT INTO tags (id, name) VALUES (1, 'spring-boot') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (2, 'react') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (3, 'next-js') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (4, 'typescript') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (5, 'python') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (6, 'java') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (7, 'docker') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (8, 'kubernetes') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (9, 'aws') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (10, 'postgresql') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (11, 'redis') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (12, 'flutter') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (13, 'vue') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (14, 'go') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (15, 'rust') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (16, 'JPA') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (17, 'Next.js') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (18, 'Docker') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (19, 'Node.js') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (20, 'AWS') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (21, 'Kubernetes') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (22, 'Python') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (23, 'React') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (24, 'PostgreSQL') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (25, 'Unity') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (26, 'WebSocket') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (27, 'Redis') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (28, 'Spring Boot') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (29, 'TypeScript') ON CONFLICT (id) DO NOTHING;
INSERT INTO tags (id, name) VALUES (30, 'Java') ON CONFLICT (id) DO NOTHING;

SELECT setval('tags_id_seq', (SELECT COALESCE(MAX(id), 0) FROM tags));

-- ========== feeds (200개) ==========
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (1, 1, '오늘 WebSocket 기반 채팅을 드디어 완성했다. 역시 실시간 기능은 재미있다.', 'ACTIVE', 34, 5, 0, NULL, '2026-10-05 17:48:58.349364', '2026-10-05 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (2, 2, 'React 18의 Concurrent 기능을 드디어 실제 프로젝트에 적용해봤다. 렌더링 성능이 눈에 띄게 개선됐다.', 'ACTIVE', 22, 3, 0, NULL, '2026-10-05 17:48:58.349364', '2026-10-05 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (3, 3, 'Spring Boot 3.x로 마이그레이션 완료. 가상 스레드 도입이 특히 인상적이었다.', 'ACTIVE', 18, 2, 0, NULL, '2026-10-06 17:48:58.349364', '2026-10-06 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (4, 4, '파이썬 asyncio로 크롤러 성능을 10배 올렸다. 비동기 프로그래밍의 힘을 다시 한번 느꼈다.', 'ACTIVE', 41, 7, 0, NULL, '2026-10-06 17:48:58.349364', '2026-10-06 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (5, 5, '포트폴리오 생성기 첫 버전을 배포했다. 벌써 100명이 사용해줬다. 뿌듯하다.', 'ACTIVE', 56, 9, 0, NULL, '2026-10-07 17:48:58.349364', '2026-10-07 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (6, 6, 'Docker Compose로 로컬 개발환경 구성 완료. 팀원 모두 동일한 환경에서 개발할 수 있게 됐다.', 'ACTIVE', 27, 4, 0, NULL, '2026-10-07 17:48:58.349364', '2026-10-07 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (7, 7, 'TypeScript 5.0의 const type parameter를 적용해봤다. 타입 추론이 훨씬 정확해졌다.', 'ACTIVE', 19, 3, 0, NULL, '2026-10-08 17:48:58.349364', '2026-10-08 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (8, 8, 'JVM 튜닝으로 GC pause를 50ms 이하로 줄이는 데 성공했다. 삽질만 3일...', 'ACTIVE', 38, 6, 0, NULL, '2026-10-08 17:48:58.349364', '2026-10-08 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (9, 9, 'Flutter로 iOS/Android 동시 배포 성공. 하나의 코드베이스로 두 플랫폼을 커버하는 게 아직도 신기하다.', 'ACTIVE', 15, 2, 0, NULL, '2026-10-09 17:48:58.349364', '2026-10-09 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (10, 10, 'AWS Cost Explorer API로 월별 비용 추이 분석 도구를 만들었다. 의외로 쓰는 곳이 많다.', 'ACTIVE', 44, 8, 0, NULL, '2026-10-09 17:48:58.349364', '2026-10-09 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (11, 11, 'Vue 3 Composition API로 리팩토링 중. 로직 재사용성이 확실히 좋아졌다.', 'ACTIVE', 12, 1, 0, NULL, '2026-10-10 17:48:58.349364', '2026-10-10 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (12, 12, 'PostgreSQL 파티셔닝으로 1억 건 테이블 조회 속도를 3초에서 0.2초로 단축시켰다.', 'ACTIVE', 62, 10, 0, NULL, '2026-10-10 17:48:58.349364', '2026-10-10 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (13, 13, 'Go의 goroutine과 channel로 동시성 처리를 구현했다. 자바보다 훨씬 직관적이다.', 'ACTIVE', 29, 5, 0, NULL, '2026-10-11 17:48:58.349364', '2026-10-11 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (14, 14, 'Rust 소유권 개념을 드디어 이해한 것 같다. borrow checker와 싸운 지 일주일...', 'ACTIVE', 8, 1, 0, NULL, '2026-10-11 17:48:58.349364', '2026-10-11 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (15, 15, 'Next.js App Router로 블로그를 만들었다. SSR과 SSG를 혼합해서 쓰니 SEO가 확실히 좋다.', 'ACTIVE', 48, 7, 0, NULL, '2026-10-12 17:48:58.349364', '2026-10-12 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (16, 16, 'Kubernetes HPA 설정으로 트래픽 급증 상황에서도 안정적으로 서비스를 유지했다.', 'ACTIVE', 35, 5, 0, NULL, '2026-10-12 17:48:58.349364', '2026-10-12 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (17, 17, 'Spring Security 6에서 JWT 필터 체인 구성이 많이 바뀌었다. 공식 문서를 꼼꼼히 읽어야 한다.', 'ACTIVE', 21, 4, 0, NULL, '2026-10-13 17:48:58.349364', '2026-10-13 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (18, 18, 'Framer Motion으로 페이지 전환 애니메이션을 넣었다. 사용자 경험이 확 달라진다.', 'ACTIVE', 40, 6, 0, NULL, '2026-10-13 17:48:58.349364', '2026-10-13 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (19, 19, 'GitHub Actions로 CI/CD 파이프라인 구성 완료. 코드 푸시부터 배포까지 5분이면 된다.', 'ACTIVE', 33, 5, 0, NULL, '2026-10-14 17:48:58.349364', '2026-10-14 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (20, 20, 'Rust로 CLI 도구를 만들었다. 바이너리 크기가 작고 속도가 빠르다.', 'ACTIVE', 18, 3, 0, NULL, '2026-10-14 17:48:58.349364', '2026-10-14 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (21, 1, 'Spring Data JPA N+1 문제를 fetch join으로 해결했다. 쿼리 수가 100개에서 1개로 줄었다.', 'ACTIVE', 47, 8, 0, NULL, '2026-10-15 17:48:58.349364', '2026-10-15 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (22, 2, 'React Query로 서버 상태 관리를 전환했다. Redux 대비 코드량이 절반으로 줄었다.', 'ACTIVE', 31, 4, 0, NULL, '2026-10-15 17:48:58.349364', '2026-10-15 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (23, 3, 'Redis Pub/Sub으로 서버 간 이벤트 브로드캐스팅을 구현했다.', 'ACTIVE', 24, 3, 0, NULL, '2026-10-16 17:48:58.349364', '2026-10-16 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (24, 4, 'pandas와 polars를 비교해봤다. 대용량 데이터에서 polars가 10배는 빠르다.', 'ACTIVE', 37, 6, 0, NULL, '2026-10-16 17:48:58.349364', '2026-10-16 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (25, 5, '이력서 PDF 생성기에 커스텀 템플릿 기능을 추가했다. 사용자 피드백이 엄청 좋다.', 'ACTIVE', 52, 9, 0, NULL, '2026-10-17 17:48:58.349364', '2026-10-17 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (26, 6, 'Prometheus + Grafana 조합으로 서버 모니터링 대시보드를 구축했다.', 'ACTIVE', 28, 4, 0, NULL, '2026-10-17 17:48:58.349364', '2026-10-17 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (27, 7, 'zod로 런타임 타입 검증을 추가하니 버그가 확 줄었다. 강력 추천한다.', 'ACTIVE', 22, 3, 0, NULL, '2026-10-18 17:48:58.349364', '2026-10-18 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (28, 8, 'Spring Batch 5.0으로 마이그레이션했다. JobRepository 설정 방식이 많이 바뀌었다.', 'ACTIVE', 19, 2, 0, NULL, '2026-10-18 17:48:58.349364', '2026-10-18 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (29, 9, 'Flutter Riverpod 2.0으로 상태관리를 전환했다. Provider 대비 코드가 훨씬 깔끔하다.', 'ACTIVE', 14, 2, 0, NULL, '2026-10-19 17:48:58.349364', '2026-10-19 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (30, 10, 'AWS Lambda cold start를 프로비저닝된 동시성으로 해결했다. 응답 시간 90% 개선.', 'ACTIVE', 43, 7, 0, NULL, '2026-10-19 17:48:58.349364', '2026-10-19 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (31, 11, 'Pinia로 Vue 상태관리 전환 완료. Vuex보다 TypeScript 지원이 훨씬 좋다.', 'ACTIVE', 16, 2, 0, NULL, '2026-10-20 17:48:58.349364', '2026-10-20 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (32, 12, 'PostgreSQL 인덱스 전략을 정리해봤다. B-tree, GIN, GiST의 차이를 이제야 제대로 이해했다.', 'ACTIVE', 55, 9, 0, NULL, '2026-10-20 17:48:58.349364', '2026-10-20 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (33, 13, 'Go 1.21의 새로운 내장 함수들을 써봤다. slices.Sort가 특히 편하다.', 'ACTIVE', 23, 3, 0, NULL, '2026-10-21 17:48:58.349364', '2026-10-21 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (34, 14, 'Rust로 처음 HTTP 서버를 만들어봤다. axum 프레임워크가 생각보다 쓰기 편하다.', 'ACTIVE', 11, 2, 0, NULL, '2026-10-21 17:48:58.349364', '2026-10-21 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (35, 15, 'Next.js Server Actions를 처음 써봤다. API 라우트 없이 서버 로직을 실행할 수 있다니 신기하다.', 'ACTIVE', 39, 5, 0, NULL, '2026-10-22 17:48:58.349364', '2026-10-22 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (36, 16, 'ArgoCD로 GitOps 배포 파이프라인을 구축했다. 선언적 배포 방식이 너무 좋다.', 'ACTIVE', 30, 4, 0, NULL, '2026-10-22 17:48:58.349364', '2026-10-22 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (37, 17, 'Hibernate 6의 새로운 쿼리 DSL이 꽤 쓸만하다. JPQL보다 타입 안전성이 높다.', 'ACTIVE', 17, 2, 0, NULL, '2026-10-23 17:48:58.349364', '2026-10-23 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (38, 18, 'shadcn/ui를 처음 써봤다. 복사해서 커스터마이징하는 방식이 처음엔 어색했는데 익숙해지니 편하다.', 'ACTIVE', 36, 5, 0, NULL, '2026-10-23 17:48:58.349364', '2026-10-23 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (39, 19, 'Terraform으로 AWS 인프라를 코드로 관리하기 시작했다. 인프라 변경 이력이 Git으로 관리된다.', 'ACTIVE', 28, 4, 0, NULL, '2026-10-24 17:48:58.349364', '2026-10-24 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (40, 20, 'Rust의 trait system을 이용해 플러그인 아키텍처를 구현했다.', 'ACTIVE', 14, 2, 0, NULL, '2026-10-24 17:48:58.349364', '2026-10-24 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (41, 1, 'Spring AOT 컴파일로 GraalVM 네이티브 이미지를 만들어봤다. 시작 시간이 10ms 이하다.', 'ACTIVE', 44, 7, 0, NULL, '2026-10-25 17:48:58.349364', '2026-10-25 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (42, 2, 'Storybook 7로 컴포넌트 문서화를 했다. 디자이너와 협업할 때 엄청 도움이 된다.', 'ACTIVE', 25, 3, 0, NULL, '2026-10-25 17:48:58.349364', '2026-10-25 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (43, 3, 'Apache Kafka로 이벤트 드리븐 아키텍처를 구현했다. 서비스 간 결합도가 많이 낮아졌다.', 'ACTIVE', 33, 5, 0, NULL, '2026-10-26 17:48:58.349364', '2026-10-26 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (44, 4, 'FastAPI로 ML 모델 서빙 API를 만들었다. Django보다 훨씬 빠르고 문서화도 자동으로 된다.', 'ACTIVE', 40, 6, 0, NULL, '2026-10-26 17:48:58.349364', '2026-10-26 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (45, 5, 'Turborepo로 모노레포를 구성했다. 프론트/백 공유 타입을 하나의 레포에서 관리한다.', 'ACTIVE', 29, 4, 0, NULL, '2026-10-27 17:48:58.349364', '2026-10-27 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (46, 6, 'Kubernetes Ingress 설정으로 여러 서비스를 하나의 도메인으로 라우팅했다.', 'ACTIVE', 21, 3, 0, NULL, '2026-10-27 17:48:58.349364', '2026-10-27 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (47, 7, '타입스크립트 decorators를 활용한 DI 컨테이너를 직접 만들어봤다. NestJS 내부가 이런 식이구나 싶었다.', 'ACTIVE', 18, 2, 0, NULL, '2026-10-28 17:48:58.349364', '2026-10-28 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (48, 8, 'Virtual thread로 기존 동기 코드를 거의 수정 없이 높은 동시성을 달성했다.', 'ACTIVE', 35, 5, 0, NULL, '2026-10-28 17:48:58.349364', '2026-10-28 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (49, 9, '앱 스토어 첫 출시 성공! Flutter로 만든 앱이 드디어 세상에 나왔다.', 'ACTIVE', 50, 9, 0, NULL, '2026-10-29 17:48:58.349364', '2026-10-29 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (50, 10, 'Lambda@Edge로 CDN 레벨에서 A/B 테스트를 구현했다. 응답 지연 없이 실험을 돌릴 수 있다.', 'ACTIVE', 32, 5, 0, NULL, '2026-10-29 17:48:58.349364', '2026-10-29 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (51, 11, 'Nuxt 3로 SSR 앱을 만들어봤다. Vue 개발자라면 정말 익숙한 느낌이다.', 'ACTIVE', 15, 2, 0, NULL, '2026-10-30 17:48:58.349364', '2026-10-30 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (52, 12, 'TimescaleDB로 시계열 데이터 저장소를 구축했다. PostgreSQL 기반이라 익숙하게 쓸 수 있다.', 'ACTIVE', 42, 6, 0, NULL, '2026-10-30 17:48:58.349364', '2026-10-30 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (53, 13, 'Go embed를 사용해서 정적 파일을 바이너리에 포함했다. 배포 파일이 하나만 된다.', 'ACTIVE', 20, 3, 0, NULL, '2026-10-31 17:48:58.349364', '2026-10-31 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (54, 14, 'unsafe Rust 코드를 처음 써봤다. 왜 unsafe인지 이제 실감한다.', 'ACTIVE', 9, 1, 0, NULL, '2026-10-31 17:48:58.349364', '2026-10-31 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (55, 15, 'SEO 분석 도구를 만들면서 Core Web Vitals를 깊게 공부했다.', 'ACTIVE', 37, 5, 0, NULL, '2026-11-01 17:48:58.349364', '2026-11-01 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (56, 16, 'Istio service mesh를 도입했다. 서비스 간 트래픽 관리와 관찰이 훨씬 쉬워졌다.', 'ACTIVE', 26, 4, 0, NULL, '2026-11-01 17:48:58.349364', '2026-11-01 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (57, 17, 'QueryDSL 없이 Spring Data JPA Specification으로 동적 쿼리를 구현했다.', 'ACTIVE', 22, 3, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (58, 18, 'CSS-in-JS에서 CSS Modules로 전환했다. 번들 크기가 줄고 렌더링도 빨라졌다.', 'ACTIVE', 31, 4, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (59, 19, 'Ansible로 서버 초기 설정을 자동화했다. 새 서버 세팅이 10분이면 된다.', 'ACTIVE', 24, 3, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (60, 20, 'cargo-flamegraph로 Rust 코드 성능 병목을 찾아냈다.', 'ACTIVE', 13, 2, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (61, 1, '코드 리뷰를 할 때 코드보다 설계 의도를 먼저 이해하는 게 중요하다는 걸 깨달았다.', 'ACTIVE', 28, 4, 0, NULL, '2026-10-06 17:48:58.349364', '2026-10-06 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (62, 2, 'useMemo와 useCallback을 남발하면 오히려 성능이 나빠질 수 있다. 프로파일링 먼저.', 'ACTIVE', 19, 3, 0, NULL, '2026-10-07 17:48:58.349364', '2026-10-07 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (63, 3, '로컬 캐시와 글로벌 캐시 전략의 트레이드오프를 정리해봤다.', 'ACTIVE', 16, 2, 0, NULL, '2026-10-08 17:48:58.349364', '2026-10-08 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (64, 4, 'Python GIL이 결국 제거된다. asyncio만 쓰던 나한테는 별로 상관없지만 반가운 소식.', 'ACTIVE', 33, 5, 0, NULL, '2026-10-09 17:48:58.349364', '2026-10-09 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (65, 5, '사이드 프로젝트를 6개월간 운영하면서 배운 것들을 정리했다.', 'ACTIVE', 45, 7, 0, NULL, '2026-10-10 17:48:58.349364', '2026-10-10 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (66, 6, 'Docker layer caching 전략을 제대로 적용하니 빌드 시간이 반으로 줄었다.', 'ACTIVE', 23, 3, 0, NULL, '2026-10-11 17:48:58.349364', '2026-10-11 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (67, 7, 'TypeScript 프로젝트에서 strict 모드를 활성화했다. 처음엔 힘들지만 장기적으로 이득이다.', 'ACTIVE', 20, 3, 0, NULL, '2026-10-12 17:48:58.349364', '2026-10-12 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (68, 8, 'Spring의 @Transactional 전파 옵션을 제대로 이해하지 못하면 데이터 불일치가 생긴다.', 'ACTIVE', 36, 5, 0, NULL, '2026-10-13 17:48:58.349364', '2026-10-13 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (69, 9, '모바일 앱 UX 개선을 위해 사용자 인터뷰를 진행했다. 개발자가 놓치는 부분이 생각보다 많다.', 'ACTIVE', 12, 2, 0, NULL, '2026-10-14 17:48:58.349364', '2026-10-14 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (70, 10, 'S3 Intelligent-Tiering으로 스토리지 비용을 30% 절감했다.', 'ACTIVE', 27, 4, 0, NULL, '2026-10-15 17:48:58.349364', '2026-10-15 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (71, 11, 'Vue의 Teleport를 이용해 모달 z-index 문제를 깔끔하게 해결했다.', 'ACTIVE', 14, 2, 0, NULL, '2026-10-16 17:48:58.349364', '2026-10-16 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (72, 12, 'EXPLAIN ANALYZE로 쿼리 실행 계획을 분석하는 방법을 팀 내 발표했다.', 'ACTIVE', 38, 5, 0, NULL, '2026-10-17 17:48:58.349364', '2026-10-17 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (73, 13, 'Go의 context 패키지로 요청 취소와 타임아웃을 제대로 처리했다.', 'ACTIVE', 18, 2, 0, NULL, '2026-10-18 17:48:58.349364', '2026-10-18 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (74, 14, 'Rust에서 trait object와 generic의 차이를 실제 코드로 비교해봤다.', 'ACTIVE', 7, 1, 0, NULL, '2026-10-19 17:48:58.349364', '2026-10-19 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (75, 15, 'next/image로 이미지 최적화를 하니 LCP 점수가 크게 올랐다.', 'ACTIVE', 29, 4, 0, NULL, '2026-10-20 17:48:58.349364', '2026-10-20 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (76, 16, 'Helm chart로 Kubernetes 앱 배포를 표준화했다. 환경별 설정 관리가 편해졌다.', 'ACTIVE', 24, 3, 0, NULL, '2026-10-21 17:48:58.349364', '2026-10-21 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (77, 17, 'Spring의 이벤트 시스템으로 도메인 이벤트를 구현했다. 서비스 간 의존도가 낮아진다.', 'ACTIVE', 19, 3, 0, NULL, '2026-10-22 17:48:58.349364', '2026-10-22 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (78, 18, '접근성을 고려한 UI 개발을 시작했다. ARIA 속성이 생각보다 중요하다.', 'ACTIVE', 32, 5, 0, NULL, '2026-10-23 17:48:58.349364', '2026-10-23 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (79, 19, 'Datadog APM으로 서비스 레이턴시 병목을 찾아냈다.', 'ACTIVE', 21, 3, 0, NULL, '2026-10-24 17:48:58.349364', '2026-10-24 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (80, 20, 'GitHub에 첫 번째 오픈소스 라이브러리를 공개했다. 스타가 50개 달렸다.', 'ACTIVE', 48, 8, 0, NULL, '2026-10-25 17:48:58.349364', '2026-10-25 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (81, 1, '개발자로서 5년을 돌아보는 회고를 작성했다.', 'ACTIVE', 39, 6, 0, NULL, '2026-10-26 17:48:58.349364', '2026-10-26 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (82, 2, '테스트 코드를 먼저 작성하는 습관을 들이기 시작했다. TDD가 처음엔 느리지만 결국 빠르다.', 'ACTIVE', 25, 4, 0, NULL, '2026-10-27 17:48:58.349364', '2026-10-27 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (83, 3, 'API 버저닝 전략을 정리했다. URL 버저닝 vs 헤더 버저닝의 트레이드오프.', 'ACTIVE', 17, 2, 0, NULL, '2026-10-28 17:48:58.349364', '2026-10-28 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (84, 4, 'Jupyter Notebook을 팀원들과 공유하는 워크플로우를 만들었다.', 'ACTIVE', 14, 2, 0, NULL, '2026-10-29 17:48:58.349364', '2026-10-29 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (85, 5, '사이드 프로젝트 2.0 버전 로드맵을 공개했다. 기대해주세요!', 'ACTIVE', 51, 9, 0, NULL, '2026-10-30 17:48:58.349364', '2026-10-30 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (86, 6, 'containerd로 Docker 없이 컨테이너 런타임을 직접 다뤄봤다.', 'ACTIVE', 16, 2, 0, NULL, '2026-10-31 17:48:58.349364', '2026-10-31 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (87, 7, 'tRPC를 프로젝트에 도입했다. 프론트-백 타입 공유가 이렇게 쉬울 줄이야.', 'ACTIVE', 27, 4, 0, NULL, '2026-11-01 17:48:58.349364', '2026-11-01 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (88, 8, 'Java record를 DTO로 활용하니 코드가 훨씬 간결해졌다.', 'ACTIVE', 22, 3, 0, NULL, '2026-11-01 17:48:58.349364', '2026-11-01 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (89, 9, 'Flutter web을 처음 배포해봤다. 모바일 코드를 그대로 웹으로 쓸 수 있다.', 'ACTIVE', 10, 1, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (90, 10, 'CloudFront Function으로 URL 리다이렉트를 처리했다. Lambda@Edge보다 가볍다.', 'ACTIVE', 23, 3, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (91, 11, 'Vite로 빌드 도구를 전환했다. 개발 서버 시작이 1초도 안 걸린다.', 'ACTIVE', 18, 2, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (92, 12, 'pg_stat_statements로 자주 실행되는 느린 쿼리를 찾아냈다.', 'ACTIVE', 31, 4, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (93, 13, 'gRPC로 마이크로서비스 간 통신을 REST에서 전환했다. 속도가 체감될 정도로 빠르다.', 'ACTIVE', 24, 3, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (94, 14, 'Rust WASM으로 브라우저에서 이미지 처리를 구현했다.', 'ACTIVE', 12, 2, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (95, 15, 'Vercel Analytics로 실제 사용자 경험을 측정하기 시작했다.', 'ACTIVE', 20, 2, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (96, 16, 'K8s resource request/limit 설정의 중요성을 OOM kill을 당하고 나서야 깨달았다.', 'ACTIVE', 29, 4, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (97, 17, '이번 주 스프링 스터디 발표 준비 완료. 의존성 주입 원리를 깊게 파봤다.', 'ACTIVE', 13, 1, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (98, 18, 'Tailwind CSS v4 베타를 써봤다. CSS 변수 기반으로 바뀌어서 커스터마이징이 훨씬 편하다.', 'ACTIVE', 34, 5, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (99, 19, '사내 온콜 대응 가이드를 작성했다. 장애 상황에서 빠르게 대응하는 방법을 정리했다.', 'ACTIVE', 19, 2, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (100, 20, 'ripgrep 소스 코드를 분석하면서 Rust 실력을 키우고 있다.', 'ACTIVE', 16, 2, 0, NULL, '2026-11-02 17:48:58.349364', '2026-11-02 17:48:58.349364') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (101, 21, '시드 피드 #1-1. Spring Boot 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, NULL, '2026-11-04 13:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (102, 21, '시드 피드 #1-2. JPA 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-11-04 12:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (103, 22, '시드 피드 #2-1. JPA 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-11-04 11:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (104, 22, '시드 피드 #2-2. React 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, NULL, '2026-11-04 10:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (105, 23, '시드 피드 #3-1. React 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, NULL, '2026-11-04 09:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (106, 23, '시드 피드 #3-2. TypeScript 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 1, NULL, '2026-11-04 08:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (107, 24, '시드 피드 #4-1. TypeScript 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 1, NULL, '2026-11-04 07:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (108, 24, '시드 피드 #4-2. Next.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-11-04 06:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (109, 25, '시드 피드 #5-1. Next.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-11-04 05:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (110, 25, '시드 피드 #5-2. Python 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-04 04:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (111, 26, '시드 피드 #6-1. Python 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-04 03:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (112, 26, '시드 피드 #6-2. Docker 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, NULL, '2026-11-04 02:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (113, 27, '시드 피드 #7-1. Docker 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, NULL, '2026-11-04 01:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (114, 27, '시드 피드 #7-2. Kubernetes 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-11-04 00:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (115, 28, '시드 피드 #8-1. Kubernetes 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-11-03 23:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (116, 28, '시드 피드 #8-2. AWS 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 1, NULL, '2026-11-03 22:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (117, 29, '시드 피드 #9-1. AWS 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 1, NULL, '2026-11-03 21:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (118, 29, '시드 피드 #9-2. Node.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-11-03 20:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (119, 30, '시드 피드 #10-1. Node.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-11-03 19:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (120, 30, '시드 피드 #10-2. WebSocket 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-11-03 18:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (121, 31, '시드 피드 #11-1. WebSocket 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-11-03 17:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (122, 31, '시드 피드 #11-2. Redis 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, NULL, '2026-11-03 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (123, 32, '시드 피드 #12-1. Redis 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, NULL, '2026-11-03 15:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (124, 32, '시드 피드 #12-2. PostgreSQL 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-03 14:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (125, 33, '시드 피드 #13-1. PostgreSQL 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-03 13:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (126, 33, '시드 피드 #13-2. Unity 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 1, NULL, '2026-11-03 12:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (127, 34, '시드 피드 #14-1. Unity 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 1, NULL, '2026-11-03 11:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (128, 34, '시드 피드 #14-2. Java 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, NULL, '2026-11-03 10:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (129, 35, '시드 피드 #15-1. Java 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, NULL, '2026-11-03 09:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (130, 35, '시드 피드 #15-2. Spring Boot 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-11-03 08:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (131, 36, '시드 피드 #16-1. Spring Boot 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-11-03 07:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (132, 36, '시드 피드 #16-2. JPA 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-11-03 06:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (133, 37, '시드 피드 #17-1. JPA 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-11-03 05:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (134, 37, '시드 피드 #17-2. React 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, NULL, '2026-11-03 04:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (135, 38, '시드 피드 #18-1. React 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, NULL, '2026-11-03 03:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (136, 38, '시드 피드 #18-2. TypeScript 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 1, NULL, '2026-11-03 02:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (137, 39, '시드 피드 #19-1. TypeScript 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 1, NULL, '2026-11-03 01:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (138, 39, '시드 피드 #19-2. Next.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-03 00:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (139, 40, '시드 피드 #20-1. Next.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-02 23:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (140, 40, '시드 피드 #20-2. Python 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, NULL, '2026-11-02 22:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (141, 41, '시드 피드 #21-1. Python 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, NULL, '2026-11-02 21:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (142, 41, '시드 피드 #21-2. Docker 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-11-02 20:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (143, 42, '시드 피드 #22-1. Docker 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-11-02 19:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (144, 42, '시드 피드 #22-2. Kubernetes 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-11-02 18:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (145, 43, '시드 피드 #23-1. Kubernetes 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-11-02 17:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (146, 43, '시드 피드 #23-2. AWS 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 1, NULL, '2026-11-02 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (147, 44, '시드 피드 #24-1. AWS 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 1, NULL, '2026-11-02 15:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (148, 44, '시드 피드 #24-2. Node.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, NULL, '2026-11-02 14:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (149, 45, '시드 피드 #25-1. Node.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, NULL, '2026-11-02 13:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (150, 45, '시드 피드 #25-2. WebSocket 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-11-02 12:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (151, 46, '시드 피드 #26-1. WebSocket 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-11-02 11:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (152, 46, '시드 피드 #26-2. Redis 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-02 10:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (153, 47, '시드 피드 #27-1. Redis 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-02 09:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (154, 47, '시드 피드 #27-2. PostgreSQL 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-11-02 08:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (155, 48, '시드 피드 #28-1. PostgreSQL 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-11-02 07:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (156, 48, '시드 피드 #28-2. Unity 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 1, NULL, '2026-11-02 06:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (157, 49, '시드 피드 #29-1. Unity 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 1, NULL, '2026-11-02 05:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (158, 49, '시드 피드 #29-2. Java 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, NULL, '2026-11-02 04:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (159, 50, '시드 피드 #30-1. Java 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, NULL, '2026-11-02 03:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (160, 50, '시드 피드 #30-2. Spring Boot 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, NULL, '2026-11-02 02:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (161, 51, '시드 피드 #31-1. Spring Boot 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, NULL, '2026-11-02 01:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (162, 51, '시드 피드 #31-2. JPA 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-11-02 00:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (163, 52, '시드 피드 #32-1. JPA 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-11-01 23:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (164, 52, '시드 피드 #32-2. React 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, NULL, '2026-11-01 22:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (165, 53, '시드 피드 #33-1. React 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, NULL, '2026-11-01 21:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (166, 53, '시드 피드 #33-2. TypeScript 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 1, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-01 20:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (167, 54, '시드 피드 #34-1. TypeScript 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 1, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-01 19:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (168, 54, '시드 피드 #34-2. Next.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-11-01 18:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (169, 55, '시드 피드 #35-1. Next.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-11-01 17:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (170, 55, '시드 피드 #35-2. Python 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, NULL, '2026-11-01 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (171, 56, '시드 피드 #36-1. Python 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, NULL, '2026-11-01 15:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (172, 56, '시드 피드 #36-2. Docker 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, NULL, '2026-11-01 14:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (173, 57, '시드 피드 #37-1. Docker 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, NULL, '2026-11-01 13:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (174, 57, '시드 피드 #37-2. Kubernetes 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-11-01 12:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (175, 58, '시드 피드 #38-1. Kubernetes 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-11-01 11:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (176, 58, '시드 피드 #38-2. AWS 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 1, NULL, '2026-11-01 10:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (177, 59, '시드 피드 #39-1. AWS 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 1, NULL, '2026-11-01 09:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (178, 59, '시드 피드 #39-2. Node.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-11-01 08:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (179, 60, '시드 피드 #40-1. Node.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-11-01 07:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (180, 60, '시드 피드 #40-2. WebSocket 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-01 06:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (181, 61, '시드 피드 #41-1. WebSocket 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-11-01 05:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (182, 61, '시드 피드 #41-2. Redis 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, NULL, '2026-11-01 04:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (183, 62, '시드 피드 #42-1. Redis 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, NULL, '2026-11-01 03:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (184, 62, '시드 피드 #42-2. PostgreSQL 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, NULL, '2026-11-01 02:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (185, 63, '시드 피드 #43-1. PostgreSQL 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 0, NULL, '2026-11-01 01:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (186, 63, '시드 피드 #43-2. Unity 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 1, NULL, '2026-11-01 00:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (187, 64, '시드 피드 #44-1. Unity 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 1, NULL, '2026-10-31 23:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (188, 64, '시드 피드 #44-2. Java 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, NULL, '2026-10-31 22:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (189, 65, '시드 피드 #45-1. Java 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, NULL, '2026-10-31 21:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (190, 65, '시드 피드 #45-2. Spring Boot 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-10-31 20:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (191, 66, '시드 피드 #46-1. Spring Boot 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 6, 1, 0, NULL, '2026-10-31 19:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (192, 66, '시드 피드 #46-2. JPA 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-10-31 18:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (193, 67, '시드 피드 #47-1. JPA 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 1, 3, 0, NULL, '2026-10-31 17:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (194, 67, '시드 피드 #47-2. React 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-10-31 16:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (195, 68, '시드 피드 #48-1. React 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 2, 1, 0, '["Spring", "Node", "둘 다"]'::jsonb, '2026-10-31 15:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (196, 68, '시드 피드 #48-2. TypeScript 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 1, NULL, '2026-10-31 14:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (197, 69, '시드 피드 #49-1. TypeScript 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 3, 2, 1, NULL, '2026-10-31 13:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (198, 69, '시드 피드 #49-2. Next.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-10-31 12:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (199, 70, '시드 피드 #50-1. Next.js 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 4, 2, 0, NULL, '2026-10-31 11:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;
INSERT INTO feeds (id, user_id, content, status, like_count, comment_count, repost_count, poll_options, created_at, updated_at) VALUES (200, 70, '시드 피드 #50-2. Python 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.', 'ACTIVE', 5, 2, 0, NULL, '2026-10-31 10:01:18.637528', '2026-11-04 16:01:18.637528') ON CONFLICT (id) DO NOTHING;

SELECT setval('feeds_id_seq', (SELECT COALESCE(MAX(id), 0) FROM feeds));

-- ========== feed_tags ==========
INSERT INTO feed_tags (feed_id, tag_id) VALUES (1, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (1, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (2, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (2, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (3, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (3, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (3, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (4, 5) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (4, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (5, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (5, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (5, 3) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (6, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (6, 8) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (7, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (7, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (8, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (8, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (9, 12) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (10, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (10, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (11, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (11, 13) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (12, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (12, 10) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (13, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (13, 14) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (14, 15) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (15, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (15, 3) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (15, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (16, 8) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (16, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (17, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (17, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (18, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (18, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (19, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (19, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (20, 14) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (20, 15) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (21, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (21, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (21, 10) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (22, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (22, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (23, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (23, 11) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (24, 5) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (24, 10) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (25, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (25, 3) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (26, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (26, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (27, 3) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (27, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (28, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (28, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (29, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (29, 12) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (30, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (30, 11) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (31, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (31, 13) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (32, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (32, 10) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (33, 14) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (34, 14) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (34, 15) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (35, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (35, 3) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (36, 8) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (36, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (37, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (37, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (38, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (38, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (39, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (39, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (40, 15) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (41, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (41, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (42, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (43, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (43, 11) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (44, 5) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (44, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (45, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (45, 3) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (45, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (46, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (46, 8) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (47, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (47, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (48, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (48, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (49, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (49, 12) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (50, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (50, 11) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (51, 3) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (51, 13) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (52, 10) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (52, 11) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (53, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (53, 14) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (54, 15) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (55, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (55, 3) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (56, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (56, 8) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (57, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (57, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (58, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (58, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (59, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (59, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (60, 14) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (60, 15) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (61, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (61, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (62, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (62, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (63, 10) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (63, 11) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (64, 5) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (65, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (65, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (66, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (67, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (67, 4) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (68, 1) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (68, 6) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (69, 12) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (70, 9) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (70, 11) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (71, 2) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (71, 13) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (72, 10) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (73, 7) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (73, 14) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (101, 28) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (102, 16) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (103, 16) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (104, 23) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (105, 23) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (106, 29) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (107, 29) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (108, 17) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (109, 17) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (110, 22) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (111, 22) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (112, 18) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (113, 18) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (114, 21) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (115, 21) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (116, 20) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (117, 20) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (118, 19) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (119, 19) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (120, 26) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (121, 26) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (122, 27) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (123, 27) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (124, 24) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (125, 24) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (126, 25) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (127, 25) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (128, 30) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (129, 30) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (130, 28) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (131, 28) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (132, 16) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (133, 16) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (134, 23) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (135, 23) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (136, 29) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (137, 29) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (138, 17) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (139, 17) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (140, 22) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (141, 22) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (142, 18) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (143, 18) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (144, 21) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (145, 21) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (146, 20) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (147, 20) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (148, 19) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (149, 19) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (150, 26) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (151, 26) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (152, 27) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (153, 27) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (154, 24) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (155, 24) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (156, 25) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (157, 25) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (158, 30) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (159, 30) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (160, 28) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (161, 28) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (162, 16) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (163, 16) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (164, 23) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (165, 23) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (166, 29) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (167, 29) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (168, 17) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (169, 17) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (170, 22) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (171, 22) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (172, 18) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (173, 18) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (174, 21) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (175, 21) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (176, 20) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (177, 20) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (178, 19) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (179, 19) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (180, 26) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (181, 26) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (182, 27) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (183, 27) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (184, 24) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (185, 24) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (186, 25) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (187, 25) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (188, 30) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (189, 30) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (190, 28) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (191, 28) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (192, 16) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (193, 16) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (194, 23) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (195, 23) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (196, 29) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (197, 29) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (198, 17) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (199, 17) ON CONFLICT DO NOTHING;
INSERT INTO feed_tags (feed_id, tag_id) VALUES (200, 22) ON CONFLICT DO NOTHING;

-- ========== feed_likes ==========
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (1, 2, '2026-10-06 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (1, 3, '2026-10-07 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (1, 4, '2026-10-07 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (1, 5, '2026-10-08 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (1, 6, '2026-10-09 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (2, 1, '2026-10-06 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (2, 3, '2026-10-07 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (2, 7, '2026-10-08 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (3, 1, '2026-10-07 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (3, 4, '2026-10-08 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (4, 5, '2026-10-07 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (4, 6, '2026-10-08 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (4, 7, '2026-10-09 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (4, 8, '2026-10-10 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (5, 2, '2026-10-08 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (5, 3, '2026-10-09 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (5, 4, '2026-10-10 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (5, 6, '2026-10-11 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (5, 7, '2026-10-12 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (5, 8, '2026-10-13 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (5, 9, '2026-10-14 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (6, 1, '2026-10-08 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (6, 3, '2026-10-09 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (6, 5, '2026-10-10 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (7, 2, '2026-10-09 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (7, 4, '2026-10-10 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (8, 1, '2026-10-09 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (8, 3, '2026-10-10 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (8, 5, '2026-10-11 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (8, 6, '2026-10-12 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (9, 2, '2026-10-10 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (10, 1, '2026-10-10 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (10, 3, '2026-10-11 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (10, 4, '2026-10-12 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (10, 5, '2026-10-13 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (10, 6, '2026-10-14 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (12, 2, '2026-10-11 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (12, 4, '2026-10-12 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (12, 5, '2026-10-13 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (12, 6, '2026-10-14 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (12, 7, '2026-10-15 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (12, 8, '2026-10-16 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (15, 1, '2026-10-13 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (15, 2, '2026-10-14 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (15, 3, '2026-10-15 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (15, 4, '2026-10-16 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (15, 6, '2026-10-17 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (15, 7, '2026-10-18 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (18, 1, '2026-10-14 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (18, 2, '2026-10-15 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (18, 3, '2026-10-16 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (18, 5, '2026-10-17 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (21, 6, '2026-10-16 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (21, 7, '2026-10-17 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (21, 8, '2026-10-18 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (21, 9, '2026-10-19 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (21, 10, '2026-10-20 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (22, 1, '2026-10-16 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (22, 3, '2026-10-17 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (22, 5, '2026-10-18 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (24, 2, '2026-10-18 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (24, 3, '2026-10-19 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (24, 4, '2026-10-20 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (24, 6, '2026-10-21 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (24, 7, '2026-10-22 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (25, 8, '2026-10-19 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (25, 9, '2026-10-20 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (30, 1, '2026-10-25 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (30, 2, '2026-10-26 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (30, 3, '2026-10-27 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (32, 4, '2026-10-21 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (32, 5, '2026-10-22 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (32, 6, '2026-10-23 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (32, 7, '2026-10-24 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (32, 8, '2026-10-25 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (35, 1, '2026-10-23 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (35, 2, '2026-10-24 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (35, 3, '2026-10-25 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (38, 4, '2026-10-24 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (38, 5, '2026-10-25 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (38, 6, '2026-10-26 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (38, 7, '2026-10-27 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (41, 1, '2026-10-26 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (41, 2, '2026-10-27 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (41, 3, '2026-10-28 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (41, 4, '2026-10-29 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (41, 5, '2026-10-30 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (44, 2, '2026-10-27 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (44, 3, '2026-10-28 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (44, 5, '2026-10-29 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (44, 6, '2026-10-30 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (48, 1, '2026-10-29 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (48, 3, '2026-10-30 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (48, 4, '2026-10-31 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (49, 2, '2026-10-30 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (49, 3, '2026-10-31 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (49, 4, '2026-11-01 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (49, 6, '2026-11-01 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (49, 7, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (52, 1, '2026-10-31 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (52, 2, '2026-11-01 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (52, 3, '2026-11-01 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (52, 4, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (55, 5, '2026-11-01 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (55, 6, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (58, 7, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (58, 8, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (61, 1, '2026-10-07 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (61, 2, '2026-10-08 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (61, 3, '2026-10-09 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (65, 4, '2026-10-11 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (65, 5, '2026-10-12 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (65, 6, '2026-10-13 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (65, 7, '2026-10-14 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (65, 8, '2026-10-15 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (68, 1, '2026-10-14 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (68, 2, '2026-10-15 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (68, 3, '2026-10-16 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (68, 4, '2026-10-17 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (72, 1, '2026-10-18 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (72, 2, '2026-10-19 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (72, 3, '2026-10-20 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (72, 5, '2026-10-21 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (75, 2, '2026-10-21 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (75, 4, '2026-10-22 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (78, 6, '2026-10-24 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (78, 7, '2026-10-25 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (78, 8, '2026-10-26 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (80, 1, '2026-10-26 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (80, 2, '2026-10-27 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (80, 3, '2026-10-28 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (80, 4, '2026-10-29 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (80, 5, '2026-10-30 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (81, 6, '2026-10-27 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (81, 7, '2026-10-28 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (81, 8, '2026-10-29 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (81, 9, '2026-10-30 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (85, 1, '2026-10-31 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (85, 2, '2026-11-01 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (85, 3, '2026-11-01 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (85, 4, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (85, 5, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (87, 1, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (87, 3, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (92, 5, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (92, 6, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (92, 7, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (98, 2, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (98, 4, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (98, 6, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (100, 1, '2026-11-02 17:48:58.358538') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (101, 27, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (101, 32, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (101, 37, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (102, 28, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (102, 33, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (102, 38, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (102, 43, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (103, 28, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (103, 33, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (103, 38, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (103, 43, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (104, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (104, 34, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (104, 39, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (104, 44, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (104, 49, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (105, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (105, 34, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (105, 39, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (105, 44, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (105, 49, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (106, 30, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (106, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (106, 40, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (106, 45, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (106, 50, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (106, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (107, 30, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (107, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (107, 40, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (107, 45, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (107, 50, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (107, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (108, 31, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (109, 31, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (110, 32, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (110, 37, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (111, 32, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (111, 37, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (112, 33, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (112, 38, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (112, 43, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (113, 33, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (113, 38, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (113, 43, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (114, 34, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (114, 39, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (114, 44, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (114, 49, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (115, 34, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (115, 39, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (115, 44, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (115, 49, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (116, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (116, 40, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (116, 45, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (116, 50, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (116, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (117, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (117, 40, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (117, 45, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (117, 50, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (117, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (118, 36, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (118, 41, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (118, 46, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (118, 51, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (118, 56, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (118, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (119, 36, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (119, 41, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (119, 46, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (119, 51, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (119, 56, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (119, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (120, 37, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (121, 37, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (122, 38, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (122, 43, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (123, 38, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (123, 43, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (124, 39, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (124, 44, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (124, 49, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (125, 39, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (125, 44, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (125, 49, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (126, 40, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (126, 45, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (126, 50, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (126, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (127, 40, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (127, 45, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (127, 50, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (127, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (128, 41, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (128, 46, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (128, 51, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (128, 56, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (128, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (129, 41, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (129, 46, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (129, 51, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (129, 56, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (129, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (130, 42, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (130, 47, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (130, 52, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (130, 57, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (130, 62, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (130, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (131, 42, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (131, 47, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (131, 52, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (131, 57, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (131, 62, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (131, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (132, 43, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (133, 43, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (134, 44, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (134, 49, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (135, 44, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (135, 49, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (136, 45, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (136, 50, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (136, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (137, 45, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (137, 50, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (137, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (138, 46, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (138, 51, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (138, 56, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (138, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (139, 46, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (139, 51, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (139, 56, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (139, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (140, 47, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (140, 52, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (140, 57, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (140, 62, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (140, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (141, 47, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (141, 52, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (141, 57, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (141, 62, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (141, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (142, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (142, 48, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (142, 53, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (142, 58, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (142, 63, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (142, 68, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (143, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (143, 48, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (143, 53, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (143, 58, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (143, 63, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (143, 68, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (144, 49, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (145, 49, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (146, 50, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (146, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (147, 50, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (147, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (148, 51, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (148, 56, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (148, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (149, 51, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (149, 56, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (149, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (150, 52, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (150, 57, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (150, 62, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (150, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (151, 52, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (151, 57, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (151, 62, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (151, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (152, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (152, 53, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (152, 58, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (152, 63, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (152, 68, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (153, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (153, 53, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (153, 58, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (153, 63, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (153, 68, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (154, 24, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (154, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (154, 54, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (154, 59, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (154, 64, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (154, 69, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (155, 24, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (155, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (155, 54, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (155, 59, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (155, 64, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (155, 69, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (156, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (157, 55, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (158, 56, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (158, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (159, 56, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (159, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (160, 57, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (160, 62, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (160, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (161, 57, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (161, 62, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (161, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (162, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (162, 58, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (162, 63, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (162, 68, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (163, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (163, 58, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (163, 63, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (163, 68, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (164, 24, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (164, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (164, 59, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (164, 64, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (164, 69, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (165, 24, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (165, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (165, 59, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (165, 64, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (165, 69, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (166, 25, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (166, 30, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (166, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (166, 60, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (166, 65, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (166, 70, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (167, 25, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (167, 30, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (167, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (167, 60, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (167, 65, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (167, 70, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (168, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (169, 61, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (170, 62, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (170, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (171, 62, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (171, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (172, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (172, 63, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (172, 68, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (173, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (173, 63, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (173, 68, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (174, 24, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (174, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (174, 64, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (174, 69, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (175, 24, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (175, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (175, 64, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (175, 69, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (176, 25, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (176, 30, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (176, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (176, 65, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (176, 70, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (177, 25, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (177, 30, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (177, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (177, 65, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (177, 70, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (178, 21, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (178, 26, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (178, 31, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (178, 36, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (178, 41, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (178, 66, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (179, 21, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (179, 26, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (179, 31, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (179, 36, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (179, 41, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (179, 66, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (180, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (181, 67, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (182, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (182, 68, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (183, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (183, 68, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (184, 24, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (184, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (184, 69, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (185, 24, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (185, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (185, 69, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (186, 25, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (186, 30, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (186, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (186, 70, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (187, 25, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (187, 30, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (187, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (187, 70, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (188, 21, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (188, 26, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (188, 31, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (188, 36, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (188, 41, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (189, 21, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (189, 26, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (189, 31, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (189, 36, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (189, 41, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (190, 22, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (190, 27, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (190, 32, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (190, 37, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (190, 42, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (190, 47, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (191, 22, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (191, 27, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (191, 32, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (191, 37, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (191, 42, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (191, 47, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (192, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (193, 23, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (194, 24, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (194, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (195, 24, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (195, 29, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (196, 25, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (196, 30, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (196, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (197, 25, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (197, 30, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (197, 35, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (198, 26, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (198, 31, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (198, 36, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (198, 41, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (199, 26, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (199, 31, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (199, 36, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (199, 41, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (200, 27, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (200, 32, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (200, 37, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (200, 42, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO feed_likes (feed_id, user_id, created_at) VALUES (200, 47, '2026-11-04 15:41:18.637528') ON CONFLICT DO NOTHING;

-- ========== feed_comments ==========











































































































































































































































-- ========== follows ==========
INSERT INTO follows (follower_id, following_id, created_at) VALUES (1, 3, '2026-09-09 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (1, 6, '2026-09-28 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (1, 9, '2026-10-09 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (1, 12, '2026-10-15 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (1, 15, '2026-10-21 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (1, 20, '2026-11-02 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (2, 1, '2026-08-15 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (2, 4, '2026-09-20 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (2, 9, '2026-10-12 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (2, 12, '2026-10-17 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (2, 15, '2026-10-23 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (3, 1, '2026-08-20 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (3, 5, '2026-09-18 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (3, 10, '2026-10-11 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (3, 12, '2026-10-19 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (3, 15, '2026-10-25 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (4, 1, '2026-08-25 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (4, 5, '2026-09-22 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (4, 12, '2026-10-21 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (4, 15, '2026-10-27 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (5, 1, '2026-08-30 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (5, 3, '2026-09-14 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (5, 10, '2026-10-14 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (5, 16, '2026-10-23 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (6, 2, '2026-08-27 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (6, 3, '2026-09-24 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (6, 5, '2026-09-26 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (6, 16, '2026-10-25 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (7, 2, '2026-09-02 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (7, 5, '2026-09-30 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (7, 10, '2026-10-16 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (7, 17, '2026-10-24 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (8, 2, '2026-09-06 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (8, 6, '2026-10-02 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (8, 17, '2026-10-26 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (9, 4, '2026-09-12 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (9, 7, '2026-10-04 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (9, 18, '2026-10-26 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (10, 4, '2026-09-16 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (10, 7, '2026-10-06 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (10, 18, '2026-10-28 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (11, 4, '2026-10-04 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (11, 7, '2026-10-08 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (11, 18, '2026-10-30 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (12, 8, '2026-10-06 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (12, 19, '2026-10-29 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (13, 8, '2026-10-10 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (13, 19, '2026-10-31 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (14, 8, '2026-10-12 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (14, 19, '2026-11-01 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (15, 11, '2026-10-14 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (15, 20, '2026-11-01 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (16, 11, '2026-10-16 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (16, 20, '2026-11-02 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (17, 13, '2026-10-17 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (18, 2, '2026-10-14 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (18, 13, '2026-10-19 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (19, 14, '2026-10-19 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (20, 5, '2026-10-24 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (20, 14, '2026-10-21 17:48:58.366274') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 22, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 23, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 24, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 25, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 26, '2026-11-04 10:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 27, '2026-11-04 09:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 29, '2026-11-04 07:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 30, '2026-11-04 06:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 32, '2026-11-04 04:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 33, '2026-11-04 03:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 35, '2026-11-04 01:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 36, '2026-11-04 00:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 38, '2026-11-03 22:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 39, '2026-11-03 21:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 41, '2026-11-03 19:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 42, '2026-11-03 18:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 44, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (21, 45, '2026-11-03 15:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 21, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 23, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 24, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 25, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 26, '2026-11-04 10:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 28, '2026-11-04 08:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 29, '2026-11-04 07:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 31, '2026-11-04 05:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 32, '2026-11-04 04:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 34, '2026-11-04 02:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 35, '2026-11-04 01:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 37, '2026-11-03 23:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 38, '2026-11-03 22:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 40, '2026-11-03 20:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 41, '2026-11-03 19:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 43, '2026-11-03 17:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (22, 44, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 21, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 22, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 24, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 25, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 27, '2026-11-04 09:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 28, '2026-11-04 08:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 30, '2026-11-04 06:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 31, '2026-11-04 05:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 33, '2026-11-04 03:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 34, '2026-11-04 02:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 36, '2026-11-04 00:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 37, '2026-11-03 23:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 39, '2026-11-03 21:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 40, '2026-11-03 20:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 42, '2026-11-03 18:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 43, '2026-11-03 17:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (23, 45, '2026-11-03 15:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 21, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 22, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 23, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 25, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 26, '2026-11-04 10:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 27, '2026-11-04 09:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 29, '2026-11-04 07:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 30, '2026-11-04 06:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 32, '2026-11-04 04:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 33, '2026-11-04 03:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 35, '2026-11-04 01:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 36, '2026-11-04 00:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 38, '2026-11-03 22:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 39, '2026-11-03 21:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 41, '2026-11-03 19:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 42, '2026-11-03 18:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 44, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (24, 45, '2026-11-03 15:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 21, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 22, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 23, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 24, '2026-10-25 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 26, '2026-11-04 10:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 28, '2026-11-04 08:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 29, '2026-11-04 07:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 31, '2026-11-04 05:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 32, '2026-11-04 04:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 34, '2026-11-04 02:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 35, '2026-11-04 01:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 37, '2026-11-03 23:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 38, '2026-11-03 22:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 40, '2026-11-03 20:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 41, '2026-11-03 19:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 43, '2026-11-03 17:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (25, 44, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (26, 22, '2026-11-04 10:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (26, 33, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (26, 40, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (26, 47, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (26, 54, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (27, 21, '2026-11-04 09:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (27, 25, '2026-11-04 09:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (27, 34, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (27, 41, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (27, 48, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (27, 55, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (28, 24, '2026-11-04 08:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (28, 35, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (28, 42, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (28, 49, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (28, 56, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (29, 23, '2026-11-04 07:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (29, 36, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (29, 43, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (29, 50, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (29, 57, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (30, 22, '2026-11-04 06:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (30, 37, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (30, 44, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (30, 51, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (30, 58, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (31, 21, '2026-11-04 05:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (31, 25, '2026-11-04 05:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (31, 38, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (31, 45, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (31, 52, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (31, 59, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (32, 24, '2026-11-04 04:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (32, 39, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (32, 46, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (32, 53, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (32, 60, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (33, 23, '2026-11-04 03:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (33, 40, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (33, 47, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (33, 54, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (33, 61, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (34, 22, '2026-11-04 02:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (34, 41, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (34, 48, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (34, 55, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (34, 62, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (35, 21, '2026-11-04 01:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (35, 25, '2026-11-04 01:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (35, 42, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (35, 49, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (35, 56, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (35, 63, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (36, 24, '2026-11-04 00:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (36, 43, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (36, 50, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (36, 57, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (36, 64, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (37, 23, '2026-11-03 23:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (37, 44, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (37, 51, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (37, 58, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (37, 65, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (38, 22, '2026-11-03 22:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (38, 45, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (38, 52, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (38, 59, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (38, 66, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (39, 21, '2026-11-03 21:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (39, 25, '2026-11-03 21:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (39, 46, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (39, 53, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (39, 60, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (39, 67, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (40, 24, '2026-11-03 20:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (40, 47, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (40, 54, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (40, 61, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (40, 68, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (41, 23, '2026-11-03 19:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (41, 48, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (41, 55, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (41, 62, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (41, 69, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (42, 22, '2026-11-03 18:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (42, 49, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (42, 56, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (42, 63, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (42, 70, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (43, 21, '2026-11-03 17:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (43, 25, '2026-11-03 17:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (43, 50, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (43, 57, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (43, 64, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (44, 22, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (44, 24, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (44, 51, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (44, 58, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (44, 65, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (45, 23, '2026-11-03 15:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (45, 52, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (45, 59, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (45, 66, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (46, 24, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (46, 53, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (46, 60, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (46, 67, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (47, 25, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (47, 54, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (47, 61, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (47, 68, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (48, 26, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (48, 55, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (48, 62, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (48, 69, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (49, 27, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (49, 56, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (49, 63, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (49, 70, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (50, 21, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (50, 28, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (50, 57, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (50, 64, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (51, 22, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (51, 29, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (51, 58, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (51, 65, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (52, 23, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (52, 30, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (52, 59, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (52, 66, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (53, 24, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (53, 31, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (53, 60, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (53, 67, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (54, 25, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (54, 32, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (54, 61, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (54, 68, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (55, 26, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (55, 33, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (55, 62, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (55, 69, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (56, 27, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (56, 34, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (56, 63, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (56, 70, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (57, 21, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (57, 28, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (57, 35, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (57, 64, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (58, 22, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (58, 29, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (58, 36, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (58, 65, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (59, 23, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (59, 30, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (59, 37, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (59, 66, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (60, 24, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (60, 31, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (60, 38, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (60, 67, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (61, 25, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (61, 32, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (61, 39, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (61, 68, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (62, 26, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (62, 33, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (62, 40, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (62, 69, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (63, 27, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (63, 34, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (63, 41, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (63, 70, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (64, 21, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (64, 28, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (64, 35, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (64, 42, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (65, 22, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (65, 29, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (65, 36, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (65, 43, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (66, 23, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (66, 30, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (66, 37, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (66, 44, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (67, 24, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (67, 31, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (67, 38, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (67, 45, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (68, 25, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (68, 32, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (68, 39, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (68, 46, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (69, 26, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (69, 33, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (69, 40, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (69, 47, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (70, 27, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (70, 34, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (70, 41, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO follows (follower_id, following_id, created_at) VALUES (70, 48, '2026-10-30 16:01:18.637528') ON CONFLICT DO NOTHING;

-- ========== user_interest_tags ==========
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (1, 1, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (1, 6, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (1, 10, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (2, 2, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (2, 3, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (2, 4, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (3, 1, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (3, 11, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (4, 5, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (4, 7, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (4, 9, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (5, 1, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (5, 2, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (5, 10, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (6, 7, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (6, 8, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (6, 14, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (7, 2, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (7, 3, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (7, 4, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (8, 1, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (8, 6, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (8, 11, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (9, 12, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (10, 7, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (10, 8, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (10, 9, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (11, 4, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (11, 13, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (12, 1, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (12, 10, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (12, 11, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (13, 7, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (13, 14, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (14, 14, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (14, 15, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (15, 2, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (15, 3, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (15, 9, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (16, 8, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (16, 9, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (16, 14, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (17, 1, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (17, 6, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (18, 2, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (18, 4, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (18, 13, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (19, 7, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (19, 8, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (19, 9, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (20, 5, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (20, 14, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (20, 15, '2026-11-03 17:48:58.367965') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (21, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (21, 28, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (21, 30, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (22, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (22, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (22, 28, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (22, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (23, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (23, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (24, 17, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (24, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (24, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (25, 17, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (25, 18, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (25, 22, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (25, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (26, 17, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (26, 22, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (27, 18, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (27, 21, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (27, 22, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (28, 18, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (28, 19, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (28, 20, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (28, 21, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (29, 20, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (29, 21, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (30, 19, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (30, 20, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (30, 26, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (31, 19, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (31, 24, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (31, 26, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (31, 27, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (32, 26, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (32, 27, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (33, 24, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (33, 25, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (33, 27, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (34, 24, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (34, 25, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (34, 28, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (34, 30, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (35, 25, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (35, 30, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (36, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (36, 28, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (36, 30, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (37, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (37, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (37, 28, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (37, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (38, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (38, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (39, 17, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (39, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (39, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (40, 17, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (40, 18, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (40, 22, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (40, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (41, 17, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (41, 22, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (42, 18, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (42, 21, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (42, 22, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (43, 18, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (43, 19, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (43, 20, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (43, 21, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (44, 20, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (44, 21, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (45, 19, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (45, 20, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (45, 26, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (46, 19, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (46, 24, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (46, 26, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (46, 27, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (47, 26, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (47, 27, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (48, 24, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (48, 25, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (48, 27, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (49, 24, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (49, 25, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (49, 28, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (49, 30, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (50, 25, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (50, 30, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (51, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (51, 28, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (51, 30, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (52, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (52, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (52, 28, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (52, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (53, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (53, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (54, 17, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (54, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (54, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (55, 17, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (55, 18, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (55, 22, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (55, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (56, 17, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (56, 22, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (57, 18, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (57, 21, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (57, 22, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (58, 18, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (58, 19, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (58, 20, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (58, 21, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (59, 20, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (59, 21, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (60, 19, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (60, 20, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (60, 26, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (61, 19, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (61, 24, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (61, 26, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (61, 27, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (62, 26, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (62, 27, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (63, 24, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (63, 25, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (63, 27, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (64, 24, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (64, 25, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (64, 28, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (64, 30, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (65, 25, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (65, 30, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (66, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (66, 28, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (66, 30, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (67, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (67, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (67, 28, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (67, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (68, 16, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (68, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (69, 17, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (69, 23, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (69, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (70, 17, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (70, 18, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (70, 22, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_interest_tags (user_id, tag_id, created_at) VALUES (70, 29, '2026-11-03 16:01:18.637528') ON CONFLICT DO NOTHING;

-- ========== user_tag_profile ==========
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (21, 16, 13, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (21, 28, 12, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (21, 30, 11, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (22, 16, 13, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (22, 23, 14, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (22, 28, 12, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (22, 29, 15, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (23, 16, 13, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (23, 23, 14, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (24, 17, 16, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (24, 23, 14, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (24, 29, 15, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (25, 17, 16, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (25, 18, 18, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (25, 22, 17, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (25, 29, 15, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (26, 17, 16, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (26, 22, 17, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (27, 18, 18, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (27, 21, 19, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (27, 22, 17, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (28, 18, 18, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (28, 19, 21, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (28, 20, 20, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (28, 21, 19, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (29, 20, 20, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (29, 21, 19, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (30, 19, 21, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (30, 20, 20, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (30, 26, 22, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (31, 19, 21, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (31, 24, 24, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (31, 26, 22, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (31, 27, 23, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (32, 26, 22, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (32, 27, 23, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (33, 24, 24, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (33, 25, 25, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (33, 27, 23, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (34, 24, 24, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (34, 25, 25, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (34, 28, 27, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (34, 30, 26, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (35, 25, 25, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (35, 30, 26, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (36, 16, 28, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (36, 28, 27, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (36, 30, 26, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (37, 16, 28, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (37, 23, 29, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (37, 28, 27, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (37, 29, 30, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (38, 16, 28, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (38, 23, 29, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (39, 17, 31, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (39, 23, 29, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (39, 29, 30, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (40, 17, 31, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (40, 18, 33, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (40, 22, 32, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (40, 29, 30, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (41, 17, 31, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (41, 22, 32, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (42, 18, 33, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (42, 21, 34, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (42, 22, 32, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (43, 18, 33, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (43, 19, 36, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (43, 20, 35, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (43, 21, 34, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (44, 20, 35, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (44, 21, 34, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (45, 19, 36, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (45, 20, 35, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (45, 26, 37, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (46, 19, 36, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (46, 24, 39, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (46, 26, 37, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (46, 27, 38, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (47, 26, 37, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (47, 27, 38, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (48, 24, 39, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (48, 25, 40, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (48, 27, 38, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (49, 24, 39, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (49, 25, 40, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (49, 28, 42, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (49, 30, 41, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (50, 25, 40, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (50, 30, 41, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (51, 16, 43, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (51, 28, 42, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (51, 30, 41, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (52, 16, 43, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (52, 23, 44, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (52, 28, 42, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (52, 29, 45, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (53, 16, 43, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (53, 23, 44, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (54, 17, 46, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (54, 23, 44, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (54, 29, 45, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (55, 17, 46, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (55, 18, 48, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (55, 22, 47, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (55, 29, 45, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (56, 17, 46, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (56, 22, 47, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (57, 18, 48, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (57, 21, 49, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (57, 22, 47, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (58, 18, 48, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (58, 19, 11, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (58, 20, 10, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (58, 21, 49, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (59, 20, 10, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (59, 21, 49, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (60, 19, 11, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (60, 20, 10, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (60, 26, 12, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (61, 19, 11, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (61, 24, 14, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (61, 26, 12, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (61, 27, 13, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (62, 26, 12, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (62, 27, 13, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (63, 24, 14, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (63, 25, 15, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (63, 27, 13, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (64, 24, 14, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (64, 25, 15, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (64, 28, 17, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (64, 30, 16, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (65, 25, 15, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (65, 30, 16, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (66, 16, 18, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (66, 28, 17, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (66, 30, 16, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (67, 16, 18, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (67, 23, 19, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (67, 28, 17, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (67, 29, 20, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (68, 16, 18, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (68, 23, 19, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (69, 17, 21, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (69, 23, 19, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (69, 29, 20, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (70, 17, 21, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (70, 18, 23, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (70, 22, 22, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;
INSERT INTO user_tag_profile (user_id, tag_id, weight, updated_at) VALUES (70, 29, 20, '2026-11-04 16:01:18.637528') ON CONFLICT DO NOTHING;

COMMIT;
