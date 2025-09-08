package com.cleveronion.blog.presentation.api.controller;

import com.cleveronion.blog.application.comment.service.CommentApplicationService;
import com.cleveronion.blog.application.comment.command.CreateCommentCommand;
import com.cleveronion.blog.application.comment.command.DeleteCommentCommand;
import com.cleveronion.blog.application.comment.command.GetCommentsQuery;
import com.cleveronion.blog.application.user.service.UserApplicationService;
import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
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
 * 
 * @author CleverOnion
 */
@RestController
@RequestMapping("/comments")
public class CommentController {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    
    private final CommentApplicationService commentApplicationService;
    private final UserApplicationService userApplicationService;
    
    public CommentController(CommentApplicationService commentApplicationService,
                           UserApplicationService userApplicationService) {
        this.commentApplicationService = commentApplicationService;
        this.userApplicationService = userApplicationService;
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
        logger.info("用户 {} 开始发表评论，文章ID: {}", StpUtil.getLoginId(), request.getArticleId());
        
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
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
        CommentAggregate comment = commentApplicationService.createComment(command);
        
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
        logger.info("用户 {} 开始删除评论，评论ID: {}", StpUtil.getLoginId(), commentId);
        
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 构建删除评论命令
        DeleteCommentCommand command = new DeleteCommentCommand(commentId, userId);
        
        // 执行删除评论业务逻辑
        commentApplicationService.deleteComment(command);
        
        logger.info("用户 {} 成功删除评论，评论ID: {}", userId, commentId);
        
        return Result.success();
    }
    
    /**
     * 查询文章的评论列表
     * 
     * @param articleId 文章ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 评论列表
     */
    @GetMapping
    public Result<CommentListResponse> getComments(
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("查询文章评论，文章ID: {}, 页码: {}, 每页大小: {}", articleId, page, size);
        
        // 构建查询评论命令
        GetCommentsQuery query = new GetCommentsQuery(articleId, null, page, size);
        
        // 执行查询评论业务逻辑
        List<CommentAggregate> comments = commentApplicationService.getComments(query);
        
        // 转换为响应DTO
        List<CommentResponse> commentResponses = comments.stream()
            .map(this::convertToCommentResponse)
            .collect(Collectors.toList());
        
        // 获取评论总数
        long totalComments = commentApplicationService.countCommentsByArticleId(articleId);
        
        CommentListResponse response = new CommentListResponse(
            commentResponses,
            totalComments,
            page,
            size
        );
        
        logger.debug("成功查询文章评论，文章ID: {}, 返回评论数: {}", articleId, commentResponses.size());
        
        return Result.success(response);
    }
    
    /**
     * 查询文章的顶级评论列表
     * 
     * @param articleId 文章ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 顶级评论列表
     */
    @GetMapping("/top-level")
    public Result<CommentListResponse> getTopLevelComments(
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("查询文章顶级评论，文章ID: {}, 页码: {}, 每页大小: {}", articleId, page, size);
        
        // 执行查询顶级评论业务逻辑
        List<CommentAggregate> comments = commentApplicationService.getTopLevelComments(articleId, page, size);
        
        // 转换为响应DTO
        List<CommentResponse> commentResponses = comments.stream()
            .map(this::convertToCommentResponse)
            .collect(Collectors.toList());
        
        // 获取顶级评论总数
        long totalComments = commentApplicationService.countTopLevelCommentsByArticleId(articleId);
        
        CommentListResponse response = new CommentListResponse(
            commentResponses,
            totalComments,
            page,
            size
        );
        
        logger.debug("成功查询文章顶级评论，文章ID: {}, 返回评论数: {}", articleId, commentResponses.size());
        
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
        List<CommentAggregate> replies = commentApplicationService.getReplies(parentId, page, size);
        
        // 转换为响应DTO
        List<CommentResponse> replyResponses = replies.stream()
            .map(this::convertToCommentResponse)
            .collect(Collectors.toList());
        
        // 获取回复总数
        long totalReplies = commentApplicationService.countRepliesByParentId(parentId);
        
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
        Optional<UserAggregate> userOpt = userApplicationService.findById(userId);
        
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
}