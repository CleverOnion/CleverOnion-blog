package com.cleveronion.blog.presentation.api.dto;

import com.cleveronion.blog.domain.article.aggregate.TagAggregate;

/**
 * 标签响应DTO
 * 用于返回标签信息
 * 
 * @author CleverOnion
 */
public class TagResponse {
    
    /**
     * 标签ID
     */
    private Long id;
    
    /**
     * 标签名称
     */
    private String name;
    
    public TagResponse() {
    }
    
    public TagResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    /**
     * 从TagAggregate构造TagResponse
     * 
     * @param tagAggregate 标签聚合根
     * @return 标签响应DTO
     */
    public static TagResponse from(TagAggregate tagAggregate) {
        if (tagAggregate == null) {
            return null;
        }
        
        return new TagResponse(
            tagAggregate.getId().getValue(),
            tagAggregate.getName()
        );
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "TagResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}