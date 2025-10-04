package com.cleveronion.blog.domain.article.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 文章内容值对象
 * 封装文章的内容信息，包括标题、正文和摘要
 * 作为不可变值对象，其相等性由所有属性值决定
 * 
 * @author CleverOnion
 */
public class ArticleContent {
    
    private final String title;
    private final String content;
    private final String summary;
    
    /**
     * 构造文章内容值对象
     * 
     * @param title 文章标题，不能为空
     * @param content 文章正文内容（Markdown格式），不能为空
     * @param summary 文章摘要，可以为空
     * @throws IllegalArgumentException 当标题或内容为空时抛出
     */
    @JsonCreator
    public ArticleContent(
        @JsonProperty("title") String title,
        @JsonProperty("content") String content,
        @JsonProperty("summary") String summary
    ) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
        
        this.title = title.trim();
        this.content = content.trim();
        this.summary = summary != null ? summary.trim() : null;
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
     * 获取文章内容
     * 
     * @return 文章内容（Markdown格式）
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 获取文章摘要
     * 
     * @return 文章摘要，可能为null
     */
    public String getSummary() {
        return summary;
    }
    
    /**
     * 判断是否有摘要
     * 
     * @return 如果有摘要返回true，否则返回false
     */
    @JsonIgnore
    public boolean hasSummary() {
        return summary != null && !summary.isEmpty();
    }
    
    /**
     * 获取内容长度
     * 
     * @return 内容字符数
     */
    @JsonIgnore
    public int getContentLength() {
        return content.length();
    }
    
    /**
     * 创建新的文章内容值对象，更新标题
     * 
     * @param newTitle 新标题
     * @return 新的文章内容值对象
     */
    public ArticleContent withTitle(String newTitle) {
        return new ArticleContent(newTitle, this.content, this.summary);
    }
    
    /**
     * 创建新的文章内容值对象，更新内容
     * 
     * @param newContent 新内容
     * @return 新的文章内容值对象
     */
    public ArticleContent withContent(String newContent) {
        return new ArticleContent(this.title, newContent, this.summary);
    }
    
    /**
     * 创建新的文章内容值对象，更新摘要
     * 
     * @param newSummary 新摘要
     * @return 新的文章内容值对象
     */
    public ArticleContent withSummary(String newSummary) {
        return new ArticleContent(this.title, this.content, newSummary);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleContent that = (ArticleContent) o;
        return Objects.equals(title, that.title) &&
               Objects.equals(content, that.content) &&
               Objects.equals(summary, that.summary);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, content, summary);
    }
    
    @Override
    public String toString() {
        return "ArticleContent{" +
                "title='" + title + '\'' +
                ", contentLength=" + content.length() +
                ", hasSummary=" + hasSummary() +
                '}';
    }
}