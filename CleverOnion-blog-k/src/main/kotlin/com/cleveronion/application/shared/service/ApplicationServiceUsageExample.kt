package com.cleveronion.application.shared.service

import com.cleveronion.application.article.command.CreateArticleCommand
import com.cleveronion.application.article.command.PublishArticleCommand
import com.cleveronion.application.article.query.GetArticleQuery
import com.cleveronion.application.article.query.SearchArticlesQuery
import com.cleveronion.application.user.command.AuthenticateUserCommand
import com.cleveronion.application.comment.command.CreateCommentCommand
import com.cleveronion.application.upload.command.UploadImageCommand
import com.cleveronion.application.upload.service.ImageUploadInfo

/**
 * 应用服务使用示例
 * 
 * 展示如何使用应用服务层的各种功能
 * 这个类仅用于文档和示例目的，不会在实际运行时使用
 */
class ApplicationServiceUsageExample(
    private val applicationServiceFacade: ApplicationServiceFacade
) {
    
    /**
     * 文章管理示例
     */
    suspend fun articleManagementExample() {
        val articleService = applicationServiceFacade.articles()
        
        // 创建文章
        val createCommand = CreateArticleCommand(
            title = "DDD 六边形架构实践",
            contentMd = "# 介绍\n\n本文介绍如何实现DDD六边形架构...",
            authorId = 1L,
            tags = listOf("DDD", "架构", "Kotlin")
        )
        val createdArticle = articleService.createArticle(createCommand)
        
        // 发布文章
        val publishCommand = PublishArticleCommand(
            articleId = createdArticle.id,
            userId = 1L
        )
        val publishedArticle = articleService.publishArticle(publishCommand)
        
        // 获取文章
        val getQuery = GetArticleQuery(
            articleId = publishedArticle.id,
            includeUnpublished = false,
            requestUserId = null
        )
        val article = articleService.getArticle(getQuery)
        
        // 搜索文章
        val searchQuery = SearchArticlesQuery(
            keyword = "DDD",
            tags = listOf("架构"),
            page = 1,
            pageSize = 10
        )
        val searchResults = articleService.searchArticles(searchQuery)
        
        // 获取用户的文章
        val userArticles = articleService.getArticlesByAuthor(
            authorId = 1L,
            page = 1,
            pageSize = 20
        )
        
        // 获取已发布的文章
        val publishedArticles = articleService.getPublishedArticles(
            page = 1,
            pageSize = 20
        )
    }
    
    /**
     * 用户管理示例
     */
    suspend fun userManagementExample() {
        val userService = applicationServiceFacade.users()
        
        // GitHub OAuth 登录
        val authResult = userService.loginWithGitHub(
            githubId = 12345L,
            githubLogin = "johndoe",
            email = "john@example.com",
            name = "John Doe",
            bio = "Software Developer",
            avatarUrl = "https://github.com/johndoe.png"
        )
        
        // 更新用户基本信息
        val updatedUser = userService.updateBasicInfo(
            userId = authResult.user.id,
            name = "John Smith",
            bio = "Senior Software Developer",
            email = "johnsmith@example.com"
        )
        
        // 更新用户头像
        val userWithNewAvatar = userService.updateAvatar(
            userId = updatedUser.id,
            avatarUrl = "https://example.com/new-avatar.png"
        )
    }
    
    /**
     * 评论管理示例
     */
    suspend fun commentManagementExample() {
        val commentService = applicationServiceFacade.comments()
        
        // 为文章添加评论
        val comment = commentService.addCommentToArticle(
            articleId = 1L,
            userId = 1L,
            content = "这篇文章写得很好，学到了很多！"
        )
        
        // 回复评论
        val reply = commentService.replyToSpecificComment(
            parentCommentId = comment.id,
            articleId = 1L,
            userId = 2L,
            content = "我也觉得很有用，感谢分享！"
        )
        
        // 删除用户评论
        commentService.deleteUserComment(
            commentId = comment.id,
            userId = 1L
        )
    }
    
    /**
     * 文件上传示例
     */
    suspend fun fileUploadExample() {
        val uploadService = applicationServiceFacade.uploads()
        
        // 上传用户头像
        val avatarBytes = "fake-image-data".toByteArray()
        val avatarResult = uploadService.uploadUserAvatar(
            userId = 1L,
            fileName = "avatar.jpg",
            fileContent = avatarBytes,
            contentType = "image/jpeg"
        )
        
        // 上传文章图片
        val articleImageBytes = "fake-article-image-data".toByteArray()
        val articleImageResult = uploadService.uploadArticleImage(
            userId = 1L,
            fileName = "diagram.png",
            fileContent = articleImageBytes,
            contentType = "image/png"
        )
        
        // 批量上传图片
        val images: List<ImageUploadInfo> = listOf(
            ImageUploadInfo(
                fileName = "image1.jpg",
                fileContent = "image1-data".toByteArray(),
                contentType = "image/jpeg"
            ),
            ImageUploadInfo(
                fileName = "image2.png",
                fileContent = "image2-data".toByteArray(),
                contentType = "image/png"
            )
        )
        val batchResults = uploadService.uploadMultipleImages(
            userId = 1L,
            images = images
        )
        
        // 删除图片
        uploadService.deleteUserImage(
            userId = 1L,
            fileUrl = avatarResult.url
        )
    }
    
    /**
     * 综合业务流程示例
     */
    suspend fun comprehensiveWorkflowExample() {
        // 1. 用户登录
        val authResult = applicationServiceFacade.users().loginWithGitHub(
            githubId = 12345L,
            githubLogin = "author",
            email = "author@example.com",
            name = "Article Author"
        )
        
        // 2. 上传文章图片
        val imageBytes = "article-image-data".toByteArray()
        val imageResult = applicationServiceFacade.uploads().uploadArticleImage(
            userId = authResult.user.id,
            fileName = "article-cover.jpg",
            fileContent = imageBytes,
            contentType = "image/jpeg"
        )
        
        // 3. 创建文章（包含上传的图片）
        val articleContent = """
            # 我的新文章
            
            ![封面图片](${imageResult.url})
            
            这是文章内容...
        """.trimIndent()
        
        val article = applicationServiceFacade.articles().createArticle(
            CreateArticleCommand(
                title = "我的新文章",
                contentMd = articleContent,
                authorId = authResult.user.id,
                tags = listOf("技术", "分享")
            )
        )
        
        // 4. 发布文章
        val publishedArticle = applicationServiceFacade.articles().publishArticle(
            PublishArticleCommand(
                articleId = article.id,
                userId = authResult.user.id
            )
        )
        
        // 5. 其他用户评论
        val comment = applicationServiceFacade.comments().addCommentToArticle(
            articleId = publishedArticle.id,
            userId = 999L, // 其他用户ID
            content = "很棒的文章！"
        )
        
        // 6. 作者回复评论
        applicationServiceFacade.comments().replyToSpecificComment(
            parentCommentId = comment.id,
            articleId = publishedArticle.id,
            userId = authResult.user.id,
            content = "谢谢你的支持！"
        )
    }
}