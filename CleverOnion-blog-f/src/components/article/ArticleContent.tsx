import React, { forwardRef } from 'react';
import { motion } from 'motion/react';

interface ArticleContentProps {
  // 可以在未来添加文章数据的props
}

const ArticleContent = forwardRef<HTMLElement, ArticleContentProps>((props, ref) => {
  return (
    <motion.article 
      ref={ref}
      className="flex-1 bg-white/80 backdrop-blur-sm rounded-lg p-8"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, ease: "easeOut" }}
    >
      <div className="prose max-w-none">
        {/* 引言 */}
        <motion.section
          id="introduction"
          className="scroll-mt-20"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2, ease: "easeOut" }}
        >
          <h2 className="text-3xl font-semibold text-gray-900 mt-8 mb-6">引言</h2>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            React Hooks 的出现彻底改变了我们编写 React 组件的方式。在这篇文章中，我们将深入探讨 Hooks 的核心概念、使用方法以及最佳实践。
          </p>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            无论你是 React 新手还是有经验的开发者，这篇文章都将为你提供有价值的见解和实用的技巧。
          </p>
        </motion.section>

        {/* 什么是 React Hooks */}
        <motion.section
          id="what-are-hooks"
          className="scroll-mt-20"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.4, ease: "easeOut" }}
        >
          <h2 className="text-3xl font-semibold text-gray-900 mt-12 mb-6">什么是 React Hooks</h2>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            React Hooks 是 React 16.8 引入的新特性，它让你在不编写 class 的情况下使用 state 以及其他的 React 特性。这一革命性的变化彻底改变了我们编写 React 组件的方式。
          </p>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            Hooks 的核心思想是将组件逻辑从组件中抽离出来，使其可以独立测试和复用。这不仅提高了代码的可维护性，还让组件变得更加简洁和易于理解。
          </p>
        </motion.section>

        {/* useState Hook */}
        <motion.section
          id="useState-hook"
          className="scroll-mt-20"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.6, ease: "easeOut" }}
        >
          <h3 className="text-2xl font-semibold text-gray-900 mt-10 mb-4">useState Hook</h3>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            useState 是最基础也是最常用的 Hook，它让函数组件能够拥有自己的状态。通过简洁的 API 设计，我们可以轻松地管理组件的状态变化。
          </p>
          <div className="bg-gray-100 p-4 rounded-lg mb-6">
            <code className="text-sm text-gray-800">
              const [count, setCount] = useState(0);
            </code>
          </div>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            useState 返回一个数组，第一个元素是当前状态值，第二个元素是更新状态的函数。这种设计模式被称为数组解构，让我们能够以声明式的方式管理状态。
          </p>
        </motion.section>

        {/* useEffect Hook */}
        <motion.section
          id="useEffect-hook"
          className="scroll-mt-20"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.8, ease: "easeOut" }}
        >
          <h3 className="text-2xl font-semibold text-gray-900 mt-10 mb-4">useEffect Hook</h3>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            useEffect Hook 让你能够在函数组件中执行副作用操作。它相当于 class 组件中的 componentDidMount、componentDidUpdate 和 componentWillUnmount 的组合。
          </p>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            通过 useEffect，我们可以处理数据获取、订阅、手动更改 DOM 等操作。它的设计哲学是将相关的逻辑组织在一起，而不是按照生命周期方法来分离。
          </p>
        </motion.section>

        {/* 自定义 Hooks */}
        <motion.section
          id="custom-hooks"
          className="scroll-mt-20"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 1.0, ease: "easeOut" }}
        >
          <h2 className="text-3xl font-semibold text-gray-900 mt-12 mb-6">自定义 Hooks</h2>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            自定义 Hooks 是 React Hooks 最强大的特性之一。它允许我们将组件逻辑提取到可重用的函数中，实现真正的逻辑复用。
          </p>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            自定义 Hook 就是一个以 "use" 开头的 JavaScript 函数，它可以调用其他的 Hook。通过自定义 Hook，我们可以在不同组件之间共享状态逻辑。
          </p>
        </motion.section>

        {/* 最佳实践 */}
        <motion.section
          id="best-practices"
          className="scroll-mt-20"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 1.2, ease: "easeOut" }}
        >
          <h2 className="text-3xl font-semibold text-gray-900 mt-12 mb-6">最佳实践</h2>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            在使用 React Hooks 时，遵循一些最佳实践可以帮助我们写出更好的代码。这些实践不仅能提高代码质量，还能避免常见的陷阱。
          </p>

          {/* 常见模式 */}
          <h3 id="common-patterns" className="text-2xl font-semibold text-gray-900 mt-10 mb-4">常见模式</h3>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            在实际开发中，有一些常见的 Hooks 使用模式值得我们学习和掌握。这些模式经过了社区的验证，能够帮助我们更高效地解决问题。
          </p>

          {/* 性能优化技巧 */}
          <h3 id="performance-tips" className="text-2xl font-semibold text-gray-900 mt-10 mb-4">性能优化技巧</h3>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            React Hooks 提供了多种性能优化的方法，如 useMemo、useCallback 等。正确使用这些 Hook 可以显著提升应用的性能。
          </p>
        </motion.section>

        {/* 总结 */}
        <motion.section
          id="conclusion"
          className="scroll-mt-20"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 1.4, ease: "easeOut" }}
        >
          <h2 className="text-3xl font-semibold text-gray-900 mt-12 mb-6">总结</h2>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            React Hooks 是现代 React 开发的核心特性。通过本文的学习，相信你已经对 Hooks 有了更深入的理解。
          </p>
          <p className="text-gray-700 leading-relaxed mb-6 text-lg">
            继续实践和探索，你将能够充分发挥 Hooks 的威力，写出更优雅、更高效的 React 代码。
          </p>
        </motion.section>
      </div>
    </motion.article>
  );
});

ArticleContent.displayName = 'ArticleContent';

export default ArticleContent;