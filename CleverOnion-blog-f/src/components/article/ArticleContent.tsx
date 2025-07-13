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
      <div className="prose prose-lg prose-gray max-w-none prose-headings:text-gray-900 prose-p:text-gray-700 prose-p:leading-relaxed prose-strong:text-gray-900 prose-code:text-pink-600 prose-code:bg-pink-50 prose-code:px-1 prose-code:py-0.5 prose-code:rounded prose-pre:bg-gray-900 prose-pre:text-gray-100">
        {/* 引言 */}
        <motion.section
          id="introduction"
          className="scroll-mt-20"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2, ease: "easeOut" }}
        >
          <h2>引言</h2>
          <p>
            React Hooks 的出现彻底改变了我们编写 React 组件的方式。在这篇文章中，我们将深入探讨 Hooks 的核心概念、使用方法以及最佳实践。
          </p>
          <p>
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
          <h2>什么是 React Hooks</h2>
          <p>
            React Hooks 是 <strong>React 16.8</strong> 引入的新特性，它让你在不编写 class 的情况下使用 state 以及其他的 React 特性。这一革命性的变化彻底改变了我们编写 React 组件的方式。
          </p>
          <p>
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
          <h3>useState Hook</h3>
          <p>
            <code>useState</code> 是最基础也是最常用的 Hook，它让函数组件能够拥有自己的状态。通过简洁的 API 设计，我们可以轻松地管理组件的状态变化。
          </p>
          <div className="not-prose">
            <pre className="bg-gray-900 text-gray-100 p-6 rounded-lg mb-6 overflow-x-auto">
              <code className="text-sm font-mono">
                <span className="text-blue-400">const</span> [<span className="text-yellow-300">count</span>, <span className="text-yellow-300">setCount</span>] = <span className="text-green-400">useState</span>(<span className="text-orange-400">0</span>);
              </code>
            </pre>
          </div>
          <p>
            <code>useState</code> 返回一个数组，第一个元素是当前状态值，第二个元素是更新状态的函数。这种设计模式被称为<strong>数组解构</strong>，让我们能够以声明式的方式管理状态。
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
          <h3>useEffect Hook</h3>
          <p>
            <code>useEffect</code> Hook 让你能够在函数组件中执行副作用操作。它相当于 class 组件中的 <code>componentDidMount</code>、<code>componentDidUpdate</code> 和 <code>componentWillUnmount</code> 的组合。
          </p>
          <p>
            通过 <code>useEffect</code>，我们可以处理数据获取、订阅、手动更改 DOM 等操作。它的设计哲学是将相关的逻辑组织在一起，而不是按照生命周期方法来分离。
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
          <h2>自定义 Hooks</h2>
          <p>
            自定义 Hooks 是 React Hooks 最强大的特性之一。它允许我们将组件逻辑提取到可重用的函数中，实现真正的逻辑复用。
          </p>
          <p>
            自定义 Hook 就是一个以 <code>"use"</code> 开头的 JavaScript 函数，它可以调用其他的 Hook。通过自定义 Hook，我们可以在不同组件之间共享状态逻辑。
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
          <h2>最佳实践</h2>
          <p>
            在使用 React Hooks 时，遵循一些最佳实践可以帮助我们写出更好的代码。这些实践不仅能提高代码质量，还能避免常见的陷阱。
          </p>

          <blockquote>
            <p>记住：Hooks 的规则很简单，但遵循它们至关重要。</p>
          </blockquote>

          {/* 常见模式 */}
          <h3 id="common-patterns">常见模式</h3>
          <p>
            在实际开发中，有一些常见的 Hooks 使用模式值得我们学习和掌握。这些模式经过了社区的验证，能够帮助我们更高效地解决问题。
          </p>

          <ul>
            <li>使用 <code>useReducer</code> 管理复杂状态</li>
            <li>通过 <code>useContext</code> 避免 prop drilling</li>
            <li>利用 <code>useRef</code> 访问 DOM 元素</li>
          </ul>

          {/* 性能优化技巧 */}
          <h3 id="performance-tips">性能优化技巧</h3>
          <p>
            React Hooks 提供了多种性能优化的方法，如 <code>useMemo</code>、<code>useCallback</code> 等。正确使用这些 Hook 可以显著提升应用的性能。
          </p>

          <ol>
            <li><strong>useMemo</strong>：缓存计算结果</li>
            <li><strong>useCallback</strong>：缓存函数引用</li>
            <li><strong>React.memo</strong>：组件级别的优化</li>
          </ol>
        </motion.section>

        {/* 总结 */}
        <motion.section
          id="conclusion"
          className="scroll-mt-20"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 1.4, ease: "easeOut" }}
        >
          <h2>总结</h2>
          <p>
            React Hooks 是现代 React 开发的核心特性。通过本文的学习，相信你已经对 Hooks 有了更深入的理解。
          </p>
          <p>
            继续实践和探索，你将能够充分发挥 Hooks 的威力，写出更优雅、更高效的 React 代码。
          </p>

          <hr />

          <p><em>希望这篇文章对你的 React 学习之路有所帮助！</em></p>
        </motion.section>
      </div>
    </motion.article>
  );
});

ArticleContent.displayName = 'ArticleContent';

export default ArticleContent;