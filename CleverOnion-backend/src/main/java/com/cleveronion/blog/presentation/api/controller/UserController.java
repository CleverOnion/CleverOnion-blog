package com.cleveronion.blog.presentation.api.controller;

import com.cleveronion.blog.application.user.service.UserApplicationService;
import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.presentation.api.dto.Result;
import com.cleveronion.blog.presentation.api.dto.UserResponse;
import com.cleveronion.blog.presentation.api.dto.UserListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户控制器
 * 提供用户管理相关的 RESTful API 接口
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "用户的查询等操作")
@Validated
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserApplicationService userApplicationService;
    
    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }
    
    /**
     * 分页获取用户列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 用户列表响应
     */
    @GetMapping
    @Operation(summary = "分页获取用户列表", description = "分页查询用户列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<UserListResponse> getUsers(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        
        logger.debug("接收到分页获取用户列表请求，页码: {}, 每页大小: {}", page, size);
        
        // 调用应用服务获取分页用户列表
        UserListResponse response = userApplicationService.getUsersWithPagination(page, size);
        
        logger.debug("成功获取用户列表，总数: {}, 当前页: {}, 每页大小: {}", 
            response.getTotalCount(), page, size);
        
        return Result.success("用户列表查询成功", response);
    }
    
    /**
     * 根据用户ID获取用户详情
     * 
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<UserResponse> getUserById(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long id) {
        
        logger.debug("接收到获取用户详情请求，用户ID: {}", id);
        
        UserId userId = UserId.of(id);
        Optional<UserAggregate> userOpt = userApplicationService.findById(userId);
        
        if (userOpt.isPresent()) {
            UserResponse response = UserResponse.from(userOpt.get());
            logger.debug("成功获取用户详情，用户ID: {}, 用户名: {}", id, response.getUsername());
            return Result.success("用户详情查询成功", response);
        } else {
            logger.warn("用户不存在，用户ID: {}", id);
            return Result.notFound("用户不存在");
        }
    }
    
    /**
     * 获取用户总数
     * 
     * @return 用户总数
     */
    @GetMapping("/count")
    @Operation(summary = "获取用户总数", description = "统计系统中用户的总数量")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "统计成功"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Long> countUsers() {
        
        logger.debug("接收到获取用户总数请求");
        
        long count = userApplicationService.countUsers();
        
        logger.debug("成功获取用户总数: {}", count);
        
        return Result.success("用户总数统计成功", count);
    }
}