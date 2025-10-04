package com.cleveronion.blog.presentation.api.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建评论请求DTO
 * 用于接收创建评论的请求参数
 * 
 * @author CleverOnion
 */
@Schema(description = "创建评论请求")
public class CreateCommentRequest {
    
    @Schema(description = "评论内容", required = true)
    @JsonProperty("content")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000个字符")
    private String content;
    
    @Schema(description = "文章ID", required = true)
    @JsonProperty("article_id")
    @NotBlank(message = "文章ID不能为空")
    private String articleId;
    
    @Schema(description = "父评论ID")
    @JsonProperty("parent_id")
    private String parentId;
    
    /**
     * 默认构造函数
     */
    public CreateCommentRequest() {
    }
    
    /**
     * 构造函数
     * 
     * @param content 评论内容
     * @param articleId 文章ID
     * @param parentId 父评论ID
     */
    public CreateCommentRequest(String content, String articleId, String parentId) {
        this.content = content;
        this.articleId = articleId;
        this.parentId = parentId;
    }
    
    // Getters and Setters
    
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
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    @Override
    public String toString() {
        return "CreateCommentRequest{" +
                "content='" + content + '\'' +
                ", articleId='" + articleId + '\'' +
                ", parentId='" + parentId + '\'' +
                '}';
    }
}