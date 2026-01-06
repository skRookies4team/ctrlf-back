package com.ctrlf.infra.hr.repository;

import com.ctrlf.infra.hr.entity.WelfarePoint;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 복지 포인트 잔액 Repository (Q14, Q15)
 */
@Repository
public interface WelfarePointRepository extends JpaRepository<WelfarePoint, UUID> {

    /**
     * 사용자의 복지 포인트 잔액 조회
     */
    Optional<WelfarePoint> findByUserUuid(UUID userUuid);

    /**
     * 사용자의 특정 연도 복지 포인트 잔액 조회
     */
    Optional<WelfarePoint> findByUserUuidAndYear(UUID userUuid, Integer year);
}
