package com.dipsource.service.dm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.dm.DmReadResponse;
import com.dipsource.entity.dm.DmMessage;
import com.dipsource.entity.dm.DmRoom;
import com.dipsource.entity.dm.DmRoomMember;
import com.dipsource.global.websocket.UserMessageSender;
import com.dipsource.repository.block.BlockRepository;
import com.dipsource.repository.dm.DmMessageRepository;
import com.dipsource.repository.dm.DmRoomMemberRepository;
import com.dipsource.repository.dm.DmRoomRepository;
import com.dipsource.service.dm.impl.DmServiceImpl;
import com.dipsource.service.user.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DmServiceImplTest {

    private static final Long ME = 1L;
    private static final Long PARTNER = 2L;

    private DmRoomRepository roomRepository;
    private DmRoomMemberRepository memberRepository;
    private DmMessageRepository messageRepository;
    private BlockRepository blockRepository;
    private UserMessageSender userMessageSender;
    private DmServiceImpl service;
    private DmRoomMember me;

    @BeforeEach
    void setUp() {
        roomRepository = mock(DmRoomRepository.class);
        memberRepository = mock(DmRoomMemberRepository.class);
        messageRepository = mock(DmMessageRepository.class);
        blockRepository = mock(BlockRepository.class);
        userMessageSender = mock(UserMessageSender.class);
        service = new DmServiceImpl(roomRepository, memberRepository, messageRepository,
                mock(UserService.class), blockRepository, userMessageSender);

        DmRoom room = DmRoom.create();
        ReflectionTestUtils.setField(room, "id", 10L);
        me = new DmRoomMember(room, ME);
        when(roomRepository.findSharedRooms(ME, PARTNER)).thenReturn(List.of(room));
        when(memberRepository.findByIdRoomId(10L)).thenReturn(List.of(me, new DmRoomMember(room, PARTNER)));
        DmMessage latest = mock(DmMessage.class);
        when(latest.getId()).thenReturn(50L);
        when(messageRepository.findFirstByRoomIdAndDeletedAtIsNullOrderByIdDesc(10L)).thenReturn(Optional.of(latest));
    }

    @Test
    void markReadTellsPartner() {
        service.markRead(ME, PARTNER);

        assertThat(me.getLastReadMessageId()).isEqualTo(50L);
        verify(userMessageSender).send(PARTNER, UserMessageSender.DM_READ, new DmReadResponse(ME, 50L));
    }

    @Test
    void alreadyReadDoesNotTellAgain() {
        me.read(50L);

        service.markRead(ME, PARTNER);

        verify(userMessageSender, never()).send(anyLong(), any(), any());
    }

    @Test
    void blockedDoesNothing() {
        when(blockRepository.existsBetween(ME, PARTNER)).thenReturn(true);

        service.markRead(ME, PARTNER);

        assertThat(me.getLastReadMessageId()).isNull();
        verify(userMessageSender, never()).send(eq(PARTNER), any(), any());
    }
}
