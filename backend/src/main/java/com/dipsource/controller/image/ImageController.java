package com.dipsource.controller.image;

import com.dipsource.global.auth.UserPrincipal;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.image.ImageService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 업로드 컨트롤러
 *
 * POST /api/v1/images — 이미지 업로드 (인증 필요, 여러 장 가능)
 */
@RestController
@RequestMapping("/api/v1/images")
@ConditionalOnProperty(name = "r2.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ImageController {

    private static final int MAX_FILE_COUNT = 5;

    private final ImageService imageService;

    /** 이미지 업로드. 최대 5장. 업로드된 URL 목록 반환 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<String>>> upload(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("files") List<MultipartFile> files
    ) {
        if (files.size() > MAX_FILE_COUNT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미지는 최대 " + MAX_FILE_COUNT + "장까지 업로드 가능합니다");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(imageService.upload(file, "feeds"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(urls));
    }
}
