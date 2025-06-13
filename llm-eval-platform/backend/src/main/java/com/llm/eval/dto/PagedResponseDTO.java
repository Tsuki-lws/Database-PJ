package com.llm.eval.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PagedResponseDTO<T> {
    private long total;
    private int pages;
    private int currentPage;
    private int pageSize;
    private List<T> content;
    
    // 构造函数
    public PagedResponseDTO() {}
    
    public PagedResponseDTO(List<T> content, PageInfo pageInfo) {
        this.content = content;
        this.total = pageInfo.getTotalElements();
        this.pages = pageInfo.getTotalPages();
        this.currentPage = pageInfo.getNumber() + 1; // 转换为前端页码（从1开始）
        this.pageSize = pageInfo.getSize();
    }
    
    public static <T> PagedResponseDTO<T> fromPage(Page<T> page) {
        PagedResponseDTO<T> dto = new PagedResponseDTO<>();
        dto.setTotal(page.getTotalElements());
        dto.setPages(page.getTotalPages());
        dto.setCurrentPage(page.getNumber() + 1); // Spring分页从0开始，前端通常从1开始
        dto.setPageSize(page.getSize());
        dto.setContent(page.getContent());
        return dto;
    }
    
    public static <T, R> PagedResponseDTO<R> fromPageWithMapper(Page<T> page, List<R> mappedContent) {
        PagedResponseDTO<R> dto = new PagedResponseDTO<>();
        dto.setTotal(page.getTotalElements());
        dto.setPages(page.getTotalPages());
        
        // 确保前端页码从1开始
        int frontendPageNumber = page.getNumber() + 1;
        dto.setCurrentPage(frontendPageNumber);
        
        dto.setPageSize(page.getSize());
        dto.setContent(mappedContent);
        
        // 创建并设置页面信息对象
        PageInfo pageInfo = new PageInfo(
            page.getNumber(),  // 保持后端页码从0开始
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            mappedContent.size()
        );
        dto.setPage(pageInfo);
        
        System.out.println("创建分页响应: 后端页码=" + page.getNumber() + 
                          ", 前端页码=" + frontendPageNumber + 
                          ", 总数=" + page.getTotalElements() + 
                          ", 内容大小=" + mappedContent.size());
        
        return dto;
    }
    
    // 设置当前页码（前端页码从1开始）
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        // 同时更新PageInfo中的number（后端页码从0开始）
        if (this.page != null) {
            this.page.setNumber(currentPage - 1);
        }
    }
    
    // 获取分页信息的方法
    public PageInfo getPage() {
        if (this.page == null) {
            this.page = new PageInfo(
                this.currentPage - 1, // Spring分页从0开始
                this.pageSize,
                this.total,
                this.pages,
                this.currentPage == 1,
                this.currentPage == this.pages,
                this.content != null ? this.content.size() : 0
            );
        }
        return this.page;
    }
    
    // 添加page字段，避免重复创建PageInfo对象
    private PageInfo page;
    
    // 设置PageInfo对象
    public void setPage(PageInfo page) {
        this.page = page;
    }
    
    @Data
    public static class PageInfo {
        private int number;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private int numberOfElements;
        
        public PageInfo() {}
        
        public PageInfo(int number, int size, long totalElements, int totalPages, 
                       boolean first, boolean last, int numberOfElements) {
            this.number = number;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
            this.numberOfElements = numberOfElements;
        }
    }
}
