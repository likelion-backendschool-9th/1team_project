-- 앱이 뜰 때마다 실행된다(spring.sql.init.mode=always). ddl-auto는 확장을 만들지 않아서 여기서 만든다.
-- pg_trgm: 3글자 조각(trigram) 유사도. fuzzystrmatch: 편집 거리(levenshtein). 둘 다 검색어 오타 교정에 쓴다.
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;
