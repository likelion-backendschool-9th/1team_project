package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoBackendResponse;
import com.dipsource.dto.demo.DemoResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoBackendStatus;
import com.dipsource.entity.demo.DemoStatus;
import com.dipsource.entity.demo.DemoTypeConfig;
import com.dipsource.entity.demo.DemoVersion;
import com.dipsource.global.config.DemoBackendProperties;
import com.dipsource.global.config.DemoProperties;
import com.dipsource.repository.demo.DemoBookmarkRepository;
import com.dipsource.repository.demo.DemoLikeRepository;
import com.dipsource.repository.demo.DemoTagRepository;
import com.dipsource.repository.demo.DemoTypeConfigRepository;
import com.dipsource.repository.demo.DemoVersionRepository;
import com.dipsource.service.user.UserService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 데모 응답을 만든다. 작성자·태그·좋아요·버전은 한 번에 모아 읽어 N+1을 피한다. */
@Component
@RequiredArgsConstructor
public class DemoResponseAssembler {

    private static final double BYTES_PER_MB = 1024.0 * 1024.0;
    private static final String DEFAULT_ENGINE = "html/css/js";

    private final UserService userService;
    private final DemoTagRepository tagRepository;
    private final DemoLikeRepository likeRepository;
    private final DemoBookmarkRepository bookmarkRepository;
    private final DemoVersionRepository versionRepository;
    private final DemoTypeConfigRepository typeConfigRepository;
    private final DemoProperties properties;
    private final DemoBackendProperties backendProperties;

    public DemoResponse toResponse(Demo demo, Long viewerId) {
        return toResponses(List.of(demo), viewerId).get(0);
    }

    public List<DemoResponse> toResponses(List<Demo> demos, Long viewerId) {
        if (demos.isEmpty()) {
            return List.of();
        }
        List<Long> demoIds = demos.stream().map(Demo::getId).toList();
        Map<Long, UserSummaryResponse> authors = userService.getSummaries(
                demos.stream().map(Demo::getUserId).distinct().toList());
        Map<Long, List<String>> tags = tagsOf(demoIds);
        Set<Long> liked = viewerId == null ? Set.of() : new HashSet<>(likeRepository.findLikedDemoIds(viewerId, demoIds));
        Set<Long> bookmarked = viewerId == null
                ? Set.of() : new HashSet<>(bookmarkRepository.findBookmarkedDemoIds(viewerId, demoIds));
        Map<Long, DemoVersion> versions = versionRepository.findAllById(
                        demos.stream().map(Demo::getCurrentVersionId).filter(Objects::nonNull).toList())
                .stream().collect(Collectors.toMap(DemoVersion::getId, Function.identity()));
        Map<Long, DemoTypeConfig> configs = configsOf(demos);

        return demos.stream().map(demo -> {
            List<String> demoTags = tags.getOrDefault(demo.getId(), List.of());
            DemoVersion version = versions.get(demo.getCurrentVersionId());
            return new DemoResponse(
                    demo.getId(),
                    demo.getTitle(),
                    demo.getDescription() == null ? "" : demo.getDescription(),
                    demo.getThumbnail(),
                    demoTags,
                    authors.getOrDefault(demo.getUserId(), UserSummaryResponse.from(null)),
                    demo.getLikeCount(),
                    demo.getPlayCount(),
                    demo.getFeedbackCount(),
                    demo.getCreatedAt(),
                    "web",
                    version == null ? 0 : version.getTotalBytes() / BYTES_PER_MB,
                    liked.contains(demo.getId()),
                    bookmarked.contains(demo.getId()),
                    demo.getStatus().name(),
                    demo.getCurrentVersionId(),
                    runUrl(demo, version),
                    demoTags.isEmpty() ? DEFAULT_ENGINE : demoTags.get(0),
                    backendOf(demo, configs.get(demo.getId()), viewerId));
        }).toList();
    }

    public DemoBackendResponse toBackendResponse(Demo demo, Long viewerId) {
        return backendOf(demo, configsOf(List.of(demo)).get(demo.getId()), viewerId);
    }

    private Map<Long, DemoTypeConfig> configsOf(List<Demo> demos) {
        List<Long> connected = demos.stream().filter(demo -> demo.getApiUrl() != null).map(Demo::getId).toList();
        if (connected.isEmpty()) {
            return Map.of();
        }
        return typeConfigRepository.findByDemoIdIn(connected).stream()
                .collect(Collectors.toMap(config -> config.getDemo().getId(), Function.identity()));
    }

    private DemoBackendResponse backendOf(Demo demo, DemoTypeConfig config, Long viewerId) {
        if (demo.getApiUrl() == null) {
            return new DemoBackendResponse(null, null, DemoBackendStatus.NOT_CONFIGURED, null, false);
        }
        String healthCheckUrl = config == null ? null : config.getHealthCheckUrl();
        boolean checkable = backendProperties.healthCheckEnabled() && healthCheckUrl != null;
        return new DemoBackendResponse(
                demo.getApiUrl(),
                demo.getUserId().equals(viewerId) ? healthCheckUrl : null,
                backendStatus(config, checkable),
                healthCheckUrl == null ? null : config.getLastHealthAt(),
                checkable);
    }

    /** 확인한 적 없거나 결과가 오래되면 UNKNOWN이다. is_healthy 기본값이 true라 시각을 먼저 본다. */
    private DemoBackendStatus backendStatus(DemoTypeConfig config, boolean checkable) {
        if (!checkable || config.getLastHealthAt() == null
                || config.getLastHealthAt().isBefore(LocalDateTime.now().minusMinutes(backendProperties.healthFreshMinutes()))) {
            return DemoBackendStatus.UNKNOWN;
        }
        return config.isHealthy() ? DemoBackendStatus.ONLINE : DemoBackendStatus.OFFLINE;
    }

    private String runUrl(Demo demo, DemoVersion version) {
        if (demo.getStatus() != DemoStatus.ACTIVE || version == null) {
            return null;
        }
        return properties.workerBaseUrl() + "/demos/" + demo.getId() + "/versions/" + version.getId()
                + "/" + version.getEntryPath();
    }

    private Map<Long, List<String>> tagsOf(List<Long> demoIds) {
        Map<Long, List<String>> tags = new HashMap<>();
        for (Object[] row : tagRepository.findTagNames(demoIds)) {
            tags.computeIfAbsent((Long) row[0], id -> new ArrayList<>()).add((String) row[1]);
        }
        return tags;
    }
}
