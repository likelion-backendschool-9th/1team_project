-- DM 화면 확인용 더미 데이터. 로컬 DB에서만 실행한다. (docs/임시-목업-현황.md)
-- 회원 4명(나=딥소스 + 대화 상대 3명)과 대화방 3개를 만든다. 이미 있으면 아무것도 하지 않는다.
do $$
declare
  me bigint; cha bigint; kwak bigint; kim bigint;
  r bigint; last_me bigint;
begin
  if exists (select 1 from users where email = 'preview-me@test.local') then
    raise notice '더미 데이터가 이미 있어요.';
    return;
  end if;

  -- 차재인·곽다윗은 방금 활동(접속 중), 김승조는 2시간 전 활동
  insert into users(email, nickname, role, status, chip_balance, blog_subscription_price, created_at, updated_at, tech_stacks, last_active_at)
  values ('preview-me@test.local', '딥소스', 'USER', 'ACTIVE', 0, 0, now(), now(), '["Java","Spring"]', now()) returning id into me;
  insert into users(email, nickname, role, status, chip_balance, blog_subscription_price, created_at, updated_at, tech_stacks, last_active_at)
  values ('preview-cha@test.local', '차재인', 'USER', 'ACTIVE', 0, 0, now(), now(), '["Spring"]', now()) returning id into cha;
  insert into users(email, nickname, role, status, chip_balance, blog_subscription_price, created_at, updated_at, tech_stacks, last_active_at)
  values ('preview-kwak@test.local', '곽다윗', 'USER', 'ACTIVE', 0, 0, now(), now(), '["React"]', now()) returning id into kwak;
  insert into users(email, nickname, role, status, chip_balance, blog_subscription_price, created_at, updated_at, tech_stacks, last_active_at)
  values ('preview-kim@test.local', '김승조', 'USER', 'ACTIVE', 0, 0, now(), now(), '["Unity"]', now() - interval '2 hours') returning id into kim;

  -- 1) 차재인: 마지막 2개를 내가 안 읽음
  insert into dm_rooms(created_at) values (now() - interval '1 hour') returning id into r;
  insert into dm_messages(room_id, sender_id, content, created_at) values
   (r, cha, '안녕하세요! 피드에 올리신 채팅앱 데모 잘 봤어요.', now() - interval '50 min'),
   (r, me,  '앗 감사합니다 😊 어떤 부분이 궁금하셨어요?',         now() - interval '47 min'),
   (r, cha, '재접속할 때 메시지 유실이 없던데 어떻게 하셨나요?',   now() - interval '45 min'),
   (r, me,  '마지막으로 받은 메시지 id를 들고 다시 붙어요.',        now() - interval '41 min');
  select max(id) into last_me from dm_messages where room_id = r;
  insert into dm_messages(room_id, sender_id, content, created_at) values
   (r, cha, '오 깔끔하네요. 코드 일부 공유 가능할까요?', now() - interval '5 min'),
   (r, cha, '채팅앱 재연결 로직 코드 공유 부탁드려요!',  now() - interval '3 min');
  insert into dm_room_members(room_id, user_id, joined_at, last_read_message_id) values
   (r, me,  now() - interval '1 hour', last_me),
   (r, cha, now() - interval '1 hour', (select max(id) from dm_messages where room_id = r));
  update dm_rooms set last_message_at = now() - interval '3 min' where id = r;

  -- 2) 곽다윗: 모두 읽음, 내가 마지막에 보냄
  insert into dm_rooms(created_at) values (now() - interval '2 hour') returning id into r;
  insert into dm_messages(room_id, sender_id, content, created_at) values
   (r, kwak, '글래스모피즘 컴포넌트 제 프로젝트에 써도 될까요?', now() - interval '70 min'),
   (r, me,   '그럼요! 출처만 남겨 주세요 ㅎㅎ',                   now() - interval '40 min');
  insert into dm_room_members(room_id, user_id, joined_at, last_read_message_id) values
   (r, me,   now() - interval '2 hour', (select max(id) from dm_messages where room_id = r)),
   (r, kwak, now() - interval '2 hour', (select max(id) from dm_messages where room_id = r));
  update dm_rooms set last_message_at = now() - interval '40 min' where id = r;

  -- 3) 김승조: 1개 안 읽음, 상대는 내 메시지를 아직 안 읽음
  insert into dm_rooms(created_at) values (now() - interval '1 day') returning id into r;
  insert into dm_messages(room_id, sender_id, content, created_at) values
   (r, me,  '오목 게임 버그 제보드려요! 대각선 판정이 가끔 틀려요.', now() - interval '26 hour');
  select max(id) into last_me from dm_messages where room_id = r;
  insert into dm_messages(room_id, sender_id, content, created_at) values
   (r, kim, '제보 감사합니다 🙏 이번 주에 고칠게요!', now() - interval '3 hour');
  insert into dm_room_members(room_id, user_id, joined_at, last_read_message_id) values
   (r, me,  now() - interval '1 day', last_me),
   (r, kim, now() - interval '1 day', null);
  update dm_rooms set last_message_at = now() - interval '3 hour' where id = r;
end $$;
