# CleverOnion-blog-f Milkdown功能升级计划

## 概述

本文档详细说明了CleverOnion-blog-f项目中milkdown编辑器需要添加的crepe功能模块，以提升编辑器的功能性和用户体验。

## 当前状态分析

### 已实现功能
- ✅ **基础编辑**: commonmark、gfm预设
- ✅ **代码高亮**: prism插件（支持多种语言）
- ✅ **基础工具**: clipboard、history、cursor、indent
- ✅ **简单工具栏**: 粗体、斜体、删除线、行内代码
- ✅ **主题**: nord主题

### 缺失功能
共缺失8个主要crepe功能模块，需要逐步集成。

## 功能模块升级计划

### 优先级1：核心编辑体验 (立即实施)

#### 1. BlockEdit (块编辑)
**功能描述**:
- 拖拽块管理和重新排列
- 斜杠命令("/")快速插入内容
- 块级操作菜单

**技术实现**:
```typescript
import { blockEdit } from '@milkdown/crepe/feature/block-edit'

// 配置选项
interface BlockEditConfig {
  handleAddIcon: string
  handleDragIcon: string
  buildMenu: (builder: GroupBuilder<SlashMenuItem>) => void
  textGroup: { /* 文本相关菜单项 */ }
  listGroup: { /* 列表相关菜单项 */ }
  advancedGroup: { /* 高级功能菜单项 */ }
}
```

**实现优先级**: 🔴 高
**预估工作量**: 2-3天
**用户价值**: 极大提升内容组织效率

#### 2. Table (表格)
**功能描述**:
- 完整的表格创建和编辑
- 行/列的添加、删除、移动
- 单元格对齐选项
- 表格拖拽功能

**技术实现**:
```typescript
import { table } from '@milkdown/crepe/feature/table'

// 表格功能包含完整的UI组件
// 支持右键菜单、工具栏集成
```

**实现优先级**: 🔴 高
**预估工作量**: 3-4天
**用户价值**: 支持结构化数据展示

### 优先级2：增强用户体验 (第二阶段)

#### 3. ImageBlock (图片块)
**功能描述**:
- 图片上传和管理
- 图片大小调整
- 图片标题和描述
- 支持行内和块级图片

**技术实现**:
```typescript
import { imageBlock } from '@milkdown/crepe/feature/image-block'

// 需要配置图片上传处理器
interface ImageBlockConfig {
  uploadHandler: (file: File) => Promise<string>
  maxSize?: number
  allowedTypes?: string[]
}
```

**实现优先级**: 🟡 中
**预估工作量**: 2-3天
**用户价值**: 改善图片处理体验

#### 4. LinkTooltip (链接工具提示)
**功能描述**:
- 增强的链接编辑界面
- 链接预览功能
- 复制链接功能
- 编辑/删除链接操作

**技术实现**:
```typescript
import { linkTooltip } from '@milkdown/crepe/feature/link-tooltip'

// 提供丰富的链接交互体验
```

**实现优先级**: 🟡 中
**预估工作量**: 1-2天
**用户价值**: 提升链接操作便利性

#### 5. Placeholder (占位符)
**功能描述**:
- 文档级占位符
- 块级占位符
- 引导用户输入的提示文本

**技术实现**:
```typescript
import { placeholder } from '@milkdown/crepe/feature/placeholder'

// 配置不同类型的占位符文本
```

**实现优先级**: 🟡 中
**预估工作量**: 1天
**用户价值**: 改善新用户体验

### 优先级3：专业功能 (第三阶段)

#### 6. Latex (数学公式)
**功能描述**:
- 行内数学公式支持
- 块级数学公式支持
- KaTeX渲染引擎
- 公式编辑预览

**技术实现**:
```typescript
import { latex } from '@milkdown/crepe/feature/latex'

// 需要额外安装KaTeX依赖
// npm install katex @types/katex
```

**实现优先级**: 🟢 低
**预估工作量**: 2-3天
**用户价值**: 支持技术文档编写

#### 7. CodeMirror (增强代码编辑)
**功能描述**:
- 替换当前的prism代码高亮
- 代码块语言选择界面
- 主题切换功能
- 代码预览模式
- 更丰富的编程语言支持

**技术实现**:
```typescript
import { codeMirror } from '@milkdown/crepe/feature/code-mirror'

// 需要配置CodeMirror扩展和语言包
interface CodeMirrorConfig {
  extensions: Extension[]
  languages: LanguageDescription[]
  theme: Extension
}
```

**实现优先级**: 🟢 低
**预估工作量**: 2-3天
**用户价值**: 提升代码编辑体验

#### 8. ListItem (增强列表)
**功能描述**:
- 待办事项列表
- 自定义列表图标
- 嵌套列表支持
- 列表项拖拽排序

**技术实现**:
```typescript
import { listItem } from '@milkdown/crepe/feature/list-item'

// 增强现有的列表功能
```

**实现优先级**: 🟢 低
**预估工作量**: 1-2天
**用户价值**: 丰富列表功能

## 实施计划

### 第一阶段 (1-2周)
1. **BlockEdit**: 实现拖拽和斜杠命令
2. **Table**: 完整表格功能

### 第二阶段 (1周)
3. **ImageBlock**: 增强图片处理
4. **LinkTooltip**: 改善链接体验
5. **Placeholder**: 添加占位符

### 第三阶段 (1-2周)
6. **Latex**: 数学公式支持
7. **CodeMirror**: 替换代码高亮
8. **ListItem**: 增强列表功能

## 技术注意事项

### 依赖管理
- 确保@milkdown/crepe版本与其他milkdown包版本一致
- 可能需要额外安装KaTeX、CodeMirror相关依赖

### 样式集成
- 需要导入crepe的CSS样式文件
- 可能需要调整现有的tailwind样式

### 配置迁移
- 逐步替换现有的基础配置
- 保持向后兼容性

### 测试策略
- 每个功能模块独立测试
- 确保不影响现有功能
- 用户体验测试

## 预期收益

1. **编辑效率**: 拖拽、斜杠命令大幅提升内容组织效率
2. **功能完整性**: 支持表格、数学公式等专业内容
3. **用户体验**: 更直观的操作界面和反馈
4. **内容丰富度**: 支持更多类型的内容创作

## 风险评估

- **兼容性风险**: 新功能可能与现有代码冲突
- **性能影响**: 更多功能可能影响编辑器性能
- **学习成本**: 用户需要适应新的操作方式

## 总结

通过分阶段实施这8个功能模块，CleverOnion-blog-f的编辑器将从基础的markdown编辑器升级为功能完整的现代化富文本编辑器，显著提升内容创作体验。建议优先实施BlockEdit和Table功能，这两个功能将带来最直接的用户价值提升。