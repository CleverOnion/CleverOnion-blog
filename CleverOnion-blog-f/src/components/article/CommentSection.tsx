import React, { useState } from 'react';
import { motion } from 'motion/react';
import { FaReply } from 'react-icons/fa';

interface Comment {
  id: number;
  author: string;
  avatar: string;
  content: string;
  timeAgo: string;
  replies?: Comment[];
  isCollapsed?: boolean;
}

interface CommentSectionProps {
  // 可以在未来添加评论数据的props
}

const CommentSection: React.FC<CommentSectionProps> = () => {
  const [comments] = useState<Comment[]>([
    {
      id: 1,
      author: "小明",
      avatar: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=40&h=40&fit=crop&crop=face&auto=format",
      content: "这篇文章写得太好了！作者的观点很有见地，让我学到了很多新知识。",
      timeAgo: "11天前"
    },
    {
      id: 2,
      author: "技术爱好者",
      avatar: "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=40&h=40&fit=crop&crop=face&auto=format",
      content: "同意楼上的观点，这个技术栈确实很值得学习。我也在项目中使用过类似的方案。",
      timeAgo: "10天前",
      replies: [
        {
          id: 21,
          author: "前端小白",
          avatar: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=40&h=40&fit=crop&crop=face&auto=format",
          content: "请问有没有相关的学习资源推荐？我是刚入门的新手，想深入了解一下。",
          timeAgo: "9天前"
        }
      ]
    },
    {
      id: 3,
      author: "代码女神",
      avatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=40&h=40&fit=crop&crop=face&auto=format",
      content: "文章结构清晰，代码示例也很实用。希望作者能多分享一些实战经验！",
      timeAgo: "8天前"
    },
    {
      id: 4,
      author: "全栈工程师",
      avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=40&h=40&fit=crop&crop=face&auto=format",
      content: "从后端的角度来看，这种前端架构设计确实能够很好地配合API接口。点赞！",
      timeAgo: "7天前"
    },
    {
      id: 5,
      author: "UI设计师",
      avatar: "https://images.unsplash.com/photo-1544725176-7c40e5a71c5e?w=40&h=40&fit=crop&crop=face&auto=format",
      content: "不仅技术实现得好，界面设计也很美观。用户体验考虑得很周到。",
      timeAgo: "6天前"
    },
    {
      id: 6,
      author: "学习达人",
      avatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=40&h=40&fit=crop&crop=face&auto=format",
      content: "收藏了！准备按照文章的思路在自己的项目中实践一下。感谢分享！",
      timeAgo: "5天前"
    }
  ]);



  const renderComment = (comment: Comment, isReply = false) => (
    <div key={comment.id} className={`${isReply ? 'ml-12 mt-4' : ''}`}>
      <div className="flex items-start space-x-3">
        {/* 头像 */}
        <img 
          src={comment.avatar} 
          alt={comment.author}
          className="w-10 h-10 rounded-full flex-shrink-0"
        />
        
        {/* 评论内容 */}
        <div className="flex-1 min-w-0">
          {/* 用户信息和时间 */}
          <div className="flex items-center space-x-2 mb-2">
            <span className="font-medium text-gray-800 text-sm">{comment.author}</span>
            <span className="text-xs text-gray-500">{comment.timeAgo}</span>
            <button className="text-xs text-gray-400 hover:text-gray-600">hide</button>
            <span className="text-xs text-gray-400">#</span>
            <span className="text-xs text-gray-400">|</span>
          </div>
          
          {/* 评论文本 */}
          <p className="text-gray-700 text-sm leading-relaxed mb-3">
            {comment.content}
          </p>
          
          {/* 操作按钮 */}
           <div className="flex items-center space-x-4">
             {/* 回复按钮 */}
             <button className="flex items-center space-x-1 text-xs text-gray-500 hover:text-gray-700 transition-colors">
               <FaReply className="w-3 h-3" />
               <span>回复</span>
             </button>
             
             {/* 回复数量 */}
             {comment.replies && comment.replies.length > 0 && (
               <span className="text-xs text-gray-500">
                 {comment.replies.length} 条回复
               </span>
             )}
           </div>
          
          {/* 回复列表 */}
          {comment.replies && comment.replies.map(reply => renderComment(reply, true))}
        </div>
      </div>
    </div>
  );

  return (
    <motion.section 
      className="max-w-4xl mx-auto px-4 pb-12"
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.8, delay: 0.6, ease: "easeOut" }}
    >
      <div className="text-center mb-12">
        <h3 className="text-pink-500 font-semibold text-lg uppercase tracking-wider mb-6">COMMENTS</h3>
      </div>
      
      {/* 评论表单 */}
      <div className="mb-12 bg-white rounded-lg border border-gray-200 p-6">
        <textarea
          className="w-full p-4 border border-gray-200 rounded-lg resize-none focus:ring-2 focus:ring-sky-200 focus:border-sky-300 transition-all duration-200 text-gray-700"
          rows={4}
          placeholder="写下你的评论..."
        />
        <div className="flex justify-end mt-4">
          <button className="px-6 py-3 bg-sky-100 text-gray-700 rounded-full hover:bg-sky-200 transition-colors font-medium">
            发表评论
          </button>
        </div>
      </div>
      
      {/* 评论列表 */}
      <div className="bg-white rounded-lg border border-gray-200">
        <div className="p-6 space-y-6">
          {comments.map(comment => renderComment(comment))}
        </div>
      </div>
    </motion.section>
  );
};

export default CommentSection;