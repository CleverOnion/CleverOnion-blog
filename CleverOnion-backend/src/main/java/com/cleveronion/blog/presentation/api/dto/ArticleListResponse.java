package com.cleveronion.blog.presentation.api.dto;

import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章列表响应 DTO
 * 用于封装文章列表接口的响应数据，包含分页信息
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class ArticleListResponse {
    
    @JsonProperty("articles")
    private List<ArticleResponse> articles;
    
    @JsonProperty("total_count")
    private Long totalCount;
    
    @JsonProperty("page")
    private Integer page;
    
    @JsonProperty("size")
    private Integer size;
    
    @JsonProperty("total_pages")
    private Integer totalPages;
    
    @JsonProperty("has_next")
    private Boolean hasNext;
    
    @JsonProperty("has_previous")
    private Boolean hasPrevious;
    
    /**
     * 默认构造函数
     */
    public ArticleListResponse() {
    }
    
    /**
     * 构造函数
     * 
     * @param articles 文章列表
     * @param totalCount 总数量
     * @param page 当前页码
     * @param size 每页大小
     */
    public ArticleListResponse(List<ArticleResponse> articles, Long totalCount, Integer page, Integer size) {
        this.articles = articles;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = totalCount != null && size != null && size > 0 ? 
            (int) Math.ceil((double) totalCount / size) : 0;
        this.hasNext = page != null && totalPages != null && page < totalPages - 1;
        this.hasPrevious = page != null && page > 0;
    }
    
    /**
     * 从文章聚合列表创建响应对象（仅包含基本信息）
     * 注意：此方法创建的ArticleResponse只包含基本字段，不包含完整的关联实体信息
     * 推荐在Controller中直接使用buildArticleResponseWithEntities方法构建完整响应
     * 
     * @param articleAggregates 文章聚合列表
     * @param totalCount 总数量
     * @param page 当前页码
     * @param size 每页大小
     * @return 文章列表响应对象
     */
    @Deprecated
    public static ArticleListResponse from(List<ArticleAggregate> articleAggregates, 
                                          Long totalCount, Integer page, Integer size) {
        List<ArticleResponse> articles = articleAggregates != null ? 
            articleAggregates.stream()
                .map(ArticleResponse::new)
                .collect(Collectors.toList()) : null;
        
        return new ArticleListResponse(articles, totalCount, page, size);
    }
    
    /**
     * 获取文章列表
     * 
     * @return 文章列表
     */
    public List<ArticleResponse> getArticles() {
        return articles;
    }
    
    /**
     * 设置文章列表
     * 
     * @param articles 文章列表
     */
    public void setArticles(List<ArticleResponse> articles) {
        this.articles = articles;
    }
    
    /**
     * 获取总数量
     * 
     * @return 总数量
     */
    public Long getTotalCount() {
        return totalCount;
    }
    
    /**
     * 设置总数量
     * 
     * @param totalCount 总数量
     */
    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
    
    /**
     * 获取当前页码
     * 
     * @return 当前页码
     */
    public Integer getPage() {
        return page;
    }
    
    /**
     * 设置当前页码
     * 
     * @param page 当前页码
     */
    public void setPage(Integer page) {
        this.page = page;
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
     * 设置每页大小
     * 
     * @param size 每页大小
     */
    public void setSize(Integer size) {
        this.size = size;
    }
    
    /**
     * 获取总页数
     * 
     * @return 总页数
     */
    public Integer getTotalPages() {
        return totalPages;
    }
    
    /**
     * 设置总页数
     * 
     * @param totalPages 总页数
     */
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    /**
     * 是否有下一页
     * 
     * @return 是否有下一页
     */
    public Boolean getHasNext() {
        return hasNext;
    }
    
    /**
     * 设置是否有下一页
     * 
     * @param hasNext 是否有下一页
     */
    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    /**
     * 是否有上一页
     * 
     * @return 是否有上一页
     */
    public Boolean getHasPrevious() {
        return hasPrevious;
    }
    
    /**
     * 设置是否有上一页
     * 
     * @param hasPrevious 是否有上一页
     */
    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    @Override
    public String toString() {
        return "ArticleListResponse{" +
                "articles=" + (articles != null ? articles.size() + " items" : "null") +
                ", totalCount=" + totalCount +
                ", page=" + page +
                ", size=" + size +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}