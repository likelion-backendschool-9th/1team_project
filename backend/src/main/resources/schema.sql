-- 앱이 뜰 때마다 실행된다(spring.sql.init.mode=always). ddl-auto는 확장을 만들지 않아서 여기서 만든다.
-- pg_trgm: 3글자 조각(trigram) 유사도. fuzzystrmatch: 편집 거리(levenshtein). 둘 다 검색어 오타 교정에 쓴다.
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;

-- ddl-auto=update는 이미 있는 CHECK 제약을 고치지 않는다. 데모에 DRAFT 상태가 추가되어 옛 제약을 지운다(새 DB는 Hibernate가 새 값으로 만든다).
ALTER TABLE IF EXISTS demos DROP CONSTRAINT IF EXISTS demos_status_check;
ALTER TABLE IF EXISTS demo_versions DROP CONSTRAINT IF EXISTS demo_versions_status_check;
-- 회원 권한에 MASTER(최고 관리자)가 추가되어 옛 role 제약을 지운다(새 DB는 Hibernate가 새 값으로 만든다).
ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS users_role_check;
-- 칩 거래 종류에 INVENTORY_SLOT(데모 인벤토리 칸 구매)이 추가되어 옛 type 제약을 지운다.
ALTER TABLE IF EXISTS chip_transactions DROP CONSTRAINT IF EXISTS chip_transactions_type_check;
-- 시리즈 구매(SERIES_PURCHASE)·딥소스 플러스(PLUS)·알림 종류(SERIES_PURCHASE)가 추가되어 옛 제약을 지운다(새 DB는 Hibernate가 새 값으로 만든다).
ALTER TABLE IF EXISTS payments DROP CONSTRAINT IF EXISTS payments_payment_type_check;
ALTER TABLE IF EXISTS notifications DROP CONSTRAINT IF EXISTS notifications_type_check;
