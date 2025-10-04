# 评论系统文档（前端）

## 📋 概述

评论系统 v2.0.0 - 支持懒加载子评论的高性能评论组件。

---

## 🎯 核心功能

### 1. 懒加载子评论

- 默认显示最新 3 条回复
- 点击"查看全部 N 条回复"展开
- 点击"收起回复"折叠

### 2. 回复目标显示

- 显示"回复 @用户名"
- 清晰的上下文关系

### 3. 完整的交互

- 发表评论/回复
- 删除评论（仅作者）
- 实时更新

---

## 📁 相关文件

### 组件

- `src/components/article/CommentSection.tsx` - 评论区组件

### Hook

- `src/hooks/useCommentsV2.ts` - 评论管理逻辑

### API

- `src/api/comments.ts` - 评论 API 接口

### 类型

- `src/types/comment.ts` - TypeScript 类型定义

---

## 🔧 使用示例

```tsx
import CommentSection from "@/components/article/CommentSection";

function Article() {
  return (
    <div>
      <CommentSection articleId="123" />
    </div>
  );
}
```

---

## 📚 后端文档

查看后端评论系统文档：`CleverOnion-backend/docs/03-features/comments/`

---

**最后更新**：2025-10-04
