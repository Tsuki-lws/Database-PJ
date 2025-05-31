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
        this.currentPage = pageInfo.getNumber() + 1;
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
        dto.setCurrentPage(page.getNumber() + 1);
        dto.setPageSize(page.getSize());
        dto.setContent(mappedContent);
        return dto;
    }
    
    // 获取分页信息的方法
    public PageInfo getPage() {
        return new PageInfo(
            this.currentPage - 1, // Spring分页从0开始
            this.pageSize,
            this.total,
            this.pages,
            this.currentPage == 1,
            this.currentPage == this.pages,
            this.content != null ? this.content.size() : 0
        );
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
