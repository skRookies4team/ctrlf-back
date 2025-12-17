package com.ctrlf.education.config;

import com.ctrlf.education.entity.Education;
import com.ctrlf.education.entity.EducationCategory;
import com.ctrlf.education.entity.EducationTopic;
import com.ctrlf.education.repository.EducationRepository;
import com.ctrlf.education.video.entity.EducationScript;
import com.ctrlf.education.video.entity.EducationScriptChapter;
import com.ctrlf.education.video.entity.EducationScriptScene;
import com.ctrlf.education.video.repository.EducationScriptChapterRepository;
import com.ctrlf.education.video.repository.EducationScriptRepository;
import com.ctrlf.education.video.repository.EducationScriptSceneRepository;
import com.ctrlf.education.video.repository.EducationVideoProgressRepository;
import com.ctrlf.education.video.repository.EducationVideoRepository;
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
    private final VideoGenerationJobRepository jobRepository;
    private final EducationScriptChapterRepository chapterRepository;
    private final EducationScriptSceneRepository sceneRepository;
    private final EducationVideoRepository educationVideoRepository;
    private final EducationVideoProgressRepository educationVideoProgressRepository;

    public SeedDataRunner(
        EducationRepository educationRepository,
        EducationScriptRepository scriptRepository,
        VideoGenerationJobRepository jobRepository,
        EducationScriptChapterRepository chapterRepository,
        EducationScriptSceneRepository sceneRepository,
        EducationVideoRepository educationVideoRepository,
        EducationVideoProgressRepository educationVideoProgressRepository
    ) {
        this.educationRepository = educationRepository;
        this.scriptRepository = scriptRepository;
        this.jobRepository = jobRepository;
        this.chapterRepository = chapterRepository;
        this.sceneRepository = sceneRepository;
        this.educationVideoRepository = educationVideoRepository;
        this.educationVideoProgressRepository = educationVideoProgressRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedScriptsAndJobs();
        seedEducationsVideosAndProgress();
    }

    private void seedScriptsAndJobs() {
        // 교육 엔티티 선행 생성 (FK 충족)
        Education eduA = new Education();
        eduA.setTitle("테스트 교육 A");
        eduA.setCategory(EducationTopic.JOB_DUTY);
        eduA.setDescription("테스트 교육 A 설명");
        eduA.setPassScore(80);
        eduA.setPassRatio(90);
        eduA.setDepartmentScope("[\"개발팀\",\"인사팀\"]");
        eduA.setRequire(Boolean.TRUE);
        eduA.setEduType(EducationCategory.MANDATORY);
        educationRepository.save(eduA);
        UUID eduId1 = eduA.getId();

        Education eduB = new Education();
        eduB.setTitle("테스트 교육 B");
        eduB.setCategory(EducationTopic.WORKPLACE_BULLYING);
        eduB.setDescription("테스트 교육 B 설명");
        eduB.setPassScore(70);
        eduB.setPassRatio(80);
        eduB.setDepartmentScope("[\"개발팀\",\"총무팀\"]");
        eduB.setRequire(Boolean.FALSE);
        eduB.setEduType(EducationCategory.ETC);
        educationRepository.save(eduB);
        UUID eduId2 = eduB.getId();

        // 고정 자료 UUID (테스트 호출 용이)
        UUID materialId1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

        UUID scriptId1 = insertScript(eduId1, materialId1,
            "샘플 스크립트 내용입니다. 첫 번째 버전.", 1);

        // 두 번째 샘플
        UUID materialId2 = UUID.fromString("660e8400-e29b-41d4-a716-446655440002");

        UUID scriptId2 = insertScript(eduId2, materialId2,
            "두 번째 샘플 스크립트입니다.", 1);

        // 스크립트 INSERT를 먼저 DB에 반영
        scriptRepository.flush();

        // 그 다음 챕터/씬 시드
        seedChaptersAndScenes(scriptId1);
        seedChaptersAndScenes(scriptId2);

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

    private UUID insertScript(UUID eduId, UUID materialId, String content, Integer version) {
        EducationScript s = new EducationScript();
        s.setEducationId(eduId);
        // FK 충돌 방지: 소스 문서 시더가 없으므로 null로 둡니다.
        s.setSourceDocId(null);
        s.setTitle("직장내괴롭힘교육 교육 영상");
        s.setTotalDurationSec(720);
        // 변경된 스키마에 맞춰 raw_payload(JSONB)에 저장
        // content가 JSON이 아닐 수 있으므로 간단히 JSON 문자열로 포장
        String payload = content != null && content.trim().startsWith("{")
            ? content
            : "{\"script\":\"" + content.replace("\"", "\\\"") + "\"}";
        s.setRawPayload(payload);
        s.setVersion(version);
        scriptRepository.save(s);
        log.info("Seed created: EducationScript scriptId={}, eduId={}, materialId={}", s.getId(), eduId, materialId);
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

