-- seed.sql로 넣은 로컬 더미 데이터를 모두 지운다. 배포 DB에서는 실행하지 않는다.
do $$
declare
  seed_users bigint[];
  seed_feeds bigint[];
  seed_posts bigint[];
  seed_demos bigint[];
  seed_homes bigint[];
  seed_rooms bigint[];
begin
  select array_agg(id) into seed_users from users where email like 'seed-%@dev.local';
  if seed_users is null then
    raise notice '지울 dev-seed 데이터가 없어요.';
    return;
  end if;

  select array_agg(id) into seed_feeds from feeds where user_id = any(seed_users);
  select array_agg(id) into seed_posts from posts where user_id = any(seed_users);
  select array_agg(id) into seed_demos from demos where user_id = any(seed_users);
  select array_agg(id) into seed_homes from mini_homes where user_id = any(seed_users);
  select array_agg(distinct m.room_id) into seed_rooms
  from dm_room_members m where m.user_id = any(seed_users);

  -- DM
  if seed_rooms is not null then
    delete from dm_messages where room_id = any(seed_rooms);
    delete from dm_room_members where room_id = any(seed_rooms);
    delete from dm_rooms where id = any(seed_rooms);
  end if;

  -- 피드 상호작용
  if seed_feeds is not null then
    delete from feed_comment_likes
    where comment_id in (select id from feed_comments where feed_id = any(seed_feeds));
    delete from feed_comments where parent_id is not null and feed_id = any(seed_feeds);
    delete from feed_comments where feed_id = any(seed_feeds);
    delete from feed_likes where feed_id = any(seed_feeds);
    delete from feed_bookmarks where feed_id = any(seed_feeds);
    delete from feed_reposts where feed_id = any(seed_feeds);
    delete from feed_votes where feed_id = any(seed_feeds);
    delete from feed_tags where feed_id = any(seed_feeds);
    delete from feeds where id = any(seed_feeds);
  end if;

  -- 시드 회원이 남긴 타 콘텐츠 좋아요 등 (없을 수 있음)
  delete from feed_comment_likes where user_id = any(seed_users);
  delete from feed_likes where user_id = any(seed_users);
  delete from feed_bookmarks where user_id = any(seed_users);
  delete from feed_reposts where user_id = any(seed_users);
  delete from feed_votes where user_id = any(seed_users);

  -- 블로그
  if seed_posts is not null then
    delete from post_comment_likes
    where comment_id in (select id from post_comments where post_id = any(seed_posts));
    delete from post_comments where parent_id is not null and post_id = any(seed_posts);
    delete from post_comments where post_id = any(seed_posts);
    delete from post_likes where post_id = any(seed_posts);
    delete from post_bookmarks where post_id = any(seed_posts);
    delete from post_purchases where post_id = any(seed_posts);
    delete from post_tags where post_id = any(seed_posts);
    delete from posts where id = any(seed_posts);
  end if;
  delete from post_comment_likes where user_id = any(seed_users);
  delete from post_likes where user_id = any(seed_users);
  delete from post_bookmarks where user_id = any(seed_users);
  delete from post_purchases where user_id = any(seed_users);
  delete from blog_subscriptions
  where subscriber_id = any(seed_users) or blog_owner_id = any(seed_users);

  -- 데모
  if seed_demos is not null then
    delete from demo_feedback_likes
    where feedback_id in (select id from demo_feedbacks where demo_id = any(seed_demos));
    delete from demo_feedbacks where parent_id is not null and demo_id = any(seed_demos);
    delete from demo_feedbacks where demo_id = any(seed_demos);
    delete from demo_likes where demo_id = any(seed_demos);
    delete from bookmarks where demo_id = any(seed_demos);
    delete from demo_tags where demo_id = any(seed_demos);
    delete from demo_type_config where demo_id = any(seed_demos);
    -- 미니홈피 showcase가 시드 데모를 가리키면 끊는다
    update mini_homes set showcase_demo1_id = null
    where showcase_demo1_id = any(seed_demos);
    update mini_homes set showcase_demo2_id = null
    where showcase_demo2_id = any(seed_demos);
    update mini_homes set showcase_demo3_id = null
    where showcase_demo3_id = any(seed_demos);
    update feeds set demo_id = null where demo_id = any(seed_demos);
    update posts set demo_id = null where demo_id = any(seed_demos);
    delete from demos where id = any(seed_demos);
  end if;
  delete from demo_feedback_likes where user_id = any(seed_users);
  delete from demo_likes where user_id = any(seed_users);
  delete from bookmarks where user_id = any(seed_users);

  -- 미니홈피
  if seed_homes is not null then
    delete from guestbooks where home_id = any(seed_homes);
    delete from mini_homes where id = any(seed_homes);
  end if;
  delete from guestbooks where writer_id = any(seed_users);

  -- 관계·프로필
  delete from follows
  where follower_id = any(seed_users) or following_id = any(seed_users);
  delete from blocks
  where blocker_id = any(seed_users) or blocked_id = any(seed_users);
  delete from user_interest_tags where user_id = any(seed_users);
  delete from user_tag_profile where user_id = any(seed_users);
  delete from user_badges where user_id = any(seed_users);
  delete from user_sanctions where user_id = any(seed_users) or admin_id = any(seed_users);
  delete from admin_action_logs where admin_id = any(seed_users);
  delete from social_accounts where user_id = any(seed_users);
  delete from notifications
  where receiver_id = any(seed_users) or actor_id = any(seed_users);
  delete from reports
  where reporter_id = any(seed_users) or handled_by = any(seed_users);

  delete from users where id = any(seed_users);

  raise notice 'dev-seed 데이터를 지웠어요.';
end $$;
