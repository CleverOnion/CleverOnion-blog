package com.cleveronion.blog.application.category.command;

import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UpdateCategoryCommand 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@DisplayName("UpdateCategoryCommand 单元测试")
class UpdateCategoryCommandTest {
    
    @Test
    @DisplayName("应该成功创建更新命令")
    void shouldCreateCommand() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        String name = "新名称";
        String icon = "new-icon";
        
        // When
        UpdateCategoryCommand command = UpdateCategoryCommand.of(categoryId, name, icon);
        
        // Then
        assertNotNull(command);
        assertEquals(categoryId, command.getCategoryId());
        assertEquals("新名称", command.getName());
        assertEquals("new-icon", command.getIcon());
    }
    
    @Test
    @DisplayName("应该自动去除名称前后空格")
    void shouldTrimName() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        String name = "  新名称  ";
        
        // When
        UpdateCategoryCommand command = UpdateCategoryCommand.of(categoryId, name, null);
        
        // Then
        assertEquals("新名称", command.getName());
    }
    
    @Test
    @DisplayName("应该拒绝空的分类ID")
    void shouldRejectNullCategoryId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UpdateCategoryCommand.of(null, "名称", "icon")
        );
        
        assertEquals("分类ID不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝空名称")
    void shouldRejectNullName() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UpdateCategoryCommand.of(categoryId, null, "icon")
        );
        
        assertEquals("分类名称不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝空字符串名称")
    void shouldRejectEmptyName() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UpdateCategoryCommand.of(categoryId, "   ", "icon")
        );
        
        assertEquals("分类名称不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("应该拒绝过长的名称")
    void shouldRejectTooLongName() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        String longName = "a".repeat(51);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> UpdateCategoryCommand.of(categoryId, longName, null)
        );
        
        assertEquals("分类名称长度不能超过50个字符", exception.getMessage());
    }
    
    @Test
    @DisplayName("toString 应该包含所有字段")
    void shouldHaveProperToString() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        UpdateCategoryCommand command = UpdateCategoryCommand.of(categoryId, "技术", "icon");
        
        // When
        String result = command.toString();
        
        // Then
        assertTrue(result.contains("123"));
        assertTrue(result.contains("技术"));
        assertTrue(result.contains("icon"));
    }
}

