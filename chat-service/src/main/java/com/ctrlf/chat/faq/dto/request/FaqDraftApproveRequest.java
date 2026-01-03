package com.ctrlf.chat.faq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FAQ Draft 승인 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FaqDraftApproveRequest {

    @NotNull(message = "승인자 ID는 필수입니다.")
    private UUID reviewerId;

    @NotBlank(message = "질문은 필수입니다.")
    private String question;

    @NotBlank(message = "답변은 필수입니다.")
    private String answer;
}

