import React from 'react';

interface Article {
  id: number;
  title: string;
  excerpt: string;
  author: string;
  publishedAt: string;
  tags: string[];
}

interface LatestArticlesProps {
  articles?: Article[];
}

const LatestArticles: React.FC<LatestArticlesProps> = ({ articles = [] }) => {
  // 模拟数据
  const mockArticles: Article[] = [
    {
      id: 1,
      title: "React 18 新特性详解",
      excerpt: "深入了解 React 18 带来的并发特性和性能优化...",
      author: "张三",
      publishedAt: "2024-01-15",
      tags: ["React", "前端"]
    },
    {
      id: 2,
      title: "TypeScript 高级类型应用",
      excerpt: "探索 TypeScript 中的高级类型系统和实际应用场景...",
      author: "李四",
      publishedAt: "2024-01-14",
      tags: ["TypeScript", "编程"]
    },
    {
      id: 3,
      title: "Node.js 性能优化实践",
      excerpt: "分享 Node.js 应用性能优化的最佳实践和工具...",
      author: "王五",
      publishedAt: "2024-01-13",
      tags: ["Node.js", "后端"]
    }
  ];

  const displayArticles = articles.length > 0 ? articles : mockArticles;

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-gray-900 mb-6">最新文章</h2>
      
      <div className="space-y-4">
        {displayArticles.map((article) => (
          <article key={article.id} className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
            <h3 className="text-xl font-semibold text-gray-900 mb-2 hover:text-blue-600 cursor-pointer">
              {article.title}
            </h3>
            
            <p className="text-gray-600 mb-4 line-clamp-2">
              {article.excerpt}
            </p>
            
            <div className="flex items-center justify-between text-sm text-gray-500">
              <div className="flex items-center space-x-4">
                <span>作者: {article.author}</span>
                <span>{article.publishedAt}</span>
              </div>
              
              <div className="flex space-x-2">
                {article.tags.map((tag) => (
                  <span key={tag} className="bg-blue-100 text-blue-800 px-2 py-1 rounded text-xs">
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          </article>
        ))}
      </div>
      
      <div className="text-center">
        <button className="text-blue-600 hover:text-blue-800 font-medium">
          查看更多文章 →
        </button>
      </div>
    </div>
  );
};

export default LatestArticles;