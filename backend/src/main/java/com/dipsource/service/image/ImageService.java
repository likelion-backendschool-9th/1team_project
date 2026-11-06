package com.dipsource.service.image;

import com.dipsource.dto.image.ImageUploadResult;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
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

    /** 리사이징·썸네일 생성을 적용하는 타입 */
    private static final Set<String> RESIZABLE_TYPES = Set.of("image/jpeg", "image/png");
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_WIDTH = 1200;
    private static final int THUMB_WIDTH = 400;
    private static final double OUTPUT_QUALITY = 0.85;

    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.public-url}")
    private String publicUrl;

    /**
     * 이미지 업로드.
     * JPEG·PNG는 최대 1200px로 리사이즈하고 400px 썸네일을 생성한다.
     * GIF·WebP는 원본 그대로 업로드하며 thumbnailUrl은 url과 동일하다.
     *
     * @param file   업로드할 이미지 파일
     * @param folder 저장 폴더 (예: "feeds", "profiles")
     * @return 원본 URL과 썸네일 URL
     */
    public ImageUploadResult upload(MultipartFile file, String folder) {
        validate(file);

        String uuid = UUID.randomUUID().toString();
        String contentType = file.getContentType();

        if (RESIZABLE_TYPES.contains(contentType)) {
            return uploadResized(file, folder, uuid);
        }
        // GIF·WebP — 원본 그대로 업로드
        String ext = getExtension(file.getOriginalFilename());
        String key = folder + "/" + uuid + ext;
        try {
            putToR2(key, file.getBytes(), contentType);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미지 업로드에 실패했습니다");
        }
        String url = publicUrl + "/" + key;
        return new ImageUploadResult(url, url);
    }

    /**
     * 이미지 삭제. JPEG·PNG인 경우 썸네일도 함께 삭제한다.
     *
     * @param imageUrl 삭제할 이미지의 공개 URL
     */
    public void delete(String imageUrl) {
        String key = toKey(imageUrl);
        deleteKey(key);
        // thumbs/ 폴더에 썸네일이 있으면 함께 삭제
        String thumbKey = toThumbKey(key);
        if (!thumbKey.equals(key)) {
            deleteKey(thumbKey);
        }
    }

    // ── private ──────────────────────────────────────────────────────────────

    /** JPEG·PNG 업로드: 1200px 원본 + 400px 썸네일 */
    private ImageUploadResult uploadResized(MultipartFile file, String folder, String uuid) {
        BufferedImage original;
        try {
            original = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미지를 읽을 수 없습니다");
        }
        if (original == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "지원하지 않는 이미지 형식입니다");
        }

        byte[] mainBytes = resizeToJpeg(original, MAX_WIDTH);
        byte[] thumbBytes = resizeToJpeg(original, THUMB_WIDTH);

        String mainKey = folder + "/" + uuid + ".jpg";
        String thumbKey = folder + "/thumbs/" + uuid + ".jpg";

        putToR2(mainKey, mainBytes, "image/jpeg");
        putToR2(thumbKey, thumbBytes, "image/jpeg");

        return new ImageUploadResult(publicUrl + "/" + mainKey, publicUrl + "/" + thumbKey);
    }

    /**
     * 이미지를 JPEG로 리사이즈한다.
     * 원본이 maxWidth보다 작으면 원본 크기를 유지한다.
     */
    private byte[] resizeToJpeg(BufferedImage original, int maxWidth) {
        int targetWidth = Math.min(maxWidth, original.getWidth());
        int targetHeight = (int) (original.getHeight() * ((double) targetWidth / original.getWidth()));
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Thumbnails.of(original)
                    .size(targetWidth, targetHeight)
                    .outputFormat("JPEG")
                    .outputQuality(OUTPUT_QUALITY)
                    .toOutputStream(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미지 처리에 실패했습니다");
        }
    }

    private void putToR2(String key, byte[] bytes, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(bytes)
        );
    }

    private void deleteKey(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }

    /** URL → R2 키 변환 */
    private String toKey(String imageUrl) {
        return imageUrl.replace(publicUrl + "/", "");
    }

    /**
     * 원본 키 → 썸네일 키 변환.
     * 예: feeds/abc.jpg → feeds/thumbs/abc.jpg
     * thumbs/ 폴더에 없는 경우(GIF·WebP 등) 원본 키를 그대로 반환한다.
     */
    private String toThumbKey(String key) {
        int lastSlash = key.lastIndexOf('/');
        if (lastSlash < 0) return key;
        String dir = key.substring(0, lastSlash);
        String filename = key.substring(lastSlash + 1);
        return dir + "/thumbs/" + filename;
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
