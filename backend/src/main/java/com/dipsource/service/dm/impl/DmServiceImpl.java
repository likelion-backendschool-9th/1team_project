package com.dipsource.service.dm.impl;

import com.dipsource.dto.dm.DmMessageItemResponse;
import com.dipsource.dto.dm.DmMessageResponse;
import com.dipsource.dto.dm.DmReadResponse;
import com.dipsource.dto.dm.DmRoomResponse;
import com.dipsource.dto.dm.DmSendRequest;
import com.dipsource.dto.user.UserSummaryResponse;
import com.dipsource.entity.dm.DmMessage;
import com.dipsource.entity.dm.DmRoom;
import com.dipsource.entity.dm.DmRoomMember;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.global.websocket.UserMessageSender;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.dm.DmMessageRepository;
import com.dipsource.repository.dm.DmRoomMemberRepository;
import com.dipsource.repository.dm.DmRoomRepository;
import com.dipsource.service.dm.DmService;
import com.dipsource.service.user.UserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link DmService} 구현체. */
@Service
@RequiredArgsConstructor
public class DmServiceImpl implements DmService {

    /** 대화 내역 한 번에 최대 몇 건까지 주는지 */
    private static final int MAX_PAGE_SIZE = 100;

    private final DmRoomRepository dmRoomRepository;
    private final DmRoomMemberRepository dmRoomMemberRepository;
    private final DmMessageRepository dmMessageRepository;
    private final UserService userService;
    private final BlockRepository blockRepository;
    private final UserMessageSender userMessageSender;

    @Override
    @Transactional
    public DmMessageResponse send(Long senderId, DmSendRequest request) {
        Long receiverId = request.receiverId();
        String content = request.content() == null ? "" : request.content().trim();
        if (content.isEmpty() || content.length() > 1000) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "메시지는 1~1000자로 입력해 주세요.");
        }
        if (receiverId == null || receiverId.equals(senderId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "받는 사람을 확인해 주세요.");
        }
        // 차단 여부가 드러나지 않도록 없는 회원과 같은 응답을 준다.
        if (!userService.isActiveUser(receiverId) || blockRepository.existsBetween(senderId, receiverId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "메시지를 보낼 수 없는 회원이에요.");
        }

        DmRoom room = findOrCreateRoom(senderId, receiverId);
        DmMessage message = dmMessageRepository.save(DmMessage.builder()
                .room(room)
                .senderId(senderId)
                .content(content)
                .build());
        room.touch(message.getCreatedAt());

        for (DmRoomMember member : dmRoomMemberRepository.findByIdRoomId(room.getId())) {
            // 나갔던 사람이 있으면 새 메시지가 왔으니 다시 대화방에 넣는다.
            member.rejoin();
            // 내가 보낸 메시지는 내가 읽은 것으로 본다.
            if (member.getId().getUserId().equals(senderId)) {
                member.read(message.getId());
            }
        }
        userService.markActive(senderId);

        UserSummaryResponse sender = userService.getSummaries(List.of(senderId)).get(senderId);
        return DmMessageResponse.of(message, sender, receiverId);
    }

    @Override
    @Transactional
    public List<DmRoomResponse> getRooms(Long userId) {
        userService.markActive(userId);

        Set<Long> hidden = new HashSet<>(blockRepository.findHiddenUserIds(userId));
        List<RoomSummary> rooms = new ArrayList<>();
        for (DmRoomMember me : dmRoomMemberRepository.findByIdUserIdAndLeftAtIsNull(userId)) {
            Long roomId = me.getId().getRoomId();
            Optional<Long> partnerId = findPartnerId(roomId, userId);
            Optional<DmMessage> last = dmMessageRepository.findFirstByRoomIdAndDeletedAtIsNullOrderByIdDesc(roomId);
            // 상대가 없거나 메시지가 하나도 없거나 차단 관계인 방은 목록에 보이지 않는다.
            if (partnerId.isEmpty() || last.isEmpty() || hidden.contains(partnerId.get())) {
                continue;
            }
            long unread = dmMessageRepository.countByRoomIdAndSenderIdNotAndIdGreaterThanAndDeletedAtIsNull(
                    roomId, userId, me.getLastReadMessageId() == null ? 0L : me.getLastReadMessageId());
            rooms.add(new RoomSummary(partnerId.get(), last.get(), unread));
        }

        List<Long> partnerIds = rooms.stream().map(RoomSummary::partnerId).toList();
        Map<Long, UserSummaryResponse> partners = userService.getSummaries(partnerIds);
        Set<Long> online = userService.getOnlineUserIds(partnerIds);

        return rooms.stream()
                .filter(room -> partners.containsKey(room.partnerId()))
                // 마지막 메시지 시각이 최근인 방이 위로 온다.
                .sorted(Comparator.comparing((RoomSummary room) -> room.last().getCreatedAt()).reversed())
                .map(room -> new DmRoomResponse(
                        partners.get(room.partnerId()),
                        online.contains(room.partnerId()),
                        room.last().getContent(),
                        room.last().getCreatedAt(),
                        room.unreadCount()))
                .toList();
    }

    @Override
    @Transactional
    public PageResponse<DmMessageItemResponse> getMessages(Long userId, Long partnerId, Pageable pageable) {
        if (partnerId == null || partnerId.equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "대화 상대를 확인해 주세요.");
        }
        userService.markActive(userId);

        int size = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Optional<DmRoom> room = blockRepository.existsBetween(userId, partnerId)
                ? Optional.empty()
                : dmRoomRepository.findSharedRooms(userId, partnerId).stream().findFirst();
        if (room.isEmpty()) {
            return new PageResponse<>(List.of(), pageable.getPageNumber(), size, 0, 0);
        }
        Long roomId = room.get().getId();

        // 최신 메시지부터 가져와(0페이지 = 가장 최근) 화면 순서대로 오래된 → 최신으로 뒤집는다.
        Page<DmMessage> page = dmMessageRepository.findByRoomIdAndDeletedAtIsNull(
                roomId, PageRequest.of(pageable.getPageNumber(), size, Sort.by(Sort.Direction.DESC, "id")));

        Long partnerLastRead = null;
        DmRoomMember me = null;
        for (DmRoomMember member : dmRoomMemberRepository.findByIdRoomId(roomId)) {
            if (member.getId().getUserId().equals(userId)) {
                me = member;
            } else {
                partnerLastRead = member.getLastReadMessageId();
            }
        }

        List<DmMessageItemResponse> items = new ArrayList<>();
        for (DmMessage message : page.getContent()) {
            boolean mine = message.getSenderId().equals(userId);
            boolean read = !mine || (partnerLastRead != null && partnerLastRead >= message.getId());
            items.add(new DmMessageItemResponse(
                    message.getId(), message.getSenderId(), message.getContent(), message.getCreatedAt(), read));
        }
        Collections.reverse(items);

        // 대화 내역을 열었으니 가장 최근 메시지까지 읽은 것으로 표시한다.
        if (me != null) {
            readUpToLatest(me, partnerId);
        }

        return PageResponse.of(page, items);
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long partnerId) {
        if (partnerId == null || partnerId.equals(userId) || blockRepository.existsBetween(userId, partnerId)) {
            return;
        }
        dmRoomRepository.findSharedRooms(userId, partnerId).stream()
                .findFirst()
                .flatMap(room -> dmRoomMemberRepository.findByIdRoomId(room.getId()).stream()
                        .filter(member -> member.getId().getUserId().equals(userId))
                        .findFirst())
                .ifPresent(me -> readUpToLatest(me, partnerId));
    }

    /** 방의 가장 최근 메시지까지 읽음 처리하고, 새로 읽은 게 있으면 상대 화면의 "1"을 지우도록 알린다. */
    private void readUpToLatest(DmRoomMember me, Long partnerId) {
        dmMessageRepository.findFirstByRoomIdAndDeletedAtIsNullOrderByIdDesc(me.getId().getRoomId())
                .ifPresent(latest -> {
                    Long before = me.getLastReadMessageId();
                    if (before != null && before >= latest.getId()) {
                        return;
                    }
                    me.read(latest.getId());
                    userMessageSender.send(partnerId, UserMessageSender.DM_READ,
                            new DmReadResponse(me.getId().getUserId(), latest.getId()));
                });
    }

    private Optional<Long> findPartnerId(Long roomId, Long userId) {
        return dmRoomMemberRepository.findByIdRoomId(roomId).stream()
                .map(member -> member.getId().getUserId())
                .filter(memberId -> !memberId.equals(userId))
                .findFirst();
    }

    private DmRoom findOrCreateRoom(Long userId, Long partnerId) {
        // 메시지가 동시에 여러 개 오면 각자 "방 없음"으로 보고 방을 여러 개 만들 수 있어, 두 회원 단위로 잠근다.
        long low = Math.min(userId, partnerId);
        long high = Math.max(userId, partnerId);
        dmRoomRepository.lockPair((low << 32) | high);

        return dmRoomRepository.findSharedRooms(userId, partnerId).stream()
                .findFirst()
                .orElseGet(() -> {
                    DmRoom room = dmRoomRepository.save(DmRoom.create());
                    dmRoomMemberRepository.save(new DmRoomMember(room, userId));
                    dmRoomMemberRepository.save(new DmRoomMember(room, partnerId));
                    return room;
                });
    }

    /** 목록을 만들 때 쓰는 중간 값 */
    private record RoomSummary(Long partnerId, DmMessage last, long unreadCount) {
    }
}
