package com.ctrlf.chat.faq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FAQ Draft 반려 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FaqDraftRejectRequest {

    @NotNull(message = "반려자 ID는 필수입니다.")
    private UUID reviewerId;

    @NotBlank(message = "반려 사유는 필수입니다.")
    private String reason;
}

