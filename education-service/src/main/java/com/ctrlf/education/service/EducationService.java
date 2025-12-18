package com.ctrlf.education.service;

import com.ctrlf.common.dto.MutationResponse;
import com.ctrlf.education.dto.EducationRequests.CreateEducationRequest;
import com.ctrlf.education.dto.EducationRequests.UpdateEducationRequest;
import com.ctrlf.education.dto.EducationRequests.VideoProgressUpdateRequest;
import com.ctrlf.education.dto.EducationResponses;
import com.ctrlf.education.dto.EducationResponses.EducationDetailResponse;
import com.ctrlf.education.dto.EducationResponses.EducationVideosResponse;
import com.ctrlf.education.dto.EducationResponses.VideoProgressResponse;
import com.ctrlf.education.entity.Education;
import com.ctrlf.education.entity.EducationCategory;
import com.ctrlf.education.repository.EducationRepository;
import com.ctrlf.education.video.entity.EducationVideo;
import com.ctrlf.education.video.entity.EducationVideoProgress;
import com.ctrlf.education.video.repository.EducationVideoProgressRepository;
import com.ctrlf.education.video.repository.EducationVideoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;

/**
 * 교육 도메인 비즈니스 로직을 담당하는 서비스.
 * <p>
 * - 교육 생성/조회/수정/삭제<br>
 * - 교육 영상 목록 및 사용자 진행률 조회<br>
 * - 영상 시청 진행률 업데이트 및 교육 이수 처리<br>
 * 를 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class EducationService {

    private final EducationRepository educationRepository;
    private final EducationVideoRepository educationVideoRepository;
    private final EducationVideoProgressRepository educationVideoProgressRepository;
    private final ObjectMapper objectMapper;

    /**
     * 교육 생성.
     *
     * @param req 생성 요청
     * @return 생성된 교육 ID
     */
    @Transactional
    public MutationResponse<UUID> createEducation(CreateEducationRequest req) {
        validateCreate(req);
        Education e = new Education();
        e.setTitle(req.getTitle());
        // 주제 카테고리 enum 저장
        e.setCategory(parseTopic(req.getCategory()));
        // edu_type은 enum으로 저장
        e.setEduType(parseEduType(req.getEduType() != null ? req.getEduType() : req.getCategory()));
        e.setDescription(req.getDescription());
        e.setPassScore(req.getPassScore());
        e.setPassRatio(req.getPassRatio());
        e.setRequire(req.getRequire());
        UUID id = educationRepository.save(e).getId();
        return new MutationResponse<>(id);
    }

    /**
     * 교육 생성 - 표준 MutationResponse 형태 반환.
     */
    @Transactional
    public MutationResponse<UUID> createEducationMutation(CreateEducationRequest req) {
        MutationResponse<UUID> id = createEducation(req);
        return id;
    }

    /**
     * 교육 및 영상 목록(내 목록).
     * - completed/category/sort 파라미터를 받아 사용자 기준으로 영상 목록과 진행 정보를 포함해 반환
     * - 페이지네이션 없음(상한 1000)
     */
    /**
     * 사용자 기준 교육 및 영상 목록 집계.
     *
     * - 정렬/카테고리/이수여부 필터를 적용해 교육 목록을 조회한 뒤,
     *   각 교육에 포함된 영상 목록과 사용자 진행 정보를 합성한다.
     * - 영상 시청완료 판단은 education.pass_ratio(%) 이상 시청 또는 진행 엔티티의 완료 플래그로 결정한다.
     * - 교육 진행률은 포함된 영상 진행률의 평균으로 계산한다.
     * - 페이지네이션은 적용하지 않고 최대 1000건까지 반환한다.
     *
     * @param completed 이수 여부 필터(옵션)
     * @param category 카테고리(MANDATORY/JOB/ETC, 옵션)
     * @param sort 정렬 기준(UPDATED|TITLE, 기본 UPDATED)
     * @param userUuid 로그인 사용자 UUID(옵션)
     * @return 교육 목록(영상 목록/진행 포함)
     */
    public List<EducationResponses.EducationListItem> getEducationsMe(
        Boolean completed,
        String category,
        String sort,
        Optional<UUID> userUuid
    ) {
        // 정렬 키 정규화
        String sortKey = (!StringUtils.hasText(sort)) ? "UPDATED" : sort.trim().toUpperCase();
        if (!"UPDATED".equals(sortKey) && !"TITLE".equals(sortKey)) {
            sortKey = "UPDATED";
        }
        // 카테고리 필터 정규화(미지정이면 null)
        String categoryFilter = !StringUtils.hasText(category) ? null : category.trim().toUpperCase();
        int offset = 0;
        int size = 1000;
        // 교육 기본 목록 조회(사용자 기준 일부 플래그 포함)
        List<Object[]> rows = educationRepository.findEducationsNative(
            offset, size, completed, null, categoryFilter, userUuid.orElse(null), sortKey
        );
        List<EducationResponses.EducationListItem> result = new ArrayList<>();
        for (Object[] r : rows) {
            UUID eduId = (UUID) r[0];
            String title = (String) r[1];
            String description = (String) r[2];
            String cat = (String) r[3];
            Boolean required = (Boolean) r[4];
            Boolean hasProgress = (Boolean) r[6];
            // 교육별 영상/진행 정보 결합
            List<EducationVideo> vids = educationVideoRepository.findByEducationId(eduId);
            List<EducationResponses.EducationVideosResponse.VideoItem> videoItems = new ArrayList<>();
            int sumPct = 0;
            // 교육 시청완료 기준 비율(pass_ratio), 미설정 시 100%
            Integer passRatio = educationRepository.findById(eduId).map(Education::getPassRatio).orElse(100);
            for (EducationVideo v : vids) {
                Integer resume = 0;
                Integer total = 0;
                Boolean completedV = false;
                Integer pctV = 0;
                if (userUuid.isPresent()) {
                    var pv = educationVideoProgressRepository.findByUserUuidAndEducationIdAndVideoId(userUuid.get(), eduId, v.getId());
                    if (pv.isPresent()) {
                        var p = pv.get();
                        resume = p.getLastPositionSeconds() != null ? p.getLastPositionSeconds() : 0;
                        total = p.getTotalWatchSeconds() != null ? p.getTotalWatchSeconds() : 0;
                        completedV = p.getIsCompleted() != null && p.getIsCompleted();
                        pctV = p.getProgress() != null ? p.getProgress() : 0;
                    }
                }
                int durationSec = v.getDuration() != null ? v.getDuration() : 0;
                // 진행률 값이 없으면 position/duration으로 보정 계산
                if ((pctV == null || pctV == 0) && durationSec > 0 && resume != null && resume > 0) {
                    pctV = Math.min(100, Math.max(0, (int) Math.round((resume * 100.0) / durationSec)));
                }
                String vStatus;
                // 영상 시청완료: 완료 플래그이거나 pass_ratio 이상 시청
                if (Boolean.TRUE.equals(completedV) || (pctV != null && pctV >= (passRatio != null ? passRatio : 100))) {
                    vStatus = "시청완료";
                    pctV = 100;
                } else if ((pctV != null && pctV > 0) || (resume != null && resume > 0)) {
                    vStatus = "시청중";
                } else {
                    vStatus = "시청전";
                }
                sumPct += pctV != null ? pctV : 0;
                videoItems.add(new EducationResponses.EducationVideosResponse.VideoItem(
                    v.getId(),
                    v.getTitle(),
                    v.getFileUrl(),
                    durationSec,
                    v.getVersion() != null ? v.getVersion() : 1,
                    v.getTargetDeptCode(),
                    v.getDepartmentScope(),
                    resume,
                    completedV,
                    total,
                    pctV,
                    vStatus
                ));
            }
            // 교육 진행률: 포함된 영상 진행률 평균
            int eduProgress = vids.isEmpty() ? 0 : (sumPct / Math.max(vids.size(), 1));
            String watchStatus;
            // 교육 시청 상태: pass_ratio 기준 또는 진행 여부로 라벨링
            if (eduProgress >= (passRatio != null ? passRatio : 100)) {
                watchStatus = "시청완료";
            } else if (Boolean.TRUE.equals(hasProgress) || eduProgress > 0) {
                watchStatus = "시청중";
            } else {
                watchStatus = "시청전";
            }
            result.add(new EducationResponses.EducationListItem(
                eduId,
                title,
                description,
                cat,
                required != null && required,
                eduProgress,
                watchStatus,
                videoItems
            ));
        }
        return result;
    }


    /**
     * 교육 상세 조회.
     *
     * @param id 교육 ID
     * @return 상세 응답
     * @throws IllegalArgumentException 존재하지 않으면
     */
    public EducationResponses.EducationDetailResponse getEducationDetail(UUID id) {
        Education e = educationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "education not found"));
        // 총 duration은 교육에 속한 영상들의 duration 합산
        List<EducationVideo> videos = educationVideoRepository.findByEducationId(id);
        int totalDuration = 0;
        for (EducationVideo v : videos) {
            totalDuration += v.getDuration() != null ? v.getDuration() : 0;
        }
        return EducationDetailResponse.builder()
            .id(e.getId())
            .title(e.getTitle())
            .description(e.getDescription())
            .duration(totalDuration)
            .sections(Collections.emptyList())
            .build();
    }

    /**
     * 교육 수정(부분 수정 허용).
     *
     * @param id 교육 ID
     * @param req 수정 요청
     * @return 갱신된 updatedAt
     * @throws IllegalArgumentException 존재하지 않으면
     */
    @Transactional
    public Instant updateEducation(UUID id, UpdateEducationRequest req) {
        Education e = educationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "education not found"));
        if (StringUtils.hasText(req.getTitle())) e.setTitle(req.getTitle());
        if (req.getDescription() != null) e.setDescription(req.getDescription());
        if (StringUtils.hasText(req.getCategory())) e.setCategory(parseTopic(req.getCategory()));
        if (StringUtils.hasText(req.getEduType())) e.setEduType(parseEduType(req.getEduType()));
        if (req.getRequire() != null) e.setRequire(req.getRequire());
        if (req.getPassScore() != null) e.setPassScore(req.getPassScore());
        if (req.getPassRatio() != null) e.setPassRatio(req.getPassRatio());
        educationRepository.saveAndFlush(e);
        return e.getUpdatedAt();
    }

    /**
     * 교육 수정 - 표준 MutationResponse 형태 반환.
     */
    @Transactional
    public MutationResponse<UUID> updateEducationMutation(UUID id, UpdateEducationRequest req) {
        updateEducation(id, req);
        return new MutationResponse<>(id);
    }

    /**
     * 교육 삭제.
     * 연관된 영상/진행 정보는 단순 삭제 처리합니다.
     *
     * @param id 교육 ID
     */
    @Transactional
    public void deleteEducation(UUID id) {
        educationVideoProgressRepository.softDeleteByEducationId(id);
        educationVideoRepository.softDeleteByEducationId(id);
        int affected = educationRepository.softDeleteById(id);
        if (affected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "education not found");
        }
    }
    
    /**
     * 교육 영상 목록 조회. 사용자 UUID가 있으면 각 영상의 진행률 정보를 포함합니다.
     * 사용자 부서 목록이 있으면 해당 부서에 속하는 영상만 필터링합니다.
     *
     * @param id 교육 ID
     * @param userUuid 사용자 UUID(옵션)
     * @param userDepartments 사용자 부서 목록(옵션)
     * @return 영상 목록 응답
     */
    public EducationVideosResponse getEducationVideos(UUID id, Optional<UUID> userUuid, List<String> userDepartments) {
        Education e = educationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "education not found"));
        // 해당 교육에 속한 영상 목록 조회
        List<EducationVideo> videos = educationVideoRepository.findByEducationId(id);
        // 응답으로 내려줄 영상 항목 DTO 리스트
        List<EducationVideosResponse.VideoItem> items = new ArrayList<>();
        // 영상 시청 완료 기준 비율(education.pass_ratio, 기본 100)
        Integer passRatio = e.getPassRatio() != null ? e.getPassRatio() : 100;
        for (EducationVideo v : videos) {
            // 사용자 부서 필터링: departmentScope가 설정된 경우 사용자 부서와 매칭 확인
            if (!isVideoAccessibleByDepartment(v, userDepartments)) {
                continue; // 부서 권한이 없으면 스킵
            }
            // 사용자별 이어보기 위치/누적 시청시간/완료 여부 기본값
            Integer resume = 0;
            Integer total = 0;
            Boolean completed = false;
            Integer pct = 0;
            // 사용자 UUID가 있으면 진행 정보 조회
            if (userUuid.isPresent()) {
                var pv = educationVideoProgressRepository.findByUserUuidAndEducationIdAndVideoId(userUuid.get(), id, v.getId());
                if (pv.isPresent()) {
                    var p = pv.get();
                    resume = p.getLastPositionSeconds() != null ? p.getLastPositionSeconds() : 0;
                    total = p.getTotalWatchSeconds() != null ? p.getTotalWatchSeconds() : 0;
                    completed = p.getIsCompleted() != null && p.getIsCompleted();
                    pct = p.getProgress() != null ? p.getProgress() : 0;
                }
            }
            // 진행률 계산 보정: progress 값이 없으면 duration/position으로 계산
            int durationSec = v.getDuration() != null ? v.getDuration() : 0;
            if ((pct == null || pct == 0) && durationSec > 0 && resume != null && resume > 0) {
                pct = Math.min(100, Math.max(0, (int) Math.round((resume * 100.0) / durationSec)));
            }
            // 시청 상태 라벨
            String watchStatus;
            if (Boolean.TRUE.equals(completed) || (pct != null && pct >= (passRatio != null ? passRatio : 100))) {
                watchStatus = "시청완료";
                pct = 100;
            } else if ((pct != null && pct > 0) || (resume != null && resume > 0)) {
                watchStatus = "시청중";
            } else {
                watchStatus = "시청전";
            }
            // 단일 영상 항목 구성
            items.add(new EducationVideosResponse.VideoItem(
                v.getId(),
                v.getTitle(),
                v.getFileUrl(),
                durationSec,
                v.getVersion() != null ? v.getVersion() : 1,
                v.getTargetDeptCode(),
                v.getDepartmentScope(),
                resume,
                completed,
                total,
                pct,
                watchStatus
            ));
        }
        // 목록 응답 생성
        return EducationVideosResponse.builder()
            .id(e.getId())
            .title(e.getTitle())
            .videos(items)
            .build();
    }

    /**
     * 영상이 사용자 부서에서 접근 가능한지 확인합니다.
     * - departmentScope가 null이거나 비어있으면 모든 부서에서 접근 가능
     * - departmentScope에 사용자 부서 중 하나라도 포함되면 접근 가능
     */
    private boolean isVideoAccessibleByDepartment(EducationVideo video, List<String> userDepartments) {
        String deptScope = video.getDepartmentScope();
        // departmentScope가 없으면 모든 부서 접근 가능
        if (deptScope == null || deptScope.isBlank()) {
            return true;
        }
        // 사용자 부서가 없으면 접근 불가
        if (userDepartments == null || userDepartments.isEmpty()) {
            return false;
        }
        // departmentScope JSON 파싱
        try {
            List<String> allowedDepts = objectMapper.readValue(deptScope, new TypeReference<List<String>>() {});
            if (allowedDepts == null || allowedDepts.isEmpty()) {
                return true; // 빈 리스트면 모든 부서 접근 가능
            }
            // 사용자 부서 중 하나라도 허용 목록에 있으면 접근 가능
            for (String userDept : userDepartments) {
                if (allowedDepts.contains(userDept)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            // 파싱 실패 시 접근 허용 (관대하게 처리)
            return true;
        }
    }

    /**
     * 영상 시청 진행률 업데이트.
     *
     * @param educationId 교육 ID
     * @param videoId 영상 ID
     * @param userUuid 사용자 UUID(필수)
     * @param req 진행률 요청
     * @return 결과 요약 응답
     * @throws IllegalArgumentException 사용자 없음 등 잘못된 요청
     */
    @Transactional
    public VideoProgressResponse updateVideoProgress(UUID educationId, UUID videoId, UUID userUuid, VideoProgressUpdateRequest req) {
        // 사용자 UUID 필수 검증
        if (userUuid == null) {
            throw new IllegalArgumentException("user required");
        }
        // 요청 값 파싱 및 기본값 설정
        int position = req.getPosition() != null ? req.getPosition() : 0;
        int duration = Math.max(1, req.getDuration() != null ? req.getDuration() : 1);
        int watch = req.getWatchTime() != null ? req.getWatchTime() : 0;

        // 진행 엔티티 조회(없으면 새로 생성)
        EducationVideoProgress progress = educationVideoProgressRepository
            .findByUserUuidAndEducationIdAndVideoId(userUuid, educationId, videoId)
            .orElseGet(() -> {
                return EducationVideoProgress.create(userUuid, educationId, videoId);
            });
        // 마지막 시청 위치 및 누적 시청 시간 갱신
        progress.setLastPositionSeconds(position);
        int currentTotal = progress.getTotalWatchSeconds() != null ? progress.getTotalWatchSeconds() : 0;
        progress.setTotalWatchSeconds(currentTotal + Math.max(0, watch));
        // 진행률(%) 계산 및 완료 여부 반영
        int pct = Math.min(100, Math.max(0, (int) Math.round((position * 100.0) / duration)));
        progress.setProgress(pct);
        progress.setIsCompleted(pct >= 100);
        // 변경 사항 저장
        educationVideoProgressRepository.save(progress);

        // 교육 전체 진행률(간단히 평균)
        List<EducationVideoProgress> all = educationVideoProgressRepository.findByUserUuidAndEducationId(userUuid, educationId);
        int avg = 0;
        boolean allCompleted = false;
        if (!all.isEmpty()) {
            // 전체 영상 진행률 합산 및 완료 개수 집계
            int sum = 0; int completedCount = 0;
            for (var p : all) {
                sum += p.getProgress() != null ? p.getProgress() : 0;
                if (p.getIsCompleted() != null && p.getIsCompleted()) completedCount++;
            }
            // 평균 진행률 및 전체 완료 여부 계산
            avg = sum / all.size();
            allCompleted = completedCount == all.size();
        }
        // 응답 DTO 구성
        return VideoProgressResponse.builder()
            .updated(true)
            .progress(pct)
            .isCompleted(progress.getIsCompleted() != null && progress.getIsCompleted())
            .totalWatchSeconds(progress.getTotalWatchSeconds() != null ? progress.getTotalWatchSeconds() : 0)
            .eduProgress(avg)
            .eduCompleted(allCompleted)
            .build();
    }

    /**
     * 교육 이수 처리.
     * 모든 영상이 완료 상태인지 검증하여 완료/실패를 반환합니다.
     *
     * @param educationId 교육 ID
     * @param userUuid 사용자 UUID
     * @return 처리 결과 맵
     */
    @Transactional
    public Map<String, Object> completeEducation(UUID educationId, UUID userUuid) {
        Map<String, Object> result = new HashMap<>();
        if (userUuid == null) {
            result.put("status", "FAILED");
            result.put("message", "user required");
            return result;
        }
        List<EducationVideoProgress> all = educationVideoProgressRepository.findByUserUuidAndEducationId(userUuid, educationId);

        // 모두 영상 시청이 완료되었는지 확인
        boolean ok = !all.isEmpty() && all.stream().allMatch(p -> p.getIsCompleted() != null && p.getIsCompleted());
        if (ok) {
            result.put("status", "COMPLETED");
            result.put("completedAt", Instant.now().toString());
        } else {
            result.put("status", "FAILED");
            result.put("message", "영상 이수 조건 미충족");
        }
        return result;
    }

    /**
     * 생성 요청 유효성 검사.
     *
     * @param req 요청 본문
     */
    private void validateCreate(CreateEducationRequest req) {
        if (req == null) throw new IllegalArgumentException("Request body is required");
        if (!StringUtils.hasText(req.getTitle())) throw new IllegalArgumentException("title is required");
        if (!StringUtils.hasText(req.getCategory())) throw new IllegalArgumentException("category is required");
        if (req.getRequire() == null) throw new IllegalArgumentException("require is required");
    }

    /**
     * 문자열 카테고리를 enum(EducationCategory)으로 변환.
     * - 허용값: MANDATORY, JOB, ETC (대소문자 무시)
     */
    private EducationCategory parseEduType(String eduType) {
        String normalized = eduType == null ? null : eduType.trim().toUpperCase();
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("eduType(category) is required");
        }
        try {
            return EducationCategory.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid eduType: " + eduType + " (allowed: MANDATORY, JOB, ETC)");
        }
    }

    private com.ctrlf.education.entity.EducationTopic parseTopic(String topic) {
        if (!StringUtils.hasText(topic)) {
            throw new IllegalArgumentException("category(topic) is required");
        }
        String n = topic.trim().toUpperCase().replace(' ', '_');
        // 한글 라벨 허용 매핑
        switch (topic.trim()) {
            case "직무": n = "JOB_DUTY"; break;
            case "성희롱 예방": n = "SEXUAL_HARASSMENT_PREVENTION"; break;
            case "개인 정보 보호":
            case "개인정보 보호": n = "PERSONAL_INFO_PROTECTION"; break;
            case "직장 내 괴롭힘": n = "WORKPLACE_BULLYING"; break;
            case "장애인 인식 개선": n = "DISABILITY_AWARENESS"; break;
            default: break;
        }
        try {
            return com.ctrlf.education.entity.EducationTopic.valueOf(n);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid category(topic): " + topic + " (allowed: JOB_DUTY, SEXUAL_HARASSMENT_PREVENTION, PERSONAL_INFO_PROTECTION, WORKPLACE_BULLYING, DISABILITY_AWARENESS)");
        }
    }
}

