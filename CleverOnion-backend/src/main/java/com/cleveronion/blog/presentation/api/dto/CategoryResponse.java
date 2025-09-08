package com.cleveronion.blog.presentation.api.dto;

import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * 分类响应 DTO
 * 用于封装分类接口的响应数据
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class CategoryResponse {
    
    private Long id;
    
    private String name;
    
    private String icon;
    
    /**
     * 默认构造函数
     */
    public CategoryResponse() {
    }
    
    /**
     * 从分类聚合构造响应对象
     * 
     * @param categoryAggregate 分类聚合
     */
    public CategoryResponse(CategoryAggregate categoryAggregate) {
        if (categoryAggregate != null) {
            this.id = categoryAggregate.getId() != null ? categoryAggregate.getId().getValue() : null;
            this.name = categoryAggregate.getName();
            this.icon = categoryAggregate.getIcon();
        }
    }
    
    /**
     * 构造函数
     * 
     * @param id 分类ID
     * @param name 分类名称
     */
    public CategoryResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    /**
     * 构造函数
     * 
     * @param id 分类ID
     * @param name 分类名称
     * @param icon 分类图标
     */
    public CategoryResponse(Long id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }
    
    /**
     * 获取分类ID
     * 
     * @return 分类ID
     */
    public Long getId() {
        return id;
    }
    
    /**
     * 设置分类ID
     * 
     * @param id 分类ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * 获取分类名称
     * 
     * @return 分类名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置分类名称
     * 
     * @param name 分类名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取分类图标
     * 
     * @return 分类图标
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * 设置分类图标
     * 
     * @param icon 分类图标
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    @Override
    public String toString() {
        return "CategoryResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}