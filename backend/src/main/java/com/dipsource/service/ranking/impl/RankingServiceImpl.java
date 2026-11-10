package com.dipsource.service.ranking.impl;

import com.dipsource.dto.ranking.RankingResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoLikeRepository;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.repository.minihome.MiniHomeRepository;
import com.dipsource.repository.post.PostRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.ranking.RankingService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 이미 있는 좋아요·조회·방문·팔로우 수로 순위를 만든다. 새 테이블은 쓰지 않는다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    static final int LIMIT = 20;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final DemoLikeRepository demoLikeRepository;
    private final DemoRepository demoRepository;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final MiniHomeRepository miniHomeRepository;
    private final UserRepository userRepository;

    @Override
    public List<RankingResponse> rank(String type, String period) {
        LocalDateTime since = since(period);
        List<Score> scores = switch (type == null ? "" : type) {
            case "demo" -> scores(demoLikeRepository.rankByRecentLikes(since));
            case "blog" -> scores(postRepository.rankByViews());
            case "follower" -> followerScores(since);
            case "home" -> scores(miniHomeRepository.rankByVisits());
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "순위 종류는 demo, blog, follower, home 중 하나예요.");
        };
        return top(scores);
    }

    /** 팔로워 수 동점이면 공개 데모 좋아요 합으로 가른다. */
    private List<Score> followerScores(LocalDateTime since) {
        List<Object[]> counts = followRepository.countNewFollowers(since);
        if (counts.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = counts.stream().map(row -> number(row[0])).toList();
        Map<Long, Long> likes = new HashMap<>();
        for (Object[] row : demoRepository.sumActiveLikesByUserIds(userIds)) {
            likes.put(number(row[0]), number(row[1]));
        }
        List<Score> scores = new ArrayList<>();
        for (Object[] row : counts) {
            long userId = number(row[0]);
            scores.add(new Score(userId, userId, "", number(row[1]), likes.getOrDefault(userId, 0L)));
        }
        return scores;
    }

    private List<RankingResponse> top(List<Score> scores) {
        List<Score> ranked = scores.stream()
                .filter(score -> score.score > 0)
                .sorted(Comparator.comparingLong(Score::score).reversed()
                        .thenComparing(Comparator.comparingLong(Score::tieBreak).reversed())
                        .thenComparingLong(Score::subjectId))
                .toList();
        if (ranked.isEmpty()) {
            return List.of();
        }
        Map<Long, User> users = new HashMap<>();
        for (User user : userRepository.findAllById(ranked.stream().map(Score::userId).distinct().toList())) {
            users.put(user.getId(), user);
        }
        List<RankingResponse> result = new ArrayList<>();
        for (Score score : ranked) {
            if (result.size() == LIMIT) {
                break;
            }
            User user = users.get(score.userId);
            if (user == null || user.getStatus() != UserStatus.ACTIVE) {
                continue;
            }
            result.add(new RankingResponse(
                    result.size() + 1,
                    UserSummaryResponse.from(user),
                    score.title,
                    score.score,
                    0));
        }
        return result;
    }

    private static List<Score> scores(List<Object[]> rows) {
        List<Score> scores = new ArrayList<>();
        for (Object[] row : rows) {
            scores.add(new Score(
                    number(row[0]),
                    number(row[1]),
                    row[2] == null ? "" : row[2].toString(),
                    number(row[3]),
                    number(row[4])));
        }
        return scores;
    }

    private static long number(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    /** daily는 서울 오늘 0시, weekly는 최근 7일, monthly는 최근 30일. */
    private static LocalDateTime since(String period) {
        ZonedDateTime now = ZonedDateTime.now(SEOUL);
        return switch (period == null ? "" : period) {
            case "daily" -> now.toLocalDate().atStartOfDay();
            case "weekly" -> now.minusDays(7).toLocalDateTime();
            case "monthly" -> now.minusDays(30).toLocalDateTime();
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "기간은 daily, weekly, monthly 중 하나예요.");
        };
    }

    private record Score(long subjectId, long userId, String title, long score, long tieBreak) {
    }
}
