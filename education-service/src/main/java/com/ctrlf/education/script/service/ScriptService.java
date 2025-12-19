package com.ctrlf.education.script.service;

import com.ctrlf.education.script.dto.EducationScriptDto.ChapterItem;
import com.ctrlf.education.script.dto.EducationScriptDto.ChapterUpsert;
import com.ctrlf.education.script.dto.EducationScriptDto.SceneItem;
import com.ctrlf.education.script.dto.EducationScriptDto.SceneUpsert;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptCompleteCallback;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptCompleteResponse;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptDetailResponse;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptResponse;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptUpdateRequest;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptUpdateResponse;
import com.ctrlf.education.script.entity.EducationScript;
import com.ctrlf.education.script.entity.EducationScriptChapter;
import com.ctrlf.education.script.entity.EducationScriptScene;
import com.ctrlf.education.script.repository.EducationScriptChapterRepository;
import com.ctrlf.education.script.repository.EducationScriptRepository;
import com.ctrlf.education.script.repository.EducationScriptSceneRepository;
import com.ctrlf.education.video.entity.EducationVideo;
import com.ctrlf.education.video.repository.EducationVideoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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

@Service
@RequiredArgsConstructor
public class ScriptService {
  private static final Logger log = LoggerFactory.getLogger(ScriptService.class);

  private static final String STATUS_PROCESSING = "PROCESSING";
  private static final String STATUS_SCRIPT_GENERATING = "SCRIPT_GENERATING";

  private final EducationScriptRepository scriptRepository;
  private final EducationScriptChapterRepository chapterRepository;
  private final EducationScriptSceneRepository sceneRepository;
  private final EducationVideoRepository videoRepository;
  private final ObjectMapper objectMapper;

  // ========================
  // 스크립트 자동생성 요청
  // ========================

  /**
   * 임베딩 완료된 자료를 기반으로 스크립트 자동생성을 AI 서버에 요청합니다.
   * (전처리/임베딩은 infra-service의 POST /rag/documents/upload에서 이미 완료됨)
   *
   * @param materialId 자료 ID (= RagDocument.id)
   * @param eduId      교육 ID
   * @param videoId    영상 컨텐츠 ID
   * @param fileUrl    S3 파일 URL
   * @return AI 서버 응답 (accepted, status, videoId)
   */
  @Transactional
  public ScriptGenerationResponse requestScriptGeneration(UUID materialId, UUID eduId, UUID videoId, String fileUrl) {
    // 영상 컨텐츠 존재 확인 및 materialId 연결
    EducationVideo video = videoRepository.findById(videoId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "영상 컨텐츠를 찾을 수 없습니다: " + videoId));
    
    video.setMaterialId(materialId);
    video.setStatus(STATUS_SCRIPT_GENERATING);
    videoRepository.save(video);

    // AI 서버 미구현: 모의 응답으로 처리
    log.info("[MOCK] 스크립트 자동생성 요청 처리. materialId={}, eduId={}, videoId={}", materialId, eduId, videoId);
    return new ScriptGenerationResponse(true, STATUS_PROCESSING, videoId);
  }

  /**
   * 스크립트 자동생성 응답 DTO.
   */
  public record ScriptGenerationResponse(boolean accepted, String status, UUID videoId) {}

  @Transactional(readOnly = true)
  public ScriptDetailResponse getScript(UUID scriptId) {
    EducationScript script =
        scriptRepository
            .findById(scriptId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "스크립트를 찾을 수 없습니다: " + scriptId));

    List<EducationScriptChapter> chapters =
        chapterRepository.findByScriptIdOrderByChapterIndexAsc(scriptId);
    List<ChapterItem> chapterItems = new ArrayList<>();
    for (EducationScriptChapter ch : chapters) {
      List<EducationScriptScene> scenes =
          sceneRepository.findByChapterIdOrderBySceneIndexAsc(ch.getId());
      List<SceneItem> sceneItems = new ArrayList<>();
      for (EducationScriptScene sc : scenes) {
        sceneItems.add(
            new SceneItem(
                sc.getId(),
                sc.getSceneIndex(),
                sc.getPurpose(),
                sc.getNarration(),
                sc.getCaption(),
                sc.getVisual(),
                sc.getDurationSec(),
                sc.getSourceChunkIndexes(),
                sc.getConfidenceScore()));
      }
      chapterItems.add(
          new ChapterItem(
              ch.getId(), ch.getChapterIndex(), ch.getTitle(), ch.getDurationSec(), sceneItems));
    }

    return new ScriptDetailResponse(
        script.getId(),
        script.getEducationId(),
        script.getSourceDocId(),
        script.getTitle(),
        script.getTotalDurationSec(),
        script.getVersion(),
        script.getLlmModel(),
        script.getRawPayload(),
        chapterItems);
  }

  @Transactional(readOnly = true)
  public List<ScriptResponse> listScripts(int page, int size) {
    Page<EducationScript> pageRes = scriptRepository.findAll(PageRequest.of(page, size));
    List<ScriptResponse> list = new ArrayList<>();
    for (EducationScript s : pageRes.getContent()) {
      list.add(
          new ScriptResponse(
              s.getId(), s.getEducationId(), s.getSourceDocId(), s.getRawPayload(), s.getVersion()));
    }
    return list;
  }

  @Transactional
  public void deleteScript(UUID scriptId) {
    EducationScript script =
        scriptRepository
            .findById(scriptId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "스크립트를 찾을 수 없습니다: " + scriptId));
    scriptRepository.delete(script);
    log.info("스크립트 삭제 완료. scriptId={}", scriptId);
  }

  @Transactional
  public ScriptUpdateResponse updateScript(UUID scriptId, ScriptUpdateRequest request) {
    EducationScript script =
        scriptRepository
            .findById(scriptId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "스크립트를 찾을 수 없습니다: " + scriptId));

    if (request.script() != null) {
      script.setRawPayload(request.script());
      script.setVersion(script.getVersion() == null ? 2 : script.getVersion() + 1);
      scriptRepository.save(script);
    }

    if (request.chapters() != null) {
      List<EducationScriptScene> oldScenes =
          sceneRepository.findByScriptIdOrderByChapterIdAscSceneIndexAsc(scriptId);
      if (!oldScenes.isEmpty()) {
        sceneRepository.deleteAll(oldScenes);
      }
      List<EducationScriptChapter> oldChapters =
          chapterRepository.findByScriptIdOrderByChapterIndexAsc(scriptId);
      if (!oldChapters.isEmpty()) {
        chapterRepository.deleteAll(oldChapters);
      }

      for (ChapterUpsert cu : request.chapters()) {
        EducationScriptChapter ch = new EducationScriptChapter();
        ch.setScriptId(scriptId);
        ch.setChapterIndex(cu.index());
        ch.setTitle(cu.title());
        ch.setDurationSec(cu.durationSec());
        chapterRepository.save(ch);

        if (cu.scenes() != null) {
          for (SceneUpsert su : cu.scenes()) {
            EducationScriptScene sc = new EducationScriptScene();
            sc.setScriptId(scriptId);
            sc.setChapterId(ch.getId());
            sc.setSceneIndex(su.index());
            sc.setPurpose(su.purpose());
            sc.setNarration(su.narration());
            sc.setCaption(su.caption());
            sc.setVisual(su.visual());
            sc.setDurationSec(su.durationSec());
            sc.setSourceChunkIndexes(su.sourceChunkIndexes());
            sc.setConfidenceScore(su.confidenceScore());
            sceneRepository.save(sc);
          }
        }
      }
    }

    log.info("스크립트 수정 완료. scriptId={}, newVersion={}", scriptId, script.getVersion());
    return new ScriptUpdateResponse(true, scriptId);
  }

  @Transactional
  public ScriptCompleteResponse handleScriptComplete(ScriptCompleteCallback callback) {
    // EducationVideo 조회
    EducationVideo video = videoRepository.findById(callback.videoId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "영상 컨텐츠를 찾을 수 없습니다: " + callback.videoId()));

    // script Object를 JSON 문자열로 변환
    String scriptJson;
    try {
      scriptJson = objectMapper.writeValueAsString(callback.script());
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "스크립트 JSON 변환 실패: " + e.getMessage());
    }

    // 기존 스크립트가 있으면 소프트 삭제 및 버전 가져오기
    int newVersion = 1;
    if (video.getScriptId() != null) {
      EducationScript oldScript = scriptRepository.findById(video.getScriptId()).orElse(null);
      if (oldScript != null) {
        // 이전 스크립트 소프트 삭제
        oldScript.setDeletedAt(Instant.now());
        scriptRepository.save(oldScript);
        // 버전 증가
        newVersion = (oldScript.getVersion() != null ? oldScript.getVersion() : 0) + 1;
        log.info("이전 스크립트 소프트 삭제. oldScriptId={}, oldVersion={}", 
            oldScript.getId(), oldScript.getVersion());
      }
    }

    // 새 스크립트 생성 (ID는 자동 생성)
    // NOTE: sourceDocId는 설정하지 않음 (materialId는 infra-service의 RagDocument.id이므로 FK 위반)
    //       materialId는 EducationVideo에 이미 저장되어 있음
    EducationScript script = new EducationScript();
    script.setEducationId(video.getEducationId());
    script.setRawPayload(scriptJson);
    script.setVersion(newVersion);
    scriptRepository.save(script);

    // EducationVideo에 scriptId 연결 및 상태 업데이트
    video.setScriptId(script.getId());
    video.setStatus("SCRIPT_READY"); // 스크립트 생성 완료 → 영상 생성 대기
    videoRepository.save(video);

    log.info(
        "스크립트 생성 완료 콜백 처리. materialId={}, videoId={}, scriptId={}, version={}",
        video.getMaterialId(),
        callback.videoId(),
        script.getId(),
        newVersion);
    return new ScriptCompleteResponse(true, script.getId());
  }
}
