package com.cleveronion.blog.application.category.command;

import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeleteCategoryCommand 单元测试
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
@DisplayName("DeleteCategoryCommand 单元测试")
class DeleteCategoryCommandTest {
    
    @Test
    @DisplayName("应该成功创建删除命令")
    void shouldCreateCommand() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        
        // When
        DeleteCategoryCommand command = DeleteCategoryCommand.of(categoryId);
        
        // Then
        assertNotNull(command);
        assertEquals(categoryId, command.getCategoryId());
    }
    
    @Test
    @DisplayName("应该拒绝空的分类ID")
    void shouldRejectNullCategoryId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DeleteCategoryCommand.of(null)
        );
        
        assertEquals("分类ID不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("toString 应该包含分类ID")
    void shouldHaveProperToString() {
        // Given
        CategoryId categoryId = CategoryId.of(123L);
        DeleteCategoryCommand command = DeleteCategoryCommand.of(categoryId);
        
        // When
        String result = command.toString();
        
        // Then
        assertTrue(result.contains("123"));
    }
}

