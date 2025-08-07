package com.cleveronion.domain.article.valueobject

/**
 * 文章内容值对象
 * 
 * 包含Markdown原始内容和转换后的HTML内容
 */
data class ArticleContent(
    val markdown: String,
    val html: String
) {
    init {
        require(markdown.isNotBlank()) { "Markdown content cannot be blank" }
        require(markdown.length <= 50000) { "Markdown content cannot exceed 50000 characters" }
        require(html.isNotBlank()) { "HTML content cannot be blank" }
        require(html.length <= 100000) { "HTML content cannot exceed 100000 characters" }
    }
    
    companion object {
        /**
         * 从Markdown内容创建ArticleContent
         * 注意：这里简化了HTML转换逻辑，实际应使用专门的Markdown处理器
         */
        fun fromMarkdown(markdown: String): ArticleContent {
            val trimmedMarkdown = markdown.trim()
            require(trimmedMarkdown.isNotBlank()) { "Markdown content cannot be blank" }
            
            // 简化的HTML转换（实际应使用如CommonMark等库）
            val html = convertMarkdownToHtml(trimmedMarkdown)
            
            return ArticleContent(trimmedMarkdown, html)
        }
        
        /**
         * 创建空内容（用于草稿）
         */
        fun empty(): ArticleContent = ArticleContent("# 新文章", "<h1>新文章</h1>")
        
        /**
         * 最大长度常量
         */
        const val MAX_MARKDOWN_LENGTH = 50000
        const val MAX_HTML_LENGTH = 100000
        
        /**
         * 简化的Markdown到HTML转换
         * 实际项目中应使用专门的Markdown处理库
         */
        private fun convertMarkdownToHtml(markdown: String): String {
            return markdown
                .replace(Regex("^# (.+)$", RegexOption.MULTILINE), "<h1>$1</h1>")
                .replace(Regex("^## (.+)$", RegexOption.MULTILINE), "<h2>$1</h2>")
                .replace(Regex("^### (.+)$", RegexOption.MULTILINE), "<h3>$1</h3>")
                .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
                .replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
                .replace(Regex("\\[(.+?)\\]\\((.+?)\\)"), "<a href=\"$2\">$1</a>")
                .replace("\n\n", "</p><p>")
                .let { "<p>$it</p>" }
                .replace("<p></p>", "")
        }
    }
    
    /**
     * 获取Markdown内容长度
     */
    fun markdownLength(): Int = markdown.length
    
    /**
     * 获取HTML内容长度
     */
    fun htmlLength(): Int = html.length
    
    /**
     * 检查内容是否为空
     */
    fun isEmpty(): Boolean = markdown.isBlank()
    
    /**
     * 检查是否包含指定关键词
     */
    fun contains(keyword: String, ignoreCase: Boolean = true): Boolean {
        return markdown.contains(keyword, ignoreCase)
    }
    
    /**
     * 获取内容摘要（前200个字符）
     */
    fun getSummary(maxLength: Int = 200): String {
        val plainText = html
            .replace(Regex("<[^>]+>"), "") // 移除HTML标签
            .replace(Regex("\\s+"), " ") // 合并空白字符
            .trim()
        
        return if (plainText.length <= maxLength) {
            plainText
        } else {
            plainText.substring(0, maxLength) + "..."
        }
    }
}