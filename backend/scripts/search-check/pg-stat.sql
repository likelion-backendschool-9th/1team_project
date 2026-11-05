-- 부하 테스트 동안 시간을 가장 많이 쓴 쿼리 순위 (pg_stat_statements). 로컬 DB에서만 쓴다.
-- 준비(한 번): ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements'; → DB 재시작 → 이 파일 실행
-- 측정 전 초기화: SELECT pg_stat_statements_reset();
\set ON_ERROR_STOP on
SET client_min_messages = warning;
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
SELECT calls,
       round(total_exec_time::numeric, 1) AS total_ms,
       round(mean_exec_time::numeric, 2) AS mean_ms,
       round(max_exec_time::numeric, 2) AS max_ms,
       round((100 * total_exec_time / sum(total_exec_time) OVER ())::numeric, 1) AS pct,
       left(regexp_replace(query, '\s+', ' ', 'g'), 110) AS query
FROM pg_stat_statements
WHERE dbid = (SELECT oid FROM pg_database WHERE datname = current_database())
  AND query !~* 'pg_stat_statements|^(begin|commit|set|show|select \$1$)'
ORDER BY total_exec_time DESC
LIMIT 12;
