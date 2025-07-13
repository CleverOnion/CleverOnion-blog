import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import CategoryHero from '../components/category/CategoryHero';
import ArticleList from '../components/category/ArticleList';
import FloatingCategoryIndicator from '../components/category/FloatingCategoryIndicator';

const Category: React.FC = () => {
  const { categoryId } = useParams();
  const [showFloatingIndicator, setShowFloatingIndicator] = useState(false);

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

  // 这里可以根据categoryId获取对应的文章数据
  // 目前使用ArticleList组件内的模拟数据
  const articles = [];

  return (
    <div className="min-h-screen bg-white">
      {/* Hero区域 */}
      <CategoryHero />
      
      {/* 文章列表区域 */}
      <ArticleList articles={articles} categoryId={categoryId || 'unknown'} />
      
      {/* 悬浮分类指示器 */}
      <FloatingCategoryIndicator isVisible={showFloatingIndicator} />
    </div>
  );
};

export default Category;