package com.llm.eval.repository;

import com.llm.eval.model.DatasetQuestionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据集问题映射Repository
 */
@Repository
public interface DatasetQuestionMappingRepository extends JpaRepository<DatasetQuestionMapping, Integer> {
    
    /**
     * 根据版本ID查找所有问题映射
     */
    List<DatasetQuestionMapping> findByVersionId(Integer versionId);
    
    /**
     * 根据标准问题ID查找所有数据集版本映射
     */
    List<DatasetQuestionMapping> findByStandardQuestionId(Integer standardQuestionId);
    
    /**
     * 根据版本ID和问题ID查找映射
     */
    DatasetQuestionMapping findByVersionIdAndStandardQuestionId(Integer versionId, Integer standardQuestionId);
    
    /**
     * 检查版本中是否包含特定问题
     */
    boolean existsByVersionIdAndStandardQuestionId(Integer versionId, Integer standardQuestionId);
      /**
     * 统计版本中的问题数量
     */
    @Query("SELECT COUNT(dqm) FROM DatasetQuestionMapping dqm WHERE dqm.versionId = :versionId")
    long countQuestionsByVersionId(@Param("versionId") Integer versionId);
    
    /**
     * 获取版本中的所有问题ID
     */
    @Query("SELECT dqm.standardQuestionId FROM DatasetQuestionMapping dqm WHERE dqm.versionId = :versionId")
    List<Integer> findQuestionIdsByVersionId(@Param("versionId") Integer versionId);
    
    /**
     * 删除版本中的特定问题
     */
    void deleteByVersionIdAndStandardQuestionId(Integer versionId, Integer standardQuestionId);
    
    /**
     * 删除版本中的所有问题
     */
    void deleteByVersionId(Integer versionId);
}
