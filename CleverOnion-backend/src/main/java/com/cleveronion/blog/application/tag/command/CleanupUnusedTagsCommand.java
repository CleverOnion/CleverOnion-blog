package com.cleveronion.blog.application.tag.command;

/**
 * 清理未使用标签命令
 * 不可变对象，用于触发清理未使用标签的操作
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public final class CleanupUnusedTagsCommand {
    
    private CleanupUnusedTagsCommand() {
        // 私有构造函数
    }
    
    /**
     * 创建命令对象的工厂方法
     * 
     * @return 清理未使用标签命令
     */
    public static CleanupUnusedTagsCommand create() {
        return new CleanupUnusedTagsCommand();
    }
    
    @Override
    public String toString() {
        return "CleanupUnusedTagsCommand{}";
    }
}

