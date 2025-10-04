package com.cleveronion.blog.presentation.api.controller;

import com.cleveronion.blog.application.comment.dto.CommentWithRepliesDTO;
import com.cleveronion.blog.application.comment.service.CommentCommandService;
import com.cleveronion.blog.application.comment.service.CommentQueryService;
import com.cleveronion.blog.application.comment.command.CreateCommentCommand;
import com.cleveronion.blog.application.comment.command.DeleteCommentCommand;
import com.cleveronion.blog.application.user.service.UserQueryService;
import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import com.cleveronion.blog.domain.comment.valueobject.CommentId;
import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.presentation.api.dto.comment.CreateCommentRequest;
import com.cleveronion.blog.presentation.api.dto.comment.CommentResponse;
import com.cleveronion.blog.presentation.api.dto.comment.CommentListResponse;
import com.cleveronion.blog.presentation.api.dto.UserResponse;
import com.cleveronion.blog.presentation.api.dto.Result;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 评论控制器
 * 提供评论相关的RESTful API接口
 * 使用CQRS模式，命令和查询分离
 * 
 * @author CleverOnion
 * @since 1.0.0
 * @version 2.0.0 - 引入CQRS架构
 */
@RestController
@RequestMapping("/comments")
public class CommentController {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    
    private final CommentCommandService commentCommandService;
    private final CommentQueryService commentQueryService;
    private final UserQueryService userQueryService;
    
    public CommentController(CommentCommandService commentCommandService,
                           CommentQueryService commentQueryService,
                           UserQueryService userQueryService) {
        this.commentCommandService = commentCommandService;
        this.commentQueryService = commentQueryService;
        this.userQueryService = userQueryService;
    }
    
    /**
     * 发表评论
     * 
     * @param request 创建评论请求
     * @return 创建的评论信息
     */
    @PostMapping
    @SaCheckLogin
    public Result<CommentResponse> createComment(@Valid @RequestBody CreateCommentRequest request) {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        logger.info("用户 {} 开始发表评论，文章ID: {}", userId, request.getArticleId());
        
        // 构建创建评论命令
        Long articleId = Long.parseLong(request.getArticleId());
        Long parentId = request.getParentId() != null ? Long.parseLong(request.getParentId()) : null;
        
        CreateCommentCommand command = new CreateCommentCommand(
            request.getContent(),
            articleId,
            userId,
            parentId
        );
        
        // 执行创建评论业务逻辑
        CommentAggregate comment = commentCommandService.createComment(command);
        
        // 转换为响应DTO
        CommentResponse response = convertToCommentResponse(comment);
        
        logger.info("用户 {} 成功发表评论，评论ID: {}", userId, comment.getId().getValue());
        
        return Result.success(response);
    }
    
    /**
     * 删除评论
     * 
     * @param commentId 评论ID
     * @return 删除结果
     */
    @DeleteMapping("/{commentId}")
    @SaCheckLogin
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        logger.info("用户 {} 开始删除评论，评论ID: {}", userId, commentId);
        
        // 构建删除评论命令
        DeleteCommentCommand command = new DeleteCommentCommand(commentId, userId);
        
        // 执行删除评论业务逻辑
        commentCommandService.deleteComment(command);
        
        logger.info("用户 {} 成功删除评论，评论ID: {}", userId, commentId);
        
        return Result.success();
    }
    
    /**
     * 查询文章的顶级评论列表（带回复信息）
     * 支持懒加载子评论，每个顶级评论包含回复统计和最新回复列表
     * 
     * @param articleId 文章ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param replyLimit 每个评论返回的最新回复数（默认3条）
     * @return 顶级评论列表（包含回复统计和最新回复）
     * @since 2.0.0
     */
    @GetMapping("/top-level-with-replies")
    public Result<CommentListResponse> getTopLevelCommentsWithReplies(
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "3") int replyLimit) {
        
        logger.debug("查询文章顶级评论（带回复），文章ID: {}, 页码: {}, 每页: {}, 回复数: {}", 
            articleId, page, size, replyLimit);
        
        // 执行查询
        ArticleId articleIdVO = new ArticleId(articleId.toString());
        List<CommentWithRepliesDTO> commentsWithReplies = 
            commentQueryService.findTopLevelCommentsWithLatestReplies(articleIdVO, page, size, replyLimit);
        
        // 转换为响应DTO
        List<CommentResponse> commentResponses = commentsWithReplies.stream()
            .map(this::convertToCommentResponseWithReplies)
            .collect(Collectors.toList());
        
        // 获取顶级评论总数
        long totalComments = commentQueryService.countTopLevelByArticleId(articleIdVO);
        
        CommentListResponse response = new CommentListResponse(
            commentResponses,
            totalComments,
            page,
            size
        );
        
        logger.debug("成功查询文章顶级评论（带回复），文章ID: {}, 返回评论数: {}", 
            articleId, commentResponses.size());
        
        return Result.success(response);
    }
    
    /**
     * 查询评论的回复列表
     * 
     * @param parentId 父评论ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 回复列表
     */
    @GetMapping("/replies")
    public Result<CommentListResponse> getReplies(
            @RequestParam Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        logger.debug("查询评论回复，父评论ID: {}, 页码: {}, 每页大小: {}", parentId, page, size);
        
        // 执行查询回复业务逻辑
        CommentId parentIdVO = new CommentId(parentId);
        List<CommentAggregate> replies = commentQueryService.findRepliesByParentId(parentIdVO, page, size);
        
        // 转换为响应DTO
        List<CommentResponse> replyResponses = replies.stream()
            .map(this::convertToCommentResponse)
            .collect(Collectors.toList());
        
        // 获取回复总数
        long totalReplies = commentQueryService.countRepliesByParentId(parentIdVO);
        
        CommentListResponse response = new CommentListResponse(
            replyResponses,
            totalReplies,
            page,
            size
        );
        
        logger.debug("成功查询评论回复，父评论ID: {}, 返回回复数: {}", parentId, replyResponses.size());
        
        return Result.success(response);
    }
    
    /**
     * 将CommentAggregate转换为CommentResponse
     * 
     * @param comment 评论聚合
     * @return 评论响应DTO
     */
    private CommentResponse convertToCommentResponse(CommentAggregate comment) {
        // 根据用户ID获取用户信息
        UserId userId = comment.getUserId();
        Optional<UserAggregate> userOpt = userQueryService.findById(userId);
        
        UserResponse userResponse = null;
        if (userOpt.isPresent()) {
            userResponse = UserResponse.from(userOpt.get());
        } else {
            // 如果用户不存在，创建一个默认的用户响应
            logger.warn("评论关联的用户不存在，用户ID: {}, 评论ID: {}", userId.getValue(), comment.getId().getValue());
            userResponse = new UserResponse(userId.getValue(), null, "未知用户", null);
        }
        
        CommentResponse response = new CommentResponse(
            comment.getId().getValue().toString(),
            comment.getContent(),
            comment.getArticleId().getValue().toString(),
            userResponse,
            comment.getParentId() != null ? comment.getParentId().getValue().toString() : null,
            comment.isTopLevel()
        );
        response.setPublishedAt(comment.getPublishedAt());
        return response;
    }
    
    /**
     * 转换带回复信息的评论（新版评论系统）
     * 
     * @param dto 包含评论和回复信息的DTO
     * @return 评论响应DTO（包含回复统计和最新回复）
     * @since 2.0.0
     */
    private CommentResponse convertToCommentResponseWithReplies(CommentWithRepliesDTO dto) {
        // 转换主评论
        CommentResponse response = convertToCommentResponse(dto.getComment());
        
        // 添加回复统计
        response.setReplyCount((int) dto.getReplyCount());
        
        // 添加最新回复
        if (dto.getLatestReplies() != null && !dto.getLatestReplies().isEmpty()) {
            List<CommentResponse> latestReplies = dto.getLatestReplies().stream()
                .map(this::convertToCommentResponse)
                .collect(Collectors.toList());
            response.setLatestReplies(latestReplies);
        }
        
        return response;
    }
}