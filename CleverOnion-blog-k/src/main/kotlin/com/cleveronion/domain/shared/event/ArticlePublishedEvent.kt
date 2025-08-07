package com.cleveronion.domain.shared.event

import java.time.Instant

/**
 * 文章发布事件
 * 
 * 当文章状态从草稿变为已发布时触发此事件
 */
data class ArticlePublishedEvent(
    val articleId: Long,
    val authorId: Long,
    val title: String,
    override val occurredOn: Instant = Instant.now(),
    override val aggregateId: String = articleId.toString()
) : DomainEvent