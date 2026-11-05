-- 로컬 통합 테스트용 더미 데이터. CI/CD·배포 서버에서는 절대 실행하지 않는다.
-- 회원 50명(팀원 로그인용 5 + 데이터용 45), 미니홈피, 피드/블로그/데모, 댓글·대댓글·좋아요 조합.
-- 이메일: seed-team-01@dev.local ~ seed-team-05@dev.local, seed-user-06@dev.local ~ seed-user-50@dev.local
-- 지우기: cleanup.sql
do $$
declare
  uids bigint[] := array[]::bigint[];
  tag_names text[] := array[
    'Java', 'Spring Boot', 'JPA', 'React', 'TypeScript', 'Next.js',
    'Python', 'Docker', 'Kubernetes', 'AWS', 'Node.js', 'WebSocket',
    'Redis', 'PostgreSQL', 'Unity'
  ];
  stack_pool text[] := array[
    '["Java","Spring Boot","JPA"]',
    '["React","TypeScript","Next.js"]',
    '["Python","Docker","AWS"]',
    '["Node.js","TypeScript","Redis"]',
    '["Unity","C#"]',
    '["Spring Boot","PostgreSQL","Redis"]',
    '["React","WebSocket","Node.js"]',
    '["Kubernetes","Docker","AWS"]'
  ];
  i int;
  j int;
  uid bigint;
  other_id bigint;
  did bigint;
  fid bigint;
  pid bigint;
  cid bigint;
  rid bigint;
  hid bigint;
  demo_id_pick bigint;
  tag_id bigint;
  tag_name text;
  like_n int;
  demo_type text;
  poll jsonb;
  team_a bigint;
  team_b bigint;
  r_id bigint;
  last_msg bigint;
begin
  if exists (select 1 from users where email like 'seed-%@dev.local') then
    raise notice 'dev-seed 데이터가 이미 있어요. 다시 넣으려면 cleanup.sql을 먼저 실행하세요.';
    return;
  end if;

  -- 로컬에서 수동 INSERT가 섞여 identity 시퀀스가 어긋난 경우 대비
  perform setval(pg_get_serial_sequence('tags', 'id'),
    greatest((select coalesce(max(id), 0) from tags), 1),
    (select coalesce(max(id), 0) from tags) > 0);
  perform setval(pg_get_serial_sequence('users', 'id'),
    greatest((select coalesce(max(id), 0) from users), 1),
    (select coalesce(max(id), 0) from users) > 0);
  perform setval(pg_get_serial_sequence('demos', 'id'),
    greatest((select coalesce(max(id), 0) from demos), 1),
    (select coalesce(max(id), 0) from demos) > 0);
  perform setval(pg_get_serial_sequence('feeds', 'id'),
    greatest((select coalesce(max(id), 0) from feeds), 1),
    (select coalesce(max(id), 0) from feeds) > 0);
  perform setval(pg_get_serial_sequence('posts', 'id'),
    greatest((select coalesce(max(id), 0) from posts), 1),
    (select coalesce(max(id), 0) from posts) > 0);
  perform setval(pg_get_serial_sequence('mini_homes', 'id'),
    greatest((select coalesce(max(id), 0) from mini_homes), 1),
    (select coalesce(max(id), 0) from mini_homes) > 0);
  perform setval(pg_get_serial_sequence('feed_comments', 'id'),
    greatest((select coalesce(max(id), 0) from feed_comments), 1),
    (select coalesce(max(id), 0) from feed_comments) > 0);
  perform setval(pg_get_serial_sequence('post_comments', 'id'),
    greatest((select coalesce(max(id), 0) from post_comments), 1),
    (select coalesce(max(id), 0) from post_comments) > 0);
  perform setval(pg_get_serial_sequence('demo_feedbacks', 'id'),
    greatest((select coalesce(max(id), 0) from demo_feedbacks), 1),
    (select coalesce(max(id), 0) from demo_feedbacks) > 0);
  perform setval(pg_get_serial_sequence('guestbooks', 'id'),
    greatest((select coalesce(max(id), 0) from guestbooks), 1),
    (select coalesce(max(id), 0) from guestbooks) > 0);
  perform setval(pg_get_serial_sequence('dm_rooms', 'id'),
    greatest((select coalesce(max(id), 0) from dm_rooms), 1),
    (select coalesce(max(id), 0) from dm_rooms) > 0);
  perform setval(pg_get_serial_sequence('dm_messages', 'id'),
    greatest((select coalesce(max(id), 0) from dm_messages), 1),
    (select coalesce(max(id), 0) from dm_messages) > 0);
  perform setval(pg_get_serial_sequence('feed_votes', 'id'),
    greatest((select coalesce(max(id), 0) from feed_votes), 1),
    (select coalesce(max(id), 0) from feed_votes) > 0);

  ---------------------------------------------------------------- 태그 (없는 이름만 삽입)
  insert into tags(name)
  select t
  from unnest(tag_names) as t
  where not exists (select 1 from tags where name = t);

  ---------------------------------------------------------------- 회원 50
  for i in 1..50 loop
    if i <= 5 then
      insert into users(
        email, nickname, bio, role, status, chip_balance, blog_subscription_price,
        tech_stacks, github_username, created_at, updated_at, last_active_at
      ) values (
        format('seed-team-%s@dev.local', lpad(i::text, 2, '0')),
        format('시드팀%s', lpad(i::text, 2, '0')),
        format('팀원 로그인용 계정 %s. 로컬 테스트 전용.', i),
        'USER', 'ACTIVE',
        100 * i,
        case when i <= 2 then 500 else 0 end,
        stack_pool[((i - 1) % array_length(stack_pool, 1)) + 1]::jsonb,
        format('seed-team-%s', lpad(i::text, 2, '0')),
        now() - ((60 - i) || ' day')::interval,
        now(),
        now() - ((i - 1) || ' minute')::interval
      ) returning id into uid;
    else
      insert into users(
        email, nickname, bio, role, status, chip_balance, blog_subscription_price,
        tech_stacks, github_username, created_at, updated_at, last_active_at
      ) values (
        format('seed-user-%s@dev.local', lpad(i::text, 2, '0')),
        format('시드유저%s', lpad(i::text, 2, '0')),
        format('데이터용 계정 %s. 피드·블로그·데모 작성자.', i),
        'USER', 'ACTIVE',
        (i % 7) * 50,
        case when i % 10 = 0 then 300 else 0 end,
        stack_pool[((i - 1) % array_length(stack_pool, 1)) + 1]::jsonb,
        format('seed-user-%s', lpad(i::text, 2, '0')),
        now() - ((90 - i) || ' day')::interval,
        now(),
        case
          when i % 5 = 0 then now() - interval '2 hour'
          when i % 5 = 1 then now() - interval '1 day'
          else now() - ((i % 12) || ' hour')::interval
        end
      ) returning id into uid;
    end if;
    uids := array_append(uids, uid);

    -- 관심 태그 2~3개
    for j in 0..((i % 3) + 1) loop
      tag_name := tag_names[((i + j - 1) % array_length(tag_names, 1)) + 1];
      select id into tag_id from tags where name = tag_name;
      insert into user_interest_tags(user_id, tag_id, created_at)
      values (uid, tag_id, now() - interval '1 day')
      on conflict do nothing;
      insert into user_tag_profile(user_id, tag_id, weight, updated_at)
      values (uid, tag_id, 10 + ((i + j) % 40), now())
      on conflict do nothing;
    end loop;
  end loop;

  ---------------------------------------------------------------- 팔로우 (팀원끼리 + 팀원→유저 + 유저끼리 일부)
  for i in 1..5 loop
    for j in 1..5 loop
      if i <> j then
        insert into follows(follower_id, following_id, created_at)
        values (uids[i], uids[j], now() - interval '10 day')
        on conflict do nothing;
      end if;
    end loop;
    for j in 6..25 loop
      if (i + j) % 3 <> 0 then
        insert into follows(follower_id, following_id, created_at)
        values (uids[i], uids[j], now() - ((j) || ' hour')::interval)
        on conflict do nothing;
      end if;
      if (i + j) % 4 = 0 then
        insert into follows(follower_id, following_id, created_at)
        values (uids[j], uids[i], now() - ((j) || ' hour')::interval)
        on conflict do nothing;
      end if;
    end loop;
  end loop;

  for i in 6..50 loop
    for j in 1..4 loop
      other_id := uids[(((i + j * 7 - 1) % 50) + 1)];
      if other_id <> uids[i] then
        insert into follows(follower_id, following_id, created_at)
        values (uids[i], other_id, now() - interval '5 day')
        on conflict do nothing;
      end if;
    end loop;
  end loop;

  ---------------------------------------------------------------- 데모 (~30개: 짝수 번호 유저 + 팀원)
  for i in 1..50 loop
    if i > 5 and i % 2 = 1 then
      continue;
    end if;
    uid := uids[i];
    demo_type := case (i % 3)
      when 0 then 'STATIC'
      when 1 then 'API'
      else 'EXTERNAL'
    end;
    insert into demos(
      user_id, title, description, demo_type, status,
      file_path, api_url, external_url,
      like_count, play_count, feedback_count, version,
      created_at, updated_at
    ) values (
      uid,
      format('%s의 데모 #%s', case when i <= 5 then '팀' else '유저' end, i),
      format('로컬 시드 데모입니다. 작성자 인덱스 %s. 타입 %s.', i, demo_type),
      demo_type,
      'ACTIVE',
      case when demo_type = 'STATIC' then format('/seed/demos/%s/index.html', i) else null end,
      case when demo_type = 'API' then format('https://seed.example.dev/api/%s', i) else null end,
      case when demo_type = 'EXTERNAL' then format('https://seed.example.dev/ext/%s', i) else null end,
      0, (i * 11) % 200, 0, 1,
      now() - ((i * 3) || ' hour')::interval,
      now()
    ) returning id into did;

    tag_name := tag_names[((i - 1) % array_length(tag_names, 1)) + 1];
    select id into tag_id from tags where name = tag_name;
    insert into demo_tags(demo_id, tag_id) values (did, tag_id) on conflict do nothing;
    tag_name := tag_names[((i + 2) % array_length(tag_names, 1)) + 1];
    select id into tag_id from tags where name = tag_name;
    insert into demo_tags(demo_id, tag_id) values (did, tag_id) on conflict do nothing;

    -- 좋아요·북마크 (작성자 제외 일부)
    for j in 1..((i % 5) + 2) loop
      other_id := uids[(((i + j * 3 - 1) % 50) + 1)];
      if other_id = uid then
        continue;
      end if;
      insert into demo_likes(user_id, demo_id, created_at)
      values (other_id, did, now() - interval '1 hour')
      on conflict do nothing;
      if j % 2 = 0 then
        insert into bookmarks(user_id, demo_id, created_at)
        values (other_id, did, now() - interval '1 hour')
        on conflict do nothing;
      end if;
    end loop;

    -- 피드백(댓글) + 대댓글
    other_id := uids[(((i + 4 - 1) % 50) + 1)];
    if other_id <> uid then
      insert into demo_feedbacks(demo_id, user_id, parent_id, content, like_count, created_at)
      values (did, other_id, null, format('데모 #%s 잘 봤어요!', i), 0, now() - interval '50 minute')
      returning id into cid;

      insert into demo_feedbacks(demo_id, user_id, parent_id, content, like_count, created_at)
      values (did, uid, cid, '감사합니다! 피드백 반영할게요.', 0, now() - interval '40 minute')
      returning id into rid;

      insert into demo_feedback_likes(user_id, feedback_id, created_at)
      values (uids[(((i + 1 - 1) % 50) + 1)], cid, now() - interval '30 minute')
      on conflict do nothing;

      update demo_feedbacks set like_count = (
        select count(*)::int from demo_feedback_likes where feedback_id = demo_feedbacks.id
      ) where id in (cid, rid);
    end if;

    update demos d set
      like_count = (select count(*)::int from demo_likes where demo_id = d.id),
      feedback_count = (select count(*)::int from demo_feedbacks where demo_id = d.id and deleted_at is null)
    where d.id = did;
  end loop;

  ---------------------------------------------------------------- 피드 (~100개: 유저당 2개)
  for i in 1..50 loop
    uid := uids[i];
    for j in 1..2 loop
      select d.id into demo_id_pick
      from demos d
      where d.user_id = any(uids) and d.status = 'ACTIVE'
      order by d.id
      offset ((i + j) % 20) limit 1;

      poll := case when (i + j) % 7 = 0
        then '["Spring","Node","둘 다"]'::jsonb
        else null end;

      insert into feeds(
        user_id, content, demo_id, status,
        like_count, comment_count, repost_count,
        poll_options, created_at, updated_at
      ) values (
        uid,
        format('시드 피드 #%s-%s. %s 관련 짧은 글입니다. 알고리즘·타임라인 테스트용.',
               i, j, tag_names[((i + j - 1) % array_length(tag_names, 1)) + 1]),
        case when (i + j) % 4 = 0 then demo_id_pick else null end,
        'ACTIVE',
        0, 0, 0,
        poll,
        now() - (((i * 2) + j) || ' hour')::interval,
        now()
      ) returning id into fid;

      tag_name := tag_names[((i + j - 1) % array_length(tag_names, 1)) + 1];
      select id into tag_id from tags where name = tag_name;
      insert into feed_tags(feed_id, tag_id) values (fid, tag_id) on conflict do nothing;

      -- 좋아요
      like_n := ((i + j) % 6) + 1;
      for k in 1..like_n loop
        other_id := uids[(((i + j + k * 5 - 1) % 50) + 1)];
        if other_id = uid then
          continue;
        end if;
        insert into feed_likes(user_id, feed_id, created_at)
        values (other_id, fid, now() - interval '20 minute')
        on conflict do nothing;
      end loop;

      -- 북마크·리포스트 (일부)
      if (i + j) % 3 = 0 then
        other_id := uids[(((i + 11 - 1) % 50) + 1)];
        if other_id <> uid then
          insert into feed_bookmarks(user_id, feed_id, created_at)
          values (other_id, fid, now() - interval '15 minute')
          on conflict do nothing;
        end if;
      end if;
      if (i + j) % 5 = 0 then
        other_id := uids[(((i + 17 - 1) % 50) + 1)];
        if other_id <> uid then
          insert into feed_reposts(user_id, feed_id, created_at)
          values (other_id, fid, now() - interval '10 minute')
          on conflict do nothing;
        end if;
      end if;

      -- 투표
      if poll is not null then
        for k in 1..5 loop
          other_id := uids[(((i + k * 9 - 1) % 50) + 1)];
          insert into feed_votes(feed_id, user_id, option_index, created_at)
          values (fid, other_id, ((k - 1) % 3)::smallint, now() - interval '5 minute')
          on conflict do nothing;
        end loop;
      end if;

      -- 댓글 + 대댓글 (케이스 조합)
      -- 1) 댓글만  2) 댓글+대댓글  3) 댓글 여러 개 + 일부 좋아요
      other_id := uids[(((i + j + 2 - 1) % 50) + 1)];
      if other_id <> uid then
        insert into feed_comments(feed_id, user_id, parent_id, content, like_count, created_at)
        values (fid, other_id, null, format('피드 #%s-%s 댓글입니다.', i, j), 0, now() - interval '25 minute')
        returning id into cid;

        if (i + j) % 2 = 0 then
          insert into feed_comments(feed_id, user_id, parent_id, content, like_count, created_at)
          values (fid, uid, cid, '대댓글로 답장합니다.', 0, now() - interval '20 minute')
          returning id into rid;

          insert into feed_comment_likes(user_id, comment_id, created_at)
          values (uids[(((i + 3 - 1) % 50) + 1)], cid, now() - interval '18 minute')
          on conflict do nothing;
        end if;

        if (i + j) % 3 = 0 then
          other_id := uids[(((i + j + 8 - 1) % 50) + 1)];
          insert into feed_comments(feed_id, user_id, parent_id, content, like_count, created_at)
          values (fid, other_id, null, '두 번째 댓글. 추천 알고리즘 테스트용.', 0, now() - interval '12 minute');
        end if;
      end if;

      update feed_comments fc set like_count = (
        select count(*)::int from feed_comment_likes where comment_id = fc.id
      ) where fc.feed_id = fid;

      update feeds f set
        like_count = (select count(*)::int from feed_likes where feed_id = f.id),
        comment_count = (select count(*)::int from feed_comments where feed_id = f.id and deleted_at is null),
        repost_count = (select count(*)::int from feed_reposts where feed_id = f.id)
      where f.id = fid;
    end loop;
  end loop;

  ---------------------------------------------------------------- 블로그 글 (~40개)
  for i in 1..50 loop
    if i > 10 and i % 2 = 1 then
      continue;
    end if;
    uid := uids[i];
    select d.id into demo_id_pick
    from demos d where d.user_id = uid order by d.id limit 1;

    insert into posts(
      user_id, demo_id, title, content, thumbnail_url,
      is_paid, price_chips, status,
      view_count, like_count, comment_count,
      created_at, updated_at
    ) values (
      uid,
      demo_id_pick,
      format('시드 블로그: %s 회고 #%s', tag_names[((i - 1) % array_length(tag_names, 1)) + 1], i),
      format(E'# 시드 글 %s\n\n로컬 테스트용 본문입니다.\n\n- 태그: %s\n- 작성자 인덱스: %s\n\n더미 문단을 이어 붙입니다. 피드 알고리즘과 블로그 목록·상세·댓글을 확인할 때 씁니다.',
             i, tag_names[((i - 1) % array_length(tag_names, 1)) + 1], i),
      null,
      (i % 8 = 0),
      case when i % 8 = 0 then 50 else 0 end,
      'ACTIVE',
      (i * 13) % 500,
      0, 0,
      now() - ((i * 5) || ' hour')::interval,
      now()
    ) returning id into pid;

    tag_name := tag_names[((i - 1) % array_length(tag_names, 1)) + 1];
    select id into tag_id from tags where name = tag_name;
    insert into post_tags(post_id, tag_id) values (pid, tag_id) on conflict do nothing;

    for j in 1..((i % 4) + 1) loop
      other_id := uids[(((i + j * 4 - 1) % 50) + 1)];
      if other_id = uid then
        continue;
      end if;
      insert into post_likes(user_id, post_id, created_at)
      values (other_id, pid, now() - interval '1 hour')
      on conflict do nothing;
      if j % 2 = 0 then
        insert into post_bookmarks(user_id, post_id, created_at)
        values (other_id, pid, now() - interval '1 hour')
        on conflict do nothing;
      end if;
    end loop;

    other_id := uids[(((i + 6 - 1) % 50) + 1)];
    if other_id <> uid then
      insert into post_comments(post_id, user_id, parent_id, content, like_count, created_at)
      values (pid, other_id, null, format('블로그 #%s 잘 읽었습니다.', i), 0, now() - interval '40 minute')
      returning id into cid;

      if i % 2 = 0 then
        insert into post_comments(post_id, user_id, parent_id, content, like_count, created_at)
        values (pid, uid, cid, '읽어 주셔서 감사합니다!', 0, now() - interval '30 minute');

        insert into post_comment_likes(user_id, comment_id, created_at)
        values (uids[(((i + 2 - 1) % 50) + 1)], cid, now() - interval '25 minute')
        on conflict do nothing;
      end if;
    end if;

    update post_comments pc set like_count = (
      select count(*)::int from post_comment_likes where comment_id = pc.id
    ) where pc.post_id = pid;

    update posts p set
      like_count = (select count(*)::int from post_likes where post_id = p.id),
      comment_count = (select count(*)::int from post_comments where post_id = p.id and deleted_at is null)
    where p.id = pid;
  end loop;

  ---------------------------------------------------------------- 미니홈피 50 + 방명록
  for i in 1..50 loop
    uid := uids[i];
    select d.id into demo_id_pick
    from demos d where d.user_id = uid order by d.id limit 1;

    insert into mini_homes(
      user_id, title, intro, skin_theme,
      showcase_demo1_id, today_count, total_count,
      created_at, updated_at
    ) values (
      uid,
      format('%s의 미니홈피', case when i <= 5
        then format('시드팀%s', lpad(i::text, 2, '0'))
        else format('시드유저%s', lpad(i::text, 2, '0')) end),
      format('로컬 시드 미니홈피입니다. (#%s)', i),
      'default',
      demo_id_pick,
      (i % 20),
      50 + i * 3,
      now() - interval '30 day',
      now()
    ) returning id into hid;

    -- 방명록 1~2개
    other_id := uids[(((i + 9 - 1) % 50) + 1)];
    if other_id <> uid then
      insert into guestbooks(home_id, writer_id, content, is_secret, created_at)
      values (hid, other_id, format('미니홈피 #%s 놀러왔어요!', i), false, now() - interval '2 day');
    end if;
    if i % 3 = 0 then
      other_id := uids[(((i + 13 - 1) % 50) + 1)];
      if other_id <> uid then
        insert into guestbooks(home_id, writer_id, content, is_secret, created_at)
        values (hid, other_id, '비밀 방명록 테스트', true, now() - interval '1 day');
      end if;
    end if;
  end loop;

  ---------------------------------------------------------------- 팀원 DM (로그인 테스트 편의)
  team_a := uids[1];
  team_b := uids[2];
  insert into dm_rooms(created_at) values (now() - interval '2 hour') returning id into r_id;
  insert into dm_messages(room_id, sender_id, content, created_at) values
    (r_id, team_a, '시드 데이터로 DM 테스트해요.', now() - interval '90 minute'),
    (r_id, team_b, '네, 토큰으로 로그인해서 확인해볼게요.', now() - interval '80 minute');
  select max(id) into last_msg from dm_messages where room_id = r_id;
  insert into dm_room_members(room_id, user_id, joined_at, last_read_message_id) values
    (r_id, team_a, now() - interval '2 hour', last_msg),
    (r_id, team_b, now() - interval '2 hour', last_msg);
  update dm_rooms set last_message_at = now() - interval '80 minute' where id = r_id;

  team_a := uids[1];
  team_b := uids[3];
  insert into dm_rooms(created_at) values (now() - interval '1 day') returning id into r_id;
  insert into dm_messages(room_id, sender_id, content, created_at) values
    (r_id, team_b, '피드 알고리즘도 같이 볼까요?', now() - interval '20 hour');
  select max(id) into last_msg from dm_messages where room_id = r_id;
  insert into dm_room_members(room_id, user_id, joined_at, last_read_message_id) values
    (r_id, team_a, now() - interval '1 day', null),
    (r_id, team_b, now() - interval '1 day', last_msg);
  update dm_rooms set last_message_at = now() - interval '20 hour' where id = r_id;

  raise notice 'dev-seed 완료: 회원 50, 미니홈피 50, 피드/블로그/데모/댓글/좋아요/DM 삽입';
end $$;
