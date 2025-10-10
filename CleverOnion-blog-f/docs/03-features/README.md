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

### [LaTeX 数学公式支持 (latex-support/)](./latex-support/) ⭐ 新增

- ✅ **状态**：v1.0.0 已完成
- 🎯 **核心功能**：编辑器实时预览、阅读页渲染、KaTeX 引擎
- 📖 **文档**：
  - [快速开始](./latex-support/QUICK_START.md) - 5 分钟上手
  - [完整功能手册](./latex-support/README.md)
  - [公式示例集](./latex-support/EXAMPLES.md)
  - [技术实现](./latex-support/IMPLEMENTATION.md)
  - [文档索引](./latex-support/INDEX.md)

---

## 🔍 按功能查找

| 功能            | 模块          | 文档链接                                      |
| --------------- | ------------- | --------------------------------------------- |
| 懒加载评论      | comments      | [评论系统](./comments/)                       |
| 回复目标显示    | comments      | [评论系统](./comments/)                       |
| Milkdown 编辑器 | editor        | [编辑器](./editor/)                           |
| 未保存警告      | editor        | [编辑器](./editor/)                           |
| 音效管理        | sound-system  | [音效系统](./sound-system/)                   |
| LaTeX 公式      | latex-support | [LaTeX 支持](./latex-support/) ⭐             |
| 数学公式编辑    | latex-support | [快速开始](./latex-support/QUICK_START.md)    |
| 公式实时预览    | latex-support | [技术实现](./latex-support/IMPLEMENTATION.md) |

---

**最后更新**：2025-10-10
