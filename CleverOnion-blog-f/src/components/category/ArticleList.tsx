import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';

interface Article {
  id: string;
  title: string;
  excerpt: string;
  author: string;
  publishDate: string;
  readTime: string;
  views: number;
  tags: string[];
  coverImage?: string;
}

interface ArticleListProps {
  articles: Article[];
  categoryId: string;
}

const ArticleList: React.FC<ArticleListProps> = ({ articles, categoryId }) => {
  // 模拟数据，实际应该从props或API获取
  const mockArticles: Article[] = [
    {
      id: '1',
      title: 'React Hooks 深度解析：从入门到精通',
      excerpt: '深入探讨React Hooks的工作原理，包括useState、useEffect、useContext等核心Hook的使用技巧和最佳实践。',
      author: 'CleverOnion',
      publishDate: '2024-01-15',
      readTime: '8 分钟',
      views: 1234,
      tags: ['React', 'Hooks', '前端开发']
    },
    {
      id: '2',
      title: 'CSS Grid 布局完全指南',
      excerpt: '全面介绍CSS Grid布局系统，从基础概念到高级应用，帮助你掌握现代网页布局的强大工具。',
      author: 'CleverOnion',
      publishDate: '2024-01-12',
      readTime: '12 分钟',
      views: 987,
      tags: ['CSS', 'Grid', '布局']
    },
    {
      id: '3',
      title: 'JavaScript 异步编程最佳实践',
      excerpt: '探讨Promise、async/await、以及现代JavaScript异步编程模式，提升代码质量和性能。',
      author: 'CleverOnion',
      publishDate: '2024-01-10',
      readTime: '10 分钟',
      views: 756,
      tags: ['JavaScript', '异步编程', 'Promise']
    },
    {
      id: '4',
      title: 'TypeScript 进阶技巧与实战应用',
      excerpt: '深入学习TypeScript的高级特性，包括泛型、装饰器、模块系统等，提升开发效率。',
      author: 'CleverOnion',
      publishDate: '2024-01-08',
      readTime: '15 分钟',
      views: 543,
      tags: ['TypeScript', '进阶', '实战']
    },
    {
      id: '5',
      title: '前端性能优化实用指南',
      excerpt: '从代码分割到懒加载，从缓存策略到图片优化，全方位提升前端应用性能的实用技巧。',
      author: 'CleverOnion',
      publishDate: '2024-01-05',
      readTime: '18 分钟',
      views: 432,
      tags: ['性能优化', '前端', '最佳实践']
    }
  ];

  const displayArticles = articles.length > 0 ? articles : mockArticles;

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">
          共找到 {displayArticles.length} 篇文章
        </h2>
        <p className="text-gray-600">
          以下是 {categoryId} 分类下的所有文章
        </p>
      </div>

      <div className="space-y-6">
        {displayArticles.map((article, index) => (
          <motion.article
            key={article.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: index * 0.1 }}
            className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow duration-300"
          >
            <div className="p-6">
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <Link 
                    to={`/article/${article.id}`}
                    className="block group"
                  >
                    <h3 className="text-xl font-semibold text-gray-900 mb-2 group-hover:text-blue-600 transition-colors duration-200">
                      {article.title}
                    </h3>
                  </Link>
                  <p className="text-gray-600 leading-relaxed mb-4">
                    {article.excerpt}
                  </p>
                </div>
              </div>

              {/* 标签 */}
              <div className="flex flex-wrap gap-2 mb-4">
                {article.tags.map((tag, tagIndex) => (
                  <span
                    key={tagIndex}
                    className="inline-block px-3 py-1 text-xs font-medium bg-blue-50 text-blue-600 rounded-full"
                  >
                    {tag}
                  </span>
                ))}
              </div>

              {/* 文章元信息 */}
              <div className="flex items-center justify-between text-sm text-gray-500">
                <div className="flex items-center space-x-4">
                  <span className="flex items-center">
                    <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                    </svg>
                    {article.author}
                  </span>
                  <span className="flex items-center">
                    <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
                    </svg>
                    {article.publishDate}
                  </span>
                  <span className="flex items-center">
                    <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clipRule="evenodd" />
                    </svg>
                    {article.readTime}
                  </span>
                  <span className="flex items-center">
                    <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                      <path fillRule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
                    </svg>
                    {article.views} 次浏览
                  </span>
                </div>
                <Link 
                  to={`/article/${article.id}`}
                  className="text-blue-600 hover:text-blue-800 font-medium transition-colors duration-200"
                >
                  阅读全文 →
                </Link>
              </div>
            </div>
          </motion.article>
        ))}
      </div>

      {/* 分页组件占位 */}
      <div className="mt-12 flex justify-center">
        <div className="bg-white rounded-lg shadow-sm border border-gray-100 px-6 py-4">
          <p className="text-gray-600 text-center">
            分页组件将在这里实现
          </p>
        </div>
      </div>
    </div>
  );
};

export default ArticleList;