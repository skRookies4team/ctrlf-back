package com.ctrlf.infra.s3.service;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ctrlf.infra.s3.service.S3Service.S3Path;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * S3 Presigned URL 발급 서비스.
 * - 업로드/다운로드 URL 생성
 * - 파일 URL(s3://bucket/key) 생성
 */
@Service
public class S3Service {

    private final S3Presigner presigner;
    private final String defaultBucket;
    private final Duration ttl;

    public S3Service(
        S3Presigner presigner,
        @Value("${app.s3.bucket:}") String defaultBucket,
        @Value("${app.s3.ttlSeconds:600}") long ttlSeconds
    ) {
        this.presigner = presigner;
        this.defaultBucket = defaultBucket;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    /**
     * 업로드용 Presigned URL 생성.
     *
     * @param type 업로드 카테고리 경로 prefix
     * @param filename 원본 파일명(확장자 추출)
     * @param contentType MIME 타입
     */
    public URL presignUpload(String type, String filename, String contentType) {
        S3Path path = S3Path.buildUploadPath(defaultBucket, type, filename);
        PutObjectRequest putReq = PutObjectRequest.builder()
            .bucket(path.bucket())
            .key(path.key())
            .contentType(contentType)
            .build();
        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
            .signatureDuration(ttl)
            .putObjectRequest(putReq)
            .build();
        return presigner.presignPutObject(presignReq).url();
    }

    /**
     * 다운로드용 Presigned URL 생성.
     *
     * @param fileUrl s3://bucket/key 또는 key 문자열
     */
    public URL presignDownload(String fileUrl) {
        S3Path path = S3Path.fromUrl(fileUrl, defaultBucket);
        GetObjectRequest getReq = GetObjectRequest.builder()
            .bucket(path.bucket())
            .key(path.key())
            .build();
        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
            .signatureDuration(ttl)
            .getObjectRequest(getReq)
            .build();
        return presigner.presignGetObject(presignReq).url();
    }

    /**
     * 업로드 완료 후 저장될 S3 파일 URL 문자열 생성.
     *
     * @param type 경로 prefix
     * @param filename 원본 파일명
     * @return s3://bucket/key
     */
    public String buildFileUrl(String type, String filename) {
        S3Path path = S3Path.buildUploadPath(defaultBucket, type, filename);
        return "s3://" + path.bucket() + "/" + path.key();
    }

    /**
     * 내부 S3 경로 표현 및 파서.
     */
    record S3Path(String bucket, String key) {
        /**
         * s3 URL 또는 key를 파싱하여 bucket/key로 변환.
         */
        static S3Path fromUrl(String url, String fallbackBucket) {
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException("fileUrl required");
            }
            if (url.startsWith("s3://")) {
                String rest = url.substring(5);
                int slash = rest.indexOf('/');
                if (slash < 1) throw new IllegalArgumentException("invalid s3 url");
                String bucket = rest.substring(0, slash);
                String key = rest.substring(slash + 1);
                return new S3Path(bucket, key);
            }
            // treat as key only
            if (fallbackBucket == null || fallbackBucket.isBlank()) {
                throw new IllegalArgumentException("bucket required");
            }
            return new S3Path(fallbackBucket, url);
        }

        /**
         * 업로드 대상 키 경로를 생성.
         * - {type}/{uuid}.{ext}
         */
        static S3Path buildUploadPath(String defaultBucket, String type, String filename) {
            if (defaultBucket == null || defaultBucket.isBlank()) {
                throw new IllegalArgumentException("bucket not configured");
            }
            String safeType = (type == null ? "misc" : type.trim()).replaceAll("[^a-zA-Z0-9_\\-/]", "_");
            String ext = "";
            String base = UUID.randomUUID().toString();
            if (filename != null) {
                int dot = filename.lastIndexOf('.');
                if (dot > -1 && dot < filename.length() - 1) {
                    ext = filename.substring(dot);
                }
            }
            String key = safeType + "/" + base + ext;
            return new S3Path(defaultBucket, key);
        }
    }
}

