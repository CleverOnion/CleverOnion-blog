package com.cleveronion.blog.application.user.command;

import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.infrastructure.auth.client.dto.GitHubUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SyncGitHubUserCommand 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@DisplayName("SyncGitHubUserCommand 单元测试")
class SyncGitHubUserCommandTest {
    
    @Test
    @DisplayName("应该从GitHubUserInfo创建命令对象")
    void shouldCreateFromGitHubInfo() {
        // Given
        GitHubUserInfo info = new GitHubUserInfo();
        info.setId(12345L);
        info.setLogin("testuser");
        info.setName("Test User");
        info.setAvatarUrl("https://avatar.com/test.jpg");
        
        // When
        SyncGitHubUserCommand command = SyncGitHubUserCommand.fromGitHubInfo(info);
        
        // Then
        assertNotNull(command);
        assertEquals(GitHubId.of(12345L), command.getGitHubId());
        assertEquals("Test User", command.getUsername());
        assertEquals("https://avatar.com/test.jpg", command.getAvatarUrl());
    }
    
    @Test
    @DisplayName("应该使用工厂方法创建命令对象")
    void shouldCreateWithFactoryMethod() {
        // Given
        GitHubId gitHubId = GitHubId.of(12345L);
        String username = "testuser";
        String avatarUrl = "https://avatar.com/test.jpg";
        
        // When
        SyncGitHubUserCommand command = SyncGitHubUserCommand.of(gitHubId, username, avatarUrl);
        
        // Then
        assertNotNull(command);
        assertEquals(gitHubId, command.getGitHubId());
        assertEquals("testuser", command.getUsername());
        assertEquals(avatarUrl, command.getAvatarUrl());
    }
    
    @Test
    @DisplayName("应该自动去除用户名前后空格")
    void shouldTrimUsername() {
        // Given
        GitHubId gitHubId = GitHubId.of(12345L);
        
        // When
        SyncGitHubUserCommand command = SyncGitHubUserCommand.of(gitHubId, "  testuser  ", null);
        
        // Then
        assertEquals("testuser", command.getUsername());
    }
    
    @Test
    @DisplayName("应该拒绝空的GitHub ID")
    void shouldRejectNullGitHubId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SyncGitHubUserCommand.of(null, "username", null)
        );
        
        assertEquals("GitHub用户ID不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝空的用户名")
    void shouldRejectNullUsername() {
        // Given
        GitHubId gitHubId = GitHubId.of(12345L);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SyncGitHubUserCommand.of(gitHubId, null, null)
        );
        
        assertEquals("用户名不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝空字符串用户名")
    void shouldRejectEmptyUsername() {
        // Given
        GitHubId gitHubId = GitHubId.of(12345L);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SyncGitHubUserCommand.of(gitHubId, "   ", null)
        );
        
        assertEquals("用户名不能为空", exception.getMessage());
    }
}

