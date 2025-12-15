package com.ctrlf.education.config;

import com.ctrlf.education.entity.EducationScript;
import com.ctrlf.education.entity.VideoGenerationJob;
import com.ctrlf.education.repository.EducationScriptRepository;
import com.ctrlf.education.repository.VideoGenerationJobRepository;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로컬 개발용 Job 시드.
 * 스크립트 시드(@Order(1))가 끝난 뒤 실행되어 FK를 안전하게 만족합니다.
 */
@Profile("local-seed")
@Order(2)
@Component
public class JobSeedRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(JobSeedRunner.class);

    private final EducationScriptRepository scriptRepository;
    private final VideoGenerationJobRepository jobRepository;

    public JobSeedRunner(
        EducationScriptRepository scriptRepository,
        VideoGenerationJobRepository jobRepository
    ) {
        this.scriptRepository = scriptRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedJobForFixedScript();
    }

    private void seedJobForFixedScript() {
        // 최근 생성된 스크립트 1건을 찾아 Job 생성
        Optional<EducationScript> scriptOpt = scriptRepository
            .findAll(PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")))
            .get()
            .findFirst();
        if (scriptOpt.isEmpty()) {
            log.warn("Job seed skip: no scripts found");
            return;
        }
        EducationScript s = scriptOpt.get();
        UUID scriptId = s.getId();
        // 이미 Job 있으면 스킵
        boolean exists = jobRepository.findAll().stream().anyMatch(j -> scriptId.equals(j.getScriptId()));
        if (exists) {
            log.info("Job seed skip: existing job for scriptId={}", scriptId);
            return;
        }
        VideoGenerationJob job = new VideoGenerationJob();
        job.setEducationId(s.getEducationId());
        job.setScriptId(scriptId);
        job.setTemplateOption("{}");
        job.setStatus("QUEUED");
        job.setRetryCount(0);
        jobRepository.save(job);
        log.info("Job seed created: jobId={}, scriptId={}", job.getId(), scriptId);
    }
}

