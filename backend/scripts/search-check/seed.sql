-- 검색 기능 확인용 더미 데이터. 로컬 DB에서만 실행한다. 지우기: cleanup.sql
-- 회원 8명, 태그 18개, 블로그 글 35개, 피드 12개, 데모 8개를 만든다. 이미 있으면 아무것도 하지 않는다.
--
-- 일부러 섞어 둔 조건
--  - 상태: ACTIVE / HIDDEN / DELETED 글·피드·데모, 정지(SUSPENDED)·탈퇴(DELETED) 회원의 글
--  - 유료 글: 본문에만 Redis·Kafka 같은 키워드가 있는 글
--  - 관련도: 제목에 키워드가 있는 오래된 인기 글 vs 본문에 한 번 언급된 최신 글
--  - 동의어·한영: 스프링/Spring, 리액트/React, 쿠버네티스/Kubernetes
--  - 단어 경계: Java / JavaScript, 오타가 들어간 글(Spirng)
--  - 특수문자: 100%, user_id, C#, C++, Node.js
--  - 인기 지표: like_count, view_count를 글마다 다르게, 작성일은 1~40일 전으로 분산
do $$
declare
  s bigint; r bigint; d bigint; u bigint; o bigint; j bigint; x bigint; w bigint;
  writers bigint[];
begin
  if exists (select 1 from users where email like '%@search.local') then
    raise notice '검색 더미 데이터가 이미 있어요. 다시 넣으려면 cleanup.sql을 먼저 실행하세요.';
    return;
  end if;

  ---------------------------------------------------------------- 회원
  insert into users(email, nickname, bio, role, status, chip_balance, blog_subscription_price, tech_stacks, github_username, created_at, updated_at, last_active_at)
  values ('spring@search.local', '스프링장인', '10년차 백엔드. Spring과 JPA를 좋아합니다', 'USER', 'ACTIVE', 500, 100, '["Java","Spring Boot","JPA"]', 'spring-master', now() - interval '60 day', now(), now()) returning id into s;
  insert into users(email, nickname, bio, role, status, chip_balance, blog_subscription_price, tech_stacks, created_at, updated_at, last_active_at)
  values ('react@search.local', 'reactqueen', '프론트엔드 개발자. React, Next.js', 'USER', 'ACTIVE', 0, 0, '["React","TypeScript","Next.js"]', now() - interval '55 day', now(), now()) returning id into r;
  insert into users(email, nickname, bio, role, status, chip_balance, blog_subscription_price, tech_stacks, created_at, updated_at, last_active_at)
  values ('data@search.local', '데이터곰', '데이터 엔지니어. 검색 엔진에 관심 많음', 'USER', 'ACTIVE', 0, 0, '["Python","Kafka","Elasticsearch"]', now() - interval '50 day', now(), now() - interval '3 hour') returning id into d;
  insert into users(email, nickname, bio, role, status, chip_balance, blog_subscription_price, tech_stacks, created_at, updated_at, last_active_at)
  values ('unity@search.local', 'unity_maker', '인디 게임 개발자', 'USER', 'ACTIVE', 0, 0, '["Unity","C#"]', now() - interval '45 day', now(), now() - interval '2 day') returning id into u;
  insert into users(email, nickname, bio, role, status, chip_balance, blog_subscription_price, tech_stacks, created_at, updated_at, last_active_at)
  values ('devops@search.local', 'devops_lee', '쿠버네티스로 먹고삽니다', 'USER', 'ACTIVE', 0, 0, '["Docker","Kubernetes","AWS"]', now() - interval '40 day', now(), now()) returning id into o;
  insert into users(email, nickname, bio, role, status, chip_balance, blog_subscription_price, tech_stacks, created_at, updated_at, last_active_at)
  values ('js@search.local', 'JavaScripter', 'Java 말고 JavaScript 개발자입니다', 'USER', 'ACTIVE', 0, 0, '["JavaScript","Node.js"]', now() - interval '35 day', now(), now()) returning id into j;
  -- 정지된 회원: 이 사람의 글이 검색에 나와야 하는지는 정책 결정이 필요하다
  insert into users(email, nickname, bio, role, status, chip_balance, blog_subscription_price, tech_stacks, created_at, updated_at, last_active_at)
  values ('suspended@search.local', '정지된유저', 'Spring 공부 중', 'USER', 'SUSPENDED', 0, 0, '["Spring Boot"]', now() - interval '30 day', now(), now() - interval '10 day') returning id into x;
  -- 탈퇴한 회원: 닉네임은 deleted_{id}, 화면에는 "삭제된 유저"로 보인다
  insert into users(email, nickname, role, status, chip_balance, blog_subscription_price, tech_stacks, created_at, updated_at, deleted_at)
  values ('withdrawn@search.local', 'withdrawn_tmp', 'USER', 'DELETED', 0, 0, '[]', now() - interval '90 day', now(), now() - interval '5 day') returning id into w;
  update users set nickname = 'deleted_' || w where id = w;

  writers := array[s, r, d, u, o, j, x, w];

  ---------------------------------------------------------------- 태그
  insert into tags(name) values
   ('Java'), ('Spring Boot'), ('JPA'), ('React'), ('TypeScript'), ('Next.js'), ('Python'), ('Kafka'), ('Redis'),
   ('Docker'), ('Kubernetes'), ('AWS'), ('Unity'), ('C#'), ('JavaScript'), ('Node.js'), ('WebSocket'), ('Elasticsearch')
  on conflict (name) do nothing;

  insert into user_interest_tags(user_id, tag_id, created_at)
  select v.uid, t.id, now() from (values
   (s, 'Spring Boot'), (s, 'JPA'), (r, 'React'), (r, 'TypeScript'), (d, 'Kafka'), (d, 'Elasticsearch'), (o, 'Kubernetes'), (j, 'JavaScript')
  ) v(uid, tag) join tags t on t.name = v.tag;

  ---------------------------------------------------------------- 데모
  insert into demos(user_id, title, description, demo_type, status, file_path, api_url, external_url, like_count, play_count, feedback_count, version, created_at, updated_at, deleted_at) values
   (s, '실시간 채팅앱',              'WebSocket + STOMP 기반 실시간 채팅. 재연결 시 메시지 유실 없음', 'API',      'ACTIVE',  null, 'https://chat.example.dev/api', null, 42, 510, 0, 1, now() - interval '28 day', now(), null),
   (r, '마크다운 에디터',            'React 실시간 미리보기 마크다운 에디터',                          'STATIC',   'ACTIVE',  '/demos/markdown/index.html', null, null, 24, 230, 0, 1, now() - interval '22 day', now(), null),
   (u, '오목 게임',                  'Unity WebGL로 만든 2인 오목',                                    'STATIC',   'ACTIVE',  '/demos/omok/index.html', null, null, 27, 300, 0, 1, now() - interval '18 day', now(), null),
   (d, '검색어 자동완성 데모',       'Elasticsearch edge n-gram으로 만든 자동완성',                    'EXTERNAL', 'ACTIVE',  null, null, 'https://autocomplete.example.dev', 33, 410, 0, 1, now() - interval '14 day', now(), null),
   (o, 'K8s 대시보드',               '쿠버네티스 클러스터 상태를 한눈에 보는 대시보드',                'EXTERNAL', 'ACTIVE',  null, null, 'https://k8s.example.dev', 15, 120, 0, 1, now() - interval '10 day', now(), null),
   (j, 'JavaScript 테트리스',        '바닐라 JavaScript로 만든 테트리스',                              'STATIC',   'ACTIVE',  '/demos/tetris/index.html', null, null, 51, 900, 0, 1, now() - interval '6 day', now(), null),
   (s, '숨긴 데모: Spring Batch 모니터', '관리자가 숨긴 데모',                                         'STATIC',   'HIDDEN',  '/demos/batch/index.html', null, null, 3, 20, 0, 1, now() - interval '4 day', now(), null),
   (r, '삭제한 데모: 날씨 대시보드', '작성자가 삭제한 데모',                                           'STATIC',   'DELETED', '/demos/weather/index.html', null, null, 0, 5, 0, 1, now() - interval '3 day', now(), now() - interval '1 day');

  insert into demo_tags(demo_id, tag_id)
  select dm.id, t.id from demos dm join (values
   ('실시간 채팅앱', 'Spring Boot'), ('실시간 채팅앱', 'WebSocket'), ('마크다운 에디터', 'React'), ('마크다운 에디터', 'TypeScript'),
   ('오목 게임', 'Unity'), ('오목 게임', 'C#'), ('검색어 자동완성 데모', 'Elasticsearch'), ('검색어 자동완성 데모', 'Python'),
   ('K8s 대시보드', 'Kubernetes'), ('K8s 대시보드', 'Docker'), ('JavaScript 테트리스', 'JavaScript')
  ) v(title, tag) on dm.title = v.title join tags t on t.name = v.tag
  where dm.user_id = any(writers);

  ---------------------------------------------------------------- 블로그 글
  insert into posts(user_id, title, content, is_paid, price_chips, status, view_count, like_count, comment_count, created_at, updated_at, deleted_at, category) values
   -- 스프링장인
   (s, 'Spring Boot 3에서 JPA N+1 문제 해결하기',
       E'## 문제\n연관관계를 지연 로딩으로 두고 목록을 조회하면 쿼리가 N+1번 나간다.\n\n```java\n@Query("select p from Post p join fetch p.user")\nList<Post> findAllWithUser();\n```\n\n## 해결\nfetch join, @EntityGraph, default_batch_fetch_size 세 가지를 비교했다.',
       false, 0, 'ACTIVE', 3400, 120, 18, now() - interval '30 day', now(), null, 'BACKEND'),
   (s, 'Spring Security + JWT 로그인 구현기', 'SecurityFilterChain에 JWT 필터를 끼우고 Access/Refresh 토큰을 쿠키로 내려주는 구조를 정리했다.', false, 0, 'ACTIVE', 2100, 85, 9, now() - interval '25 day', now(), null, 'BACKEND'),
   (s, '스프링 트랜잭션 전파 속성 정리', '@Transactional의 REQUIRED, REQUIRES_NEW, NESTED 차이를 예제로 정리했다. 내부 호출에서는 프록시를 타지 않는다는 점에 주의.', false, 0, 'ACTIVE', 900, 40, 3, now() - interval '20 day', now(), null, 'BACKEND'),
   (s, '[유료] 대용량 트래픽을 견디는 Spring 아키텍처', '[유료 본문] Redis 분산 락과 Kafka 이벤트 소싱으로 초당 1만 건 주문을 처리한 실전 노하우.', true, 50, 'ACTIVE', 1500, 200, 22, now() - interval '15 day', now(), null, 'BACKEND'),
   (s, 'JPA 영속성 컨텍스트 완벽 이해',
       E'영속성 컨텍스트는 엔티티를 영구 저장하는 환경이다. 1차 캐시, 동일성 보장, 쓰기 지연, 변경 감지, 지연 로딩이라는 다섯 가지 이점이 있다.\n\n'
       || repeat('플러시는 영속성 컨텍스트의 변경 내용을 데이터베이스에 반영하는 것이다. 트랜잭션 커밋 시점에 자동으로 일어난다. ', 6),
       false, 0, 'ACTIVE', 1200, 66, 5, now() - interval '12 day', now(), null, 'BACKEND'),
   (s, 'Redis 캐시 전략 총정리', 'Cache-Aside, Write-Through, Write-Behind 패턴과 TTL, 캐시 무효화 전략을 비교했다.', false, 0, 'ACTIVE', 4100, 150, 20, now() - interval '9 day', now(), null, 'BACKEND'),
   (s, 'spring boot 시작하기', '스프링 부트로 첫 프로젝트를 만들 때 알아두면 좋은 것들.', false, 0, 'ACTIVE', 80, 5, 0, now() - interval '3 day', now(), null, 'BACKEND'),
   (s, '테스트 커버리지 100% 달성기', '커버리지 숫자보다 중요한 것은 무엇을 테스트하느냐다.', false, 0, 'ACTIVE', 300, 12, 1, now() - interval '8 day', now(), null, 'BACKEND'),
   (s, '하루 1000건 배포 자동화', 'CI 파이프라인 캐시와 병렬화로 배포 시간을 줄였다.', false, 0, 'ACTIVE', 250, 9, 0, now() - interval '7 day', now(), null, 'BACKEND'),
   (s, 'user_id 컬럼 인덱스 설계', '복합 인덱스 컬럼 순서는 카디널리티와 쿼리 패턴으로 정한다.', false, 0, 'ACTIVE', 180, 7, 0, now() - interval '6 day', now(), null, 'BACKEND'),
   (s, '실시간 채팅앱 제작기: STOMP + WebSocket', '데모로 올린 채팅앱을 만들면서 겪은 재연결, 메시지 순서 보장 문제를 정리했다.', false, 0, 'ACTIVE', 700, 33, 4, now() - interval '27 day', now(), null, 'BACKEND'),
   -- reactqueen
   (r, 'React 19 use()로 로딩 분기 줄이기', 'Suspense와 서버 컴포넌트로 데이터 패칭 코드를 단순화했다.', false, 0, 'ACTIVE', 1900, 95, 11, now() - interval '24 day', now(), null, 'FRONTEND'),
   (r, 'Next.js App Router 캐싱 전략', 'fetch 캐시, revalidate, 동적 렌더링이 언제 일어나는지 정리.', false, 0, 'ACTIVE', 1300, 70, 6, now() - interval '19 day', now(), null, 'FRONTEND'),
   (r, 'TypeScript 제네릭 실전 패턴', '제네릭 제약, 조건부 타입, infer를 실무 예제로.', false, 0, 'ACTIVE', 800, 44, 2, now() - interval '13 day', now(), null, 'FRONTEND'),
   (r, '리액트 상태 관리 라이브러리 비교', 'Zustand, Redux Toolkit, Jotai를 번들 크기와 사용성으로 비교했다.', false, 0, 'ACTIVE', 1000, 52, 7, now() - interval '5 day', now(), null, 'FRONTEND'),
   (r, '숨긴 글: React 비밀 팁', '관리자가 숨긴 글', false, 0, 'HIDDEN', 10, 0, 0, now() - interval '4 day', now(), null, 'FRONTEND'),
   -- 데이터곰
   (d, 'Kafka 컨슈머 랙 줄이기', '파티션 수, max.poll.records, 컨슈머 그룹 리밸런싱을 조정했다.', false, 0, 'ACTIVE', 1100, 60, 5, now() - interval '23 day', now(), null, 'BACKEND'),
   (d, 'Python으로 Elasticsearch 검색 엔진 만들기', 'BM25 점수, nori 형태소 분석기, 동의어 사전을 설정했다.', false, 0, 'ACTIVE', 1600, 78, 8, now() - interval '17 day', now(), null, 'AI'),
   (d, 'PostgreSQL 전문 검색(Full-Text Search) 입문', 'tsvector, tsquery, ts_rank와 GIN 인덱스로 LIKE 검색을 대체해 봤다.', false, 0, 'ACTIVE', 950, 49, 3, now() - interval '11 day', now(), null, 'AI'),
   (d, '검색 엔진의 오타 교정 원리', '레벤슈타인 거리와 n-gram으로 Sprng를 Spring으로 고치는 방법.', false, 0, 'ACTIVE', 600, 31, 2, now() - interval '2 day', now(), null, 'AI'),
   (d, '삭제한 글: Kafka 옛날 정리', '작성자가 삭제한 글', false, 0, 'DELETED', 40, 1, 0, now() - interval '38 day', now(), now() - interval '20 day', 'BACKEND'),
   -- unity_maker
   (u, 'Unity WebGL 빌드 용량 38MB → 9MB', 'Brotli 압축, ASTC 텍스처, 패키지 정리 체크리스트.', false, 0, 'ACTIVE', 720, 38, 4, now() - interval '21 day', now(), null, 'GAME'),
   (u, 'C# 비동기 프로그래밍과 Unity 코루틴', 'async/await와 코루틴을 언제 써야 하는지 비교했다.', false, 0, 'ACTIVE', 400, 21, 1, now() - interval '16 day', now(), null, 'GAME'),
   (u, 'Spirng 설정 파일 정리', '제목에 오타가 있는 글. application.yml 프로필 분리 방법.', false, 0, 'ACTIVE', 50, 2, 0, now() - interval '14 day', now(), null, 'BACKEND'),
   -- devops_lee
   (o, 'Docker Compose로 로컬 개발 환경 구축', 'PostgreSQL, Redis, 백엔드를 한 번에 띄우는 compose 파일.', false, 0, 'ACTIVE', 1400, 64, 6, now() - interval '26 day', now(), null, 'BACKEND'),
   (o, '쿠버네티스 입문: Pod와 Deployment', '파드, 레플리카셋, 디플로이먼트의 관계를 그림으로 설명.', false, 0, 'ACTIVE', 1700, 80, 9, now() - interval '18 day', now(), null, 'BACKEND'),
   (o, 'Kubernetes HPA 오토스케일링 설정', 'CPU 사용률 기준 HPA 설정과 metrics-server 설치.', false, 0, 'ACTIVE', 650, 29, 2, now() - interval '10 day', now(), null, 'BACKEND'),
   (o, '[유료] AWS 비용 30% 절감 노하우', '[유료 본문] Spot 인스턴스와 Savings Plans 조합, Redis 노드 다운사이징 사례.', true, 30, 'ACTIVE', 500, 45, 3, now() - interval '8 day', now(), null, 'BACKEND'),
   (o, '오늘의 회고', '점심 먹고 redis 잠깐 봤다. Spring 스터디도 했다.', false, 0, 'ACTIVE', 30, 2, 0, now() - interval '1 day', now(), null, 'CAREER'),
   -- JavaScripter
   (j, 'JavaScript 클로저와 스코프 완벽 정리', '렉시컬 스코프와 클로저로 private 변수를 흉내 내는 방법.', false, 0, 'ACTIVE', 2300, 110, 15, now() - interval '29 day', now(), null, 'FRONTEND'),
   (j, 'Node.js 이벤트 루프 동작 원리', 'libuv, 태스크 큐, 마이크로태스크 큐의 실행 순서.', false, 0, 'ACTIVE', 1250, 58, 4, now() - interval '15 day', now(), null, 'BACKEND'),
   (j, 'Java와 JavaScript는 뭐가 다를까?', '이름만 비슷하고 전혀 다른 언어다. 타입 시스템과 런타임 비교.', false, 0, 'ACTIVE', 3000, 99, 25, now() - interval '9 day', now(), null, 'FRONTEND'),
   (j, 'C++ vs Rust 메모리 관리', '소유권과 RAII 비교.', false, 0, 'ACTIVE', 420, 19, 1, now() - interval '4 day', now(), null, 'BACKEND'),
   -- 정지·탈퇴 회원의 공개 글
   (x, 'Spring Batch 대용량 처리', '정지된 회원이 쓴 공개 글. chunk 지향 처리와 재시작.', false, 0, 'ACTIVE', 100, 3, 0, now() - interval '13 day', now(), null, 'BACKEND'),
   (w, 'Spring Cloud Gateway 사용기', '탈퇴한 회원이 쓴 공개 글. 라우팅과 필터 설정.', false, 0, 'ACTIVE', 200, 8, 1, now() - interval '40 day', now(), null, 'BACKEND');

  update posts set demo_id = (select id from demos where user_id = s and title = '실시간 채팅앱')
  where user_id = s and title = '실시간 채팅앱 제작기: STOMP + WebSocket';

  insert into post_tags(post_id, tag_id)
  select p.id, t.id from posts p join (values
   ('Spring Boot 3에서 JPA N+1 문제 해결하기', 'Spring Boot'), ('Spring Boot 3에서 JPA N+1 문제 해결하기', 'JPA'),
   ('Spring Security + JWT 로그인 구현기', 'Spring Boot'), ('스프링 트랜잭션 전파 속성 정리', 'Spring Boot'),
   ('[유료] 대용량 트래픽을 견디는 Spring 아키텍처', 'Redis'), ('[유료] 대용량 트래픽을 견디는 Spring 아키텍처', 'Kafka'),
   ('JPA 영속성 컨텍스트 완벽 이해', 'JPA'), ('Redis 캐시 전략 총정리', 'Redis'), ('spring boot 시작하기', 'Spring Boot'),
   ('실시간 채팅앱 제작기: STOMP + WebSocket', 'WebSocket'),
   ('React 19 use()로 로딩 분기 줄이기', 'React'), ('Next.js App Router 캐싱 전략', 'Next.js'), ('TypeScript 제네릭 실전 패턴', 'TypeScript'),
   ('리액트 상태 관리 라이브러리 비교', 'React'), ('Kafka 컨슈머 랙 줄이기', 'Kafka'),
   ('Python으로 Elasticsearch 검색 엔진 만들기', 'Elasticsearch'), ('Python으로 Elasticsearch 검색 엔진 만들기', 'Python'),
   ('Unity WebGL 빌드 용량 38MB → 9MB', 'Unity'), ('C# 비동기 프로그래밍과 Unity 코루틴', 'C#'),
   ('Docker Compose로 로컬 개발 환경 구축', 'Docker'), ('쿠버네티스 입문: Pod와 Deployment', 'Kubernetes'),
   ('Kubernetes HPA 오토스케일링 설정', 'Kubernetes'), ('[유료] AWS 비용 30% 절감 노하우', 'AWS'),
   ('JavaScript 클로저와 스코프 완벽 정리', 'JavaScript'), ('Node.js 이벤트 루프 동작 원리', 'Node.js'),
   ('Java와 JavaScript는 뭐가 다를까?', 'Java'), ('Java와 JavaScript는 뭐가 다를까?', 'JavaScript')
  ) v(title, tag) on p.title = v.title join tags t on t.name = v.tag
  where p.user_id = any(writers);

  ---------------------------------------------------------------- 피드
  insert into feeds(user_id, content, poll_options, demo_id, status, like_count, comment_count, repost_count, created_at, updated_at, deleted_at) values
   (s, 'Spring Boot 3.4 업데이트 정리했어요. 가상 스레드 설정이 정말 편해졌네요', null, null, 'ACTIVE', 34, 5, 3, now() - interval '6 day', now(), null),
   (s, 'JPA 지연 로딩 때문에 LazyInitializationException 만났다… open-in-view 끄면 꼭 만나는 녀석', null, null, 'ACTIVE', 12, 4, 0, now() - interval '2 day', now(), null),
   (s, '오늘은 redis 공부', null, null, 'ACTIVE', 1, 0, 0, now() - interval '5 hour', now(), null),
   (r, 'React 19 정식 출시! use() 훅 써보신 분?', '["써봤어요","아직이요"]', null, 'ACTIVE', 41, 8, 6, now() - interval '5 day', now(), null),
   (d, 'Kafka vs RabbitMQ 뭐 쓰세요?', '["Kafka","RabbitMQ","Redis Stream"]', null, 'ACTIVE', 27, 12, 2, now() - interval '4 day', now(), null),
   (u, '오목 게임 데모 올렸어요, 찍먹해보세요', null, (select id from demos where user_id = u and title = '오목 게임'), 'ACTIVE', 19, 3, 4, now() - interval '3 day', now(), null),
   (o, '쿠버네티스 자격증(CKA) 합격했습니다 🎉', null, null, 'ACTIVE', 55, 14, 1, now() - interval '1 day', now(), null),
   (o, 'Docker 이미지 용량 줄이는 팁: multi-stage build 쓰세요', null, null, 'ACTIVE', 22, 2, 5, now() - interval '7 day', now(), null),
   (j, 'JavaScript의 this는 왜 이렇게 헷갈릴까', null, null, 'ACTIVE', 30, 9, 0, now() - interval '12 hour', now(), null),
   (r, '숨긴 피드: React 비밀', null, null, 'HIDDEN', 0, 0, 0, now() - interval '2 day', now(), null),
   (d, '삭제한 피드: Kafka 옛날 얘기', null, null, 'DELETED', 0, 0, 0, now() - interval '9 day', now(), now() - interval '8 day'),
   (x, 'Spring 공부 중입니다 (정지된 회원의 피드)', null, null, 'ACTIVE', 0, 0, 0, now() - interval '11 day', now(), null);

  insert into feed_tags(feed_id, tag_id)
  select f.id, t.id from feeds f join (values
   ('Spring Boot 3.4 업데이트 정리했어요. 가상 스레드 설정이 정말 편해졌네요', 'Spring Boot'),
   ('JPA 지연 로딩 때문에 LazyInitializationException 만났다… open-in-view 끄면 꼭 만나는 녀석', 'JPA'),
   ('오늘은 redis 공부', 'Redis'),
   ('React 19 정식 출시! use() 훅 써보신 분?', 'React'),
   ('Kafka vs RabbitMQ 뭐 쓰세요?', 'Kafka'),
   ('오목 게임 데모 올렸어요, 찍먹해보세요', 'Unity'),
   ('쿠버네티스 자격증(CKA) 합격했습니다 🎉', 'Kubernetes'),
   ('Docker 이미지 용량 줄이는 팁: multi-stage build 쓰세요', 'Docker'),
   ('JavaScript의 this는 왜 이렇게 헷갈릴까', 'JavaScript')
  ) v(content, tag) on f.content = v.content join tags t on t.name = v.tag
  where f.user_id = any(writers);

  raise notice '검색 더미 데이터를 넣었어요.';
end $$;
