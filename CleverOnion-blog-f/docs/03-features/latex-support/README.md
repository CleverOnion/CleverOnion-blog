# LaTeX 数学公式支持

本博客系统完整支持在文章中使用 LaTeX 数学公式，无论是在编辑器中编写还是在阅读页面中展示。

## 🎯 功能特性

- ✅ **行内公式**：在段落中内嵌数学表达式
- ✅ **块级公式**：独立展示的数学公式块
- ✅ **实时预览**：编辑器中即时渲染
- ✅ **响应式设计**：适配移动端和桌面端
- ✅ **高性能渲染**：使用 KaTeX 引擎
- ✅ **丰富语法**：支持绝大多数 LaTeX 数学命令

## 📝 基础语法

### 行内公式

在段落中使用 `$...$` 包裹公式：

```markdown
爱因斯坦的质能方程 $E = mc^2$ 揭示了质量和能量的关系。

圆的面积公式是 $A = \pi r^2$，其中 $r$ 是半径。
```

**渲染效果**：

> 爱因斯坦的质能方程 $E = mc^2$ 揭示了质量和能量的关系。
>
> 圆的面积公式是 $A = \pi r^2$，其中 $r$ 是半径。

### 块级公式

使用 `$$...$$` 独立展示公式：

```markdown
高斯积分：

$$
\int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi}
$$
```

**渲染效果**：

$$
\int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi}
$$

## 🧮 常用公式示例

### 1. 代数与基础运算

#### 分数

```markdown
$$
\frac{a}{b}, \quad \frac{dy}{dx}, \quad \frac{\partial f}{\partial x}
$$
```

#### 上标与下标

```markdown
$$
x^2, \quad x^{n+1}, \quad x_i, \quad x_{i,j}
$$
```

#### 根号

```markdown
$$
\sqrt{2}, \quad \sqrt[n]{x}, \quad \sqrt{x^2 + y^2}
$$
```

#### 求和与乘积

```markdown
$$
\sum_{i=1}^{n} i = \frac{n(n+1)}{2}
$$

$$
\prod_{i=1}^{n} i = n!
$$
```

### 2. 微积分

#### 极限

```markdown
$$
\lim_{x \to 0} \frac{\sin x}{x} = 1
$$

$$
\lim_{n \to \infty} \left(1 + \frac{1}{n}\right)^n = e
$$
```

#### 导数

```markdown
$$
f'(x) = \lim_{h \to 0} \frac{f(x+h) - f(x)}{h}
$$
```

#### 积分

```markdown
$$
\int_0^1 x^2 dx = \frac{1}{3}
$$

$$
\oint_C \mathbf{F} \cdot d\mathbf{r}
$$
```

### 3. 线性代数

#### 矩阵

```markdown
$$
\begin{bmatrix}
a & b \\
c & d
\end{bmatrix}
\begin{bmatrix}
x \\
y
\end{bmatrix}
=
\begin{bmatrix}
ax + by \\
cx + dy
\end{bmatrix}
$$
```

#### 行列式

```markdown
$$
\det(A) = \begin{vmatrix}
a & b \\
c & d
\end{vmatrix} = ad - bc
$$
```

#### 向量

```markdown
$$
\vec{v} = \langle x, y, z \rangle, \quad |\vec{v}| = \sqrt{x^2 + y^2 + z^2}
$$
```

### 4. 概率与统计

#### 期望与方差

```markdown
$$
E[X] = \sum_{i=1}^{n} x_i p(x_i)
$$

$$
\text{Var}(X) = E[(X - E[X])^2]
$$
```

#### 正态分布

```markdown
$$
f(x) = \frac{1}{\sigma\sqrt{2\pi}} e^{-\frac{(x-\mu)^2}{2\sigma^2}}
$$
```

### 5. 方程组

```markdown
$$
\begin{cases}
x + y = 5 \\
2x - y = 1
\end{cases}
$$
```

### 6. 物理公式

#### 薛定谔方程

```markdown
$$
i\hbar\frac{\partial}{\partial t}\Psi(\mathbf{r},t) = \hat{H}\Psi(\mathbf{r},t)
$$
```

#### 麦克斯韦方程组

```markdown
$$
\begin{align}
\nabla \cdot \mathbf{E} &= \frac{\rho}{\epsilon_0} \\
\nabla \cdot \mathbf{B} &= 0 \\
\nabla \times \mathbf{E} &= -\frac{\partial \mathbf{B}}{\partial t} \\
\nabla \times \mathbf{B} &= \mu_0\mathbf{J} + \mu_0\epsilon_0\frac{\partial \mathbf{E}}{\partial t}
\end{align}
$$
```

## 📚 常用符号速查

### 希腊字母

| 小写 | 代码       | 大写 | 代码       |
| ---- | ---------- | ---- | ---------- |
| α    | `\alpha`   | Α    | `\Alpha`   |
| β    | `\beta`    | Β    | `\Beta`    |
| γ    | `\gamma`   | Γ    | `\Gamma`   |
| δ    | `\delta`   | Δ    | `\Delta`   |
| ε    | `\epsilon` | Ε    | `\Epsilon` |
| θ    | `\theta`   | Θ    | `\Theta`   |
| λ    | `\lambda`  | Λ    | `\Lambda`  |
| μ    | `\mu`      | Μ    | `\Mu`      |
| π    | `\pi`      | Π    | `\Pi`      |
| σ    | `\sigma`   | Σ    | `\Sigma`   |
| φ    | `\phi`     | Φ    | `\Phi`     |
| ω    | `\omega`   | Ω    | `\Omega`   |

### 运算符

| 符号 | 代码     | 符号 | 代码       |
| ---- | -------- | ---- | ---------- |
| ±    | `\pm`    | ∓    | `\mp`      |
| ×    | `\times` | ÷    | `\div`     |
| ≤    | `\leq`   | ≥    | `\geq`     |
| ≠    | `\neq`   | ≈    | `\approx`  |
| ∞    | `\infty` | ∂    | `\partial` |
| ∇    | `\nabla` | ∫    | `\int`     |
| ∑    | `\sum`   | ∏    | `\prod`    |

### 箭头

| 符号 | 代码                   |
| ---- | ---------------------- |
| →    | `\rightarrow` 或 `\to` |
| ←    | `\leftarrow`           |
| ⇒    | `\Rightarrow`          |
| ⇐    | `\Leftarrow`           |
| ↑    | `\uparrow`             |
| ↓    | `\downarrow`           |
| ↔    | `\leftrightarrow`      |

### 集合论

| 符号 | 代码        |
| ---- | ----------- |
| ∈    | `\in`       |
| ∉    | `\notin`    |
| ⊂    | `\subset`   |
| ⊆    | `\subseteq` |
| ∪    | `\cup`      |
| ∩    | `\cap`      |
| ∅    | `\emptyset` |

## 💡 最佳实践

### 1. 编写建议

- **使用描述性文字**：在公式前后添加说明文字
- **合理分段**：复杂公式考虑分步展示
- **对齐美化**：使用 `align` 环境对齐多行公式
- **注释说明**：为复杂符号添加文字说明

### 2. 性能优化

- **避免过大公式**：超长公式会影响渲染性能
- **合理使用块级公式**：重要公式使用块级展示
- **测试移动端**：确保公式在小屏幕上可读

### 3. 可访问性

- **提供文字描述**：为重要公式提供文字解释
- **避免纯公式段落**：公式应配合文字说明
- **测试对比度**：确保公式在不同背景下清晰可读

## 🔧 技术实现

### 编辑器端

- **引擎**：Milkdown with `@milkdown/plugin-math`
- **渲染器**：KaTeX
- **功能**：实时预览、语法高亮

### 阅读端

- **解析器**：`remark-math`
- **渲染器**：`rehype-katex` + KaTeX
- **优化**：自定义 CSS 样式、响应式设计

## 📖 参考资源

- [KaTeX 支持的函数完整列表](https://katex.org/docs/supported.html)
- [LaTeX 数学符号大全](https://www.caam.rice.edu/~heinken/latex/symbols.pdf)
- [LaTeX 数学模式教程](https://www.overleaf.com/learn/latex/Mathematical_expressions)

## ❓ 常见问题

### Q: 为什么我的公式没有渲染？

A: 请检查：

1. 是否正确使用了 `$...$` 或 `$$...$$` 语法
2. LaTeX 语法是否正确
3. 特殊字符是否需要转义

### Q: 如何输入复杂的矩阵？

A: 使用 `bmatrix`、`pmatrix` 等环境，使用 `\\` 换行，`&` 分隔列：

```markdown
$$
\begin{bmatrix}
1 & 2 & 3 \\
4 & 5 & 6 \\
7 & 8 & 9
\end{bmatrix}
$$
```

### Q: 公式在移动端显示不全怎么办？

A: 系统已优化移动端显示，长公式会自动添加横向滚动条。

### Q: 如何居中对齐公式？

A: 使用块级公式 `$$...$$` 会自动居中。

## 🎓 学习路径

1. **入门**：从简单的行内公式开始
2. **进阶**：学习分数、根号、求和等常用符号
3. **高级**：掌握矩阵、方程组、多行对齐
4. **实战**：在实际文章中运用数学公式

---

**提示**：建议在编辑器中实时预览公式效果，确保渲染正确后再发布文章。
