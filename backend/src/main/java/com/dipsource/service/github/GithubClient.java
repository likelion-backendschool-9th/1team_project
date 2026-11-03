package com.dipsource.service.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** GitHub REST/GraphQL 호출. 로그인 OAuth 토큰과 별개다. */
@Slf4j
@Component
public class GithubClient {

    private static final String CONTRIBUTION_QUERY = """
            query($login: String!) {
              user(login: $login) {
                contributionsCollection {
                  contributionCalendar {
                    totalContributions
                    weeks {
                      contributionDays {
                        date
                        contributionCount
                        contributionLevel
                      }
                    }
                  }
                }
              }
            }
            """;

    private final RestClient apiClient;
    private final RestClient graphqlClient;
    private final ObjectMapper objectMapper;

    public GithubClient(
            ObjectMapper objectMapper,
            @Value("${app.github.token:}") String token
    ) {
        this.objectMapper = objectMapper;
        this.apiClient = buildClient(token);
        this.graphqlClient = buildClient(token);
    }

    private static RestClient buildClient(String token) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");
        if (token != null && !token.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return builder.build();
    }

    /** 공개 레포 목록 (최대 100개). */
    public List<RepoJson> fetchRepos(String login) {
        try {
            RepoJson[] body = apiClient.get()
                    .uri("/users/{login}/repos?per_page=100&sort=updated", login)
                    .retrieve()
                    .body(RepoJson[].class);
            if (body == null) {
                return List.of();
            }
            return Arrays.asList(body);
        } catch (RestClientException e) {
            log.warn("GitHub repos 조회 실패 login={}: {}", login, e.getMessage());
            return List.of();
        }
    }

    /** 레포 언어별 바이트 수. */
    public Map<String, Long> fetchRepoLanguages(String owner, String repo) {
        try {
            Map<String, Long> body = apiClient.get()
                    .uri("/repos/{owner}/{repo}/languages", owner, repo)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return body == null ? Map.of() : body;
        } catch (RestClientException e) {
            log.warn("GitHub languages 조회 실패 {}/{}: {}", owner, repo, e.getMessage());
            return Map.of();
        }
    }

    /** GraphQL contribution calendar. 없으면 null. */
    public JsonNode fetchContributionCalendar(String login) {
        try {
            ObjectNode variables = objectMapper.createObjectNode();
            variables.put("login", login);
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("query", CONTRIBUTION_QUERY);
            payload.set("variables", variables);

            JsonNode response = graphqlClient.post()
                    .uri("/graphql")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || response.has("errors")) {
                log.warn("GitHub GraphQL 오류 login={}: {}", login, response);
                return null;
            }
            return response.path("data").path("user").path("contributionsCollection")
                    .path("contributionCalendar");
        } catch (RestClientException e) {
            log.warn("GitHub contributions 조회 실패 login={}: {}", login, e.getMessage());
            return null;
        }
    }

    /** GitHub REST 레포 JSON 일부. */
    public record RepoJson(
            String name,
            String full_name,
            Long stargazers_count,
            Boolean fork,
            Boolean archived
    ) {
        public long stars() {
            return stargazers_count == null ? 0L : stargazers_count;
        }

        public boolean isUsable() {
            return name != null && !Boolean.TRUE.equals(fork) && !Boolean.TRUE.equals(archived);
        }
    }

    /** 여러 맵을 합친다. */
    public static Map<String, Long> mergeLanguageBytes(List<Map<String, Long>> maps) {
        Map<String, Long> merged = new LinkedHashMap<>();
        for (Map<String, Long> map : maps) {
            if (map == null) {
                continue;
            }
            map.forEach((lang, bytes) -> merged.merge(lang, bytes, Long::sum));
        }
        return Collections.unmodifiableMap(merged);
    }
}
