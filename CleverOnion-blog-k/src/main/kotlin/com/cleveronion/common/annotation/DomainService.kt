package com.cleveronion.common.annotation

/**
 * 领域服务注解
 * 
 * 标记领域层的服务类
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DomainService