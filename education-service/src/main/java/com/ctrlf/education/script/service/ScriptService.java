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

  private final EducationScriptRepository scriptRepository;
  private final EducationScriptChapterRepository chapterRepository;
  private final EducationScriptSceneRepository sceneRepository;

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
    EducationScript script =
        scriptRepository
            .findById(callback.scriptId())
            .orElseGet(
                () -> {
                  EducationScript newScript = new EducationScript();
                  newScript.setId(callback.scriptId());
                  return newScript;
                });

    script.setSourceDocId(callback.materialId());
    script.setRawPayload(callback.script());
    script.setVersion(callback.version());
    scriptRepository.save(script);

    log.info(
        "스크립트 생성 완료 콜백 처리. materialId={}, scriptId={}, version={}",
        callback.materialId(),
        callback.scriptId(),
        callback.version());
    return new ScriptCompleteResponse(true, callback.scriptId());
  }
}
