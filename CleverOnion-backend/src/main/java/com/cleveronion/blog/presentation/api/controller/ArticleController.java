package com.cleveronion.blog.presentation.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.cleveronion.blog.application.article.service.ArticleApplicationService;
import com.cleveronion.blog.application.article.service.CategoryApplicationService;
import com.cleveronion.blog.application.article.service.TagApplicationService;
import com.cleveronion.blog.application.user.service.UserApplicationService;
import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import com.cleveronion.blog.domain.article.aggregate.TagAggregate;
import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.domain.article.valueobject.*;
import java.util.HashSet;
import com.cleveronion.blog.presentation.api.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章控制器
 * 提供文章管理相关的 RESTful API 接口
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@RestController
@RequestMapping("/articles")
@Tag(name = "文章管理", description = "文章的创建、更新、删除、查询等操作")
@Validated
public class ArticleController {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);
    
    private final ArticleApplicationService articleApplicationService;
    private final CategoryApplicationService categoryApplicationService;
    private final TagApplicationService tagApplicationService;
    private final UserApplicationService userApplicationService;
    
    public ArticleController(ArticleApplicationService articleApplicationService,
                           CategoryApplicationService categoryApplicationService,
                           TagApplicationService tagApplicationService,
                           UserApplicationService userApplicationService) {
        this.articleApplicationService = articleApplicationService;
        this.categoryApplicationService = categoryApplicationService;
        this.tagApplicationService = tagApplicationService;
        this.userApplicationService = userApplicationService;
    }
    
    /**
     * 创建文章草稿
     * 
     * @param request 创建文章请求
     * @return 创建的文章信息
     */
    @PostMapping
    @Operation(summary = "创建文章草稿", description = "创建一篇新的文章草稿")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "文章创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleResponse> createArticle(
            @Valid @RequestBody CreateArticleRequest request) {
        
        logger.info("接收到创建文章请求，标题: {}", request.getTitle());
        
        // 构建文章内容值对象
        ArticleContent content = new ArticleContent(
            request.getTitle(),
            request.getContent(),
            request.getSummary()
        );
        
        // 构建作者ID值对象
        AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
        CategoryId categoryId = new CategoryId(request.getCategoryId());
        
        // 创建文章草稿
        ArticleAggregate article = articleApplicationService.createDraft(content, categoryId, authorId);
        
        // 如果有标签，根据标签名查找或创建标签，然后添加到文章
        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            List<TagAggregate> tags = tagApplicationService.findOrCreateByNames(request.getTagNames());
            Set<TagId> tagIds = tags.stream()
                .map(TagAggregate::getId)
                .collect(Collectors.toSet());
            article = articleApplicationService.addTags(article.getId(), tagIds, authorId);
        }
        
        ArticleResponse response = buildArticleResponseWithEntities(article);
        
        logger.info("成功创建文章，ID: {}", article.getId().getValue());
        
        return Result.success(response);
    }
    
    /**
     * 直接发布文章
     * 
     * @param request 发布文章请求
     * @return 发布的文章信息
     */
    @PostMapping("/publish")
    @Operation(summary = "直接发布文章", description = "创建并直接发布一篇新文章")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "文章发布成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleResponse> publishArticleDirectly(
            @Valid @RequestBody PublishArticleRequest request) {
        
        logger.info("接收到直接发布文章请求，标题: {}", request.getTitle());
        
        // 构建文章内容值对象
        ArticleContent content = new ArticleContent(
            request.getTitle(),
            request.getContent(),
            request.getSummary()
        );
        
        // 构建分类ID和作者ID值对象
        CategoryId categoryId = new CategoryId(request.getCategoryId());
        AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
        
        // 直接创建并发布文章
        ArticleAggregate article = articleApplicationService.createAndPublishArticle(content, categoryId, authorId);
        
        // 如果有标签，根据标签名查找或创建标签，然后添加到文章
        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            List<TagAggregate> tags = tagApplicationService.findOrCreateByNames(request.getTagNames());
            Set<TagId> tagIds = tags.stream()
                .map(TagAggregate::getId)
                .collect(Collectors.toSet());
            article = articleApplicationService.addTags(article.getId(), tagIds, authorId);
        }
        
        ArticleResponse response = buildArticleResponseWithEntities(article);
        
        logger.info("成功直接发布文章，ID: {}", article.getId().getValue());
        
        return Result.success(response);
    }
    
    /**
     * 更新文章
     * 
     * @param id 文章ID
     * @param request 更新文章请求
     * @return 更新后的文章信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新文章", description = "更新指定文章的内容")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文章更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "文章不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleResponse> updateArticle(
            @Parameter(description = "文章ID") @PathVariable @NotNull Long id,
            @Valid @RequestBody UpdateArticleRequest request) {
        
        logger.info("接收到更新文章请求，文章ID: {}", id);
        
        try {
            ArticleId articleId = new ArticleId(id.toString());
            AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
            
            // 查找文章
            Optional<ArticleAggregate> articleOpt = articleApplicationService.findById(articleId);
            if (articleOpt.isEmpty()) {
                return Result.error("文章不存在");
            }
            
            ArticleAggregate article = articleOpt.get();
            
            // 更新文章内容
            if (request.hasUpdates()) {
                ArticleContent newContent = new ArticleContent(
                    request.getTitle() != null ? request.getTitle() : article.getContent().getTitle(),
                    request.getContent() != null ? request.getContent() : article.getContent().getContent(),
                    request.getSummary() != null ? request.getSummary() : article.getContent().getSummary()
                );
                article = articleApplicationService.updateContent(articleId, newContent, authorId);
            }
            
            // 更新分类
            if (request.getCategoryId() != null) {
                CategoryId newCategoryId = new CategoryId(request.getCategoryId());
                article = articleApplicationService.updateCategory(articleId, newCategoryId, authorId);
            }
            
            // 更新标签
            if (request.getTagNames() != null) {
                // 先移除所有现有标签，再添加新标签
                if (!article.getTagIds().isEmpty()) {
                    articleApplicationService.removeTags(articleId, article.getTagIds(), authorId);
                }
                
                if (!request.getTagNames().isEmpty()) {
                    // 根据标签名查找或创建标签
                    List<TagAggregate> tags = tagApplicationService.findOrCreateByNames(request.getTagNames());
                    Set<TagId> newTagIds = tags.stream()
                        .map(TagAggregate::getId)
                        .collect(Collectors.toSet());
                    article = articleApplicationService.addTags(articleId, newTagIds, authorId);
                }
            }
            
            ArticleResponse response = buildArticleResponseWithEntities(article);
            
            logger.info("成功更新文章，ID: {}", id);
            
            return Result.success(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("更新文章失败，参数错误: {}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("更新文章失败，文章ID: {}", id, e);
            return Result.error("更新文章失败");
        }
    }
    
    /**
     * 发布文章
     * 
     * @param id 文章ID
     * @return 发布后的文章信息
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布文章", description = "将草稿状态的文章发布")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文章发布成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "文章不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleResponse> publishArticle(
            @Parameter(description = "文章ID") @PathVariable @NotNull Long id) {
        
        logger.info("接收到发布文章请求，文章ID: {}", id);
        
        try {
            ArticleId articleId = new ArticleId(id.toString());
            AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
            
            ArticleAggregate article = articleApplicationService.publishArticle(articleId, authorId);
            ArticleResponse response = buildArticleResponseWithEntities(article);
            
            logger.info("成功发布文章，ID: {}", id);
            
            return Result.success(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("发布文章失败，参数错误: {}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("发布文章失败，状态错误: {}", e.getMessage());
            return Result.error("状态错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("发布文章失败，文章ID: {}", id, e);
            return Result.error("发布文章失败");
        }
    }
    
    /**
     * 归档文章
     * 
     * @param id 文章ID
     * @return 归档后的文章信息
     */
    @PostMapping("/{id}/archive")
    @Operation(summary = "归档文章", description = "将已发布的文章归档")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文章归档成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "文章不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleResponse> archiveArticle(
            @Parameter(description = "文章ID") @PathVariable @NotNull Long id) {
        
        logger.info("接收到归档文章请求，文章ID: {}", id);
        
        try {
            ArticleId articleId = new ArticleId(id.toString());
            AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
            
            ArticleAggregate article = articleApplicationService.archiveArticle(articleId, authorId);
            ArticleResponse response = buildArticleResponseWithEntities(article);
            
            logger.info("成功归档文章，ID: {}", id);
            
            return Result.success(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("归档文章失败，参数错误: {}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("归档文章失败，状态错误: {}", e.getMessage());
            return Result.error("状态错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("归档文章失败，文章ID: {}", id, e);
            return Result.error("归档文章失败");
        }
    }
    
    /**
     * 恢复文章为草稿
     * 
     * @param id 文章ID
     * @return 恢复后的文章信息
     */
    @PostMapping("/{id}/revert-to-draft")
    @Operation(summary = "恢复为草稿", description = "将已发布或已归档的文章恢复为草稿状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文章恢复成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "文章不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleResponse> revertToDraft(
            @Parameter(description = "文章ID") @PathVariable @NotNull Long id) {
        
        logger.info("接收到恢复文章为草稿请求，文章ID: {}", id);
        
        try {
            ArticleId articleId = new ArticleId(id.toString());
            AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
            
            ArticleAggregate article = articleApplicationService.revertToDraft(articleId, authorId);
            ArticleResponse response = buildArticleResponseWithEntities(article);
            
            logger.info("成功恢复文章为草稿，ID: {}", id);
            
            return Result.success(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("恢复文章为草稿失败，参数错误: {}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("恢复文章为草稿失败，文章ID: {}", id, e);
            return Result.error("恢复文章为草稿失败");
        }
    }
    
    /**
     * 删除文章
     * 
     * @param id 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章", description = "删除指定的文章")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文章删除成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "文章不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Void> deleteArticle(
            @Parameter(description = "文章ID") @PathVariable @NotNull Long id) {
        
        logger.info("接收到删除文章请求，文章ID: {}", id);
        
        try {
            ArticleId articleId = new ArticleId(id.toString());
            AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
            
            articleApplicationService.deleteArticle(articleId, authorId);
            
            logger.info("成功删除文章，ID: {}", id);
            
            return Result.success(null);
            
        } catch (IllegalArgumentException e) {
            logger.warn("删除文章失败，参数错误: {}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("删除文章失败，文章ID: {}", id, e);
            return Result.error("删除文章失败");
        }
    }
    
    /**
     * 查询文章列表（分页，支持动态过滤）
     * 为了保持向后兼容性而保留的接口
     * @param page 页码
     * @param size 每页大小
     * @param status 文章状态（可选，默认查询全部）
     * @param categoryId 分类ID（可选）
     * @param tagId 标签ID（可选）
     * @return 文章列表
     */
    @GetMapping
    @Operation(summary = "查询文章列表", description = "分页查询文章列表，支持按状态、分类和标签动态过滤")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleListResponse> getPublishedArticles(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @Parameter(description = "文章状态（可选）：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档") @RequestParam(required = false) String status,
            @Parameter(description = "分类ID（可选）") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "标签ID（可选）") @RequestParam(required = false) Long tagId) {
        
        logger.debug("接收到查询文章列表请求，页码: {}, 每页大小: {}, 状态: {}, 分类ID: {}, 标签ID: {}", page, size, status, categoryId, tagId);
        
        try {
            List<ArticleAggregate> articles;
            long totalCount;
            
            // 解析状态参数
            ArticleStatus statusEnum = null;
            if (status != null && !status.trim().isEmpty()) {
                try {
                    statusEnum = ArticleStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return Result.error("无效的文章状态: " + status + "，支持的状态：DRAFT、PUBLISHED、ARCHIVED");
                }
            }
            
            // 根据过滤条件选择不同的查询方法
            if (categoryId != null && tagId != null) {
                // 同时按分类和标签过滤（支持状态筛选）
                CategoryId categoryIdVO = new CategoryId(categoryId);
                TagId tagIdVO = new TagId(tagId);
                if (statusEnum != null) {
                    articles = articleApplicationService.findByCategoryAndTagAndStatus(categoryIdVO, tagIdVO, statusEnum, page, size);
                    totalCount = articleApplicationService.countByCategoryAndTagAndStatus(categoryIdVO, tagIdVO, statusEnum);
                } else {
                    articles = articleApplicationService.findByCategoryAndTag(categoryIdVO, tagIdVO, page, size);
                    totalCount = articleApplicationService.countByCategoryAndTagAllStatuses(categoryIdVO, tagIdVO);
                }
            } else if (categoryId != null) {
                // 按分类过滤（支持状态筛选）
                CategoryId categoryIdVO = new CategoryId(categoryId);
                articles = articleApplicationService.findByCategoryId(categoryIdVO, statusEnum, page, size);
                totalCount = articleApplicationService.countByCategoryId(categoryIdVO, statusEnum);
            } else if (tagId != null) {
                // 按标签过滤（支持状态筛选）
                TagId tagIdVO = new TagId(tagId);
                articles = articleApplicationService.findByTagId(tagIdVO, statusEnum, page, size);
                totalCount = articleApplicationService.countByTagId(tagIdVO, statusEnum);
            } else if (statusEnum != null) {
                // 仅按状态过滤
                articles = articleApplicationService.findByStatus(statusEnum, page, size);
                totalCount = articleApplicationService.countByStatus(statusEnum);
            } else {
                // 无过滤条件，查询所有文章
                articles = articleApplicationService.findAllArticles(page, size);
                totalCount = articleApplicationService.countAllArticles();
            }
            
            // 构造响应对象，包含完整的实体信息
            List<ArticleResponse> articleResponses = articles.stream()
                .map(this::buildArticleResponseWithEntities)
                .collect(Collectors.toList());
            
            ArticleListResponse response = new ArticleListResponse(articleResponses, totalCount, page, size);
            
            return Result.success(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("查询文章列表失败，参数错误: {}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("查询文章列表失败", e);
            return Result.error("查询文章列表失败");
        }
    }
    
    /**
     * 根据ID查询文章
     * 
     * @param id 文章ID
     * @return 文章信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询文章详情", description = "根据文章ID查询文章详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "文章不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleResponse> getArticleById(
            @Parameter(description = "文章ID") @PathVariable @NotNull Long id) {
        
        logger.debug("接收到查询文章请求，文章ID: {}", id);
        
        try {
            ArticleId articleId = new ArticleId(id.toString());
            Optional<ArticleAggregate> articleOpt = articleApplicationService.findById(articleId);
            
            if (articleOpt.isEmpty()) {
                return Result.error("文章不存在");
            }
            
            ArticleResponse response = buildArticleResponseWithEntities(articleOpt.get());
            return Result.success(response);
            
        } catch (Exception e) {
            logger.error("查询文章失败，文章ID: {}", id, e);
            return Result.error("查询文章失败");
        }
    }
    
    /**
     * 构建包含完整实体信息的ArticleResponse
     * 
     * @param article 文章聚合根
     * @return 包含完整实体信息的ArticleResponse
     */
    private ArticleResponse buildArticleResponseWithEntities(ArticleAggregate article) {
        try {
            // 获取分类信息
            CategoryResponse categoryResponse = null;
            if (article.getCategoryId() != null) {
                Optional<CategoryAggregate> categoryOpt = categoryApplicationService.findById(article.getCategoryId());
                if (categoryOpt.isPresent()) {
                    categoryResponse = new CategoryResponse(categoryOpt.get());
                }
            }
            
            // 获取作者信息
            UserResponse authorResponse = null;
            if (article.getAuthorId() != null) {
                // 将AuthorId转换为UserId
                UserId userId = UserId.of(article.getAuthorId().getValue());
                Optional<UserAggregate> userOpt = userApplicationService.findById(userId);
                if (userOpt.isPresent()) {
                    authorResponse = UserResponse.from(userOpt.get());
                }
            }
            
            // 获取标签信息
            Set<TagResponse> tagResponses = new HashSet<>();
            if (article.getTagIds() != null && !article.getTagIds().isEmpty()) {
                List<TagAggregate> tags = tagApplicationService.findByIds(article.getTagIds());
                tagResponses = tags.stream()
                    .map(TagResponse::from)
                    .collect(Collectors.toSet());
            }
            
            // 构造包含完整实体信息的ArticleResponse
            return new ArticleResponse(article, categoryResponse, authorResponse, tagResponses);
            
        } catch (Exception e) {
            logger.error("构建文章响应时发生错误，文章ID: {}", article.getId().getValue(), e);
            // 如果获取关联实体失败，返回基本的ArticleResponse
            return new ArticleResponse(article);
        }
    }
    
    /**
     * 查询已发布的文章列表（分页，支持可选过滤）
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param categoryId 可选的分类ID过滤
     * @param tagId 可选的标签ID过滤
     * @return 文章列表
     */
    @GetMapping("/all")
    @Operation(summary = "查询文章列表", description = "分页查询文章列表，支持按状态、分类和标签过滤")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleListResponse> getAllArticles(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @Parameter(description = "文章状态（可选）：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档") @RequestParam(required = false) String status,
            @Parameter(description = "分类ID（可选）") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "标签ID（可选）") @RequestParam(required = false) Long tagId) {
        
        logger.debug("接收到查询所有文章列表请求，页码: {}, 每页大小: {}, 状态: {}, 分类ID: {}, 标签ID: {}", page, size, status, categoryId, tagId);
        
        try {
            List<ArticleAggregate> articles;
            long totalCount;
            
            // 解析状态参数
            ArticleStatus statusEnum = null;
            if (status != null && !status.trim().isEmpty()) {
                try {
                    statusEnum = ArticleStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return Result.error("无效的文章状态: " + status + "，支持的状态：DRAFT、PUBLISHED、ARCHIVED");
                }
            }
            
            // 根据过滤条件选择不同的查询方法
            if (categoryId != null && tagId != null) {
                // 同时按分类和标签过滤（支持状态筛选）
                CategoryId categoryIdVO = new CategoryId(categoryId);
                TagId tagIdVO = new TagId(tagId);
                if (statusEnum != null) {
                    articles = articleApplicationService.findByCategoryAndTagAndStatus(categoryIdVO, tagIdVO, statusEnum, page, size);
                    totalCount = articleApplicationService.countByCategoryAndTagAndStatus(categoryIdVO, tagIdVO, statusEnum);
                } else {
                    articles = articleApplicationService.findByCategoryAndTag(categoryIdVO, tagIdVO, page, size);
                    totalCount = articleApplicationService.countByCategoryAndTagAllStatuses(categoryIdVO, tagIdVO);
                }
            } else if (categoryId != null) {
                // 按分类过滤（支持状态筛选）
                CategoryId categoryIdVO = new CategoryId(categoryId);
                articles = articleApplicationService.findByCategoryId(categoryIdVO, statusEnum, page, size);
                totalCount = articleApplicationService.countByCategoryId(categoryIdVO, statusEnum);
            } else if (tagId != null) {
                // 按标签过滤（支持状态筛选）
                TagId tagIdVO = new TagId(tagId);
                articles = articleApplicationService.findByTagId(tagIdVO, statusEnum, page, size);
                totalCount = articleApplicationService.countByTagId(tagIdVO, statusEnum);
            } else if (statusEnum != null) {
                // 仅按状态过滤
                articles = articleApplicationService.findByStatus(statusEnum, page, size);
                totalCount = articleApplicationService.countByStatus(statusEnum);
            } else {
                // 无过滤条件，查询所有文章
                articles = articleApplicationService.findAllArticles(page, size);
                totalCount = articleApplicationService.countAllArticles();
            }
            
            // 构造响应对象，包含完整的实体信息
            List<ArticleResponse> articleResponses = articles.stream()
                .map(this::buildArticleResponseWithEntities)
                .collect(Collectors.toList());
            
            ArticleListResponse response = new ArticleListResponse(articleResponses, totalCount, page, size);
            
            return Result.success(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("查询所有文章列表失败，参数错误: {}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("查询所有文章列表失败", e);
            return Result.error("查询文章列表失败");
        }
    }
    
    /**
     * 根据作者ID查询文章列表（分页）
     * 
     * @param authorId 作者ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章列表
     */
    @GetMapping("/author/{authorId}")
    @Operation(summary = "查询作者文章列表", description = "分页查询指定作者的文章列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleListResponse> getArticlesByAuthor(
            @Parameter(description = "作者ID") @PathVariable @NotNull Long authorId,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        
        logger.debug("接收到查询作者文章列表请求，作者ID: {}, 页码: {}, 每页大小: {}", authorId, page, size);
        
        try {
            AuthorId authorIdVO = new AuthorId(authorId);
            List<ArticleAggregate> articles = articleApplicationService.findByAuthorId(authorIdVO, page, size);
            
            // 获取总数
            long totalCount = articleApplicationService.countByAuthorId(authorIdVO);
            
            // 构造响应对象，包含完整的实体信息
            List<ArticleResponse> articleResponses = articles.stream()
                .map(this::buildArticleResponseWithEntities)
                .collect(Collectors.toList());
            
            ArticleListResponse response = new ArticleListResponse(articleResponses, totalCount, page, size);
            
            return Result.success(response);
            
        } catch (Exception e) {
            logger.error("查询作者文章列表失败，作者ID: {}", authorId, e);
            return Result.error("查询作者文章列表失败");
        }
    }
    
    /**
     * 根据分类ID查询文章列表
     * 
     * @param categoryId 分类ID
     * @return 文章列表
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "查询分类文章列表", description = "查询指定分类下的文章列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleListResponse> getArticlesByCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long categoryId) {
        
        logger.debug("接收到查询分类文章列表请求，分类ID: {}", categoryId);
        
        try {
            CategoryId categoryIdVO = new CategoryId(categoryId);
            List<ArticleAggregate> articles = articleApplicationService.findByCategoryId(categoryIdVO);
            
            // 获取总数
            long totalCount = articleApplicationService.countByCategoryId(categoryIdVO);
            
            // 构造响应对象，包含完整的实体信息
            List<ArticleResponse> articleResponses = articles.stream()
                .map(this::buildArticleResponseWithEntities)
                .collect(Collectors.toList());
            
            ArticleListResponse response = new ArticleListResponse(articleResponses, totalCount, 0, articles.size());
            
            return Result.success(response);
            
        } catch (Exception e) {
            logger.error("查询分类文章列表失败，分类ID: {}", categoryId, e);
            return Result.error("查询分类文章列表失败");
        }
    }
    

    
    /**
     * 根据标题关键词搜索文章
     * 
     * @param keyword 搜索关键词
     * @return 文章列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索文章", description = "根据标题关键词搜索文章")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "搜索成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleListResponse> searchArticles(
            @Parameter(description = "搜索关键词") @RequestParam @NotBlank String keyword) {
        
        logger.debug("接收到搜索文章请求，关键词: {}", keyword);
        
        try {
            List<ArticleAggregate> articles = articleApplicationService.searchByTitle(keyword);
            
            // 构造响应对象，包含完整的实体信息
            List<ArticleResponse> articleResponses = articles.stream()
                .map(this::buildArticleResponseWithEntities)
                .collect(Collectors.toList());
            
            ArticleListResponse response = new ArticleListResponse(articleResponses, (long) articles.size(), 0, articles.size());
            
            return Result.success(response);
            
        } catch (Exception e) {
            logger.error("搜索文章失败，关键词: {}", keyword, e);
            return Result.error("搜索文章失败");
        }
    }
    
    /**
     * 查询最近发布的文章
     * 
     * @param limit 限制数量
     * @return 文章列表
     */
    @GetMapping("/recent")
    @Operation(summary = "查询最近发布文章", description = "查询最近发布的文章列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleListResponse> getRecentArticles(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        
        logger.debug("接收到查询最近发布文章请求，限制数量: {}", limit);
        
        try {
            List<ArticleAggregate> articles = articleApplicationService.findRecentlyPublished(limit);
            
            // 构造响应对象，包含完整的实体信息
            List<ArticleResponse> articleResponses = articles.stream()
                .map(this::buildArticleResponseWithEntities)
                .collect(Collectors.toList());
            
            ArticleListResponse response = new ArticleListResponse(articleResponses, (long) articles.size(), 0, articles.size());
            
            return Result.success(response);
            
        } catch (Exception e) {
            logger.error("查询最近发布文章失败", e);
            return Result.error("查询最近发布文章失败");
        }
    }
    
    /**
     * 查询热门文章
     * 
     * @param limit 限制数量
     * @return 文章列表
     */
    @GetMapping("/popular")
    @Operation(summary = "查询热门文章", description = "查询热门文章列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ArticleListResponse> getPopularArticles(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        
        logger.debug("接收到查询热门文章请求，限制数量: {}", limit);
        
        try {
            List<ArticleAggregate> articles = articleApplicationService.findPopularArticles(limit);
            
            // 构造响应对象，包含完整的实体信息
            List<ArticleResponse> articleResponses = articles.stream()
                .map(this::buildArticleResponseWithEntities)
                .collect(Collectors.toList());
            
            ArticleListResponse response = new ArticleListResponse(articleResponses, (long) articles.size(), 0, articles.size());
            
            return Result.success(response);
            
        } catch (Exception e) {
            logger.error("查询热门文章失败", e);
            return Result.error("查询热门文章失败");
        }
    }
}