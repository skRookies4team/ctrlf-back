package com.ctrlf.education.config;

import com.ctrlf.education.entity.Education;
import com.ctrlf.education.entity.EducationCategory;
import com.ctrlf.education.entity.EducationTopic;
import com.ctrlf.education.repository.EducationRepository;
import com.ctrlf.education.video.entity.EducationVideo;
import com.ctrlf.education.video.repository.EducationVideoRepository;

import java.time.Instant;
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
 * 프로덕션 환경용 시드 데이터 주입기.
 * 활성화: --spring.profiles.active=prod,prod-seed
 */
@Profile("prod-seed")
@Order(1)
@Component
public class ProductionSeedRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ProductionSeedRunner.class);

    private final EducationRepository educationRepository;
    private final EducationVideoRepository educationVideoRepository;

    public ProductionSeedRunner(
        EducationRepository educationRepository,
        EducationVideoRepository educationVideoRepository
    ) {
        this.educationRepository = educationRepository;
        this.educationVideoRepository = educationVideoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            log.info("Starting production seed data generation...");
            
            // 1. 교육 생성
            seedEducations();
            
            // 2. 영상 생성
            seedVideos();
            
            log.info("Production seed data generation completed successfully!");
        } catch (Exception e) {
            log.error("Failed to generate production seed data (non-blocking): {}", e.getMessage(), e);
        }
    }

    /**
     * 교육 시드 생성 (기존 데이터는 유지)
     */
    private void seedEducations() {
        log.info("Starting to seed educations (production mode - no deletion)...");
        
        try {
            // 직무 교육 생성
            String[] departments = {"총무팀", "기획팀", "마케팅팀", "인사팀", "재무팀", "개발팀", "영업팀", "법무팀"};
            String[] departmentDescriptions = {
                "총무 업무 수행에 필요한 핵심 역량을 강화하는 직무 교육입니다.",
                "기획 업무 수행에 필요한 핵심 역량을 강화하는 직무 교육입니다.",
                "마케팅 업무 수행에 필요한 핵심 역량을 강화하는 직무 교육입니다.",
                "인사 업무 수행에 필요한 핵심 역량을 강화하는 직무 교육입니다.",
                "재무 업무 수행에 필요한 핵심 역량을 강화하는 직무 교육입니다.",
                "개발 업무 수행에 필요한 핵심 역량을 강화하는 직무 교육입니다.",
                "영업 업무 수행에 필요한 핵심 역량을 강화하는 직무 교육입니다.",
                "법무 업무 수행에 필요한 핵심 역량을 강화하는 직무 교육입니다."
            };
            int[] startDaysAgo = {30, 35, 40, 45, 50, 55, 60, 65};
            
            for (int i = 0; i < departments.length; i++) {
                String title = departments[i] + " 직무 역량 강화 교육";
                if (educationRepository.findByTitleAndDeletedAtIsNull(title).isPresent()) {
                    continue;
                }
                try {
                    Education edu = new Education();
                    edu.setTitle(title);
                    edu.setCategory(EducationTopic.JOB_DUTY);
                    edu.setDescription(departmentDescriptions[i]);
                    edu.setPassScore(80);
                    edu.setPassRatio(90);
                    edu.setRequire(Boolean.TRUE);
                    edu.setEduType(EducationCategory.JOB);
                    edu.setVersion(1);
                    edu.setStartAt(Instant.now().minusSeconds(86400L * startDaysAgo[i]));
                    edu.setEndAt(Instant.now().plusSeconds(86400L * (180 - startDaysAgo[i])));
                    edu.setDepartmentScope(new String[]{departments[i]});
                    educationRepository.save(edu);
                } catch (Exception e) {
                    log.warn("Failed to create education '{}': {}", title, e.getMessage());
                }
            }

            // 법정 의무 교육 생성
            createEducationIfNotExists("성희롱 예방 교육", EducationTopic.SEXUAL_HARASSMENT_PREVENTION,
                "직장 내 성희롱 예방 및 대응 방법에 대한 법정 필수 교육입니다.", EducationCategory.MANDATORY, 60, 120);

            createEducationIfNotExists("개인정보 보호 교육", EducationTopic.PERSONAL_INFO_PROTECTION,
                "개인정보 보호법에 따른 개인정보 취급 및 보호에 관한 법정 필수 교육입니다.", EducationCategory.MANDATORY, 90, 90);

            createEducationIfNotExists("직장 내 괴롭힘 예방 교육", EducationTopic.WORKPLACE_BULLYING,
                "직장 내 괴롭힘 예방 및 대응 방법에 대한 법정 필수 교육입니다.", EducationCategory.MANDATORY, 120, 60);

            createEducationIfNotExists("장애인 인식 개선 교육", EducationTopic.DISABILITY_AWARENESS,
                "장애인에 대한 인식 개선 및 편견 해소를 위한 법정 필수 교육입니다.", EducationCategory.MANDATORY, 15, 165);

            log.info("Education seeding completed (production mode)");
        } catch (Exception e) {
            log.error("Error during education seeding: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void createEducationIfNotExists(
        String title,
        EducationTopic category,
        String description,
        EducationCategory eduType,
        int startDaysAgo,
        int endDaysFromNow
    ) {
        try {
            if (educationRepository.findByTitleAndDeletedAtIsNull(title).isPresent()) {
                return;
            }
            Education edu = new Education();
            edu.setTitle(title);
            edu.setCategory(category);
            edu.setDescription(description);
            edu.setPassScore(80);
            edu.setPassRatio(90);
            edu.setRequire(Boolean.TRUE);
            edu.setEduType(eduType);
            edu.setVersion(1);
            edu.setStartAt(Instant.now().minusSeconds(86400L * startDaysAgo));
            edu.setEndAt(Instant.now().plusSeconds(86400L * endDaysFromNow));
            edu.setDepartmentScope(new String[]{"전체 부서"});
            educationRepository.save(edu);
            log.info("Created education: {}", title);
        } catch (Exception e) {
            log.warn("Failed to create education '{}': {}", title, e.getMessage());
        }
    }
    
    /**
     * 영상 생성을 위한 헬퍼 메서드 (이름을 createVideo로 통일)
     */
    private void createVideo(String educationTitle, String videoTitle, String videoUrl, int durationSec, int orderIndex) {
        try {
            List<Education> allEducations = educationRepository.findAll();
            Education education = allEducations.stream()
                .filter(e -> e.getTitle().equals(educationTitle) && e.getDeletedAt() == null)
                .findFirst()
                .orElse(null);

            if (education == null) {
                log.warn("Education not found for video: {}", educationTitle);
                return;
            }

            boolean exists = educationVideoRepository.findByEducationId(education.getId()).stream()
                .anyMatch(v -> v.getTitle().equals(videoTitle));
            
            if (exists) {
                log.debug("Video already exists, skipping: [{}]", videoTitle);
                return;
            }

            // createDraft + Setter 패턴 사용
            var video = EducationVideo.createDraft(
                education.getId(),
                videoTitle,
                UUID.randomUUID()
            );

            video.setFileUrl(videoUrl);
            video.setDuration(durationSec);
            video.setOrderIndex(orderIndex);
            video.setStatus("PUBLISHED");

            educationVideoRepository.save(video);
            log.info("Created video: [{}] in [{}]", videoTitle, educationTitle);

        } catch (Exception e) {
            log.error("Failed to create video '{}': {}", videoTitle, e.getMessage());
        }
    }

    /**
     * 다양한 교육에 대한 영상 시드 일괄 생성
     */
    private void seedVideos() {
        log.info("Starting to seed videos with specific URLs...");

        // 1. 개인정보 보호 교육
        createVideo(
            "개인정보 보호 교육", 
            "개인정보 보호 교육 - 기본편", 
            "https://ctrl-s3.s3.ap-northeast-2.amazonaws.com/videos/edu.mp4",
            1200, 
            0
        );
        createVideo(
            "개인정보 보호 교육", 
            "개인정보 보호 교육 ", 
            "https://ctrl-s3.s3.ap-northeast-2.amazonaws.com/education_videos/%EC%A0%95%EB%B3%B4%EB%B3%B4%ED%98%B8.mp4",
            1200, 
            1
        );

        // 2. 성희롱 예방 교육
        createVideo(
            "성희롱 예방 교육", 
            "직장 내 성희롱 예방 교육 ", 
            "https://ctrl-s3.s3.ap-northeast-2.amazonaws.com/education_videos/%EC%84%B1%ED%9D%AC%EB%A1%B1.mp4",
            600, 
            0
        );

        // 3. 직장 내 괴롭힘 예방 교육
        createVideo(
            "직장 내 괴롭힘 예방 교육", 
            "직장 내 괴롭힘 판단 기준 및 대응", 
            "https://ctrl-s3.s3.ap-northeast-2.amazonaws.com/education_videos/compa.mp4",
            1200, 
            0
        );

        // 4. 장애인 인식 개선 교육
        createVideo(
            "장애인 인식 개선 교육", 
            "함께 일하는 동료, 장애인 인식 개선", 
            "https://ctrl-s3.s3.ap-northeast-2.amazonaws.com/education_videos/hurt.mp4",
            850, 
            0
        );
        createVideo(
            "장애인 인식 개선 교육", 
            "장애인 인식 개선 우리의 동료", 
            "https://ctrl-s3.s3.ap-northeast-2.amazonaws.com/education_videos/hurt2.mp4",
            850, 
            1
        );
        
        // 5. 직무 역량 강화 교육 (개발, 인사)
        createVideo(
            "개발팀 직무 역량 강화 교육", 
            "MSA 아키텍처 패턴", 
            "https://ctrl-s3.s3.ap-northeast-2.amazonaws.com/education_videos/MSA.mp4", 
            2000, 
            2
        );
        
        createVideo(
            "인사팀 직무 역량 강화 교육", 
            "인사직무 핵심 교육", 
            "https://ctrl-s3.s3.ap-northeast-2.amazonaws.com/education_videos/insa.mp4", 
            1500, 
            0
        );
        
        createVideo(
            "개발팀 직무 역량 강화 교육", 
            "CI/CD 실무 마스터", 
            "https://ctrl-s3.s3.ap-northeast-2.amazonaws.com/education_videos/cicd.mp4", 
            1800, 
            1
        );

        // 6. 마케팅팀 직무 교육
        createVideo(
            "개발팀 직무 역량 강화 교육", 
            "2025년 디지털 트렌드", 
            "https://ctrl-s3.s3.ap-northeast-2.amazonaws.com/videos/marketing_trend_2025.mp4", 
            1100, 
            2
        );

        log.info("Video seeding completed.");
    }
}