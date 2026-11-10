package com.dipsource.service.github.impl;

import com.dipsource.dto.user.GithubContributionsResponse;
import com.dipsource.dto.user.GithubLanguageResponse;
import com.dipsource.dto.user.GithubRepoResponse;
import com.dipsource.entity.user.User;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.github.GithubClient;
import com.dipsource.service.github.GithubClient.RepoJson;
import com.dipsource.service.github.GithubService;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link GithubService} 구현체. GitHub 공개 API를 호출해 집계한다. */
@Service
@RequiredArgsConstructor
public class GithubServiceImpl implements GithubService {

    private static final int TOP_REPOS = 3;
    private static final int LANGUAGE_REPO_SAMPLE = 5;

    private final UserRepository userRepository;
    private final GithubClient githubClient;

    @Override
    @Transactional(readOnly = true)
    public GithubContributionsResponse getContributions(Long userId) {
        String login = findGithubUsername(userId);
        if (login == null) {
            return GithubContributionsResponse.empty();
        }
        JsonNode calendar = githubClient.fetchContributionCalendar(login);
        if (calendar == null || calendar.isMissingNode() || calendar.isNull()) {
            return GithubContributionsResponse.empty();
        }

        long total = calendar.path("totalContributions").asLong(0);
        List<GithubContributionsResponse.Day> days = new ArrayList<>();
        for (JsonNode week : calendar.path("weeks")) {
            for (JsonNode day : week.path("contributionDays")) {
                days.add(new GithubContributionsResponse.Day(
                        day.path("date").asText(),
                        mapLevel(day.path("contributionLevel").asText())
                ));
            }
        }
        return new GithubContributionsResponse(total, List.copyOf(days));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GithubRepoResponse> getRepos(Long userId) {
        String login = findGithubUsername(userId);
        if (login == null) {
            return List.of();
        }
        return githubClient.fetchRepos(login).stream()
                .filter(RepoJson::isUsable)
                .sorted(Comparator.comparingLong(RepoJson::stars).reversed())
                .limit(TOP_REPOS)
                .map(repo -> new GithubRepoResponse(repo.name(), repo.stars()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GithubLanguageResponse> getLanguages(Long userId) {
        String login = findGithubUsername(userId);
        if (login == null) {
            return List.of();
        }
        List<RepoJson> top = githubClient.fetchRepos(login).stream()
                .filter(RepoJson::isUsable)
                .sorted(Comparator.comparingLong(RepoJson::stars).reversed())
                .limit(LANGUAGE_REPO_SAMPLE)
                .toList();

        List<Map<String, Long>> languageMaps = new ArrayList<>();
        for (RepoJson repo : top) {
            languageMaps.add(githubClient.fetchRepoLanguages(login, repo.name()));
        }
        Map<String, Long> merged = GithubClient.mergeLanguageBytes(languageMaps);
        long sum = merged.values().stream().mapToLong(Long::longValue).sum();
        if (sum <= 0) {
            return List.of();
        }

        // percent 내림차순, 상위 언어만 (나머지는 기타로 묶지 않고 상위 8개)
        List<Map.Entry<String, Long>> sorted = merged.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .toList();

        List<GithubLanguageResponse> result = new ArrayList<>();
        int assigned = 0;
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<String, Long> entry = sorted.get(i);
            int percent;
            if (i == sorted.size() - 1) {
                percent = Math.max(0, 100 - assigned); // 합이 100이 되게 마지막 보정
            } else {
                percent = (int) Math.round(entry.getValue() * 100.0 / sum);
                assigned += percent;
            }
            if (percent > 0) {
                result.add(new GithubLanguageResponse(entry.getKey(), percent));
            }
        }
        return List.copyOf(result);
    }

    private String findGithubUsername(Long userId) {
        return userRepository.findById(userId)
                .map(User::getGithubUsername)
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .orElse(null);
    }

    private static int mapLevel(String contributionLevel) {
        if (contributionLevel == null) {
            return 0;
        }
        return switch (contributionLevel) {
            case "FIRST_QUARTILE" -> 1;
            case "SECOND_QUARTILE" -> 2;
            case "THIRD_QUARTILE" -> 3;
            case "FOURTH_QUARTILE" -> 4;
            default -> 0;
        };
    }
}
