package com.cleveronion.blog.domain.article.valueobject;

/**
 * 文章状态值对象
 * 表示文章的发布状态，是一个不可变的值对象
 * 
 * @author CleverOnion
 */
public enum ArticleStatus {
    
    /**
     * 草稿状态 - 文章尚未发布，仅作者可见
     */
    DRAFT("草稿"),
    
    /**
     * 已发布状态 - 文章已公开发布，所有用户可见
     */
    PUBLISHED("已发布"),
    
    /**
     * 已归档状态 - 文章已归档，不在主要列表中显示
     */
    ARCHIVED("已归档");
    
    private final String description;
    
    ArticleStatus(String description) {
        this.description = description;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断是否为已发布状态
     * 
     * @return 如果是已发布状态返回true，否则返回false
     */
    public boolean isPublished() {
        return this == PUBLISHED;
    }
    
    /**
     * 判断是否为草稿状态
     * 
     * @return 如果是草稿状态返回true，否则返回false
     */
    public boolean isDraft() {
        return this == DRAFT;
    }
    
    /**
     * 判断是否为归档状态
     * 
     * @return 如果是归档状态返回true，否则返回false
     */
    public boolean isArchived() {
        return this == ARCHIVED;
    }
}