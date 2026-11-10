package com.dipsource.service.block.impl;

import com.dipsource.dto.block.BlockedUserResponse;
import com.dipsource.entity.block.Block;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.follow.FollowRepository;
import com.dipsource.repository.post.PostBookmarkRepository;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.block.BlockService;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link BlockService} 구현체. */
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;
    private final FollowRepository followRepository;
    private final PostBookmarkRepository postBookmarkRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void block(Long blockerId, Long blockedId) {
        requireOther(blockerId, blockedId);
        if (!userService.isActiveUser(blockedId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "차단할 수 없는 회원이에요.");
        }
        blockRepository.insertIgnore(blockerId, blockedId);
        followRepository.deleteByFollowerIdAndFollowingId(blockerId, blockedId);
        followRepository.deleteByFollowerIdAndFollowingId(blockedId, blockerId);
        // 차단하면 서로의 글이 보이지 않으니 블로그 글 북마크도 양쪽 모두 지운다. 차단을 풀어도 되살리지 않는다.
        // 차단당한 쪽에는 알리지 않는다 (차단 사실이 드러나지 않도록).
        postBookmarkRepository.deleteBetween(blockerId, blockedId);
    }

    @Override
    @Transactional
    public void unblock(Long blockerId, Long blockedId) {
        requireOther(blockerId, blockedId);
        blockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockedUserResponse> listBlocked(Long blockerId) {
        List<Block> blocks = blockRepository.findByIdBlockerIdOrderByCreatedAtDesc(blockerId);
        if (blocks.isEmpty()) {
            return List.of();
        }
        List<Long> ids = blocks.stream().map(block -> block.getId().getBlockedId()).toList();
        Map<Long, User> users = userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return blocks.stream()
                .filter(block -> users.containsKey(block.getId().getBlockedId()))
                .map(block -> BlockedUserResponse.of(users.get(block.getId().getBlockedId()), block.getCreatedAt()))
                .toList();
    }

    private static void requireOther(Long blockerId, Long blockedId) {
        if (blockedId == null || blockedId.equals(blockerId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "나 자신은 차단할 수 없어요.");
        }
    }
}