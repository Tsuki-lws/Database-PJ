package com.llm.eval.repository;

import com.llm.eval.model.AnswerKeyPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 答案关键点数据访问接口
 */
@Repository
public interface AnswerKeyPointRepository extends JpaRepository<AnswerKeyPoint, Integer> {
    
    /**
     * 根据标准答案ID查询关键点
     * 
     * @param standardAnswerId 标准答案ID
     * @return 关键点列表
     */
    List<AnswerKeyPoint> findByStandardAnswerStandardAnswerId(Integer standardAnswerId);
    
    /**
     * 根据重要性级别查询关键点
     * 
     * @param importance 重要性级别
     * @return 关键点列表
     */
    List<AnswerKeyPoint> findByImportance(Integer importance);
    
    /**
     * 删除某个标准答案的所有关键点
     * 
     * @param standardAnswerId 标准答案ID
     */
    void deleteByStandardAnswerStandardAnswerId(Integer standardAnswerId);
} 