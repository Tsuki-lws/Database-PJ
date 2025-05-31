package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "import_tasks")
@Data
@SQLDelete(sql = "UPDATE import_tasks SET deleted_at = NOW() WHERE import_id = ?")
@Where(clause = "deleted_at IS NULL")
public class ImportTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "import_id")
    private Integer importId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ImportType type;
    
    @Column(name = "filename")
    private String filename;
    
    @Column(name = "imported_count")
    private Integer importedCount;
    
    @Column(name = "updated_count")
    private Integer updatedCount;
    
    @Column(name = "failed_count")
    private Integer failedCount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ImportStatus status;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToMany(mappedBy = "importTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImportTaskLog> logs = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    public enum ImportType {
        RAW_QA, STANDARD_QA, RAW_QUESTION, STANDARD_QA_SINGLE
    }
    
    public enum ImportStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
    }
} 