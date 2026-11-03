-- preview-seed.sql로 넣은 더미 회원과 그 회원들의 대화를 모두 지운다.
with preview as (select id from users where email like 'preview-%@test.local'),
     rooms as (select distinct room_id from dm_room_members where user_id in (select id from preview)),
     del_messages as (delete from dm_messages where room_id in (select room_id from rooms)),
     del_members as (delete from dm_room_members where room_id in (select room_id from rooms))
select 1;
delete from dm_rooms r where not exists (select 1 from dm_room_members m where m.room_id = r.id);
delete from users where email like 'preview-%@test.local';
