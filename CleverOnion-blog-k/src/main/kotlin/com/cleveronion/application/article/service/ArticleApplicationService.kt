package com.cleveronion.application.article.service

import com.cleveronion.application.article.command.CreateArticleCommand
import com.cleveronion.application.article.command.PublishArticleCommand
import com.cleveronion.application.article.command.UpdateArticleCommand
import com.cleveronion.application.article.command.DeleteArticleCommand
import com.cleveronion.application.article.query.GetArticleQuery
import com.cleveronion.application.article.query.SearchArticlesQuery
import com.cleveronion.application.article.dto.ArticleDto
import com.cleveronion.application.article.dto.ArticleListDto
import com.cleveronion.application.article.usecase.CreateArticleUseCase
import com.cleveronion.application.article.usecase.PublishArticleUseCase
import com.cleveronion.application.article.usecase.UpdateArticleUseCase
import com.cleveronion.application.article.usecase.DeleteArticleUseCase
import com.cleveronion.application.article.usecase.GetArticleUseCase
import com.cleveronion.application.article.usecase.SearchArticlesUseCase
import com.cleveronion.common.annotation.UseCase

/**
 * 文章应用服务
 * 
 * 协调文章相关的用例，提供统一的文章管理接口
 * 作为文章功能的门面，封装复杂的业务流程
 */
@UseCase
class ArticleApplicationService(
    private val createArticleUseCase: CreateArticleUseCase,
    private val publishArticleUseCase: PublishArticleUseCase,
    private val updateArticleUseCase: UpdateArticleUseCase,
    private val deleteArticleUseCase: DeleteArticleUseCase,
    private val getArticleUseCase: GetArticleUseCase,
    private val searchArticlesUseCase: SearchArticlesUseCase
) {
    
    /**
     * 创建文章
     * 
     * @param command 创建文章命令
     * @return 创建的文章DTO
     */
    suspend fun createArticle(command: CreateArticleCommand): ArticleDto {
        return createArticleUseCase.execute(command)
    }
    
    /**
     * 发布文章
     * 
     * @param command 发布文章命令
     * @return 发布的文章DTO
     */
    suspend fun publishArticle(command: PublishArticleCommand): ArticleDto {
        return publishArticleUseCase.execute(command)
    }
    
    /**
     * 更新文章
     * 
     * @param command 更新文章命令
     * @return 更新的文章DTO
     */
    suspend fun updateArticle(command: UpdateArticleCommand): ArticleDto {
        return updateArticleUseCase.execute(command)
    }
    
    /**
     * 删除文章
     * 
     * @param command 删除文章命令
     */
    suspend fun deleteArticle(command: DeleteArticleCommand) {
        deleteArticleUseCase.execute(command)
    }
    
    /**
     * 获取单篇文章
     * 
     * @param query 获取文章查询
     * @return 文章DTO
     */
    suspend fun getArticle(query: GetArticleQuery): ArticleDto {
        return getArticleUseCase.execute(query)
    }
    
    /**
     * 搜索文章
     * 
     * @param query 搜索文章查询
     * @return 文章列表DTO
     */
    suspend fun searchArticles(query: SearchArticlesQuery): ArticleListDto {
        return searchArticlesUseCase.execute(query)
    }
    
    /**
     * 获取用户的文章列表
     * 
     * 便捷方法，用于获取特定用户的文章
     * 
     * @param authorId 作者ID
     * @param page 页码
     * @param pageSize 页面大小
     * @param includeUnpublished 是否包含未发布文章
     * @return 文章列表DTO
     */
    suspend fun getArticlesByAuthor(
        authorId: Long,
        page: Int = 1,
        pageSize: Int = 20,
        includeUnpublished: Boolean = false
    ): ArticleListDto {
        val status = if (includeUnpublished) null else "PUBLISHED"
        val query = SearchArticlesQuery(
            authorId = authorId,
            status = status,
            page = page,
            pageSize = pageSize
        )
        return searchArticlesUseCase.execute(query)
    }
    
    /**
     * 按标签获取文章列表
     * 
     * 便捷方法，用于获取特定标签的文章
     * 
     * @param tags 标签列表
     * @param page 页码
     * @param pageSize 页面大小
     * @return 文章列表DTO
     */
    suspend fun getArticlesByTags(
        tags: List<String>,
        page: Int = 1,
        pageSize: Int = 20
    ): ArticleListDto {
        val query = SearchArticlesQuery(
            tags = tags,
            page = page,
            pageSize = pageSize
        )
        return searchArticlesUseCase.execute(query)
    }
    
    /**
     * 获取已发布的文章列表
     * 
     * 便捷方法，用于获取公开可见的文章
     * 
     * @param page 页码
     * @param pageSize 页面大小
     * @return 文章列表DTO
     */
    suspend fun getPublishedArticles(
        page: Int = 1,
        pageSize: Int = 20
    ): ArticleListDto {
        val query = SearchArticlesQuery(
            status = "PUBLISHED",
            page = page,
            pageSize = pageSize
        )
        return searchArticlesUseCase.execute(query)
    }
}