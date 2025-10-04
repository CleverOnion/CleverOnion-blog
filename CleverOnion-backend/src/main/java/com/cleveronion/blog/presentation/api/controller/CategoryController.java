package com.cleveronion.blog.presentation.api.controller;

import com.cleveronion.blog.application.category.command.CreateCategoryCommand;
import com.cleveronion.blog.application.category.command.DeleteCategoryCommand;
import com.cleveronion.blog.application.category.command.UpdateCategoryCommand;
import com.cleveronion.blog.application.category.service.CategoryCommandService;
import com.cleveronion.blog.application.category.service.CategoryQueryService;
import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import com.cleveronion.blog.domain.article.repository.CategoryRepository;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.presentation.api.dto.*;
import com.cleveronion.blog.presentation.api.dto.CategoryWithCountResponse;
import com.cleveronion.blog.presentation.api.dto.CategoryWithCountListResponse;
import com.cleveronion.blog.common.exception.ResourceNotFoundException;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 分类控制器
 * 提供分类管理相关的 RESTful API 接口
 * 使用CQRS模式，命令和查询分离
 * 
 * @author CleverOnion
 * @since 1.0.0
 * @version 2.0.0 - 引入CQRS架构
 */
@RestController
@RequestMapping("/categories")
@Tag(name = "分类管理", description = "分类的创建、更新、删除、查询等操作")
@Validated
public class CategoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    
    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;
    
    public CategoryController(
        CategoryCommandService categoryCommandService,
        CategoryQueryService categoryQueryService
    ) {
        this.categoryCommandService = categoryCommandService;
        this.categoryQueryService = categoryQueryService;
    }
    
    /**
     * 创建分类
     * 
     * @param request 创建分类请求
     * @return 创建的分类信息
     */
    @PostMapping
    @Operation(summary = "创建分类", description = "创建一个新的分类")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "分类创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        
        logger.info("接收到创建分类请求，名称: {}, 图标: {}", request.getName(), request.getIcon());
        
        // 构建命令对象
        CreateCategoryCommand command = CreateCategoryCommand.of(request.getName(), request.getIcon());
        
        // 执行命令
        CategoryAggregate category = categoryCommandService.createCategory(command);
        
        // 构建响应
        CategoryResponse response = new CategoryResponse(category);
        
        logger.info("成功创建分类，分类ID: {}, 名称: {}", category.getId().getValue(), category.getName());
        
        return Result.success(response);
    }
    
    /**
     * 更新分类
     * 
     * @param id 分类ID
     * @param request 更新分类请求
     * @return 更新后的分类信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分类", description = "更新指定分类的信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "分类更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "分类不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<CategoryResponse> updateCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        
        logger.info("接收到更新分类请求，分类ID: {}, 新名称: {}, 新图标: {}", id, request.getName(), request.getIcon());
        
        // 构建分类ID值对象
        CategoryId categoryId = CategoryId.of(id);
        
        // 构建命令对象
        UpdateCategoryCommand command = UpdateCategoryCommand.of(categoryId, request.getName(), request.getIcon());
        
        // 执行命令
        CategoryAggregate category = categoryCommandService.updateCategory(command);
        
        // 构建响应
        CategoryResponse response = new CategoryResponse(category);
        
        logger.info("成功更新分类，分类ID: {}, 新名称: {}", category.getId().getValue(), category.getName());
        
        return Result.success(response);
    }
    
    /**
     * 删除分类
     * 
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "删除指定的分类")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "分类删除成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "分类不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long id) {
        
        logger.info("接收到删除分类请求，分类ID: {}", id);
        
        // 构建分类ID值对象
        CategoryId categoryId = CategoryId.of(id);
        
        // 构建命令对象
        DeleteCategoryCommand command = DeleteCategoryCommand.of(categoryId);
        
        // 执行命令
        categoryCommandService.deleteCategory(command);
        
        logger.info("成功删除分类，分类ID: {}", id);
        
        return Result.success();
    }
    
    /**
     * 查询分类详情
     * 
     * @param id 分类ID
     * @return 分类详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询分类详情", description = "根据分类ID查询分类详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "分类不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<CategoryResponse> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long id) {
        
        logger.debug("接收到查询分类详情请求，分类ID: {}", id);
        
        // 构建分类ID值对象
        CategoryId categoryId = CategoryId.of(id);
        
        // 查询分类
        Optional<CategoryAggregate> categoryOpt = categoryQueryService.findById(categoryId);
        
        if (categoryOpt.isEmpty()) {
            logger.warn("分类不存在，分类ID: {}", id);
            throw new ResourceNotFoundException("分类不存在，ID: " + id);
        }
        
        // 构建响应
        CategoryResponse response = new CategoryResponse(categoryOpt.get());
        
        return Result.success(response);
    }
    
    /**
     * 查询所有分类列表
     * 
     * @param sortBy 排序方式（name: 按名称排序, created_at: 按创建时间排序）
     * @param order 排序顺序（asc: 升序, desc: 降序）
     * @return 分类列表
     */
    @GetMapping
    @Operation(summary = "查询分类列表", description = "查询所有分类列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<CategoryListResponse> getAllCategories(
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "排序顺序") @RequestParam(defaultValue = "asc") String order) {
        
        logger.debug("接收到查询分类列表请求，排序方式: {}, 排序顺序: {}", sortBy, order);
        
        boolean ascending = "asc".equalsIgnoreCase(order);
        List<CategoryAggregate> categories;
        
        // 根据排序方式查询
        if ("created_at".equalsIgnoreCase(sortBy)) {
            categories = categoryQueryService.findAllOrderByCreatedAt(ascending);
        } else {
            // 默认按名称排序
            categories = categoryQueryService.findAllOrderByName(ascending);
        }
        
        // 构建响应
        List<CategoryResponse> categoryResponses = categories.stream()
            .map(CategoryResponse::new)
            .collect(Collectors.toList());
        
        CategoryListResponse response = new CategoryListResponse(categoryResponses, categoryResponses.size());
        
        return Result.success(response);
    }
    
    /**
     * 分页查询分类列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分类列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询分类列表", description = "分页查询分类列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<CategoryListResponse> getCategoriesWithPagination(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        
        logger.debug("接收到分页查询分类列表请求，页码: {}, 每页大小: {}", page, size);
        
        // 分页查询分类
        List<CategoryAggregate> categories = categoryQueryService.findWithPagination(page, size);
        
        // 获取总数
        long totalCount = categoryQueryService.countAll();
        
        // 构建响应
        List<CategoryResponse> categoryResponses = categories.stream()
            .map(CategoryResponse::new)
            .collect(Collectors.toList());
        
        CategoryListResponse response = new CategoryListResponse(categoryResponses, (int) totalCount);
        
        return Result.success(response);
    }
    
    /**
     * 搜索分类
     * 
     * @param keyword 搜索关键词
     * @return 分类列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索分类", description = "根据名称关键词搜索分类")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "搜索成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<CategoryListResponse> searchCategories(
            @Parameter(description = "搜索关键词") @RequestParam @NotBlank String keyword) {
        
        logger.debug("接收到搜索分类请求，关键词: {}", keyword);
        
        // 搜索分类
        List<CategoryAggregate> categories = categoryQueryService.searchByName(keyword);
        
        // 构建响应
        List<CategoryResponse> categoryResponses = categories.stream()
            .map(CategoryResponse::new)
            .collect(Collectors.toList());
        
        CategoryListResponse response = new CategoryListResponse(categoryResponses, categoryResponses.size());
        
        return Result.success(response);
    }
    
    /**
     * 查询最近创建的分类
     * 
     * @param limit 限制数量
     * @return 分类列表
     */
    @GetMapping("/recent")
    @Operation(summary = "查询最近创建分类", description = "查询最近创建的分类列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<CategoryListResponse> getRecentCategories(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        
        logger.debug("接收到查询最近创建分类请求，限制数量: {}", limit);
        
        // 查询最近创建的分类
        List<CategoryAggregate> categories = categoryQueryService.findRecentlyCreated(limit);
        
        // 构建响应
        List<CategoryResponse> categoryResponses = categories.stream()
            .map(CategoryResponse::new)
            .collect(Collectors.toList());
        
        CategoryListResponse response = new CategoryListResponse(categoryResponses, categoryResponses.size());
        
        return Result.success(response);
    }
    
    /**
     * 统计分类总数
     * 
     * @return 分类总数
     */
    @GetMapping("/count")
    @Operation(summary = "统计分类总数", description = "统计系统中分类的总数量")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "统计成功"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Long> countCategories() {
        
        logger.debug("接收到统计分类总数请求");
        
        // 统计分类总数
        long count = categoryQueryService.countAll();
        
        return Result.success(count);
    }
    
    /**
     * 分页查询分类及其文章数量
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分类及文章数量列表
     */
    @GetMapping("/with-count")
    @Operation(summary = "分页查询分类及其文章数量", description = "分页查询分类列表，包含每个分类下的文章数量")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<CategoryWithCountListResponse> getCategoriesWithArticleCount(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        
        logger.debug("接收到分页查询分类及文章数量请求，页码: {}, 每页大小: {}", page, size);
        
        // 分页查询分类及文章数量
        List<CategoryRepository.CategoryWithArticleCount> categoriesWithCount = 
            categoryQueryService.findWithArticleCount(page, size);
        
        // 获取总数
        long totalCount = categoryQueryService.countAll();
        
        // 构建响应
        List<CategoryWithCountResponse> categoryResponses = categoriesWithCount.stream()
            .map(categoryWithCount -> CategoryWithCountResponse.from(
                categoryWithCount.getCategory(), 
                categoryWithCount.getArticleCount()))
            .collect(Collectors.toList());
        
        // 计算总页数
        int totalPages = (int) Math.ceil((double) totalCount / size);
        
        CategoryWithCountListResponse response = new CategoryWithCountListResponse(
            categoryResponses, totalCount, page, size, totalPages);
        
        return Result.success(response);
    }
}