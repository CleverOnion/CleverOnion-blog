import React, { useState, useEffect } from "react";
import { articleApi, type Article } from "../../api/articles";

interface PopularArticlesProps {
  articles?: Article[];
}

const PopularArticles: React.FC<PopularArticlesProps> = React.memo(
  ({ articles = [] }) => {
    const [popularArticles, setPopularArticles] = useState<Article[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // 加载热门文章
    const loadPopularArticles = async () => {
      try {
        setLoading(true);
        setError(null);

        // 获取已发布的文章，按阅读量排序，取前10篇
        const response = await articleApi.getAllArticles({
          page: 0,
          size: 10,
          status: "PUBLISHED",
        });

        setPopularArticles(response.articles || []);
      } catch (err) {
        console.error("加载热门文章失败:", err);
        setError("加载热门文章失败");
      } finally {
        setLoading(false);
      }
    };

    useEffect(() => {
      loadPopularArticles();
    }, []);

    const displayArticles = articles.length > 0 ? articles : popularArticles;

    if (loading) {
      return (
        <div className="p-4 sm:p-6 sticky top-20">
          <div className="text-center mb-6 sm:mb-8">
            <h2 className="text-pink-500 font-semibold text-xl sm:text-2xl uppercase tracking-wider mb-4 sm:mb-6">
              热门文章
            </h2>
          </div>
          <div className="space-y-3 sm:space-y-4">
            {[...Array(10)].map((_, index) => (
              <div key={index} className="flex items-center">
                <span className="text-gray-300 mr-2 sm:mr-3 text-base sm:text-lg">
                  →
                </span>
                <div className="h-3 sm:h-4 bg-gray-200 rounded animate-pulse flex-1"></div>
              </div>
            ))}
          </div>
        </div>
      );
    }

    if (error) {
      return (
        <div className="p-4 sm:p-6 sticky top-20">
          <div className="text-center mb-6 sm:mb-8">
            <h3 className="text-pink-500 font-semibold text-xl sm:text-2xl uppercase tracking-wider mb-4 sm:mb-6">
              POPULAR CONTENT
            </h3>
          </div>
          <div className="text-center text-red-500 text-xs sm:text-sm">
            {error}
          </div>
        </div>
      );
    }

    return (
      <div className="p-4 sm:p-6 sticky top-20">
        <div className="text-center mb-6 sm:mb-8">
          <h2 className="text-pink-500 font-semibold text-xl sm:text-2xl uppercase tracking-wider mb-4 sm:mb-6">
            热门文章
          </h2>
        </div>

        <div className="space-y-3 sm:space-y-4">
          {displayArticles.length > 0 ? (
            displayArticles.map((article) => (
              <div
                key={article.id}
                className="flex items-center group cursor-pointer"
              >
                <span className="text-gray-800 mr-2 sm:mr-3 text-base sm:text-lg">
                  →
                </span>
                <h3 className="text-gray-800 font-bold text-sm sm:text-base group-hover:text-blue-600 transition-colors line-clamp-2">
                  {article.title}
                </h3>
              </div>
            ))
          ) : (
            <div className="text-center text-gray-500 text-xs sm:text-sm">
              暂无热门文章
            </div>
          )}
        </div>
      </div>
    );
  }
);

PopularArticles.displayName = "PopularArticles";

export default PopularArticles;
