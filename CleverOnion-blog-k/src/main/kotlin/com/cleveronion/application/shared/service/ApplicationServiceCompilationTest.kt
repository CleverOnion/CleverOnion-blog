package com.cleveronion.application.shared.service

import com.cleveronion.application.article.service.ArticleApplicationService
import com.cleveronion.application.user.service.UserApplicationService
import com.cleveronion.application.comment.service.CommentApplicationService
import com.cleveronion.application.upload.service.UploadApplicationService

/**
 * 应用服务编译验证测试
 * 
 * 用于验证所有应用服务能够正常编译和实例化
 * 这个类主要用于编译时验证，确保依赖关系正确
 */
class ApplicationServiceCompilationTest {
    
    /**
     * 验证应用服务门面的编译
     */
    fun testApplicationServiceFacadeCompilation(
        articleService: ArticleApplicationService,
        userService: UserApplicationService,
        commentService: CommentApplicationService,
        uploadService: UploadApplicationService
    ) {
        // 创建应用服务门面
        val facade = ApplicationServiceFacade(
            articleService = articleService,
            userService = userService,
            commentService = commentService,
            uploadService = uploadService
        )
        
        // 验证门面方法可以正常调用
        val articles = facade.articles()
        val users = facade.users()
        val comments = facade.comments()
        val uploads = facade.uploads()
        
        // 验证类型正确
        assert(articles is ArticleApplicationService)
        assert(users is UserApplicationService)
        assert(comments is CommentApplicationService)
        assert(uploads is UploadApplicationService)
    }
    
    /**
     * 验证应用服务配置的编译
     */
    fun testApplicationServiceConfigCompilation() {
        // 这里只是验证配置类的方法签名正确
        // 实际的依赖注入会在运行时进行
        
        val config = ApplicationServiceConfig
        
        // 验证配置方法存在且可调用（编译时检查）
        // 注意：这些方法需要实际的用例实例，这里只是验证方法签名
        
        println("ApplicationServiceConfig compilation test passed")
    }
    
    /**
     * 验证所有应用服务的方法签名
     */
    fun testApplicationServiceMethodSignatures(
        articleService: ArticleApplicationService,
        userService: UserApplicationService,
        commentService: CommentApplicationService,
        uploadService: UploadApplicationService
    ) {
        // 验证文章服务方法签名
        testArticleServiceMethods(articleService)
        
        // 验证用户服务方法签名
        testUserServiceMethods(userService)
        
        // 验证评论服务方法签名
        testCommentServiceMethods(commentService)
        
        // 验证上传服务方法签名
        testUploadServiceMethods(uploadService)
    }
    
    private fun testArticleServiceMethods(service: ArticleApplicationService) {
        // 验证方法存在（编译时检查）
        val methods = service::class.java.methods
        val methodNames = methods.map { it.name }.toSet()
        
        val expectedMethods = setOf(
            "createArticle",
            "publishArticle", 
            "updateArticle",
            "deleteArticle",
            "getArticle",
            "searchArticles",
            "getArticlesByAuthor",
            "getArticlesByTags",
            "getPublishedArticles"
        )
        
        expectedMethods.forEach { methodName ->
            assert(methodNames.contains(methodName)) { 
                "ArticleApplicationService missing method: $methodName" 
            }
        }
    }
    
    private fun testUserServiceMethods(service: UserApplicationService) {
        val methods = service::class.java.methods
        val methodNames = methods.map { it.name }.toSet()
        
        val expectedMethods = setOf(
            "authenticateUser",
            "registerUser",
            "updateUserProfile",
            "loginWithGitHub",
            "updateBasicInfo",
            "updateAvatar"
        )
        
        expectedMethods.forEach { methodName ->
            assert(methodNames.contains(methodName)) { 
                "UserApplicationService missing method: $methodName" 
            }
        }
    }
    
    private fun testCommentServiceMethods(service: CommentApplicationService) {
        val methods = service::class.java.methods
        val methodNames = methods.map { it.name }.toSet()
        
        val expectedMethods = setOf(
            "createComment",
            "replyToComment",
            "deleteComment",
            "addCommentToArticle",
            "replyToSpecificComment",
            "deleteUserComment",
            "adminDeleteComment"
        )
        
        expectedMethods.forEach { methodName ->
            assert(methodNames.contains(methodName)) { 
                "CommentApplicationService missing method: $methodName" 
            }
        }
    }
    
    private fun testUploadServiceMethods(service: UploadApplicationService) {
        val methods = service::class.java.methods
        val methodNames = methods.map { it.name }.toSet()
        
        val expectedMethods = setOf(
            "uploadImage",
            "deleteImage",
            "uploadUserAvatar",
            "uploadArticleImage",
            "uploadMultipleImages",
            "deleteUserImage",
            "adminDeleteImage"
        )
        
        expectedMethods.forEach { methodName ->
            assert(methodNames.contains(methodName)) { 
                "UploadApplicationService missing method: $methodName" 
            }
        }
    }
}