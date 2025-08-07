# ImageBlock 图片块组件

## 概述

ImageBlock 是为 CleverOnion-blog-f 项目的 Milkdown 编辑器实现的图片块功能，提供了完整的图片编辑体验，包括图片上传、链接输入、标题编辑、尺寸调整等功能。

## 功能特性

### 🎯 核心功能
- **图片上传**: 支持文件选择和拖拽上传
- **链接输入**: 支持粘贴图片链接
- **图片标题**: 可添加和编辑图片标题
- **尺寸调整**: 提供调整手柄调整图片大小
- **占位符**: 空图片块的友好占位符界面

### 🎨 用户体验
- 直观的上传界面
- 实时预览效果
- 平滑的动画过渡
- 响应式设计
- 暗色主题支持

## 文件结构

```
ImageBlock/
├── index.ts          # 主要导出文件，包含配置函数
├── ImageBlock.css    # 样式文件
└── README.md         # 说明文档
```

## 使用方法

### 基础集成

```typescript
import { configureImageBlock, imageBlockComponent } from './editor/milkdown/ImageBlock';

// 在编辑器配置中使用
Editor.make()
  .config((ctx) => {
    configureImageBlock(ctx, {
      uploadButton: '上传图片',
      uploadPlaceholderText: '或粘贴图片链接...',
      captionPlaceholderText: '添加图片标题',
      onUpload: async (file: File) => {
        // 自定义上传逻辑
        const formData = new FormData();
        formData.append('image', file);
        const response = await fetch('/api/upload', {
          method: 'POST',
          body: formData
        });
        const data = await response.json();
        return data.url;
      }
    });
  })
  .use(imageBlockComponent)
```

### 配置选项

```typescript
interface ImageBlockOptions {
  /** 空图片块占位符图标 */
  imageIcon?: string;
  /** 标题切换按钮图标 */
  captionIcon?: string;
  /** 上传按钮文本 */
  uploadButton?: string;
  /** 确认按钮文本 */
  confirmButton?: string;
  /** 图片块占位符文本 */
  uploadPlaceholderText?: string;
  /** 标题输入占位符文本 */
  captionPlaceholderText?: string;
  /** 图片上传处理函数 */
  onUpload?: (file: File) => Promise<string>;
  /** 图片URL代理函数 */
  proxyDomURL?: (url: string) => Promise<string> | string;
}
```

## 配置说明

### onUpload

图片上传处理函数，当用户选择文件时调用。需要返回一个 Promise，解析为图片的 URL。

```typescript
onUpload: async (file: File) => {
  // 上传到云存储
  const url = await uploadToCloudStorage(file);
  return url;
}
```

### proxyDomURL

可选的图片 URL 代理函数，用于在渲染时代理图片链接。

```typescript
proxyDomURL: (url: string) => {
  // 添加 CDN 前缀或其他处理
  return `https://cdn.example.com/proxy?url=${encodeURIComponent(url)}`;
}
```

### 文本配置

所有文本选项都支持自定义，包括图标、按钮文本和占位符文本：

```typescript
{
  imageIcon: '🖼️',
  captionIcon: '📝',
  uploadButton: '选择图片',
  confirmButton: '确认',
  uploadPlaceholderText: '拖拽图片到此处或粘贴链接',
  captionPlaceholderText: '输入图片描述'
}
```

## 样式自定义

可以通过修改 `ImageBlock.css` 文件来自定义样式：

```css
/* 自定义图片容器样式 */
.milkdown .image-container {
  border-radius: 12px;
  border-width: 3px;
}

/* 自定义上传按钮样式 */
.milkdown .upload-button {
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
}

/* 自定义标题样式 */
.milkdown .image-caption {
  font-style: italic;
  font-weight: 500;
}
```

## 主题支持

ImageBlock 组件完全支持 Milkdown 的主题系统，包括：

- 亮色主题（默认）
- 暗色主题（通过 `data-theme="dark"` 属性）
- 自定义 CSS 变量

## 响应式设计

组件在移动设备上会自动调整：

- 减小内边距和间距
- 调整按钮和图标大小
- 优化触摸交互

## 依赖项

- `@milkdown/kit/component/image-block` - Milkdown 官方 ImageBlock 组件
- `@milkdown/kit/ctx` - Milkdown 上下文系统

## 注意事项

1. **图片上传**: ✅ 已对接后端上传接口，使用阿里云 OSS 存储服务
2. **用户认证**: ⚠️ 需要用户登录后才能上传图片，未登录会提示错误
3. **文件限制**: 支持常见图片格式，最大文件大小 5MB
4. **错误处理**: 上传失败时应该提供适当的错误提示
5. **性能优化**: 大图片建议进行压缩处理

## 示例

### 完整配置示例

```typescript
configureImageBlock(ctx, {
  imageIcon: '🖼️',
  captionIcon: '📝',
  uploadButton: '上传图片',
  confirmButton: '确认 ⏎',
  uploadPlaceholderText: '拖拽图片到此处或粘贴图片链接',
  captionPlaceholderText: '添加图片描述（可选）',
  onUpload: async (file: File) => {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      throw new Error('请选择图片文件');
    }
    
    // 验证文件大小（5MB）
    if (file.size > 5 * 1024 * 1024) {
      throw new Error('图片大小不能超过 5MB');
    }
    
    // 上传到服务器
    const formData = new FormData();
    formData.append('image', file);
    
    const response = await fetch('/upload/image', {
      method: 'POST',
      body: formData
    });
    
    if (!response.ok) {
      throw new Error('上传失败，请重试');
    }
    
    const data = await response.json();
    return data.url;
  },
  proxyDomURL: (url: string) => {
    // 如果是相对路径，添加域名前缀
    if (url.startsWith('/')) {
      return `${window.location.origin}${url}`;
    }
    return url;
  }
});
```