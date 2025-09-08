package com.cleveronion.blog.presentation.api.controller;

import com.cleveronion.blog.application.article.service.TagApplicationService;
import com.cleveronion.blog.domain.article.aggregate.TagAggregate;
import com.cleveronion.blog.domain.article.valueobject.TagId;
import com.cleveronion.blog.presentation.api.dto.CreateTagRequest;
import com.cleveronion.blog.presentation.api.dto.TagListResponse;
import com.cleveronion.blog.presentation.api.dto.TagResponse;
import com.cleveronion.blog.presentation.api.dto.TagWithCountListResponse;
import com.cleveronion.blog.presentation.api.dto.TagWithCountResponse;
import com.cleveronion.blog.presentation.api.dto.UpdateTagRequest;
import com.cleveronion.blog.domain.article.repository.TagRepository;
import com.cleveronion.blog.presentation.api.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 标签管理控制器
 * 提供标签的CRUD操作API接口
 * 
 * @author CleverOnion
 */
@RestController
@RequestMapping("/tags")
@Tag(name = "标签管理", description = "标签的增删改查操作")
public class TagController {
    
    private final TagApplicationService tagApplicationService;
    
    public TagController(TagApplicationService tagApplicationService) {
        this.tagApplicationService = tagApplicationService;
    }
    
    /**
     * 创建标签
     * 
     * @param request 创建标签请求
     * @return 创建的标签信息
     */
    @PostMapping
    @Operation(summary = "创建标签", description = "创建一个新的标签")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "标签创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数无效"),
        @ApiResponse(responseCode = "409", description = "标签名称已存在")
    })
    public Result<TagResponse> createTag(
            @Valid @RequestBody CreateTagRequest request) {
        
        TagAggregate tag = tagApplicationService.createTag(request.getName());
        TagResponse response = TagResponse.from(tag);
        
        return Result.success(response);
    }
    
    /**
     * 更新标签
     * 
     * @param id 标签ID
     * @param request 更新标签请求
     * @return 更新后的标签信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新标签", description = "更新指定ID的标签信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "标签更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数无效"),
        @ApiResponse(responseCode = "404", description = "标签不存在"),
        @ApiResponse(responseCode = "409", description = "标签名称已存在")
    })
    public Result<TagResponse> updateTag(
            @Parameter(description = "标签ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequest request) {
        
        TagId tagId = TagId.of(id);
        TagAggregate tag = tagApplicationService.updateTagName(tagId, request.getName());
        TagResponse response = TagResponse.from(tag);
        
        return Result.success(response);
    }
    
    /**
     * 删除标签
     * 
     * @param id 标签ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签", description = "删除指定ID的标签")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "标签删除成功"),
        @ApiResponse(responseCode = "404", description = "标签不存在")
    })
    public Result<Void> deleteTag(
            @Parameter(description = "标签ID", required = true) @PathVariable Long id) {
        
        TagId tagId = TagId.of(id);
        tagApplicationService.deleteTag(tagId);
        
        return Result.success();
    }
    
    /**
     * 根据ID查询标签详情
     * 
     * @param id 标签ID
     * @return 标签详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询标签详情", description = "根据ID查询标签的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "标签不存在")
    })
    public Result<TagResponse> getTagById(
            @Parameter(description = "标签ID", required = true) @PathVariable Long id) {
        
        TagId tagId = TagId.of(id);
        Optional<TagAggregate> tagOpt = tagApplicationService.findById(tagId);
        
        if (tagOpt.isEmpty()) {
            return Result.notFound("标签不存在");
        }
        
        TagResponse response = TagResponse.from(tagOpt.get());
        return Result.success(response);
    }
    
    /**
     * 查询所有标签
     * 
     * @return 所有标签列表
     */
    @GetMapping
    @Operation(summary = "查询所有标签", description = "查询系统中的所有标签")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<TagListResponse> getAllTags() {
        
        List<TagAggregate> tags = tagApplicationService.findAllTags();
        List<TagResponse> tagResponses = tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        TagListResponse response = new TagListResponse(tagResponses, tagResponses.size());
        return Result.success(response);
    }
    
    /**
     * 分页查询标签
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页标签列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询标签", description = "分页查询标签列表")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<TagListResponse> getTagsWithPagination(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        

        List<TagAggregate> tags = tagApplicationService.findTags(page, size);
        long total = tagApplicationService.countTags();
        
        List<TagResponse> tagResponses = tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        TagListResponse response = new TagListResponse(tagResponses, (int) total);
        return Result.success(response);
    }
    
    /**
     * 搜索标签
     * 
     * @param keyword 搜索关键词
     * @return 匹配的标签列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索标签", description = "根据关键词搜索标签")
    @ApiResponse(responseCode = "200", description = "搜索成功")
    public Result<TagListResponse> searchTags(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword) {
        
        List<TagAggregate> tags = tagApplicationService.searchByName(keyword);
        List<TagResponse> tagResponses = tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        TagListResponse response = new TagListResponse(tagResponses, tagResponses.size());
        return Result.success(response);
    }
    
    /**
     * 查询最近创建的标签
     * 
     * @param limit 限制数量
     * @return 最近创建的标签列表
     */
    @GetMapping("/recent")
    @Operation(summary = "查询最近创建的标签", description = "查询最近创建的标签列表")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<TagListResponse> getRecentTags(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") int limit) {
        
        List<TagAggregate> tags = tagApplicationService.findRecentlyCreated(limit);
        List<TagResponse> tagResponses = tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        TagListResponse response = new TagListResponse(tagResponses, tagResponses.size());
        return Result.success(response);
    }
    
    /**
     * 统计标签总数
     * 
     * @return 标签总数
     */
    @GetMapping("/count")
    @Operation(summary = "统计标签总数", description = "获取系统中标签的总数量")
    @ApiResponse(responseCode = "200", description = "统计成功")
    public Result<Long> getTagCount() {
        
        long count = tagApplicationService.countTags();
        return Result.success(count);
    }
    
    /**
     * 分页查询标签及文章数量
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 带文章数量的分页标签列表
     */
    @GetMapping("/page-with-count")
    @Operation(summary = "分页查询标签及文章数量", description = "分页查询标签列表，包含每个标签的文章数量")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<TagWithCountListResponse> getTagsWithArticleCount(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        List<TagRepository.TagWithArticleCount> tagsWithCount = tagApplicationService.findTagsWithArticleCount(page, size);
        long total = tagApplicationService.countTags();
        
        List<TagWithCountResponse> tagResponses = tagsWithCount.stream()
                .map(tagWithCount -> TagWithCountResponse.from(tagWithCount.getTag(), tagWithCount.getArticleCount()))
                .collect(Collectors.toList());
        
        TagWithCountListResponse response = new TagWithCountListResponse(tagResponses, (int) total, page, size);
        return Result.success(response);
    }
}