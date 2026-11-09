package com.dipsource.service.post;

import com.dipsource.dto.post.PostDetailResponse;
import com.dipsource.dto.post.PostIdResponse;
import com.dipsource.dto.post.PostSummaryResponse;
import com.dipsource.dto.post.WritePostRequest;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 블로그 글 CRUD. */
public interface PostService {

    /** 전체 블로그 목록. category·q는 비어 있으면 거르지 않는다. sort: latest / popular (recommended는 아직 latest와 같다). viewerId는 비로그인이면 null */
    PageResponse<PostSummaryResponse> getPosts(String category, String q, String sort, Long viewerId, Pageable pageable);

    /** 한 회원의 블로그 글 목록 (최신순). 차단 관계면 빈 목록 */
    PageResponse<PostSummaryResponse> getUserPosts(Long userId, Long viewerId, Pageable pageable);

    /**
     * 글 상세. 조회수를 올린다(작성자 본인 제외, 로그인 회원은 같은 글을 한 시간에 한 번만).
     * 잠긴 유료 글이면 앞부분만 준다. viewerId는 비로그인이면 null
     */
    PostDetailResponse getPost(Long postId, Long viewerId);

    PostIdResponse create(Long userId, WritePostRequest request);

    /** 작성자 본인만 */
    void update(Long userId, Long postId, WritePostRequest request);

    /** 작성자 본인만. soft delete */
    void delete(Long userId, Long postId);

    /** 삭제·숨김되지 않은 이 회원의 글인지 확인한다. 미니홈피 대표 글 검증에 쓴다. */
    boolean isActivePostOf(Long postId, Long userId);

    /** 관리자 삭제. 작성자 검사 없이 소프트 삭제만 한다. */
    void deleteByAdmin(Long postId);

    /**
     * 좋아요. 이미 눌렀으면 no-op (멱등).
     * INSERT ON CONFLICT DO NOTHING으로 동시 중복을 막는다.
     */
    void like(Long userId, Long postId);

    /**
     * 좋아요 취소. 없으면 no-op (멱등).
     * DELETE 결과로 실제 삭제 여부를 판단해 race condition 방지.
     */
    void unlike(Long userId, Long postId);

    /** 북마크. 이미 했어도 오류 없이 넘어간다 (멱등). 공개 글만, 차단 관계인 글은 404 */
    void bookmark(Long userId, Long postId);

    /** 북마크 취소. 안 했어도 오류 없이 넘어간다 (멱등). 공개 글만 */
    void unbookmark(Long userId, Long postId);
}
