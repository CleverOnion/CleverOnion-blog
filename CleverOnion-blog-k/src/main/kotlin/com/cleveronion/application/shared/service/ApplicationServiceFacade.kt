package com.cleveronion.application.shared.service

import com.cleveronion.application.article.service.ArticleApplicationService
import com.cleveronion.application.user.service.UserApplicationService
import com.cleveronion.application.comment.service.CommentApplicationService
import com.cleveronion.application.upload.service.UploadApplicationService
import com.cleveronion.common.annotation.UseCase

/**
 * 应用服务门面
 * 
 * 提供统一的应用服务访问入口，简化外部层对应用层的调用
 * 封装所有应用服务，提供一致的接口
 */
@UseCase
class ApplicationServiceFacade(
    val articleService: ArticleApplicationService,
    val userService: UserApplicationService,
    val commentService: CommentApplicationService,
    val uploadService: UploadApplicationService
) {
    
    /**
     * 获取文章应用服务
     * 
     * @return 文章应用服务实例
     */
    fun articles(): ArticleApplicationService = articleService
    
    /**
     * 获取用户应用服务
     * 
     * @return 用户应用服务实例
     */
    fun users(): UserApplicationService = userService
    
    /**
     * 获取评论应用服务
     * 
     * @return 评论应用服务实例
     */
    fun comments(): CommentApplicationService = commentService
    
    /**
     * 获取文件上传应用服务
     * 
     * @return 文件上传应用服务实例
     */
    fun uploads(): UploadApplicationService = uploadService
}