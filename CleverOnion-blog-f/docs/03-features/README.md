# 功能模块文档

## 📂 模块列表

### [评论系统 (comments/)](./comments/)

- ✅ **状态**：v2.0.0 已完成
- 🎯 **核心功能**：懒加载子评论、回复目标显示、性能优化
- 📖 **组件**：`CommentSection.tsx`、`useCommentsV2` Hook

### [编辑器 (editor/)](./editor/)

- ✅ **状态**：已实现
- 🎯 **核心功能**：Milkdown 编辑器、未保存更改警告
- 📖 **文档**：
  - [编辑器优化](./editor/optimization.md)
  - [未保存更改警告](./editor/unsaved-changes-warning-implementation.md)

### [音效系统 (sound-system/)](./sound-system/)

- ✅ **状态**：已实现
- 🎯 **核心功能**：全局音效管理、可配置
- 📖 **文档**：[查看详情](./sound-system/README.md)

---

## 🔍 按功能查找

| 功能            | 模块         | 文档链接                    |
| --------------- | ------------ | --------------------------- |
| 懒加载评论      | comments     | [评论系统](./comments/)     |
| 回复目标显示    | comments     | [评论系统](./comments/)     |
| Milkdown 编辑器 | editor       | [编辑器](./editor/)         |
| 未保存警告      | editor       | [编辑器](./editor/)         |
| 音效管理        | sound-system | [音效系统](./sound-system/) |

---

**最后更新**：2025-10-04
