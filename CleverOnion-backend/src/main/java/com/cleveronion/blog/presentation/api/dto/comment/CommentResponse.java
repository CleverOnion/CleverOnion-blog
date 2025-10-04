package com.cleveronion.blog.presentation.api.dto.comment;

import com.cleveronion.blog.presentation.api.dto.UserResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应DTO
 * 用于返回评论信息给前端
 * 
 * @author CleverOnion
 */
@Schema(description = "评论响应")
public class CommentResponse {
    
    @Schema(description = "评论ID")
    @JsonProperty("id")
    private String id;
    
    @Schema(description = "评论内容")
    @JsonProperty("content")
    private String content;
    
    @Schema(description = "文章ID")
    @JsonProperty("article_id")
    private String articleId;
    
    @Schema(description = "用户信息")
    @JsonProperty("user")
    private UserResponse user;
    
    @Schema(description = "父评论ID")
    @JsonProperty("parent_id")
    private String parentId;
    
    @Schema(description = "是否为顶级评论")
    @JsonProperty("is_top_level")
    private boolean isTopLevel;
    
    @Schema(description = "发布时间")
    @JsonProperty("published_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;
    
    @Schema(description = "回复总数（仅顶级评论有效）")
    @JsonProperty("reply_count")
    private Integer replyCount;
    
    @Schema(description = "最新的几条回复（仅顶级评论有效，默认3条）")
    @JsonProperty("latest_replies")
    private List<CommentResponse> latestReplies;
    
    /**
     * 默认构造函数
     */
    public CommentResponse() {
    }
    
    /**
     * 构造函数
     * 
     * @param id 评论ID
     * @param content 评论内容
     * @param articleId 文章ID
     * @param user 用户信息
     * @param parentId 父评论ID
     * @param isTopLevel 是否为顶级评论
     */
    public CommentResponse(String id, String content, String articleId, 
                          UserResponse user, String parentId, boolean isTopLevel) {
        this.id = id;
        this.content = content;
        this.articleId = articleId;
        this.user = user;
        this.parentId = parentId;
        this.isTopLevel = isTopLevel;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getArticleId() {
        return articleId;
    }
    
    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }
    
    public UserResponse getUser() {
        return user;
    }
    
    public void setUser(UserResponse user) {
        this.user = user;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    public boolean isTopLevel() {
        return isTopLevel;
    }
    
    public void setTopLevel(boolean topLevel) {
        isTopLevel = topLevel;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public Integer getReplyCount() {
        return replyCount;
    }
    
    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }
    
    public List<CommentResponse> getLatestReplies() {
        return latestReplies;
    }
    
    public void setLatestReplies(List<CommentResponse> latestReplies) {
        this.latestReplies = latestReplies;
    }
    
    @Override
    public String toString() {
        return "CommentResponse{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", articleId='" + articleId + '\'' +
                ", parentId='" + parentId + '\'' +
                ", isTopLevel=" + isTopLevel +
                ", publishedAt=" + publishedAt +
                ", replyCount=" + replyCount +
                ", latestRepliesCount=" + (latestReplies != null ? latestReplies.size() : 0) +
                '}';
    }
}