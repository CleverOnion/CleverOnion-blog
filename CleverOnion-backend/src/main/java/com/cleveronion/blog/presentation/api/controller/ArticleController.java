package com.cleveronion.blog.presentation.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.cleveronion.blog.application.article.command.CreateArticleDraftCommand;
import com.cleveronion.blog.application.article.command.PublishArticleCommand;
import com.cleveronion.blog.application.article.command.UpdateArticleCommand;
import com.cleveronion.blog.application.article.service.ArticleCommandService;
import com.cleveronion.blog.application.article.service.ArticleQueryService;
import com.cleveronion.blog.application.category.service.CategoryQueryService;
import com.cleveronion.blog.application.tag.command.FindOrCreateTagsCommand;
import com.cleveronion.blog.application.tag.service.TagCommandService;
import com.cleveronion.blog.application.tag.service.TagQueryService;
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

import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
    
    private final ArticleCommandService articleCommandService;  // CQRS命令服务
    private final ArticleQueryService articleQueryService;      // CQRS查询服务
    private final CategoryQueryService categoryQueryService;    // CQRS查询服务
    private final TagCommandService tagCommandService;          // CQRS命令服务
    private final TagQueryService tagQueryService;              // CQRS查询服务
    private final UserApplicationService userApplicationService;
    
    public ArticleController(ArticleCommandService articleCommandService,
                           ArticleQueryService articleQueryService,
                           CategoryQueryService categoryQueryService,
                           TagCommandService tagCommandService,
                           TagQueryService tagQueryService,
                           UserApplicationService userApplicationService) {
        this.articleCommandService = articleCommandService;
        this.articleQueryService = articleQueryService;
        this.categoryQueryService = categoryQueryService;
        this.tagCommandService = tagCommandService;
        this.tagQueryService = tagQueryService;
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
        
        // 构建作者ID和分类ID值对象
        AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
        CategoryId categoryId = new CategoryId(request.getCategoryId());
        
        // 处理标签：根据标签名查找或创建标签
        Set<TagId> tagIds = null;
        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            FindOrCreateTagsCommand tagCommand = FindOrCreateTagsCommand.of(request.getTagNames());
            List<TagAggregate> tags = tagCommandService.findOrCreateByNames(tagCommand);
            tagIds = tags.stream()
                .map(TagAggregate::getId)
                .collect(Collectors.toSet());
        }
        
        // 构建命令对象
        CreateArticleDraftCommand command = new CreateArticleDraftCommand(
            content,
            categoryId,
            authorId,
            tagIds
        );
        
        // 执行命令 - 使用CQRS命令服务
        ArticleAggregate article = articleCommandService.createDraft(command);
        
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
        
        // 处理标签：根据标签名查找或创建标签
        Set<TagId> tagIds = null;
        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            FindOrCreateTagsCommand tagCommand = FindOrCreateTagsCommand.of(request.getTagNames());
            List<TagAggregate> tags = tagCommandService.findOrCreateByNames(tagCommand);
            tagIds = tags.stream()
                .map(TagAggregate::getId)
                .collect(Collectors.toSet());
        }
        
        // 构建命令对象
        PublishArticleCommand command = new PublishArticleCommand(
            content,
            categoryId,
            authorId,
            tagIds
        );
        
        // 执行命令 - 使用CQRS命令服务
        ArticleAggregate article = articleCommandService.createAndPublish(command);
        
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
        
        ArticleId articleId = new ArticleId(id.toString());
        AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
        
        // 查找文章（用于获取原始值）- 使用CQRS查询服务
        Optional<ArticleAggregate> articleOpt = articleQueryService.findById(articleId);
        if (articleOpt.isEmpty()) {
            return Result.error("文章不存在");
        }
        
        ArticleAggregate article = articleOpt.get();
        
        // 构建新的内容（合并原有值和新值）
        ArticleContent newContent = new ArticleContent(
            request.getTitle() != null ? request.getTitle() : article.getContent().getTitle(),
            request.getContent() != null ? request.getContent() : article.getContent().getContent(),
            request.getSummary() != null ? request.getSummary() : article.getContent().getSummary()
        );
        
        // 处理标签更新
        Set<TagId> newTagIds = null;
        if (request.getTagNames() != null) {
            if (!request.getTagNames().isEmpty()) {
                FindOrCreateTagsCommand tagCommand = FindOrCreateTagsCommand.of(request.getTagNames());
            List<TagAggregate> tags = tagCommandService.findOrCreateByNames(tagCommand);
                newTagIds = tags.stream()
                    .map(TagAggregate::getId)
                    .collect(Collectors.toSet());
            } else {
                newTagIds = Set.of(); // 空集合表示清空标签
            }
        }
        
        // 构建更新命令
        UpdateArticleCommand command = new UpdateArticleCommand(
            articleId,
            newContent,
            request.getCategoryId() != null ? new CategoryId(request.getCategoryId()) : null,
            newTagIds,
            authorId
        );
        
        // 执行命令 - 使用CQRS命令服务
        article = articleCommandService.updateContent(command);
        
        ArticleResponse response = buildArticleResponseWithEntities(article);
        
        logger.info("成功更新文章，ID: {}", id);
        
        return Result.success(response);
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
        
        ArticleId articleId = new ArticleId(id.toString());
        AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
        
        // 执行命令 - 使用CQRS命令服务
        ArticleAggregate article = articleCommandService.publish(articleId, authorId);
        ArticleResponse response = buildArticleResponseWithEntities(article);
        
        logger.info("成功发布文章，ID: {}", id);
        
        return Result.success(response);
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
        
        ArticleId articleId = new ArticleId(id.toString());
        AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
        
        // 执行命令 - 使用CQRS命令服务
        ArticleAggregate article = articleCommandService.archive(articleId, authorId);
        ArticleResponse response = buildArticleResponseWithEntities(article);
        
        return Result.success(response);
    }
    
    /**
     * 恢复文章为草稿
     * 
     * @param id 文章ID
     * @return 恢复后的文章信息
     */
    @PostMapping("/{id}/revert")
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
        
        ArticleId articleId = new ArticleId(id.toString());
        AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
        
        // 使用CQRS命令服务
        ArticleAggregate article = articleCommandService.revertToDraft(articleId, authorId);
        ArticleResponse response = buildArticleResponseWithEntities(article);
        return Result.success(response);
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
        
        ArticleId articleId = new ArticleId(id.toString());
        AuthorId authorId = new AuthorId(StpUtil.getLoginIdAsLong());
        
        // 执行命令 - 使用CQRS命令服务
        articleCommandService.delete(articleId, authorId);
        return Result.success();
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
    @Cacheable(value = "article-list-responses", 
               key = "'p:' + #page + ':s:' + #size + ':st:' + #status + ':c:' + #categoryId + ':t:' + #tagId")
    public Result<ArticleListResponse> getPublishedArticles(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @Parameter(description = "文章状态（可选）：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档") @RequestParam(required = false) String status,
            @Parameter(description = "分类ID（可选）") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "标签ID（可选）") @RequestParam(required = false) Long tagId) {
        
        logger.debug("接收到查询文章列表请求，页码: {}, 每页大小: {}, 状态: {}, 分类ID: {}, 标签ID: {}", page, size, status, categoryId, tagId);
        
        List<ArticleAggregate> articles;
        long totalCount;
        
        // 解析状态参数
        ArticleStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            statusEnum = ArticleStatus.valueOf(status.toUpperCase());
        }
        
        // 使用CQRS查询服务
        if (statusEnum == null && categoryId == null && tagId == null) {
            // 无过滤条件，查询所有文章
            articles = articleQueryService.findAllArticles(page, size);
            totalCount = articleQueryService.countAllArticles();
        } else if (statusEnum != null && categoryId == null && tagId == null) {
            // 仅按状态过滤
            articles = articleQueryService.findByStatus(statusEnum, page, size);
            totalCount = articleQueryService.countByStatus(statusEnum);
        } else if (statusEnum == ArticleStatus.PUBLISHED && categoryId != null && tagId == null) {
            // 按分类过滤已发布文章
            articles = articleQueryService.findPublishedByCategoryId(new CategoryId(categoryId), page, size);
            totalCount = articleQueryService.countByCategoryId(new CategoryId(categoryId));
        } else if (statusEnum == ArticleStatus.PUBLISHED && tagId != null && categoryId == null) {
            // 按标签过滤已发布文章
            articles = articleQueryService.findPublishedByTagId(new TagId(tagId), page, size);
            totalCount = articleQueryService.countByTagId(new TagId(tagId));
        } else if (statusEnum == null || statusEnum == ArticleStatus.PUBLISHED) {
            // 默认查询已发布文章
            articles = articleQueryService.findPublishedArticles(page, size);
            totalCount = articleQueryService.countPublishedArticles();
        } else {
            // 复杂过滤暂时使用旧服务（待优化）
            CategoryId categoryIdVO = categoryId != null ? new CategoryId(categoryId) : null;
            TagId tagIdVO = tagId != null ? new TagId(tagId) : null;
            if (categoryIdVO != null && tagIdVO != null) {
                articles = articleQueryService.findPublishedByCategoryAndTag(categoryIdVO, tagIdVO, page, size);
                totalCount = articleQueryService.countByCategoryId(categoryIdVO);
            } else {
                articles = articleQueryService.findAllArticles(page, size);
                totalCount = articleQueryService.countAllArticles();
            }
        }
        
        // 构造响应对象，包含完整的实体信息（使用批量查询优化N+1问题）
        List<ArticleResponse> articleResponses = buildArticleResponsesWithEntitiesBatch(articles);
        
        ArticleListResponse response = new ArticleListResponse(articleResponses, totalCount, page, size);
        
        return Result.success(response);
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
    @Cacheable(value = "article-responses", key = "#id")  // 缓存DTO
    public Result<ArticleResponse> getArticleById(
            @Parameter(description = "文章ID") @PathVariable @NotNull Long id) {
        
        logger.debug("接收到查询文章请求，文章ID: {}", id);
        
        ArticleId articleId = new ArticleId(id.toString());
        
        // 使用CQRS查询服务
        Optional<ArticleAggregate> articleOpt = articleQueryService.findById(articleId);
        
        if (articleOpt.isEmpty()) {
            return Result.error("文章不存在");
        }
        
        ArticleResponse response = buildArticleResponseWithEntities(articleOpt.get());
        return Result.success(response);
    }
    
    /**
     * 构建包含完整实体信息的ArticleResponse
     * 
     * @param article 文章聚合根
     * @return 包含完整实体信息的ArticleResponse
     */
    private ArticleResponse buildArticleResponseWithEntities(ArticleAggregate article) {
        // 获取分类信息
        CategoryResponse categoryResponse = null;
        if (article.getCategoryId() != null) {
            Optional<CategoryAggregate> categoryOpt = categoryQueryService.findById(article.getCategoryId());
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
            List<TagAggregate> tags = tagQueryService.findByIds(article.getTagIds());
            tagResponses = tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toSet());
        }
        
        // 构造包含完整实体信息的ArticleResponse
        return new ArticleResponse(article, categoryResponse, authorResponse, tagResponses);
    }
    
    /**
     * 批量构建包含完整实体信息的ArticleResponse列表
     * 优化N+1查询问题，一次性获取所有关联数据
     * 
     * @param articles 文章聚合根列表
     * @return 包含完整实体信息的ArticleResponse列表
     */
    private List<ArticleResponse> buildArticleResponsesWithEntitiesBatch(List<ArticleAggregate> articles) {
        if (articles == null || articles.isEmpty()) {
            return List.of();
        }
        
        logger.debug("开始批量构建文章响应，文章数量: {}", articles.size());
        
        // 收集所有需要查询的ID
        Set<CategoryId> categoryIds = articles.stream()
            .map(ArticleAggregate::getCategoryId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
            
        Set<UserId> userIds = articles.stream()
            .map(ArticleAggregate::getAuthorId)
            .filter(authorId -> authorId != null)
            .map(authorId -> UserId.of(authorId.getValue()))
            .collect(Collectors.toSet());
            
        Set<TagId> allTagIds = articles.stream()
            .filter(article -> article.getTagIds() != null)
            .flatMap(article -> article.getTagIds().stream())
            .collect(Collectors.toSet());
        
        // 批量查询关联数据
        Map<CategoryId, CategoryAggregate> categoryMap = categoryIds.isEmpty() ? 
            Map.of() : 
            categoryQueryService.findByIds(categoryIds).stream()
                .collect(Collectors.toMap(CategoryAggregate::getId, Function.identity()));
                
        Map<UserId, UserAggregate> userMap = userIds.isEmpty() ? 
            Map.of() : 
            userApplicationService.findByIds(userIds).stream()
                .collect(Collectors.toMap(UserAggregate::getId, Function.identity()));
                
        Map<TagId, TagAggregate> tagMap = allTagIds.isEmpty() ? 
            Map.of() : 
            tagQueryService.findByIds(allTagIds).stream()
                .collect(Collectors.toMap(TagAggregate::getId, Function.identity()));
        
        logger.debug("批量查询完成，分类数量: {}, 用户数量: {}, 标签数量: {}", 
            categoryMap.size(), userMap.size(), tagMap.size());
        
        // 构建响应列表
        return articles.stream()
            .map(article -> {
                // 获取分类信息
                CategoryResponse categoryResponse = null;
                if (article.getCategoryId() != null) {
                    CategoryAggregate category = categoryMap.get(article.getCategoryId());
                    if (category != null) {
                        categoryResponse = new CategoryResponse(category);
                    }
                }
                
                // 获取作者信息
                UserResponse authorResponse = null;
                if (article.getAuthorId() != null) {
                    UserId userId = UserId.of(article.getAuthorId().getValue());
                    UserAggregate user = userMap.get(userId);
                    if (user != null) {
                        authorResponse = UserResponse.from(user);
                    }
                }
                
                // 获取标签信息
                Set<TagResponse> tagResponses = new HashSet<>();
                if (article.getTagIds() != null && !article.getTagIds().isEmpty()) {
                    tagResponses = article.getTagIds().stream()
                        .map(tagMap::get)
                        .filter(tag -> tag != null)
                        .map(TagResponse::from)
                        .collect(Collectors.toSet());
                }
                
                return new ArticleResponse(article, categoryResponse, authorResponse, tagResponses);
            })
            .collect(Collectors.toList());
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
        
        List<ArticleAggregate> articles;
        long totalCount;
        
        // 解析状态参数
        ArticleStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            statusEnum = ArticleStatus.valueOf(status.toUpperCase());
        }
        
        // 根据过滤条件选择不同的查询方法
        if (categoryId != null && tagId != null) {
            // 同时按分类和标签过滤（支持状态筛选）
            CategoryId categoryIdVO = new CategoryId(categoryId);
            TagId tagIdVO = new TagId(tagId);
            // 使用CQRS查询服务
            if (statusEnum == ArticleStatus.PUBLISHED || statusEnum == null) {
                articles = articleQueryService.findPublishedByCategoryAndTag(categoryIdVO, tagIdVO, page, size);
                totalCount = articleQueryService.countByCategoryId(categoryIdVO);
            } else {
                // 非已发布状态的复杂查询，简化为基础查询
                articles = articleQueryService.findByStatus(statusEnum, page, size);
                totalCount = articleQueryService.countByStatus(statusEnum);
            }
        } else if (categoryId != null) {
            // 按分类过滤 - 使用CQRS查询服务
            CategoryId categoryIdVO = new CategoryId(categoryId);
            if (statusEnum == ArticleStatus.PUBLISHED || statusEnum == null) {
                articles = articleQueryService.findPublishedByCategoryId(categoryIdVO, page, size);
            } else {
                articles = articleQueryService.findByStatus(statusEnum, page, size);
            }
            totalCount = articleQueryService.countByCategoryId(categoryIdVO);
        } else if (tagId != null) {
            // 按标签过滤 - 使用CQRS查询服务
            TagId tagIdVO = new TagId(tagId);
            if (statusEnum == ArticleStatus.PUBLISHED || statusEnum == null) {
                articles = articleQueryService.findPublishedByTagId(tagIdVO, page, size);
            } else {
                articles = articleQueryService.findByStatus(statusEnum, page, size);
            }
            totalCount = articleQueryService.countByTagId(tagIdVO);
        } else if (statusEnum != null) {
            // 仅按状态过滤 - 使用CQRS查询服务
            articles = articleQueryService.findByStatus(statusEnum, page, size);
            totalCount = articleQueryService.countByStatus(statusEnum);
        } else {
            // 无过滤条件，查询所有文章 - 使用CQRS查询服务
            articles = articleQueryService.findAllArticles(page, size);
            totalCount = articleQueryService.countAllArticles();
        }
        
        // 构造响应对象，包含完整的实体信息（使用批量查询优化N+1问题）
        List<ArticleResponse> articleResponses = buildArticleResponsesWithEntitiesBatch(articles);

        ArticleListResponse response = new ArticleListResponse(articleResponses, totalCount, page, size);

        return Result.success(response);
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
        
        AuthorId authorIdVO = new AuthorId(authorId);
        
        // 使用CQRS查询服务
        List<ArticleAggregate> articles = articleQueryService.findByAuthorId(authorIdVO, page, size);
        long totalCount = articleQueryService.countByAuthorId(authorIdVO);
        
        // 构造响应对象，包含完整的实体信息（使用批量查询优化N+1问题）
        List<ArticleResponse> articleResponses = buildArticleResponsesWithEntitiesBatch(articles);

        ArticleListResponse response = new ArticleListResponse(articleResponses, totalCount, page, size);

        return Result.success(response);
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
        
        CategoryId categoryIdVO = new CategoryId(categoryId);
        
        // 使用CQRS查询服务
        List<ArticleAggregate> articles = articleQueryService.findByCategoryId(categoryIdVO);
        long totalCount = articleQueryService.countByCategoryId(categoryIdVO);
        
        // 构造响应对象，包含完整的实体信息（使用批量查询优化N+1问题）
        List<ArticleResponse> articleResponses = buildArticleResponsesWithEntitiesBatch(articles);

        ArticleListResponse response = new ArticleListResponse(articleResponses, totalCount, 0, articles.size());

        return Result.success(response);
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
    @Cacheable(value = "article-search-responses", key = "'search:' + #keyword")
    public Result<ArticleListResponse> searchArticles(
            @Parameter(description = "搜索关键词") @RequestParam @NotBlank String keyword) {
        
        logger.debug("接收到搜索文章请求，关键词: {}", keyword);
        
        // 使用CQRS查询服务
        List<ArticleAggregate> articles = articleQueryService.searchByTitle(keyword);
        
        // 构造响应对象，包含完整的实体信息（使用批量查询优化N+1问题）
        List<ArticleResponse> articleResponses = buildArticleResponsesWithEntitiesBatch(articles);

        ArticleListResponse response = new ArticleListResponse(articleResponses, (long) articles.size(), 0, articles.size());

        return Result.success(response);
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
    @Cacheable(value = "article-list-responses", key = "'recent:' + #limit")
    public Result<ArticleListResponse> getRecentArticles(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        
        logger.debug("接收到查询最近发布文章请求，限制数量: {}", limit);
        
        // 使用CQRS查询服务
        List<ArticleAggregate> articles = articleQueryService.findRecentlyPublished(limit);
        
        // 构造响应对象，包含完整的实体信息（使用批量查询优化N+1问题）
        List<ArticleResponse> articleResponses = buildArticleResponsesWithEntitiesBatch(articles);

        ArticleListResponse response = new ArticleListResponse(articleResponses, (long) articles.size(), 0, articles.size());

        return Result.success(response);
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
    @Cacheable(value = "article-list-responses", key = "'popular:' + #limit")
    public Result<ArticleListResponse> getPopularArticles(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        
        logger.debug("接收到查询热门文章请求，限制数量: {}", limit);
        
        // 使用CQRS查询服务
        List<ArticleAggregate> articles = articleQueryService.findPopularArticles(limit);
        
        // 构造响应对象，包含完整的实体信息（使用批量查询优化N+1问题）
        List<ArticleResponse> articleResponses = buildArticleResponsesWithEntitiesBatch(articles);

        ArticleListResponse response = new ArticleListResponse(articleResponses, (long) articles.size(), 0, articles.size());

        return Result.success(response);
    }
}