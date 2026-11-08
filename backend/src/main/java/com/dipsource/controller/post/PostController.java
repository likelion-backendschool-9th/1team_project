package com.dipsource.controller.post;

import com.dipsource.dto.post.PostDetailResponse;
import com.dipsource.dto.post.PostIdResponse;
import com.dipsource.dto.post.PostSummaryResponse;
import com.dipsource.dto.post.WritePostRequest;
import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.common.PageResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.post.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 블로그 글 CRUD. 작성자는 JWT에서만 가져온다. */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /** 전체 블로그 목록. ?category=&q=&sort=latest|popular (비로그인 가능) */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> posts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "latest") String sort,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of(postService.getPosts(category, q, sort, viewerId(principal), pageable)));
    }

    /** 한 회원의 블로그 글 목록 (비로그인 가능) */
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> userPosts(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of(postService.getUserPosts(userId, viewerId(principal), pageable)));
    }

    /** 글 상세 (비로그인 가능, 잠긴 유료 글은 앞부분만) */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> post(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(ApiResponse.of(postService.getPost(postId, viewerId(principal))));
    }

    /** 글 작성 */
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<PostIdResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WritePostRequest request
    ) {
        PostIdResponse response = postService.create(requireUserId(principal), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    /** 글 수정 (본인만) */
    @PutMapping("/posts/{postId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @Valid @RequestBody WritePostRequest request
    ) {
        postService.update(requireUserId(principal), postId, request);
        return ResponseEntity.noContent().build();
    }

    /** 글 삭제 (본인만, soft delete) */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId
    ) {
        postService.delete(requireUserId(principal), postId);
        return ResponseEntity.noContent().build();
    }

    /** 좋아요. 이미 눌렀어도 204 (멱등). 삭제된 글이면 404 */
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Void> like(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId
    ) {
        postService.like(requireUserId(principal), postId);
        return ResponseEntity.noContent().build();
    }

    /** 좋아요 취소. 안 눌렀어도 204 (멱등). 삭제된 글이면 404 */
    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<Void> unlike(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId
    ) {
        postService.unlike(requireUserId(principal), postId);
        return ResponseEntity.noContent().build();
    }

    private static Long viewerId(UserPrincipal principal) {
        return principal == null ? null : principal.getUserId();
    }

    private static Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return principal.getUserId();
    }
}
