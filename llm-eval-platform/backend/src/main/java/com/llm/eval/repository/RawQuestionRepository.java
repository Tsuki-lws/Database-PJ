package com.llm.eval.repository;

import com.llm.eval.model.RawQuestion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface RawQuestionRepository extends JpaRepository<RawQuestion, Integer>, JpaSpecificationExecutor<RawQuestion> {
    
    @Query("SELECT COUNT(rq) FROM RawQuestion rq")
    long countRawQuestions();
    
    @Query("SELECT COUNT(rq) FROM RawQuestion rq WHERE rq.source = :source")
    long countBySource(String source);

    /**
     * 根据源问题ID查询原始问题
     * @param sourceQuestionId 源问题ID
     * @return 原始问题
     */
    Optional<RawQuestion> findBySourceQuestionId(Integer sourceQuestionId);
    
    /**
     * 查询所有不同的来源
     * @return 来源列表
     */
    @Query("SELECT DISTINCT rq.source FROM RawQuestion rq WHERE rq.source IS NOT NULL AND rq.source <> ''")
    List<String> findDistinctSources();
    
    /**
     * 按来源统计问题数量
     * @return 来源-数量映射
     */
    @Query("SELECT rq.source as source, COUNT(rq) as count FROM RawQuestion rq WHERE rq.source IS NOT NULL GROUP BY rq.source")
    List<Object[]> countGroupBySource();
    
    /**
     * 按来源统计问题数量（转换为Map）
     * @return 来源-数量映射
     */
    default Map<String, Long> countBySource() {
        List<Object[]> results = countGroupBySource();
        Map<String, Long> countsMap = new java.util.HashMap<>();
        
        for (Object[] result : results) {
            String source = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            countsMap.put(source, count);
        }
        
        return countsMap;
    }
    
    /**
     * 根据关键字和来源查询问题
     * @param titleKeyword 标题关键字
     * @param bodyKeyword 内容关键字 
     * @param source 来源
     * @param pageable 分页参数
     * @return 问题列表
     */
    @Query("SELECT rq FROM RawQuestion rq WHERE (LOWER(rq.questionTitle) LIKE LOWER(:titleKeyword) OR LOWER(rq.questionBody) LIKE LOWER(:bodyKeyword)) AND rq.source = :source")
    List<RawQuestion> findByKeywordAndSource(
        @Param("titleKeyword") String titleKeyword, 
        @Param("bodyKeyword") String bodyKeyword, 
        @Param("source") String source, 
        Pageable pageable
    );
    
    /**
     * 统计符合关键字和来源的问题数量
     * @param titleKeyword 标题关键字
     * @param bodyKeyword 内容关键字
     * @param source 来源
     * @return 问题数量
     */
    @Query("SELECT COUNT(rq) FROM RawQuestion rq WHERE (LOWER(rq.questionTitle) LIKE LOWER(:titleKeyword) OR LOWER(rq.questionBody) LIKE LOWER(:bodyKeyword)) AND rq.source = :source")
    long countByKeywordAndSource(
        @Param("titleKeyword") String titleKeyword, 
        @Param("bodyKeyword") String bodyKeyword, 
        @Param("source") String source
    );
    
    /**
     * 根据关键字查询问题
     * @param titleKeyword 标题关键字
     * @param bodyKeyword 内容关键字
     * @param pageable 分页参数
     * @return 问题列表
     */
    @Query("SELECT rq FROM RawQuestion rq WHERE LOWER(rq.questionTitle) LIKE LOWER(:titleKeyword) OR LOWER(rq.questionBody) LIKE LOWER(:bodyKeyword)")
    List<RawQuestion> findByKeyword(
        @Param("titleKeyword") String titleKeyword, 
        @Param("bodyKeyword") String bodyKeyword, 
        Pageable pageable
    );
    
    /**
     * 统计符合关键字的问题数量
     * @param titleKeyword 标题关键字
     * @param bodyKeyword 内容关键字
     * @return 问题数量
     */
    @Query("SELECT COUNT(rq) FROM RawQuestion rq WHERE LOWER(rq.questionTitle) LIKE LOWER(:titleKeyword) OR LOWER(rq.questionBody) LIKE LOWER(:bodyKeyword)")
    long countByKeyword(
        @Param("titleKeyword") String titleKeyword, 
        @Param("bodyKeyword") String bodyKeyword
    );
    
    /**
     * 根据来源查询问题
     * @param source 来源
     * @param pageable 分页参数
     * @return 问题列表
     */
    @Query("SELECT rq FROM RawQuestion rq WHERE rq.source = :source")
    List<RawQuestion> findBySource(@Param("source") String source, Pageable pageable);
    
    /**
     * 查询所有问题（带分页）
     * @param pageable 分页参数
     * @return 问题列表
     */
    @Query("SELECT rq FROM RawQuestion rq")
    List<RawQuestion> findAllQuestions(Pageable pageable);
    
    /**
     * 统计所有问题数量
     * @return 问题数量
     */
    @Query("SELECT COUNT(rq) FROM RawQuestion rq")
    long countAllQuestions();
} 