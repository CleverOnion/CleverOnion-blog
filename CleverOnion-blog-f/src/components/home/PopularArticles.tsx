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
      title: "An Interactive Guide to Flexbox",
      author: "张三",
      publishedAt: "2024-01-10",
      viewCount: 1250
    },
    {
      id: 2,
      title: "A Modern CSS Reset",
      author: "李四",
      publishedAt: "2024-01-08",
      viewCount: 980
    },
    {
      id: 3,
      title: "An Interactive Guide to CSS Transitions",
      author: "王五",
      publishedAt: "2024-01-05",
      viewCount: 856
    },
    {
      id: 4,
      title: "How To Center a Div",
      author: "赵六",
      publishedAt: "2024-01-03",
      viewCount: 742
    },
    {
      id: 5,
      title: "The End of Front-End Development",
      author: "孙七",
      publishedAt: "2024-01-01",
      viewCount: 689
    },
    {
      id: 6,
      title: "Designing Beautiful Shadows in CSS",
      author: "孙七",
      publishedAt: "2024-01-01",
      viewCount: 689
    },
    {
      id: 7,
      title: "An Interactive Guide to CSS Grid",
      author: "孙七",
      publishedAt: "2024-01-01",
      viewCount: 689
    },
    {
      id: 8,
      title: "CSS Variables for React Devs",
      author: "孙七",
      publishedAt: "2024-01-01",
      viewCount: 689
    },
    {
      id: 9,
      title: "Why React Re-Renders",
      author: "孙七",
      publishedAt: "2024-01-01",
      viewCount: 689
    },
    {
      id: 10,
      title: "Making Sense of React Server Components",
      author: "孙七",
      publishedAt: "2024-01-01",
      viewCount: 689
    }
  ];

  const displayArticles = articles.length > 0 ? articles : mockArticles;

  return (
    <div className="p-6 sticky top-20">
      <div className="text-center mb-8">
        <h3 className="text-pink-500 font-semibold text-lg uppercase tracking-wider mb-6">POPULAR CONTENT</h3>
      </div>
      
      <div className="space-y-4">
        {displayArticles.map((article) => (
          <div key={article.id} className="flex items-center group cursor-pointer">
            <span className="text-gray-800 mr-3 text-lg">→</span>
            <h4 className="text-gray-800 font-bold text-base group-hover:text-blue-600 transition-colors">
              {article.title}
            </h4>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PopularArticles;