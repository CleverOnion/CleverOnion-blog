# 未保存更改警告实现文档

## ✅ 实现完成

已成功实现文章编辑器的未保存更改警告功能，完全符合 Web Interface Guidelines 要求。

## 📁 新增文件

### 1. **核心 Hook**

- `src/hooks/useUnsavedChanges.ts` - 未保存更改检测 Hook
  - 监听页面刷新/关闭事件（beforeunload）
  - 拦截路由切换（react-router-dom useBlocker）
  - 优雅的状态管理

### 2. **UI 组件**

- `src/components/ui/UnsavedChangesIndicator.tsx` - 状态指示器
  - 工具栏上的未保存状态提示
  - 脉冲动画效果
  - ARIA 实时区域支持

**注意：** 未保存更改确认对话框使用项目统一的 `Modal` 组件（`src/components/ui/Modal.tsx`），而不是创建独立的对话框，保持了整个项目的 UI 一致性和代码复用。

## 🔄 更新的文件

### 1. **EditorToolbar.tsx**

- 添加 `hasUnsavedChanges` 属性
- 集成 `UnsavedChangesIndicator` 组件
- 在工具栏显示未保存状态

### 2. **ArticleEditor.tsx**

- 集成 `useUnsavedChanges` Hook
- 实现脏数据检测逻辑
- 保存原始文章数据用于比对
- 保存成功后更新原始数据
- 添加路由拦截对话框

## 🎯 实现的功能

### ✅ 脏数据检测

使用 `useMemo` 优化的实时检测：

```typescript
const hasUnsavedChanges = useMemo(() => {
  if (!originalArticle) return false;

  return (
    article.title !== originalArticle.title ||
    article.content !== originalArticle.content ||
    article.summary !== originalArticle.summary ||
    article.category_id !== originalArticle.category_id ||
    JSON.stringify(article.tag_names) !==
      JSON.stringify(originalArticle.tag_names)
  );
}, [article, originalArticle]);
```

**检测的字段：**

- ✅ 标题变化
- ✅ 内容变化
- ✅ 摘要变化
- ✅ 分类变化
- ✅ 标签变化

### ✅ 双重保护机制

#### 1. **页面刷新/关闭保护**

- 监听 `beforeunload` 事件
- 浏览器原生确认对话框
- 防止意外关闭标签页/窗口

#### 2. **路由切换保护**

- 使用 `react-router-dom` 的 `useBlocker`
- 自定义确认对话框
- 优雅的用户体验

### ✅ 状态指示器

- 🟡 **未保存状态**：工具栏显示黄色脉冲圆点
- ✅ **保存后清除**：保存成功后自动更新原始数据
- 🎯 **实时更新**：`useMemo` 确保性能优化

### ✅ 用户体验优化

1. **工具栏指示器**

   - 黄色圆点 + "未保存" 文字
   - 脉冲动画吸引注意
   - 不干扰编辑流程

2. **优雅的确认对话框**

   - 清晰的警告图标
   - 明确的提示文字
   - 两个选项：继续编辑 / 离开
   - 平滑的进入/退出动画

3. **智能提示时机**
   - 只在真正有更改时提示
   - 保存成功后自动清除状态
   - 新文章初始化时不提示

## 📐 代码质量

### ✅ TypeScript 类型安全

- 完整的类型定义
- 类型推导
- 无 `any` 类型

### ✅ 性能优化

- **useMemo** 缓存脏数据检测结果
- **useCallback** 优化事件处理函数
- **useRef** 避免闭包陷阱

### ✅ 代码组织

- 清晰的职责分离
- 可复用的 Hook
- 模块化的组件
- DRY 原则

## 🧪 测试场景

### 页面刷新/关闭

- [x] 修改标题后刷新页面 → 显示浏览器确认
- [x] 修改内容后关闭标签页 → 显示浏览器确认
- [x] 修改分类后刷新页面 → 显示浏览器确认
- [x] 保存后刷新页面 → 不显示确认
- [x] 未修改时刷新页面 → 不显示确认

### 路由切换

- [x] 修改标题后点击返回 → 显示自定义对话框
- [x] 修改内容后切换路由 → 显示自定义对话框
- [x] 点击"继续编辑" → 取消导航
- [x] 点击"离开" → 确认导航
- [x] 按 Escape 键 → 取消导航
- [x] 保存后切换路由 → 不显示对话框

### 状态指示器

- [x] 修改任意字段 → 显示未保存指示器
- [x] 保存草稿 → 指示器消失
- [x] 发布文章 → 指示器消失
- [x] 更新文章 → 指示器消失

### 无障碍访问

- [x] 对话框有正确的 ARIA 角色
- [x] 指示器有 `role="status"` 和 `aria-live`
- [x] 键盘可以操作对话框
- [x] 焦点管理正确

## 📊 符合的 Web Guidelines

### ✅ State & Navigation

- ✅ **MUST: Warn on unsaved changes before navigation**
- ✅ Prevent data loss on refresh/close
- ✅ Prevent data loss on route change

### ✅ Feedback

- ✅ Clear visual indicator of unsaved state
- ✅ Polite `aria-live` for status updates
- ✅ Confirm destructive actions

### ✅ Accessibility

- ✅ Dialog with proper ARIA roles
- ✅ Keyboard navigation support
- ✅ Focus management
- ✅ Screen reader friendly

## 🚀 使用示例

```typescript
// 1. 检测未保存的更改
const hasUnsavedChanges = useMemo(() => {
  if (!originalArticle) return false;
  return article.title !== originalArticle.title ||
         article.content !== originalArticle.content;
}, [article, originalArticle]);

// 2. 使用 Hook
const { blocker } = useUnsavedChanges(
  hasUnsavedChanges,
  "您有未保存的更改。如果离开此页面，您的更改将会丢失。"
);

// 3. 使用统一的 Modal 组件渲染对话框
<Modal
  isOpen={blocker.state === "blocked"}
  onClose={() => blocker.reset?.()}
  title="未保存的更改"
  size="sm"
  closeOnOverlayClick={false}
>
  <div className="space-y-4">
    <p className="text-gray-600">
      您有未保存的更改。如果离开此页面，您的更改将会丢失。
    </p>
    <div className="flex justify-end space-x-3">
      <button
        onClick={() => blocker.reset?.()}
        className="px-4 py-2 text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors cursor-pointer"
        autoFocus
      >
        继续编辑
      </button>
      <button
        onClick={() => blocker.proceed?.()}
        className="px-4 py-2 text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors cursor-pointer"
      >
        离开
      </button>
    </div>
  </div>
</Modal>

// 4. 显示状态指示器
<UnsavedChangesIndicator hasUnsavedChanges={hasUnsavedChanges} />
```

## 🎓 最佳实践

### 1. **原始数据管理**

- 加载文章后立即保存原始数据
- 保存成功后更新原始数据
- 新文章初始化时设置原始状态

```typescript
// 加载文章
const loadedArticle = {
  /* ... */
};
setArticle(loadedArticle);
setOriginalArticle(loadedArticle); // 保存原始数据

// 保存成功后
await api.save(articleData);
setOriginalArticle({ ...article, ...articleData }); // 更新原始数据
```

### 2. **性能优化**

- 使用 `useMemo` 缓存检测结果
- 避免不必要的重新计算
- 合理使用 `JSON.stringify` 比较数组

### 3. **用户体验**

- 工具栏始终显示状态
- 对话框文案清晰友好
- 提供明确的操作选项
- 支持键盘操作

### 4. **边界情况处理**

- 新文章初始化
- 加载失败处理
- 保存失败不更新原始数据
- 路由参数变化时重新加载

## 🔧 维护建议

### 扩展到其他表单

1. 复用 `useUnsavedChanges` Hook
2. 实现脏数据检测逻辑
3. 使用项目统一的 `Modal` 组件创建确认对话框
4. 添加 `UnsavedChangesIndicator` 指示器

### 保持 UI 一致性

项目使用统一的 `Modal` 组件（`src/components/ui/Modal.tsx`），所有对话框都应该使用这个组件，而不是创建独立的对话框组件。这样可以：

- ✅ 保持整个项目的 UI 风格统一
- ✅ 共享焦点管理、键盘导航等功能
- ✅ 减少代码重复，提高可维护性
- ✅ 确保无障碍访问的一致性

### 自定义检测逻辑

```typescript
// 简单字段比较
const hasChanges = field1 !== original1 && field2 !== original2;

// 深度比较对象
const hasChanges = JSON.stringify(obj) !== JSON.stringify(originalObj);

// 自定义比较函数
const hasChanges = customCompare(data, originalData);
```

## 🔒 安全特性

- ✅ 防止意外数据丢失
- ✅ 双重保护机制
- ✅ 用户确认后才允许离开
- ✅ 保存成功才清除警告

## 🎨 UI/UX 设计

### 对话框设计（使用统一的 Modal 组件）

- 使用项目标准的 `Modal` 组件
- 简洁的标题："未保存的更改"
- 清晰的提示文字
- 灰色"继续编辑"按钮（默认焦点）
- 红色"离开"按钮（危险操作）
- 继承 Modal 的所有功能：
  - 焦点陷阱
  - Escape 键关闭
  - 点击遮罩关闭（可配置）
  - 平滑的进入/退出动画

### 指示器设计

- 黄色脉冲圆点
- "未保存"文字标签
- 不干扰主要内容
- 位置显眼但不突兀

---

**实现日期**: 2025 年 9 月 30 日  
**符合标准**: Web Interface Guidelines  
**代码质量**: ✅ 无 Linter 错误  
**类型安全**: ✅ 完整 TypeScript 支持  
**测试状态**: ✅ 所有场景已验证
