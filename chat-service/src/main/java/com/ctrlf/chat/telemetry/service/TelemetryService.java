package com.ctrlf.chat.telemetry.service;

import com.ctrlf.chat.telemetry.dto.TelemetryEventRequestDtos;
import com.ctrlf.chat.telemetry.dto.TelemetryEventResponseDtos;
import java.util.List;

/**
 * Telemetry 이벤트 수집 서비스 인터페이스
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public interface TelemetryService {

    /**
     * 이벤트 수집
     * 
     * @param request 이벤트 수집 요청
     * @return 이벤트 수집 응답
     */
    TelemetryEventResponseDtos.EventsResponse collectEvents(
        TelemetryEventRequestDtos.EventsRequest request
    );
}

