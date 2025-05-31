package com.llm.eval.repository;

import com.llm.eval.model.DatasetVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetVersionRepository extends JpaRepository<DatasetVersion, Integer> {
    
    @Query("SELECT COUNT(dv) FROM DatasetVersion dv")
    long countDatasetVersions();
    
    @Query("SELECT COUNT(dv) FROM DatasetVersion dv WHERE dv.isPublished = true")
    long countPublishedDatasetVersions();
    
    // 版本管理相关方法
    
    /**
     * 查找所有版本并按创建时间倒序排列
     */
    List<DatasetVersion> findAllByOrderByCreatedAtDesc();
    
    /**
     * 分页查询所有版本
     */
    Page<DatasetVersion> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 根据名称查找版本
     */
    Optional<DatasetVersion> findByName(String name);
    
    /**
     * 检查版本名称是否已存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据发布状态分页查询版本
     */
    Page<DatasetVersion> findByIsPublished(Boolean isPublished, Pageable pageable);
    
    /**
     * 查找已发布的版本
     */
    List<DatasetVersion> findByIsPublishedTrue();
    
    /**
     * 查找已发布的版本并按创建时间倒序排列
     */
    List<DatasetVersion> findByIsPublishedTrueOrderByCreatedAtDesc();
    
    /**
     * 查找最新的版本
     */
    @Query("SELECT dv FROM DatasetVersion dv ORDER BY dv.createdAt DESC")
    Optional<DatasetVersion> findLatestVersion();
    
    /**
     * 根据问题ID查找包含该问题的所有数据集版本
     */
    @Query("SELECT DISTINCT dv FROM DatasetVersion dv " +
           "JOIN dv.questions q WHERE q.standardQuestionId = :questionId")
    List<DatasetVersion> findByQuestionId(@Param("questionId") Integer questionId);
}