import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import CategoryHero from '../components/category/CategoryHero';
import ArticleList from '../components/category/ArticleList';
import FloatingCategoryIndicator from '../components/category/FloatingCategoryIndicator';
import { articleApi, Article } from '../api/articles';
import categoryApi, { Category as CategoryType } from '../api/categories';

const Category: React.FC = () => {
  const { categoryId } = useParams();
  const [showFloatingIndicator, setShowFloatingIndicator] = useState(false);
  const [articles, setArticles] = useState<Article[]>([]);
  const [category, setCategory] = useState<CategoryType | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  // 监听滚动事件
  useEffect(() => {
    const handleScroll = () => {
      // CategoryHero组件的高度是320px (h-80 = 20rem = 320px)
      const heroHeight = 320;
      const scrollY = window.scrollY;
      
      // 当滚动超过Hero区域时显示悬浮指示器
      setShowFloatingIndicator(scrollY > heroHeight);
    };

    window.addEventListener('scroll', handleScroll);
    
    // 清理事件监听器
    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, []);

  // 加载分类信息和文章数据
  useEffect(() => {
    const loadCategoryData = async () => {
      if (!categoryId) return;
      
      try {
        setLoading(true);
        setError(null);
        
        const categoryIdNum = parseInt(categoryId);
        if (isNaN(categoryIdNum)) {
          setError('无效的分类ID');
          return;
        }
        
        // 并行加载分类信息和文章数据
        const [categoryResponse, articlesResponse] = await Promise.all([
          categoryApi.getCategoryById(categoryIdNum),
          articleApi.getAllArticles({ 
            categoryId: categoryIdNum, 
            page: 0, 
            size: 10,
            status: 'PUBLISHED'
          })
        ]);
        
        setCategory(categoryResponse);
        setArticles(articlesResponse.articles);
        setCurrentPage(articlesResponse.page);
        setTotalPages(articlesResponse.total_pages);
        setHasMore(articlesResponse.has_next);
        
      } catch (error) {
        console.error('加载分类数据失败:', error);
        setError('加载分类数据失败，请稍后重试');
      } finally {
        setLoading(false);
      }
    };
    
    loadCategoryData();
  }, [categoryId]);
  
  // 加载更多文章
  const loadMoreArticles = async () => {
    if (!categoryId || !hasMore || loading) return;
    
    try {
      setLoading(true);
      const categoryIdNum = parseInt(categoryId);
      const nextPage = currentPage + 1;
      
      const response = await articleApi.getAllArticles({
        categoryId: categoryIdNum,
        page: nextPage,
        size: 10,
        status: 'PUBLISHED'
      });
      
      setArticles(prev => [...prev, ...response.articles]);
      setCurrentPage(response.page);
      setHasMore(response.has_next);
      
    } catch (error) {
      console.error('加载更多文章失败:', error);
    } finally {
      setLoading(false);
    }
  };

  if (error) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="text-6xl mb-4">😕</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">出错了</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button 
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            重新加载
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      {/* Hero区域 */}
      <CategoryHero category={category} loading={loading} />
      
      {/* 文章列表区域 */}
      <ArticleList 
        articles={articles} 
        categoryId={categoryId}
        loading={loading}
        hasMore={hasMore}
        onLoadMore={loadMoreArticles}
      />
      
      {/* 悬浮分类指示器 */}
      <FloatingCategoryIndicator 
        isVisible={showFloatingIndicator} 
        category={category}
      />
    </div>
  );
};

export default Category;