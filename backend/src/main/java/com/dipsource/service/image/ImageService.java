package com.dipsource.service.image;

import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * 이미지 업로드/삭제 서비스
 * Cloudflare R2에 저장하고 공개 URL을 반환함
 */
@Service
@ConditionalOnProperty(name = "r2.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ImageService {

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.public-url}")
    private String publicUrl;

    /**
     * 이미지 업로드
     * 파일 검증 후 R2에 저장하고 공개 URL 반환
     *
     * @param file 업로드할 이미지 파일
     * @param folder 저장 폴더 (예: "feeds", "profiles")
     * @return 공개 접근 가능한 이미지 URL
     */
    public String upload(MultipartFile file, String folder) {
        validate(file);

        String key = folder + "/" + UUID.randomUUID() + getExtension(file.getOriginalFilename());

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미지 업로드에 실패했습니다");
        }

        return publicUrl + "/" + key;
    }

    /**
     * 이미지 삭제
     * URL에서 키를 추출해 R2에서 삭제함
     *
     * @param imageUrl 삭제할 이미지의 공개 URL
     */
    public void delete(String imageUrl) {
        String key = imageUrl.replace(publicUrl + "/", "");
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }

    /** 파일 타입, 크기 검증 */
    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "파일이 비어있습니다");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "파일 크기는 10MB 이하만 가능합니다");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 이미지 형식입니다 (jpeg, png, gif, webp만 가능)");
        }
    }

    /** 파일명에서 확장자 추출 */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
