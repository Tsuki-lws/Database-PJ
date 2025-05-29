package com.llm.eval.service;

import com.llm.eval.model.DatasetVersion;
import com.llm.eval.model.StandardQuestion;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DatasetVersionService {
    
    /**
     * Get all dataset versions
     * 
     * @return List of all dataset versions
     */
    List<DatasetVersion> getAllDatasetVersions();
    
    /**
     * Get published dataset versions
     * 
     * @return List of published dataset versions
     */
    List<DatasetVersion> getPublishedDatasetVersions();
    
    /**
     * Get a dataset version by ID
     * 
     * @param id The ID of the dataset version
     * @return The dataset version if found
     */
    Optional<DatasetVersion> getDatasetVersionById(Integer id);
    
    /**
     * Get questions in a dataset version
     * 
     * @param versionId The ID of the dataset version
     * @return List of questions in the dataset version
     */
    Set<StandardQuestion> getQuestionsInDatasetVersion(Integer versionId);
    
    /**
     * Create a new dataset version
     * 
     * @param datasetVersion The dataset version to create
     * @return The created dataset version
     */
    DatasetVersion createDatasetVersion(DatasetVersion datasetVersion);
    
    /**
     * Update a dataset version
     * 
     * @param id The ID of the dataset version to update
     * @param datasetVersion The updated dataset version
     * @return The updated dataset version
     */
    DatasetVersion updateDatasetVersion(Integer id, DatasetVersion datasetVersion);
    
    /**
     * Add questions to a dataset version
     * 
     * @param versionId The ID of the dataset version
     * @param questionIds The IDs of the questions to add
     * @return The updated dataset version
     */
    DatasetVersion addQuestionsToDatasetVersion(Integer versionId, Set<Integer> questionIds);
    
    /**
     * Remove questions from a dataset version
     * 
     * @param versionId The ID of the dataset version
     * @param questionIds The IDs of the questions to remove
     * @return The updated dataset version
     */
    DatasetVersion removeQuestionsFromDatasetVersion(Integer versionId, Set<Integer> questionIds);
    
    /**
     * Publish a dataset version
     * 
     * @param id The ID of the dataset version to publish
     * @return The published dataset version
     */
    DatasetVersion publishDatasetVersion(Integer id);
    
    /**
     * Delete a dataset version
     * 
     * @param id The ID of the dataset version to delete
     */
    void deleteDatasetVersion(Integer id);
} 