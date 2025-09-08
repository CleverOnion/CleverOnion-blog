package com.cleveronion.blog.presentation.api.controller;

import com.cleveronion.blog.application.admin.service.AdminApplicationService;
import com.cleveronion.blog.presentation.api.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员控制器
 * 提供管理员相关的API接口
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "管理员管理", description = "管理员权限相关接口")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    private final AdminApplicationService adminApplicationService;
    
    public AdminController(AdminApplicationService adminApplicationService) {
        this.adminApplicationService = adminApplicationService;
    }
    
    /**
     * 检查当前用户是否为管理员
     * 
     * @return 管理员检查结果
     */
    @GetMapping("/check")
    @Operation(summary = "检查当前用户是否为管理员", description = "检查当前登录用户是否具有管理员权限")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "检查成功"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Boolean> checkCurrentUserAdmin() {
        logger.debug("开始检查当前用户是否为管理员");
        
        boolean isAdmin = adminApplicationService.isCurrentUserAdmin();
        
        logger.debug("当前用户管理员检查完成，结果: {}", isAdmin);
        
        return Result.success("管理员权限检查完成", isAdmin);
    }
}