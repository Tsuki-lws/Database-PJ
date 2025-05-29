package com.llm.eval.service.impl;

import com.llm.eval.model.DatasetVersion;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.repository.DatasetVersionRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.service.DatasetVersionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DatasetVersionServiceImpl implements DatasetVersionService {
    
    private final DatasetVersionRepository datasetVersionRepository;
    private final StandardQuestionRepository standardQuestionRepository;
    
    @Autowired
    public DatasetVersionServiceImpl(
            DatasetVersionRepository datasetVersionRepository,
            StandardQuestionRepository standardQuestionRepository) {
        this.datasetVersionRepository = datasetVersionRepository;
        this.standardQuestionRepository = standardQuestionRepository;
    }
    
    @Override
    public List<DatasetVersion> getAllDatasetVersions() {
        return datasetVersionRepository.findAll();
    }
    
    @Override
    public List<DatasetVersion> getPublishedDatasetVersions() {
        return datasetVersionRepository.findByIsPublishedTrue();
    }
    
    @Override
    public Optional<DatasetVersion> getDatasetVersionById(Integer id) {
        return datasetVersionRepository.findById(id);
    }
    
    @Override
    public Set<StandardQuestion> getQuestionsInDatasetVersion(Integer versionId) {
        DatasetVersion version = datasetVersionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + versionId));
        
        return version.getQuestions();
    }
    
    @Override
    @Transactional
    public DatasetVersion createDatasetVersion(DatasetVersion datasetVersion) {
        // Set default values
        if (datasetVersion.getCreatedAt() == null) {
            datasetVersion.setCreatedAt(LocalDateTime.now());
        }
        datasetVersion.setUpdatedAt(LocalDateTime.now());
        
        // Default to not published
        if (datasetVersion.getIsPublished() == null) {
            datasetVersion.setIsPublished(false);
        }
        
        // Initialize question count
        if (datasetVersion.getQuestions() != null) {
            datasetVersion.setQuestionCount(datasetVersion.getQuestions().size());
        } else {
            datasetVersion.setQuestionCount(0);
            datasetVersion.setQuestions(new HashSet<>());
        }
        
        return datasetVersionRepository.save(datasetVersion);
    }
    
    @Override
    @Transactional
    public DatasetVersion updateDatasetVersion(Integer id, DatasetVersion datasetVersion) {
        DatasetVersion existingVersion = datasetVersionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + id));
        
        // Update basic properties
        existingVersion.setName(datasetVersion.getName());
        existingVersion.setDescription(datasetVersion.getDescription());
        existingVersion.setReleaseDate(datasetVersion.getReleaseDate());
        existingVersion.setUpdatedAt(LocalDateTime.now());
        
        // Don't update isPublished or questions here
        
        return datasetVersionRepository.save(existingVersion);
    }
    
    @Override
    @Transactional
    public DatasetVersion addQuestionsToDatasetVersion(Integer versionId, Set<Integer> questionIds) {
        DatasetVersion version = datasetVersionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + versionId));
        
        Set<StandardQuestion> questionsToAdd = standardQuestionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toSet());
        
        // Create a new set if it's null
        if (version.getQuestions() == null) {
            version.setQuestions(new HashSet<>());
        }
        
        // Add all questions
        version.getQuestions().addAll(questionsToAdd);
        
        // Update question count
        version.setQuestionCount(version.getQuestions().size());
        version.setUpdatedAt(LocalDateTime.now());
        
        return datasetVersionRepository.save(version);
    }
    
    @Override
    @Transactional
    public DatasetVersion removeQuestionsFromDatasetVersion(Integer versionId, Set<Integer> questionIds) {
        DatasetVersion version = datasetVersionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + versionId));
        
        // If no questions, nothing to remove
        if (version.getQuestions() == null || version.getQuestions().isEmpty()) {
            return version;
        }
        
        // Remove the specified questions
        version.setQuestions(
                version.getQuestions().stream()
                        .filter(q -> !questionIds.contains(q.getStandardQuestionId()))
                        .collect(Collectors.toSet())
        );
        
        // Update question count
        version.setQuestionCount(version.getQuestions().size());
        version.setUpdatedAt(LocalDateTime.now());
        
        return datasetVersionRepository.save(version);
    }
    
    @Override
    @Transactional
    public DatasetVersion publishDatasetVersion(Integer id) {
        DatasetVersion version = datasetVersionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + id));
        
        // Cannot publish a version with no questions
        if (version.getQuestions() == null || version.getQuestions().isEmpty()) {
            throw new IllegalStateException("Cannot publish a dataset version with no questions");
        }
        
        // Set published state
        version.setIsPublished(true);
        
        // Set release date if not already set
        if (version.getReleaseDate() == null) {
            version.setReleaseDate(LocalDate.now());
        }
        
        version.setUpdatedAt(LocalDateTime.now());
        
        return datasetVersionRepository.save(version);
    }
    
    @Override
    @Transactional
    public void deleteDatasetVersion(Integer id) {
        datasetVersionRepository.deleteById(id);
    }
} 