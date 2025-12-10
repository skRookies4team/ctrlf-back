package com.ctrlf.infra.s3.controller;

import static com.ctrlf.infra.s3.dto.PresignDtos.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ctrlf.infra.s3.service.S3Service;
import java.util.Locale;

@RestController
@RequestMapping("/infra/files/presign")
@RequiredArgsConstructor
@Tag(name = "Infra - S3", description = "S3 Presigned URL API")
public class S3Controller {

    private final S3Service presignService;

    @PostMapping("/upload")
    @Operation(summary = "Presigned Upload URL 발급")
    public ResponseEntity<UploadResponse> presignUpload(@Valid @RequestBody UploadRequest req) {
        // 업로드용 사인드 URL 생성
        String type = req.getType().trim().toLowerCase(Locale.ROOT);
        URL putUrl = presignService.presignUpload(type, req.getFilename(), req.getContentType());
        // 프론트가 업로드 후 보관할 S3 주소(s3://bucket/key)
        String fileUrl = presignService.buildFileUrl(type, req.getFilename());
        return ResponseEntity.ok(new UploadResponse(putUrl.toString(), fileUrl));
    }

    @PostMapping("/download")
    @Operation(summary = "Presigned GET URL 발급")
    public ResponseEntity<DownloadResponse> presignDownload(@Valid @RequestBody DownloadRequest req) {
        // 다운로드용 사인드 URL 생성
        URL getUrl = presignService.presignDownload(req.getFileUrl());
        return ResponseEntity.ok(new DownloadResponse(getUrl.toString()));
    }
}

