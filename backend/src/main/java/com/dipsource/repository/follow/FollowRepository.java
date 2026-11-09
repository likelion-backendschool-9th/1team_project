package com.dipsource.repository.follow;

import com.dipsource.entity.follow.Follow;
import com.dipsource.entity.follow.FollowId;
import com.dipsource.entity.user.enums.UserStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    /** 내가 팔로잉하는 사람들의 ID 목록 */
    @Query("SELECT f.id.followingId FROM Follow f WHERE f.id.followerId = :userId")
    List<Long> findFollowingIdsByUserId(@Param("userId") Long userId);

    boolean existsByIdFollowerIdAndIdFollowingId(Long followerId, Long followingId);

    /**
     * 내가 팔로우하는 회원 ID. 최근에 팔로우한 순, 해당 상태의 회원만.
     * nicknamePattern은 소문자 LIKE 패턴이다. 검색하지 않으면 "%".
     */
    @Query(value = """
            SELECT f.id.followingId FROM Follow f JOIN User u ON u.id = f.id.followingId
            WHERE f.id.followerId = :userId AND u.status = :status
              AND LOWER(u.nickname) LIKE :nicknamePattern ESCAPE '\\'
            ORDER BY f.createdAt DESC, f.id.followingId DESC
            """,
            countQuery = """
            SELECT COUNT(f) FROM Follow f JOIN User u ON u.id = f.id.followingId
            WHERE f.id.followerId = :userId AND u.status = :status
              AND LOWER(u.nickname) LIKE :nicknamePattern ESCAPE '\\'
            """)
    Page<Long> findFollowingIds(
            @Param("userId") Long userId,
            @Param("status") UserStatus status,
            @Param("nicknamePattern") String nicknamePattern,
            Pageable pageable);

    /**
     * 나를 팔로우하는 회원 ID. 최근에 팔로우한 순, 해당 상태의 회원만.
     * nicknamePattern은 소문자 LIKE 패턴이다. 검색하지 않으면 "%".
     */
    @Query(value = """
            SELECT f.id.followerId FROM Follow f JOIN User u ON u.id = f.id.followerId
            WHERE f.id.followingId = :userId AND u.status = :status
              AND LOWER(u.nickname) LIKE :nicknamePattern ESCAPE '\\'
            ORDER BY f.createdAt DESC, f.id.followerId DESC
            """,
            countQuery = """
            SELECT COUNT(f) FROM Follow f JOIN User u ON u.id = f.id.followerId
            WHERE f.id.followingId = :userId AND u.status = :status
              AND LOWER(u.nickname) LIKE :nicknamePattern ESCAPE '\\'
            """)
    Page<Long> findFollowerIds(
            @Param("userId") Long userId,
            @Param("status") UserStatus status,
            @Param("nicknamePattern") String nicknamePattern,
            Pageable pageable);

    /** 나를 팔로우하는 사람 수 (해당 상태의 회원만. 팔로워 목록 전체 수와 같은 기준) */
    @Query("""
            SELECT COUNT(f) FROM Follow f JOIN User u ON u.id = f.id.followerId
            WHERE f.id.followingId = :userId AND u.status = :status
            """)
    long countFollowers(@Param("userId") Long userId, @Param("status") UserStatus status);

    /** 내가 팔로우하는 사람 수 (해당 상태의 회원만. 팔로잉 목록 전체 수와 같은 기준) */
    @Query("""
            SELECT COUNT(f) FROM Follow f JOIN User u ON u.id = f.id.followingId
            WHERE f.id.followerId = :userId AND u.status = :status
            """)
    long countFollowings(@Param("userId") Long userId, @Param("status") UserStatus status);

    /** targetIds 가운데 내가 팔로우 중인 회원 ID */
    @Query("SELECT f.id.followingId FROM Follow f WHERE f.id.followerId = :userId AND f.id.followingId IN :targetIds")
    List<Long> findFollowingIdsIn(@Param("userId") Long userId, @Param("targetIds") Collection<Long> targetIds);

    /** targetIds 가운데 내가 팔로우 중이고 해당 상태인 회원 ID */
    @Query("""
            SELECT f.id.followingId FROM Follow f JOIN User u ON u.id = f.id.followingId
            WHERE f.id.followerId = :userId AND f.id.followingId IN :targetIds AND u.status = :status
            """)
    List<Long> findFollowingIdsInWithStatus(
            @Param("userId") Long userId,
            @Param("targetIds") Collection<Long> targetIds,
            @Param("status") UserStatus status);

    /** targetIds 가운데 나를 팔로우 중인 회원 ID */
    @Query("SELECT f.id.followerId FROM Follow f WHERE f.id.followingId = :userId AND f.id.followerId IN :targetIds")
    List<Long> findFollowerIdsIn(@Param("userId") Long userId, @Param("targetIds") Collection<Long> targetIds);

    /**
     * 팔로우 저장. 이미 팔로우 중이면 무시한다.
     * @return 저장된 행 수 (0이면 이미 팔로우 중, 1이면 새로 팔로우)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO follows (follower_id, following_id, created_at)
            VALUES (:followerId, :followingId, NOW())
            ON CONFLICT (follower_id, following_id) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 팔로우 삭제. 팔로우 중이 아니면 아무것도 지우지 않는다.
     * @return 삭제된 행 수 (0이면 원래 팔로우 안 함, 1이면 언팔로우)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            DELETE FROM follows
            WHERE follower_id = :followerId AND following_id = :followingId
            """, nativeQuery = true)
    int deleteByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /** 후보마다 내 팔로잉 중 몇 명이 그 사람을 팔로우하는지. 요청 하나에 쿼리 하나다 */
    @Query(value = """
            SELECT f.following_id AS userId, count(*) AS mutualCount
            FROM follows f
            WHERE f.follower_id IN (SELECT mine.following_id FROM follows mine WHERE mine.follower_id = :viewerId)
              AND f.following_id IN (:candidateIds)
            GROUP BY f.following_id
            """, nativeQuery = true)
    List<MutualFollowCount> countMutualFollows(@Param("viewerId") Long viewerId, @Param("candidateIds") Collection<Long> candidateIds);

    interface MutualFollowCount {
        Long getUserId();

        Long getMutualCount();
    }
}
