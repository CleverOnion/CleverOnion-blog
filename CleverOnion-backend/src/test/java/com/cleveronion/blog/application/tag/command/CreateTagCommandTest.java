package com.cleveronion.blog.application.tag.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CreateTagCommand 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@DisplayName("CreateTagCommand 单元测试")
class CreateTagCommandTest {
    
    @Test
    @DisplayName("应该成功创建命令对象")
    void shouldCreateCommand() {
        // Given
        String name = "Java";
        
        // When
        CreateTagCommand command = CreateTagCommand.of(name);
        
        // Then
        assertNotNull(command);
        assertEquals("Java", command.getName());
    }
    
    @Test
    @DisplayName("应该自动去除名称前后空格")
    void shouldTrimName() {
        // Given
        String name = "  Java  ";
        
        // When
        CreateTagCommand command = CreateTagCommand.of(name);
        
        // Then
        assertEquals("Java", command.getName());
    }
    
    @Test
    @DisplayName("应该拒绝空名称")
    void shouldRejectNullName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CreateTagCommand.of(null)
        );
        
        assertEquals("标签名称不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝空字符串名称")
    void shouldRejectEmptyName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CreateTagCommand.of("   ")
        );
        
        assertEquals("标签名称不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝过长的名称")
    void shouldRejectTooLongName() {
        // Given
        String longName = "a".repeat(31);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CreateTagCommand.of(longName)
        );
        
        assertEquals("标签名称长度不能超过30个字符", exception.getMessage());
    }
}

