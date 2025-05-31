package com.llm.eval.entity;

import jakarta.persistence.*;
import com.llm.eval.model.StandardQuestion;

/**
 * 数据集问题映射实体
 */
@Entity
@Table(name = "dataset_question_mapping")
public class DatasetQuestionMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long mappingId;
    
    @Column(name = "version_id", nullable = false)
    private Long versionId;
    
    @Column(name = "standard_question_id", nullable = false)
    private Long standardQuestionId;
    
    // 关联的数据集版本
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private DatasetVersion datasetVersion;
    
    // 关联的标准问题
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_question_id", insertable = false, updatable = false)
    private StandardQuestion standardQuestion;
    
    // Constructors
    public DatasetQuestionMapping() {}
    
    public DatasetQuestionMapping(Long versionId, Long standardQuestionId) {
        this.versionId = versionId;
        this.standardQuestionId = standardQuestionId;
    }
    
    // Getters and Setters
    public Long getMappingId() { return mappingId; }
    public void setMappingId(Long mappingId) { this.mappingId = mappingId; }
    
    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    
    public Long getStandardQuestionId() { return standardQuestionId; }
    public void setStandardQuestionId(Long standardQuestionId) { this.standardQuestionId = standardQuestionId; }
    
    public DatasetVersion getDatasetVersion() { return datasetVersion; }
    public void setDatasetVersion(DatasetVersion datasetVersion) { this.datasetVersion = datasetVersion; }
    
    public StandardQuestion getStandardQuestion() { return standardQuestion; }
    public void setStandardQuestion(StandardQuestion standardQuestion) { this.standardQuestion = standardQuestion; }
    
    @Override
    public String toString() {
        return "DatasetQuestionMapping{" +
                "mappingId=" + mappingId +
                ", versionId=" + versionId +
                ", standardQuestionId=" + standardQuestionId +
                '}';
    }
}
