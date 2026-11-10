package com.dipsource.dto.user;

/**
 * 미니홈피 대표 레포 응답.
 *
 * @param url GitHub 레포 주소 (예: https://github.com/owner/repo)
 */
public record GithubRepoResponse(String name, long stars, String url) {
}