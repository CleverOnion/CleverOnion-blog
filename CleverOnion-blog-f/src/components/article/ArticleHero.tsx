import React from 'react';
import { FaClock, FaEye, FaUser, FaCalendarAlt } from 'react-icons/fa';

interface ArticleHeroProps {
  title: string;
  author: string;
  publishDate: string;
  readTime: string;
  views: number;
  tags: string[];
}

const ArticleHero: React.FC<ArticleHeroProps> = ({
  title,
  author,
  publishDate,
  readTime,
  views,
  tags
}) => {
  return (
    <section className="relative h-[36rem] bg-gradient-to-br from-yellow-400 via-orange-500 to-red-600 overflow-hidden">
      {/* 分层云朵背景 */}
      <div className="absolute inset-0">
        <svg className="absolute inset-0 w-full h-full" viewBox="0 0 1200 580" preserveAspectRatio="xMidYMid slice">
          {/* 简化云朵 - 纯白色 */}
          <g fill="#ffffff">
            {/* 左侧云朵 */}
            <circle cx="200" cy="450" r="120" />
            <circle cx="350" cy="430" r="100" />
            <circle cx="480" cy="460" r="90" />
            
            {/* 右侧云朵 */}
            <circle cx="750" cy="440" r="110" />
            <circle cx="900" cy="420" r="95" />
            <circle cx="1050" cy="450" r="85" />
            
            {/* 底部大云朵 */}
            <circle cx="100" cy="520" r="150" />
            <circle cx="300" cy="540" r="140" />
            <circle cx="600" cy="530" r="130" />
            <circle cx="900" cy="550" r="140" />
            <circle cx="1100" cy="520" r="120" />
          </g>
        </svg>
      </div>

      {/* 内容区域 */}
      <div className="relative z-10 h-full flex items-center justify-center px-4">
        <div className="text-center max-w-4xl mx-auto">
          {/* 文章标题 */}
          <h1 className="text-5xl md:text-6xl font-bold text-black mb-6 leading-tight drop-shadow-lg">
            {title}
          </h1>
          
          {/* 文章元信息 */}
          <div className="flex flex-wrap items-center justify-center gap-6 mb-6 text-black/80">
            <div className="flex items-center gap-2">
              <FaUser className="text-lg" />
              <span className="text-lg font-medium">{author}</span>
            </div>
            <div className="flex items-center gap-2">
              <FaCalendarAlt className="text-lg" />
              <span className="text-lg">{publishDate}</span>
            </div>
            <div className="flex items-center gap-2">
              <FaClock className="text-lg" />
              <span className="text-lg">{readTime}</span>
            </div>
            <div className="flex items-center gap-2">
              <FaEye className="text-lg" />
              <span className="text-lg">{views.toLocaleString()} 次阅读</span>
            </div>
          </div>
          
          {/* 标签 */}
          <div className="flex flex-wrap justify-center gap-3">
            {tags.map((tag, index) => (
              <span
                key={index}
                className="px-4 py-2 bg-white/30 backdrop-blur-sm text-black rounded-full text-sm font-medium border border-black/20 hover:bg-white/40 transition-colors duration-200"
              >
                {tag}
              </span>
            ))}
          </div>
        </div>
      </div>
      
      {/* 底部渐变遮罩 */}
      <div className="absolute bottom-0 left-0 right-0 h-20 bg-gradient-to-t from-white/20 to-transparent"></div>
    </section>
  );
};

export default ArticleHero;