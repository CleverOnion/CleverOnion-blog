import React from 'react';
import { motion } from 'motion/react';

interface ArticleHeaderProps {
  title?: string;
  author?: string;
  publishDate?: string;
  readTime?: string;
  tags?: string[];
}

const ArticleHeader: React.FC<ArticleHeaderProps> = ({
  title = "React Hooks 深度解析",
  author = "CleverOnion",
  publishDate = "2024年1月15日",
  readTime = "8分钟阅读",
  tags = ["React", "Hooks", "前端开发", "JavaScript"]
}) => {
  return (
    <motion.header 
      className="max-w-7xl mx-auto px-4 pt-8 pb-12"
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, ease: "easeOut" }}
    >
      <div className="flex gap-8">
        <div className="flex-1">
          <div className="bg-white/80 backdrop-blur-sm rounded-lg p-8">
            {/* 文章标题 */}
            <motion.h1 
              className="text-4xl font-bold text-gray-900 mb-6 leading-tight"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.2, ease: "easeOut" }}
            >
              {title}
            </motion.h1>
            
            {/* 文章元信息 */}
            <motion.div 
              className="flex flex-wrap items-center gap-4 text-gray-600 mb-6"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.3, ease: "easeOut" }}
            >
              <div className="flex items-center space-x-2">
                <div className="w-8 h-8 bg-sky-500 rounded-full flex items-center justify-center text-white text-sm font-semibold">
                  {author.charAt(0)}
                </div>
                <span className="font-medium">{author}</span>
              </div>
              
              <span className="text-gray-400">•</span>
              <span>{publishDate}</span>
              
              <span className="text-gray-400">•</span>
              <span>{readTime}</span>
            </motion.div>
            
            {/* 标签 */}
            <motion.div 
              className="flex flex-wrap gap-2"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.4, ease: "easeOut" }}
            >
              {tags.map((tag, index) => (
                <motion.span
                  key={`header-tag-${tag}-${index}`}
                  className="px-3 py-1 bg-sky-100 text-sky-700 rounded-full text-sm font-medium hover:bg-sky-200 transition-colors cursor-pointer"
                  initial={{ opacity: 0, scale: 0.8 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ 
                    duration: 0.3, 
                    delay: 0.5 + index * 0.1, 
                    ease: "easeOut" 
                  }}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  #{tag}
                </motion.span>
              ))}
            </motion.div>
          </div>
        </div>
        
        {/* 右侧占位，保持布局一致 */}
        <div className="w-80"></div>
      </div>
    </motion.header>
  );
};

export default ArticleHeader;