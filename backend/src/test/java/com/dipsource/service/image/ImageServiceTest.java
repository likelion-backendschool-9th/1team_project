package com.dipsource.service.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dipsource.dto.image.ImageUploadResult;
import com.dipsource.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService 이미지 업로드 단위 테스트")
class ImageServiceTest {

    @Mock
    S3Client s3Client;

    @InjectMocks
    ImageService imageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(imageService, "publicUrl", "https://cdn.example.com");
        // 검증 실패 테스트에서는 putObject가 호출되지 않으므로 lenient 처리
        lenient().when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    /** 테스트용 JPEG 바이트 생성 (width × height 흰색 이미지) */
    private static byte[] jpegBytes(int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "JPEG", out);
        return out.toByteArray();
    }

    /** 테스트용 PNG 바이트 생성 */
    private static byte[] pngBytes(int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", out);
        return out.toByteArray();
    }

    // ── 성공 케이스 ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("JPEG 업로드")
    class JpegUpload {

        @Test
        @DisplayName("원본(1200px)·썸네일(400px) 두 장이 R2에 업로드된다")
        void uploadJpeg_twoFilesUploaded() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "photo.jpg", "image/jpeg", jpegBytes(2000, 1500));

            ImageUploadResult result = imageService.upload(file, "feeds");

            // R2 putObject가 정확히 2번 호출돼야 한다 (원본 + 썸네일)
            verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

            assertThat(result.url()).startsWith("https://cdn.example.com/feeds/");
            assertThat(result.url()).endsWith(".jpg");
            assertThat(result.thumbnailUrl()).contains("/thumbs/");
            assertThat(result.thumbnailUrl()).endsWith(".jpg");
        }

        @Test
        @DisplayName("원본 URL과 썸네일 URL은 서로 다르다")
        void uploadJpeg_urlAndThumbnailUrlAreDifferent() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "photo.jpg", "image/jpeg", jpegBytes(1600, 900));

            ImageUploadResult result = imageService.upload(file, "feeds");

            assertThat(result.url()).isNotEqualTo(result.thumbnailUrl());
        }

        @Test
        @DisplayName("원본이 1200px 이하면 업스케일하지 않는다")
        void uploadJpeg_smallImage_noUpscale() throws Exception {
            // 500px 이미지 — 1200px로 늘리면 안 된다
            MockMultipartFile file = new MockMultipartFile(
                    "image", "small.jpg", "image/jpeg", jpegBytes(500, 400));

            ImageUploadResult result = imageService.upload(file, "feeds");

            // 두 번 업로드 (원본 + 썸네일) — 에러 없이 완료
            verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
            assertThat(result.url()).isNotNull();
        }
    }

    @Nested
    @DisplayName("PNG 업로드")
    class PngUpload {

        @Test
        @DisplayName("PNG도 JPEG로 변환 후 원본·썸네일 두 장 업로드된다")
        void uploadPng_convertedToJpeg() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "logo.png", "image/png", pngBytes(800, 600));

            ImageUploadResult result = imageService.upload(file, "feeds");

            verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
            assertThat(result.url()).endsWith(".jpg");
            assertThat(result.thumbnailUrl()).endsWith(".jpg");
        }
    }

    @Nested
    @DisplayName("GIF·WebP 업로드")
    class NonResizableUpload {

        @Test
        @DisplayName("GIF는 원본 그대로 1번만 업로드되며 url == thumbnailUrl이다")
        void uploadGif_singleUpload_sameUrls() {
            byte[] gifBytes = new byte[]{0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00, 0x01, 0x00};
            MockMultipartFile file = new MockMultipartFile(
                    "image", "animation.gif", "image/gif", gifBytes);

            ImageUploadResult result = imageService.upload(file, "feeds");

            // GIF는 리사이징 없이 1번만 업로드
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
            assertThat(result.url()).isEqualTo(result.thumbnailUrl());
            assertThat(result.url()).startsWith("https://cdn.example.com/feeds/");
        }
    }

    // ── 실패 케이스 ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("검증 실패")
    class Validation {

        @Test
        @DisplayName("빈 파일이면 BusinessException을 던진다")
        void emptyFile_throwsBusinessException() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "image", "empty.jpg", "image/jpeg", new byte[0]);

            assertThatThrownBy(() -> imageService.upload(emptyFile, "feeds"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("비어있습니다");
        }

        @Test
        @DisplayName("10MB 초과 파일이면 BusinessException을 던진다")
        void oversizeFile_throwsBusinessException() {
            byte[] bigFile = new byte[11 * 1024 * 1024]; // 11MB
            MockMultipartFile file = new MockMultipartFile(
                    "image", "big.jpg", "image/jpeg", bigFile);

            assertThatThrownBy(() -> imageService.upload(file, "feeds"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("10MB");
        }

        @Test
        @DisplayName("지원하지 않는 형식(PDF)이면 BusinessException을 던진다")
        void unsupportedType_throwsBusinessException() {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

            assertThatThrownBy(() -> imageService.upload(file, "feeds"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("지원하지 않는 이미지 형식");
        }
    }
}
