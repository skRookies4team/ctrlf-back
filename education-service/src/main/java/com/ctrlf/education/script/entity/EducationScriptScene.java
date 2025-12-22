package com.ctrlf.education.script.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "education_script_scene", schema = "education")
@Getter
@Setter
@NoArgsConstructor
public class EducationScriptScene {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "script_id", columnDefinition = "uuid")
    private UUID scriptId;

    @Column(name = "chapter_id", columnDefinition = "uuid")
    private UUID chapterId;

    @Column(name = "scene_index")
    private Integer sceneIndex;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "narration")
    private String narration;

    @Column(name = "caption")
    private String caption;

    @Column(name = "visual")
    private String visual;

    @Column(name = "duration_sec")
    private Integer durationSec;

    /** 레거시: 단일 문서일 때만 사용 (deprecated) */
    @Column(name = "source_chunk_indexes", columnDefinition = "int[]")
    private int[] sourceChunkIndexes;

    /** 출처 참조 (멀티문서): [{"documentId": "uuid", "chunkIndex": 0}, ...] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_refs", columnDefinition = "jsonb")
    private String sourceRefs;

    @Column(name = "confidence_score")
    private Float confidenceScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}

