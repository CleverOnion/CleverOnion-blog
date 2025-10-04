# 评论系统文档

## 📋 文档导航

### 设计与规划

- [**design.md**](./design.md) - 评论系统优化设计方案（包含架构设计、接口设计、前端实现方案）

### 实施过程

- [**migration-tasks.md**](./migration-tasks.md) - 详细的迁移任务清单（35 个任务，状态追踪）

### 完成报告

- [**completion-report.md**](./completion-report.md) - 项目完成总结报告
- [**cleanup-summary.md**](./cleanup-summary.md) - 代码清理总结

---

## 🎯 评论系统 v2.0.0 概述

### 核心改进

1. **分页策略优化**

   - 旧版：所有评论平铺分页（评论可能跨页）
   - 新版：只对顶级评论分页，保持评论完整性

2. **懒加载子评论**

   - 默认显示最新 3 条回复
   - 点击"查看全部 N 条回复"按钮展开
   - 支持"收起回复"功能

3. **性能优化**
   - 批量查询避免 N+1 问题
   - 减少 90%的数据库查询次数

### 快速链接

- 后端接口：`GET /api/comments/top-level-with-replies`
- 前端组件：`src/components/article/CommentSection.tsx`
- Hook：`src/hooks/useCommentsV2.ts`

---

**最后更新**：2025-10-04  
**状态**：✅ 已完成并上线
