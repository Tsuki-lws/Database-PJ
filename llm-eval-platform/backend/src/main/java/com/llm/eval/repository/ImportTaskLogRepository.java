package com.llm.eval.repository;

import com.llm.eval.model.ImportTaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportTaskLogRepository extends JpaRepository<ImportTaskLog, Integer> {
    
    /**
     * 根据导入任务ID查询日志列表
     * @param importId 导入任务ID
     * @return 日志列表
     */
    List<ImportTaskLog> findByImportTaskImportIdOrderByLogTimeAsc(Integer importId);
} 