# LaTeX 功能测试文章

本文档是一个完整的测试示例，展示了各种 LaTeX 公式的使用方法。可以将此内容复制到编辑器中进行测试。

---

# 数学公式的美妙世界

数学公式是表达科学思想的强大工具。本文将展示如何在文章中优雅地使用数学公式。

## 1. 基础公式

### 1.1 行内公式

在文本中，我们可以这样写：爱因斯坦的质能方程 $E = mc^2$ 揭示了质量和能量的本质联系。圆的面积公式是 $A = \pi r^2$，其中 $r$ 是半径。

勾股定理告诉我们，在直角三角形中，$a^2 + b^2 = c^2$，这是几何学中最基本的定理之一。

### 1.2 块级公式

当我们需要强调一个重要的公式时，可以使用块级公式：

$$
\int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi}
$$

这是著名的高斯积分，在概率论和统计学中有着广泛的应用。

## 2. 微积分

### 2.1 极限

极限是微积分的基础概念：

$$
\lim_{x \to 0} \frac{\sin x}{x} = 1
$$

自然对数的底 $e$ 可以通过极限定义：

$$
e = \lim_{n \to \infty} \left(1 + \frac{1}{n}\right)^n
$$

### 2.2 导数

函数 $f(x)$ 的导数定义为：

$$
f'(x) = \lim_{h \to 0} \frac{f(x+h) - f(x)}{h}
$$

### 2.3 积分

定积分的计算示例：

$$
\int_0^1 x^2 dx = \left[\frac{x^3}{3}\right]_0^1 = \frac{1}{3}
$$

## 3. 代数与方程

### 3.1 二次方程求根公式

对于方程 $ax^2 + bx + c = 0$（其中 $a \neq 0$），其解为：

$$
x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}
$$

### 3.2 方程组

线性方程组可以用矩阵形式表示：

$$
\begin{cases}
x + y = 5 \\
2x - y = 1
\end{cases}
$$

解得 $x = 2$, $y = 3$。

## 4. 线性代数

### 4.1 矩阵运算

矩阵乘法示例：

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

### 4.2 行列式

二阶行列式的计算：

$$
\det(A) = \begin{vmatrix}
a & b \\
c & d
\end{vmatrix} = ad - bc
$$

### 4.3 向量

向量的模（长度）：

$$
|\vec{v}| = \sqrt{x^2 + y^2 + z^2}
$$

向量点积：

$$
\vec{a} \cdot \vec{b} = |\vec{a}||\vec{b}|\cos\theta
$$

## 5. 概率与统计

### 5.1 期望与方差

随机变量 $X$ 的期望：

$$
E[X] = \sum_{i=1}^{n} x_i p(x_i)
$$

方差的定义：

$$
\text{Var}(X) = E[(X - E[X])^2] = E[X^2] - (E[X])^2
$$

### 5.2 正态分布

正态分布的概率密度函数：

$$
f(x) = \frac{1}{\sigma\sqrt{2\pi}} e^{-\frac{(x-\mu)^2}{2\sigma^2}}
$$

其中 $\mu$ 是均值，$\sigma$ 是标准差。

### 5.3 贝叶斯定理

贝叶斯定理是概率论中的重要定理：

$$
P(A|B) = \frac{P(B|A)P(A)}{P(B)}
$$

## 6. 级数与序列

### 6.1 等差数列

等差数列的前 $n$ 项和：

$$
S_n = \frac{n(a_1 + a_n)}{2} = \frac{n[2a_1 + (n-1)d]}{2}
$$

### 6.2 等比数列

等比数列的前 $n$ 项和（$r \neq 1$）：

$$
S_n = \frac{a_1(1 - r^n)}{1 - r}
$$

### 6.3 泰勒级数

函数 $f(x)$ 在 $x = a$ 处的泰勒级数：

$$
f(x) = \sum_{n=0}^{\infty} \frac{f^{(n)}(a)}{n!}(x-a)^n
$$

## 7. 物理公式

### 7.1 经典力学

牛顿第二定律：

$$
\vec{F} = m\vec{a}
$$

万有引力定律：

$$
F = G\frac{m_1 m_2}{r^2}
$$

动能公式：

$$
E_k = \frac{1}{2}mv^2
$$

### 7.2 电磁学

库仑定律：

$$
F = k\frac{q_1 q_2}{r^2}
$$

欧姆定律：$V = IR$

### 7.3 量子力学

薛定谔方程（时间依赖形式）：

$$
i\hbar\frac{\partial}{\partial t}\Psi(\mathbf{r},t) = \hat{H}\Psi(\mathbf{r},t)
$$

海森堡不确定性原理：

$$
\Delta x \cdot \Delta p \geq \frac{\hbar}{2}
$$

## 8. 特殊数学结构

### 8.1 分段函数

绝对值函数的分段定义：

$$
|x| = \begin{cases}
x & \text{if } x \geq 0 \\
-x & \text{if } x < 0
\end{cases}
$$

### 8.2 多行对齐

完全平方公式的推导：

$$
\begin{align}
(a + b)^2 &= (a + b)(a + b) \\
&= a^2 + ab + ba + b^2 \\
&= a^2 + 2ab + b^2
\end{align}
$$

### 8.3 组合数与排列数

组合数的定义：

$$
\binom{n}{k} = C(n, k) = \frac{n!}{k!(n-k)!}
$$

二项式定理：

$$
(a + b)^n = \sum_{k=0}^{n} \binom{n}{k} a^{n-k} b^k
$$

## 9. 高等数学

### 9.1 欧拉公式

数学中最美的公式之一：

$$
e^{i\theta} = \cos\theta + i\sin\theta
$$

特别地，当 $\theta = \pi$ 时：

$$
e^{i\pi} + 1 = 0
$$

这个公式联系了五个最重要的数学常数：$e$、$i$、$\pi$、$1$ 和 $0$。

### 9.2 傅里叶变换

傅里叶变换将时域信号转换到频域：

$$
\hat{f}(\xi) = \int_{-\infty}^{\infty} f(x) e^{-2\pi i x \xi} dx
$$

### 9.3 拉格朗日乘数法

在约束条件 $g(x, y) = 0$ 下，优化目标函数 $f(x, y)$：

$$
\nabla f = \lambda \nabla g
$$

## 10. 希腊字母展示

### 小写字母

$\alpha$, $\beta$, $\gamma$, $\delta$, $\epsilon$, $\zeta$, $\eta$, $\theta$, $\iota$, $\kappa$, $\lambda$, $\mu$, $\nu$, $\xi$, $\pi$, $\rho$, $\sigma$, $\tau$, $\upsilon$, $\phi$, $\chi$, $\psi$, $\omega$

### 大写字母

$\Gamma$, $\Delta$, $\Theta$, $\Lambda$, $\Xi$, $\Pi$, $\Sigma$, $\Phi$, $\Psi$, $\Omega$

## 11. 常用符号

### 运算符

$\pm$, $\mp$, $\times$, $\div$, $\cdot$, $\ast$, $\star$

### 关系符号

$\leq$, $\geq$, $\neq$, $\approx$, $\equiv$, $\sim$, $\propto$

### 集合符号

$\in$, $\notin$, $\subset$, $\subseteq$, $\cup$, $\cap$, $\emptyset$

### 箭头

$\rightarrow$, $\leftarrow$, $\Rightarrow$, $\Leftarrow$, $\leftrightarrow$, $\uparrow$, $\downarrow$

### 其他符号

$\infty$, $\partial$, $\nabla$, $\forall$, $\exists$, $\therefore$, $\because$

## 总结

本文展示了在博客中使用 LaTeX 数学公式的各种方法。无论是简单的行内公式，还是复杂的多行对齐，都能够得到优雅的渲染效果。

希望这个示例能够帮助你快速掌握 LaTeX 公式的使用！

---

**测试说明**：

1. 将本文内容复制到编辑器中
2. 查看实时预览效果
3. 保存并发布
4. 在文章页面查看最终渲染效果

**预期结果**：

- ✅ 所有公式正确渲染
- ✅ 移动端自适应显示
- ✅ 长公式支持横向滚动
- ✅ 页面加载流畅
