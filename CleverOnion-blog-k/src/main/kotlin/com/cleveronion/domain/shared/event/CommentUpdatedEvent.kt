package com.cleveronion.domain.shared.event

/**
 * 评论更新事件
 * 
 * 当评论内容被更新时发布此事件
 */
data class CommentUpdatedEvent(
    val commentId: Long,
    val articleId: Long,
    val userId: Long,
    override val occurredOn: java.time.Instant = java.time.Instant.now(),
    override val aggregateId: String = commentId.toString()
) : DomainEvent