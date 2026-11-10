package com.dipsource.service.block;

import com.dipsource.dto.block.BlockedUserResponse;
import java.util.List;

/** 차단·해제. 이미 차단/아니어도 오류 없이 넘어간다 (멱등). */
public interface BlockService {

    /** 차단. 나 자신이거나 활동 중이 아닌 회원이면 예외. */
    void block(Long blockerId, Long blockedId);

    /** 차단 해제. 나 자신이면 예외. 상대가 탈퇴했어도 목록 정리를 위해 삭제는 한다. */
    void unblock(Long blockerId, Long blockedId);

    /** 내가 차단한 회원 목록. 최근 차단 순. */
    List<BlockedUserResponse> listBlocked(Long blockerId);
}