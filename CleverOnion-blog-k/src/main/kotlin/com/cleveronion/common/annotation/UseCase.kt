package com.cleveronion.common.annotation

/**
 * 用例注解
 * 
 * 标记应用层的用例类，用于依赖注入和组件扫描
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class UseCase