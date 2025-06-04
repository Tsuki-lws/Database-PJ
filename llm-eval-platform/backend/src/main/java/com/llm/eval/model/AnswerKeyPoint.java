package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "answer_key_points")
@Data
@SQLDelete(sql = "UPDATE answer_key_points SET deleted_at = NOW() WHERE key_point_id = ?")
@Where(clause = "deleted_at IS NULL")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AnswerKeyPoint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "key_point_id")
    private Integer keyPointId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_answer_id", nullable = false)
    @JsonBackReference
    private StandardAnswer standardAnswer;
    
    @Column(name = "point_text", nullable = false)
    private String pointText;
    
    @Column(name = "point_order", nullable = false)
    private Integer pointOrder;
    
    @Column(name = "point_weight")
    private BigDecimal pointWeight;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "point_type")
    private PointType pointType;
    
    @Column(name = "example_text")
    private String exampleText;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "version")
    private Integer version;
    
    public enum PointType {
        required, bonus, penalty
    }
} 