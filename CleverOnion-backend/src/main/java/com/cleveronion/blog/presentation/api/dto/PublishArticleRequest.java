package com.cleveronion.blog.presentation.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * 直接发布文章请求 DTO
 * 用于接收前端传递的直接发布文章请求参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class PublishArticleRequest {
    
    @JsonProperty("title")
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题长度不能超过200个字符")
    private String title;
    
    @JsonProperty("content")
    @NotBlank(message = "文章内容不能为空")
    private String content;
    
    @JsonProperty("summary")
    @Size(max = 500, message = "文章摘要长度不能超过500个字符")
    private String summary;
    
    @JsonProperty("category_id")
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    @JsonProperty("tag_names")
    private Set<String> tagNames;
    
    /**
     * 默认构造函数
     */
    public PublishArticleRequest() {
    }
    
    /**
     * 构造函数
     * 
     * @param title 文章标题
     * @param content 文章内容
     * @param summary 文章摘要
     * @param categoryId 分类ID
     * @param tagNames 标签名称集合
     */
    public PublishArticleRequest(String title, String content, String summary, Long categoryId, Set<String> tagNames) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.categoryId = categoryId;
        this.tagNames = tagNames;
    }
    
    /**
     * 获取文章标题
     * 
     * @return 文章标题
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * 设置文章标题
     * 
     * @param title 文章标题
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * 获取文章内容
     * 
     * @return 文章内容
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 设置文章内容
     * 
     * @param content 文章内容
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * 获取文章摘要
     * 
     * @return 文章摘要
     */
    public String getSummary() {
        return summary;
    }
    
    /**
     * 设置文章摘要
     * 
     * @param summary 文章摘要
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    /**
     * 获取分类ID
     * 
     * @return 分类ID
     */
    public Long getCategoryId() {
        return categoryId;
    }
    
    /**
     * 设置分类ID
     * 
     * @param categoryId 分类ID
     */
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    /**
     * 获取标签名称集合
     * 
     * @return 标签名称集合
     */
    public Set<String> getTagNames() {
        return tagNames;
    }
    
    /**
     * 设置标签名称集合
     * 
     * @param tagNames 标签名称集合
     */
    public void setTagNames(Set<String> tagNames) {
        this.tagNames = tagNames;
    }
    
    @Override
    public String toString() {
        return "PublishArticleRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", summary='" + summary + '\'' +
                ", categoryId=" + categoryId +
                ", tagNames=" + tagNames +
                '}';
    }
}