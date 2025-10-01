package com.cleveronion.blog.application.user.service;

import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.repository.UserRepository;
import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.presentation.api.dto.UserListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserQueryService 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserQueryService 单元测试")
class UserQueryServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    private UserQueryService queryService;
    
    @BeforeEach
    void setUp() {
        queryService = new UserQueryService(userRepository);
    }
    
    @Test
    @DisplayName("应该根据ID查找用户")
    void shouldFindById() {
        // Given
        UserId userId = UserId.of(1L);
        UserAggregate user = mock(UserAggregate.class);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When
        Optional<UserAggregate> result = queryService.findById(userId);
        
        // Then
        assertTrue(result.isPresent());
        verify(userRepository).findById(userId);
    }
    
    @Test
    @DisplayName("ID为空时应该返回空Optional")
    void shouldReturnEmptyWhenIdIsNull() {
        // When
        Optional<UserAggregate> result = queryService.findById(null);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("应该根据GitHub ID查找用户")
    void shouldFindByGitHubId() {
        // Given
        GitHubId gitHubId = GitHubId.of(12345L);
        UserAggregate user = mock(UserAggregate.class);
        
        when(userRepository.findByGitHubId(gitHubId)).thenReturn(Optional.of(user));
        
        // When
        Optional<UserAggregate> result = queryService.findByGitHubId(gitHubId);
        
        // Then
        assertTrue(result.isPresent());
    }
    
    @Test
    @DisplayName("应该根据用户名查找用户")
    void shouldFindByUsername() {
        // Given
        UserAggregate user = mock(UserAggregate.class);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
        // When
        Optional<UserAggregate> result = queryService.findByUsername("testuser");
        
        // Then
        assertTrue(result.isPresent());
    }
    
    @Test
    @DisplayName("应该检查GitHub ID是否存在")
    void shouldCheckGitHubIdExists() {
        // Given
        GitHubId gitHubId = GitHubId.of(12345L);
        when(userRepository.existsByGitHubId(gitHubId)).thenReturn(true);
        
        // When
        boolean result = queryService.existsByGitHubId(gitHubId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    @DisplayName("应该检查用户名是否存在")
    void shouldCheckUsernameExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When
        boolean result = queryService.existsByUsername("testuser");
        
        // Then
        assertTrue(result);
    }
    
    @Test
    @DisplayName("应该分页查询用户")
    void shouldFindWithPagination() {
        // Given
        List<UserAggregate> users = List.of(mock(UserAggregate.class));
        when(userRepository.findAll(0, 10)).thenReturn(users);
        when(userRepository.count()).thenReturn(1L);
        
        // When
        UserListResponse result = queryService.findWithPagination(0, 10);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getTotalCount());
    }
    
    @Test
    @DisplayName("分页查询应该拒绝无效参数")
    void shouldRejectInvalidPaginationParams() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> queryService.findWithPagination(-1, 10));
        assertThrows(IllegalArgumentException.class, 
            () -> queryService.findWithPagination(0, 0));
        assertThrows(IllegalArgumentException.class, 
            () -> queryService.findWithPagination(0, 101)); // 超过最大值
    }
    
    @Test
    @DisplayName("应该统计用户总数")
    void shouldCountAll() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        
        // When
        long result = queryService.countAll();
        
        // Then
        assertEquals(100L, result);
    }
    
    @Test
    @DisplayName("应该批量查询用户")
    void shouldFindByIds() {
        // Given
        Set<UserId> userIds = Set.of(UserId.of(1L), UserId.of(2L));
        List<UserAggregate> users = List.of(mock(UserAggregate.class), mock(UserAggregate.class));
        
        when(userRepository.findByIds(userIds)).thenReturn(users);
        
        // When
        List<UserAggregate> result = queryService.findByIds(userIds);
        
        // Then
        assertEquals(2, result.size());
    }
    
    @Test
    @DisplayName("批量查询空集合时应该返回空列表")
    void shouldReturnEmptyListWhenIdsAreEmpty() {
        // When
        List<UserAggregate> result = queryService.findByIds(Set.of());
        
        // Then
        assertTrue(result.isEmpty());
    }
}

