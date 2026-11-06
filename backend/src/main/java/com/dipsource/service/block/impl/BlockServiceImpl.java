package com.dipsource.service.block.impl;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.service.block.BlockService;
import com.dipsource.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link BlockService} 구현체. */
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void block(Long blockerId, Long blockedId) {
        requireOther(blockerId, blockedId);
        if (!userService.isActiveUser(blockedId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "차단할 수 없는 회원이에요.");
        }
        blockRepository.insertIgnore(blockerId, blockedId);
    }

    @Override
    @Transactional
    public void unblock(Long blockerId, Long blockedId) {
        requireOther(blockerId, blockedId);
        blockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    private static void requireOther(Long blockerId, Long blockedId) {
        if (blockedId == null || blockedId.equals(blockerId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "나 자신은 차단할 수 없어요.");
        }
    }
}