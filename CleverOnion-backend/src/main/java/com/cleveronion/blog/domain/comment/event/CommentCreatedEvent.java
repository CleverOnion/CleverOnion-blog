package com.cleveronion.blog.domain.comment.event;

import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import com.cleveronion.blog.domain.common.event.DomainEvent;
import com.cleveronion.blog.domain.user.valueobject.UserId;

import java.util.Objects;

/**
 * 评论创建事件
 * 当评论被成功创建时发布此事件
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class CommentCreatedEvent extends DomainEvent {
    
    private final CommentId commentId;
    private final ArticleId articleId;
    private final UserId userId;
    private final boolean isReply;
    
    public CommentCreatedEvent(Object source, CommentId commentId, ArticleId articleId, UserId userId, boolean isReply) {
        super(source, String.valueOf(commentId.getValue()));
        this.commentId = Objects.requireNonNull(commentId, "评论ID不能为空");
        this.articleId = Objects.requireNonNull(articleId, "文章ID不能为空");
        this.userId = Objects.requireNonNull(userId, "用户ID不能为空");
        this.isReply = isReply;
    }
    
    public CommentId getCommentId() {
        return commentId;
    }
    
    public ArticleId getArticleId() {
        return articleId;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public boolean isReply() {
        return isReply;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentCreatedEvent that = (CommentCreatedEvent) o;
        return Objects.equals(getEventId(), that.getEventId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }
    
    @Override
    public String toString() {
        return "CommentCreatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", occurredOn=" + getOccurredOn() +
                ", commentId=" + commentId.getValue() +
                ", articleId=" + articleId.getValue() +
                ", userId=" + userId.getValue() +
                ", isReply=" + isReply +
                '}';
    }
}

