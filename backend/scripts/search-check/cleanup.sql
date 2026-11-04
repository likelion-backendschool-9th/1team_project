-- seed.sql로 만든 검색 더미 데이터를 지운다. (예전 시드의 search-writer@test.local도 함께 지운다)
do $$
declare
  writers bigint[];
begin
  select array_agg(id) into writers from users
  where email like '%@search.local' or email = 'search-writer@test.local';
  if writers is null then
    raise notice '지울 검색 더미 데이터가 없어요.';
    return;
  end if;

  delete from post_tags where post_id in (select id from posts where user_id = any(writers));
  delete from feed_tags where feed_id in (select id from feeds where user_id = any(writers));
  delete from demo_tags where demo_id in (select id from demos where user_id = any(writers));
  delete from posts where user_id = any(writers);
  delete from feeds where user_id = any(writers);
  delete from demos where user_id = any(writers);
  delete from user_interest_tags where user_id = any(writers);
  delete from users where id = any(writers);

  -- seed.sql이 만든 태그 중 아무도 쓰지 않는 것만 지운다
  delete from tags t
  where t.name in ('Java', 'Spring Boot', 'JPA', 'React', 'TypeScript', 'Next.js', 'Python', 'Kafka', 'Redis',
                   'Docker', 'Kubernetes', 'AWS', 'Unity', 'C#', 'JavaScript', 'Node.js', 'WebSocket', 'Elasticsearch')
    and not exists (select 1 from post_tags x where x.tag_id = t.id)
    and not exists (select 1 from feed_tags x where x.tag_id = t.id)
    and not exists (select 1 from demo_tags x where x.tag_id = t.id)
    and not exists (select 1 from user_interest_tags x where x.tag_id = t.id);

  raise notice '검색 더미 데이터를 지웠어요.';
end $$;
