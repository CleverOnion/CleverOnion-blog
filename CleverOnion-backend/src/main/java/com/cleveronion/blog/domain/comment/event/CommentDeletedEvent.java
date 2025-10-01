package com.cleveronion.blog.domain.comment.event;

import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import com.cleveronion.blog.domain.common.event.DomainEvent;

import java.util.Objects;

/**
 * 评论删除事件
 * 当评论被删除时发布此事件
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class CommentDeletedEvent extends DomainEvent {
    
    private final CommentId commentId;
    private final ArticleId articleId;
    
    public CommentDeletedEvent(Object source, CommentId commentId, ArticleId articleId) {
        super(source, String.valueOf(commentId.getValue()));
        this.commentId = Objects.requireNonNull(commentId, "评论ID不能为空");
        this.articleId = Objects.requireNonNull(articleId, "文章ID不能为空");
    }
    
    public CommentId getCommentId() {
        return commentId;
    }
    
    public ArticleId getArticleId() {
        return articleId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentDeletedEvent that = (CommentDeletedEvent) o;
        return Objects.equals(getEventId(), that.getEventId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }
    
    @Override
    public String toString() {
        return "CommentDeletedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", occurredOn=" + getOccurredOn() +
                ", commentId=" + commentId.getValue() +
                ", articleId=" + articleId.getValue() +
                '}';
    }
}

