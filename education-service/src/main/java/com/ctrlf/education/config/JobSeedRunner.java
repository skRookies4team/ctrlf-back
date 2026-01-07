package com.ctrlf.education.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Override
    @Transactional
    public void run(String... args) {
        // 교육 시드만 생성하므로 Job 시드는 생성하지 않음
        log.info("Job seed skipped: only education seed is enabled");
    }
}

