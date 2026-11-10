package com.dipsource.controller.admin;

import com.dipsource.dto.image.ImageUploadResult;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이벤트 대표 이미지 업로드. 권한은 SecurityConfig의 /admin/** 를 따른다.
 * 받은 url을 이벤트 등록·수정 요청의 imageUrl로 보낸다.
 * ImageService는 R2를 켠 환경에만 있어서, 꺼진 환경(로컬)에서는 이유를 알려 주는 오류를 돌려준다.
 */
@RestController
@RequestMapping("/admin/events/images")
@RequiredArgsConstructor
public class AdminEventImageController {

    private static final String FOLDER = "events";

    private final ObjectProvider<ImageService> imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResult>> upload(@RequestPart("file") MultipartFile file) {
        ImageService images = imageService.getIfAvailable();
        if (images == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미지 저장소(R2)가 꺼져 있어 이미지를 올릴 수 없어요.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(images.upload(file, FOLDER)));
    }
}
