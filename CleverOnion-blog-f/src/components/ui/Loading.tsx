import React from 'react';
import { FiLoader } from 'react-icons/fi';

/**
 * 通用Loading组件
 * 提供统一的加载状态UI，支持不同尺寸和样式
 */
interface LoadingProps {
  /** 加载状态文本 */
  text?: string;
  /** 组件尺寸 */
  size?: 'sm' | 'md' | 'lg';
  /** 是否显示为全屏遮罩 */
  overlay?: boolean;
  /** 自定义类名 */
  className?: string;
}

const Loading: React.FC<LoadingProps> = ({ 
  text = '加载中...', 
  size = 'md', 
  overlay = false, 
  className = '' 
}) => {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8'
  };

  const textSizeClasses = {
    sm: 'text-sm',
    md: 'text-base',
    lg: 'text-lg'
  };

  const LoadingContent = (
    <div className={`flex flex-col items-center justify-center space-y-3 ${className}`}>
      <FiLoader className={`${sizeClasses[size]} text-blue-600 animate-spin`} />
      <p className={`${textSizeClasses[size]} text-gray-600 font-medium`}>
        {text}
      </p>
    </div>
  );

  if (overlay) {
    return (
      <div className="fixed inset-0 bg-white bg-opacity-75 backdrop-blur-sm z-50 flex items-center justify-center">
        {LoadingContent}
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center py-12">
      {LoadingContent}
    </div>
  );
};

export { Loading };

/**
 * 内联Loading组件 - 用于按钮等小型组件内部
 */
export const InlineLoading: React.FC<{ size?: 'sm' | 'md' }> = ({ size = 'sm' }) => {
  const sizeClass = size === 'sm' ? 'w-4 h-4' : 'w-5 h-5';
  
  return (
    <FiLoader className={`${sizeClass} animate-spin`} />
  );
};

/**
 * 骨架屏Loading组件 - 用于列表项加载
 */
export const SkeletonLoading: React.FC<{ 
  rows?: number;
  className?: string;
}> = ({ rows = 3, className = '' }) => {
  return (
    <div className={`space-y-4 ${className}`}>
      {Array.from({ length: rows }).map((_, index) => (
        <div key={index} className="animate-pulse">
          <div className="flex space-x-4">
            <div className="rounded-full bg-gray-200 h-10 w-10"></div>
            <div className="flex-1 space-y-2 py-1">
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
              <div className="space-y-2">
                <div className="h-3 bg-gray-200 rounded"></div>
                <div className="h-3 bg-gray-200 rounded w-5/6"></div>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};