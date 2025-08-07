package com.cleveronion.domain.shared.event

import java.time.Instant

/**
 * 标签重命名事件
 * 
 * 当标签名称被修改时触发此事件
 */
data class TagRenamedEvent(
    val tagId: Long,
    val oldName: String,
    val newName: String,
    override val occurredOn: Instant = Instant.now(),
    override val aggregateId: String = tagId.toString()
) : DomainEvent