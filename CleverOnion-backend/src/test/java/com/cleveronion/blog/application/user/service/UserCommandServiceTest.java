package com.cleveronion.blog.application.user.service;

import com.cleveronion.blog.application.user.command.SyncGitHubUserCommand;
import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.repository.UserRepository;
import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserCommandService 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserCommandService 单元测试")
class UserCommandServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    private UserCommandService commandService;
    
    @BeforeEach
    void setUp() {
        commandService = new UserCommandService(userRepository);
    }
    
    @Test
    @DisplayName("应该创建新的GitHub用户")
    void shouldCreateNewGitHubUser() {
        // Given
        GitHubId gitHubId = GitHubId.of(12345L);
        SyncGitHubUserCommand command = SyncGitHubUserCommand.of(gitHubId, "testuser", "https://avatar.com/test.jpg");
        
        UserAggregate savedUser = mock(UserAggregate.class);
        when(savedUser.getId()).thenReturn(UserId.of(1L));
        when(savedUser.getGitHubId()).thenReturn(gitHubId);
        when(savedUser.getUsername()).thenReturn("testuser");
        
        when(userRepository.findByGitHubId(gitHubId)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserAggregate.class))).thenReturn(savedUser);
        
        // When
        UserAggregate result = commandService.syncUserFromGitHub(command);
        
        // Then
        assertNotNull(result);
        verify(userRepository).findByGitHubId(gitHubId);
        verify(userRepository).save(any(UserAggregate.class));
    }
    
    @Test
    @DisplayName("应该更新已存在的GitHub用户")
    void shouldUpdateExistingGitHubUser() {
        // Given
        GitHubId gitHubId = GitHubId.of(12345L);
        SyncGitHubUserCommand command = SyncGitHubUserCommand.of(gitHubId, "newname", "https://new-avatar.com/test.jpg");
        
        UserAggregate existingUser = mock(UserAggregate.class);
        when(existingUser.getId()).thenReturn(UserId.of(1L));
        when(existingUser.getGitHubId()).thenReturn(gitHubId);
        when(existingUser.getUsername()).thenReturn("newname");
        
        when(userRepository.findByGitHubId(gitHubId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserAggregate.class))).thenReturn(existingUser);
        
        // When
        UserAggregate result = commandService.syncUserFromGitHub(command);
        
        // Then
        assertNotNull(result);
        verify(userRepository).findByGitHubId(gitHubId);
        verify(existingUser).updateProfile("newname", "https://new-avatar.com/test.jpg");
        verify(userRepository).save(existingUser);
    }
}

