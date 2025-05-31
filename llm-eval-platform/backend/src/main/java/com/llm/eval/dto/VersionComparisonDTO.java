package com.llm.eval.dto;

import java.util.Map;
import java.util.HashMap;

/**
 * 版本比较结果DTO
 */
public class VersionComparisonDTO {
    
    private VersionInfo fromVersion;
    private VersionInfo toVersion;
    private Map<String, FieldDifference> differences;
      public static class VersionInfo {
        private Integer id;
        private String versionName;
        private String createTime;
        
        public VersionInfo() {}
        
        public VersionInfo(Integer id, String versionName, String createTime) {
            this.id = id;
            this.versionName = versionName;
            this.createTime = createTime;
        }
        
        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public String getVersionName() { return versionName; }
        public void setVersionName(String versionName) { this.versionName = versionName; }
        
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
    }
    
    public static class FieldDifference {
        private Boolean changed;
        private String oldValue;
        private String newValue;
        
        public FieldDifference() {}
        
        public FieldDifference(Boolean changed, String oldValue, String newValue) {
            this.changed = changed;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        
        // Getters and Setters
        public Boolean getChanged() { return changed; }
        public void setChanged(Boolean changed) { this.changed = changed; }
        
        public String getOldValue() { return oldValue; }
        public void setOldValue(String oldValue) { this.oldValue = oldValue; }
        
        public String getNewValue() { return newValue; }
        public void setNewValue(String newValue) { this.newValue = newValue; }
    }
      public static class VersionDifferences {
        private Map<String, FieldDifference> fieldChanges;
        private FieldDifference questionBody;
        private FieldDifference difficulty;
        private FieldDifference questionType;
        
        public VersionDifferences() {
            this.fieldChanges = new HashMap<>();
        }
        
        public Map<String, FieldDifference> getFieldChanges() { 
            return fieldChanges; 
        }
        
        public void setFieldChanges(Map<String, FieldDifference> fieldChanges) { 
            this.fieldChanges = fieldChanges; 
        }
        
        public void addFieldChange(String fieldName, String oldValue, String newValue) {
            fieldChanges.put(fieldName, new FieldDifference(true, oldValue, newValue));
        }
        
        public FieldDifference getQuestionBody() { return questionBody; }
        public void setQuestionBody(FieldDifference questionBody) { this.questionBody = questionBody; }
        
        public FieldDifference getDifficulty() { return difficulty; }
        public void setDifficulty(FieldDifference difficulty) { this.difficulty = difficulty; }
        
        public FieldDifference getQuestionType() { return questionType; }
        public void setQuestionType(FieldDifference questionType) { this.questionType = questionType; }
    }
    
    // Constructors
    public VersionComparisonDTO() {}
    
    public VersionComparisonDTO(VersionInfo fromVersion, VersionInfo toVersion, 
                              Map<String, FieldDifference> differences) {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.differences = differences;
    }
    
    // Getters and Setters
    public VersionInfo getFromVersion() { return fromVersion; }
    public void setFromVersion(VersionInfo fromVersion) { this.fromVersion = fromVersion; }
    
    public VersionInfo getToVersion() { return toVersion; }
    public void setToVersion(VersionInfo toVersion) { this.toVersion = toVersion; }
    
    public Map<String, FieldDifference> getDifferences() { return differences; }
    public void setDifferences(Map<String, FieldDifference> differences) { this.differences = differences; }
}
