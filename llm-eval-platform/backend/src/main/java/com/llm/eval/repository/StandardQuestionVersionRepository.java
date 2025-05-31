package com.llm.eval.repository;

import com.llm.eval.model.StandardQuestionVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标准问题版本Repository
 */
@Repository
public interface StandardQuestionVersionRepository extends JpaRepository<StandardQuestionVersion, Integer> {
      /**
     * 根据标准问题ID查找所有版本
     */
    List<StandardQuestionVersion> findByStandardQuestionIdOrderByVersionDesc(Integer standardQuestionId);
    
    /**
     * 根据标准问题ID分页查找版本
     */
    Page<StandardQuestionVersion> findByStandardQuestionIdOrderByVersionDesc(Integer standardQuestionId, Pageable pageable);
    
    /**
     * 根据标准问题ID和版本号查找
     */
    Optional<StandardQuestionVersion> findByStandardQuestionIdAndVersion(Integer standardQuestionId, Integer version);
      /**
     * 获取标准问题的最新版本
     */
    @Query("SELECT sqv FROM StandardQuestionVersion sqv WHERE " +
           "sqv.standardQuestionId = :standardQuestionId " +
           "ORDER BY sqv.version DESC")
    Optional<StandardQuestionVersion> findLatestVersionByQuestionId(@Param("standardQuestionId") Integer standardQuestionId);
    
    /**
     * 获取标准问题的最大版本号
     */
    @Query("SELECT MAX(sqv.version) FROM StandardQuestionVersion sqv WHERE " +
           "sqv.standardQuestionId = :standardQuestionId")
    Optional<Integer> findMaxVersionByQuestionId(@Param("standardQuestionId") Integer standardQuestionId);
    
    /**
     * 根据修改人查找版本
     */
    List<StandardQuestionVersion> findByChangedByOrderByCreatedAtDesc(Integer changedBy);
    
    /**
     * 查找特定时间范围内的版本
     */
    @Query("SELECT sqv FROM StandardQuestionVersion sqv WHERE " +
           "sqv.createdAt >= :startTime AND sqv.createdAt <= :endTime " +
           "ORDER BY sqv.createdAt DESC")
    List<StandardQuestionVersion> findVersionsInTimeRange(@Param("startTime") java.time.LocalDateTime startTime, 
                                                         @Param("endTime") java.time.LocalDateTime endTime);    /**
     * 统计标准问题的版本数量
     */
    @Query("SELECT COUNT(sqv) FROM StandardQuestionVersion sqv WHERE " +
           "sqv.standardQuestionId = :standardQuestionId")
    long countVersionsByQuestionId(@Param("standardQuestionId") Integer standardQuestionId);
}
