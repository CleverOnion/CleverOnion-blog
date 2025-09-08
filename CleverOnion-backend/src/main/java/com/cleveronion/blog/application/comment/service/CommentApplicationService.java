package com.cleveronion.blog.application.comment.service;

import com.cleveronion.blog.application.comment.command.CreateCommentCommand;
import com.cleveronion.blog.application.comment.command.DeleteCommentCommand;
import com.cleveronion.blog.application.comment.command.GetCommentsQuery;
import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import com.cleveronion.blog.domain.comment.repository.CommentRepository;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 评论应用服务
 * 负责评论相关的业务流程编排
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
@Transactional
public class CommentApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentApplicationService.class);
    
    private final CommentRepository commentRepository;
    
    public CommentApplicationService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
    
    /**
     * 创建评论
     * 
     * @param command 创建评论命令
     * @return 创建的评论聚合
     */
    public CommentAggregate createComment(CreateCommentCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("创建评论命令不能为空");
        }
        if (command.getContent() == null || command.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (command.getArticleId() == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        String content = command.getContent().trim();
        ArticleId articleId = new ArticleId(command.getArticleId().toString());
        UserId userId = UserId.of(command.getUserId());
        
        logger.debug("开始创建评论，用户ID: {}, 文章ID: {}, 内容长度: {}", 
            userId.getValue(), articleId.getValue(), content.length());
        
        CommentAggregate comment;
        
        if (command.isReply()) {
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
        CommentAggregate savedComment = commentRepository.save(comment);
        
        logger.info("成功创建评论，评论ID: {}, 用户ID: {}, 文章ID: {}", 
            savedComment.getId().getValue(), userId.getValue(), articleId.getValue());
        
        return savedComment;
    }
    
    /**
     * 删除评论
     * 
     * @param command 删除评论命令
     */
    public void deleteComment(DeleteCommentCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("删除评论命令不能为空");
        }
        if (command.getCommentId() == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        CommentId commentId = new CommentId(command.getCommentId());
        UserId userId = UserId.of(command.getUserId());
        
        logger.debug("开始删除评论，评论ID: {}, 操作用户ID: {}", 
            commentId.getValue(), userId.getValue());
        
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
            for (CommentAggregate reply : replies) {
                deleteCommentRecursively(reply.getId());
            }
        }
        
        // 删除评论
        commentRepository.deleteById(commentId);
        
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
    
    /**
     * 根据查询条件获取评论列表
     * 
     * @param query 查询条件
     * @return 评论列表
     */
    @Transactional(readOnly = true)
    public List<CommentAggregate> getComments(GetCommentsQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("查询条件不能为空");
        }
        if (query.getArticleId() == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        ArticleId articleId = new ArticleId(query.getArticleId().toString());
        
        if (query.isQueryingReplies()) {
            // 查询特定评论的回复
            CommentId parentId = new CommentId(query.getParentId());
            if (query.isPaginated()) {
                return commentRepository.findByParentId(parentId, query.getPage(), query.getSize());
            } else {
                return commentRepository.findByParentId(parentId);
            }
        } else {
            // 查询文章的评论
            if (query.isPaginated()) {
                return commentRepository.findByArticleId(articleId, query.getPage(), query.getSize());
            } else {
                return commentRepository.findByArticleId(articleId);
            }
        }
    }
    
    /**
     * 获取文章的顶级评论
     * 
     * @param articleId 文章ID
     * @return 顶级评论列表
     */
    @Transactional(readOnly = true)
    public List<CommentAggregate> getTopLevelComments(Long articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        ArticleId articleIdVO = new ArticleId(articleId.toString());
        return commentRepository.findTopLevelCommentsByArticleId(articleIdVO);
    }
    
    /**
     * 获取文章的顶级评论（分页）
     * 
     * @param articleId 文章ID
     * @param page 页码
     * @param size 每页大小
     * @return 顶级评论列表
     */
    @Transactional(readOnly = true)
    public List<CommentAggregate> getTopLevelComments(Long articleId, int page, int size) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        ArticleId articleIdVO = new ArticleId(articleId.toString());
        return commentRepository.findTopLevelCommentsByArticleId(articleIdVO, page, size);
    }
    
    /**
     * 获取评论的回复
     * 
     * @param parentId 父评论ID
     * @return 回复列表
     */
    @Transactional(readOnly = true)
    public List<CommentAggregate> getReplies(Long parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("父评论ID不能为空");
        }
        
        CommentId parentIdVO = new CommentId(parentId);
        return commentRepository.findByParentId(parentIdVO);
    }
    
    /**
     * 获取评论的回复（分页）
     * 
     * @param parentId 父评论ID
     * @param page 页码
     * @param size 每页大小
     * @return 回复列表
     */
    @Transactional(readOnly = true)
    public List<CommentAggregate> getReplies(Long parentId, int page, int size) {
        if (parentId == null) {
            throw new IllegalArgumentException("父评论ID不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        CommentId parentIdVO = new CommentId(parentId);
        return commentRepository.findByParentId(parentIdVO, page, size);
    }
    
    /**
     * 根据ID查找评论
     * 
     * @param commentId 评论ID
     * @return 评论聚合（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<CommentAggregate> findById(Long commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }
        
        CommentId commentIdVO = new CommentId(commentId);
        return commentRepository.findById(commentIdVO);
    }
    
    /**
     * 统计文章的评论总数
     * 
     * @param articleId 文章ID
     * @return 评论总数
     */
    @Transactional(readOnly = true)
    public long countCommentsByArticleId(Long articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        ArticleId articleIdVO = new ArticleId(articleId.toString());
        return commentRepository.countByArticleId(articleIdVO);
    }
    
    /**
     * 统计文章的顶级评论数
     * 
     * @param articleId 文章ID
     * @return 顶级评论数
     */
    @Transactional(readOnly = true)
    public long countTopLevelCommentsByArticleId(Long articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        ArticleId articleIdVO = new ArticleId(articleId.toString());
        return commentRepository.countTopLevelCommentsByArticleId(articleIdVO);
    }
    
    /**
     * 统计评论的回复数
     * 
     * @param parentId 父评论ID
     * @return 回复数
     */
    @Transactional(readOnly = true)
    public long countRepliesByParentId(Long parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("父评论ID不能为空");
        }
        
        CommentId parentIdVO = new CommentId(parentId);
        return commentRepository.countRepliesByParentId(parentIdVO);
    }
}