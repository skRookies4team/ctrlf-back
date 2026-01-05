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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ctrlf.infra.s3.service.S3Service;
import java.util.Locale;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

@RestController
@RequestMapping("/api-infra/files/presign")
@RequiredArgsConstructor
@Tag(name = "Infra - S3", description = "S3 Presigned URL API")
public class S3Controller {

    private final S3Service presignService;

    @PostMapping("/upload")
    @Operation(summary = "Presigned Upload URL 발급")
    public ResponseEntity<UploadResponse> presignUpload(@Valid @RequestBody S3UploadRequest req) {
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
        // 다운로드용 사인드 URL 생성 (설정 파일의 downloadTtlSeconds 사용)
        URL getUrl = presignService.presignDownload(req.getFileUrl());
        return ResponseEntity.ok(new DownloadResponse(getUrl.toString()));
    }

    @PostMapping(value = "/upload/put", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "서버가 Presigned URL로 업로드(프록시)",
        parameters = {
            @Parameter(name = "url", in = ParameterIn.QUERY, description = "Presigned PUT URL", required = true)
        }
    )
    public ResponseEntity<String> uploadViaPresigned(
        @RequestParam("url") String presignedUrl,
        @Parameter(description = "업로드 파일", required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(type = "string", format = "binary")))
        @RequestPart("file") MultipartFile file
    ) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(presignedUrl))
            .header("Content-Type", contentType)
            .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
            .build();
        HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return ResponseEntity.ok("uploaded");
        }
        return ResponseEntity.status(resp.statusCode())
            .body(new String(resp.body(), StandardCharsets.UTF_8));
    }
}

