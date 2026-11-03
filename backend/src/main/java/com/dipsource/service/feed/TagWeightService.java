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

        for (Long tagId : tagIds) {
            UserTagId id = new UserTagId(user.getId(), tagId);
            UserTagProfile profile = userTagProfileRepository.findById(id)
                    .orElseGet(() -> new UserTagProfile(user, tagId, 0));

            profile.addWeight(delta);
            userTagProfileRepository.save(profile);
        }
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
