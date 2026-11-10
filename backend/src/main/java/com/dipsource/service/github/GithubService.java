package com.dipsource.service.github;

import com.dipsource.dto.user.GithubContributionsResponse;
import com.dipsource.dto.user.GithubLanguageResponse;
import com.dipsource.dto.user.GithubRepoResponse;
import java.util.List;

/** 미니홈피용 GitHub 공개 프로필 집계. */
public interface GithubService {

    /** 깃허브 잔디(기여 캘린더). 연동 계정이 없거나 조회에 실패하면 빈 결과. */
    GithubContributionsResponse getContributions(Long userId);

    /** 대표 레포 (star 상위). */
    List<GithubRepoResponse> getRepos(Long userId);

    /** 언어 사용 비율. */
    List<GithubLanguageResponse> getLanguages(Long userId);
}
