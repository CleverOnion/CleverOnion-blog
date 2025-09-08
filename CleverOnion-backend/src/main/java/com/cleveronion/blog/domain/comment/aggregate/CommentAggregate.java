package com.cleveronion.blog.domain.comment.aggregate;

import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import com.cleveronion.blog.domain.common.aggregate.AggregateRoot;
import com.cleveronion.blog.domain.user.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 评论聚合根
 * 负责管理评论的业务逻辑和规则，支持层级回复
 * 
 * @author CleverOnion
 */
public class CommentAggregate extends AggregateRoot {
    
    private CommentId id;
    private String content;
    private ArticleId articleId;
    private UserId userId;
    private CommentId parentId; // 父评论ID，用于层级回复
    private LocalDateTime publishedAt; // 发布时间
    
    /**
     * 私有构造函数，防止外部直接实例化
     */
    private CommentAggregate() {
    }
    
    /**
     * 创建新评论（顶级评论）
     * 
     * @param content 评论内容
     * @param articleId 文章ID
     * @param userId 用户ID
     * @return 新创建的评论聚合根
     */
    public static CommentAggregate createComment(String content, ArticleId articleId, UserId userId) {
        validateContent(content);
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        CommentAggregate comment = new CommentAggregate();
        // 新创建的评论不设置ID，由数据库自动生成
        comment.id = null;
        comment.content = content.trim();
        comment.articleId = articleId;
        comment.userId = userId;
        comment.parentId = null; // 顶级评论没有父评论
        comment.publishedAt = LocalDateTime.now();
        
        return comment;
    }
    
    /**
     * 创建回复评论（子评论）
     * 
     * @param content 评论内容
     * @param articleId 文章ID
     * @param userId 用户ID
     * @param parentId 父评论ID
     * @return 新创建的回复评论聚合根
     */
    public static CommentAggregate createReply(String content, ArticleId articleId, UserId userId, CommentId parentId) {
        validateContent(content);
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (parentId == null) {
            throw new IllegalArgumentException("父评论ID不能为空");
        }
        
        CommentAggregate comment = new CommentAggregate();
        // 新创建的回复评论不设置ID，由数据库自动生成
        comment.id = null;
        comment.content = content.trim();
        comment.articleId = articleId;
        comment.userId = userId;
        comment.parentId = parentId;
        comment.publishedAt = LocalDateTime.now();
        
        return comment;
    }
    
    /**
     * 重建评论聚合根（用于从持久化存储重建）
     * 
     * @param id 评论ID
     * @param content 评论内容
     * @param articleId 文章ID
     * @param userId 用户ID
     * @param parentId 父评论ID（可为null）
     * @param publishedAt 发布时间
     * @return 重建的评论聚合根
     */
    public static CommentAggregate rebuild(CommentId id, String content, ArticleId articleId, 
                                         UserId userId, CommentId parentId, LocalDateTime publishedAt) {
        CommentAggregate comment = new CommentAggregate();
        comment.id = id;
        comment.content = content;
        comment.articleId = articleId;
        comment.userId = userId;
        comment.parentId = parentId;
        comment.publishedAt = publishedAt;
        
        return comment;
    }
    
    /**
     * 更新评论内容
     * 
     * @param newContent 新的评论内容
     */
    public void updateContent(String newContent) {
        validateContent(newContent);
        this.content = newContent.trim();
    }
    
    /**
     * 检查是否为顶级评论
     * 
     * @return 如果是顶级评论返回true，否则返回false
     */
    public boolean isTopLevel() {
        return parentId == null;
    }
    
    /**
     * 检查是否为回复评论
     * 
     * @return 如果是回复评论返回true，否则返回false
     */
    public boolean isReply() {
        return parentId != null;
    }
    
    /**
     * 验证评论内容
     * 
     * @param content 评论内容
     * @throws IllegalArgumentException 如果内容无效
     */
    private static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (content.trim().length() > 1000) {
            throw new IllegalArgumentException("评论内容不能超过1000个字符");
        }
    }
    
    // Getters
    public CommentId getId() {
        return id;
    }
    
    public String getContent() {
        return content;
    }
    
    public ArticleId getArticleId() {
        return articleId;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public CommentId getParentId() {
        return parentId;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    @Override
    public String getAggregateId() {
        return id != null ? id.getValue().toString() : null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentAggregate comment = (CommentAggregate) o;
        return Objects.equals(id, comment.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "CommentAggregate{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", articleId=" + articleId +
                ", userId=" + userId +
                ", parentId=" + parentId +
                ", isTopLevel=" + isTopLevel() +
                '}';
    }
}