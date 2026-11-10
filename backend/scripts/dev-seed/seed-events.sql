-- 이벤트 페이지 시연용 시드 데이터
-- 다른 시드와 관계없이 단독으로 실행할 수 있다. 여러 번 실행해도 중복되지 않는다.
-- 사용법: psql -h localhost -U dipsource -d dipsource -f backend/scripts/dev-seed/seed-events.sql
--
-- 넣는 것
--   이벤트 10개(id 1001~1010). 진행 중 4개, 예정 3개, 종료 3개
-- 상태는 저장하지 않고 시작·종료일로 계산하므로, 날짜를 실행한 날(CURRENT_DATE) 기준으로 넣는다.
-- 다시 실행하면 같은 id의 내용과 날짜를 오늘 기준으로 새로 맞춘다(삭제했던 시드 이벤트도 되살아난다).
-- 상세 화면 본문도 summary를 쓰므로 summary에 참여 방법까지 적는다(최대 200자).

SET client_encoding = 'UTF8';

BEGIN;

INSERT INTO site_events (id, title, emoji, summary, reward, starts_at, ends_at, deleted_at, created_at, updated_at) VALUES
-- 진행 중
(1001, '11월 찍먹 챌린지: 첫 데모 올리기', '🚀',
 '이번 달에 처음으로 데모를 올리면 참여 완료! 작은 버튼 하나, 애니메이션 하나도 좋아요. 데모를 공개 상태로 올리고 피드에 소개 글을 남겨 주세요.',
 '참여자 전원 30칩 + 첫 데모 배지', CURRENT_DATE - 10, CURRENT_DATE + 19, NULL, NOW() - INTERVAL '10 days', NOW()),
(1002, '데모 좋아요 랭킹 위클리', '🏆',
 '이번 주 좋아요를 가장 많이 받은 데모 3개를 뽑아요. 랭킹 페이지의 데모 탭(이번 주)에서 실시간 순위를 확인할 수 있어요.',
 '1등 300칩 · 2등 200칩 · 3등 100칩', CURRENT_DATE - 2, CURRENT_DATE + 4, NULL, NOW() - INTERVAL '2 days', NOW()),
(1003, '블로그 시리즈 연재 응원전', '✍️',
 '기술 블로그에 시리즈를 만들고 회차를 3편 이상 연재해 주세요. 무료 시리즈도 참여할 수 있어요. 완주한 분께는 프로필에 연재 작가 배지를 달아 드려요.',
 '완주자 전원 100칩 + 연재 작가 배지', CURRENT_DATE - 7, CURRENT_DATE + 23, NULL, NOW() - INTERVAL '7 days', NOW()),
(1004, '미니홈피 꾸미기 콘테스트', '🏠',
 '대표 데모, 자기소개, 스택 태그를 채워 나만의 미니홈피를 꾸며 보세요. 방명록에 "콘테스트 참가"라고 남기면 접수돼요. 방문 수와 운영진 투표로 뽑아요.',
 '대상 딥소스 플러스 3개월 · 우수상 150칩', CURRENT_DATE - 5, CURRENT_DATE + 9, NULL, NOW() - INTERVAL '5 days', NOW()),
-- 예정
(1005, '겨울 게임잼: 48시간 브라우저 게임', '🎮',
 '주제는 시작 날 공개돼요. 48시간 안에 브라우저에서 바로 실행되는 게임을 만들어 데모로 올려 주세요. 혼자 또는 3명까지 팀으로 참여할 수 있어요.',
 '1등 500칩 · 참가자 전원 게임잼 배지', CURRENT_DATE + 6, CURRENT_DATE + 8, NULL, NOW(), NOW()),
(1006, '코드 리뷰 품앗이 주간', '🤝',
 '다른 개발자의 데모나 블로그 글에 구체적인 피드백 댓글을 남겨 주세요. 도움이 된 댓글을 운영진이 골라 소개하고 칩을 드려요.',
 '베스트 리뷰어 5명 각 80칩', CURRENT_DATE + 12, CURRENT_DATE + 18, NULL, NOW(), NOW()),
(1007, '연말 회고 블로그 릴레이', '🎄',
 '올해 만든 것, 배운 것, 아쉬웠던 것을 블로그에 회고로 남겨 주세요. 글 마지막에 다음 주자를 지목하면 릴레이가 이어져요.',
 '참여자 전원 20칩 · 추첨 10명 플러스 1개월', CURRENT_DATE + 20, CURRENT_DATE + 41, NULL, NOW(), NOW()),
-- 종료
(1008, '찍먹 오픈 기념 출석 체크', '🎉',
 '찍먹 오픈을 기념해 7일 동안 매일 로그인하고 피드에 한 줄 인사를 남기는 이벤트였어요. 참여해 주신 모든 분께 감사드려요.',
 '7일 출석 시 50칩', CURRENT_DATE - 40, CURRENT_DATE - 34, NULL, NOW() - INTERVAL '40 days', NOW()),
(1009, '10월 UI 인터랙션 챌린지', '✨',
 '마우스나 스크롤에 반응하는 인터랙션을 데모로 만드는 챌린지였어요. 수상작은 데모 탐색의 좋아요순에서 확인할 수 있어요.',
 '1등 300칩 · 참가 배지', CURRENT_DATE - 30, CURRENT_DATE - 16, NULL, NOW() - INTERVAL '30 days', NOW()),
(1010, '첫 팔로우 맺기 이벤트', '👋',
 '관심 있는 개발자 3명을 팔로우하고 DM으로 인사를 건네는 이벤트였어요. 새로 생긴 연결이 오래 이어지길 바라요.',
 '참여자 전원 10칩', CURRENT_DATE - 25, CURRENT_DATE - 18, NULL, NOW() - INTERVAL '25 days', NOW())
ON CONFLICT (id) DO UPDATE SET
  title = EXCLUDED.title,
  emoji = EXCLUDED.emoji,
  summary = EXCLUDED.summary,
  reward = EXCLUDED.reward,
  starts_at = EXCLUDED.starts_at,
  ends_at = EXCLUDED.ends_at,
  deleted_at = NULL,
  updated_at = NOW();

-- 앱에서 새 이벤트를 만들 때 id가 시드와 겹치지 않게 시퀀스를 최댓값으로 맞춘다.
SELECT setval(pg_get_serial_sequence('site_events', 'id'), (SELECT MAX(id) FROM site_events));

COMMIT;
