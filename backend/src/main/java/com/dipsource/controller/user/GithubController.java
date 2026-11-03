package com.dipsource.controller.user;

import com.dipsource.dto.user.GithubContributionsResponse;
import com.dipsource.dto.user.GithubLanguageResponse;
import com.dipsource.dto.user.GithubRepoResponse;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.service.github.GithubService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 미니홈피 GitHub 공개 정보 API.
 * WBS: users/{userId}/github/contributions|repos|languages
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/github")
@RequiredArgsConstructor
public class GithubController {

    private final GithubService githubService;

    /** 깃허브 잔디(기여 캘린더) */
    @GetMapping("/contributions")
    public ApiResponse<GithubContributionsResponse> contributions(@PathVariable Long userId) {
        return ApiResponse.of(githubService.getContributions(userId));
    }

    /** 대표 레포 (star 상위) */
    @GetMapping("/repos")
    public ApiResponse<List<GithubRepoResponse>> repos(@PathVariable Long userId) {
        return ApiResponse.of(githubService.getRepos(userId));
    }

    /** 언어 사용 비율 */
    @GetMapping("/languages")
    public ApiResponse<List<GithubLanguageResponse>> languages(@PathVariable Long userId) {
        return ApiResponse.of(githubService.getLanguages(userId));
    }
}
