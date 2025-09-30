# 未保存更改警告逻辑修复

## 🐛 问题描述

**问题：** 点击"保存草稿"或"发布文章"按钮后，会弹出"未保存的更改"警告对话框。

**原因：**

1. 保存操作成功后，`originalArticle` 状态没有及时更新
2. 发布操作成功后立即 `navigate()` 导航
3. 此时 `hasUnsavedChanges` 仍为 `true`
4. 路由拦截器检测到未保存更改，显示确认对话框

## ✅ 解决方案

### 核心原则

**保存/发布成功后，立即同步 `originalArticle` 状态，确保 `hasUnsavedChanges` 变为 `false`**

### 修复代码

#### 1. **保存草稿**

```typescript
const handleSaveDraft = async () => {
  // ... 验证逻辑

  try {
    setSaving(true);

    if (isEdit && articleId) {
      await articleApi.updateArticle(articleId, articleData);
      toast.success("草稿保存成功！");
      // ✅ 立即更新原始数据
      setOriginalArticle({ ...article, ...articleData });
    } else {
      const newArticle = await articleApi.createArticle(articleData);
      const updatedArticle = { ...article, id: newArticle.id };
      setArticle(updatedArticle);
      // ✅ 立即更新原始数据
      setOriginalArticle(updatedArticle);
      navigate(`/admin/articles/edit/${newArticle.id}`, { replace: true });
      toast.success("草稿创建成功！");
    }
  } finally {
    setSaving(false);
  }
};
```

#### 2. **发布文章**

```typescript
const handlePublish = async () => {
  // ... 验证逻辑

  try {
    setSaving(true);

    if (isEdit && articleId) {
      if (article.status === "DRAFT") {
        await articleApi.publishArticle(articleId);
        toast.success("文章发布成功！");
        // ✅ 更新原始数据（状态变为 PUBLISHED）
        setOriginalArticle({ ...article, status: "PUBLISHED" });
      } else {
        await articleApi.updateArticle(articleId, articleData);
        toast.success("文章发布成功！");
        // ✅ 更新原始数据
        setOriginalArticle({ ...article, ...articleData });
      }
    } else {
      await articleApi.publishArticleDirectly(articleData);
      toast.success("文章发布成功！");
      // ✅ 更新原始数据
      setOriginalArticle({ ...article, ...articleData, status: "PUBLISHED" });
    }

    // ✅ 使用 setTimeout 确保状态更新完成后再导航
    setTimeout(() => {
      navigate("/admin/articles");
    }, 0);
  } finally {
    setSaving(false);
  }
};
```

#### 3. **更新文章**

```typescript
const handleUpdate = async () => {
  // ... 验证逻辑

  try {
    setSaving(true);

    await articleApi.updateArticle(articleId, articleData);
    toast.success("文章更新成功！");
    // ✅ 更新原始数据，防止触发未保存警告
    setOriginalArticle({ ...article, ...articleData });
  } finally {
    setSaving(false);
  }
};
```

#### 4. **转为草稿**

```typescript
const handleUnpublish = async () => {
  try {
    setSaving(true);
    await articleApi.unpublishArticle(articleId);
    setArticle((prev) => ({ ...prev, status: "DRAFT" }));
    toast.success("文章已转为草稿！");
    // ✅ 更新原始数据（状态变为 DRAFT）
    setOriginalArticle((prev) => (prev ? { ...prev, status: "DRAFT" } : null));
  } finally {
    setSaving(false);
  }
};
```

## 🔍 关键改动

### 1. **同步更新原始数据**

在每个保存/发布/更新操作成功后，立即调用：

```typescript
setOriginalArticle({ ...article, ...savedData });
```

### 2. **延迟导航**

发布成功后使用 `setTimeout` 延迟导航：

```typescript
setTimeout(() => {
  navigate("/admin/articles");
}, 0);
```

**原因：**

- React 状态更新是异步的
- `setOriginalArticle` 后需要等待状态同步
- `setTimeout(fn, 0)` 将导航推迟到下一个事件循环
- 确保 `hasUnsavedChanges` 已更新为 `false`

## 🧪 测试验证

### 保存草稿

- [x] 修改标题
- [x] 点击"保存草稿"
- [x] **不显示**未保存警告 ✅
- [x] 显示保存成功 Toast
- [x] 未保存指示器消失

### 发布文章

- [x] 填写完整表单
- [x] 点击"发布文章"
- [x] **不显示**未保存警告 ✅
- [x] 自动跳转到文章列表
- [x] 显示发布成功 Toast

### 更新文章

- [x] 修改已发布文章
- [x] 点击"更新文章"
- [x] **不显示**未保存警告 ✅
- [x] 停留在编辑页面
- [x] 未保存指示器消失

### 转为草稿

- [x] 已发布文章
- [x] 点击"转为草稿"
- [x] **不显示**未保存警告 ✅
- [x] 按钮组切换到草稿状态
- [x] 未保存指示器消失

### 真正的未保存场景

- [x] 修改标题但不保存
- [x] 点击返回按钮
- [x] **显示**未保存警告 ✅
- [x] 可以选择继续编辑或离开

## 💡 设计模式

### 状态同步模式

```typescript
// 保存成功后的标准流程
try {
  await api.save(data);

  // 1. 更新显示状态
  setArticle(updatedData);

  // 2. 同步原始数据（关键！）
  setOriginalArticle(updatedData);

  // 3. 显示成功提示
  toast.success("保存成功");

  // 4. 导航（如果需要）
  setTimeout(() => navigate(path), 0);
} catch (error) {
  // 错误处理
}
```

### 状态计算

```typescript
const hasUnsavedChanges = useMemo(() => {
  if (!originalArticle) return false;

  // 比较当前数据和原始数据
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

## 🎯 最佳实践

### 1. **保存成功后立即同步**

```typescript
// ✅ 正确
await api.save(data);
setOriginalArticle(data); // 立即同步

// ❌ 错误
await api.save(data);
// 忘记同步 originalArticle
```

### 2. **导航前确保状态更新**

```typescript
// ✅ 正确
setOriginalArticle(data);
setTimeout(() => navigate(path), 0); // 延迟导航

// ❌ 错误
setOriginalArticle(data);
navigate(path); // 立即导航，状态可能未更新
```

### 3. **包含所有相关字段**

```typescript
// ✅ 正确
setOriginalArticle({
  ...article,
  ...savedData,
  status: "PUBLISHED", // 包含状态变化
});

// ❌ 错误
setOriginalArticle(article); // 状态未更新
```

### 4. **错误时不更新原始数据**

```typescript
try {
  await api.save(data);
  setOriginalArticle(data); // 只在成功时更新
} catch (error) {
  // ❌ 不要在这里更新 originalArticle
  // 保持未保存状态
}
```

## 🔧 调试技巧

### 添加日志

```typescript
// 检查状态变化
console.log("Before save:", {
  article,
  originalArticle,
  hasUnsavedChanges,
});

setOriginalArticle(newData);

console.log("After save:", {
  article,
  originalArticle,
  hasUnsavedChanges,
});
```

### 使用 React DevTools

- 查看 `article` 状态
- 查看 `originalArticle` 状态
- 观察 `hasUnsavedChanges` 计算结果
- 确认状态更新时机

## 📊 修复效果

### 修复前 ❌

```
用户操作：修改标题 → 点击"发布文章"
预期：直接跳转到列表页
实际：弹出"未保存的更改"对话框
```

### 修复后 ✅

```
用户操作：修改标题 → 点击"发布文章"
流程：
1. 验证表单 ✅
2. 保存到服务器 ✅
3. 更新 originalArticle ✅
4. hasUnsavedChanges = false ✅
5. 延迟导航 ✅
6. 路由拦截器：检测到无未保存更改，允许导航 ✅
7. 跳转到列表页 ✅
```

---

**修复日期**: 2025 年 9 月 30 日  
**问题类型**: 状态同步逻辑  
**影响范围**: 保存、发布、更新、转为草稿  
**测试状态**: ✅ 所有场景已验证

## 🎉 总结

通过在所有保存操作成功后立即同步 `originalArticle` 状态，并使用 `setTimeout` 延迟导航，完美解决了误触发未保存警告的问题。

现在的行为完全符合用户预期：

- ✅ 保存成功 → 不提示
- ✅ 发布成功 → 不提示
- ✅ 更新成功 → 不提示
- ✅ 真正未保存 → 提示

逻辑清晰、体验完美！🎯
