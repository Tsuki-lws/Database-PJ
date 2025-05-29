package com.llm.eval.repository;

import com.llm.eval.model.DatasetVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetVersionRepository extends JpaRepository<DatasetVersion, Integer> {
    
    @Query("SELECT COUNT(dv) FROM DatasetVersion dv")
    long countDatasetVersions();
    
    @Query("SELECT COUNT(dv) FROM DatasetVersion dv WHERE dv.isPublished = true")
    long countPublishedDatasetVersions();
    
    List<DatasetVersion> findByIsPublishedTrue();
    
    List<DatasetVersion> findByNameContainingIgnoreCase(String name);
} 