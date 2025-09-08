package com.cleveronion.blog.infrastructure.comment.persistence.converter;

import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.infrastructure.comment.persistence.po.CommentPO;

/**
 * 评论聚合与持久化对象转换器
 * 负责CommentAggregate和CommentPO之间的相互转换
 * 
 * @author CleverOnion
 */
public class CommentConverter {
    
    /**
     * 将CommentAggregate转换为CommentPO
     * 
     * @param commentAggregate 评论聚合
     * @return 评论持久化对象
     */
    public static CommentPO toCommentPO(CommentAggregate commentAggregate) {
        if (commentAggregate == null) {
            return null;
        }
        
        CommentPO commentPO = new CommentPO(
            commentAggregate.getContent(),
            Long.valueOf(commentAggregate.getArticleId().getValue()),
            commentAggregate.getUserId().getValue(),
            commentAggregate.getParentId() != null ? commentAggregate.getParentId().getValue() : null
        );
        
        // 如果聚合有ID，说明是已存在的评论
        if (commentAggregate.getId() != null) {
            commentPO.setId(commentAggregate.getId().getValue());
        }
        
        // 时间戳由基础设施层的@CreationTimestamp和@UpdateTimestamp注解自动管理
        
        return commentPO;
    }
    
    /**
     * 将CommentPO转换为CommentAggregate
     * 
     * @param commentPO 评论持久化对象
     * @return 评论聚合
     */
    public static CommentAggregate toCommentAggregate(CommentPO commentPO) {
        if (commentPO == null) {
            return null;
        }
        
        // 构建父评论ID值对象
        CommentId parentId = commentPO.getParentId() != null ? 
            CommentId.of(commentPO.getParentId()) : null;
        
        // 使用rebuild方法重建聚合
        CommentAggregate commentAggregate = CommentAggregate.rebuild(
            CommentId.of(commentPO.getId()),
            commentPO.getContent(),
            ArticleId.of(commentPO.getArticleId().toString()),
            UserId.of(commentPO.getUserId()),
            parentId,
            commentPO.getCreatedAt() // 使用createdAt作为发布时间
        );
        
        return commentAggregate;
    }
    
    /**
     * 更新CommentPO的字段（用于更新操作）
     * 
     * @param commentPO 要更新的持久化对象
     * @param commentAggregate 包含新数据的聚合
     */
    public static void updateCommentPO(CommentPO commentPO, CommentAggregate commentAggregate) {
        if (commentPO == null || commentAggregate == null) {
            return;
        }
        
        commentPO.setContent(commentAggregate.getContent());
        // 注意：通常评论的文章ID、用户ID和父评论ID不应该被修改
        // 如果业务需要修改这些字段，可以在这里添加相应的逻辑
    }
    
    /**
     * 创建顶级评论的CommentPO
     * 
     * @param content 评论内容
     * @param articleId 文章ID
     * @param userId 用户ID
     * @return 评论持久化对象
     */
    public static CommentPO createTopLevelCommentPO(String content, Long articleId, Long userId) {
        return new CommentPO(content, articleId, userId, null);
    }
    
    /**
     * 创建回复评论的CommentPO
     * 
     * @param content 评论内容
     * @param articleId 文章ID
     * @param userId 用户ID
     * @param parentId 父评论ID
     * @return 评论持久化对象
     */
    public static CommentPO createReplyCommentPO(String content, Long articleId, Long userId, Long parentId) {
        return new CommentPO(content, articleId, userId, parentId);
    }
}
