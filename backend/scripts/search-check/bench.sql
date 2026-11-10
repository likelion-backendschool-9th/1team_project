-- 검색 쿼리 속도 비교용 벤치마크. 단계마다 같은 데이터·같은 조건으로 다시 돌려 비교한다.
-- 임시 테이블(bench_posts)에 글 10만 건을 고정 seed로 만들고, 세션이 끝나면 사라진다. 실제 테이블은 건드리지 않는다.
-- 실행: docker exec -i dipsource-db-local sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < bench.sql
-- 결과는 쿼리별로 7번 실행한 시간의 중앙값(ms)이다. 첫 실행은 캐시 준비용으로 버린다.

\set ON_ERROR_STOP on
SELECT setseed(0.42);

CREATE TEMP TABLE bench_posts AS
WITH words AS (
    SELECT ARRAY['spring', 'boot', 'jpa', 'redis', 'kafka', 'docker', 'react', 'next.js', 'java', 'javascript',
                 'typescript', 'python', 'unity', 'websocket', 'aws', 'kubernetes', 'elasticsearch', 'security',
                 '스프링', '리팩토링', '캐시', '성능', '배포', '회고', '입문', '정리', '실전', '트러블슈팅',
                 '설계', '테스트', '검색', '인덱스', '쿼리', '동시성', '트랜잭션', '로그인', '채팅', '게임'] AS w
)
SELECT g AS id,
       -- 하위 쿼리 안의 s를 참조해야 단어마다 random()이 새로 계산된다
       (SELECT string_agg(w[1 + floor(random() * array_length(w, 1))::int + 0 * s], ' ')
        FROM generate_series(1, 3 + (g % 4)) AS s) AS title,
       (SELECT string_agg(w[1 + floor(random() * array_length(w, 1))::int + 0 * s], ' ')
        FROM generate_series(1, 40 + (g % 20)) AS s) AS content,
       random() < 0.05 AS is_paid,
       CASE WHEN random() < 0.98 THEN 'ACTIVE' ELSE 'HIDDEN' END AS status,
       CAST(NULL AS timestamp) AS deleted_at,
       timestamp '2026-01-01' + g * interval '1 minute' AS created_at
FROM words, generate_series(1, 100000) AS g;

ANALYZE bench_posts;

CREATE TEMP TABLE bench_result (name text, median_ms numeric, rows bigint);

CREATE FUNCTION pg_temp.bench(name text, sql text) RETURNS void AS $$
DECLARE
    started timestamptz;
    times numeric[] := '{}';
    n bigint;
BEGIN
    EXECUTE 'SELECT count(*) FROM (' || sql || ') s' INTO n;
    FOR i IN 1..7 LOOP
        started := clock_timestamp();
        EXECUTE 'SELECT count(*) FROM (' || sql || ') s' INTO n;
        times := times || (extract(epoch FROM clock_timestamp() - started) * 1000)::numeric;
    END LOOP;
    INSERT INTO bench_result
    VALUES (name, round((SELECT percentile_cont(0.5) WITHIN GROUP (ORDER BY t) FROM unnest(times) t)::numeric, 2), n);
END;
$$ LANGUAGE plpgsql;

-- 1단계: 검색어 전체를 한 덩어리로 LOWER() + LIKE, 최신순
CREATE FUNCTION pg_temp.stage1(keyword text, page_limit int) RETURNS text AS $$
    SELECT format($q$
        SELECT p.id FROM bench_posts p
        WHERE p.status = 'ACTIVE' AND p.deleted_at IS NULL
          AND (lower(p.title) LIKE %1$L OR (NOT p.is_paid AND lower(p.content) LIKE %1$L))
        ORDER BY p.created_at DESC, p.id DESC %2$s
    $q$, '%' || keyword || '%', CASE WHEN page_limit IS NULL THEN '' ELSE 'LIMIT ' || page_limit END);
$$ LANGUAGE sql;

-- 2단계: 단어별 LIKE + 관련도 점수 (PostSearchRepository와 같은 SQL)
CREATE FUNCTION pg_temp.stage2(keywords text[], match_all boolean, relevance boolean, page_limit int) RETURNS text AS $$
    SELECT format($q$
        SELECT p.id FROM bench_posts p
        CROSS JOIN LATERAL (
            SELECT count(*) FILTER (WHERE lower(p.title) LIKE t.k
                                       OR (NOT p.is_paid AND lower(p.content) LIKE t.k)) AS matched,
                   count(*) FILTER (WHERE lower(p.title) LIKE t.k) AS title_hits,
                   count(*) FILTER (WHERE NOT p.is_paid AND lower(p.content) LIKE t.k) AS body_hits
            FROM unnest(%1$L::text[]) AS t(k)
        ) m
        WHERE p.status = 'ACTIVE' AND p.deleted_at IS NULL
          AND m.matched >= CASE WHEN %2$L THEN cardinality(%1$L::text[]) ELSE 1 END
        ORDER BY CASE WHEN %3$L THEN
                     CASE WHEN lower(p.title) = %4$L THEN 100 ELSE 0 END
                   + CASE WHEN lower(p.title) LIKE %5$L THEN 30 ELSE 0 END
                   + m.title_hits * 10 + m.body_hits * 3
                 END DESC,
                 p.created_at DESC, p.id DESC %6$s
    $q$,
    (SELECT array_agg('%' || k || '%') FROM unnest(keywords) k), match_all, relevance,
    array_to_string(keywords, ' '), '%' || array_to_string(keywords, ' ') || '%',
    CASE WHEN page_limit IS NULL THEN '' ELSE 'LIMIT ' || page_limit END);
$$ LANGUAGE sql;

-- 첫 페이지(20건) 조회
SELECT pg_temp.bench('1단계 spring 최신순', pg_temp.stage1('spring', 20));
SELECT pg_temp.bench('2단계 spring 최신순', pg_temp.stage2('{spring}', true, false, 20));
SELECT pg_temp.bench('2단계 spring 관련도순', pg_temp.stage2('{spring}', true, true, 20));
SELECT pg_temp.bench('1단계 "spring jpa" 최신순', pg_temp.stage1('spring jpa', 20));
SELECT pg_temp.bench('2단계 spring jpa and 관련도순', pg_temp.stage2('{spring,jpa}', true, true, 20));
SELECT pg_temp.bench('2단계 spring jpa or 관련도순', pg_temp.stage2('{spring,jpa}', false, true, 20));
SELECT pg_temp.bench('2단계 단어 5개 and 관련도순', pg_temp.stage2('{spring,jpa,redis,kafka,docker}', true, true, 20));
-- 총개수(count) 조회: 페이지 응답마다 한 번씩 더 실행된다
SELECT pg_temp.bench('1단계 spring 전체 일치', pg_temp.stage1('spring', NULL));
SELECT pg_temp.bench('2단계 spring 전체 일치', pg_temp.stage2('{spring}', true, false, NULL));
SELECT pg_temp.bench('2단계 spring jpa or 전체 일치', pg_temp.stage2('{spring,jpa}', false, false, NULL));


-- 3단계: 오타 교정 후보 (PostSearchRepository#findClosestTitleTerm과 같은 SQL)
-- 결과가 0건(교정)이거나 1~4건(제안)일 때만, 검색 단어마다 태그·글 제목·데모 제목에서 한 번씩 실행된다. 여기서는 글 제목만 잰다
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;

CREATE FUNCTION pg_temp.stage3(word text, max_distance int) RETURNS text AS $$
    SELECT format($q$
        SELECT w.term AS term,
               w.term LIKE %2$L AS contained,
               levenshtein(%1$L, w.term) AS distance,
               w.frequency AS frequency,
               CAST(similarity(%1$L, w.term) AS double precision) AS similarity
        FROM (
            SELECT s.term, count(*) AS frequency
            FROM bench_posts p
            CROSS JOIN LATERAL regexp_split_to_table(lower(p.title), '[^a-z0-9가-힣_.#+-]+') AS s(term)
            WHERE p.status = 'ACTIVE' AND p.deleted_at IS NULL
            GROUP BY s.term
        ) w
        WHERE length(w.term) BETWEEN 1 AND 50
          AND (w.term LIKE %2$L
               OR (abs(length(w.term) - length(%1$L)) <= %3$s
                   AND levenshtein_less_equal(%1$L, w.term, %3$s) <= %3$s))
        ORDER BY contained DESC, distance, frequency DESC, similarity DESC, term
        LIMIT 2
    $q$, word, '%' || word || '%', max_distance);
$$ LANGUAGE sql;

SELECT pg_temp.bench('2단계 sprng 관련도순 (0건, 교정 전 첫 검색)', pg_temp.stage2('{sprng}', true, true, 20));
SELECT pg_temp.bench('3단계 교정 후보 sprng (5글자, 거리 2)', pg_temp.stage3('sprng', 2));
SELECT pg_temp.bench('3단계 교정 후보 kafak (5글자, 거리 2)', pg_temp.stage3('kafak', 2));
SELECT pg_temp.bench('3단계 교정 후보 캐쉬 (2글자라 실제로는 교정 안 함)', pg_temp.stage3('캐쉬', 1));

-- 4단계: 자동완성 단어 후보 (PostSearchRepository#findTitleTermsByPrefix와 같은 SQL)
-- 키를 누를 때마다(debounce 후) 태그 이름·태그 단어·글 제목·데모 제목에서 한 번씩 실행된다. 여기서는 글 제목만 잰다
CREATE FUNCTION pg_temp.stage4(pattern text, composing boolean, stem_pattern text, stem_length int,
                               next_from text, next_to text) RETURNS text AS $$
    SELECT format($q$
        SELECT s.term AS term, count(*) AS frequency
        FROM bench_posts p
        CROSS JOIN LATERAL regexp_split_to_table(lower(p.title), '[^a-z0-9가-힣_.#+-]+') AS s(term)
        WHERE length(s.term) BETWEEN 1 AND 50
          AND p.status = 'ACTIVE'
          AND p.deleted_at IS NULL
          AND (s.term LIKE %1$L
               OR (%2$L
                   AND s.term LIKE %3$L
                   AND substr(s.term, %4$s + 1, 1) COLLATE "C" BETWEEN %5$L AND %6$L))
        GROUP BY s.term
        ORDER BY frequency DESC, length(s.term), s.term
        LIMIT 8
    $q$, pattern, composing, stem_pattern, stem_length, next_from, next_to);
$$ LANGUAGE sql;

SELECT pg_temp.bench('4단계 자동완성 spr', pg_temp.stage4('spr%', false, 'spr%', 0, '', ''));
SELECT pg_temp.bench('4단계 자동완성 s (한 글자)', pg_temp.stage4('s%', false, 's%', 0, '', ''));
SELECT pg_temp.bench('4단계 자동완성 스플 (조합 중, 스프 + ㄹ)', pg_temp.stage4('스플%', true, '스프%', 2, '라', '맇'));
SELECT pg_temp.bench('4단계 자동완성 zz (맞는 단어 없음)', pg_temp.stage4('zz%', false, 'zz%', 0, '', ''));

SELECT name AS "쿼리", median_ms AS "중앙값(ms)", rows AS "결과 건수" FROM bench_result;
