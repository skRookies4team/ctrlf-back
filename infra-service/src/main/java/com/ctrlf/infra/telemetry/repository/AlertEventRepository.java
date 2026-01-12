package com.ctrlf.infra.telemetry.repository;

import com.ctrlf.infra.telemetry.entity.AlertEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertEventRepository extends JpaRepository<AlertEventEntity, UUID> {
}
