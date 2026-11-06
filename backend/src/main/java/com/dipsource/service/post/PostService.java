package com.dipsource.service.post;

import com.dipsource.dto.post.PostDetailResponse;
import com.dipsource.dto.post.PostIdResponse;
import com.dipsource.dto.post.PostSummaryResponse;
import com.dipsource.dto.post.WritePostRequest;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 블로그 글 CRUD. */
public interface PostService {

    /**
     * 전체 블로그 목록. category·q는 비어 있으면 거르지 않는다. sort: latest / popular (recommended는 아직 latest와 같다).
     * viewerId는 liked 표시용이며 비로그인이면 null.
     */
    PageResponse<PostSummaryResponse> getPosts(String category, String q, String sort, Long viewerId, Pageable pageable);

    /** 한 회원의 블로그 글 목록 (최신순). 삭제한 글은 빠진다 */
    PageResponse<PostSummaryResponse> getUserPosts(Long userId, Long viewerId, Pageable pageable);

    /** 글 상세. 조회수를 올린다(작성자 본인 제외). 잠긴 유료 글이면 앞부분만 준다. viewerId는 비로그인이면 null */
    PostDetailResponse getPost(Long postId, Long viewerId);

    PostIdResponse create(Long userId, WritePostRequest request);

    /** 작성자 본인만 */
    void update(Long userId, Long postId, WritePostRequest request);

    /** 작성자 본인만. soft delete. 삭제한 글은 목록·검색에서 빠지고 좋아요·댓글도 막힌다 */
    void delete(Long userId, Long postId);

    /** 좋아요. 이미 눌렀어도 오류 없이 넘어간다 (멱등). 공개 글만 */
    void like(Long userId, Long postId);

    /** 좋아요 취소. 누르지 않았어도 오류 없이 넘어간다 (멱등). 공개 글만 */
    void unlike(Long userId, Long postId);
}
