package com.dipsource.service.demo.storage;

import com.dipsource.global.config.DemoProperties;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/** Cloudflare R2(S3 호환 API) 구현. */
@Component
@ConditionalOnProperty(name = "r2.enabled", havingValue = "true")
@RequiredArgsConstructor
public class R2DemoStorage implements DemoStorage {

    /** DeleteObjects 한 번에 지울 수 있는 최대 개수 */
    private static final int DELETE_BATCH = 1000;

    private final S3Client s3Client;
    private final DemoProperties properties;

    @Override
    public void putFile(String key, Path file, String contentType) {
        s3Client.putObject(put(key, contentType), RequestBody.fromFile(file));
    }

    @Override
    public void putBytes(String key, byte[] bytes, String contentType) {
        s3Client.putObject(put(key, contentType), RequestBody.fromBytes(bytes));
    }

    @Override
    public Optional<byte[]> getBytes(String key) {
        try {
            return Optional.of(s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(properties.bucket()).key(key).build()).asByteArray());
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(properties.bucket()).key(key).build());
    }

    @Override
    public void deletePrefix(String prefix) {
        List<ObjectIdentifier> keys = list(prefix).stream()
                .map(object -> ObjectIdentifier.builder().key(object.key()).build())
                .toList();
        for (int from = 0; from < keys.size(); from += DELETE_BATCH) {
            List<ObjectIdentifier> batch = keys.subList(from, Math.min(from + DELETE_BATCH, keys.size()));
            s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(properties.bucket())
                    .delete(Delete.builder().objects(batch).quiet(true).build())
                    .build());
        }
    }

    @Override
    public StorageSummary summarize(String prefix) {
        List<S3Object> objects = list(prefix);
        return new StorageSummary(objects.size(), objects.stream().mapToLong(S3Object::size).sum());
    }

    private List<S3Object> list(String prefix) {
        return s3Client.listObjectsV2Paginator(ListObjectsV2Request.builder()
                        .bucket(properties.bucket()).prefix(prefix).build())
                .contents().stream().toList();
    }

    private PutObjectRequest put(String key, String contentType) {
        return PutObjectRequest.builder().bucket(properties.bucket()).key(key).contentType(contentType).build();
    }
}
