package com.cleveronion.blog.presentation.api.dto;

import java.util.List;

/**
 * 带文章数量的标签列表响应DTO
 * 用于返回标签列表及分页信息
 * 
 * @author CleverOnion
 */
public class TagWithCountListResponse {
    
    /**
     * 标签列表
     */
    private List<TagWithCountResponse> tags;
    
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
    
    public TagWithCountListResponse() {
    }
    
    public TagWithCountListResponse(List<TagWithCountResponse> tags, Integer total) {
        this.tags = tags;
        this.total = total;
    }
    
    public TagWithCountListResponse(List<TagWithCountResponse> tags, Integer total, Integer page, Integer size) {
        this.tags = tags;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (total + size - 1) / size; // 计算总页数
    }
    
    public List<TagWithCountResponse> getTags() {
        return tags;
    }
    
    public void setTags(List<TagWithCountResponse> tags) {
        this.tags = tags;
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
        return "TagWithCountListResponse{" +
                "tags=" + tags +
                ", total=" + total +
                ", page=" + page +
                ", size=" + size +
                ", totalPages=" + totalPages +
                '}';
    }
}