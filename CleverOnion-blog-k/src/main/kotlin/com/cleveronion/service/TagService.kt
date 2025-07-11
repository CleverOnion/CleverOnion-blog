package com.cleveronion.service

import com.cleveronion.config.DatabaseConfig
import com.cleveronion.domain.entity.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * 标签服务类
 * 负责标签的业务逻辑处理，包括CRUD操作、标签统计等
 * 遵循单一职责原则，专注于标签相关的业务逻辑
 */
class TagService {
    
    /**
     * 创建新标签
     * @param request 创建标签请求
     * @return 创建的标签
     */
    fun createTag(request: CreateTagRequest): Tag {
        return transaction(DatabaseConfig.database) {
            require(request.name.isNotBlank()) { "标签名称不能为空" }
            require(request.name.length <= 50) { "标签名称不能超过50个字符" }
            
            val tagName = request.name.trim()
            
            // 检查标签是否已存在
            val existingTag = findByName(tagName)
            if (existingTag != null) {
                throw IllegalArgumentException("标签 '$tagName' 已存在")
            }
            
            val tagId = Tags.insertAndGetId {
                it[name] = tagName
            }
            
            Tag(
                id = tagId.value,
                name = tagName
            )
        }
    }
    
    /**
     * 根据ID查找标签
     * @param id 标签ID
     * @return 标签信息
     */
    fun findById(id: Long): Tag? {
        return transaction(DatabaseConfig.database) {
            Tags.select { Tags.id eq id }
                .map { rowToTag(it) }
                .singleOrNull()
        }
    }
    
    /**
     * 根据名称查找标签
     * @param name 标签名称
     * @return 标签信息
     */
    fun findByName(name: String): Tag? {
        return transaction(DatabaseConfig.database) {
            Tags.select { Tags.name eq name }
                .map { rowToTag(it) }
                .singleOrNull()
        }
    }
    
    /**
     * 获取所有标签
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 标签列表
     */
    fun getAllTags(page: Int = 1, pageSize: Int = 50): List<TagWithCount> {
        return transaction(DatabaseConfig.database) {
            val offset = (page - 1) * pageSize
            val articleCount = ArticleTags.articleId.count()
            
            // 使用Exposed DSL进行LEFT JOIN查询
            Tags
                .leftJoin(ArticleTags)
                .leftJoin(Articles, { ArticleTags.articleId }, { Articles.id })
                .slice(Tags.id, Tags.name, articleCount)
                .select { Articles.status.isNull() or (Articles.status eq ArticleStatus.PUBLISHED) }
                .groupBy(Tags.id, Tags.name)
                .orderBy(articleCount to SortOrder.DESC, Tags.name to SortOrder.ASC)
                .limit(pageSize, offset.toLong())
                .map { row ->
                    TagWithCount(
                        id = row[Tags.id].value,
                        name = row[Tags.name],
                        articleCount = row[articleCount].toInt()
                    )
                }
        }
    }
    
    /**
     * 获取标签总数
     * @return 标签总数
     */
    fun getTagCount(): Long {
        return transaction(DatabaseConfig.database) {
            Tags.selectAll().count()
        }
    }
    
    /**
     * 更新标签
     * @param id 标签ID
     * @param name 新的标签名称
     * @return 更新后的标签
     */
    fun updateTag(id: Long, newName: String): Tag? {
        return transaction(DatabaseConfig.database) {
            require(newName.isNotBlank()) { "标签名称不能为空" }
            require(newName.length <= 50) { "标签名称不能超过50个字符" }
            
            val trimmedName = newName.trim()
            
            // 检查新名称是否与其他标签冲突
            val existingTag = Tags.select { (Tags.name eq trimmedName) and (Tags.id neq id) }
                .singleOrNull()
            
            if (existingTag != null) {
                throw IllegalArgumentException("标签名称已存在")
            }
            
            // 更新标签
            val updateCount = Tags.update({ Tags.id eq id }) {
                it[name] = trimmedName
            }
            
            if (updateCount > 0) {
                findById(id)
            } else {
                null
            }
        }
    }
    
    /**
     * 删除标签
     * @param id 标签ID
     * @return 是否删除成功
     */
    fun deleteTag(id: Long): Boolean {
        return transaction(DatabaseConfig.database) {
            // 先删除文章-标签关联
            ArticleTags.deleteWhere { tagId eq id }
            
            // 删除标签
            val deletedCount = Tags.deleteWhere { Tags.id eq id }
            
            deletedCount > 0
        }
    }
    
    /**
     * 搜索标签
     * @param keyword 关键词
     * @param limit 返回数量限制
     * @return 匹配的标签列表
     */
    fun searchTags(keyword: String, limit: Int = 20): List<Tag> {
        return transaction(DatabaseConfig.database) {
            Tags.select { Tags.name like "%$keyword%" }
                .orderBy(Tags.name)
                .limit(limit)
                .map { rowToTag(it) }
        }
    }
    
    /**
     * 获取热门标签（按文章数量排序）
     * @param limit 返回数量限制
     * @return 热门标签列表
     */
    fun getPopularTags(limit: Int = 10): List<TagWithCount> {
        return transaction(DatabaseConfig.database) {
            val articleCount = ArticleTags.articleId.count()
            
            Tags
                .innerJoin(ArticleTags)
                .innerJoin(Articles)
                .slice(Tags.id, Tags.name, articleCount)
                .select { Articles.status eq ArticleStatus.PUBLISHED }
                .groupBy(Tags.id, Tags.name)
                .having { articleCount greater 0 }
                .orderBy(articleCount to SortOrder.DESC, Tags.name to SortOrder.ASC)
                .limit(limit)
                .map { row ->
                    TagWithCount(
                        id = row[Tags.id].value,
                        name = row[Tags.name],
                        articleCount = row[articleCount].toInt()
                    )
                }
        }
    }
    
    /**
     * 批量创建标签（如果不存在）
     * @param tagNames 标签名称列表
     * @return 创建或已存在的标签列表
     */
    fun createTagsIfNotExists(tagNames: List<String>): List<Tag> {
        return transaction(DatabaseConfig.database) {
            val validNames = tagNames
                .map { it.trim() }
                .filter { it.isNotBlank() && it.length <= 50 }
                .distinct()
            
            if (validNames.isEmpty()) {
                return@transaction emptyList()
            }
            
            // 查找已存在的标签
            val existingTags = Tags.select { Tags.name inList validNames }
                .map { rowToTag(it) }
            
            val existingNames = existingTags.map { it.name }.toSet()
            val newNames = validNames.filter { it !in existingNames }
            
            // 创建新标签
            val newTags = newNames.map { name ->
                val tagId = Tags.insertAndGetId {
                    it[Tags.name] = name
                }
                Tag(id = tagId.value, name = name)
            }
            
            existingTags + newTags
        }
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 将数据库行转换为Tag对象
     */
    private fun rowToTag(row: ResultRow): Tag {
        return Tag(
            id = row[Tags.id].value,
            name = row[Tags.name]
        )
    }
}

/**
 * 带文章数量的标签数据类
 */
@kotlinx.serialization.Serializable
data class TagWithCount(
    val id: Long,
    val name: String,
    val articleCount: Int
)