package com.llm.eval.repository;

import com.llm.eval.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    
    Optional<Tag> findByTagName(String tagName);
    
    boolean existsByTagName(String tagName);
    
    @Query("SELECT t FROM Tag t JOIN t.questions q WHERE q.standardQuestionId = :questionId")
    List<Tag> findByQuestionId(Integer questionId);
    
    /**
     * 检查问题和标签之间是否存在关联
     * 
     * @param questionId 问题ID
     * @param tagId 标签ID
     * @return 如果存在关联则返回true，否则返回false
     */
    @Query(value = "SELECT COUNT(*) FROM standard_question_tags WkaERE standard_question_id = :questionId AND tag_id = :tagId", nativeQuery = true)
    int countQuestionTagMapping(@Param("questionId") Integer questionId, @Param("tagId") Integer tagId);
    
    /**
     * 删除问题和标签之间的关联
     * 
     * @param questionId 问题ID
     * @param tagId 标签ID
     * @return 受影响的行数
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM standard_question_tags WHERE standard_question_id = :questionId AND tag_id = :tagId", nativeQuery = true)
    int deleteQuestionTagMapping(@Param("questionId") Integer questionId, @Param("tagId") Integer tagId);
    
    /**
     * 向关联表中插入问题和标签的关联
     * 
     * @param questionId 问题ID
     * @param tagId 标签ID
     * @return 受影响的行数
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO standard_question_tags (standard_question_id, tag_id) VALUES (:questionId, :tagId)", nativeQuery = true)
    int insertQuestionTagMapping(@Param("questionId") Integer questionId, @Param("tagId") Integer tagId);
    
    /**
     * 查询关联表中的记录
     * 
     * @param questionId 问题ID
     * @param tagId 标签ID
     * @return 关联表中的记录ID，如果不存在则返回null
     */
    @Query(value = "SELECT mapping_id FROM standard_question_tags WHERE standard_question_id = :questionId AND tag_id = :tagId", nativeQuery = true)
    Integer findMappingId(@Param("questionId") Integer questionId, @Param("tagId") Integer tagId);
    
    /**
     * 查询标准问题标签关联表中的记录
     * 
     * @param questionId 问题ID
     * @param tagId 标签ID
     * @return 关联表中的记录列表
     */
    @Query(nativeQuery = true, value = "SELECT * FROM standard_question_tags WHERE standard_question_id = ?1 AND tag_id = ?2")
    List<Object[]> findQuestionTagMappings(Integer questionId, Integer tagId);
} 