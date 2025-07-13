import React from 'react';

interface PopularArticle {
  id: number;
  title: string;
  author: string;
  publishedAt: string;
  viewCount: number;
}

interface PopularArticlesProps {
  articles?: PopularArticle[];
}

const PopularArticles: React.FC<PopularArticlesProps> = ({ articles = [] }) => {
  // 模拟数据
  const mockArticles: PopularArticle[] = [
    {
      id: 1,
      title: "深入理解 JavaScript 闭包",
      author: "张三",
      publishedAt: "2024-01-10",
      viewCount: 1250
    },
    {
      id: 2,
      title: "React Hooks 最佳实践",
      author: "李四",
      publishedAt: "2024-01-08",
      viewCount: 980
    },
    {
      id: 3,
      title: "CSS Grid 布局完全指南",
      author: "王五",
      publishedAt: "2024-01-05",
      viewCount: 856
    },
    {
      id: 4,
      title: "Node.js 微服务架构设计",
      author: "赵六",
      publishedAt: "2024-01-03",
      viewCount: 742
    },
    {
      id: 5,
      title: "TypeScript 类型体操进阶",
      author: "孙七",
      publishedAt: "2024-01-01",
      viewCount: 689
    }
  ];

  const displayArticles = articles.length > 0 ? articles : mockArticles;

  return (
    <div className="bg-white rounded-lg shadow-sm p-6 sticky top-4">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">热门文章</h3>
      
      <div className="space-y-3">
        {displayArticles.map((article, index) => (
          <div key={article.id} className="flex items-start space-x-3 group cursor-pointer">
            <div className="flex-shrink-0 w-6 h-6 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-sm font-medium">
              {index + 1}
            </div>
            
            <div className="flex-1 min-w-0">
              <h4 className="text-sm font-medium text-gray-900 group-hover:text-blue-600 transition-colors line-clamp-2">
                {article.title}
              </h4>
              
              <div className="mt-1 flex items-center text-xs text-gray-500 space-x-2">
                <span>{article.author}</span>
                <span>•</span>
                <span>{article.viewCount} 阅读</span>
              </div>
            </div>
          </div>
        ))}
      </div>
      
      <div className="mt-4 text-center">
        <button className="text-blue-600 hover:text-blue-800 text-sm font-medium">
          查看更多 →
        </button>
      </div>
    </div>
  );
};

export default PopularArticles;