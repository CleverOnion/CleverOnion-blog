package com.cleveronion.blog.presentation.api.controller;

import com.cleveronion.blog.application.article.service.ArticleApplicationService;
import com.cleveronion.blog.application.article.service.CategoryApplicationService;
import com.cleveronion.blog.application.article.service.TagApplicationService;
import com.cleveronion.blog.application.user.service.UserApplicationService;
import com.cleveronion.blog.domain.article.aggregate.ArticleAggregate;
import com.cleveronion.blog.domain.article.valueobject.ArticleId;
import com.cleveronion.blog.domain.article.valueobject.AuthorId;
import com.cleveronion.blog.domain.article.aggregate.CategoryAggregate;
import com.cleveronion.blog.domain.article.valueobject.CategoryId;
import com.cleveronion.blog.domain.article.aggregate.TagAggregate;
import com.cleveronion.blog.domain.article.valueobject.TagId;
import com.cleveronion.blog.domain.user.aggregate.UserAggregate;
import com.cleveronion.blog.domain.user.valueobject.UserId;
import com.cleveronion.blog.presentation.api.dto.ArticleListResponse;
import com.cleveronion.blog.presentation.api.dto.ArticleResponse;
import com.cleveronion.blog.presentation.api.dto.Result;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ArticleController批量查询优化测试
 * 验证N+1查询问题的解决效果
 */
@ExtendWith(MockitoExtension.class)
class ArticleControllerBatchTest {

    @Mock
    private ArticleApplicationService articleApplicationService;

    @Mock
    private CategoryApplicationService categoryApplicationService;

    @Mock
    private UserApplicationService userApplicationService;

    @Mock
    private TagApplicationService tagApplicationService;

    @InjectMocks
    private ArticleController articleController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(articleController).build();
    }

    /**
     * 测试批量查询优化 - 验证批量查询方法被调用
     */
    @Test
    void testGetArticles_WithBatchOptimization_ShouldUseBatchQueries() {
        // 准备测试数据
        List<ArticleAggregate> mockArticles = createMockArticles();
        
        // 收集所有需要查询的ID
        Set<CategoryId> categoryIds = Set.of(
            CategoryId.of(1L), 
            CategoryId.of(2L)
        );
        Set<UserId> userIds = Set.of(
            UserId.of(1L), 
            UserId.of(2L)
        );
        Set<TagId> tagIds = Set.of(
            TagId.of(1L), 
            TagId.of(2L), 
            TagId.of(3L)
        );
        
        // 准备批量查询结果
        List<CategoryAggregate> mockCategories = createMockCategories();
        List<UserAggregate> mockUsers = createMockUsers();
        List<TagAggregate> mockTags = createMockTags();
        
        // Mock 服务调用
        when(articleApplicationService.findAllArticles(0, 10)).thenReturn(mockArticles);
        when(articleApplicationService.countAllArticles()).thenReturn(2L);
        when(categoryApplicationService.findByIds(categoryIds)).thenReturn(mockCategories);
        when(userApplicationService.findByIds(userIds)).thenReturn(mockUsers);
        when(tagApplicationService.findByIds(tagIds)).thenReturn(mockTags);
        
        // 调用控制器方法
        Result<ArticleListResponse> result = articleController.getPublishedArticles(0, 10, null, null, null);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().getArticles().size());
        
        // 验证批量查询方法被调用（而不是单个查询）
        verify(categoryApplicationService, times(1)).findByIds(categoryIds);
        verify(userApplicationService, times(1)).findByIds(userIds);
        verify(tagApplicationService, times(1)).findByIds(tagIds);
        
        // 验证单个查询方法没有被调用
        verify(categoryApplicationService, never()).findById(any(CategoryId.class));
        verify(userApplicationService, never()).findById(any(UserId.class));
        
        // 验证响应包含完整的关联数据
        List<ArticleResponse> articles = result.getData().getArticles();
        for (ArticleResponse article : articles) {
            assertNotNull(article.getCategory(), "文章应该包含分类信息");
            assertNotNull(article.getAuthor(), "文章应该包含作者信息");
            assertNotNull(article.getTags(), "文章应该包含标签信息");
            assertFalse(article.getTags().isEmpty(), "文章标签不应为空");
        }
    }
    
    /**
     * 测试空文章列表的批量查询优化
     */
    @Test
    void testGetArticles_WithEmptyList_ShouldHandleGracefully() {
        // Mock 空文章列表
        when(articleApplicationService.findAllArticles(0, 10)).thenReturn(List.of());
        when(articleApplicationService.countAllArticles()).thenReturn(0L);
        
        // 调用控制器方法
        Result<ArticleListResponse> result = articleController.getPublishedArticles(0, 10, null, null, null);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getArticles().size());
        
        // 验证没有进行任何批量查询
        verify(categoryApplicationService, never()).findByIds(any());
        verify(userApplicationService, never()).findByIds(any());
        verify(tagApplicationService, never()).findByIds(any());
    }
    
    /**
     * 测试批量查询性能优化效果
     */
    @Test
    void testBatchQuery_ComparedToSingleQueries_ShouldBeMoreEfficient() {
        // 准备大量测试数据
        List<ArticleAggregate> mockArticles = createLargeMockArticles(100);
        
        // 收集所有唯一的ID
        Set<CategoryId> uniqueCategoryIds = new HashSet<>();
        Set<UserId> uniqueUserIds = new HashSet<>();
        Set<TagId> uniqueTagIds = new HashSet<>();
        
        for (ArticleAggregate article : mockArticles) {
            if (article.getCategoryId() != null) {
                uniqueCategoryIds.add(article.getCategoryId());
            }
            if (article.getAuthorId() != null) {
                uniqueUserIds.add(UserId.of(article.getAuthorId().getValue()));
            }
            if (article.getTagIds() != null) {
                uniqueTagIds.addAll(article.getTagIds());
            }
        }
        
        // 准备批量查询结果
        List<CategoryAggregate> mockCategories = createMockCategoriesForIds(uniqueCategoryIds);
        List<UserAggregate> mockUsers = createMockUsersForIds(uniqueUserIds);
        List<TagAggregate> mockTags = createMockTagsForIds(uniqueTagIds);
        
        // Mock 服务调用
        when(articleApplicationService.findAllArticles(0, 100)).thenReturn(mockArticles);
        when(articleApplicationService.countAllArticles()).thenReturn(100L);
        when(categoryApplicationService.findByIds(uniqueCategoryIds)).thenReturn(mockCategories);
        when(userApplicationService.findByIds(uniqueUserIds)).thenReturn(mockUsers);
        when(tagApplicationService.findByIds(uniqueTagIds)).thenReturn(mockTags);
        
        // 调用控制器方法
        Result<ArticleListResponse> result = articleController.getPublishedArticles(0, 100, null, null, null);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(100, result.getData().getArticles().size());
        
        // 验证批量查询只调用了一次（而不是100次单个查询）
        verify(categoryApplicationService, times(1)).findByIds(uniqueCategoryIds);
        verify(userApplicationService, times(1)).findByIds(uniqueUserIds);
        verify(tagApplicationService, times(1)).findByIds(uniqueTagIds);
        
        // 验证单个查询方法没有被调用
        verify(categoryApplicationService, never()).findById(any(CategoryId.class));
        verify(userApplicationService, never()).findById(any(UserId.class));
        
        System.out.println("批量查询优化效果：");
        System.out.println("- 文章数量: 100");
        System.out.println("- 唯一分类数量: " + uniqueCategoryIds.size());
        System.out.println("- 唯一用户数量: " + uniqueUserIds.size());
        System.out.println("- 唯一标签数量: " + uniqueTagIds.size());
        System.out.println("- 分类查询次数: 1 (优化前可能是 100 次)");
        System.out.println("- 用户查询次数: 1 (优化前可能是 100 次)");
        System.out.println("- 标签查询次数: 1 (优化前可能是 100 次)");
    }
    
    /**
     * 创建模拟文章数据
     */
    private List<ArticleAggregate> createMockArticles() {
        ArticleAggregate article1 = mock(ArticleAggregate.class);
        when(article1.getId()).thenReturn(ArticleId.of("1"));
        when(article1.getCategoryId()).thenReturn(CategoryId.of(1L));
        when(article1.getAuthorId()).thenReturn(AuthorId.of(1L));
        when(article1.getTagIds()).thenReturn(Set.of(TagId.of(1L), TagId.of(2L)));
        
        ArticleAggregate article2 = mock(ArticleAggregate.class);
        when(article2.getId()).thenReturn(ArticleId.of("2"));
        when(article2.getCategoryId()).thenReturn(CategoryId.of(2L));
        when(article2.getAuthorId()).thenReturn(AuthorId.of(2L));
        when(article2.getTagIds()).thenReturn(Set.of(TagId.of(2L), TagId.of(3L)));
        
        return List.of(article1, article2);
    }
    
    /**
     * 创建大量模拟文章数据
     */
    private List<ArticleAggregate> createLargeMockArticles(int count) {
        List<ArticleAggregate> articles = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            ArticleAggregate article = mock(ArticleAggregate.class);
            when(article.getId()).thenReturn(ArticleId.of(String.valueOf(i)));
            // 模拟有限的分类和用户（模拟真实场景中的数据分布）
            when(article.getCategoryId()).thenReturn(CategoryId.of((long)((i % 5) + 1)));
            when(article.getAuthorId()).thenReturn(AuthorId.of((long)((i % 10) + 1)));
            // 每篇文章有1-3个标签
            Set<TagId> tagIds = new HashSet<>();
            for (int j = 1; j <= (i % 3) + 1; j++) {
                tagIds.add(TagId.of((long)((i + j) % 20 + 1)));
            }
            when(article.getTagIds()).thenReturn(tagIds);
            
            articles.add(article);
        }
        
        return articles;
    }
    
    /**
     * 创建模拟分类数据
     */
    private List<CategoryAggregate> createMockCategories() {
        CategoryAggregate category1 = mock(CategoryAggregate.class);
        when(category1.getId()).thenReturn(CategoryId.of(1L));
        
        CategoryAggregate category2 = mock(CategoryAggregate.class);
        when(category2.getId()).thenReturn(CategoryId.of(2L));
        
        return List.of(category1, category2);
    }
    
    /**
     * 创建模拟用户数据
     */
    private List<UserAggregate> createMockUsers() {
        UserAggregate user1 = mock(UserAggregate.class);
        when(user1.getId()).thenReturn(UserId.of(1L));
        
        UserAggregate user2 = mock(UserAggregate.class);
        when(user2.getId()).thenReturn(UserId.of(2L));
        
        return List.of(user1, user2);
    }
    
    /**
     * 创建模拟标签数据
     */
    private List<TagAggregate> createMockTags() {
        TagAggregate tag1 = mock(TagAggregate.class);
        when(tag1.getId()).thenReturn(TagId.of(1L));
        
        TagAggregate tag2 = mock(TagAggregate.class);
        when(tag2.getId()).thenReturn(TagId.of(2L));
        
        TagAggregate tag3 = mock(TagAggregate.class);
        when(tag3.getId()).thenReturn(TagId.of(3L));
        
        return List.of(tag1, tag2, tag3);
    }
    
    /**
     * 根据ID集合创建模拟分类数据
     */
    private List<CategoryAggregate> createMockCategoriesForIds(Set<CategoryId> categoryIds) {
        return categoryIds.stream()
            .map(id -> {
                CategoryAggregate category = mock(CategoryAggregate.class);
                when(category.getId()).thenReturn(id);
                return category;
            })
            .toList();
    }
    
    /**
     * 根据ID集合创建模拟用户数据
     */
    private List<UserAggregate> createMockUsersForIds(Set<UserId> userIds) {
        return userIds.stream()
            .map(id -> {
                UserAggregate user = mock(UserAggregate.class);
                when(user.getId()).thenReturn(id);
                return user;
            })
            .toList();
    }
    
    /**
     * 根据ID集合创建模拟标签数据
     */
    private List<TagAggregate> createMockTagsForIds(Set<TagId> tagIds) {
        return tagIds.stream()
            .map(id -> {
                TagAggregate tag = mock(TagAggregate.class);
                when(tag.getId()).thenReturn(id);
                return tag;
            })
            .toList();
    }
}