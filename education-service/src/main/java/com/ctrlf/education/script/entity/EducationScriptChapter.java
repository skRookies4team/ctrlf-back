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

@Entity
@Table(name = "education_script_chapter", schema = "education")
@Getter
@Setter
@NoArgsConstructor
public class EducationScriptChapter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "script_id", columnDefinition = "uuid")
    private UUID scriptId;

    @Column(name = "chapter_index")
    private Integer chapterIndex;

    @Column(name = "title")
    private String title;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}

