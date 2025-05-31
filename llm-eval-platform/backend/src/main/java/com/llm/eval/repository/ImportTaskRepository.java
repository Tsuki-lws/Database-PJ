package com.llm.eval.repository;

import com.llm.eval.model.ImportTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportTaskRepository extends JpaRepository<ImportTask, Integer> {
    
    /**
     * 查询所有导入任务
     * @param pageable 分页参数
     * @return 导入任务分页结果
     */
    Page<ImportTask> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 根据导入类型查询导入任务
     * @param type 导入类型
     * @param pageable 分页参数
     * @return 导入任务分页结果
     */
    Page<ImportTask> findByTypeOrderByCreatedAtDesc(ImportTask.ImportType type, Pageable pageable);
    
    /**
     * 查询最近一次导入任务
     * @param type 导入类型
     * @return 导入任务
     */
    ImportTask findTopByTypeOrderByCreatedAtDesc(ImportTask.ImportType type);
} 