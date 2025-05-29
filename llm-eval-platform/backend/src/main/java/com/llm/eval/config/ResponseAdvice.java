package com.llm.eval.config;

import com.llm.eval.model.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.LinkedHashMap;

/**
 * 统一响应处理器
 * 将所有Controller返回的响应包装成统一格式
 */
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 对所有响应进行处理
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, 
                                 MethodParameter returnType, 
                                 MediaType selectedContentType,
                                 Class<? extends HttpMessageConverter<?>> selectedConverterType, 
                                 ServerHttpRequest request, 
                                 ServerHttpResponse response) {
        
        // 如果响应体已经是ApiResponse类型，直接返回
        if (body instanceof ApiResponse) {
            return body;
        }
        
        // 如果响应体是ResponseEntity类型，不做处理
        if (body instanceof ResponseEntity) {
            return body;
        }
        
        // 空响应体处理
        if (body == null) {
            return new ApiResponse<>(200, "Success", null);
        }
        
        // 包装响应体
        return new ApiResponse<>(200, "Success", body);
    }
    
    /**
     * 全局异常处理
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception ex) {
        return new ApiResponse<>(500, ex.getMessage(), null);
    }
    
    /**
     * 业务异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ApiResponse<>(400, ex.getMessage(), null);
    }
} 