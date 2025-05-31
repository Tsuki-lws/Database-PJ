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
    
    // Constructors
    public DatasetQuestionMapping() {}
      public DatasetQuestionMapping(Integer versionId, Integer standardQuestionId) {
        this.versionId = versionId;
        this.standardQuestionId = standardQuestionId;
    }
      // Getters and Setters
    public Integer getMappingId() { return mappingId; }
    public void setMappingId(Integer mappingId) { this.mappingId = mappingId; }
    
    public Integer getVersionId() { return versionId; }
    public void setVersionId(Integer versionId) { this.versionId = versionId; }
    
    public Integer getStandardQuestionId() { return standardQuestionId; }
    public void setStandardQuestionId(Integer standardQuestionId) { this.standardQuestionId = standardQuestionId; }
    
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
