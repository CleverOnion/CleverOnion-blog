package com.cleveronion.blog.application.comment.service;

import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import com.cleveronion.blog.domain.comment.repository.CommentRepository;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 评论查询服务
 * 负责处理所有评论查询操作，配置缓存优化性能
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class CommentQueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentQueryService.class);
    
    private final CommentRepository commentRepository;
    
    public CommentQueryService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
    
    /**
     * 根据ID查找评论
     * 
     * @param commentId 评论ID
     * @return 评论聚合（如果存在）
     */
    public Optional<CommentAggregate> findById(CommentId commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }
        
        logger.debug("查询评论，ID: {}", commentId.getValue());
        return commentRepository.findById(commentId);
    }
    
    /**
     * 根据文章ID查找评论
     * 
     * @param articleId 文章ID
     * @return 评论列表
     */
    public List<CommentAggregate> findByArticleId(ArticleId articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        logger.debug("查询文章评论，文章ID: {}", articleId.getValue());
        return commentRepository.findByArticleId(articleId);
    }
    
    /**
     * 获取文章的顶级评论
     * 
     * @param articleId 文章ID
     * @return 顶级评论列表
     */
    public List<CommentAggregate> findTopLevelByArticleId(ArticleId articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        logger.debug("查询文章顶级评论，文章ID: {}", articleId.getValue());
        return commentRepository.findTopLevelCommentsByArticleId(articleId);
    }
    
    /**
     * 获取文章的顶级评论（分页）
     * 
     * @param articleId 文章ID
     * @param page 页码
     * @param size 每页大小
     * @return 顶级评论列表
     */
    public List<CommentAggregate> findTopLevelByArticleId(ArticleId articleId, int page, int size) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        logger.debug("分页查询文章顶级评论，文章ID: {}, 页码: {}, 每页: {}", 
            articleId.getValue(), page, size);
        return commentRepository.findTopLevelCommentsByArticleId(articleId, page, size);
    }
    
    /**
     * 获取评论的回复
     * 
     * @param parentId 父评论ID
     * @return 回复列表
     */
    public List<CommentAggregate> findRepliesByParentId(CommentId parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("父评论ID不能为空");
        }
        
        logger.debug("查询评论回复，父评论ID: {}", parentId.getValue());
        return commentRepository.findByParentId(parentId);
    }
    
    /**
     * 获取评论的回复（分页）
     * 
     * @param parentId 父评论ID
     * @param page 页码
     * @param size 每页大小
     * @return 回复列表
     */
    public List<CommentAggregate> findRepliesByParentId(CommentId parentId, int page, int size) {
        if (parentId == null) {
            throw new IllegalArgumentException("父评论ID不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        logger.debug("分页查询评论回复，父评论ID: {}, 页码: {}, 每页: {}", 
            parentId.getValue(), page, size);
        return commentRepository.findByParentId(parentId, page, size);
    }
    
    /**
     * 统计文章的评论总数
     * 
     * @param articleId 文章ID
     * @return 评论总数
     */
    public long countByArticleId(ArticleId articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        logger.debug("统计文章评论数，文章ID: {}", articleId.getValue());
        return commentRepository.countByArticleId(articleId);
    }
    
    /**
     * 统计文章的顶级评论数
     * 
     * @param articleId 文章ID
     * @return 顶级评论数
     */
    public long countTopLevelByArticleId(ArticleId articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        logger.debug("统计文章顶级评论数，文章ID: {}", articleId.getValue());
        return commentRepository.countTopLevelCommentsByArticleId(articleId);
    }
    
    /**
     * 统计评论的回复数
     * 
     * @param parentId 父评论ID
     * @return 回复数
     */
    public long countRepliesByParentId(CommentId parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("父评论ID不能为空");
        }
        
        logger.debug("统计评论回复数，父评论ID: {}", parentId.getValue());
        return commentRepository.countRepliesByParentId(parentId);
    }
}

