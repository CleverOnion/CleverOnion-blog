package com.cleveronion.blog.infrastructure.auth.client;

import com.cleveronion.blog.infrastructure.auth.client.dto.GitHubAccessTokenResponse;
import com.cleveronion.blog.infrastructure.auth.client.dto.GitHubUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * GitHub OAuth2 客户端
 * 负责与 GitHub OAuth2 API 进行交互
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@Component
public class GitHubOAuth2Client {
    
    private static final Logger logger = LoggerFactory.getLogger(GitHubOAuth2Client.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${github.oauth2.client-id}")
    private String clientId;
    
    @Value("${github.oauth2.client-secret}")
    private String clientSecret;
    
    @Value("${github.oauth2.access-token-url}")
    private String accessTokenUrl;
    
    @Value("${github.oauth2.user-info-url}")
    private String userInfoUrl;
    
    @Value("${github.oauth2.authorize-url}")
    private String authorizeUrl;
    
    @Value("${github.oauth2.redirect-uri}")
    private String redirectUri;

    public GitHubOAuth2Client(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * 生成GitHub OAuth2授权URL
     * 
     * @param state 状态参数，用于防止CSRF攻击
     * @return GitHub授权URL
     */
    public String generateAuthorizationUrl(String state) {
        logger.debug("生成GitHub OAuth2授权URL，状态参数: {}", state);
        
        StringBuilder urlBuilder = new StringBuilder(authorizeUrl);
        urlBuilder.append("?client_id=").append(clientId);
        urlBuilder.append("&redirect_uri=").append(redirectUri);
        urlBuilder.append("&scope=user:email");
        
        if (state != null && !state.trim().isEmpty()) {
            urlBuilder.append("&state=").append(state);
        }
        
        String authUrl = urlBuilder.toString();
        logger.debug("生成的GitHub OAuth2授权URL: {}", authUrl);
        
        return authUrl;
    }
    
    /**
     * 使用授权码获取访问令牌
     * 
     * @param code GitHub OAuth2 授权码
     * @return GitHub访问令牌响应
     * @throws GitHubOAuth2Exception 当获取访问令牌失败时抛出
     */
    public GitHubAccessTokenResponse getAccessToken(String code) {
        logger.debug("开始获取GitHub访问令牌，授权码: {}", code);
        
        try {
            // 构建请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("code", code);
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            // 发送请求
            ResponseEntity<GitHubAccessTokenResponse> response = restTemplate.postForEntity(
                accessTokenUrl, request, GitHubAccessTokenResponse.class);
            
            GitHubAccessTokenResponse tokenResponse = response.getBody();
            
            if (tokenResponse == null) {
                logger.error("GitHub访问令牌响应为空");
                throw new GitHubOAuth2Exception("GitHub访问令牌响应为空");
            }
            
            if (tokenResponse.hasError()) {
                logger.error("获取GitHub访问令牌失败: {}", tokenResponse.getFullErrorMessage());
                throw new GitHubOAuth2Exception("获取GitHub访问令牌失败: " + tokenResponse.getFullErrorMessage());
            }
            
            if (!tokenResponse.isSuccess()) {
                logger.error("GitHub访问令牌响应无效");
                throw new GitHubOAuth2Exception("GitHub访问令牌响应无效");
            }
            
            logger.debug("成功获取GitHub访问令牌");
            return tokenResponse;
            
        } catch (RestClientException e) {
            logger.error("调用GitHub访问令牌API失败", e);
            throw new GitHubOAuth2Exception("调用GitHub访问令牌API失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 使用访问令牌获取用户信息
     * 
     * @param accessToken GitHub访问令牌
     * @return GitHub用户信息
     * @throws GitHubOAuth2Exception 当获取用户信息失败时抛出
     */
    public GitHubUserInfo getUserInfo(String accessToken) {
        logger.debug("开始获取GitHub用户信息");
        
        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            // 发送请求
            ResponseEntity<GitHubUserInfo> response = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, request, GitHubUserInfo.class);
            
            GitHubUserInfo userInfo = response.getBody();
            
            if (userInfo == null) {
                logger.error("GitHub用户信息响应为空");
                throw new GitHubOAuth2Exception("GitHub用户信息响应为空");
            }
            
            if (userInfo.getId() == null) {
                logger.error("GitHub用户信息中缺少用户ID");
                throw new GitHubOAuth2Exception("GitHub用户信息中缺少用户ID");
            }
            
            logger.debug("成功获取GitHub用户信息，用户ID: {}, 用户名: {}", 
                userInfo.getId(), userInfo.getLogin());
            return userInfo;
            
        } catch (RestClientException e) {
            logger.error("调用GitHub用户信息API失败", e);
            throw new GitHubOAuth2Exception("调用GitHub用户信息API失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * GitHub OAuth2 异常
     */
    public static class GitHubOAuth2Exception extends RuntimeException {
        
        public GitHubOAuth2Exception(String message) {
            super(message);
        }
        
        public GitHubOAuth2Exception(String message, Throwable cause) {
            super(message, cause);
        }
    }
}