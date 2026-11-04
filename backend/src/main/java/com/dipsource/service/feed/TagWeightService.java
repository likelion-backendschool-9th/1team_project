package com.dipsource.service.feed;

import com.dipsource.entity.user.UserTagId;
import com.dipsource.entity.user.UserTagProfile;
import com.dipsource.entity.user.User;
import com.dipsource.repository.feed.FeedTagRepository;
import com.dipsource.repository.user.UserTagProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 피드 활동(좋아요, 댓글 등) 시 사용자의 태그 가중치를 갱신한다.
 * 다른 서비스에서 좋아요/댓글 처리 후 이 서비스를 호출하면 됨.
 */
@Service
@RequiredArgsConstructor
public class TagWeightService {

    private final FeedTagRepository feedTagRepository;
    private final UserTagProfileRepository userTagProfileRepository;

    /**
     * 피드에 달린 태그들의 가중치를 증가시킨다.
     * 좋아요, 댓글, 북마크 시 호출.
     */
    @Transactional
    public void increaseWeight(User user, Long feedId, int delta) {
        List<Long> tagIds = feedTagRepository.findTagIdsByFeedId(feedId);
        if (tagIds.isEmpty()) {
            return;
        }

        List<UserTagId> ids = tagIds.stream()
                .map(tagId -> new UserTagId(user.getId(), tagId))
                .toList();

        // 기존 프로필을 한 번에 조회
        List<UserTagProfile> existing = userTagProfileRepository.findAllById(ids);
        java.util.Map<Long, UserTagProfile> byTagId = new java.util.HashMap<>();
        for (UserTagProfile p : existing) {
            byTagId.put(p.getId().getTagId(), p);
        }

        // 없는 태그는 새로 생성, 있는 건 가중치 갱신
        List<UserTagProfile> toSave = new java.util.ArrayList<>();
        for (Long tagId : tagIds) {
            UserTagProfile profile = byTagId.get(tagId);
            if (profile == null) {
                profile = new UserTagProfile(user, tagId, 0);
            }
            profile.addWeight(delta);
            toSave.add(profile);
        }
        userTagProfileRepository.saveAll(toSave);
    }

    /**
     * 피드에 달린 태그들의 가중치를 감소시킨다.
     * 좋아요 취소, 댓글 삭제 시 호출.
     */
    @Transactional
    public void decreaseWeight(User user, Long feedId, int delta) {
        increaseWeight(user, feedId, -delta);
    }
}
