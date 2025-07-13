import React from 'react';
import { motion } from 'motion/react';

interface CommentSectionProps {
  // 可以在未来添加评论数据的props
}

const CommentSection: React.FC<CommentSectionProps> = () => {
  return (
    <motion.section 
      className="max-w-7xl mx-auto px-4 pb-12"
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.8, delay: 0.6, ease: "easeOut" }}
    >
      <div className="flex gap-8">
        <div className="flex-1">
          <div className="bg-white/80 backdrop-blur-sm rounded-lg p-8">
            <h3 className="text-2xl font-semibold text-gray-900 mb-6">评论</h3>
            
            {/* 评论表单 */}
            <div className="mb-8">
              <textarea
                className="w-full p-4 border border-gray-300 rounded-lg resize-none focus:ring-2 focus:ring-sky-500 focus:border-transparent"
                rows={4}
                placeholder="写下你的评论..."
              />
              <div className="flex justify-end mt-4">
                <button className="px-6 py-2 bg-sky-600 text-white rounded-lg hover:bg-sky-700 transition-colors">
                  发表评论
                </button>
              </div>
            </div>
            
            {/* 评论列表 */}
            <div className="space-y-6">
              {/* 示例评论 */}
              <div className="border-b border-gray-200 pb-6">
                <div className="flex items-start space-x-4">
                  <div className="w-10 h-10 bg-sky-500 rounded-full flex items-center justify-center text-white font-semibold">
                    A
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-2">
                      <h4 className="font-semibold text-gray-900">Alice</h4>
                      <span className="text-sm text-gray-500">2小时前</span>
                    </div>
                    <p className="text-gray-700 leading-relaxed">
                      这篇文章写得非常好！对 React Hooks 的讲解很详细，特别是自定义 Hooks 的部分，让我学到了很多。
                    </p>
                  </div>
                </div>
              </div>
              
              <div className="border-b border-gray-200 pb-6">
                <div className="flex items-start space-x-4">
                  <div className="w-10 h-10 bg-green-500 rounded-full flex items-center justify-center text-white font-semibold">
                    B
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-2">
                      <h4 className="font-semibold text-gray-900">Bob</h4>
                      <span className="text-sm text-gray-500">5小时前</span>
                    </div>
                    <p className="text-gray-700 leading-relaxed">
                      useEffect 的使用确实需要注意很多细节，感谢分享这些最佳实践！
                    </p>
                  </div>
                </div>
              </div>
              
              <div>
                <div className="flex items-start space-x-4">
                  <div className="w-10 h-10 bg-purple-500 rounded-full flex items-center justify-center text-white font-semibold">
                    C
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-2">
                      <h4 className="font-semibold text-gray-900">Charlie</h4>
                      <span className="text-sm text-gray-500">1天前</span>
                    </div>
                    <p className="text-gray-700 leading-relaxed">
                      期待更多关于性能优化的内容，特别是 useMemo 和 useCallback 的深入讲解。
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        {/* 右侧占位，保持布局一致 */}
        <div className="w-80"></div>
      </div>
    </motion.section>
  );
};

export default CommentSection;