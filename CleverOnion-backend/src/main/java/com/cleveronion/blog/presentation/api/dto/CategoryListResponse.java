package com.cleveronion.blog.presentation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 分类列表响应DTO
 * 用于返回分类列表查询结果
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Schema(description = "分类列表响应")
public class CategoryListResponse {
    
    @Schema(description = "分类列表", required = true)
    private List<CategoryResponse> categories;
    
    @Schema(description = "总数量", required = true, example = "10")
    private Integer totalCount;
    
    /**
     * 默认构造函数
     */
    public CategoryListResponse() {
    }
    
    /**
     * 构造函数
     * 
     * @param categories 分类列表
     * @param totalCount 总数量
     */
    public CategoryListResponse(List<CategoryResponse> categories, Integer totalCount) {
        this.categories = categories;
        this.totalCount = totalCount;
    }
    
    /**
     * 获取分类列表
     * 
     * @return 分类列表
     */
    public List<CategoryResponse> getCategories() {
        return categories;
    }
    
    /**
     * 设置分类列表
     * 
     * @param categories 分类列表
     */
    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
    }
    
    /**
     * 获取总数量
     * 
     * @return 总数量
     */
    public Integer getTotalCount() {
        return totalCount;
    }
    
    /**
     * 设置总数量
     * 
     * @param totalCount 总数量
     */
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    
    @Override
    public String toString() {
        return "CategoryListResponse{" +
                "categories=" + categories +
                ", totalCount=" + totalCount +
                '}';
    }
}