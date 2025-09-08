1. 组件设计与组织
单一职责原则 (Single Responsibility Principle)
一个组件只做一件事。如果一个组件既负责数据获取、又负责复杂的状态管理，还承担着 UI 渲染，那么它就太臃肿了。应该将它拆分为：

数据获取层 (Container Component): 负责调用 API、管理状态，并将数据作为 props 传递给子组件。

展示层 (Presentational Component): 纯粹地接收 props 并渲染 UI，不包含任何业务逻辑或状态。
这有助于组件的复用和测试。

组件的命名与位置
命名： 使用 PascalCase（大驼峰）命名组件，例如 UserProfile、ProductCard。文件命名也应与组件名一致，如 UserProfile.js。

位置：

公共组件： 放在 src/components 目录下，这些组件不依赖于任何特定的业务逻辑，可以在项目的任何地方复用（例如 Button, Modal, Table）。

业务组件： 放在其所属的业务模块目录下，例如 src/modules/user/components/UserList。这能清晰地表明组件的业务归属。

2. 组件编写规范
函数式组件优先
优先使用函数式组件和 React Hooks。它们不仅是 React 的未来方向，也让组件逻辑更简洁、清晰。避免使用类组件，除非有特殊原因（如需要使用 componentDidCatch 等生命周期方法）。

Props 的使用
解构 Props： 在函数参数中解构 props，这让代码更易读，也能利用 ESLint 检查未使用的 props。避免直接使用 props.propertyName。

Props 类型检查： 强烈建议使用 TypeScript。它提供了强大的静态类型检查，可以避免很多运行时错误，并提供出色的开发体验。如果项目不使用 TypeScript，务必使用 prop-types 来验证组件的 props 类型。

避免 Props Drilling： 如果需要将 props 逐层传递给深层子组件，考虑使用 React Context 或全局状态管理库（如 Redux、Zustand）来解决，这能保持组件树的整洁。

状态管理
useState： 只用于管理组件内部的、简单的 UI 状态（例如模态框的打开/关闭）。

useReducer： 当组件的状态逻辑变得复杂，并且多个状态之间存在关联时，使用 useReducer 是一个更好的选择，它能将状态更新逻辑集中管理。

3. 性能优化与 Hooks 最佳实践
Memoization
React.memo： 当一个组件是“纯”组件（即在给定相同的 props 下，总是渲染相同的内容）时，使用 React.memo 来缓存组件结果，防止父组件重新渲染时造成不必要的子组件渲染。

useCallback 和 useMemo： * useCallback 用于缓存函数，当函数作为 props 传递给子组件时，可以避免子组件因函数引用变化而重新渲染。

useMemo 用于缓存计算结果，避免在每次渲染时都进行昂贵的计算。

提示： 不要滥用 useCallback 和 useMemo。过度使用反而可能增加代码复杂性，并带来额外的性能开销。只有当性能问题确实存在，或者函数/值作为依赖项传递给其他 Hook 或组件时才考虑使用。

useEffect 的依赖项
依赖项完整性： 确保 useEffect 的依赖数组中包含了所有在副作用函数中使用的、且可能随时间变化的变量。使用 ESLint 的 exhaustive-deps 规则来自动检查。

清理函数： 如果 useEffect 订阅了某些事件、定时器或建立了连接，务必在返回一个清理函数，在组件卸载时进行清理，以避免内存泄漏。

4. 可维护性与可读性
JSX 格式化
属性换行： 如果一个组件的 props 超过一行，将每个 prop 放在单独的一行，并使用相同的缩进。

自闭合标签： 对于没有子元素的组件，使用自闭合标签 <MyComponent />。

注释
清晰的组件注释： 在组件定义上方，使用 JSDoc 格式或简单的注释块，描述组件的用途、接收的 props 以及可能存在的注意事项。

代码逻辑注释： 注释复杂的业务逻辑或一些“黑魔法”，解释“为什么”这样做，而不是简单地重复代码。