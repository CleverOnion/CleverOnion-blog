#!/bin/bash

# 缓存修复验证脚本
# 用于测试分类缓存是否正常工作

echo "========================================="
echo "缓存修复验证脚本"
echo "========================================="
echo ""

# API 基础URL
API_URL="https://api.cleveronion.cn/api"

# 测试分类名称
TEST_CATEGORY_NAME="测试分类_$(date +%s)"

echo "步骤 1: 查询分类列表（建立缓存）"
echo "---------------------------------------"
echo "请求: GET ${API_URL}/categories"
CATEGORIES_BEFORE=$(curl -s -X GET "${API_URL}/categories")
echo "响应: ${CATEGORIES_BEFORE}"
echo ""

sleep 1

echo "步骤 2: 创建新分类"
echo "---------------------------------------"
echo "请求: POST ${API_URL}/categories"
echo "数据: {\"name\": \"${TEST_CATEGORY_NAME}\", \"icon\": \"test-icon\"}"
CREATE_RESPONSE=$(curl -s -X POST "${API_URL}/categories" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"${TEST_CATEGORY_NAME}\", \"icon\": \"test-icon\"}")
echo "响应: ${CREATE_RESPONSE}"
echo ""

# 提取新分类的ID（如果创建成功）
CATEGORY_ID=$(echo $CREATE_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

if [ -z "$CATEGORY_ID" ]; then
    echo "❌ 创建分类失败！"
    echo "可能原因："
    echo "1. 需要登录认证"
    echo "2. 权限不足"
    echo "3. 数据格式错误"
    exit 1
fi

echo "✅ 成功创建分类，ID: ${CATEGORY_ID}"
echo ""

sleep 1

echo "步骤 3: 再次查询分类列表（验证缓存已清除）"
echo "---------------------------------------"
echo "请求: GET ${API_URL}/categories"
CATEGORIES_AFTER=$(curl -s -X GET "${API_URL}/categories")
echo "响应: ${CATEGORIES_AFTER}"
echo ""

# 检查新分类是否出现在列表中
if echo "$CATEGORIES_AFTER" | grep -q "${TEST_CATEGORY_NAME}"; then
    echo "✅ 验证成功！"
    echo "   - 新分类 '${TEST_CATEGORY_NAME}' 出现在列表中"
    echo "   - 缓存已正确清除"
    echo ""
    
    # 清理：删除测试分类
    echo "步骤 4: 清理测试数据"
    echo "---------------------------------------"
    echo "请求: DELETE ${API_URL}/categories/${CATEGORY_ID}"
    DELETE_RESPONSE=$(curl -s -X DELETE "${API_URL}/categories/${CATEGORY_ID}")
    echo "响应: ${DELETE_RESPONSE}"
    echo ""
    
    if echo "$DELETE_RESPONSE" | grep -q "success"; then
        echo "✅ 测试数据已清理"
    else
        echo "⚠️ 清理测试数据失败，请手动删除分类 ID: ${CATEGORY_ID}"
    fi
else
    echo "❌ 验证失败！"
    echo "   - 新分类 '${TEST_CATEGORY_NAME}' 未出现在列表中"
    echo "   - 缓存可能未正确清除"
    echo ""
    echo "请检查："
    echo "1. 应用是否已重启"
    echo "2. Redis 缓存是否已清除"
    echo "3. 日志中是否有缓存清除的记录"
fi

echo ""
echo "========================================="
echo "验证完成"
echo "========================================="


