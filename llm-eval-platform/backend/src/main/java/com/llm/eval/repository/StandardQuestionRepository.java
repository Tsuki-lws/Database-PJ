package com.llm.eval.repository;

import com.llm.eval.model.StandardQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StandardQuestionRepository extends JpaRepository<StandardQuestion, Integer> {
    
    @Query("SELECT COUNT(sq) FROM StandardQuestion sq")
    long countStandardQuestions();
    
    @Query("SELECT COUNT(sq) FROM StandardQuestion sq WHERE sq.category.categoryId = :categoryId")
    long countByCategoryId(Integer categoryId);
    
    @Query("SELECT sq FROM StandardQuestion sq JOIN sq.tags t WHERE t.tagId = :tagId")
    List<StandardQuestion> findByTagId(Integer tagId);
    
    @Query("SELECT COUNT(sq) FROM StandardQuestion sq JOIN sq.tags t WHERE t.tagId = :tagId")
    long countByTagId(Integer tagId);
    
    @Query("SELECT sq FROM StandardQuestion sq WHERE sq.standardQuestionId NOT IN " +
           "(SELECT sa.standardQuestion.standardQuestionId FROM StandardAnswer sa WHERE sa.isFinal = true)")
    List<StandardQuestion> findWithoutStandardAnswers();
    
    @Query("SELECT sq FROM StandardQuestion sq WHERE sq.standardQuestionId NOT IN " +
           "(SELECT sa.standardQuestion.standardQuestionId FROM StandardAnswer sa WHERE sa.isFinal = true)")
    Page<StandardQuestion> findWithoutStandardAnswers(Pageable pageable);
    
    @Query("SELECT COUNT(sq) FROM StandardQuestion sq WHERE sq.standardQuestionId NOT IN " +
           "(SELECT sa.standardQuestion.standardQuestionId FROM StandardAnswer sa WHERE sa.isFinal = true)")
    long countWithoutStandardAnswers();
    
    List<StandardQuestion> findByCategoryCategoryId(Integer categoryId);
    
    @Query("SELECT sq FROM StandardQuestion sq JOIN sq.tags t WHERE t.tagName = :tagName")
    List<StandardQuestion> findByTagName(String tagName);
    
    /**
     * 根据问题文本和源问题ID查询标准问题
     * @param question 问题文本
     * @param sourceQuestionId 源问题ID
     * @return 标准问题
     */
    @Query("SELECT sq FROM StandardQuestion sq WHERE sq.question = :question AND " +
           "((:sourceQuestionId IS NULL AND sq.sourceQuestion IS NULL) OR " +
           "(sq.sourceQuestion.questionId = :sourceQuestionId))")
    Optional<StandardQuestion> findByQuestionAndSourceQuestionId(String question, Integer sourceQuestionId);
    
    /**
     * 分页查询没有标准答案的问题，支持按分类、类型、难度过滤
     */
    @Query("SELECT sq FROM StandardQuestion sq WHERE sq.standardQuestionId NOT IN " +
           "(SELECT sa.standardQuestion.standardQuestionId FROM StandardAnswer sa WHERE sa.isFinal = true) " +
           "AND (:categoryId IS NULL OR sq.category.categoryId = :categoryId) " +
           "AND (:questionType IS NULL OR sq.questionType = :questionType) " +
           "AND (:difficulty IS NULL OR sq.difficulty = :difficulty)")
    Page<StandardQuestion> findWithoutStandardAnswersWithFilters(
            @Param("categoryId") Integer categoryId,
            @Param("questionType") StandardQuestion.QuestionType questionType,
            @Param("difficulty") StandardQuestion.DifficultyLevel difficulty,
            Pageable pageable);
    
    /**
     * 统计没有标准答案的问题数量，支持按分类、类型、难度过滤
     */
    @Query("SELECT COUNT(sq) FROM StandardQuestion sq WHERE sq.standardQuestionId NOT IN " +
           "(SELECT sa.standardQuestion.standardQuestionId FROM StandardAnswer sa WHERE sa.isFinal = true) " +
           "AND (:categoryId IS NULL OR sq.category.categoryId = :categoryId) " +
           "AND (:questionType IS NULL OR sq.questionType = :questionType) " +
           "AND (:difficulty IS NULL OR sq.difficulty = :difficulty)")
    long countWithoutStandardAnswersWithFilters(
            @Param("categoryId") Integer categoryId,
            @Param("questionType") StandardQuestion.QuestionType questionType,
            @Param("difficulty") StandardQuestion.DifficultyLevel difficulty);
    
    /**
     * 分页查询标准问题，支持按分类、类型、难度和关键词过滤
     */
    @Query("SELECT DISTINCT sq FROM StandardQuestion sq LEFT JOIN FETCH sq.tags WHERE " +
           "(:categoryId IS NULL OR sq.category.categoryId = :categoryId) AND " +
           "(:questionType IS NULL OR sq.questionType = :questionType) AND " +
           "(:difficulty IS NULL OR sq.difficulty = :difficulty) AND " +
           "(:keyword IS NULL OR LOWER(sq.question) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<StandardQuestion> findByFilters(
            @Param("categoryId") Integer categoryId,
            @Param("questionType") StandardQuestion.QuestionType questionType,
            @Param("difficulty") StandardQuestion.DifficultyLevel difficulty,
            @Param("keyword") String keyword,
            Pageable pageable);
    
    /**
     * 查询没有分类的问题
     * @param pageable 分页参数
     * @return 分页的没有分类的问题列表
     */
    Page<StandardQuestion> findByCategoryIsNull(Pageable pageable);
    
    /**
     * 统计没有分类的问题数量
     * @return 没有分类的问题数量
     */
    @Query("SELECT COUNT(sq) FROM StandardQuestion sq WHERE sq.category IS NULL")
    long countByCategoryIsNull();
    
    /**
     * 查询具有所有指定标签的问题ID
     * 
     * @param tagIds 标签ID列表
     * @return 包含所有指定标签的问题ID列表
     */
    @Query("SELECT sq.standardQuestionId FROM StandardQuestion sq JOIN sq.tags t " +
           "WHERE t.tagId IN :tagIds " +
           "GROUP BY sq.standardQuestionId " +
           "HAVING COUNT(DISTINCT t.tagId) = :tagCount")
    List<Integer> findByAllTagIds(@Param("tagIds") List<Integer> tagIds, @Param("tagCount") long tagCount);
    
    /**
     * 查询具有所有指定标签的问题ID
     * 
     * @param tagIds 标签ID列表
     * @return 包含所有指定标签的问题ID列表
     */
    @Query(value = "SELECT sq.standardQuestionId FROM StandardQuestion sq JOIN sq.tags t " +
           "WHERE t.tagId IN :tagIds " +
           "GROUP BY sq.standardQuestionId " +
           "HAVING COUNT(DISTINCT t.tagId) = :#{#tagIds.size()}")
    List<Integer> findByAllTagIds(@Param("tagIds") List<Integer> tagIds);
} 