package com.cleveronion.blog.application.comment.command;

import java.util.Objects;

/**
 * 创建评论命令
 * 封装创建评论所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class CreateCommentCommand {
    
    /**
     * 评论内容
     */
    private final String content;
    
    /**
     * 文章ID
     */
    private final Long articleId;
    
    /**
     * 用户ID
     */
    private final Long userId;
    
    /**
     * 父评论ID（可选，用于回复评论）
     */
    private final Long parentId;
    
    /**
     * 构造函数
     * 
     * @param content 评论内容
     * @param articleId 文章ID
     * @param userId 用户ID
     * @param parentId 父评论ID（可选）
     */
    public CreateCommentCommand(String content, Long articleId, Long userId, Long parentId) {
        this.content = content;
        this.articleId = articleId;
        this.userId = userId;
        this.parentId = parentId;
    }
    
    /**
     * 创建顶级评论的构造函数
     * 
     * @param content 评论内容
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    public CreateCommentCommand(String content, Long articleId, Long userId) {
        this(content, articleId, userId, null);
    }
    
    /**
     * 获取评论内容
     * 
     * @return 评论内容
     */
    public String getContent() {
        return content;
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
     * 获取用户ID
     * 
     * @return 用户ID
     */
    public Long getUserId() {
        return userId;
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
     * 是否为回复评论
     * 
     * @return 如果是回复评论返回true，否则返回false
     */
    public boolean isReply() {
        return parentId != null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateCommentCommand that = (CreateCommentCommand) o;
        return Objects.equals(content, that.content) &&
               Objects.equals(articleId, that.articleId) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(parentId, that.parentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(content, articleId, userId, parentId);
    }
    
    @Override
    public String toString() {
        return "CreateCommentCommand{" +
               "content='" + content + '\'' +
               ", articleId=" + articleId +
               ", userId=" + userId +
               ", parentId=" + parentId +
               '}';
    }
}