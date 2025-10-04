# 文章编辑器优化方案（桌面端）

> 基于 Web Interface Guidelines 的全面优化建议

## 📋 概览

本文档基于 [Web Interface Guidelines](https://www.w3.org/WAI/ARIA/apg/patterns/) 对 CleverOnion 博客系统的文章编辑器（桌面端）进行全面分析，识别出需要优化的问题并提供具体的解决方案。

**注意：** 本方案专注于桌面端体验优化，不涉及移动端适配。

## 🎯 优化目标

- 提升用户体验和可访问性
- 符合现代 Web 标准和最佳实践
- 增强键盘导航和屏幕阅读器支持
- 改善表单验证和错误处理机制
- 优化桌面端编辑体验

---

## 🔴 高优先级优化项

### 1. 表单内联验证

**当前问题：**

- 表单验证只在提交时进行
- 错误信息通过 Toast 显示，用户需要记住错误位置
- 违反规则：`MUST: Errors inline next to fields; on submit, focus first error`

**优化方案：**

```typescript
// 添加实时验证状态
interface ValidationState {
  title: { isValid: boolean; message: string };
  content: { isValid: boolean; message: string };
  category: { isValid: boolean; message: string };
}

// 实时验证函数
const validateField = (field: string, value: any): ValidationResult => {
  switch (field) {
    case "title":
      return {
        isValid: value.trim().length > 0 && value.trim().length <= 200,
        message:
          value.trim().length === 0
            ? "请输入文章标题"
            : value.trim().length > 200
            ? "标题不能超过200个字符"
            : "",
      };
    case "content":
      return {
        isValid: value.trim().length > 0,
        message: value.trim().length === 0 ? "请输入文章内容" : "",
      };
    case "category":
      return {
        isValid: value !== null,
        message: value === null ? "请选择文章分类" : "",
      };
  }
};
```

**实现细节：**

- 在标题输入框下方显示验证错误
- 在设置面板中显示分类选择错误
- 使用 `aria-describedby` 关联错误信息
- 提交时自动聚焦到第一个错误字段

### 2. 未保存更改警告

**当前问题：**

- 用户可能意外离开页面丢失编辑内容
- 违反规则：`MUST: Warn on unsaved changes before navigation`

**优化方案：**

```typescript
// 添加脏数据检测
const useUnsavedChanges = (article: Article, originalArticle: Article) => {
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  useEffect(() => {
    const isDirty = JSON.stringify(article) !== JSON.stringify(originalArticle);
    setHasUnsavedChanges(isDirty);
  }, [article, originalArticle]);

  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (hasUnsavedChanges) {
        e.preventDefault();
        e.returnValue = "您有未保存的更改，确定要离开吗？";
      }
    };

    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => window.removeEventListener("beforeunload", handleBeforeUnload);
  }, [hasUnsavedChanges]);

  return hasUnsavedChanges;
};
```

**实现细节：**

- 监听页面刷新/关闭事件
- 路由切换时显示确认对话框
- 在工具栏显示未保存状态指示器

### 3. 按钮加载状态优化

**当前问题：**

- 保存按钮在请求期间没有显示加载状态
- 违反规则：`MUST: Loading buttons show spinner and keep original label`

**优化方案：**

```typescript
interface ActionButtonProps {
  loading: boolean;
  onClick: () => void;
  icon: React.ReactNode;
  children: React.ReactNode;
  variant?: "primary" | "secondary";
  disabled?: boolean;
}

const ActionButton: React.FC<ActionButtonProps> = ({
  loading,
  onClick,
  icon,
  children,
  variant = "secondary",
  disabled = false,
}) => (
  <button
    onClick={onClick}
    disabled={loading || disabled}
    className={`
      flex items-center px-4 py-2 text-sm font-medium rounded-lg 
      transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed
      ${
        variant === "primary"
          ? "text-white bg-blue-600 hover:bg-blue-700"
          : "text-gray-700 bg-white border border-gray-300 hover:bg-gray-50"
      }
    `}
    aria-label={loading ? `正在${children}...` : `${children}`}
  >
    {loading ? (
      <div className="w-4 h-4 mr-2 border-2 border-current border-t-transparent rounded-full animate-spin" />
    ) : (
      <span className="w-4 h-4 mr-2">{icon}</span>
    )}
    {children}
  </button>
);
```

**实现细节：**

- 保持按钮文字不变，只替换图标为加载动画
- 禁用按钮防止重复提交
- 添加适当的 ARIA 标签

### 4. 焦点管理优化

**当前问题：**

- 页面加载后没有自动聚焦到合适的元素
- 标题输入框按 Enter 没有合理的行为
- 违反规则：`SHOULD: Autofocus on desktop when there's a single primary input`

**优化方案：**

```typescript
const ArticleEditor = () => {
  const titleInputRef = useRef<HTMLInputElement>(null);
  const editorRef = useRef<any>(null);

  useEffect(() => {
    // 页面加载后自动聚焦到标题输入框
    if (titleInputRef.current) {
      setTimeout(() => {
        titleInputRef.current?.focus();
      }, 100);
    }
  }, []);

  // 处理键盘导航
  const handleTitleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      // 聚焦到编辑器内容区域
      editorRef.current?.focus();
    }
  };

  return (
    <>
      <input
        ref={titleInputRef}
        type="text"
        value={title}
        onChange={(e) => onTitleChange(e.target.value)}
        onKeyDown={handleTitleKeyDown}
        className="..."
        placeholder="无标题文档"
        autoComplete="title"
        aria-label="文章标题"
      />
      <EditorContent ref={editorRef} content={content} onChange={onChange} />
    </>
  );
};
```

**实现细节：**

- 页面加载后自动聚焦标题输入框
- 按 Enter 键自动跳转到编辑器内容区
- 使用 Tab 键可以在各个表单元素间导航

---

---

## 🟡 中优先级优化项

### 5. 无障碍访问改进

**当前问题：**

- 缺少 ARIA 标签和语义化标记
- 违反规则：`MUST: Accurate names (aria-label), verify in the Accessibility Tree`

**优化方案：**

```typescript
const EditorToolbar = () => (
  <header className="..." role="banner" aria-label="文章编辑工具栏">
    <div className="flex items-center space-x-4">
      <button
        onClick={onBack}
        className="..."
        aria-label="返回文章列表"
        title="返回文章列表"
      >
        <FiArrowLeft className="w-5 h-5" aria-hidden="true" />
      </button>

      <input
        type="text"
        value={title}
        onChange={(e) => onTitleChange(e.target.value)}
        className="..."
        placeholder="无标题文档"
        aria-label="文章标题"
        aria-describedby={validationErrors.title ? "title-error" : undefined}
        aria-invalid={!validationState.title.isValid}
      />

      {validationErrors.title && (
        <div
          id="title-error"
          className="text-red-600 text-sm"
          role="alert"
          aria-live="polite"
        >
          {validationState.title.message}
        </div>
      )}
    </div>
  </header>
);
```

**实现细节：**

- 添加适当的 ARIA 角色和标签
- 使用语义化 HTML 元素
- 为错误信息添加 `role="alert"`
- 确保所有交互元素都可以通过键盘访问

### 6. 错误恢复机制

**当前问题：**

- 操作失败后没有提供明确的恢复路径
- 违反规则：`MUST: No dead ends; always offer next step/recovery`

**优化方案：**

```typescript
const ErrorBoundary: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [hasError, setHasError] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const handleRetry = () => {
    setHasError(false);
    setError(null);
    window.location.reload();
  };

  const handleGoBack = () => {
    navigate("/admin/articles");
  };

  if (hasError) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen p-6">
        <div className="text-center max-w-md">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">
            编辑器加载失败
          </h1>
          <p className="text-gray-600 mb-6">
            抱歉，文章编辑器遇到了问题。您可以尝试刷新页面或返回文章列表。
          </p>
          <div className="space-x-4">
            <button
              onClick={handleRetry}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              重试
            </button>
            <button
              onClick={handleGoBack}
              className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700"
            >
              返回列表
            </button>
          </div>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};
```

### 7. 自动保存机制

**当前问题：**

- 没有自动保存功能，用户可能因为意外情况丢失内容
- 建议：增加定期自动保存草稿功能

**优化方案：**

```typescript
const useAutoSave = (
  article: Article,
  onSave: (article: Article) => Promise<void>
) => {
  const [lastSaved, setLastSaved] = useState<Date | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const timeoutRef = useRef<NodeJS.Timeout>();

  // 防抖保存
  useEffect(() => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    // 5秒无操作后自动保存
    timeoutRef.current = setTimeout(async () => {
      if (article.title.trim() || article.content.trim()) {
        setIsSaving(true);
        try {
          await onSave(article);
          setLastSaved(new Date());
        } catch (error) {
          console.error("自动保存失败:", error);
        } finally {
          setIsSaving(false);
        }
      }
    }, 5000);

    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, [article.title, article.content, article.category_id, article.tag_names]);

  return { lastSaved, isSaving };
};
```

**实现细节：**

- 5 秒无操作后自动保存
- 在工具栏显示"上次保存于 X 分钟前"
- 保存时显示"正在保存..."状态
- 使用防抖避免频繁保存

---

## 🟢 低优先级优化项

### 8. 加载骨架屏

**优化方案：**

```typescript
const EditorSkeleton = () => (
  <div className="animate-pulse">
    <div className="h-16 bg-gray-200 mb-4"></div>
    <div className="flex">
      <div className="flex-1 space-y-4">
        <div className="h-8 bg-gray-200 rounded w-3/4"></div>
        <div className="h-96 bg-gray-200 rounded"></div>
      </div>
      <div className="w-80 ml-4 space-y-4">
        <div className="h-32 bg-gray-200 rounded"></div>
        <div className="h-24 bg-gray-200 rounded"></div>
      </div>
    </div>
  </div>
);
```

### 9. 键盘快捷键增强

**优化方案：**

```typescript
const useKeyboardShortcuts = (handlers: {
  onSave: () => void;
  onPublish: () => void;
  onBack: () => void;
}) => {
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Ctrl/Cmd + S: 保存草稿
      if ((e.ctrlKey || e.metaKey) && e.key === "s") {
        e.preventDefault();
        handlers.onSave();
        return;
      }

      // Ctrl/Cmd + Enter: 发布文章
      if ((e.ctrlKey || e.metaKey) && e.key === "Enter") {
        e.preventDefault();
        handlers.onPublish();
        return;
      }

      // Ctrl/Cmd + B: 返回列表
      if ((e.ctrlKey || e.metaKey) && e.key === "b") {
        e.preventDefault();
        handlers.onBack();
        return;
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [handlers]);
};

// 在工具栏显示快捷键提示
const KeyboardShortcutsTooltip = () => (
  <div className="text-xs text-gray-500">
    <div>Ctrl+S: 保存草稿</div>
    <div>Ctrl+Enter: 发布文章</div>
    <div>Ctrl+B: 返回列表</div>
  </div>
);
```

**实现细节：**

- 添加常用操作的键盘快捷键
- 在界面显示快捷键提示
- 避免与浏览器快捷键冲突

---

## 🛠️ 实施计划

### 阶段一：核心体验优化（1-2 周）

1. 实现表单内联验证
2. 添加未保存更改警告
3. 优化按钮加载状态
4. 改进焦点管理

### 阶段二：可访问性增强（1 周）

1. 添加 ARIA 标签和语义化标记
2. 实现错误恢复机制
3. 添加自动保存机制

### 阶段三：体验细节完善（1 周）

1. 添加加载骨架屏
2. 实现键盘快捷键
3. 优化工具栏状态显示

---

## 📊 成功指标

### 用户体验指标

- 表单提交错误率下降 50%
- 用户意外离开页面导致的数据丢失减少 80%
- 编辑器使用效率提升 30%（通过键盘快捷键）
- 自动保存减少手动保存次数 60%

### 技术指标

- 无障碍访问评分达到 WCAG 2.1 AA 级别
- 键盘导航覆盖率达到 100%
- 页面加载性能保持在 95 分以上
- 所有交互操作响应时间 < 100ms

### 测试清单

- [ ] 表单验证在所有字段正常工作
- [ ] 未保存更改警告在所有导航场景下生效
- [ ] 所有按钮都有适当的加载状态
- [ ] 键盘用户可以完成所有操作
- [ ] 屏幕阅读器可以正确读取所有内容
- [ ] 自动保存功能稳定可靠
- [ ] 键盘快捷键无冲突且易用
- [ ] 错误恢复机制提供明确的操作路径

---

## 📚 参考资源

- [WAI-ARIA Authoring Practices Guide](https://www.w3.org/WAI/ARIA/apg/patterns/)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [MDN Accessibility](https://developer.mozilla.org/en-US/docs/Web/Accessibility)
- [React Accessibility](https://reactjs.org/docs/accessibility.html)

---

_最后更新：2025 年 9 月 30 日_
