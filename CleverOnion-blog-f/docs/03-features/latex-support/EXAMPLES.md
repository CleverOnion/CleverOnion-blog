# LaTeX 公式示例集

本文档提供了各种场景下的 LaTeX 公式示例，可直接复制使用。

## 📐 几何与三角

### 勾股定理

```markdown
在直角三角形中，$a^2 + b^2 = c^2$，其中 $c$ 是斜边。
```

### 三角函数恒等式

```markdown
$$
\sin^2\theta + \cos^2\theta = 1
$$

$$
\sin(A + B) = \sin A \cos B + \cos A \sin B
$$
```

### 圆的方程

```markdown
$$
(x - h)^2 + (y - k)^2 = r^2
$$
```

## 📊 统计学

### 标准差

```markdown
$$
\sigma = \sqrt{\frac{1}{N}\sum_{i=1}^{N}(x_i - \mu)^2}
$$
```

### 贝叶斯定理

```markdown
$$
P(A|B) = \frac{P(B|A)P(A)}{P(B)}
$$
```

### 协方差

```markdown
$$
\text{Cov}(X, Y) = E[(X - E[X])(Y - E[Y])]
$$
```

## 🎯 微分方程

### 一阶线性微分方程

```markdown
$$
\frac{dy}{dx} + P(x)y = Q(x)
$$
```

### 二阶常系数微分方程

```markdown
$$
ay'' + by' + cy = 0
$$
```

### 热传导方程

```markdown
$$
\frac{\partial u}{\partial t} = \alpha \frac{\partial^2 u}{\partial x^2}
$$
```

## 🔢 数论

### 费马小定理

```markdown
如果 $p$ 是质数，$a$ 不被 $p$ 整除，则：

$$
a^{p-1} \equiv 1 \pmod{p}
$$
```

### 欧拉公式

```markdown
$$
e^{i\theta} = \cos\theta + i\sin\theta
$$

特别地，当 $\theta = \pi$ 时：

$$
e^{i\pi} + 1 = 0
$$
```

## 📈 级数与序列

### 等差数列求和

```markdown
$$
T(n) = \sum_{i=0}^{L} a^i \cdot f\Big(\frac{n}{b^i}\Big)
\\
(L = \log_b n)
$$
```

### 等比数列求和

```markdown
$$
S_n = \frac{a_1(1 - r^n)}{1 - r}, \quad r \neq 1
$$
```

### 泰勒级数

```markdown
$$
f(x) = \sum_{n=0}^{\infty} \frac{f^{(n)}(a)}{n!}(x-a)^n
$$
```

### 傅里叶级数

```markdown
$$
f(x) = \frac{a_0}{2} + \sum_{n=1}^{\infty}\left(a_n\cos\frac{n\pi x}{L} + b_n\sin\frac{n\pi x}{L}\right)
$$
```

## 🧪 化学

### 化学反应平衡

```markdown
$$
K = \frac{[\text{C}]^c[\text{D}]^d}{[\text{A}]^a[\text{B}]^b}
$$
```

### 能斯特方程

```markdown
$$
E = E^0 - \frac{RT}{nF}\ln Q
$$
```

## 🌊 物理

### 牛顿第二定律

```markdown
$$
\vec{F} = m\vec{a}
$$
```

### 万有引力定律

```markdown
$$
F = G\frac{m_1 m_2}{r^2}
$$
```

### 波动方程

```markdown
$$
v = f\lambda
$$

其中 $v$ 是波速，$f$ 是频率，$\lambda$ 是波长。
```

### 洛伦兹力

```markdown
$$
\vec{F} = q(\vec{E} + \vec{v} \times \vec{B})
$$
```

### 能量-动量关系

```markdown
$$
E^2 = (pc)^2 + (m_0c^2)^2
$$
```

## 💻 计算机科学

### 大 O 记号

```markdown
一个算法的时间复杂度为 $O(n\log n)$，空间复杂度为 $O(n)$。
```

### 递归关系

```markdown
$$
T(n) = 2T\left(\frac{n}{2}\right) + O(n)
$$
```

### 信息熵

```markdown
$$
H(X) = -\sum_{i=1}^{n} P(x_i) \log_2 P(x_i)
$$
```

## 🎲 概率论

### 二项分布

```markdown
$$
P(X = k) = \binom{n}{k} p^k (1-p)^{n-k}
$$
```

### 泊松分布

```markdown
$$
P(X = k) = \frac{\lambda^k e^{-\lambda}}{k!}
$$
```

### 正态分布的标准化

```markdown
$$
Z = \frac{X - \mu}{\sigma}
$$
```

## 🔀 组合数学

### 排列

```markdown
$$
P(n, r) = \frac{n!}{(n-r)!}
$$
```

### 组合

```markdown
$$
C(n, r) = \binom{n}{r} = \frac{n!}{r!(n-r)!}
$$
```

### 二项式定理

```markdown
$$
(a + b)^n = \sum_{k=0}^{n} \binom{n}{k} a^{n-k} b^k
$$
```

## 📝 多行对齐示例

### 方程推导

```markdown
$$
\begin{align}
(a + b)^2 &= (a + b)(a + b) \\
&= a^2 + ab + ba + b^2 \\
&= a^2 + 2ab + b^2
\end{align}
$$
```

### 矩阵运算

```markdown
$$
\begin{align}
\mathbf{A}\mathbf{B} &=
\begin{bmatrix}
a & b \\
c & d
\end{bmatrix}
\begin{bmatrix}
e & f \\
g & h
\end{bmatrix} \\
&=
\begin{bmatrix}
ae + bg & af + bh \\
ce + dg & cf + dh
\end{bmatrix}
\end{align}
$$
```

## 🎨 特殊格式

### 分段函数

```markdown
$$
f(x) = \begin{cases}
x^2 & \text{if } x \geq 0 \\
-x^2 & \text{if } x < 0
\end{cases}
$$
```

### 向量点积

```markdown
$$
\vec{a} \cdot \vec{b} = |\vec{a}||\vec{b}|\cos\theta
$$
```

### 向量叉积

```markdown
$$
\vec{a} \times \vec{b} =
\begin{vmatrix}
\vec{i} & \vec{j} & \vec{k} \\
a_1 & a_2 & a_3 \\
b_1 & b_2 & b_3
\end{vmatrix}
$$
```

## 🌟 复杂示例

### 施瓦茨不等式

```markdown
$$
\left(\sum_{i=1}^{n} a_i b_i\right)^2 \leq
\left(\sum_{i=1}^{n} a_i^2\right)
\left(\sum_{i=1}^{n} b_i^2\right)
$$
```

### 斯特林公式

```markdown
$$
n! \approx \sqrt{2\pi n}\left(\frac{n}{e}\right)^n
$$
```

### 黎曼 ζ 函数

```markdown
$$
\zeta(s) = \sum_{n=1}^{\infty} \frac{1}{n^s} = \prod_{p \text{ prime}} \frac{1}{1 - p^{-s}}
$$
```

### 拉格朗日乘数法

```markdown
设要优化函数 $f(x, y)$ 在约束条件 $g(x, y) = 0$ 下的极值，则：

$$
\nabla f = \lambda \nabla g
$$
```

## 💡 使用技巧

### 1. 添加空格

```markdown
<!-- 不推荐 -->

$f(x)=x^2$

<!-- 推荐 -->

$f(x) = x^2$
```

### 2. 使用 \quad 增加间距

```markdown
$$
a = b, \quad b = c, \quad \therefore a = c
$$
```

### 3. 使用 \text{} 添加文本

```markdown
$$
\text{面积} = \frac{1}{2} \times \text{底} \times \text{高}
$$
```

### 4. 使用 \displaystyle 强制显示样式

```markdown
行内显示：$\displaystyle \sum_{i=1}^{n} i$
```

---

**提示**：这些示例可以直接在编辑器中使用，编辑器会实时预览渲染效果。
