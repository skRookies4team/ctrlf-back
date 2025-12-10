package com.ctrlf.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;

/**
 * 변경성 작업(Create/Update/Delete 등)의 표준 응답 형태.
 *
 * @param <ID> 리소스 식별자 타입 (예: UUID, Long 등)
 */
@Getter
@AllArgsConstructor
public class MutationResponse<ID> {
    /**
     * 변경된(또는 생성된/삭제된) 리소스의 식별자.
     */
    private ID id;
}


