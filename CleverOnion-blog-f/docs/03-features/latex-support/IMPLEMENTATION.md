# LaTeX 支持实现说明

本文档面向开发者，说明 LaTeX 数学公式支持的技术实现细节。

## 🏗️ 架构概览

### 系统组成

```
┌─────────────────────────────────────────┐
│         用户编写文章（Markdown）         │
│           包含 LaTeX 公式语法            │
└──────────────┬──────────────────────────┘
               │
               ├─────────────┬─────────────┐
               │             │             │
        ┌──────▼──────┐ ┌───▼────┐ ┌─────▼─────┐
        │ 编辑器端    │ │ 后端   │ │ 阅读端    │
        │ (Milkdown)  │ │ (存储) │ │ (React)   │
        └─────────────┘ └────────┘ └───────────┘
               │                         │
        ┌──────▼──────┐           ┌─────▼─────┐
        │ @milkdown/  │           │ remark-   │
        │ plugin-math │           │ math      │
        └──────┬──────┘           └─────┬─────┘
               │                         │
        ┌──────▼──────┐           ┌─────▼─────┐
        │             │           │ rehype-   │
        │   KaTeX     │◄──────────┤ katex     │
        │             │           │           │
        └─────────────┘           └───────────┘
               │                         │
        ┌──────▼──────┐           ┌─────▼─────┐
        │ 编辑器预览  │           │ 文章渲染  │
        └─────────────┘           └───────────┘
```

## 📦 依赖包

### 核心依赖

```json
{
  "@milkdown/plugin-math": "^7.15.1", // Milkdown 数学插件
  "katex": "^0.16.x", // LaTeX 渲染引擎
  "remark-math": "^6.0.0", // Markdown 数学语法解析
  "rehype-katex": "^7.0.0" // HTML 渲染插件
}
```

### 依赖关系

```
MilkdownEditor
  ├── @milkdown/plugin-math (编辑器插件)
  └── katex (渲染引擎)

ArticleContent (阅读页面)
  ├── remark-math (解析 Markdown 中的数学语法)
  ├── rehype-katex (转换为 HTML)
  └── katex (渲染引擎)
```

## 🔧 编辑器端实现

### 1. Math 插件配置

**文件**：`src/components/editor/milkdown/Math/index.ts`

```typescript
import { math } from "@milkdown/plugin-math";
import type { MilkdownPlugin } from "@milkdown/kit/ctx";

export const mathPlugin: MilkdownPlugin[] = math;
```

### 2. 集成到 MilkdownEditor

**文件**：`src/components/MilkdownEditor.tsx`

```typescript
import { mathPlugin } from "./editor/milkdown/Math";
import "katex/dist/katex.min.css";
import "../styles/katex-custom.css";

export const MilkdownEditor: React.FC<MilkdownEditorProps> = ({ ... }) => {
  const { get } = useEditor((root) =>
    Editor.make()
      // ... 其他配置
      .use(mathPlugin)  // 添加 math 插件
  );

  // ...
};
```

### 3. 工作原理

1. **输入解析**：用户输入 `$...$` 或 `$$...$$`
2. **AST 转换**：Milkdown 解析为数学节点
3. **实时渲染**：KaTeX 将 LaTeX 转换为 HTML
4. **交互支持**：支持编辑、选择、复制

## 📖 阅读端实现

### 1. ArticleContent 配置

**文件**：`src/components/article/ArticleContent.tsx`

```typescript
import ReactMarkdown from "react-markdown";
import remarkMath from "remark-math";
import rehypeKatex from "rehype-katex";
import "katex/dist/katex.min.css";
import "../../styles/katex-custom.css";

const ArticleContent = ({ content }) => {
  return (
    <ReactMarkdown
      remarkPlugins={[remarkMath]} // 解析数学语法
      rehypePlugins={[rehypeKatex]} // 渲染为 HTML
      components={
        {
          // ... 其他组件配置
        }
      }
    >
      {content}
    </ReactMarkdown>
  );
};
```

### 2. 处理流程

```
Markdown 文本
    │
    ▼
[remark-math] 解析数学语法
    │
    ├── 识别 $...$ (inline math)
    ├── 识别 $$...$$ (display math)
    └── 生成 math 节点
    │
    ▼
[rehype-katex] 转换为 HTML
    │
    ├── 调用 KaTeX API
    ├── 生成 HTML + CSS 类
    └── 处理错误
    │
    ▼
浏览器渲染
```

## 🎨 样式系统

### 1. 基础样式

**来源**：`katex/dist/katex.min.css`

- KaTeX 提供的默认样式
- 包含所有数学符号的字体和布局

### 2. 自定义样式

**文件**：`src/styles/katex-custom.css`

```css
/* 块级公式 */
.katex-display {
  margin: 1.5em 0;
  overflow-x: auto;
  text-align: center;
}

/* 行内公式 */
.katex {
  font-size: 1.05em;
}

/* 响应式 */
@media (max-width: 640px) {
  .katex-display {
    font-size: 0.9em;
  }
}

/* 错误处理 */
.katex-error {
  color: #cc0000;
  background-color: #fff0f0;
}
```

### 3. 样式特性

- ✅ **响应式**：自动适配不同屏幕尺寸
- ✅ **可滚动**：长公式支持横向滚动
- ✅ **错误提示**：语法错误有明显标记
- ✅ **无障碍**：支持焦点可见性

## 🔄 数据流

### 编辑流程

```
用户输入
  ↓
Milkdown Editor (编辑状态)
  ├── 实时解析 LaTeX
  ├── KaTeX 渲染预览
  └── 保存为 Markdown 文本
  ↓
后端 API
  ↓
数据库 (存储原始 Markdown)
```

### 阅读流程

```
数据库 (Markdown 文本)
  ↓
后端 API
  ↓
前端接收
  ↓
ArticleContent 组件
  ├── remark-math 解析
  ├── rehype-katex 转换
  └── KaTeX 渲染
  ↓
浏览器展示
```

## 🚀 性能优化

### 1. KaTeX vs MathJax

| 特性     | KaTeX       | MathJax     |
| -------- | ----------- | ----------- |
| 渲染速度 | 极快 (~1ms) | 慢 (~100ms) |
| 包大小   | 小 (~330KB) | 大 (~1.5MB) |
| 功能支持 | 常用公式    | 全面支持    |
| SSR 支持 | 优秀        | 较差        |

**选择 KaTeX 的原因**：

- ⚡ 更快的渲染速度
- 📦 更小的包体积
- 🎯 满足绝大多数使用场景

### 2. 优化策略

#### 编辑器端

```typescript
// 使用防抖避免频繁渲染
const debouncedUpdate = useMemo(
  () => debounce((value: string) => onChange(value), 300),
  [onChange]
);
```

#### 阅读端

```typescript
// 懒加载 KaTeX CSS
import("katex/dist/katex.min.css");

// 服务端渲染支持
// KaTeX 支持在 Node.js 中预渲染
```

### 3. 缓存策略

```typescript
// 浏览器缓存 KaTeX 资源
// vite.config.ts
export default {
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          katex: ["katex"],
        },
      },
    },
  },
};
```

## 🐛 错误处理

### 1. 语法错误

```typescript
// KaTeX 自动处理并显示错误
try {
  katex.render(latex, element);
} catch (error) {
  // 显示红色错误提示
  element.innerHTML = `<span class="katex-error">${error.message}</span>`;
}
```

### 2. 降级方案

```typescript
// 如果 KaTeX 加载失败，显示原始 LaTeX
<noscript>LaTeX content: {latexString}</noscript>
```

## 🧪 测试

### 单元测试示例

```typescript
describe("Math Plugin", () => {
  it("should render inline math", () => {
    const markdown = "Formula: $E = mc^2$";
    const result = render(<ArticleContent content={markdown} />);
    expect(result.container.querySelector(".katex")).toBeInTheDocument();
  });

  it("should render display math", () => {
    const markdown = "$$\\int_0^1 x^2 dx$$";
    const result = render(<ArticleContent content={markdown} />);
    expect(
      result.container.querySelector(".katex-display")
    ).toBeInTheDocument();
  });
});
```

## 📊 浏览器兼容性

KaTeX 支持：

- ✅ Chrome (最新版)
- ✅ Firefox (最新版)
- ✅ Safari (最新版)
- ✅ Edge (最新版)
- ✅ 移动端浏览器

## 🔐 安全性

### XSS 防护

KaTeX 默认转义所有输入，防止 XSS 攻击：

```typescript
// KaTeX 不会执行 HTML/JavaScript
const maliciousInput = '$<script>alert("XSS")</script>$';
// 输出：转义后的文本，不会执行脚本
```

### 内容安全策略 (CSP)

```html
<!-- 添加 CSP 头部 -->
<meta
  http-equiv="Content-Security-Policy"
  content="style-src 'self' 'unsafe-inline';"
/>
```

## 📈 未来扩展

### 可能的增强

1. **公式编辑器**：可视化 LaTeX 编辑器
2. **公式模板**：常用公式快速插入
3. **公式搜索**：支持搜索文章中的数学公式
4. **公式导出**：导出为图片或 PDF

### 技术升级路径

```typescript
// 未来可能的增强
import { mathEditor } from '@some-package/math-editor';

// 可视化编辑器
<MathEditor onSave={(latex) => insertLatex(latex)} />

// 公式库
<MathLibrary category="calculus" onSelect={(latex) => ...} />
```

## 🔗 相关资源

- [KaTeX 文档](https://katex.org/)
- [Milkdown Math Plugin](https://milkdown.dev/docs/plugin/math)
- [remark-math](https://github.com/remarkjs/remark-math)
- [rehype-katex](https://github.com/remarkjs/remark-math/tree/main/packages/rehype-katex)

## 💻 开发指南

### 本地调试

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 测试公式渲染
# 访问 http://localhost:5173/admin/articles/new
# 输入测试公式
```

### 故障排查

1. **公式不渲染**

   ```bash
   # 检查依赖
   npm list katex @milkdown/plugin-math remark-math rehype-katex

   # 重新安装
   npm install --force
   ```

2. **样式错误**

   ```bash
   # 检查 CSS 是否正确加载
   # 查看浏览器开发者工具的 Network 标签
   ```

3. **控制台错误**
   ```bash
   # 查看详细错误信息
   # 检查 LaTeX 语法是否正确
   ```

---

**维护者**：前端开发团队  
**最后更新**：2025-10-10
