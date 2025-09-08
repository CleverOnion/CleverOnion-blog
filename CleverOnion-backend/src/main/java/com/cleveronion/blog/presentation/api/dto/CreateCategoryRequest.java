package com.cleveronion.blog.presentation.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建分类请求 DTO
 * 用于接收前端传递的创建分类请求参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class CreateCategoryRequest {
    
    @JsonProperty("name")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;
    
    @JsonProperty("icon")
    @Size(max = 100, message = "分类图标长度不能超过100个字符")
    private String icon;
    
    /**
     * 默认构造函数
     */
    public CreateCategoryRequest() {
    }
    
    /**
     * 构造函数
     * 
     * @param name 分类名称
     */
    public CreateCategoryRequest(String name) {
        this.name = name;
    }
    
    /**
     * 构造函数
     * 
     * @param name 分类名称
     * @param icon 分类图标
     */
    public CreateCategoryRequest(String name, String icon) {
        this.name = name;
        this.icon = icon;
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
        return "CreateCategoryRequest{" +
                "name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}