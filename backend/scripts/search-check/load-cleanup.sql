-- load-seed.sql로 만든 부하 테스트 데이터(작성자 email @load.local)를 지운다. 다른 데이터는 건드리지 않는다.
\set ON_ERROR_STOP on
CREATE TEMP TABLE load_writers AS SELECT id FROM users WHERE email LIKE '%@load.local';
DELETE FROM posts WHERE user_id IN (SELECT id FROM load_writers);
DELETE FROM feeds WHERE user_id IN (SELECT id FROM load_writers);
DELETE FROM demos WHERE user_id IN (SELECT id FROM load_writers);
DELETE FROM users WHERE id IN (SELECT id FROM load_writers);
VACUUM ANALYZE posts;
VACUUM ANALYZE feeds;
VACUUM ANALYZE demos;
VACUUM ANALYZE users;
SELECT (SELECT count(*) FROM users) AS users, (SELECT count(*) FROM posts) AS posts,
       (SELECT count(*) FROM feeds) AS feeds, (SELECT count(*) FROM demos) AS demos;
