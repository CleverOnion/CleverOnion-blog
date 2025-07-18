import React from 'react';
import { useParams } from 'react-router-dom';
import { FaCss3Alt, FaReact, FaJs, FaBriefcase } from 'react-icons/fa';
import { MdAnimation, MdList } from 'react-icons/md';

const CategoryHero: React.FC = () => {
  const { categoryId } = useParams();

  // 分类图标映射
  const categoryIcons: { [key: string]: { icon: React.ComponentType<any>, name: string, color: string } } = {
    'css': { icon: FaCss3Alt, name: 'CSS', color: 'text-blue-500' },
    'react': { icon: FaReact, name: 'React', color: 'text-cyan-500' },
    'javascript': { icon: FaJs, name: 'JavaScript', color: 'text-yellow-500' },
    'career': { icon: FaBriefcase, name: '职场', color: 'text-gray-600' },
    'animation': { icon: MdAnimation, name: '动画', color: 'text-purple-500' },
    'other': { icon: MdList, name: '其他', color: 'text-green-500' }
  };

  const currentCategory = categoryIcons[categoryId || 'other'] || categoryIcons['other'];
  const IconComponent = currentCategory.icon;

  return (
    <section className="relative h-80 bg-gradient-to-br from-purple-400 via-pink-300 to-orange-200 overflow-hidden">
      {/* 不同风格的云朵背景 - 更加抽象和几何化 */}
      <div className="absolute inset-0">
        <svg className="absolute inset-0 w-full h-full" viewBox="0 0 1200 320" preserveAspectRatio="xMidYMid slice">
          {/* 内层云朵 - 接近背景色的纯色圆形云朵 */}
          <g fill="#e6b3cc">
            {/* 左侧云朵群 */}
            <circle cx="100" cy="200" r="60" />
            <circle cx="180" cy="180" r="45" />
            <circle cx="250" cy="210" r="50" />
            <circle cx="320" cy="190" r="40" />
            
            {/* 中央云朵群 */}
            <circle cx="450" cy="160" r="65" />
            <circle cx="530" cy="140" r="50" />
            <circle cx="600" cy="170" r="55" />
            <circle cx="680" cy="150" r="45" />
            
            {/* 右侧云朵群 */}
            <circle cx="800" cy="190" r="60" />
            <circle cx="880" cy="170" r="50" />
            <circle cx="950" cy="200" r="55" />
            <circle cx="1020" cy="180" r="45" />
            <circle cx="1100" cy="210" r="40" />
          </g>
          
          {/* 中层云朵 - 浅色纯色圆形云朵 */}
          <g fill="#f0f0f0">
            {/* 左侧大云朵群 */}
            <circle cx="80" cy="250" r="80" />
            <circle cx="200" cy="230" r="70" />
            <circle cx="300" cy="260" r="75" />
            <circle cx="420" cy="240" r="65" />
            
            {/* 右侧大云朵群 */}
            <circle cx="650" cy="240" r="85" />
            <circle cx="780" cy="220" r="75" />
            <circle cx="900" cy="250" r="70" />
            <circle cx="1020" cy="230" r="65" />
            <circle cx="1150" cy="260" r="60" />
          </g>
          
          {/* 底层云朵 - 纯白色大圆形云朵 */}
          <g fill="#ffffff">
            {/* 横跨底部的大云朵群 */}
            <circle cx="0" cy="300" r="100" />
            <circle cx="120" cy="280" r="95" />
            <circle cx="250" cy="310" r="90" />
            <circle cx="380" cy="290" r="85" />
            <circle cx="500" cy="320" r="95" />
            <circle cx="630" cy="300" r="90" />
            <circle cx="760" cy="280" r="85" />
            <circle cx="890" cy="310" r="90" />
            <circle cx="1020" cy="290" r="85" />
            <circle cx="1150" cy="320" r="80" />
            <circle cx="1200" cy="300" r="75" />
          </g>
        </svg>
      </div>
      
      {/* 分类信息 */}
      <div className="relative z-10 flex flex-col items-center justify-center h-full text-center">
        <div className="bg-white rounded-2xl p-8 shadow-lg">
          <div className="flex items-center justify-center mb-4">
            <IconComponent className={`text-6xl ${currentCategory.color}`} />
          </div>
          <h1 className="text-4xl font-bold text-gray-900 mb-2">
            {currentCategory.name}
          </h1>
          <p className="text-gray-600 text-lg">
            探索 {currentCategory.name} 相关的精彩文章
          </p>
        </div>
      </div>
      
      {/* 装饰性元素 */}
      <div className="absolute top-10 left-10 w-4 h-4 bg-white rounded-full animate-pulse"></div>
      <div className="absolute top-20 right-20 w-6 h-6 bg-gray-200 rounded-full animate-pulse delay-1000"></div>
      <div className="absolute bottom-20 left-20 w-5 h-5 bg-gray-100 rounded-full animate-pulse delay-500"></div>
    </section>
  );
};

export default CategoryHero;