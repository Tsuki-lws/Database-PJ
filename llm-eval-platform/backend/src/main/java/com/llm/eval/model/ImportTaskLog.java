package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_task_logs")
@Data
@SQLDelete(sql = "UPDATE import_task_logs SET deleted_at = NOW() WHERE log_id = ?")
@Where(clause = "deleted_at IS NULL")
public class ImportTaskLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id", nullable = false)
    private ImportTask importTask;
    
    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "log_level")
    private String logLevel;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
} 