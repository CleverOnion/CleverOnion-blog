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
      title: "Partial Keyframes",
      excerpt: "Creating dynamic, composable CSS keyframe animations",
      author: "张三",
      publishedAt: "2024-01-15",
      tags: ["CSS", "Animation"]
    },
    {
      id: 2,
      title: "The Height Enigma",
      excerpt: "Unraveling the mystery of percentage-based heights in CSS",
      author: "李四",
      publishedAt: "2024-01-14",
      tags: ["CSS", "Layout"]
    },
    {
      id: 3,
      title: "Advanced React Patterns",
      excerpt: "Exploring compound components and render props patterns",
      author: "王五",
      publishedAt: "2024-01-13",
      tags: ["React", "Patterns"]
    }
  ];

  const displayArticles = articles.length > 0 ? articles : mockArticles;

  return (
    <div className="space-y-8">
      <div className="text-center mb-12">
        <p className="text-pink-500 font-semibold text-lg uppercase tracking-wider mb-2">LATEST ARTICLES</p>
      </div>
      
      <div className="space-y-12">
        {displayArticles.map((article) => (
          <article key={article.id} className="max-w-4xl mx-auto">
            <div className="mb-6">
              <h2 className="text-4xl font-bold text-gray-900 mb-4 leading-tight">
                {article.title}
              </h2>
              <p className="text-xl text-gray-600 mb-6 leading-relaxed">
                {article.excerpt}
              </p>
              <p className="text-gray-700 text-lg leading-relaxed mb-8">
                CSS Keyframe animations are so much more powerful than most developers realize. 
                In this tutorial, I'll show you something that completely blew my mind, a technique 
                that makes our keyframe animations so much more reusable and dynamic! 🤯
              </p>
              <button className="text-gray-900 font-semibold text-lg hover:text-gray-700 transition-colors">
                Read more
              </button>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
};

export default LatestArticles;