# BlockEdit 功能实现

## 概述

BlockEdit 是为 CleverOnion-blog-f 项目的 Milkdown 编辑器实现的块编辑功能，提供了拖拽移动块和快速添加新段落的能力。

## 功能特性

### 🎯 核心功能
- **拖拽移动**: 通过拖拽句柄可以重新排列文档中的块元素
- **快速添加**: 点击添加按钮可以在当前位置插入新段落
- **智能显示**: 鼠标悬停时自动显示操作句柄
- **节点过滤**: 可配置哪些类型的节点显示句柄

### 🎨 用户体验
- 平滑的动画过渡效果
- 直观的视觉反馈
- 响应式的交互设计
- 无干扰的界面集成

## 文件结构

```
src/components/editor/milkdown/BlockEdit/
├── index.ts              # 导出入口
├── types.ts              # TypeScript 类型定义
├── blockEdit.ts          # 核心插件逻辑
├── BlockHandleView.tsx   # React 组件实现
├── BlockEdit.css         # 样式文件
└── README.md            # 说明文档
```

## 技术实现

### 插件架构

1. **blockEdit.ts**: 核心插件工厂
   - 配置管理
   - 插件初始化
   - 上下文设置

2. **BlockHandleView.tsx**: React 组件
   - UI 渲染
   - 事件处理
   - 状态管理

3. **types.ts**: 类型定义
   - 配置接口
   - 组件属性
   - 数据结构

### 关键技术点

- 使用 `@milkdown/plugin-block` 作为基础
- 集成 `@prosemirror-adapter/react` 进行 React 组件渲染
- 利用 ProseMirror 的拖拽 API 实现块移动
- 通过 Milkdown 的命令系统添加新内容

## 配置选项

```typescript
interface BlockEditConfig {
  /** 是否启用拖拽功能 */
  enableDrag?: boolean;
  /** 是否显示添加按钮 */
  showAddButton?: boolean;
  /** 自定义过滤节点函数 */
  filterNodes?: (node: Node) => boolean;
  /** 自定义偏移量 */
  offset?: number;
}
```

### 默认配置

```typescript
const defaultConfig = {
  enableDrag: true,
  showAddButton: true,
  filterNodes: (node) => {
    // 过滤掉表格内部、数学公式和引用块内的节点
    const excludeTypes = ['table_cell', 'table_header', 'math_inline', 'math_block', 'blockquote'];
    return !excludeTypes.includes(node.type.name);
  },
  offset: 8
};
```

## 使用方法

### 基本使用

```typescript
import { blockEdit } from './editor/milkdown/BlockEdit';
import { BlockHandleView } from './editor/milkdown/BlockEdit';
import { blockSpec } from '@milkdown/plugin-block';

// 在 Milkdown 编辑器中配置
Editor.make()
  .config(blockEdit.config)
  .config(ctx => {
    ctx.set(blockSpec.key, {
      view: pluginViewFactory({
        component: BlockHandleView,
      })
    })
  })
  .use(blockEdit.plugins)
```

### 自定义配置

```typescript
import { blockEditFactory } from './editor/milkdown/BlockEdit';

const customBlockEdit = blockEditFactory({
  enableDrag: true,
  showAddButton: false,
  offset: 12,
  filterNodes: (node) => node.type.name !== 'code_block'
});
```

## 样式定制

可以通过修改 `BlockEdit.css` 文件来自定义样式：

```css
/* 自定义句柄容器样式 */
.block-handle-container {
  /* 你的样式 */
}

/* 自定义拖拽按钮样式 */
.drag-handle-button {
  /* 你的样式 */
}

/* 自定义添加按钮样式 */
.add-button {
  /* 你的样式 */
}
```

## 依赖项

- `@milkdown/plugin-block`: 块插件基础
- `@prosemirror-adapter/react`: React 适配器
- `react-icons`: 图标库
- `@milkdown/kit`: Milkdown 核心工具

## 注意事项

1. **性能优化**: 组件使用了 React.memo 和 useCallback 来优化性能
2. **事件处理**: 正确处理了拖拽事件以避免冲突
3. **样式隔离**: 使用了独立的 CSS 文件来避免样式污染
4. **类型安全**: 完整的 TypeScript 类型定义确保类型安全

## 扩展功能

未来可以考虑添加的功能：

- [ ] 斜杠命令集成
- [ ] 块类型选择器
- [ ] 键盘快捷键支持
- [ ] 更多块操作（复制、删除等）
- [ ] 自定义块模板

## 故障排除

### 常见问题

1. **句柄不显示**
   - 检查 CSS 文件是否正确导入
   - 确认 blockSpec 配置是否正确

2. **拖拽不工作**
   - 确认 enableDrag 配置为 true
   - 检查是否有其他事件监听器干扰

3. **添加按钮无响应**
   - 检查 Milkdown 实例是否正确初始化
   - 确认命令系统是否正常工作

## 更新日志

### v1.0.0 (2024-01-XX)
- ✨ 初始实现
- 🎯 基础拖拽功能
- ➕ 添加段落功能
- 🎨 响应式 UI 设计
- 📝 完整文档