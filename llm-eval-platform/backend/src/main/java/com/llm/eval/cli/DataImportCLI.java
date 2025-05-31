package com.llm.eval.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.llm.eval.dto.DataImportResultDTO;
import com.llm.eval.dto.RawQuestionImportDTO;
import com.llm.eval.dto.StandardQAImportDTO;
import com.llm.eval.service.DataImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 数据导入命令行工具
 * 使用方法: java -jar app.jar --import.enabled=true --import.type=raw-qa --import.file=/path/to/file.json
 */
@Component
@ConditionalOnProperty(prefix = "import", name = "enabled", havingValue = "true")
@Slf4j
public class DataImportCLI implements CommandLineRunner {

    private final DataImportService dataImportService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DataImportCLI(DataImportService dataImportService, ObjectMapper objectMapper) {
        this.dataImportService = dataImportService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("开始命令行导入数据...");
        
        // 解析参数
        String importType = getArgValue(args, "--import.type=");
        String importFile = getArgValue(args, "--import.file=");
        
        if (importType == null || importFile == null) {
            log.error("缺少必要的参数，请指定 --import.type 和 --import.file");
            return;
        }
        
        File file = new File(importFile);
        if (!file.exists() || !file.isFile()) {
            log.error("文件不存在: {}", importFile);
            return;
        }
        
        log.info("导入类型: {}, 文件: {}", importType, importFile);
        
        // 读取文件内容
        byte[] fileContent = Files.readAllBytes(Paths.get(importFile));
        
        DataImportResultDTO result;
        
        switch (importType) {
            case "raw-qa":
                List<RawQuestionImportDTO> rawQAs = Arrays.asList(
                        objectMapper.readValue(fileContent, RawQuestionImportDTO[].class)
                );
                result = dataImportService.importRawQuestions(rawQAs);
                break;
                
            case "standard-qa":
                List<StandardQAImportDTO> standardQAs = Arrays.asList(
                        objectMapper.readValue(fileContent, StandardQAImportDTO[].class)
                );
                result = dataImportService.importStandardQAs(standardQAs);
                break;
                
            default:
                log.error("不支持的导入类型: {}", importType);
                return;
        }
        
        log.info("导入结果: 成功={}, 消息={}, 导入={}, 失败={}, 总数={}",
                result.isSuccess(), result.getMessage(), result.getImported(), 
                result.getFailed(), result.getTotal());
    }
    
    private String getArgValue(String[] args, String prefix) {
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return null;
    }
} 