package com.cleveronion.blog.application.comment.command;

import java.util.Objects;

/**
 * 查询评论查询对象
 * 封装查询评论所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class GetCommentsQuery {
    
    /**
     * 文章ID
     */
    private final Long articleId;
    
    /**
     * 父评论ID（可选，用于查询特定评论的回复）
     */
    private final Long parentId;
    
    /**
     * 页码（从0开始）
     */
    private final Integer page;
    
    /**
     * 每页大小
     */
    private final Integer size;
    
    /**
     * 构造函数 - 查询文章的所有评论
     * 
     * @param articleId 文章ID
     * @param page 页码
     * @param size 每页大小
     */
    public GetCommentsQuery(Long articleId, Integer page, Integer size) {
        this(articleId, null, page, size);
    }
    
    /**
     * 构造函数 - 查询特定父评论的回复
     * 
     * @param articleId 文章ID
     * @param parentId 父评论ID
     * @param page 页码
     * @param size 每页大小
     */
    public GetCommentsQuery(Long articleId, Long parentId, Integer page, Integer size) {
        this.articleId = articleId;
        this.parentId = parentId;
        this.page = page;
        this.size = size;
    }
    
    /**
     * 构造函数 - 查询文章的所有评论（不分页）
     * 
     * @param articleId 文章ID
     */
    public GetCommentsQuery(Long articleId) {
        this(articleId, null, null, null);
    }
    
    /**
     * 获取文章ID
     * 
     * @return 文章ID
     */
    public Long getArticleId() {
        return articleId;
    }
    
    /**
     * 获取父评论ID
     * 
     * @return 父评论ID
     */
    public Long getParentId() {
        return parentId;
    }
    
    /**
     * 获取页码
     * 
     * @return 页码
     */
    public Integer getPage() {
        return page;
    }
    
    /**
     * 获取每页大小
     * 
     * @return 每页大小
     */
    public Integer getSize() {
        return size;
    }
    
    /**
     * 是否需要分页
     * 
     * @return 如果需要分页返回true，否则返回false
     */
    public boolean isPaginated() {
        return page != null && size != null;
    }
    
    /**
     * 是否查询特定父评论的回复
     * 
     * @return 如果查询回复返回true，否则返回false
     */
    public boolean isQueryingReplies() {
        return parentId != null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetCommentsQuery that = (GetCommentsQuery) o;
        return Objects.equals(articleId, that.articleId) &&
               Objects.equals(parentId, that.parentId) &&
               Objects.equals(page, that.page) &&
               Objects.equals(size, that.size);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(articleId, parentId, page, size);
    }
    
    @Override
    public String toString() {
        return "GetCommentsQuery{" +
               "articleId=" + articleId +
               ", parentId=" + parentId +
               ", page=" + page +
               ", size=" + size +
               '}';
    }
}