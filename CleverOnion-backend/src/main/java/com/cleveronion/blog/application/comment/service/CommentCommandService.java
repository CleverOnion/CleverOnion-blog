package com.cleveronion.blog.application.comment.service;

import com.cleveronion.blog.application.comment.command.CreateCommentCommand;
import com.cleveronion.blog.application.comment.command.DeleteCommentCommand;
import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import com.cleveronion.blog.domain.comment.event.CommentCreatedEvent;
import com.cleveronion.blog.domain.comment.event.CommentDeletedEvent;
import com.cleveronion.blog.domain.comment.repository.CommentRepository;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import com.cleveronion.blog.domain.common.event.DomainEventPublisher;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 评论命令服务
 * 负责处理所有修改评论状态的操作
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
@Transactional
public class CommentCommandService {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentCommandService.class);
    
    private final CommentRepository commentRepository;
    private final DomainEventPublisher eventPublisher;
    
    public CommentCommandService(CommentRepository commentRepository, DomainEventPublisher eventPublisher) {
        this.commentRepository = commentRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 创建评论
     * 
     * @param command 创建评论命令
     * @return 创建的评论聚合
     */
    public CommentAggregate createComment(CreateCommentCommand command) {
        logger.debug("执行创建评论命令: {}", command);
        
        String content = command.getContent().trim();
        ArticleId articleId = new ArticleId(command.getArticleId().toString());
        UserId userId = UserId.of(command.getUserId());
        
        CommentAggregate comment;
        boolean isReply = command.isReply();
        
        if (isReply) {
            // 创建回复评论
            CommentId parentId = new CommentId(command.getParentId());
            
            // 验证父评论是否存在
            Optional<CommentAggregate> parentComment = commentRepository.findById(parentId);
            if (parentComment.isEmpty()) {
                throw new IllegalArgumentException("父评论不存在: " + parentId.getValue());
            }
            
            // 验证父评论是否属于同一篇文章
            if (!parentComment.get().getArticleId().equals(articleId)) {
                throw new IllegalArgumentException("父评论不属于指定文章");
            }
            
            comment = CommentAggregate.createReply(content, articleId, userId, parentId);
            logger.debug("创建回复评论，父评论ID: {}", parentId.getValue());
        } else {
            // 创建顶级评论
            comment = CommentAggregate.createComment(content, articleId, userId);
            logger.debug("创建顶级评论");
        }
        
        // 保存评论
        CommentAggregate saved = commentRepository.save(comment);
        
        // 发布领域事件
        eventPublisher.publish(new CommentCreatedEvent(
            this,
            saved.getId(),
            saved.getArticleId(),
            saved.getUserId(),
            isReply
        ));
        
        logger.info("成功创建评论，评论ID: {}, 用户ID: {}, 文章ID: {}", 
            saved.getId().getValue(), userId.getValue(), articleId.getValue());
        
        return saved;
    }
    
    /**
     * 删除评论（递归删除子评论）
     * 
     * @param command 删除评论命令
     */
    public void deleteComment(DeleteCommentCommand command) {
        logger.debug("执行删除评论命令: {}", command);
        
        CommentId commentId = new CommentId(command.getCommentId());
        UserId userId = UserId.of(command.getUserId());
        
        // 查找评论
        Optional<CommentAggregate> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            throw new IllegalArgumentException("评论不存在: " + commentId.getValue());
        }
        
        CommentAggregate comment = commentOpt.get();
        
        // 验证权限：只有评论作者可以删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权限删除该评论");
        }
        
        // 检查是否有子评论
        List<CommentAggregate> replies = commentRepository.findByParentId(commentId);
        if (!replies.isEmpty()) {
            logger.warn("删除评论时发现有 {} 条回复，将一并删除", replies.size());
            // 递归删除所有子评论
            deleteCommentRecursively(commentId);
        } else {
            // 删除评论
            commentRepository.deleteById(commentId);
        }
        
        // 发布领域事件
        eventPublisher.publish(new CommentDeletedEvent(
            this,
            commentId,
            comment.getArticleId()
        ));
        
        logger.info("成功删除评论，评论ID: {}, 操作用户ID: {}", 
            commentId.getValue(), userId.getValue());
    }
    
    /**
     * 递归删除评论及其所有子评论
     * 
     * @param commentId 评论ID
     */
    private void deleteCommentRecursively(CommentId commentId) {
        // 查找子评论
        List<CommentAggregate> replies = commentRepository.findByParentId(commentId);
        
        // 递归删除子评论
        for (CommentAggregate reply : replies) {
            deleteCommentRecursively(reply.getId());
        }
        
        // 删除当前评论
        commentRepository.deleteById(commentId);
        logger.debug("递归删除评论，评论ID: {}", commentId.getValue());
    }
}

