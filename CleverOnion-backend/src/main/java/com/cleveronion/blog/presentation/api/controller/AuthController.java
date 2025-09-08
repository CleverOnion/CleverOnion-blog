package com.cleveronion.blog.presentation.api.controller;

import com.cleveronion.blog.application.auth.command.GitHubLoginCommand;
import com.cleveronion.blog.application.auth.dto.AuthResult;
import com.cleveronion.blog.application.auth.service.AuthApplicationService;
import com.cleveronion.blog.infrastructure.common.config.FrontendConfig;
import com.cleveronion.blog.presentation.api.dto.GitHubLoginRequest;
import com.cleveronion.blog.presentation.api.dto.LoginResponse;
import com.cleveronion.blog.presentation.api.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 认证控制器
 * 提供用户认证相关的 RESTful API 接口
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户认证相关接口")
@Validated
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthApplicationService authApplicationService;
    private final FrontendConfig frontendConfig;
    
    public AuthController(AuthApplicationService authApplicationService, FrontendConfig frontendConfig) {
        this.authApplicationService = authApplicationService;
        this.frontendConfig = frontendConfig;
    }
    

    
    /**
     * 获取GitHub OAuth2授权URL
     * 前端调用此接口获取GitHub授权URL，然后重定向用户到GitHub进行授权
     * 
     * @param state 状态参数，用于防止CSRF攻击
     * @return GitHub授权URL
     */
    @GetMapping("/login/github")
    @Operation(summary = "获取GitHub OAuth2授权URL", description = "获取GitHub OAuth2授权URL，用于重定向用户到GitHub进行授权")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> getGitHubAuthUrl(
            @Parameter(description = "状态参数，用于防止CSRF攻击")
            @RequestParam(required = false) String state) {
        
        logger.info("收到获取GitHub OAuth2授权URL请求，状态参数: {}", state);
        
        try {
            String authUrl = authApplicationService.generateGitHubAuthUrl(state);
            logger.info("成功生成GitHub OAuth2授权URL: {}", authUrl);
            return Result.success("获取授权URL成功", authUrl);
            
        } catch (Exception e) {
            logger.error("生成GitHub OAuth2授权URL过程中发生错误", e);
            return Result.error("获取授权URL失败，请稍后重试");
        }
    }
    
    /**
     * GitHub OAuth2 回调处理
     * 前端在GitHub授权后调用此接口，传入授权码进行登录
     * 
     * @param request GitHub 登录请求
     * @return 登录响应
     */
    @PostMapping("/callback/github")
    @Operation(summary = "GitHub OAuth2 回调处理", description = "前端调用此接口处理GitHub授权回调，使用授权码进行用户登录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "认证失败"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<LoginResponse> handleGitHubCallback(
            @Valid @RequestBody GitHubLoginRequest request) {
        
        logger.info("收到 GitHub OAuth2 回调处理请求: {}", request);
        
        try {
            // 转换为命令对象
            GitHubLoginCommand command = new GitHubLoginCommand(request.getCode(), request.getState());
            
            // 执行登录流程
            AuthResult authResult = authApplicationService.loginWithGitHub(command);
            
            // 转换为响应对象
            LoginResponse response = new LoginResponse(authResult);
            
            logger.info("GitHub OAuth2 登录成功，用户ID: {}", 
                authResult.getUserInfo() != null ? authResult.getUserInfo().getId() : "未知");
            
            return Result.success("登录成功", response);
            
        } catch (AuthApplicationService.AuthenticationException e) {
            logger.warn("GitHub OAuth2 登录失败: {}", e.getMessage());
            return Result.unauthorized(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("GitHub OAuth2 登录请求参数错误: {}", e.getMessage());
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("GitHub OAuth2 登录过程中发生未知错误", e);
            return Result.error("登录失败，请稍后重试");
        }
    }
    
    /**
     * 用户登出
     * 
     * @param userId 用户ID
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，清除认证状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登出成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Void> logout(
            @Parameter(description = "用户ID", required = true)
            @RequestParam @NotBlank(message = "用户ID不能为空") String userId) {
        
        logger.info("收到用户登出请求，用户ID: {}", userId);
        
        try {
            authApplicationService.logout(userId);
            logger.info("用户登出成功，用户ID: {}", userId);
            return Result.success("登出成功", null);
            
        } catch (IllegalArgumentException e) {
            logger.warn("用户登出请求参数错误: {}", e.getMessage());
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("用户登出过程中发生未知错误，用户ID: {}", userId, e);
            return Result.error("登出失败，请稍后重试");
        }
    }
    
    /**
     * 检查用户登录状态
     * 
     * @param userId 用户ID
     * @return 登录状态
     */
    @GetMapping("/status")
    @Operation(summary = "检查登录状态", description = "检查指定用户的登录状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "检查成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Boolean> checkLoginStatus(
            @Parameter(description = "用户ID", required = true)
            @RequestParam @NotBlank(message = "用户ID不能为空") String userId) {
        
        logger.debug("检查用户登录状态，用户ID: {}", userId);
        
        boolean isLogin = authApplicationService.isLogin(userId);
        logger.debug("用户登录状态检查完成，用户ID: {}, 登录状态: {}", userId, isLogin);
        return Result.success("状态检查成功", isLogin);
    }
    

}