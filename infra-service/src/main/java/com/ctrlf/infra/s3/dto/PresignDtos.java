package com.ctrlf.infra.s3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * S3 Presign API 요청/응답 DTO 모음.
 */
public final class PresignDtos {
    private PresignDtos() {}

    /**
     * 업로드 URL 발급 요청.
     * - filename: 원본 파일명
     * - contentType: MIME 타입
     * - type: 파일 카테고리(경로 prefix)
     */
    @Getter
    @NoArgsConstructor
    public static class UploadRequest {
        @NotBlank
        private String filename;
        @NotBlank
        private String contentType;
        @NotBlank
        @Pattern(regexp = "^(image|docs|video)$", message = "type must be one of: image, docs, video")
        private String type;
    }

    /**
     * 업로드 URL 발급 응답.
     * - uploadUrl: 클라이언트가 PUT 업로드할 Presigned URL
     * - fileUrl: 업로드 완료 후 저장될 s3://bucket/key
     */
    @Getter
    @AllArgsConstructor
    public static class UploadResponse {
        private String uploadUrl;
        private String fileUrl;
    }

    /**
     * 다운로드 URL 발급 요청.
     * - fileUrl: s3://bucket/key 또는 key 문자열
     */
    @Getter
    @NoArgsConstructor
    public static class DownloadRequest {
        @NotBlank
        private String fileUrl; // s3://bucket/key
    }

    /**
     * 다운로드 URL 발급 응답.
     * - downloadUrl: GET으로 접근 가능한 Presigned URL
     */
    @Getter
    @AllArgsConstructor
    public static class DownloadResponse {
        private String downloadUrl;
    }
}

