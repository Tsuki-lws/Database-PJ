package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 数据集问题映射实体
 * 表示数据集版本与标准问题的多对多关系
 */
@Entity
@Table(name = "dataset_question_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetQuestionMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Integer mappingId;
    
    @Column(name = "version_id", nullable = false)
    private Integer versionId;
    
    @Column(name = "standard_question_id", nullable = false)
    private Integer standardQuestionId;
    
    // 关联的数据集版本
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private DatasetVersion datasetVersion;
    
    // 关联的标准问题
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_question_id", insertable = false, updatable = false)
    private StandardQuestion standardQuestion;
    
    public DatasetQuestionMapping(Integer versionId, Integer standardQuestionId) {
        this.versionId = versionId;
        this.standardQuestionId = standardQuestionId;
    }
}
