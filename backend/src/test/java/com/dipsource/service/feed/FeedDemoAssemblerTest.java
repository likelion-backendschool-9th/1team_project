package com.dipsource.service.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dipsource.dto.feed.FeedDemoResponse;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoStatus;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.demo.DemoRepository;
import com.dipsource.repository.demo.DemoTagRepository;
import com.dipsource.repository.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedDemoAssemblerTest {

    @Mock DemoRepository demoRepository;
    @Mock DemoTagRepository tagRepository;
    @Mock UserRepository userRepository;
    @InjectMocks FeedDemoAssembler assembler;

    @Test
    @DisplayName("공개 데모는 제목·태그와 함께 피드 카드에 붙는다")
    void showsActiveDemo() {
        Feed feed = feed(9L);
        Demo demo = demo(9L, 3L, DemoStatus.ACTIVE);
        when(demo.getTitle()).thenReturn("붙일 데모");
        when(demo.getDescription()).thenReturn("설명");
        when(demo.getThumbnail()).thenReturn(null);
        when(demo.getLikeCount()).thenReturn(1);
        when(demoRepository.findAllById(List.of(9L))).thenReturn(List.of(demo));
        when(tagRepository.findTagNames(List.of(9L))).thenReturn(List.<Object[]>of(new Object[]{9L, "Spring"}));
        User author = author(3L);
        when(userRepository.findAllById(List.of(3L))).thenReturn(List.of(author));

        FeedDemoResponse response = assembler.of(feed);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("붙일 데모");
        assertThat(response.techTags()).containsExactly("Spring");
        assertThat(response.author().nickname()).isEqualTo("곰");
    }

    @Test
    @DisplayName("삭제된 데모는 카드에 넣지 않는다")
    void hidesDeletedDemo() {
        Feed feed = feed(9L);
        Demo deleted = demo(9L, 3L, DemoStatus.DELETED);
        when(demoRepository.findAllById(List.of(9L))).thenReturn(List.of(deleted));

        assertThat(assembler.of(feed)).isNull();
    }

    @Test
    @DisplayName("없는 데모를 첨부하면 거절한다")
    void rejectsMissingDemo() {
        when(demoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assembler.requireAttachable(9L, 3L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("남의 초안은 첨부할 수 없다")
    void rejectsOthersDraft() {
        Demo draft = demo(9L, 4L, DemoStatus.DRAFT);
        when(demoRepository.findById(9L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> assembler.requireAttachable(9L, 3L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    private static Feed feed(Long demoId) {
        Feed feed = mock(Feed.class);
        when(feed.getDemoId()).thenReturn(demoId);
        return feed;
    }

    private static Demo demo(Long id, Long userId, DemoStatus status) {
        Demo demo = mock(Demo.class);
        lenient().when(demo.getId()).thenReturn(id);
        lenient().when(demo.getUserId()).thenReturn(userId);
        when(demo.getStatus()).thenReturn(status);
        return demo;
    }

    private static User author(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getDisplayNickname()).thenReturn("곰");
        when(user.isWithdrawn()).thenReturn(false);
        when(user.getProfileImage()).thenReturn(null);
        when(user.getTechStacks()).thenReturn(List.of());
        return user;
    }
}
