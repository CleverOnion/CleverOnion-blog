package com.cleveronion.blog.application.comment.dto;

import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import java.util.List;

/**
 * 评论及其回复信息DTO
 * 用于在 Service 层传递评论及其相关回复信息
 * 
 * @author CleverOnion
 * @since 2.0.0
 */
public class CommentWithRepliesDTO {
    
    private final CommentAggregate comment;
    private final long replyCount;
    private final List<CommentAggregate> latestReplies;
    
    /**
     * 构造函数
     * 
     * @param comment 评论聚合对象
     * @param replyCount 回复总数
     * @param latestReplies 最新的几条回复列表
     */
    public CommentWithRepliesDTO(CommentAggregate comment, long replyCount, List<CommentAggregate> latestReplies) {
        this.comment = comment;
        this.replyCount = replyCount;
        this.latestReplies = latestReplies;
    }
    
    /**
     * 获取评论聚合对象
     * 
     * @return 评论聚合对象
     */
    public CommentAggregate getComment() {
        return comment;
    }
    
    /**
     * 获取回复总数
     * 
     * @return 回复总数
     */
    public long getReplyCount() {
        return replyCount;
    }
    
    /**
     * 获取最新的几条回复
     * 
     * @return 最新回复列表
     */
    public List<CommentAggregate> getLatestReplies() {
        return latestReplies;
    }
    
    @Override
    public String toString() {
        return "CommentWithRepliesDTO{" +
                "commentId=" + (comment != null ? comment.getId() : null) +
                ", replyCount=" + replyCount +
                ", latestRepliesCount=" + (latestReplies != null ? latestReplies.size() : 0) +
                '}';
    }
}

