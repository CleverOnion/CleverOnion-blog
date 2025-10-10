# Math Plugin for Milkdown

本插件为 Milkdown 编辑器提供 LaTeX 数学公式支持。

## 功能特性

- ✅ **行内公式**：使用 `$...$` 语法
- ✅ **块级公式**：使用 `$$...$$` 语法
- ✅ **实时预览**：编辑时即时渲染
- ✅ **KaTeX 渲染**：快速、轻量的数学公式渲染引擎

## 语法示例

### 行内公式

```markdown
质能方程：$E = mc^2$

圆的面积：$A = \pi r^2$
```

### 块级公式

```markdown
高斯积分：

$$
\int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi}
$$

傅里叶变换：

$$
\hat{f}(\xi) = \int_{-\infty}^{\infty} f(x) e^{-2\pi i x \xi} dx
$$
```

### 矩阵

```markdown
$$
\begin{bmatrix}
a & b \\
c & d
\end{bmatrix}
$$
```

### 分数和根号

```markdown
$$
x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}
$$
```

### 求和与积分

```markdown
$$
\sum_{i=1}^{n} i = \frac{n(n+1)}{2}
$$

$$
\int_0^1 x^2 dx = \frac{1}{3}
$$
```

## 常用 LaTeX 符号

### 希腊字母

- `\alpha`, `\beta`, `\gamma`, `\delta`, `\epsilon`
- `\pi`, `\theta`, `\lambda`, `\mu`, `\sigma`

### 运算符

- `\sum`, `\prod`, `\int`, `\oint`
- `\pm`, `\mp`, `\times`, `\div`
- `\leq`, `\geq`, `\neq`, `\approx`

### 箭头

- `\rightarrow`, `\leftarrow`, `\Rightarrow`, `\Leftarrow`
- `\uparrow`, `\downarrow`

### 集合

- `\in`, `\notin`, `\subset`, `\subseteq`
- `\cup`, `\cap`, `\emptyset`

## 技术实现

- **渲染引擎**：KaTeX
- **Milkdown 插件**：`@milkdown/plugin-math`
- **解析器**：支持标准 LaTeX 语法

## 注意事项

1. **转义字符**：在 Markdown 中使用反斜杠 `\` 时要注意转义
2. **性能**：复杂公式可能影响渲染性能，建议适度使用
3. **兼容性**：KaTeX 支持大部分常用 LaTeX 命令，但不是全部

## 参考资源

- [KaTeX 支持的函数列表](https://katex.org/docs/supported.html)
- [LaTeX 数学符号大全](https://oeis.org/wiki/List_of_LaTeX_mathematical_symbols)
