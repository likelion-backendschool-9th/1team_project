package com.dipsource.repository.user;

import com.dipsource.entity.user.User;
import com.dipsource.global.common.LikePattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * 사람 검색 전용 조회. 검색 방식을 바꿀 때는 이 파일만 바꾼다.
 */
public interface UserSearchRepository extends Repository<User, Long> {

    /** 1단계: LOWER() + LIKE 부분 일치 */
    default Page<User> searchByKeyword(String keyword, Pageable pageable) {
        return searchByLikePattern(LikePattern.contains(keyword), pageable);
    }

    /**
     * 활동 중(ACTIVE)인 회원 중 닉네임, 자기소개, 기술 스택에 패턴이 들어 있는 회원. 최근 가입순.
     * 정지·탈퇴 회원은 사람 검색에 나오지 않는다.
     * 기술 스택은 jsonb 배열을 문자열로 바꿔 비교한다(1단계의 임시 방식).
     */
    @Query(value = """
            SELECT u FROM User u
            WHERE u.status = com.dipsource.entity.user.UserStatus.ACTIVE
              AND (LOWER(u.nickname) LIKE LOWER(:pattern) ESCAPE '\\'
                   OR LOWER(u.bio) LIKE LOWER(:pattern) ESCAPE '\\'
                   OR LOWER(CAST(u.techStacks AS String)) LIKE LOWER(:pattern) ESCAPE '\\')
            ORDER BY u.createdAt DESC, u.id DESC
            """,
            countQuery = """
            SELECT COUNT(u) FROM User u
            WHERE u.status = com.dipsource.entity.user.UserStatus.ACTIVE
              AND (LOWER(u.nickname) LIKE LOWER(:pattern) ESCAPE '\\'
                   OR LOWER(u.bio) LIKE LOWER(:pattern) ESCAPE '\\'
                   OR LOWER(CAST(u.techStacks AS String)) LIKE LOWER(:pattern) ESCAPE '\\')
            """)
    Page<User> searchByLikePattern(@Param("pattern") String pattern, Pageable pageable);
}
