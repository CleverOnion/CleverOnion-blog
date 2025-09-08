package com.cleveronion.blog.infrastructure.user.persistence.repository;

import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.repository.UserRepository;
import com.cleveronion.blog.domain.user.valueobject.GitHubId;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.infrastructure.user.persistence.converter.UserConverter;
import com.cleveronion.blog.infrastructure.user.persistence.po.UserPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserRepositoryImpl单元测试类
 * 使用Mock测试用户仓储实现的基本功能
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {
    
    @Mock
    private UserJpaRepository userJpaRepository;
    
    @InjectMocks
    private UserRepositoryImpl userRepository;
    
    private UserAggregate testUserAggregate;
    private UserPO testUserPO;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        GitHubId gitHubId = GitHubId.of(12345678L);
        String username = "testuser";
        String avatarUrl = "https://avatars.githubusercontent.com/u/12345678?v=4";
        
        testUserAggregate = UserAggregate.createFromGitHub(gitHubId, username, avatarUrl);
        
        testUserPO = new UserPO();
        testUserPO.setId(1L);
        testUserPO.setGitHubId(12345678L);
        testUserPO.setUsername(username);
        testUserPO.setAvatarUrl(avatarUrl);
        testUserPO.setCreatedAt(LocalDateTime.now());
        testUserPO.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void testSaveNewUser() {
        // Given
        when(userJpaRepository.save(any(UserPO.class))).thenReturn(testUserPO);
        
        // When
        UserAggregate savedUser = userRepository.save(testUserAggregate);
        
        // Then
        assertNotNull(savedUser);
        assertEquals(testUserPO.getGitHubId(), savedUser.getGitHubId().getValue());
        assertEquals(testUserPO.getUsername(), savedUser.getUsername());
        assertEquals(testUserPO.getAvatarUrl(), savedUser.getAvatarUrl());
        
        verify(userJpaRepository, times(1)).save(any(UserPO.class));
    }
    
    @Test
    void testFindById() {
        // Given
        UserId userId = UserId.of(1L);
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(testUserPO));
        
        // When
        Optional<UserAggregate> result = userRepository.findById(userId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testUserPO.getUsername(), result.get().getUsername());
        
        verify(userJpaRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindByIdNotFound() {
        // Given
        UserId userId = UserId.of(999L);
        when(userJpaRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When
        Optional<UserAggregate> result = userRepository.findById(userId);
        
        // Then
        assertFalse(result.isPresent());
        
        verify(userJpaRepository, times(1)).findById(999L);
    }
    
    @Test
    void testFindByGitHubId() {
        // Given
        GitHubId gitHubId = GitHubId.of(12345678L);
        when(userJpaRepository.findByGitHubId(12345678L)).thenReturn(Optional.of(testUserPO));
        
        // When
        Optional<UserAggregate> result = userRepository.findByGitHubId(gitHubId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testUserPO.getUsername(), result.get().getUsername());
        
        verify(userJpaRepository, times(1)).findByGitHubId(12345678L);
    }
    
    @Test
    void testFindByUsername() {
        // Given
        String username = "testuser";
        when(userJpaRepository.findByUsername(username)).thenReturn(Optional.of(testUserPO));
        
        // When
        Optional<UserAggregate> result = userRepository.findByUsername(username);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testUserPO.getUsername(), result.get().getUsername());
        
        verify(userJpaRepository, times(1)).findByUsername(username);
    }
    
    @Test
    void testExistsById() {
        // Given
        UserId userId = UserId.of(1L);
        when(userJpaRepository.existsById(1L)).thenReturn(true);
        
        // When
        boolean exists = userRepository.existsById(userId);
        
        // Then
        assertTrue(exists);
        
        verify(userJpaRepository, times(1)).existsById(1L);
    }
    
    @Test
    void testExistsByGitHubId() {
        // Given
        GitHubId gitHubId = GitHubId.of(12345678L);
        when(userJpaRepository.existsByGitHubId(12345678L)).thenReturn(true);
        
        // When
        boolean exists = userRepository.existsByGitHubId(gitHubId);
        
        // Then
        assertTrue(exists);
        
        verify(userJpaRepository, times(1)).existsByGitHubId(12345678L);
    }
    
    @Test
    void testExistsByUsername() {
        // Given
        String username = "testuser";
        when(userJpaRepository.existsByUsername(username)).thenReturn(true);
        
        // When
        boolean exists = userRepository.existsByUsername(username);
        
        // Then
        assertTrue(exists);
        
        verify(userJpaRepository, times(1)).existsByUsername(username);
    }
    
    @Test
    void testDeleteById() {
        // Given
        UserId userId = UserId.of(1L);
        when(userJpaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userJpaRepository).deleteById(1L);
        
        // When
        boolean result = userRepository.deleteById(userId);
        
        // Then
        assertTrue(result);
        verify(userJpaRepository, times(1)).existsById(1L);
        verify(userJpaRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void testFindAll() {
        // Given
        UserPO userPO2 = new UserPO();
        userPO2.setId(2L);
        userPO2.setGitHubId(87654321L);
        userPO2.setUsername("testuser2");
        userPO2.setAvatarUrl("https://avatars.githubusercontent.com/u/87654321?v=4");
        userPO2.setCreatedAt(LocalDateTime.now());
        userPO2.setUpdatedAt(LocalDateTime.now());
        
        when(userJpaRepository.findAll()).thenReturn(Arrays.asList(testUserPO, userPO2));
        
        // When
        List<UserAggregate> result = userRepository.findAll();
        
        // Then
        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("testuser2", result.get(1).getUsername());
        
        verify(userJpaRepository, times(1)).findAll();
    }
    
    @Test
    void testCount() {
        // Given
        when(userJpaRepository.count()).thenReturn(5L);
        
        // When
        long count = userRepository.count();
        
        // Then
        assertEquals(5L, count);
        
        verify(userJpaRepository, times(1)).count();
    }
    
    @Test
    void testFindByUsernameContaining() {
        // Given
        String pattern = "test";
        when(userJpaRepository.findByUsernameContaining(pattern)).thenReturn(Arrays.asList(testUserPO));
        
        // When
        List<UserAggregate> result = userRepository.findByUsernameContaining(pattern);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        
        verify(userJpaRepository, times(1)).findByUsernameContaining(pattern);
    }
}