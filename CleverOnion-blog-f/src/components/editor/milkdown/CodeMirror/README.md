# CodeMirror 代码编辑器集成

## 概述

CodeMirror 是为 CleverOnion-blog-f 项目的 Milkdown 编辑器集成的高级代码编辑功能，提供了语法高亮、代码补全、括号匹配等专业的代码编辑体验。

## 功能特性

### 🎯 核心功能
- **语法高亮**: 支持 11+ 种主流编程语言的语法高亮
- **智能补全**: 提供基于语言的代码补全和提示
- **括号匹配**: 自动匹配和高亮对应的括号
- **代码折叠**: 支持代码块的折叠和展开
- **搜索替换**: 内置强大的搜索和替换功能
- **多光标编辑**: 支持多光标同时编辑

### 🎨 用户体验
- **语言选择器**: 动态切换代码块的编程语言
- **复制功能**: 一键复制代码到剪贴板
- **主题适配**: 自动适配亮色和暗色主题
- **响应式设计**: 在不同设备上都有良好的显示效果

## 支持的编程语言

| 语言 | 别名 | 扩展名 |
|------|------|--------|
| JavaScript | js, javascript, jsx | .js, .jsx, .mjs |
| TypeScript | ts, typescript, tsx | .ts, .tsx |
| Python | py, python | .py, .pyw |
| Java | java | .java |
| C++ | cpp, c++, cxx, cc, c | .cpp, .cxx, .cc, .c, .h, .hpp |
| CSS | css | .css |
| HTML | html, htm | .html, .htm |
| JSON | json | .json |
| Markdown | md, markdown | .md, .markdown |
| SQL | sql | .sql |
| XML | xml | .xml |

## 使用方法

### 1. 创建代码块

在编辑器中输入三个反引号开始代码块：

````markdown
```javascript
console.log('Hello, World!');
```
````

### 2. 选择编程语言

- 在代码块右上角点击语言选择器
- 搜索或选择所需的编程语言
- 代码块会自动应用对应的语法高亮

### 3. 编辑代码

- 点击代码块进入编辑模式
- 享受语法高亮、自动缩进等功能
- 使用 `Ctrl+C` 复制选中代码
- 使用 `Ctrl+F` 搜索代码

### 4. 复制代码

- 点击代码块右上角的复制按钮
- 整个代码块内容会被复制到剪贴板

## 配置选项

```typescript
interface CodeMirrorFeatureConfig {
  extensions?: Extension[]           // 自定义 CodeMirror 扩展
  languages?: LanguageDescription[]  // 支持的编程语言
  theme?: Extension                  // 自定义主题
  searchPlaceholder?: string         // 搜索框占位符
  copyText?: string                  // 复制按钮文本
  copyIcon?: string                  // 复制按钮图标
  onCopy?: (text: string) => void    // 复制回调函数
  noResultText?: string              // 无搜索结果提示
}
```

## 文件结构

```
CodeMirror/
├── index.ts              # 主要配置和导出
├── languages.ts          # 编程语言定义
├── CodeMirror.css        # 样式文件
└── README.md            # 说明文档
```

## 技术实现

### 依赖包

- `@codemirror/commands` - 编辑器命令
- `@codemirror/view` - 视图层
- `@codemirror/language` - 语言支持
- `@codemirror/state` - 状态管理
- `codemirror` - 核心包
- `@codemirror/lang-*` - 各种语言包

### 集成方式

1. **配置函数**: `configureCodeMirror` 负责配置 CodeMirror 选项
2. **语言定义**: `languages.ts` 定义所有支持的编程语言
3. **组件集成**: 通过 `codeBlockComponent` 集成到 Milkdown 编辑器
4. **样式定制**: 通过 CSS 变量实现主题适配

## 自定义扩展

### 添加新语言

```typescript
// 在 languages.ts 中添加新语言
LanguageDescription.of({
  name: 'Go',
  alias: ['go', 'golang'],
  extensions: ['go'],
  load: () => import('@codemirror/lang-go').then(m => m.go())
})
```

### 自定义主题

```typescript
import { oneDark } from '@codemirror/theme-one-dark'

configureCodeMirror(ctx, {
  theme: oneDark,
  // 其他配置...
})
```

### 添加扩展

```typescript
import { autocompletion } from '@codemirror/autocomplete'
import { lintGutter } from '@codemirror/lint'

configureCodeMirror(ctx, {
  extensions: [
    autocompletion(),
    lintGutter()
  ],
  // 其他配置...
})
```

## 样式自定义

可以通过修改 CSS 变量来自定义代码编辑器的外观：

```css
.milkdown {
  --crepe-color-surface: #f8f9fa;        /* 背景色 */
  --crepe-color-outline: #e1e5e9;        /* 边框色 */
  --crepe-color-primary: #805610;        /* 主色调 */
  --crepe-color-on-surface: #201b13;     /* 文本色 */
}
```

## 性能优化

- **按需加载**: 语言包采用动态导入，减少初始包大小
- **缓存机制**: 已加载的语言会被缓存，避免重复加载
- **虚拟滚动**: 大文件编辑时使用虚拟滚动提升性能

## 浏览器兼容性

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## 故障排除

### 常见问题

1. **语法高亮不生效**
   - 检查语言名称是否正确
   - 确认对应的语言包已安装

2. **复制功能不工作**
   - 检查浏览器是否支持 Clipboard API
   - 确认网站运行在 HTTPS 环境

3. **样式显示异常**
   - 检查 CSS 文件是否正确导入
   - 确认 CSS 变量定义正确

### 调试方法

```javascript
// 在浏览器控制台中检查 CodeMirror 状态
console.log(editor.view.state.doc.toString())
```

## 更新日志

### v1.0.0 (2024-08-01)
- ✅ 初始版本发布
- ✅ 支持 11 种编程语言
- ✅ 集成语法高亮和代码补全
- ✅ 添加复制和搜索功能
- ✅ 实现主题适配

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进 CodeMirror 功能！

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 发起 Pull Request

## 许可证

本项目采用 MIT 许可证。