-- 부하 테스트용 대량 데이터. 로컬 DB에서만 실행한다. 지우기: load-cleanup.sql
-- 글 수(posts)를 정하면 같은 비율로 만든다: 글 N, 피드 N, 데모 N/10, 작성자 N/100
-- 실행: docker exec -i dipsource-db-local sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v posts=10000' < load-seed.sql
--
-- - 모자란 만큼만 더 넣는다. 1만 → 10만 → 50만 순서로 늘려 가며 같은 DB에서 잴 수 있다.
-- - 단어는 행 번호의 해시(hashtext)로 골라서, 몇 번에 나눠 넣어도 같은 행은 같은 내용이 된다(random()을 쓰지 않음).
-- - 단어 목록은 bench.sql과 같다. 오타 단어(sprng, sprnig)는 넣지 않아서, 오타 교정·제안 경로는 기존 seed.sql 데이터로 탄다.
-- - 작성자는 email이 @load.local이다. 기존 데이터(seed.sql, 직접 넣은 더미)는 건드리지 않는다.
-- - 태그는 늘리지 않는다. 태그 수는 글 수에 비례해 늘지 않는다고 보고, 지금 있는 태그를 그대로 쓴다.

\set ON_ERROR_STOP on
\if :{?posts}
\else
  \echo 'posts 변수가 필요해요. 예: -v posts=10000'
  \quit
\endif

CREATE TEMP TABLE load_words AS
SELECT ARRAY['spring', 'boot', 'jpa', 'redis', 'kafka', 'docker', 'react', 'next.js', 'java', 'javascript',
             'typescript', 'python', 'unity', 'websocket', 'aws', 'kubernetes', 'elasticsearch', 'security',
             '스프링', '리팩토링', '캐시', '성능', '배포', '회고', '입문', '정리', '실전', '트러블슈팅',
             '설계', '테스트', '검색', '인덱스', '쿼리', '동시성', '트랜잭션', '로그인', '채팅', '게임'] AS w;

-- g번째 행의 0~99 사이 값. 상태·유료 비율을 정한다
CREATE FUNCTION pg_temp.percent(g bigint, salt text) RETURNS int AS $$
    SELECT abs(hashtext(g || ':' || salt)) % 100
$$ LANGUAGE sql IMMUTABLE;

SELECT :posts AS target_posts,
       (SELECT count(*) FROM users WHERE email LIKE '%@load.local') AS writers_before,
       (SELECT count(*) FROM posts p JOIN users u ON u.id = p.user_id WHERE u.email LIKE '%@load.local') AS posts_before
\gset

---------------------------------------------------------------- 작성자 (N/100명, 2%는 정지)
INSERT INTO users(email, nickname, bio, role, status, chip_balance, blog_subscription_price, tech_stacks,
                  created_at, updated_at, last_active_at)
SELECT 'load' || k || '@load.local', 'load_user_' || k,
       (SELECT string_agg(w[1 + abs(hashtext(k || ':bio:' || i)) % array_length(w, 1)], ' ' ORDER BY i)
        FROM generate_series(1, 5 + k % 6) AS i), 'USER',
       CASE WHEN pg_temp.percent(k, 'status') < 2 THEN 'SUSPENDED' ELSE 'ACTIVE' END, 0, 0,
       to_jsonb(string_to_array((SELECT string_agg(w[1 + abs(hashtext(k || ':stack:' || i)) % array_length(w, 1)], ' ' ORDER BY i)
        FROM generate_series(1, 3) AS i), ' ')),
       timestamp '2025-01-01' + k * interval '1 hour', timestamp '2025-01-01' + k * interval '1 hour', now()
FROM load_words, generate_series(:writers_before + 1, greatest(:target_posts / 100, 1)) AS k;

CREATE TEMP TABLE load_writers AS
SELECT array_agg(id ORDER BY id) AS ids FROM users WHERE email LIKE '%@load.local';

---------------------------------------------------------------- 블로그 글 (N, 2% 숨김, 5% 유료)
INSERT INTO posts(user_id, title, content, is_paid, price_chips, status, like_count, view_count, comment_count,
                  created_at, updated_at)
SELECT ids[1 + g % array_length(ids, 1)],
       (SELECT string_agg(w[1 + abs(hashtext(g || ':title:' || i)) % array_length(w, 1)], ' ' ORDER BY i)
        FROM generate_series(1, 3 + g % 4) AS i),
       (SELECT string_agg(w[1 + abs(hashtext(g || ':content:' || i)) % array_length(w, 1)], ' ' ORDER BY i)
        FROM generate_series(1, 40 + g % 20) AS i),
       pg_temp.percent(g, 'paid') < 5,
       CASE WHEN pg_temp.percent(g, 'paid') < 5 THEN 10 ELSE 0 END,
       CASE WHEN pg_temp.percent(g, 'status') < 2 THEN 'HIDDEN' ELSE 'ACTIVE' END,
       g % 50, g % 500, 0,
       timestamp '2025-01-01' + g * interval '1 minute', timestamp '2025-01-01' + g * interval '1 minute'
FROM load_words, load_writers, generate_series(:posts_before + 1, :target_posts) AS g;

---------------------------------------------------------------- 피드 (N, 2% 숨김)
INSERT INTO feeds(user_id, content, status, like_count, comment_count, repost_count, created_at, updated_at)
SELECT ids[1 + g % array_length(ids, 1)],
       (SELECT string_agg(w[1 + abs(hashtext(g || ':feed:' || i)) % array_length(w, 1)], ' ' ORDER BY i)
        FROM generate_series(1, 8 + g % 8) AS i),
       CASE WHEN pg_temp.percent(g, 'feed-status') < 2 THEN 'HIDDEN' ELSE 'ACTIVE' END,
       g % 30, 0, 0,
       timestamp '2025-01-01' + g * interval '1 minute', timestamp '2025-01-01' + g * interval '1 minute'
FROM load_words, load_writers,
     generate_series((SELECT count(*) FROM feeds f JOIN users u ON u.id = f.user_id WHERE u.email LIKE '%@load.local') + 1,
                     :target_posts) AS g;

---------------------------------------------------------------- 데모 (N/10, 2% 숨김)
INSERT INTO demos(user_id, title, description, demo_type, status, like_count, play_count, feedback_count, version,
                  created_at, updated_at)
SELECT ids[1 + g % array_length(ids, 1)],
       (SELECT string_agg(w[1 + abs(hashtext(g || ':demo-title:' || i)) % array_length(w, 1)], ' ' ORDER BY i)
        FROM generate_series(1, 2 + g % 3) AS i),
       (SELECT string_agg(w[1 + abs(hashtext(g || ':demo-desc:' || i)) % array_length(w, 1)], ' ' ORDER BY i)
        FROM generate_series(1, 10 + g % 10) AS i),
       'STATIC',
       CASE WHEN pg_temp.percent(g, 'demo-status') < 2 THEN 'HIDDEN' ELSE 'ACTIVE' END,
       g % 40, g % 300, 0, 0,
       timestamp '2025-01-01' + g * interval '10 minute', timestamp '2025-01-01' + g * interval '10 minute'
FROM load_words, load_writers,
     generate_series((SELECT count(*) FROM demos d JOIN users u ON u.id = d.user_id WHERE u.email LIKE '%@load.local') + 1,
                     :target_posts / 10) AS g;

ANALYZE users;
ANALYZE posts;
ANALYZE feeds;
ANALYZE demos;

SELECT (SELECT count(*) FROM users) AS users, (SELECT count(*) FROM posts) AS posts,
       (SELECT count(*) FROM feeds) AS feeds, (SELECT count(*) FROM demos) AS demos, (SELECT count(*) FROM tags) AS tags;
