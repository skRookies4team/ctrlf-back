package com.ctrlf.education.service;

import com.ctrlf.education.client.VideoAiClient;
import com.ctrlf.education.dto.VideoDtos.AiProcessResponse;
import com.ctrlf.education.dto.VideoDtos.AiVideoResponse;
import com.ctrlf.education.dto.VideoDtos.MaterialProcessRequest;
import com.ctrlf.education.dto.VideoDtos.ScriptCompleteCallback;
import com.ctrlf.education.dto.VideoDtos.ScriptCompleteResponse;
import com.ctrlf.education.dto.VideoDtos.ScriptResponse;
import com.ctrlf.education.dto.VideoDtos.ScriptUpdateRequest;
import com.ctrlf.education.dto.VideoDtos.ScriptUpdateResponse;
import com.ctrlf.education.dto.VideoDtos.VideoCompleteCallback;
import com.ctrlf.education.dto.VideoDtos.VideoCompleteResponse;
import com.ctrlf.education.dto.VideoDtos.VideoJobRequest;
import com.ctrlf.education.dto.VideoDtos.VideoJobResponse;
import com.ctrlf.education.dto.VideoDtos.VideoRetryRequest;
import com.ctrlf.education.dto.VideoDtos.VideoRetryResponse;
import com.ctrlf.education.dto.VideoDtos.VideoStartRequest;
import com.ctrlf.education.entity.EducationScript;
import com.ctrlf.education.entity.VideoGenerationJob;
import com.ctrlf.education.repository.EducationScriptRepository;
import com.ctrlf.education.repository.VideoGenerationJobRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 영상 생성 관련 비즈니스 로직을 처리하는 서비스.
 *
 * <p>주요 기능:
 * <ul>
 *   <li>스크립트 조회/수정</li>
 *   <li>스크립트 생성 완료 콜백 처리</li>
 *   <li>영상 생성 Job 등록</li>
 *   <li>영상 생성 재시도</li>
 *   <li>영상 생성 완료 콜백 처리</li>
 *   <li>전처리/임베딩/스크립트 생성 요청 (AI 서버 호출)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    /** Job 상태 상수 */
    private static final String STATUS_QUEUED = "QUEUED";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    private final EducationScriptRepository scriptRepository;
    private final VideoGenerationJobRepository jobRepository;
    private final VideoAiClient videoAiClient;

    // ========================
    // 스크립트 관련 메서드
    // ========================

    /**
     * 스크립트를 조회합니다.
     *
     * @param scriptId 스크립트 ID
     * @return 스크립트 정보
     * @throws ResponseStatusException 스크립트가 존재하지 않을 경우 404
     */
    @Transactional(readOnly = true)
    public ScriptResponse getScript(UUID scriptId) {
        EducationScript script = scriptRepository.findById(scriptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스크립트를 찾을 수 없습니다: " + scriptId));

        return new ScriptResponse(
            script.getId(),
            script.getEducationId(),
            script.getSourceDocId(),
            script.getContent(),
            script.getVersion()
        );
    }

    /**
     * 스크립트 목록을 페이징으로 조회합니다.
     *
     * @param page 페이지 번호(0-base)
     * @param size 페이지 크기
     * @return 스크립트 응답 목록
     */
    @Transactional(readOnly = true)
    public List<ScriptResponse> listScripts(int page, int size) {
        Page<EducationScript> pageRes = scriptRepository.findAll(PageRequest.of(page, size));
        List<ScriptResponse> list = new ArrayList<>();
        for (EducationScript s : pageRes.getContent()) {
            list.add(new ScriptResponse(
                s.getId(),
                s.getEducationId(),
                s.getSourceDocId(),
                s.getContent(),
                s.getVersion()
            ));
        }
        return list;
    }

    /**
     * 스크립트를 삭제합니다.
     *
     * @param scriptId 스크립트 ID
     * @throws ResponseStatusException 스크립트가 존재하지 않을 경우 404
     */
    @Transactional
    public void deleteScript(UUID scriptId) {
        EducationScript script = scriptRepository.findById(scriptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스크립트를 찾을 수 없습니다: " + scriptId));
        scriptRepository.delete(script);
        log.info("스크립트 삭제 완료. scriptId={}", scriptId);
    }

    /**
     * 스크립트를 수정합니다.
     *
     * @param scriptId 스크립트 ID
     * @param request  수정 요청 (스크립트 내용)
     * @return 수정 결과
     * @throws ResponseStatusException 스크립트가 존재하지 않을 경우 404
     */
    @Transactional
    public ScriptUpdateResponse updateScript(UUID scriptId, ScriptUpdateRequest request) {
        EducationScript script = scriptRepository.findById(scriptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스크립트를 찾을 수 없습니다: " + scriptId));

        script.setContent(request.script());
        // 버전 증가
        script.setVersion(script.getVersion() == null ? 2 : script.getVersion() + 1);
        scriptRepository.save(script);

        log.info("스크립트 수정 완료. scriptId={}, newVersion={}", scriptId, script.getVersion());
        return new ScriptUpdateResponse(true, scriptId);
    }

    /**
     * AI 서버로부터 스크립트 생성 완료 콜백을 처리합니다.
     *
     * @param callback 콜백 데이터 (materialId, scriptId, script, version)
     * @return 저장 결과
     */
    @Transactional
    public ScriptCompleteResponse handleScriptComplete(ScriptCompleteCallback callback) {
        // 기존 스크립트가 있는지 확인
        EducationScript script = scriptRepository.findById(callback.scriptId())
            .orElseGet(() -> {
                EducationScript newScript = new EducationScript();
                newScript.setId(callback.scriptId());
                return newScript;
            });

        script.setSourceDocId(callback.materialId());
        script.setContent(callback.script());
        script.setVersion(callback.version());
        scriptRepository.save(script);

        log.info("스크립트 생성 완료 콜백 처리. materialId={}, scriptId={}, version={}",
            callback.materialId(), callback.scriptId(), callback.version());
        return new ScriptCompleteResponse(true, callback.scriptId());
    }

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
}
