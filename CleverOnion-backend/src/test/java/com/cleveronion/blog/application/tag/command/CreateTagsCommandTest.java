package com.cleveronion.blog.application.tag.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CreateTagsCommand 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@DisplayName("CreateTagsCommand 单元测试")
class CreateTagsCommandTest {
    
    @Test
    @DisplayName("应该成功创建批量命令")
    void shouldCreateCommand() {
        // Given
        List<String> names = List.of("Java", "Python", "Go");
        
        // When
        CreateTagsCommand command = CreateTagsCommand.of(names);
        
        // Then
        assertNotNull(command);
        assertEquals(3, command.getNames().size());
    }
    
    @Test
    @DisplayName("应该自动去重")
    void shouldDeduplicateNames() {
        // Given
        List<String> names = List.of("Java", "Java", "Python");
        
        // When
        CreateTagsCommand command = CreateTagsCommand.of(names);
        
        // Then
        assertEquals(2, command.getNames().size());
        assertTrue(command.getNames().contains("Java"));
        assertTrue(command.getNames().contains("Python"));
    }
    
    @Test
    @DisplayName("应该过滤空字符串")
    void shouldFilterEmptyStrings() {
        // Given
        List<String> names = List.of("Java", "", "  ", "Python");
        
        // When
        CreateTagsCommand command = CreateTagsCommand.of(names);
        
        // Then
        assertEquals(2, command.getNames().size());
    }
    
    @Test
    @DisplayName("应该拒绝空列表")
    void shouldRejectEmptyList() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CreateTagsCommand.of(List.of())
        );
        
        assertEquals("标签名称列表不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝全为空字符串的列表")
    void shouldRejectAllEmptyList() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CreateTagsCommand.of(List.of("", "  ", "   "))
        );
        
        assertEquals("标签名称列表不能全为空", exception.getMessage());
    }
}

