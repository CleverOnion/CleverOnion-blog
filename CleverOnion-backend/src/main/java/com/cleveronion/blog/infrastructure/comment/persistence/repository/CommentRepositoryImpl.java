package com.cleveronion.blog.infrastructure.comment.persistence.repository;

import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import com.cleveronion.blog.domain.comment.repository.CommentRepository;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.infrastructure.comment.persistence.converter.CommentConverter;
import com.cleveronion.blog.infrastructure.comment.persistence.po.CommentPO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 评论仓储实现类
 * 实现CommentRepository接口，桥接领域层和基础设施层
 * 
 * @author CleverOnion
 */
@Repository
public class CommentRepositoryImpl implements CommentRepository {
    
    private final CommentJpaRepository commentJpaRepository;
    
    public CommentRepositoryImpl(CommentJpaRepository commentJpaRepository) {
        this.commentJpaRepository = commentJpaRepository;
    }
    
    @Override
    @Transactional
    public CommentAggregate save(CommentAggregate comment) {
        // 转换为PO并保存
        CommentPO commentPO = CommentConverter.toCommentPO(comment);
        CommentPO savedCommentPO = commentJpaRepository.save(commentPO);
        
        // 转换回聚合并返回
        return CommentConverter.toCommentAggregate(savedCommentPO);
    }
    
    @Override
    public Optional<CommentAggregate> findById(CommentId id) {
        Optional<CommentPO> commentPOOpt = commentJpaRepository.findById(id.getValue());
        return commentPOOpt.map(CommentConverter::toCommentAggregate);
    }
    
    @Override
    @Transactional
    public void deleteById(CommentId id) {
        commentJpaRepository.deleteById(id.getValue());
    }
    
    @Override
    public boolean existsById(CommentId id) {
        return commentJpaRepository.existsById(id.getValue());
    }
    
    @Override
    public List<CommentAggregate> findByArticleId(ArticleId articleId) {
        List<CommentPO> commentPOs = commentJpaRepository.findByArticleIdOrderByCreatedAtAsc(
            Long.valueOf(articleId.getValue()));
        return convertToAggregates(commentPOs);
    }
    
    @Override
    public List<CommentAggregate> findByArticleId(ArticleId articleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<CommentPO> commentPOs = commentJpaRepository.findByArticleIdOrderByCreatedAtAsc(
            Long.valueOf(articleId.getValue()), pageable);
        return convertToAggregates(commentPOs);
    }
    
    @Override
    public List<CommentAggregate> findByUserId(UserId userId) {
        List<CommentPO> commentPOs = commentJpaRepository.findByUserId(userId.getValue());
        return convertToAggregates(commentPOs);
    }
    
    @Override
    public List<CommentAggregate> findByParentId(CommentId parentId) {
        List<CommentPO> commentPOs = commentJpaRepository.findByParentId(parentId.getValue());
        return convertToAggregates(commentPOs);
    }
    
    @Override
    public List<CommentAggregate> findByParentId(CommentId parentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<CommentPO> commentPOs = commentJpaRepository.findByParentId(parentId.getValue(), pageable);
        return convertToAggregates(commentPOs);
    }
    
    @Override
    public List<CommentAggregate> findTopLevelCommentsByArticleId(ArticleId articleId) {
        List<CommentPO> commentPOs = commentJpaRepository.findByArticleIdAndParentIdIsNullOrderByCreatedAtAsc(
            Long.valueOf(articleId.getValue()));
        return convertToAggregates(commentPOs);
    }
    
    @Override
    public List<CommentAggregate> findTopLevelCommentsByArticleId(ArticleId articleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<CommentPO> commentPOs = commentJpaRepository.findByArticleIdAndParentIdIsNullOrderByCreatedAtAsc(
            Long.valueOf(articleId.getValue()));
        
        // 手动分页处理（因为JPA方法返回List而不是Page）
        int start = page * size;
        int end = Math.min(start + size, commentPOs.size());
        
        if (start >= commentPOs.size()) {
            return List.of();
        }
        
        return convertToAggregates(commentPOs.subList(start, end));
    }
    
    @Override
    public long countByArticleId(ArticleId articleId) {
        return commentJpaRepository.countByArticleId(Long.valueOf(articleId.getValue()));
    }
    
    @Override
    public long countTopLevelCommentsByArticleId(ArticleId articleId) {
        return commentJpaRepository.countByArticleIdAndParentIdIsNull(Long.valueOf(articleId.getValue()));
    }
    
    @Override
    public long countRepliesByParentId(CommentId parentId) {
        return commentJpaRepository.countByParentId(parentId.getValue());
    }
    
    @Override
    public Map<Long, Long> countRepliesByParentIds(List<CommentId> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // 转换为 Long 类型的 ID 列表
        List<Long> parentIdValues = parentIds.stream()
            .map(CommentId::getValue)
            .collect(Collectors.toList());
        
        // 批量查询回复数
        List<Object[]> results = commentJpaRepository.countRepliesByParentIdIn(parentIdValues);
        
        // 转换为 Map
        Map<Long, Long> replyCountMap = new HashMap<>();
        for (Object[] result : results) {
            Long parentId = (Long) result[0];
            Long replyCount = (Long) result[1];
            replyCountMap.put(parentId, replyCount);
        }
        
        // 为没有回复的评论补充 0
        for (Long parentId : parentIdValues) {
            replyCountMap.putIfAbsent(parentId, 0L);
        }
        
        return replyCountMap;
    }
    
    @Override
    public long countByUserId(UserId userId) {
        return commentJpaRepository.countByUserId(userId.getValue());
    }
    
    @Override
    public List<CommentAggregate> findRecentCommentsByUserId(UserId userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<CommentPO> commentPOs = commentJpaRepository.findByUserIdOrderByCreatedAtDesc(
            userId.getValue(), pageable);
        return convertToAggregates(commentPOs);
    }
    
    @Override
    public List<CommentAggregate> findRecentCommentsByArticleId(ArticleId articleId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<CommentPO> commentPOs = commentJpaRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        // 过滤出指定文章的评论
        return commentPOs.stream()
            .filter(commentPO -> commentPO.getArticleId().equals(Long.valueOf(articleId.getValue())))
            .limit(limit)
            .map(CommentConverter::toCommentAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<CommentAggregate> findByContentContaining(String keyword) {
        List<CommentPO> commentPOs = commentJpaRepository.findByContentContainingIgnoreCase(keyword);
        return convertToAggregates(commentPOs);
    }
    
    @Override
    public List<CommentAggregate> findByUserId(UserId userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<CommentPO> commentPOs = commentJpaRepository.findByUserIdOrderByCreatedAtDesc(
            userId.getValue(), pageable);
        return convertToAggregates(commentPOs);
    }
    
    @Override
    public boolean isCommentOwnedByUser(CommentId commentId, UserId userId) {
        Optional<CommentPO> commentPOOpt = commentJpaRepository.findById(commentId.getValue());
        return commentPOOpt.map(commentPO -> commentPO.getUserId().equals(userId.getValue()))
            .orElse(false);
    }
    
    /**
     * 将CommentPO列表转换为CommentAggregate列表
     * 
     * @param commentPOs 评论PO列表
     * @return 评论聚合列表
     */
    private List<CommentAggregate> convertToAggregates(List<CommentPO> commentPOs) {
        return commentPOs.stream()
            .map(CommentConverter::toCommentAggregate)
            .collect(Collectors.toList());
    }
}