package com.llm.eval.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "standard_qa_pairs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardQAPair {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qa_id")
    private Integer qaId;
    
    @ManyToOne
    @JoinColumn(name = "standard_question_id")
    private StandardQuestion standardQuestion;
    
    @ManyToOne
    @JoinColumn(name = "standard_answer_id")
    private StandardAnswer standardAnswer;
    
    @ManyToOne
    @JoinColumn(name = "source_question_id")
    private RawQuestion sourceQuestion;
    
    @ManyToOne
    @JoinColumn(name = "source_answer_id")
    private RawAnswer sourceAnswer;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 