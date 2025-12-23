package com.ctrlf.education.config;

import com.ctrlf.education.entity.Education;
import com.ctrlf.education.entity.EducationCategory;
import com.ctrlf.education.entity.EducationTopic;
import com.ctrlf.education.repository.EducationRepository;
import com.ctrlf.education.script.entity.EducationScript;
import com.ctrlf.education.script.entity.EducationScriptChapter;
import com.ctrlf.education.script.entity.EducationScriptScene;
import com.ctrlf.education.script.repository.EducationScriptChapterRepository;
import com.ctrlf.education.script.repository.EducationScriptRepository;
import com.ctrlf.education.script.repository.EducationScriptSceneRepository;
import com.ctrlf.education.repository.EducationProgressRepository;
import com.ctrlf.education.video.repository.EducationVideoProgressRepository;
import com.ctrlf.education.video.repository.EducationVideoRepository;
import com.ctrlf.education.video.repository.SourceSetDocumentRepository;
import com.ctrlf.education.video.repository.SourceSetRepository;
import com.ctrlf.education.video.repository.VideoGenerationJobRepository;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로컬 개발용 시드 데이터 주입기.
 * 활성화: --spring.profiles.active=local,local-seed
 */
@Profile("local-seed")
@Order(1)
@Component
public class SeedDataRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SeedDataRunner.class);

    private final EducationRepository educationRepository;
    private final EducationScriptRepository scriptRepository;
    private final EducationScriptChapterRepository chapterRepository;
    private final EducationScriptSceneRepository sceneRepository;
    private final EducationVideoRepository educationVideoRepository;
    private final EducationVideoProgressRepository educationVideoProgressRepository;
    private final EducationProgressRepository educationProgressRepository;
    private final VideoGenerationJobRepository videoGenerationJobRepository;
    private final SourceSetDocumentRepository sourceSetDocumentRepository;
    private final SourceSetRepository sourceSetRepository;

    public SeedDataRunner(
        EducationRepository educationRepository,
        EducationScriptRepository scriptRepository,
        EducationScriptChapterRepository chapterRepository,
        EducationScriptSceneRepository sceneRepository,
        EducationVideoRepository educationVideoRepository,
        EducationVideoProgressRepository educationVideoProgressRepository,
        EducationProgressRepository educationProgressRepository,
        VideoGenerationJobRepository videoGenerationJobRepository,
        SourceSetDocumentRepository sourceSetDocumentRepository,
        SourceSetRepository sourceSetRepository
    ) {
        this.educationRepository = educationRepository;
        this.scriptRepository = scriptRepository;
        this.chapterRepository = chapterRepository;
        this.sceneRepository = sceneRepository;
        this.educationVideoRepository = educationVideoRepository;
        this.educationVideoProgressRepository = educationVideoProgressRepository;
        this.educationProgressRepository = educationProgressRepository;
        this.videoGenerationJobRepository = videoGenerationJobRepository;
        this.sourceSetDocumentRepository = sourceSetDocumentRepository;
        this.sourceSetRepository = sourceSetRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        clearAllEducationData();
        seedScriptsAndJobs();
        seedEducationsVideosAndProgress();
    }

    /**
     * 기존 교육 관련 데이터를 모두 삭제합니다.
     * FK 관계로 인해 자식 테이블부터 삭제합니다.
     */
    private void clearAllEducationData() {
        log.info("기존 교육 데이터 삭제 시작...");
        
        // 1. 교육 진행 현황 삭제 (education_id FK 참조)
        educationProgressRepository.deleteAll();
        log.info("교육 진행 현황 삭제 완료");
        
        // 2. 영상 진행률 삭제
        educationVideoProgressRepository.deleteAll();
        log.info("교육 영상 진행률 삭제 완료");
        
        // 3. 소스셋 문서 삭제 (source_set_id FK 참조)
        sourceSetDocumentRepository.deleteAll();
        log.info("소스셋 문서 삭제 완료");
        
        // 4. 소스셋 삭제 (video_id FK 참조하므로 video보다 먼저 삭제)
        sourceSetRepository.deleteAll();
        log.info("소스셋 삭제 완료");
        
        // 5. 영상 삭제
        educationVideoRepository.deleteAll();
        log.info("교육 영상 삭제 완료");
        
        // 6. 영상 생성 작업(Job) 삭제 - script를 참조하므로 스크립트보다 먼저 삭제
        videoGenerationJobRepository.deleteAll();
        log.info("영상 생성 작업 삭제 완료");
        
        // 7. 스크립트 씬 삭제
        sceneRepository.deleteAll();
        log.info("스크립트 씬 삭제 완료");
        
        // 8. 스크립트 챕터 삭제
        chapterRepository.deleteAll();
        log.info("스크립트 챕터 삭제 완료");
        
        // 9. 스크립트 삭제
        scriptRepository.deleteAll();
        log.info("스크립트 삭제 완료");
        
        // 10. 교육 삭제
        educationRepository.deleteAll();
        log.info("교육 삭제 완료");
        
        log.info("기존 교육 데이터 삭제 완료!");
    }

    private void seedScriptsAndJobs() {
        // 5가지 교육 카테고리별 시드 데이터 생성
        List<Education> educations = new ArrayList<>();

        // 1. 직무 교육 (JOB_DUTY) - edu_type: JOB
        Education edu1 = new Education();
        edu1.setTitle("직무 역량 강화 교육");
        edu1.setCategory(EducationTopic.JOB_DUTY);
        edu1.setDescription("업무 수행에 필요한 핵심 역량을 강화하는 직무 교육입니다.");
        edu1.setPassScore(80);
        edu1.setPassRatio(90);
        edu1.setRequire(Boolean.TRUE);
        edu1.setEduType(EducationCategory.JOB);
        educations.add(edu1);

        // 2. 성희롱 예방 교육 (SEXUAL_HARASSMENT_PREVENTION) - edu_type: MANDATORY
        Education edu2 = new Education();
        edu2.setTitle("성희롱 예방 교육");
        edu2.setCategory(EducationTopic.SEXUAL_HARASSMENT_PREVENTION);
        edu2.setDescription("직장 내 성희롱 예방 및 대응 방법에 대한 법정 필수 교육입니다.");
        edu2.setPassScore(80);
        edu2.setPassRatio(90);
        edu2.setRequire(Boolean.TRUE);
        edu2.setEduType(EducationCategory.MANDATORY);
        educations.add(edu2);

        // 3. 개인정보 보호 교육 (PERSONAL_INFO_PROTECTION) - edu_type: MANDATORY
        Education edu3 = new Education();
        edu3.setTitle("개인정보 보호 교육");
        edu3.setCategory(EducationTopic.PERSONAL_INFO_PROTECTION);
        edu3.setDescription("개인정보 보호법에 따른 개인정보 취급 및 보호에 관한 법정 필수 교육입니다.");
        edu3.setPassScore(80);
        edu3.setPassRatio(90);
        edu3.setRequire(Boolean.TRUE);
        edu3.setEduType(EducationCategory.MANDATORY);
        educations.add(edu3);

        // 4. 직장 내 괴롭힘 예방 교육 (WORKPLACE_BULLYING) - edu_type: MANDATORY
        Education edu4 = new Education();
        edu4.setTitle("직장 내 괴롭힘 예방 교육");
        edu4.setCategory(EducationTopic.WORKPLACE_BULLYING);
        edu4.setDescription("직장 내 괴롭힘 예방 및 대응 방법에 대한 법정 필수 교육입니다.");
        edu4.setPassScore(80);
        edu4.setPassRatio(90);
        edu4.setRequire(Boolean.TRUE);
        edu4.setEduType(EducationCategory.MANDATORY);
        educations.add(edu4);

        // 5. 장애인 인식 개선 교육 (DISABILITY_AWARENESS) - edu_type: MANDATORY
        Education edu5 = new Education();
        edu5.setTitle("장애인 인식 개선 교육");
        edu5.setCategory(EducationTopic.DISABILITY_AWARENESS);
        edu5.setDescription("장애인에 대한 인식 개선 및 편견 해소를 위한 법정 필수 교육입니다.");
        edu5.setPassScore(80);
        edu5.setPassRatio(90);
        edu5.setRequire(Boolean.TRUE);
        edu5.setEduType(EducationCategory.MANDATORY);
        educations.add(edu5);

        // 모든 교육 저장
        educationRepository.saveAll(educations);
        log.info("Seed created: {} 개의 교육 시드 데이터 생성 완료", educations.size());

        // 각 교육에 대해 스크립트 생성
        List<UUID> scriptIds = new ArrayList<>();
        String[] scriptTitles = {
            "직무 역량 강화 교육 영상",
            "성희롱 예방 교육 영상",
            "개인정보 보호 교육 영상",
            "직장 내 괴롭힘 예방 교육 영상",
            "장애인 인식 개선 교육 영상"
        };

        for (int i = 0; i < educations.size(); i++) {
            UUID scriptId = insertScript(educations.get(i).getId(), null,
                scriptTitles[i] + " 스크립트 내용입니다.", 1, scriptTitles[i]);
            scriptIds.add(scriptId);
        }

        // 스크립트 INSERT를 먼저 DB에 반영
        scriptRepository.flush();

        // 각 스크립트에 대해 챕터/씬 시드
        for (UUID scriptId : scriptIds) {
            seedChaptersAndScenes(scriptId);
        }

        // 챕터/씬 반영
        chapterRepository.flush();
        sceneRepository.flush();

        // Job 시드는 별도 러너(@Order(2))에서 처리합니다.
    }

    /**
     * 사용자용 API 확인을 위한 영상/진행 더미 데이터.
     */
    private void seedEducationsVideosAndProgress() {
        // 이미 영상이 있으면 스킵
        List<Education> edus = educationRepository.findAll();
        if (edus.isEmpty()) return;

        UUID demoUser = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        for (Education edu : edus) {
            var videos = educationVideoRepository.findByEducationId(edu.getId());
            // 영상이 없으면 더미 영상 추가
            if (videos.isEmpty()) {
                List<com.ctrlf.education.video.entity.EducationVideo> seeds = new ArrayList<>();
                String[] urls = new String[] {
                    "s3://ctrl-s3/video/13654077_3840_2160_30fps.mp4",
                    "s3://ctrl-s3/video/13671318_3840_2160_25fps.mp4",
                    "s3://ctrl-s3/video/14876583_3840_2160_30fps.mp4",
                    "s3://ctrl-s3/video/14899783_1920_1080_50fps.mp4",
                    "s3://ctrl-s3/video/14903571_3840_2160_25fps.mp4"
                };
                String[] titles = new String[] {
                    edu.getTitle() + " - 기본편",
                    edu.getTitle() + " - 심화편",
                    edu.getTitle() + " - 사례편",
                    edu.getTitle() + " - 실무편",
                    edu.getTitle() + " - 종합편"
                };
                int[] durations = new int[] { 1200, 900, 1100, 1000, 950 };
                for (int i = 0; i < urls.length; i++) {
                    var v = com.ctrlf.education.video.entity.EducationVideo.create(
                        edu.getId(),
                        titles[i],
                        urls[i],
                        durations[i],
                        "ALL",
                        1,
                        "ACTIVE"
                    );
                    v.setOrderIndex(i);
                    seeds.add(v);
                }
                educationVideoRepository.saveAll(seeds);
                videos = educationVideoRepository.findByEducationId(edu.getId());
                log.info("Seed created: {} dummy videos for eduId={}", videos.size(), edu.getId());
            }
            if (videos.isEmpty()) {
                log.info("Seed skip: 교육에 연결된 영상이 없어 진행률 더미 생성을 건너뜁니다. eduId={}", edu.getId());
                continue;
            }
            /**
             * s3://ctrl-s3/video/13654077_3840_2160_30fps.mp4
             * s3://ctrl-s3/video/13654077_3840_2160_30fps.mp4
             * s3://ctrl-s3/video/13671318_3840_2160_25fps.mp4
             * s3://ctrl-s3/video/14876583_3840_2160_30fps.mp4
             * s3://ctrl-s3/video/14899783_1920_1080_50fps.mp4
             * s3://ctrl-s3/video/14903571_3840_2160_25fps.mp4
             */

            // 진행률 더미: 첫 영상 30%, 두번째 100%
            for (int i = 0; i < videos.size(); i++) {
                var v = videos.get(i);
                var p = educationVideoProgressRepository
                    .findByUserUuidAndEducationIdAndVideoId(demoUser, edu.getId(), v.getId())
                    .orElseGet(() -> com.ctrlf.education.video.entity.EducationVideoProgress.create(demoUser, edu.getId(), v.getId()));
                if (i == 0) {
                    p.setLastPositionSeconds(540); // 9분
                    p.setTotalWatchSeconds(540);
                    p.setProgress(30);
                    p.setIsCompleted(false);
                } else {
                    p.setLastPositionSeconds(v.getDuration() != null ? v.getDuration() : 1200);
                    p.setTotalWatchSeconds(p.getLastPositionSeconds());
                    p.setProgress(100);
                    p.setIsCompleted(true);
                }
                educationVideoProgressRepository.save(p);
            }
        }
    }

    private UUID insertScript(UUID eduId, UUID materialId, String content, Integer version, String title) {
        EducationScript s = new EducationScript();
        s.setEducationId(eduId);
        // Note: sourceDocId는 제거되었으며, 이제 SourceSet을 통해 문서와 연결됩니다.
        s.setTitle(title);
        s.setTotalDurationSec(720);
        // 변경된 스키마에 맞춰 raw_payload(JSONB)에 저장
        // content가 JSON이 아닐 수 있으므로 간단히 JSON 문자열로 포장
        String payload = content != null && content.trim().startsWith("{")
            ? content
            : "{\"script\":\"" + content.replace("\"", "\\\"") + "\"}";
        s.setRawPayload(payload);
        s.setVersion(version);
        scriptRepository.save(s);
        log.info("Seed created: EducationScript scriptId={}, eduId={}, title={}", s.getId(), eduId, title);
        return s.getId();
    }

    private void seedChaptersAndScenes(UUID scriptId) {
        if (!chapterRepository.findByScriptIdOrderByChapterIndexAsc(scriptId).isEmpty()) {
            log.info("Seed skip: 챕터/씬이 이미 존재합니다. scriptId={}", scriptId);
            return;
        }
        // Chapter 0: 괴롭힘
        EducationScriptChapter ch0 = new EducationScriptChapter();
        ch0.setScriptId(scriptId);
        ch0.setChapterIndex(0);
        ch0.setTitle("괴롭힘");
        ch0.setDurationSec(180);
        chapterRepository.save(ch0);

        List<EducationScriptScene> ch0Scenes = new ArrayList<>();
        ch0Scenes.add(buildScene(scriptId, ch0.getId(), 1, "hook",
            "직장 내 괴롭힘이란 사용자에게 심리적, 물리적, 경제적 피해를 주는 행위를 의미합니다.",
            "직장 내 괴롭힘이란 사용자에게 심리적, 물리적, 경제적 피해를 주는 행위를 의미합니다.",
            "자료 원문 문장(텍스트) 강조 + 키워드 하이라이트",
            15, new int[]{1,2,3}, 0.95f));
        ch0Scenes.add(buildScene(scriptId, ch0.getId(), 2, "concept",
            "직장 내 괴롭힘이란 근로자의 정신적, 윤리적, 물리적 피해를 초래하는 행동을 의미합니다.",
            "직장 내 괴롭힘이란 사용자에게 심리적, 물리적, 경제적 피해를 주는 행위를 의미합니다.",
            "자료 원문 문장(텍스트) 강조 + 키워드 하이라이트",
            45, new int[]{1,2,3}, 0.9f));
        sceneRepository.saveAll(ch0Scenes);

        // Chapter 1: 직무
        EducationScriptChapter ch1 = new EducationScriptChapter();
        ch1.setScriptId(scriptId);
        ch1.setChapterIndex(1);
        ch1.setTitle("직무");
        ch1.setDurationSec(180);
        chapterRepository.save(ch1);

        List<EducationScriptScene> ch1Scenes = new ArrayList<>();
        ch1Scenes.add(buildScene(scriptId, ch1.getId(), 1, "hook",
            "업무상 적정 범위를 넘는 행위",
            "업무상 적정 범위를 넘는 행위",
            "자료 원문 문장(텍스트) 강조 + 키워드 하이라이트",
            15, new int[]{12,24,26}, 0.9f));
        ch1Scenes.add(buildScene(scriptId, ch1.getId(), 2, "example",
            "학교 교감이 전공과 무관한 과목 강의를 배정하고 위협을 가하는 등 부적절한 행위 사례.",
            "부적절한 인사 조치와 위협 사례",
            "자료 원문 문장(텍스트) 강조 + 키워드 하이라이트",
            30, new int[]{12,24,26}, 0.85f));
        sceneRepository.saveAll(ch1Scenes);
    }

    private EducationScriptScene buildScene(
        UUID scriptId, UUID chapterId, int sceneIndex, String purpose,
        String narration, String caption, String visual,
        int durationSec, int[] sourceChunks, Float confidence
    ) {
        EducationScriptScene s = new EducationScriptScene();
        s.setScriptId(scriptId);
        s.setChapterId(chapterId);
        s.setSceneIndex(sceneIndex);
        s.setPurpose(purpose);
        s.setNarration(narration);
        s.setCaption(caption);
        s.setVisual(visual);
        s.setDurationSec(durationSec);
        s.setSourceChunkIndexes(sourceChunks);
        s.setConfidenceScore(confidence);
        return s;
    }
}

