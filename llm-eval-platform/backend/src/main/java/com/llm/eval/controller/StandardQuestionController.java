package com.llm.eval.controller;

import com.llm.eval.dto.PagedResponseDTO;
import com.llm.eval.dto.StandardQuestionWithoutAnswerDTO;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.QuestionCategory;
import com.llm.eval.service.StandardQuestionService;
import com.llm.eval.repository.QuestionCategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Standard Question API", description = "标准问题管理接口")
public class StandardQuestionController {
    
    private static final Logger logger = LoggerFactory.getLogger(StandardQuestionController.class);
    
    private final StandardQuestionService questionService;
    private final QuestionCategoryRepository categoryRepository;

    public StandardQuestionController(StandardQuestionService questionService, QuestionCategoryRepository categoryRepository) {
        this.questionService = questionService;
        this.categoryRepository = categoryRepository;
    }

    /**
     * 分页获取标准问题列表，支持多种过滤条件
     * 
     * 示例请求:
     * GET /api/questions?page=1&size=10&tagId=5&categoryId=1&questionType=subjective&difficulty=medium&keyword=Spring&sortBy=createdAt&sortDir=desc
     * 
     * @param page 页码，从1开始
     * @param size 每页大小
     * @param tagId 标签ID（可选）
     * @param categoryId 分类ID（可选）
     * @param questionType 问题类型（可选）：single_choice, multiple_choice, simple_fact, subjective
     * @param difficulty 难度级别（可选）：easy, medium, hard
     * @param keyword 关键词搜索（可选）
     * @param sortBy 排序字段（可选），默认createdAt
     * @param sortDir 排序方向（可选），asc或desc，默认desc
     * @return 分页的标准问题列表
     */
    @GetMapping
    @Operation(summary = "分页获取标准问题列表")
    public ResponseEntity<Map<String, Object>> getStandardQuestions(
            @Parameter(description = "页码（从1开始）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "标签ID") @RequestParam(required = false) Integer tagId,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "问题类型") @RequestParam(required = false) StandardQuestion.QuestionType questionType,
            @Parameter(description = "难度级别") @RequestParam(required = false) StandardQuestion.DifficultyLevel difficulty,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        
        try {
            logger.debug("分页获取标准问题列表，参数: page={}, size={}, tagId={}, categoryId={}, questionType={}, difficulty={}, keyword={}, sortBy={}, sortDir={}",
                    page, size, tagId, categoryId, questionType, difficulty, keyword, sortBy, sortDir);
            
            // 构建排序
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            
            // 创建分页请求（注意：Spring Data JPA的页码从0开始，而API的页码从1开始）
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            
            // 调用服务层查询，标签会在服务层自动加载
            Page<StandardQuestion> questionPage;
            if (tagId != null) {
                // 如果提供了标签ID，使用带标签过滤的查询
                questionPage = questionService.getStandardQuestionsByPageWithTag(
                        tagId, categoryId, questionType, difficulty, keyword, pageable);
            } else {
                // 否则使用普通查询
                questionPage = questionService.getStandardQuestionsByPage(
                        categoryId, questionType, difficulty, keyword, pageable);
            }
            
            // 标签已在服务层被加载
            List<StandardQuestion> questions = questionPage.getContent();
            
            // 将实体对象转换为简单的Map对象，避免循环引用问题
            List<Map<String, Object>> questionDTOs = questions.stream().map(q -> {
                Map<String, Object> questionDTO = new HashMap<>();
                questionDTO.put("standardQuestionId", q.getStandardQuestionId());
                questionDTO.put("question", q.getQuestion());
                questionDTO.put("questionType", q.getQuestionType());
                questionDTO.put("difficulty", q.getDifficulty());
                questionDTO.put("status", q.getStatus());
                questionDTO.put("version", q.getVersion());
                questionDTO.put("createdAt", q.getCreatedAt());
                questionDTO.put("updatedAt", q.getUpdatedAt());
                
                // 添加分类信息（如果存在）
                if (q.getCategory() != null) {
                    Map<String, Object> categoryDTO = new HashMap<>();
                    categoryDTO.put("categoryId", q.getCategory().getCategoryId());
                    categoryDTO.put("categoryName", q.getCategory().getName());
                    questionDTO.put("category", categoryDTO);
                } else {
                    questionDTO.put("category", null);
                }
                
                // 添加标签信息
                if (q.getTags() != null && !q.getTags().isEmpty()) {
                    List<Map<String, Object>> tagDTOs = q.getTags().stream().map(tag -> {
                        Map<String, Object> tagDTO = new HashMap<>();
                        tagDTO.put("tagId", tag.getTagId());
                        tagDTO.put("tagName", tag.getTagName());
                        // 使用标签自身的颜色，如果为空则使用默认颜色
                        tagDTO.put("color", tag.getColor() != null ? tag.getColor() : "#409EFF");
                        return tagDTO;
                    }).collect(Collectors.toList());
                    questionDTO.put("tags", tagDTOs);
                } else {
                    questionDTO.put("tags", new ArrayList<>());
                }
                
                // 不包含标准答案和其他复杂关联，避免循环引用
                return questionDTO;
            }).collect(Collectors.toList());
            
            // 构建返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("content", questionDTOs);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("total", questionPage.getTotalElements());
            response.put("pages", questionPage.getTotalPages());
            
            logger.debug("查询成功，总记录数: {}, 总页数: {}", questionPage.getTotalElements(), questionPage.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("分页获取标准问题列表失败", e);
            throw new RuntimeException("获取标准问题列表失败: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取标准问题")
    public ResponseEntity<Map<String, Object>> getQuestionById(@PathVariable("id") Integer id) {
        try {
            logger.debug("根据ID获取标准问题，ID: {}", id);
            return questionService.getStandardQuestionById(id)
                    .map(q -> {
                        // 将实体对象转换为简单的Map对象，避免循环引用问题
                        Map<String, Object> questionDTO = new HashMap<>();
                        questionDTO.put("standardQuestionId", q.getStandardQuestionId());
                        questionDTO.put("question", q.getQuestion());
                        questionDTO.put("questionType", q.getQuestionType());
                        questionDTO.put("difficulty", q.getDifficulty());
                        questionDTO.put("status", q.getStatus());
                        questionDTO.put("version", q.getVersion());
                        questionDTO.put("createdAt", q.getCreatedAt());
                        questionDTO.put("updatedAt", q.getUpdatedAt());
                        
                        // 添加分类信息（如果存在）
                        if (q.getCategory() != null) {
                            Map<String, Object> categoryDTO = new HashMap<>();
                            categoryDTO.put("categoryId", q.getCategory().getCategoryId());
                            categoryDTO.put("categoryName", q.getCategory().getName());
                            questionDTO.put("category", categoryDTO);
                        } else {
                            questionDTO.put("category", null);
                        }
                        
                        // 添加标签信息
                        if (q.getTags() != null && !q.getTags().isEmpty()) {
                            List<Map<String, Object>> tagDTOs = q.getTags().stream().map(tag -> {
                                Map<String, Object> tagDTO = new HashMap<>();
                                tagDTO.put("tagId", tag.getTagId());
                                tagDTO.put("tagName", tag.getTagName());
                                // 使用标签自身的颜色，如果为空则使用默认颜色
                                tagDTO.put("color", tag.getColor() != null ? tag.getColor() : "#409EFF");
                                return tagDTO;
                            }).collect(Collectors.toList());
                            questionDTO.put("tags", tagDTOs);
                        } else {
                            questionDTO.put("tags", new ArrayList<>());
                        }
                        
                        // 添加源问题信息（如果存在）
                        if (q.getSourceQuestion() != null) {
                            Map<String, Object> sourceDTO = new HashMap<>();
                            sourceDTO.put("questionId", q.getSourceQuestion().getQuestionId());
                            sourceDTO.put("questionTitle", q.getSourceQuestion().getQuestionTitle());
                            questionDTO.put("sourceQuestion", sourceDTO);
                        } else {
                            questionDTO.put("sourceQuestion", null);
                        }
                        
                        // 添加标准答案信息（简化版本，避免循环引用）
                        if (q.getStandardAnswers() != null && !q.getStandardAnswers().isEmpty()) {
                            List<Map<String, Object>> answerDTOs = q.getStandardAnswers().stream().map(answer -> {
                                Map<String, Object> answerDTO = new HashMap<>();
                                answerDTO.put("standardAnswerId", answer.getStandardAnswerId());
                                answerDTO.put("answer", answer.getAnswer());
                                answerDTO.put("isFinal", answer.getIsFinal());
                                return answerDTO;
                            }).collect(Collectors.toList());
                            questionDTO.put("standardAnswers", answerDTOs);
                        } else {
                            questionDTO.put("standardAnswers", new ArrayList<>());
                        }
                        
                        logger.debug("获取成功，问题ID: {}", id);
                        return ResponseEntity.ok(questionDTO);
                    })
                    .orElseGet(() -> {
                        logger.warn("未找到问题，ID: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("根据ID获取标准问题失败，ID: {}", id, e);
            throw new RuntimeException("获取标准问题失败: " + e.getMessage(), e);
        }
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "获取特定分类下的问题")
    public ResponseEntity<List<com.llm.eval.model.StandardQuestion>> getQuestionsByCategory(
            @PathVariable("categoryId") Integer categoryId) {
        return ResponseEntity.ok(questionService.getStandardQuestionsByCategoryId(categoryId));
    }

    @GetMapping("/tag/{tagId}")
    @Operation(summary = "获取特定标签下的问题")
    public ResponseEntity<List<com.llm.eval.model.StandardQuestion>> getQuestionsByTag(
            @PathVariable("tagId") Integer tagId) {
        return ResponseEntity.ok(questionService.getStandardQuestionsByTagId(tagId));
    }    @GetMapping("/without-answer")
    @Operation(summary = "获取无标准答案的问题")
    public ResponseEntity<List<StandardQuestion>> getQuestionsWithoutAnswer() {
        List<StandardQuestion> questions = questionService.getQuestionsWithoutStandardAnswers();
        
        // 进行严格过滤，只保留真正没有标准答案的问题
        List<StandardQuestion> filteredQuestions = questions.stream()
                .filter(q -> q.getStandardAnswers() == null || q.getStandardAnswers().isEmpty())
                .toList();
        
        // 记录被过滤掉的问题
        if (filteredQuestions.size() < questions.size()) {
            logger.warn("从结果中过滤掉了 {} 个错误包含的有标准答案的问题", questions.size() - filteredQuestions.size());
        }
        
        return ResponseEntity.ok(filteredQuestions);
    }
    
    @GetMapping("/without-answer/paged")
    @Operation(summary = "分页获取无标准答案的问题")
    public ResponseEntity<PagedResponseDTO<StandardQuestionWithoutAnswerDTO>> getQuestionsWithoutAnswerPaged(
            @Parameter(description = "页码（从1开始）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "问题类型") @RequestParam(required = false) StandardQuestion.QuestionType questionType,
            @Parameter(description = "难度级别") @RequestParam(required = false) StandardQuestion.DifficultyLevel difficulty,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.info("分页获取无标准答案的问题，参数: page={}, size={}, categoryId={}, questionType={}, difficulty={}, sortBy={}, sortDir={}",
                page, size, categoryId, questionType, difficulty, sortBy, sortDir);
        
        // 创建排序对象
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        // 创建分页对象 (Spring分页从0开始)
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        // 获取分页数据
        Page<StandardQuestion> questionPage = questionService.getQuestionsWithoutStandardAnswersWithFilters(
                categoryId, questionType, difficulty, pageable);
        
        // 转换为DTO
        List<StandardQuestionWithoutAnswerDTO> dtoList = questionPage.getContent().stream()
                .map(StandardQuestionWithoutAnswerDTO::fromEntity)
                .toList();
        
        // 进行严格过滤，只保留真正没有标准答案的问题
        List<StandardQuestionWithoutAnswerDTO> filteredList = dtoList.stream()
                .filter(dto -> dto.getStandardAnswers() == null || dto.getStandardAnswers().isEmpty())
                .toList();
        
        // 记录被过滤掉的问题
        if (filteredList.size() < dtoList.size()) {
            logger.warn("从结果中过滤掉了 {} 个错误包含的有标准答案的问题", dtoList.size() - filteredList.size());
            dtoList.stream()
                .filter(dto -> dto.getStandardAnswers() != null && !dto.getStandardAnswers().isEmpty())
                .forEach(dto -> logger.warn("问题ID {} 被错误地包含在无标准答案列表中，它有 {} 个标准答案",
                        dto.getStandardQuestionId(), dto.getStandardAnswers().size()));
        }
        
        // 使用过滤后的列表构建响应
        PagedResponseDTO<StandardQuestionWithoutAnswerDTO> response = 
                PagedResponseDTO.fromPageWithMapper(questionPage, filteredList);
        
        // 更新总数为过滤后的实际数量
        response.setTotal((long) filteredList.size());
        
        logger.info("获取到 {} 个真正无标准答案的问题", filteredList.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "创建新标准问题")
    public ResponseEntity<com.llm.eval.model.StandardQuestion> createQuestion(
            @Valid @RequestBody com.llm.eval.model.StandardQuestion question) {
        // 先处理分类信息
        QuestionCategory category = null;
        
        // 如果前端传入了category对象
        if (question.getCategory() != null) {
            // 如果提供了分类ID，使用现有分类
            if (question.getCategory().getCategoryId() != null) {
                category = categoryRepository.findById(question.getCategory().getCategoryId())
                        .orElse(null);
            } 
            // 如果提供了分类名称，查找或创建分类
            else if (question.getCategory().getName() != null && !question.getCategory().getName().trim().isEmpty()) {
                String categoryName = question.getCategory().getName().trim();
                category = categoryRepository.findByName(categoryName)
                        .orElseGet(() -> {
                            QuestionCategory newCategory = new QuestionCategory();
                            newCategory.setName(categoryName);
                            newCategory.setDescription("自动创建的分类");
                            return categoryRepository.save(newCategory);
                        });
            }
        }
        // 如果前端通过categoryId字段传入分类ID
        else if (question.getCategoryId() != null) {
            category = categoryRepository.findById(question.getCategoryId())
                    .orElse(null);
        }
        
        // 设置处理后的分类
        question.setCategory(category);
        
        // 创建问题
        com.llm.eval.model.StandardQuestion createdQuestion = questionService.createStandardQuestion(question);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标准问题")
    public ResponseEntity<com.llm.eval.model.StandardQuestion> updateQuestion(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.llm.eval.model.StandardQuestion question) {
        try {
            // 先获取现有问题
            StandardQuestion existingQuestion = questionService.getStandardQuestionById(id)
                    .orElseThrow(() -> new EntityNotFoundException("问题不存在，ID: " + id));
            
            // 处理分类信息
            QuestionCategory category = null;
            
            // 如果前端传入了category对象
            if (question.getCategory() != null) {
                // 如果提供了分类ID，使用现有分类
                if (question.getCategory().getCategoryId() != null) {
                    category = categoryRepository.findById(question.getCategory().getCategoryId())
                            .orElse(null);
                } 
                // 如果提供了分类名称，查找或创建分类
                else if (question.getCategory().getName() != null && !question.getCategory().getName().trim().isEmpty()) {
                    String categoryName = question.getCategory().getName().trim();
                    category = categoryRepository.findByName(categoryName)
                            .orElseGet(() -> {
                                QuestionCategory newCategory = new QuestionCategory();
                                newCategory.setName(categoryName);
                                newCategory.setDescription("自动创建的分类");
                                return categoryRepository.save(newCategory);
                            });
                }
            }
            // 如果前端通过categoryId字段传入分类ID
            else if (question.getCategoryId() != null) {
                category = categoryRepository.findById(question.getCategoryId())
                        .orElse(null);
            }
            
            // 设置处理后的分类
            question.setCategory(category);
            
            // 更新问题
            com.llm.eval.model.StandardQuestion updatedQuestion = questionService.updateStandardQuestion(id, question);
            return ResponseEntity.ok(updatedQuestion);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("更新问题失败，ID: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标准问题")
    public ResponseEntity<Void> deleteQuestion(@PathVariable("id") Integer id) {
        try {
            questionService.deleteStandardQuestion(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/tags/{tagId}")
    @Operation(summary = "为问题添加标签")
    public ResponseEntity<Void> addTagToQuestion(
            @PathVariable("id") Integer questionId,
            @PathVariable("tagId") Integer tagId) {
        try {
            Set<Integer> tagIds = new HashSet<>(Collections.singletonList(tagId));
            questionService.addTagsToQuestion(questionId, tagIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}/tags/{tagId}")
    @Operation(summary = "移除问题的标签")
    public ResponseEntity<Void> removeTagFromQuestion(
            @PathVariable("id") Integer questionId,
            @PathVariable("tagId") Integer tagId) {
        try {
            // 添加日志记录
            System.out.println("StandardQuestionController: 尝试从问题 " + questionId + " 中移除标签 " + tagId);
            
            Set<Integer> tagIds = new HashSet<>(Collections.singletonList(tagId));
            questionService.removeTagsFromQuestion(questionId, tagIds);
            
            // 成功日志
            System.out.println("StandardQuestionController: 成功从问题 " + questionId + " 中移除标签 " + tagId);
            
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            // 当问题或标签不存在时
            System.err.println("StandardQuestionController: 移除标签失败(404): " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            // 当标签不在问题中或问题没有标签时
            System.err.println("StandardQuestionController: 移除标签失败(400): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            // 其他未预期的异常
            System.err.println("StandardQuestionController: 移除标签失败(500): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Message", "Internal server error: " + e.getMessage())
                    .build();
        }
    }

    // 请求体类
    public static class CategoryUpdateRequest {
        private Integer categoryId;
        private String categoryName;
        
        public Integer getCategoryId() {
            return categoryId;
        }
        
        public void setCategoryId(Integer categoryId) {
            this.categoryId = categoryId;
        }
        
        public String getCategoryName() {
            return categoryName;
        }
        
        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
    }
    
    @GetMapping("/without-category")
    @Operation(summary = "获取没有分类的问题列表")
    public ResponseEntity<Map<String, Object>> getQuestionsWithoutCategory(
            @Parameter(description = "页码（从1开始）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        
        try {
            logger.debug("获取没有分类的问题列表，参数: page={}, size={}, sortBy={}, sortDir={}", 
                    page, size, sortBy, sortDir);
            
            // 构建排序
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            
            // 创建分页请求（注意：Spring Data JPA的页码从0开始，而API的页码从1开始）
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            
            // 调用服务层查询，使用普通方法（已修改为手动加载标签）
            Page<StandardQuestion> questionPage = questionService.getQuestionsWithoutCategory(pageable);
            
            // 标签已在服务层被加载
            List<StandardQuestion> questions = questionPage.getContent();
            
            // 将实体对象转换为简单的Map对象，避免循环引用问题
            List<Map<String, Object>> questionDTOs = questions.stream().map(q -> {
                Map<String, Object> questionDTO = new HashMap<>();
                questionDTO.put("standardQuestionId", q.getStandardQuestionId());
                questionDTO.put("question", q.getQuestion());
                questionDTO.put("questionType", q.getQuestionType());
                questionDTO.put("difficulty", q.getDifficulty());
                questionDTO.put("status", q.getStatus());
                questionDTO.put("version", q.getVersion());
                questionDTO.put("createdAt", q.getCreatedAt());
                questionDTO.put("updatedAt", q.getUpdatedAt());
                
                // 添加标签信息
                if (q.getTags() != null && !q.getTags().isEmpty()) {
                    List<Map<String, Object>> tagDTOs = q.getTags().stream().map(tag -> {
                        Map<String, Object> tagDTO = new HashMap<>();
                        tagDTO.put("tagId", tag.getTagId());
                        tagDTO.put("tagName", tag.getTagName());
                        // 使用标签自身的颜色，如果为空则使用默认颜色
                        tagDTO.put("color", tag.getColor() != null ? tag.getColor() : "#409EFF");
                        return tagDTO;
                    }).collect(Collectors.toList());
                    questionDTO.put("tags", tagDTOs);
                } else {
                    questionDTO.put("tags", new ArrayList<>());
                }
                
                return questionDTO;
            }).collect(Collectors.toList());
            
            // 构建返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("content", questionDTOs);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("total", questionPage.getTotalElements());
            response.put("pages", questionPage.getTotalPages());
            
            logger.debug("查询成功，总记录数: {}, 总页数: {}", questionPage.getTotalElements(), questionPage.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取没有分类的问题列表失败", e);
            throw new RuntimeException("获取没有分类的问题列表失败: " + e.getMessage(), e);
        }
    }
    
    @PutMapping("/{id}/category")
    @Operation(summary = "更新问题的分类")
    public ResponseEntity<Map<String, Object>> updateQuestionCategory(
            @PathVariable("id") Integer questionId,
            @RequestBody CategoryUpdateRequest request) {
        try {
            logger.debug("更新问题分类，问题ID: {}, 分类ID: {}, 分类名称: {}", 
                    questionId, request.getCategoryId(), request.getCategoryName());
            
            StandardQuestion updatedQuestion;
            
            // 如果提供了分类名称，优先使用名称创建或查找分类
            if (request.getCategoryName() != null && !request.getCategoryName().trim().isEmpty()) {
                updatedQuestion = questionService.updateQuestionCategoryByName(
                        questionId, request.getCategoryName().trim());
            } 
            // 如果明确传入了null或者没有提供分类ID，则移除分类
            else if (request.getCategoryId() == null) {
                updatedQuestion = questionService.updateQuestionCategory(questionId, null);
            }
            // 否则使用分类ID更新
            else {
                updatedQuestion = questionService.updateQuestionCategory(
                        questionId, request.getCategoryId());
            }
            
            // 将实体对象转换为简单的Map对象，避免循环引用问题
            Map<String, Object> questionDTO = new HashMap<>();
            questionDTO.put("standardQuestionId", updatedQuestion.getStandardQuestionId());
            questionDTO.put("question", updatedQuestion.getQuestion());
            questionDTO.put("questionType", updatedQuestion.getQuestionType());
            questionDTO.put("difficulty", updatedQuestion.getDifficulty());
            questionDTO.put("status", updatedQuestion.getStatus());
            questionDTO.put("version", updatedQuestion.getVersion());
            questionDTO.put("createdAt", updatedQuestion.getCreatedAt());
            questionDTO.put("updatedAt", updatedQuestion.getUpdatedAt());
            
            // 添加分类信息（如果存在）
            if (updatedQuestion.getCategory() != null) {
                Map<String, Object> categoryDTO = new HashMap<>();
                categoryDTO.put("categoryId", updatedQuestion.getCategory().getCategoryId());
                categoryDTO.put("categoryName", updatedQuestion.getCategory().getName());
                questionDTO.put("category", categoryDTO);
            } else {
                questionDTO.put("category", null);
            }
            
            logger.debug("更新问题分类成功，问题ID: {}", questionId);
            return ResponseEntity.ok(questionDTO);
        } catch (EntityNotFoundException e) {
            logger.warn("更新问题分类失败，未找到问题或分类，问题ID: {}", questionId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("更新问题分类失败，问题ID: {}", questionId, e);
            return ResponseEntity.badRequest().build();
        }
    }
}