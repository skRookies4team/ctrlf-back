package com.ctrlf.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RAG 참조 문서 정보 DTO.
 * AI 서비스에서 반환하는 출처 정보를 프론트엔드로 전달합니다.
 */
public record ChatSourceDto(
    /** 문서 ID */
    @JsonProperty("doc_id")
    String docId,

    /** 문서 제목 */
    String title,

    /** 페이지 번호 (nullable) */
    Integer page,

    /** 검색 관련도 점수 (0.0 ~ 1.0) */
    Double score,

    /** 문서 발췌 내용 */
    String snippet,

    /** 조항 라벨 (예: "제10조 (연차휴가) 제1항") */
    @JsonProperty("article_label")
    String articleLabel,

    /** 조항 경로 (예: "제3장 휴가 > 제10조 > 제1항") */
    @JsonProperty("article_path")
    String articlePath,

    /** 소스 유형: POLICY(정책문서), TRAINING_SCRIPT(교육스크립트) */
    @JsonProperty("source_type")
    String sourceType
) {}
