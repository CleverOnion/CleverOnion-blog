import React, { useState, useEffect, useRef } from 'react';
import Header from '../components/Header';
import ArticleHero from '../components/article/ArticleHero';
import ArticleContent from '../components/article/ArticleContent';
import TableOfContents from '../components/article/TableOfContents';
import CommentSection from '../components/article/CommentSection';

interface TOCItem {
  id: string;
  title: string;
  level: number;
}

const Article: React.FC = () => {
  const [activeSection, setActiveSection] = useState<string>('introduction');
  const articleRef = useRef<HTMLElement>(null);

  // 目录数据
  const tableOfContents: TOCItem[] = [
    { id: 'introduction', title: '引言', level: 2 },
    { id: 'what-are-hooks', title: '什么是 React Hooks', level: 2 },
    { id: 'useState-hook', title: 'useState Hook', level: 3 },
    { id: 'useEffect-hook', title: 'useEffect Hook', level: 3 },
    { id: 'custom-hooks', title: '自定义 Hooks', level: 2 },
    { id: 'best-practices', title: '最佳实践', level: 2 },
    { id: 'common-patterns', title: '常见模式', level: 3 },
    { id: 'performance-tips', title: '性能优化技巧', level: 3 },
    { id: 'conclusion', title: '总结', level: 2 }
  ];

  // 监听滚动，更新活跃章节
  useEffect(() => {
    const handleScroll = () => {
      const sections = tableOfContents.map(item => {
        const element = document.getElementById(item.id);
        if (element) {
          const rect = element.getBoundingClientRect();
          return {
            id: item.id,
            top: rect.top,
            inView: rect.top >= 0 && rect.top <= window.innerHeight / 2
          };
        }
        return null;
      }).filter(Boolean);

      // 找到最接近顶部的可见章节
      const visibleSection = sections.find(section => section && section.inView);
      if (visibleSection) {
        setActiveSection(visibleSection.id);
      } else {
        // 如果没有章节在视口中心，选择最接近顶部的章节
        const closestSection = sections.reduce((closest, current) => {
          if (!current || !closest) return current || closest;
          return Math.abs(current.top) < Math.abs(closest.top) ? current : closest;
        });
        if (closestSection) {
          setActiveSection(closestSection.id);
        }
      }
    };

    window.addEventListener('scroll', handleScroll);
    handleScroll(); // 初始调用

    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // 点击目录项滚动到对应章节
  const scrollToSection = (id: string) => {
    const element = document.getElementById(id);
    if (element) {
      const headerHeight = 80; // Header 高度
      const elementPosition = element.offsetTop - headerHeight;
      window.scrollTo({
        top: elementPosition,
        behavior: 'smooth'
      });
    }
  };

  return (
    <div className="min-h-screen bg-white">
        <Header />
        
        <ArticleHero 
          title="React Hooks 深度解析：从入门到精通"
          author="CleverOnion"
          publishDate="2024年1月15日"
          readTime="8分钟阅读"
          views={1234}
          tags={["React", "Hooks", "前端开发", "JavaScript"]}
        />
        
        <div className="max-w-7xl mx-auto px-4 py-12">
          <div className="flex gap-8">
            <ArticleContent 
              ref={articleRef}
            />
            
            <TableOfContents 
              tableOfContents={tableOfContents}
              activeSection={activeSection}
              onSectionClick={scrollToSection}
            />
          </div>
        </div>
        
        <CommentSection />
      </div>
  );
};

export default Article;