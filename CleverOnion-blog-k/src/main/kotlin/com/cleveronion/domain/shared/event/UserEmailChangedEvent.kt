package com.cleveronion.domain.shared.event

import java.time.Instant

/**
 * 用户邮箱变更事件
 * 
 * 当用户更新邮箱地址时触发此事件
 */
data class UserEmailChangedEvent(
    val userId: Long,
    val oldEmail: String?,
    val newEmail: String?,
    override val occurredOn: Instant = Instant.now(),
    override val aggregateId: String = userId.toString()
) : DomainEvent