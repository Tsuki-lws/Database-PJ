package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "standard_answers")
@Data
@SQLDelete(sql = "UPDATE standard_answers SET deleted_at = NOW() WHERE standard_answer_id = ?")
@Where(clause = "deleted_at IS NULL")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StandardAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "standard_answer_id")
    private Integer standardAnswerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_question_id", nullable = false)
    @JsonBackReference
    private StandardQuestion standardQuestion;
    
    @Column(name = "answer", nullable = false)
    private String answer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_answer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private RawAnswer sourceAnswer;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private SourceType sourceType;
    
    @Column(name = "source_id")
    private Integer sourceId;
    
    @Column(name = "selection_reason")
    private String selectionReason;
    
    @Column(name = "selected_by")
    private Integer selectedBy;
    
    @Column(name = "is_final")
    private Boolean isFinal;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "version")
    private Integer version;
    
    @OneToMany(mappedBy = "standardAnswer", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("standardAnswer")
    private List<AnswerKeyPoint> keyPoints;
    
    public enum SourceType {
        raw, crowdsourced, expert, manual
    }
} 