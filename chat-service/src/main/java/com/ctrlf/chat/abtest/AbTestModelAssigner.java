package com.ctrlf.chat.abtest;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A/B 테스트 모델 할당 서비스
 * 
 * <p>세션 생성 시 사용자 ID 기반 해시를 사용하여 일관된 모델 할당을 보장합니다.</p>
 * <p>동일 사용자는 항상 동일한 모델을 할당받아 일관성 있는 테스트가 가능합니다.</p>
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@Component
public class AbTestModelAssigner {

    /**
     * OpenAI 모델 할당 비율 (0.0 ~ 1.0)
     * 기본값: 0.5 (50:50)
     */
    @Value("${ab-test.openai-ratio:0.5}")
    private double openaiRatio;

    /**
     * 사용자 ID와 도메인을 기반으로 임베딩 모델을 할당합니다.
     * 
     * <p>할당 규칙:</p>
     * <ul>
     *   <li>사용자 ID의 해시값을 사용하여 일관된 할당 보장</li>
     *   <li>동일 사용자는 항상 동일한 모델 할당</li>
     *   <li>설정 가능한 비율로 할당 (기본 50:50)</li>
     * </ul>
     * 
     * @param userId 사용자 UUID
     * @param domain 도메인 (선택적, 향후 도메인별 다른 비율 적용 가능)
     * @return 할당된 모델 ("openai" 또는 "sroberta")
     */
    public String assignModel(UUID userId, String domain) {
        // 사용자 ID 기반 해시 (음수 방지)
        int hash = Math.abs(userId.hashCode());
        
        // 0.0 ~ 1.0 범위로 정규화
        double normalized = (hash % 10000) / 10000.0;
        
        // 비율에 따라 할당
        String model = normalized < openaiRatio ? "openai" : "sroberta";
        
        return model;
    }

    /**
     * OpenAI 모델 할당 비율을 반환합니다.
     * 
     * @return OpenAI 모델 할당 비율 (0.0 ~ 1.0)
     */
    public double getOpenaiRatio() {
        return openaiRatio;
    }
}

