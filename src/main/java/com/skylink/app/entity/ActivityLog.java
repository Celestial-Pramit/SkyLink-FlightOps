package com.skylink.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs",
       indexes = @Index(name = "idx_activity_created", columnList = "created_at DESC"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String actorEmail;

    @Column(nullable = false, length = 50)
    private String actorName;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(nullable = false, length = 30)
    private String entityType;

    @Column(length = 50)
    private String entityId;

    @Column(length = 300)
    private String detail;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static ActivityLog of(String actorEmail, String actorName,
                                  String action, String entityType,
                                  String entityId, String detail) {
        return ActivityLog.builder()
            .actorEmail(actorEmail)
            .actorName(actorName)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .detail(detail)
            .build();
    }
}
