package com.dipsource.service.search.sync;

import com.dipsource.dto.search.SearchTargetChangedEvent;
import com.dipsource.dto.search.SearchType;
import com.dipsource.entity.demo.Demo;
import com.dipsource.entity.demo.DemoTag;
import com.dipsource.entity.feed.Feed;
import com.dipsource.entity.feed.FeedTag;
import com.dipsource.entity.post.Post;
import com.dipsource.entity.post.PostTag;
import com.dipsource.entity.user.User;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * JPA로 바뀐 글·데모·피드·회원·태그 연결을 원본과 같은 트랜잭션의 색인 대기열에 넣는다.
 * 서비스마다 호출을 넣으면 새 기능(블로그 CRUD 등)에서 빠뜨리기 쉬워서 Hibernate 이벤트에서 잡는다.
 * 좋아요 수처럼 색인하지 않는 값이나 UPDATE 쿼리로 바꾼 값은 재정렬 때 PostgreSQL에서 읽으므로 대상이 아니다.
 * 벌크 쿼리로 상태를 바꾸는 코드가 생기면 여기서 잡히지 않는다. 그 경우는 매일 도는 정합성 점검이 복구한다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "search.elasticsearch", name = "enabled", havingValue = "true")
public class SearchOutboxListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    /** 색인 내용이나 공개 여부에 영향을 주는 필드. 다른 필드만 바뀌면 대기열에 넣지 않는다 */
    private static final Map<Class<?>, Set<String>> TRACKED = Map.of(
            Post.class, Set.of("title", "content", "paid", "status", "deletedAt"),
            Demo.class, Set.of("title", "description", "status", "deletedAt"),
            Feed.class, Set.of("content", "demoId", "status", "deletedAt"),
            User.class, Set.of("nickname", "bio", "techStacks", "githubUsername", "status", "deletedAt"));

    private final EntityManagerFactory entityManagerFactory;
    private final SearchOutboxRepository outbox;

    @PostConstruct
    void register() {
        EventListenerRegistry registry = entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getServiceRegistry().getService(EventListenerRegistry.class);
        registry.appendListeners(EventType.POST_INSERT, this);
        registry.appendListeners(EventType.POST_UPDATE, this);
        registry.appendListeners(EventType.POST_DELETE, this);
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        enqueue(event.getSession(), event.getEntity());
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        enqueue(event.getSession(), event.getEntity());
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        Set<String> tracked = TRACKED.get(event.getEntity().getClass());
        if (tracked == null || event.getDirtyProperties() == null) {
            return;
        }
        String[] names = event.getPersister().getPropertyNames();
        boolean relevant = Arrays.stream(event.getDirtyProperties()).anyMatch(index -> tracked.contains(names[index]));
        if (relevant) {
            enqueue(event.getSession(), event.getEntity());
        }
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    private static void enqueue(EventSource session, Object entity) {
        Target target = targetOf(entity);
        if (target == null) {
            return;
        }
        // 플러시 도중에는 새 SQL을 끼워 넣을 수 없어서 커밋 직전에 같은 커넥션으로 넣는다
        session.getActionQueue().registerProcess((BeforeTransactionCompletionProcess) completing -> completing.doWork(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO search_outbox(target_type, target_id, created_at, attempts, next_attempt_at)
                    VALUES (?, ?, now(), 0, now())
                    """)) {
                statement.setString(1, target.type().name());
                statement.setLong(2, target.id());
                statement.executeUpdate();
            }
        }));
    }

    /** 벌크 쿼리처럼 JPA 이벤트로 잡히지 않는 변경. 발행한 트랜잭션 안에서 대기열에 넣는다 */
    @EventListener
    void onChanged(SearchTargetChangedEvent event) {
        outbox.enqueue(event.type(), List.of(event.id()));
    }

    private record Target(SearchType type, long id) {
    }

    private static Target targetOf(Object entity) {
        return switch (entity) {
            case Post post -> new Target(SearchType.POST, post.getId());
            case Demo demo -> new Target(SearchType.DEMO, demo.getId());
            case Feed feed -> new Target(SearchType.FEED, feed.getId());
            case User user -> new Target(SearchType.USER, user.getId());
            case PostTag tag -> new Target(SearchType.POST, tag.getId().getPostId());
            case DemoTag tag -> new Target(SearchType.DEMO, tag.getId().getDemoId());
            case FeedTag tag -> new Target(SearchType.FEED, tag.getId().getFeedId());
            default -> null;
        };
    }
}
