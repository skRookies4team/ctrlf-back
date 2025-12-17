package com.ctrlf.education.video.service;

import com.ctrlf.education.script.repository.EducationScriptRepository;
import com.ctrlf.education.video.dto.VideoDtos.AiProcessResponse;
import com.ctrlf.education.video.dto.VideoDtos.AiVideoResponse;
import com.ctrlf.education.video.dto.VideoDtos.JobItem;
import com.ctrlf.education.video.dto.VideoDtos.VideoCompleteCallback;
import com.ctrlf.education.video.dto.VideoDtos.VideoCompleteResponse;
import com.ctrlf.education.video.dto.VideoDtos.VideoJobRequest;
import com.ctrlf.education.video.dto.VideoDtos.VideoJobResponse;
import com.ctrlf.education.video.dto.VideoDtos.VideoJobUpdateRequest;
import com.ctrlf.education.video.dto.VideoDtos.VideoRetryResponse;
import com.ctrlf.education.video.entity.VideoGenerationJob;
import com.ctrlf.education.video.repository.VideoGenerationJobRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 영상 생성 관련 비즈니스 로직을 처리하는 서비스.
 *
 * <p>주요 기능:
 * <ul>
 *   <li>영상 생성 Job 등록/조회/수정/삭제/재시도</li>
 *   <li>전처리/임베딩/스크립트 생성 요청 (AI 서버 호출)</li>
   *   <li>영상 생성 완료 콜백 처리</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    /** Job 상태 상수 */
    private static final String STATUS_QUEUED = "QUEUED";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_FAILED = "FAILED";

    private final EducationScriptRepository scriptRepository;
    private final VideoGenerationJobRepository jobRepository;
    // private final VideoAiClient videoAiClient; // 현재 모의 처리로 사용하지 않음

    // ========================
    // 영상 생성 Job 관련 메서드
    // ========================

    /**
     * 영상 생성 Job을 등록하고 AI 서버에 요청합니다.
     *
     * @param request 영상 생성 요청 (eduId, scriptId)
     * @return Job 등록 결과 (jobId, status)
     * @throws ResponseStatusException 스크립트가 존재하지 않을 경우 404
     */
    @Transactional
    public VideoJobResponse createVideoJob(VideoJobRequest request) {
        // 스크립트 존재 확인
        if (!scriptRepository.existsById(request.scriptId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "스크립트를 찾을 수 없습니다: " + request.scriptId());
        }

        // Job 생성
        VideoGenerationJob job = new VideoGenerationJob();
        job.setEducationId(request.eduId());
        job.setScriptId(request.scriptId());
        job.setStatus(STATUS_QUEUED);
        job.setRetryCount(0);
        jobRepository.save(job);

        // AI 서버 미구현: 모의 응답으로 처리
        AiVideoResponse aiResponse = new AiVideoResponse(job.getId(), true, STATUS_QUEUED);
            job.setStatus(aiResponse.status());
            jobRepository.save(job);
        log.info("[MOCK] 영상 생성 Job 등록 처리. jobId={}, status={}", job.getId(), aiResponse.status());

        return new VideoJobResponse(job.getId(), job.getStatus());
    }

    /**
     * 실패한 영상 생성 Job을 재시도합니다.
     *
     * @param jobId 재시도할 Job ID
     * @return 재시도 결과 (jobId, status, retryCount)
     * @throws ResponseStatusException Job이 존재하지 않거나 FAILED 상태가 아닐 경우
     */
    @Transactional
    public VideoRetryResponse retryVideoJob(UUID jobId) {
        VideoGenerationJob job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job을 찾을 수 없습니다: " + jobId));

        // 상태 검증
        if (STATUS_PROCESSING.equals(job.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리 중인 Job입니다: " + jobId);
        }
        if (!STATUS_FAILED.equals(job.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FAILED 상태의 Job만 재시도할 수 있습니다. 현재 상태: " + job.getStatus());
        }

        // 재시도 카운트 증가 및 상태 변경
        int newRetryCount = (job.getRetryCount() == null ? 0 : job.getRetryCount()) + 1;
        job.setRetryCount(newRetryCount);
        job.setStatus(STATUS_QUEUED);
        job.setFailReason(null);
        jobRepository.save(job);

        // AI 서버 미구현: 모의 응답으로 처리
        AiVideoResponse aiResponse = new AiVideoResponse(jobId, true, STATUS_QUEUED);
            job.setStatus(aiResponse.status());
            jobRepository.save(job);
        log.info("[MOCK] 영상 재시도 처리. jobId={}, retryCount={}, status={}", jobId, newRetryCount, aiResponse.status());

        return new VideoRetryResponse(jobId, job.getStatus(), newRetryCount);
    }

    /**
     * AI 서버로부터 영상 생성 완료 콜백을 처리합니다.
     *
     * @param jobId    Job ID (path variable)
     * @param callback 콜백 데이터 (jobId, videoUrl, duration, status)
     * @return 저장 결과
     * @throws ResponseStatusException Job이 존재하지 않을 경우 404
     */
    @Transactional
    public VideoCompleteResponse handleVideoComplete(UUID jobId, VideoCompleteCallback callback) {
        VideoGenerationJob job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job을 찾을 수 없습니다: " + jobId));

        job.setStatus(callback.status());
        job.setGeneratedVideoUrl(callback.videoUrl());
        job.setDuration(callback.duration());
        jobRepository.save(job);

        log.info("영상 생성 완료 콜백 처리. jobId={}, status={}, videoUrl={}", 
            jobId, callback.status(), callback.videoUrl());
        return new VideoCompleteResponse(true);
    }

    // ========================
    // 전처리/임베딩/스크립트 생성 요청
    // ========================

    /**
     * 자료에 대해 전처리 + 임베딩 + 스크립트 생성을 AI 서버에 요청합니다.
     * (내부 API - 다른 서비스나 컨트롤러에서 호출)
     *
     * @param materialId 자료 ID
     * @param eduId      교육 ID
     * @param fileUrl    S3 파일 URL
     * @return AI 서버 응답
     * @throws ResponseStatusException AI 서버 요청 실패 시
     */
    public AiProcessResponse requestMaterialProcess(UUID materialId, UUID eduId, String fileUrl) {
        // AI 서버 미구현: 모의 응답으로 처리
        AiProcessResponse response = new AiProcessResponse(true, STATUS_PROCESSING);
        log.info("[MOCK] 전처리/임베딩/스크립트 생성 요청 처리. materialId={}, status={}", materialId, response.status());
            return response;
    }

    // ========================
    // 영상 생성 Job 관리 (목록/조회/수정/삭제)
    // ========================

    @Transactional(readOnly = true)
    public List<JobItem> listJobs(int page, int size) {
        Page<VideoGenerationJob> p = jobRepository.findAll(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return p.map(this::toJobItem).getContent();
    }

    @Transactional(readOnly = true)
    public JobItem getJob(UUID jobId) {
        VideoGenerationJob job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job을 찾을 수 없습니다: " + jobId));
        return toJobItem(job);
    }

    @Transactional
    public JobItem updateJob(UUID jobId, VideoJobUpdateRequest req) {
        VideoGenerationJob job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job을 찾을 수 없습니다: " + jobId));
        if (req.status() != null && !req.status().isBlank()) {
            job.setStatus(req.status());
        }
        if (req.failReason() != null) {
            job.setFailReason(req.failReason());
        }
        if (req.videoUrl() != null) {
            job.setGeneratedVideoUrl(req.videoUrl());
        }
        if (req.duration() != null) {
            job.setDuration(req.duration());
        }
        job = jobRepository.save(job);
        log.info("영상 생성 Job 업데이트. jobId={}, status={}", job.getId(), job.getStatus());
        return toJobItem(job);
    }

    @Transactional
    public void deleteJob(UUID jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job을 찾을 수 없습니다: " + jobId);
        }
        jobRepository.deleteById(jobId);
        log.error("영상 생성 Job 삭제 완료. jobId={}", jobId);
    }

    private JobItem toJobItem(VideoGenerationJob j) {
        return new JobItem(
            j.getId(),
            j.getScriptId(),
            j.getEducationId(),
            j.getStatus(),
            j.getRetryCount(),
            j.getGeneratedVideoUrl(),
            j.getDuration(),
            j.getCreatedAt() != null ? j.getCreatedAt().toString() : null,
            j.getUpdatedAt() != null ? j.getUpdatedAt().toString() : null,
            j.getFailReason()
        );
    }
}
