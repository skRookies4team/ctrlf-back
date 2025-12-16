package com.ctrlf.chat.faq.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "faq_ui_categories", schema = "chat")
@Getter
@Setter
@NoArgsConstructor
public class FaqUiCategory {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id; // ✅ 불변(요구사항)

    @Column(name = "slug", nullable = false, unique = true, length = 50)
    private String slug; // ✅ 고유

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    @Column(name = "updated_by", columnDefinition = "uuid")
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static FaqUiCategory create(UUID id, String slug, String displayName, int sortOrder, UUID operatorId) {
        FaqUiCategory c = new FaqUiCategory();
        c.setId(id);
        c.setSlug(slug);
        c.setDisplayName(displayName);
        c.setSortOrder(sortOrder);
        c.setIsActive(true);
        c.setCreatedBy(operatorId);
        c.setUpdatedBy(operatorId);
        c.setCreatedAt(Instant.now());
        c.setUpdatedAt(Instant.now());
        return c;
    }

    public void update(String displayName, Integer sortOrder, Boolean isActive, UUID operatorId) {
        if (displayName != null) this.displayName = displayName;
        if (sortOrder != null) this.sortOrder = sortOrder;
        if (isActive != null) this.isActive = isActive;
        this.updatedBy = operatorId;
        this.updatedAt = Instant.now();
    }
}
