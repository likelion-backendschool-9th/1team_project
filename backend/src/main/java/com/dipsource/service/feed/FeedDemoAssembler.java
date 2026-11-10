package com.dipsource.service.feed;

import com.dipsource.dto.feed.FeedDemoResponse;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoStatus;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.demo.DemoTagRepository;
import com.dipsource.repository.user.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 피드·블로그 글에 붙은 데모를 카드용 요약으로 모은다. */
@Component
@RequiredArgsConstructor
public class FeedDemoAssembler {

    private final DemoRepository demoRepository;
    private final DemoTagRepository tagRepository;
    private final UserRepository userRepository;

    /** 첨부가 없으면 통과한다. 없거나 숨김·삭제·남의 초안이면 거절한다. */
    public void requireAttachable(Long demoId, Long userId) {
        if (demoId == null) {
            return;
        }
        Demo demo = demoRepository.findById(demoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "데모를 찾을 수 없습니다"));
        if (!visible(demo, userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "데모를 찾을 수 없습니다");
        }
    }

    /** 피드 하나에 붙은 데모. 없거나 보여 주면 안 되면 null. */
    public FeedDemoResponse of(Feed feed) {
        if (feed.getDemoId() == null) {
            return null;
        }
        return of(List.of(feed)).get(feed.getDemoId());
    }

    /** demoId → 카드 요약. 목록 조회에서 데모를 한 번에 읽는다. */
    public Map<Long, FeedDemoResponse> of(Collection<Feed> feeds) {
        return byIds(feeds.stream().map(Feed::getDemoId).toList());
    }

    /** demoId → 카드 요약. 블로그 글처럼 피드가 아닌 곳에 붙은 데모도 같은 규칙으로 읽는다. null은 건너뛴다. */
    public Map<Long, FeedDemoResponse> byIds(Collection<Long> ids) {
        List<Long> demoIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (demoIds.isEmpty()) {
            return Map.of();
        }
        List<Demo> demos = demoRepository.findAllById(demoIds).stream()
                .filter(demo -> visible(demo, null))
                .toList();
        if (demos.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<String>> tags = tagsOf(demos.stream().map(Demo::getId).toList());
        Map<Long, User> authors = userRepository.findAllById(
                        demos.stream().map(Demo::getUserId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, FeedDemoResponse> result = new HashMap<>();
        for (Demo demo : demos) {
            result.put(demo.getId(), new FeedDemoResponse(
                    demo.getId(),
                    demo.getTitle(),
                    demo.getDescription() == null ? "" : demo.getDescription(),
                    demo.getThumbnail(),
                    tags.getOrDefault(demo.getId(), List.of()),
                    UserSummaryResponse.from(authors.get(demo.getUserId())),
                    demo.getLikeCount()));
        }
        return result;
    }

    /** 공개 데모는 누구나, 초안은 작성자만 첨부할 수 있다. 조회 때는 이미 붙은 초안도 보여 준다. */
    private boolean visible(Demo demo, Long actorId) {
        if (demo.getStatus() == DemoStatus.DELETED || demo.getStatus() == DemoStatus.HIDDEN) {
            return false;
        }
        if (demo.getStatus() == DemoStatus.DRAFT) {
            return actorId == null || demo.getUserId().equals(actorId);
        }
        return true;
    }

    private Map<Long, List<String>> tagsOf(List<Long> demoIds) {
        Map<Long, List<String>> tags = new HashMap<>();
        for (Object[] row : tagRepository.findTagNames(demoIds)) {
            tags.computeIfAbsent((Long) row[0], id -> new ArrayList<>()).add((String) row[1]);
        }
        return tags;
    }
}
