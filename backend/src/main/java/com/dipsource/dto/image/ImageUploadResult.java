package com.dipsource.dto.image;

/** 이미지 업로드 결과. 원본 URL과 썸네일 URL을 함께 반환한다. */
public record ImageUploadResult(
        /** 원본 이미지 URL (최대 1200px 리사이즈) */
        String url,
        /** 썸네일 URL (최대 400px 리사이즈). JPEG·PNG 외에는 원본 URL과 동일 */
        String thumbnailUrl
) {}
