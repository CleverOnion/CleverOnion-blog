package com.cleveronion.blog.application.tag.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FindOrCreateTagsCommand 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@DisplayName("FindOrCreateTagsCommand 单元测试")
class FindOrCreateTagsCommandTest {
    
    @Test
    @DisplayName("应该成功创建命令对象")
    void shouldCreateCommand() {
        // Given
        Set<String> names = Set.of("Java", "Python", "Go");
        
        // When
        FindOrCreateTagsCommand command = FindOrCreateTagsCommand.of(names);
        
        // Then
        assertNotNull(command);
        assertEquals(3, command.getNames().size());
    }
    
    @Test
    @DisplayName("应该过滤空字符串")
    void shouldFilterEmptyStrings() {
        // Given
        Set<String> names = Set.of("Java", "", "  ", "Python");
        
        // When
        FindOrCreateTagsCommand command = FindOrCreateTagsCommand.of(names);
        
        // Then
        assertEquals(2, command.getNames().size());
        assertTrue(command.getNames().contains("Java"));
        assertTrue(command.getNames().contains("Python"));
    }
    
    @Test
    @DisplayName("应该拒绝空集合")
    void shouldRejectEmptySet() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> FindOrCreateTagsCommand.of(Set.of())
        );
        
        assertEquals("标签名称集合不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝全为空字符串的集合")
    void shouldRejectAllEmptySet() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> FindOrCreateTagsCommand.of(Set.of("", "  "))
        );
        
        assertEquals("标签名称集合不能全为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝过长的标签名")
    void shouldRejectTooLongName() {
        // Given
        Set<String> names = Set.of("Java", "a".repeat(31));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> FindOrCreateTagsCommand.of(names)
        );
        
        assertTrue(exception.getMessage().contains("标签名称长度不能超过30个字符"));
    }
}

