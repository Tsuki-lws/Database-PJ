// package com.llm.eval.service;

// import com.llm.eval.model.CrowdsourcingAnswer;
// import com.llm.eval.model.CrowdsourcingTask;
// import com.llm.eval.model.StandardAnswer;
// import com.llm.eval.model.StandardQuestion;
// import com.llm.eval.repository.CrowdsourcingAnswerRepository;
// import com.llm.eval.repository.CrowdsourcingTaskRepository;
// import com.llm.eval.repository.StandardAnswerRepository;
// import com.llm.eval.repository.StandardQuestionRepository;
// import com.llm.eval.service.impl.CrowdsourcingServiceImpl;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// /**
//  * 众包服务测试类
//  */
// class CrowdsourcingServiceTest {

//     @Mock
//     private CrowdsourcingTaskRepository taskRepository;
    
//     @Mock
//     private CrowdsourcingAnswerRepository answerRepository;
    
//     @Mock
//     private StandardQuestionRepository questionRepository;
    
//     @Mock
//     private StandardAnswerRepository standardAnswerRepository;
    
//     @InjectMocks
//     private CrowdsourcingServiceImpl crowdsourcingService;
    
//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }
    
//     @Test
//     void testGetAllTasks() {
//         // 准备测试数据
//         CrowdsourcingTask task1 = new CrowdsourcingTask();
//         task1.setId(1);
//         task1.setTitle("任务1");
        
//         CrowdsourcingTask task2 = new CrowdsourcingTask();
//         task2.setId(2);
//         task2.setTitle("任务2");
        
//         List<CrowdsourcingTask> expectedTasks = Arrays.asList(task1, task2);
        
//         // 配置Mock行为
//         when(taskRepository.findAll()).thenReturn(expectedTasks);
        
//         // 执行测试
//         List<CrowdsourcingTask> actualTasks = crowdsourcingService.getAllTasks();
        
//         // 验证结果
//         assertEquals(2, actualTasks.size());
//         assertEquals("任务1", actualTasks.get(0).getTitle());
//         assertEquals("任务2", actualTasks.get(1).getTitle());
        
//         // 验证Mock调用
//         verify(taskRepository, times(1)).findAll();
//     }
    
//     @Test
//     void testCreateTask() {
//         // 准备测试数据
//         CrowdsourcingTask task = new CrowdsourcingTask();
//         task.setTitle("新任务");
//         task.setQuestionId(1);
        
//         CrowdsourcingTask savedTask = new CrowdsourcingTask();
//         savedTask.setId(1);
//         savedTask.setTitle("新任务");
//         savedTask.setQuestionId(1);
//         savedTask.setStatus(CrowdsourcingTask.TaskStatus.DRAFT);
//         savedTask.setCreatedAt(LocalDateTime.now());
        
//         // 配置Mock行为
//         when(questionRepository.existsById(anyInt())).thenReturn(true);
//         when(taskRepository.save(any(CrowdsourcingTask.class))).thenReturn(savedTask);
        
//         // 执行测试
//         CrowdsourcingTask result = crowdsourcingService.createTask(task);
        
//         // 验证结果
//         assertNotNull(result);
//         assertEquals(1, result.getId());
//         assertEquals("新任务", result.getTitle());
//         assertEquals(CrowdsourcingTask.TaskStatus.DRAFT, result.getStatus());
        
//         // 验证Mock调用
//         verify(questionRepository, times(1)).existsById(1);
//         verify(taskRepository, times(1)).save(any(CrowdsourcingTask.class));
//     }
    
//     @Test
//     void testPublishTask() {
//         // 准备测试数据
//         CrowdsourcingTask existingTask = new CrowdsourcingTask();
//         existingTask.setId(1);
//         existingTask.setTitle("待发布任务");
//         existingTask.setStatus(CrowdsourcingTask.TaskStatus.DRAFT);
        
//         CrowdsourcingTask publishedTask = new CrowdsourcingTask();
//         publishedTask.setId(1);
//         publishedTask.setTitle("待发布任务");
//         publishedTask.setStatus(CrowdsourcingTask.TaskStatus.PUBLISHED);
//         publishedTask.setPublishedAt(LocalDateTime.now());
        
//         // 配置Mock行为
//         when(taskRepository.findById(1)).thenReturn(Optional.of(existingTask));
//         when(taskRepository.save(any(CrowdsourcingTask.class))).thenReturn(publishedTask);
        
//         // 执行测试
//         CrowdsourcingTask result = crowdsourcingService.publishTask(1);
        
//         // 验证结果
//         assertNotNull(result);
//         assertEquals(CrowdsourcingTask.TaskStatus.PUBLISHED, result.getStatus());
//         assertNotNull(result.getPublishedAt());
        
//         // 验证Mock调用
//         verify(taskRepository, times(1)).findById(1);
//         verify(taskRepository, times(1)).save(any(CrowdsourcingTask.class));
//     }
    
//     @Test
//     void testPromoteToStandardAnswer() {
//         // 准备测试数据
//         CrowdsourcingAnswer answer = new CrowdsourcingAnswer();
//         answer.setId(1);
//         answer.setTaskId(1);
//         answer.setContent("众包答案内容");
//         answer.setReviewStatus(CrowdsourcingAnswer.ReviewStatus.APPROVED);
//         answer.setPromotedToStandard(false);
        
//         CrowdsourcingTask task = new CrowdsourcingTask();
//         task.setId(1);
//         task.setQuestionId(1);
        
//         StandardQuestion question = new StandardQuestion();
//         question.setId(1);
//         question.setContent("问题内容");
        
//         StandardAnswer standardAnswer = new StandardAnswer();
//         standardAnswer.setId(1);
//         standardAnswer.setQuestionId(1);
//         standardAnswer.setContent("众包答案内容");
        
//         // 配置Mock行为
//         when(answerRepository.findById(1)).thenReturn(Optional.of(answer));
//         when(taskRepository.findById(1)).thenReturn(Optional.of(task));
//         when(questionRepository.findById(1)).thenReturn(Optional.of(question));
//         when(standardAnswerRepository.save(any(StandardAnswer.class))).thenReturn(standardAnswer);
        
//         // 执行测试
//         StandardAnswer result = crowdsourcingService.promoteToStandardAnswer(1);
        
//         // 验证结果
//         assertNotNull(result);
//         assertEquals(1, result.getId());
//         assertEquals(1, result.getQuestionId());
//         assertEquals("众包答案内容", result.getContent());
        
//         // 验证Mock调用
//         verify(answerRepository, times(1)).findById(1);
//         verify(taskRepository, times(1)).findById(1);
//         verify(questionRepository, times(1)).findById(1);
//         verify(standardAnswerRepository, times(1)).save(any(StandardAnswer.class));
//         verify(answerRepository, times(1)).save(any(CrowdsourcingAnswer.class));
//     }
// } 