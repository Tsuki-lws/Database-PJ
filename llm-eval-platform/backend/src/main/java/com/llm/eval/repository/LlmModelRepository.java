package com.llm.eval.repository;

import com.llm.eval.model.LlmModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LlmModelRepository extends JpaRepository<LlmModel, Integer> {
    
    Optional<LlmModel> findByNameAndVersion(String name, String version);
    
    boolean existsByNameAndVersion(String name, String version);
} 