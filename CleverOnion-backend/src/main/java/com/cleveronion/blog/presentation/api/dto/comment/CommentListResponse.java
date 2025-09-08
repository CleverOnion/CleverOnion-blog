package com.cleveronion.blog.presentation.api.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 评论列表响应DTO
 * 用于返回评论列表信息给前端
 * 
 * @author CleverOnion
 */
@Schema(description = "评论列表响应")
public class CommentListResponse {
    
    @Schema(description = "评论列表")
    @JsonProperty("comments")
    private List<CommentResponse> comments;
    
    @Schema(description = "总数量")
    @JsonProperty("total_count")
    private long totalCount;
    
    @Schema(description = "当前页码")
    @JsonProperty("page")
    private int page;
    
    @Schema(description = "每页大小")
    @JsonProperty("size")
    private int size;
    
    @Schema(description = "是否有下一页")
    @JsonProperty("has_next")
    private boolean hasNext;
    
    @Schema(description = "是否有上一页")
    @JsonProperty("has_previous")
    private boolean hasPrevious;
    
    /**
     * 默认构造函数
     */
    public CommentListResponse() {
    }
    
    /**
     * 构造函数
     * 
     * @param comments 评论列表
     * @param totalCount 总数量
     * @param page 当前页码
     * @param size 每页大小
     */
    public CommentListResponse(List<CommentResponse> comments, long totalCount, int page, int size) {
        this.comments = comments;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.hasNext = (page + 1) * size < totalCount;
        this.hasPrevious = page > 0;
    }
    
    // Getters and Setters
    
    public List<CommentResponse> getComments() {
        return comments;
    }
    
    public void setComments(List<CommentResponse> comments) {
        this.comments = comments;
    }
    
    public long getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    @Override
    public String toString() {
        return "CommentListResponse{" +
                "comments=" + comments +
                ", totalCount=" + totalCount +
                ", page=" + page +
                ", size=" + size +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}