package com.cleveronion.service

import com.cleveronion.config.DatabaseConfig
import com.cleveronion.domain.entity.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.intLiteral
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * 文章服务类
 * 负责文章的业务逻辑处理，包括CRUD操作、Markdown转换、标签管理等
 * 遵循单一职责原则，专注于文章相关的业务逻辑
 */
class ArticleService {
    
    private val markdownFlavour = CommonMarkFlavourDescriptor()
    
    /**
     * 创建新文章
     * @param authorId 作者ID
     * @param request 创建文章请求
     * @return 创建的文章
     */
    fun createArticle(authorId: Long, request: CreateArticleRequest): Article {
        return transaction(DatabaseConfig.database) {
            // 验证标题不为空
            require(request.title.isNotBlank()) { "文章标题不能为空" }
            require(request.contentMd.isNotBlank()) { "文章内容不能为空" }
            
            // 转换Markdown为HTML
            val contentHtml = convertMarkdownToHtml(request.contentMd)
            
            // 插入文章
            val articleId = Articles.insertAndGetId {
                it[Articles.authorId] = authorId
                it[title] = request.title.trim()
                it[contentMd] = request.contentMd
                it[Articles.contentHtml] = contentHtml
                it[status] = ArticleStatus.DRAFT
                it[viewCount] = 0
                it[createdAt] = Instant.now()
                it[updatedAt] = Instant.now()
            }
            
            // 关联标签
            if (request.tagIds.isNotEmpty()) {
                associateArticleTags(articleId.value, request.tagIds)
            }
            
            // 返回创建的文章
            findById(articleId.value)!!
        }
    }
    
    /**
     * 根据ID查找文章
     * @param id 文章ID
     * @return 文章信息，包含标签
     */
    fun findById(id: Long): Article? {
        return transaction(DatabaseConfig.database) {
            Articles.select { Articles.id eq id }
                .map { row ->
                    val article = rowToArticle(row)
                    val tags = getArticleTags(id)
                    article.copy(tags = tags)
                }
                .singleOrNull()
        }
    }
    
    /**
     * 更新文章
     * @param id 文章ID
     * @param authorId 作者ID（用于权限验证）
     * @param request 更新请求
     * @return 更新后的文章
     */
    fun updateArticle(id: Long, authorId: Long, request: UpdateArticleRequest): Article? {
        return transaction(DatabaseConfig.database) {
            // 验证文章存在且属于当前用户
            val existingArticle = Articles.select { 
                (Articles.id eq id) and (Articles.authorId eq authorId) 
            }.singleOrNull() ?: return@transaction null
            
            // 构建更新语句
            val updateCount = Articles.update({ Articles.id eq id }) { stmt ->
                request.title?.let { 
                    require(it.isNotBlank()) { "文章标题不能为空" }
                    stmt[title] = it.trim() 
                }
                request.contentMd?.let { 
                    require(it.isNotBlank()) { "文章内容不能为空" }
                    stmt[contentMd] = it
                    stmt[contentHtml] = convertMarkdownToHtml(it)
                }
                request.status?.let { 
                    val articleStatus = try {
                        ArticleStatus.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        throw IllegalArgumentException("无效的文章状态: $it")
                    }
                    stmt[status] = articleStatus
                }
                stmt[updatedAt] = Instant.now()
            }
            
            if (updateCount == 0) return@transaction null
            
            // 更新标签关联
            request.tagIds?.let { tagIds ->
                updateArticleTags(id, tagIds)
            }
            
            // 返回更新后的文章
            findById(id)
        }
    }
    
    /**
     * 删除文章
     * @param id 文章ID
     * @param authorId 作者ID（用于权限验证）
     * @return 是否删除成功
     */
    fun deleteArticle(id: Long, authorId: Long): Boolean {
        return transaction(DatabaseConfig.database) {
            // 先删除标签关联
            ArticleTags.deleteWhere { articleId eq id }
            
            // 删除文章（只能删除自己的文章）
            val deletedCount = Articles.deleteWhere { 
                (Articles.id eq id) and (Articles.authorId eq authorId) 
            }
            
            deletedCount > 0
        }
    }
    
    /**
     * 获取文章列表（分页）
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @param status 文章状态过滤
     * @param authorId 作者ID过滤
     * @return 文章列表
     */
    fun getArticles(
        page: Int = 1,
        pageSize: Int = 20,
        status: ArticleStatus? = null,
        authorId: Long? = null
    ): List<Article> {
        return transaction(DatabaseConfig.database) {
            val query = Articles.selectAll()
            
            // 添加状态过滤
            status?.let { query.andWhere { Articles.status eq it } }
            
            // 添加作者过滤
            authorId?.let { query.andWhere { Articles.authorId eq it } }
            
            // 分页和排序
            query.orderBy(Articles.createdAt, SortOrder.DESC)
                .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
                .map { row ->
                    val article = rowToArticle(row)
                    val tags = getArticleTags(article.id)
                    article.copy(tags = tags)
                }
        }
    }
    
    /**
     * 获取文章总数
     * @param status 状态过滤
     * @param authorId 作者过滤
     * @return 文章总数
     */
    fun getArticleCount(status: ArticleStatus? = null, authorId: Long? = null): Long {
        return transaction(DatabaseConfig.database) {
            val query = Articles.selectAll()
            
            status?.let { query.andWhere { Articles.status eq it } }
            authorId?.let { query.andWhere { Articles.authorId eq it } }
            
            query.count()
        }
    }
    
    /**
     * 增加文章浏览量
     * @param id 文章ID
     */
    fun incrementViewCount(id: Long) {
        transaction(DatabaseConfig.database) {
            Articles.update({ Articles.id eq id }) {
                it[Articles.viewCount] = Articles.viewCount + intLiteral(1)
            }
        }
    }
    
    /**
     * 搜索文章
     * @param keyword 关键词
     * @param page 页码
     * @param pageSize 每页大小
     * @return 搜索结果
     */
    fun searchArticles(keyword: String, page: Int = 1, pageSize: Int = 20): List<Article> {
        return transaction(DatabaseConfig.database) {
            Articles.select { 
                (Articles.title like "%$keyword%") or 
                (Articles.contentMd like "%$keyword%")
            }
            .andWhere { Articles.status eq ArticleStatus.PUBLISHED }
            .orderBy(Articles.createdAt, SortOrder.DESC)
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .map { row ->
                val article = rowToArticle(row)
                val tags = getArticleTags(article.id)
                article.copy(tags = tags)
            }
        }
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 将数据库行转换为Article对象
     */
    private fun rowToArticle(row: ResultRow): Article {
        return Article(
            id = row[Articles.id].value,
            authorId = row[Articles.authorId],
            title = row[Articles.title],
            contentMd = row[Articles.contentMd],
            contentHtml = row[Articles.contentHtml],
            status = row[Articles.status].name,
            viewCount = row[Articles.viewCount],
            createdAt = row[Articles.createdAt].toString(),
            updatedAt = row[Articles.updatedAt].toString()
        )
    }
    
    /**
     * 获取文章关联的标签
     */
    private fun getArticleTags(articleId: Long): List<Tag> {
        return (ArticleTags innerJoin Tags)
            .select { ArticleTags.articleId eq articleId }
            .map {
                Tag(
                    id = it[Tags.id].value,
                    name = it[Tags.name]
                )
            }
    }
    
    /**
     * 关联文章和标签
     */
    private fun associateArticleTags(articleId: Long, tagIds: List<Long>) {
        tagIds.forEach { tagId ->
            // 验证标签存在
            val tagExists = Tags.select { Tags.id eq tagId }.count() > 0
            if (tagExists) {
                ArticleTags.insertIgnore {
                    it[ArticleTags.articleId] = articleId
                    it[ArticleTags.tagId] = tagId
                }
            }
        }
    }
    
    /**
     * 更新文章标签关联
     */
    private fun updateArticleTags(articleId: Long, tagIds: List<Long>) {
        // 删除现有关联
        ArticleTags.deleteWhere { ArticleTags.articleId eq articleId }
        
        // 添加新关联
        if (tagIds.isNotEmpty()) {
            associateArticleTags(articleId, tagIds)
        }
    }
    
    /**
     * 将Markdown转换为HTML
     */
    private fun convertMarkdownToHtml(markdown: String): String {
        return try {
            val parsedTree = MarkdownParser(markdownFlavour).buildMarkdownTreeFromString(markdown)
            HtmlGenerator(markdown, parsedTree, markdownFlavour).generateHtml()
        } catch (e: Exception) {
            // 如果转换失败，返回原始内容
            markdown
        }
    }
}