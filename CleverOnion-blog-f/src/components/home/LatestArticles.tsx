import React, { useState, useEffect, useCallback } from "react";
import { articleApi, Article } from "../../api/articles";
import { Link } from "react-router-dom";
import { useThrottle } from "../../hooks/useThrottle";

interface LatestArticlesProps {
  initialPageSize?: number;
}

const LatestArticles: React.FC<LatestArticlesProps> = ({
  initialPageSize = 6,
}) => {
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [showReadMore, setShowReadMore] = useState(false);

  // 加载文章数据
  const loadArticles = useCallback(
    async (page: number = 0, isLoadMore: boolean = false) => {
      try {
        if (isLoadMore) {
          setLoadingMore(true);
        } else {
          setLoading(true);
        }
        setError(null);

        const response = await articleApi.getAllArticles({
          page,
          size: initialPageSize,
          status: "PUBLISHED",
        });

        if (isLoadMore) {
          setArticles((prev) => [...prev, ...response.articles]);
        } else {
          setArticles(response.articles);
        }

        setHasMore(response.has_next);
        setCurrentPage(page);

        // 如果是首次加载且有更多文章，显示READ MORE按钮
        if (!isLoadMore && response.has_next) {
          setShowReadMore(true);
        }
      } catch (err) {
        console.error("加载文章失败:", err);
        setError("加载文章失败，请稍后重试");
      } finally {
        setLoading(false);
        setLoadingMore(false);
      }
    },
    [initialPageSize]
  );

  // 加载更多文章
  const loadMoreArticles = useCallback(() => {
    if (!loadingMore && hasMore) {
      loadArticles(currentPage + 1, true);
      setShowReadMore(false); // 隐藏READ more按钮
    }
  }, [loadArticles, currentPage, loadingMore, hasMore]);

  // 组件挂载时加载初始数据
  useEffect(() => {
    loadArticles();
  }, [loadArticles]);

  // 创建节流的滚动处理函数
  const handleScroll = useThrottle(() => {
    if (showReadMore || loadingMore || !hasMore) return;

    const scrollTop =
      window.pageYOffset || document.documentElement.scrollTop;
    const windowHeight = window.innerHeight;
    const documentHeight = document.documentElement.scrollHeight;

    // 当滚动到距离底部200px时加载更多
    if (scrollTop + windowHeight >= documentHeight - 200) {
      loadMoreArticles();
    }
  }, 150); // 每150ms最多执行一次

  // 滚动监听，实现无限滚动 - 使用节流优化性能
  useEffect(() => {
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [handleScroll, showReadMore, loadingMore, hasMore]);

  // 格式化发布时间
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("zh-CN", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  if (loading && articles.length === 0) {
    return (
      <div className="space-y-8">
        <div className="text-center mb-12">
          <p className="text-pink-500 font-semibold text-lg uppercase tracking-wider mb-2">
            LATEST ARTICLES
          </p>
        </div>
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-pink-500"></div>
          <span className="ml-3 text-gray-600">加载中...</span>
        </div>
      </div>
    );
  }

  if (error && articles.length === 0) {
    return (
      <div className="space-y-8">
        <div className="text-center mb-12">
          <p className="text-pink-500 font-semibold text-lg uppercase tracking-wider mb-2">
            LATEST ARTICLES
          </p>
        </div>
        <div className="text-center py-12">
          <p className="text-red-600 mb-4">{error}</p>
          <button
            onClick={() => loadArticles()}
            className="px-4 py-2 bg-pink-500 text-white rounded-lg hover:bg-pink-600 transition-colors"
          >
            重试
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="text-center mb-12">
        <h2 className="text-pink-500 font-semibold text-lg uppercase tracking-wider mb-2">
          最新文章
        </h2>
      </div>

      <div className="space-y-12">
        {articles.map((article) => (
          <article key={article.id} className="max-w-4xl mx-auto">
            <div className="mb-6">
              <h3 className="text-4xl font-bold text-gray-900 mb-4 leading-tight">
                <Link
                  to={`/article/${article.id}`}
                  className="hover:text-gray-700 transition-colors"
                >
                  {article.title}
                </Link>
              </h3>

              {/* 文章元信息 */}
              <div className="flex items-center gap-4 mb-6 text-sm text-gray-500">
                <span>发布于: {formatDate(article.published_at)}</span>
                {article.views && <span>阅读量: {article.views}</span>}
              </div>

              {/* 文章摘要 */}
              <p className="text-xl text-gray-600 mb-6 leading-relaxed">
                {article.excerpt || article.summary}
              </p>

              {/* 标签 */}
              {article.tags && article.tags.length > 0 && (
                <div className="flex flex-wrap gap-2 mb-6">
                  {article.tags.map((tag, index) => (
                    <span
                      key={typeof tag === "object" ? tag.id : index}
                      className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm"
                    >
                      {typeof tag === "string" ? tag : tag.name}
                    </span>
                  ))}
                </div>
              )}

              <Link
                to={`/article/${article.id}`}
                className="text-gray-900 font-semibold text-lg hover:text-gray-700 transition-colors"
              >
                阅读全文
              </Link>
            </div>
          </article>
        ))}
      </div>

      {/* READ more 按钮 */}
      {showReadMore && hasMore && (
        <div className="text-center py-8">
          <button
            onClick={loadMoreArticles}
            disabled={loadingMore}
            className="px-8 py-3 bg-pink-500 text-white font-semibold text-lg rounded-lg hover:bg-pink-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loadingMore ? "加载中..." : "READ MORE"}
          </button>
        </div>
      )}

      {/* 加载更多状态 */}
      {loadingMore && (
        <div className="flex justify-center items-center py-8">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-pink-500"></div>
          <span className="ml-3 text-gray-600">加载更多文章...</span>
        </div>
      )}

      {/* 没有更多文章提示 */}
      {!hasMore && articles.length > 0 && (
        <div className="text-center py-8">
          <p className="text-gray-500">已显示全部文章</p>
        </div>
      )}

      {/* 错误提示 */}
      {error && articles.length > 0 && (
        <div className="text-center py-4">
          <p className="text-red-600 mb-2">{error}</p>
          <button
            onClick={() => loadArticles(currentPage + 1, true)}
            className="px-4 py-2 bg-pink-500 text-white rounded-lg hover:bg-pink-600 transition-colors"
          >
            重试
          </button>
        </div>
      )}
    </div>
  );
};

export default LatestArticles;
