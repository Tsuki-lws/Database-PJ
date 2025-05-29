package com.llm.eval.repository;

import com.llm.eval.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    
    Optional<Tag> findByTagName(String tagName);
    
    boolean existsByTagName(String tagName);
    
    @Query("SELECT t FROM Tag t JOIN t.questions q WHERE q.standardQuestionId = :questionId")
    List<Tag> findByQuestionId(Integer questionId);
} 