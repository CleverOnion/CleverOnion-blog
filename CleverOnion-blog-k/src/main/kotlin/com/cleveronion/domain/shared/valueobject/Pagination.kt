package com.cleveronion.domain.shared.valueobject

/**
 * 分页值对象
 * 
 * 表示分页查询的参数，包含页码和每页大小
 */
data class Pagination(
    val page: Int,
    val pageSize: Int
) {
    init {
        require(page >= 1) { "Page number must be greater than 0" }
        require(pageSize >= 1) { "Page size must be greater than 0" }
        require(pageSize <= 100) { "Page size cannot exceed 100" }
    }
    
    companion object {
        /**
         * 默认分页参数
         */
        fun default(): Pagination = Pagination(1, 20)
        
        /**
         * 创建分页参数，使用默认值处理无效输入
         */
        fun of(page: Int?, pageSize: Int?): Pagination {
            val safePage = if (page != null && page >= 1) page else 1
            val safePageSize = when {
                pageSize == null -> 20
                pageSize < 1 -> 20
                pageSize > 100 -> 100
                else -> pageSize
            }
            return Pagination(safePage, safePageSize)
        }
    }
    
    /**
     * 计算偏移量（用于数据库查询）
     */
    fun getOffset(): Int = (page - 1) * pageSize
    
    /**
     * 计算限制数量（用于数据库查询）
     */
    fun getLimit(): Int = pageSize
    
    /**
     * 获取下一页
     */
    fun nextPage(): Pagination = Pagination(page + 1, pageSize)
    
    /**
     * 获取上一页
     */
    fun previousPage(): Pagination {
        return if (page > 1) Pagination(page - 1, pageSize) else this
    }
    
    /**
     * 检查是否为第一页
     */
    fun isFirstPage(): Boolean = page == 1
    
    /**
     * 计算总页数
     */
    fun getTotalPages(totalElements: Long): Int {
        return if (totalElements == 0L) 1 else ((totalElements - 1) / pageSize + 1).toInt()
    }
    
    /**
     * 检查是否为最后一页
     */
    fun isLastPage(totalElements: Long): Boolean = page >= getTotalPages(totalElements)
}