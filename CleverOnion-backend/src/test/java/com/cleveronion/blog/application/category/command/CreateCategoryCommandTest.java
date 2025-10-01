package com.cleveronion.blog.application.category.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CreateCategoryCommand 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@DisplayName("CreateCategoryCommand 单元测试")
class CreateCategoryCommandTest {
    
    @Test
    @DisplayName("应该成功创建命令对象 - 包含图标")
    void shouldCreateCommandWithIcon() {
        // Given
        String name = "技术分类";
        String icon = "tech-icon";
        
        // When
        CreateCategoryCommand command = CreateCategoryCommand.of(name, icon);
        
        // Then
        assertNotNull(command);
        assertEquals("技术分类", command.getName());
        assertEquals("tech-icon", command.getIcon());
    }
    
    @Test
    @DisplayName("应该成功创建命令对象 - 不包含图标")
    void shouldCreateCommandWithoutIcon() {
        // Given
        String name = "生活分类";
        
        // When
        CreateCategoryCommand command = CreateCategoryCommand.of(name, null);
        
        // Then
        assertNotNull(command);
        assertEquals("生活分类", command.getName());
        assertNull(command.getIcon());
    }
    
    @Test
    @DisplayName("应该自动去除名称前后空格")
    void shouldTrimName() {
        // Given
        String name = "  技术分类  ";
        
        // When
        CreateCategoryCommand command = CreateCategoryCommand.of(name, null);
        
        // Then
        assertEquals("技术分类", command.getName());
    }
    
    @Test
    @DisplayName("应该拒绝空名称")
    void shouldRejectNullName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CreateCategoryCommand.of(null, "icon")
        );
        
        assertEquals("分类名称不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝空字符串名称")
    void shouldRejectEmptyName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CreateCategoryCommand.of("   ", "icon")
        );
        
        assertEquals("分类名称不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝过长的名称")
    void shouldRejectTooLongName() {
        // Given
        String longName = "a".repeat(51);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CreateCategoryCommand.of(longName, null)
        );
        
        assertEquals("分类名称长度不能超过50个字符", exception.getMessage());
    }
    
    @Test
    @DisplayName("toString 应该包含所有字段")
    void shouldHaveProperToString() {
        // Given
        CreateCategoryCommand command = CreateCategoryCommand.of("技术", "icon");
        
        // When
        String result = command.toString();
        
        // Then
        assertTrue(result.contains("技术"));
        assertTrue(result.contains("icon"));
    }
}


