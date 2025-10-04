#!/bin/bash

# Redis 缓存清理脚本
# 清除所有 Response 缓存，确保应用重启后使用最新数据

echo "========================================="
echo "Redis 缓存清理脚本"
echo "========================================="
echo ""

# Redis 配置（根据实际情况修改）
REDIS_HOST="${REDIS_HOST:-localhost}"
REDIS_PORT="${REDIS_PORT:-6379}"
REDIS_PASSWORD="${REDIS_PASSWORD:-}"

echo "Redis 配置:"
echo "  主机: ${REDIS_HOST}"
echo "  端口: ${REDIS_PORT}"
echo ""

# 构建 redis-cli 命令
if [ -n "$REDIS_PASSWORD" ]; then
    REDIS_CMD="redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} -a ${REDIS_PASSWORD}"
else
    REDIS_CMD="redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT}"
fi

echo "选择清理模式："
echo "1. 清除所有缓存（FLUSHDB）- 最彻底但会影响所有缓存"
echo "2. 只清除 Response 缓存（推荐）- 只清除受影响的缓存"
echo "3. 查看当前缓存键"
echo "4. 取消"
echo ""
read -p "请选择 (1-4): " choice

case $choice in
    1)
        echo ""
        echo "⚠️  警告：这将清除 Redis 中的所有缓存数据！"
        read -p "确定要继续吗？(yes/no): " confirm
        if [ "$confirm" == "yes" ]; then
            echo ""
            echo "正在清除所有缓存..."
            $REDIS_CMD FLUSHDB
            echo "✅ 所有缓存已清除"
        else
            echo "❌ 操作已取消"
        fi
        ;;
        
    2)
        echo ""
        echo "正在清除 Response 缓存..."
        echo ""
        
        # Category 缓存
        echo "清除 Category 缓存..."
        $REDIS_CMD DEL category-responses 2>/dev/null
        $REDIS_CMD DEL category-list-responses 2>/dev/null
        $REDIS_CMD DEL category-page-responses 2>/dev/null
        
        # Tag 缓存
        echo "清除 Tag 缓存..."
        $REDIS_CMD DEL tag-responses 2>/dev/null
        $REDIS_CMD DEL tag-list-responses 2>/dev/null
        $REDIS_CMD DEL tag-page-responses 2>/dev/null
        
        # Article 缓存
        echo "清除 Article 缓存..."
        $REDIS_CMD DEL article-responses 2>/dev/null
        $REDIS_CMD DEL article-list-responses 2>/dev/null
        $REDIS_CMD DEL article-search-responses 2>/dev/null
        
        # Comment 缓存
        echo "清除 Comment 缓存..."
        $REDIS_CMD DEL comment-list-responses 2>/dev/null
        $REDIS_CMD DEL comment-top-responses 2>/dev/null
        
        # User 缓存
        echo "清除 User 缓存..."
        $REDIS_CMD DEL user-responses 2>/dev/null
        $REDIS_CMD DEL user-list-responses 2>/dev/null
        
        echo ""
        echo "✅ Response 缓存已清除"
        echo ""
        echo "已清除的缓存键："
        echo "  - category-responses, category-list-responses, category-page-responses"
        echo "  - tag-responses, tag-list-responses, tag-page-responses"
        echo "  - article-responses, article-list-responses, article-search-responses"
        echo "  - comment-list-responses, comment-top-responses"
        echo "  - user-responses, user-list-responses"
        ;;
        
    3)
        echo ""
        echo "当前缓存键列表："
        echo "---------------------------------------"
        echo ""
        echo "Category 缓存:"
        $REDIS_CMD KEYS "*category*"
        echo ""
        echo "Tag 缓存:"
        $REDIS_CMD KEYS "*tag*"
        echo ""
        echo "Article 缓存:"
        $REDIS_CMD KEYS "*article*"
        echo ""
        echo "Comment 缓存:"
        $REDIS_CMD KEYS "*comment*"
        echo ""
        echo "User 缓存:"
        $REDIS_CMD KEYS "*user*"
        ;;
        
    4)
        echo "❌ 操作已取消"
        ;;
        
    *)
        echo "❌ 无效的选择"
        ;;
esac

echo ""
echo "========================================="
echo "清理完成"
echo "========================================="


