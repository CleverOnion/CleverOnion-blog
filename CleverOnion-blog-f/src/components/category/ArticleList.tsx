import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { FaCalendar, FaUser, FaEye } from 'react-icons/fa';

interface Author {
  id: number;
  github_id?: number;
  username: string;
  avatar_url?: string;
  name?: string;
  avatar?: string;
}

interface Category {
  id: number;
  name: string;
  icon?: string;
}

interface Tag {
  id: number;
  name: string;
}

interface Article {
  id: string;
  title: string;
  content: string;
  summary: string;
  excerpt?: string;
  status: 'PUBLISHED' | 'DRAFT' | 'ARCHIVED';
  category_id?: number;
  category?: Category;
  author_id?: number;
  author?: Author;
  tag_ids?: number[];
  tags?: Tag[];
  views?: number;
  created_at: string | null;
  updated_at: string | null;
  published_at: string;
  publishedAt?: string;
}

interface ArticleListProps {
  articles: Article[];
  categoryId?: string;
  loading: boolean;
  hasMore: boolean;
  onLoadMore: () => void;
}

const ArticleList: React.FC<ArticleListProps> = ({ 
  articles, 
  categoryId, 
  loading, 
  hasMore, 
  onLoadMore 
}) => {
  // 加载状态的骨架屏组件
  const ArticleSkeleton = () => (
    <div className="bg-white rounded-lg shadow-md p-6 animate-pulse">
      <div className="h-6 bg-gray-200 rounded mb-3 w-3/4"></div>
      <div className="h-4 bg-gray-200 rounded mb-2 w-full"></div>
      <div className="h-4 bg-gray-200 rounded mb-4 w-2/3"></div>
      <div className="flex items-center space-x-4 text-sm">
        <div className="flex items-center space-x-2">
          <div className="w-6 h-6 bg-gray-200 rounded-full"></div>
          <div className="h-4 bg-gray-200 rounded w-16"></div>
        </div>
        <div className="h-4 bg-gray-200 rounded w-20"></div>
        <div className="h-4 bg-gray-200 rounded w-16"></div>
      </div>
    </div>
  );

  return (
    <div className="max-w-4xl mx-auto px-4 py-12">
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">
          文章列表
        </h2>
        <p className="text-gray-600">
          {loading && articles.length === 0 ? (
            '正在加载文章...'
          ) : (
            `共找到 ${articles.length} 篇文章`
          )}
        </p>
      </div>

      <div className="space-y-6">
        {/* 显示文章列表 */}
        {articles.map((article, index) => (
          <motion.article
            key={article.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: index * 0.1 }}
            className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow duration-200"
          >
            <div>
              <Link 
                to={`/article/${article.id}`}
                className="block group mb-3"
              >
                <h3 className="text-xl font-bold text-gray-900 group-hover:text-blue-600 transition-colors duration-200 line-clamp-2">
                  {article.title}
                </h3>
              </Link>
              
              <p className="text-gray-600 mb-4 line-clamp-3">
                {article.summary}
              </p>
              
              {/* 标签 */}
              <div className="flex flex-wrap gap-2 mb-4">
                {article.tags && article.tags.slice(0, 3).map((tag, tagIndex) => (
                  <span
                    key={tag.id || tagIndex}
                    className="inline-block px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded"
                  >
                    {tag.name}
                  </span>
                ))}
                {article.tags && article.tags.length > 3 && (
                  <span className="inline-block px-2 py-1 text-xs font-medium bg-gray-100 text-gray-600 rounded">
                    +{article.tags.length - 3}
                  </span>
                )}
              </div>
              
              {/* 文章元信息 */}
              <div className="flex items-center justify-between text-gray-500 text-sm">
                <div className="flex items-center space-x-4">
                  {article.author && (
                    <div className="flex items-center space-x-1">
                      <FaUser className="w-3 h-3" />
                      <span>{article.author.username}</span>
                    </div>
                  )}
                  <div className="flex items-center space-x-1">
                    <FaCalendar className="w-3 h-3" />
                    <span>{new Date(article.published_at).toLocaleDateString()}</span>
                  </div>
                  {article.views !== undefined && (
                    <div className="flex items-center space-x-1">
                      <FaEye className="w-3 h-3" />
                      <span>{article.views}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </motion.article>
        ))}
        
        {/* 加载状态 */}
        {loading && (
          <>
            {Array.from({ length: 3 }).map((_, index) => (
              <ArticleSkeleton key={`skeleton-${index}`} />
            ))}
          </>
        )}
        
        {/* 空状态 */}
        {!loading && articles.length === 0 && (
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">📝</div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              暂无文章
            </h3>
            <p className="text-gray-600">
              该分类下还没有发布的文章
            </p>
          </div>
        )}
        
        {/* 加载更多按钮 */}
        {!loading && hasMore && articles.length > 0 && (
          <div className="text-center py-8">
            <button
              onClick={onLoadMore}
              className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium transition-colors duration-200"
            >
              加载更多文章
            </button>
          </div>
        )}
        
        {/* 没有更多文章提示 */}
        {!loading && !hasMore && articles.length > 0 && (
          <div className="text-center py-8">
            <p className="text-gray-500">
              已显示全部文章
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ArticleList;