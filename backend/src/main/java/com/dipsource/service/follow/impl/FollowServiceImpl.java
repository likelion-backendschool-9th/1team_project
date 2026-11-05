package com.dipsource.service.follow.impl;

import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.service.follow.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link FollowService} 구현체. */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isMutualFollow(Long userId, Long targetId) {
        if (userId == null || targetId == null || userId.equals(targetId)) {
            return false;
        }
        // 내가 상대를, 상대가 나를 팔로우한 기록이 둘 다 있어야 맞팔이다.
        return followRepository.existsByIdFollowerIdAndIdFollowingId(userId, targetId)
                && followRepository.existsByIdFollowerIdAndIdFollowingId(targetId, userId);
    }
}
