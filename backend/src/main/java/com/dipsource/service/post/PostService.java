package com.dipsource.service.post;

import com.dipsource.dto.post.PostDetailResponse;
import com.dipsource.dto.post.PostIdResponse;
import com.dipsource.dto.post.PostSummaryResponse;
import com.dipsource.dto.post.WritePostRequest;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

/** 블로그 글 CRUD. */
public interface PostService {

    /** 전체 블로그 목록. category·q는 비어 있으면 거르지 않는다. sort: latest / popular (recommended는 아직 latest와 같다) */
    PageResponse<PostSummaryResponse> getPosts(String category, String q, String sort, Pageable pageable);

    /** 한 회원의 블로그 글 목록 (최신순) */
    PageResponse<PostSummaryResponse> getUserPosts(Long userId, Pageable pageable);

    /** 글 상세. 조회수를 올린다(작성자 본인 제외). 잠긴 유료 글이면 앞부분만 준다. viewerId는 비로그인이면 null */
    PostDetailResponse getPost(Long postId, Long viewerId);

    PostIdResponse create(Long userId, WritePostRequest request);

    /** 작성자 본인만 */
    void update(Long userId, Long postId, WritePostRequest request);

    /** 작성자 본인만. soft delete */
    void delete(Long userId, Long postId);
}
