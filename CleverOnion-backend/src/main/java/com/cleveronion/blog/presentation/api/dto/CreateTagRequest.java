package com.cleveronion.blog.presentation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建标签请求DTO
 * 用于接收创建标签的请求参数
 * 
 * @author CleverOnion
 */
public class CreateTagRequest {
    
    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 255, message = "标签名称长度不能超过255个字符")
    private String name;
    
    public CreateTagRequest() {
    }
    
    public CreateTagRequest(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "CreateTagRequest{" +
                "name='" + name + '\'' +
                '}';
    }
}