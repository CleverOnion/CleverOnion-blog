package com.cleveronion.blog.application.user.service;

import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.repository.UserRepository;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserApplicationService批量查询功能单元测试
 * 验证批量查询方法的正确性和性能优化效果
 * 
 * @author CleverOnion
 */
@ExtendWith(MockitoExtension.class)
class UserApplicationServiceBatchTest {
    
    static {
        // 设置lenient模式避免UnnecessaryStubbingException
        System.setProperty("mockito.strictness", "lenient");
    }

    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserApplicationService userApplicationService;
    
    @BeforeEach
    void setUp() {
        // 测试初始化
    }
    
    private UserAggregate createMockUser(String id, String username) {
        UserAggregate user = mock(UserAggregate.class);
        lenient().when(user.getId()).thenReturn(UserId.of(Long.parseLong(id)));
        lenient().when(user.getUsername()).thenReturn(username);
        return user;
    }
    
    @Test
    void testFindByIds_WithValidIds_ShouldReturnUsers() {
        // Given
        Set<UserId> userIds = Set.of(
            UserId.of(1L),
            UserId.of(2L),
            UserId.of(3L)
        );
        
        List<UserAggregate> mockUsers = Arrays.asList(
            createMockUser("1", "user1"),
            createMockUser("2", "user2"),
            createMockUser("3", "user3")
        );
        
        when(userRepository.findByIds(userIds)).thenReturn(mockUsers);
        
        // When
        List<UserAggregate> result = userApplicationService.findByIds(userIds);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // 验证仓储方法被调用
        verify(userRepository, times(1)).findByIds(userIds);
    }
    
    @Test
    void testFindByIds_WithEmptyIds_ShouldReturnEmptyList() {
        // Given
        Set<UserId> emptyIds = Collections.emptySet();
        
        // When
        List<UserAggregate> result = userApplicationService.findByIds(emptyIds);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // 验证仓储方法没有被调用
        verify(userRepository, never()).findByIds(any());
    }
    
    @Test
    void testFindByIds_WithNullIds_ShouldReturnEmptyList() {
        // Given
        Set<UserId> nullIds = null;
        
        // When
        List<UserAggregate> result = userApplicationService.findByIds(nullIds);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // 验证仓储方法没有被调用
        verify(userRepository, never()).findByIds(any());
    }
    
    @Test
    void testFindByIds_WithPartialResults_ShouldReturnAvailableUsers() {
        // Given - 请求3个用户，但只返回2个
        Set<UserId> userIds = Set.of(
            UserId.of(1L),
            UserId.of(2L),
            UserId.of(999L) // 不存在的用户
        );
        
        List<UserAggregate> partialUsers = Arrays.asList(
            createMockUser("1", "user1"),
            createMockUser("2", "user2")
        );
        
        when(userRepository.findByIds(userIds)).thenReturn(partialUsers);
        
        // When
        List<UserAggregate> result = userApplicationService.findByIds(userIds);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // 验证仓储方法被调用
        verify(userRepository, times(1)).findByIds(userIds);
    }
    
    @Test
    void testFindByIds_WithLargeDataSet_ShouldHandleEfficiently() {
        // Given - 大量用户ID
        Set<UserId> largeUserIds = new HashSet<>();
        List<UserAggregate> largeUserList = new ArrayList<>();
        
        for (int i = 1; i <= 1000; i++) {
            UserId userId = UserId.of((long) i);
            largeUserIds.add(userId);
            largeUserList.add(createMockUser(String.valueOf(i), "user" + i));
        }
        
        when(userRepository.findByIds(largeUserIds)).thenReturn(largeUserList);
        
        // When
        List<UserAggregate> result = userApplicationService.findByIds(largeUserIds);
        
        // Then
        assertNotNull(result);
        assertEquals(1000, result.size());
        
        // 验证仓储方法只被调用一次（批量查询的关键优势）
        verify(userRepository, times(1)).findByIds(largeUserIds);
    }
    
    @Test
    void testFindByIds_ComparedToSingleQueries_ShouldBeMoreEfficient() {
        // Given
        Set<UserId> userIds = Set.of(
            UserId.of(1L),
            UserId.of(2L),
            UserId.of(3L)
        );
        
        List<UserAggregate> mockUsers = Arrays.asList(
            createMockUser("1", "user1"),
            createMockUser("2", "user2"),
            createMockUser("3", "user3")
        );
        
        when(userRepository.findByIds(userIds)).thenReturn(mockUsers);
        
        // 模拟单个查询的返回
        when(userRepository.findById(UserId.of(1L))).thenReturn(Optional.of(mockUsers.get(0)));
        when(userRepository.findById(UserId.of(2L))).thenReturn(Optional.of(mockUsers.get(1)));
        when(userRepository.findById(UserId.of(3L))).thenReturn(Optional.of(mockUsers.get(2)));
        
        // When - 使用批量查询
        List<UserAggregate> batchResult = userApplicationService.findByIds(userIds);
        
        // 模拟使用单个查询的方式
        List<UserAggregate> singleResults = new ArrayList<>();
        for (UserId userId : userIds) {
            Optional<UserAggregate> user = userApplicationService.findById(userId);
            user.ifPresent(singleResults::add);
        }
        
        // Then
        assertEquals(batchResult.size(), singleResults.size());
        
        // 验证批量查询只调用一次仓储方法
        verify(userRepository, times(1)).findByIds(userIds);
        
        // 验证单个查询调用了多次仓储方法
        verify(userRepository, times(3)).findById(any(UserId.class));
        
        // 这证明了批量查询的性能优势：1次数据库调用 vs 3次数据库调用
    }
}