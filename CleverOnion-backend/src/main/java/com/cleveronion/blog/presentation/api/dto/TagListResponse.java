package com.cleveronion.blog.presentation.api.dto;

import java.util.List;

/**
 * 标签列表响应DTO
 * 用于返回标签列表信息
 * 
 * @author CleverOnion
 */
public class TagListResponse {
    
    /**
     * 标签列表
     */
    private List<TagResponse> tags;
    
    /**
     * 总数量
     */
    private Integer total;
    
    public TagListResponse() {
    }
    
    public TagListResponse(List<TagResponse> tags, Integer total) {
        this.tags = tags;
        this.total = total;
    }
    
    public List<TagResponse> getTags() {
        return tags;
    }
    
    public void setTags(List<TagResponse> tags) {
        this.tags = tags;
    }
    
    public Integer getTotal() {
        return total;
    }
    
    public void setTotal(Integer total) {
        this.total = total;
    }
    
    @Override
    public String toString() {
        return "TagListResponse{" +
                "tags=" + tags +
                ", total=" + total +
                '}';
    }
}