package com.cleveronion.application.shared.config

import com.cleveronion.application.article.service.ArticleApplicationService
import com.cleveronion.application.article.usecase.*
import com.cleveronion.application.user.service.UserApplicationService
import com.cleveronion.application.user.usecase.*
import com.cleveronion.application.comment.service.CommentApplicationService
import com.cleveronion.application.comment.usecase.*
import com.cleveronion.application.upload.service.UploadApplicationService
import com.cleveronion.application.upload.usecase.*
import com.cleveronion.application.shared.service.ApplicationServiceFacade

/**
 * 应用服务配置
 * 
 * 配置应用层的所有服务和用例的依赖关系
 * 提供应用服务的创建和装配逻辑
 */
object ApplicationServiceConfig {
    
    /**
     * 创建文章应用服务
     * 
     * @param createArticleUseCase 创建文章用例
     * @param publishArticleUseCase 发布文章用例
     * @param updateArticleUseCase 更新文章用例
     * @param deleteArticleUseCase 删除文章用例
     * @param getArticleUseCase 获取文章用例
     * @param searchArticlesUseCase 搜索文章用例
     * @return 文章应用服务实例
     */
    fun createArticleApplicationService(
        createArticleUseCase: CreateArticleUseCase,
        publishArticleUseCase: PublishArticleUseCase,
        updateArticleUseCase: UpdateArticleUseCase,
        deleteArticleUseCase: DeleteArticleUseCase,
        getArticleUseCase: GetArticleUseCase,
        searchArticlesUseCase: SearchArticlesUseCase
    ): ArticleApplicationService {
        return ArticleApplicationService(
            createArticleUseCase = createArticleUseCase,
            publishArticleUseCase = publishArticleUseCase,
            updateArticleUseCase = updateArticleUseCase,
            deleteArticleUseCase = deleteArticleUseCase,
            getArticleUseCase = getArticleUseCase,
            searchArticlesUseCase = searchArticlesUseCase
        )
    }
    
    /**
     * 创建用户应用服务
     * 
     * @param authenticateUserUseCase 用户认证用例
     * @param registerUserUseCase 用户注册用例
     * @param updateUserProfileUseCase 更新用户档案用例
     * @return 用户应用服务实例
     */
    fun createUserApplicationService(
        authenticateUserUseCase: AuthenticateUserUseCase,
        registerUserUseCase: RegisterUserUseCase,
        updateUserProfileUseCase: UpdateUserProfileUseCase
    ): UserApplicationService {
        return UserApplicationService(
            authenticateUserUseCase = authenticateUserUseCase,
            registerUserUseCase = registerUserUseCase,
            updateUserProfileUseCase = updateUserProfileUseCase
        )
    }
    
    /**
     * 创建评论应用服务
     * 
     * @param createCommentUseCase 创建评论用例
     * @param replyToCommentUseCase 回复评论用例
     * @param deleteCommentUseCase 删除评论用例
     * @return 评论应用服务实例
     */
    fun createCommentApplicationService(
        createCommentUseCase: CreateCommentUseCase,
        replyToCommentUseCase: ReplyToCommentUseCase,
        deleteCommentUseCase: DeleteCommentUseCase
    ): CommentApplicationService {
        return CommentApplicationService(
            createCommentUseCase = createCommentUseCase,
            replyToCommentUseCase = replyToCommentUseCase,
            deleteCommentUseCase = deleteCommentUseCase
        )
    }
    
    /**
     * 创建文件上传应用服务
     * 
     * @param uploadImageUseCase 上传图片用例
     * @param deleteImageUseCase 删除图片用例
     * @return 文件上传应用服务实例
     */
    fun createUploadApplicationService(
        uploadImageUseCase: UploadImageUseCase,
        deleteImageUseCase: DeleteImageUseCase
    ): UploadApplicationService {
        return UploadApplicationService(
            uploadImageUseCase = uploadImageUseCase,
            deleteImageUseCase = deleteImageUseCase
        )
    }
    
    /**
     * 创建应用服务门面
     * 
     * @param articleService 文章应用服务
     * @param userService 用户应用服务
     * @param commentService 评论应用服务
     * @param uploadService 文件上传应用服务
     * @return 应用服务门面实例
     */
    fun createApplicationServiceFacade(
        articleService: ArticleApplicationService,
        userService: UserApplicationService,
        commentService: CommentApplicationService,
        uploadService: UploadApplicationService
    ): ApplicationServiceFacade {
        return ApplicationServiceFacade(
            articleService = articleService,
            userService = userService,
            commentService = commentService,
            uploadService = uploadService
        )
    }
}