package com.cleveronion.blog.presentation.api.dto;

import java.util.List;

/**
 * 带文章数量的分类列表响应DTO
 * 用于返回分类列表及分页信息
 * 
 * @author CleverOnion
 */
public class CategoryWithCountListResponse {
    
    /**
     * 分类列表
     */
    private List<CategoryWithCountResponse> categories;
    
    /**
     * 总数量
     */
    private Integer total;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    public CategoryWithCountListResponse() {
    }
    
    public CategoryWithCountListResponse(List<CategoryWithCountResponse> categories, Integer total) {
        this.categories = categories;
        this.total = total;
    }
    
    public CategoryWithCountListResponse(List<CategoryWithCountResponse> categories, Integer total, Integer page, Integer size) {
        this.categories = categories;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (total + size - 1) / size; // 计算总页数
    }
    
    public CategoryWithCountListResponse(List<CategoryWithCountResponse> categories, long total, Integer page, Integer size, int totalPages) {
        this.categories = categories;
        this.total = (int) total;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
    }
    
    public List<CategoryWithCountResponse> getCategories() {
        return categories;
    }
    
    public void setCategories(List<CategoryWithCountResponse> categories) {
        this.categories = categories;
    }
    
    public Integer getTotal() {
        return total;
    }
    
    public void setTotal(Integer total) {
        this.total = total;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    @Override
    public String toString() {
        return "CategoryWithCountListResponse{" +
                "categories=" + categories +
                ", total=" + total +
                ", page=" + page +
                ", size=" + size +
                ", totalPages=" + totalPages +
                '}';
    }
}