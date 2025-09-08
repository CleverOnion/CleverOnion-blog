package com.cleveronion.blog.application.comment.command;

import java.util.Objects;

/**
 * 删除评论命令
 * 封装删除评论所需的参数
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class DeleteCommentCommand {
    
    /**
     * 评论ID
     */
    private final Long commentId;
    
    /**
     * 操作用户ID（用于权限验证）
     */
    private final Long userId;
    
    /**
     * 构造函数
     * 
     * @param commentId 评论ID
     * @param userId 操作用户ID
     */
    public DeleteCommentCommand(Long commentId, Long userId) {
        this.commentId = commentId;
        this.userId = userId;
    }
    
    /**
     * 获取评论ID
     * 
     * @return 评论ID
     */
    public Long getCommentId() {
        return commentId;
    }
    
    /**
     * 获取操作用户ID
     * 
     * @return 操作用户ID
     */
    public Long getUserId() {
        return userId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteCommentCommand that = (DeleteCommentCommand) o;
        return Objects.equals(commentId, that.commentId) &&
               Objects.equals(userId, that.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(commentId, userId);
    }
    
    @Override
    public String toString() {
        return "DeleteCommentCommand{" +
               "commentId=" + commentId +
               ", userId=" + userId +
               '}';
    }
}