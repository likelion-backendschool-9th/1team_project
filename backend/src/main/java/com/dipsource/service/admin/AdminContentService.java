package com.dipsource.service.admin;

import com.dipsource.dto.admin.AdminContentResponse;
import com.dipsource.global.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AdminContentService {

    /** 관리자 피드 목록. q는 본문·작성자 닉네임. */
    PageResponse<AdminContentResponse> listFeeds(String q, Pageable pageable);

    /** 관리자 피드 삭제. 소프트 삭제 후 작업 기록을 남긴다. */
    void deleteFeed(Long adminId, Long feedId);

    /** 관리자 블로그 글 목록. q는 제목·본문·작성자 닉네임. */
    PageResponse<AdminContentResponse> listPosts(String q, Pageable pageable);

    /** 관리자 블로그 글 삭제. 소프트 삭제 후 작업 기록을 남긴다. */
    void deletePost(Long adminId, Long postId);

    /** 관리자 피드 댓글 목록. q는 본문·작성자 닉네임. */
    PageResponse<AdminContentResponse> listComments(String q, Pageable pageable);

    /** 관리자 피드 댓글 삭제. 소프트 삭제 후 작업 기록을 남긴다. */
    void deleteComment(Long adminId, Long commentId);

    /** 관리자 데모 목록. q는 제목·설명·작성자 닉네임. */
    PageResponse<AdminContentResponse> listDemos(String q, Pageable pageable);

    /** 관리자 데모 삭제. 소프트 삭제 후 작업 기록을 남긴다. */
    void deleteDemo(Long adminId, Long demoId);
}
