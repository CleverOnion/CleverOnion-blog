import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { ToastProps } from './types';

/**
 * Toast组件 - 单个通知消息
 * 支持不同类型的样式和动画效果
 */
const Toast: React.FC<ToastProps> = ({ toast, onRemove }) => {
  const [isVisible, setIsVisible] = useState(true);
  const [progress, setProgress] = useState(100);

  // 图标映射
  const getIcon = () => {
    switch (toast.type) {
      case 'success':
        return (
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        );
      case 'error':
        return (
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        );
      case 'warning':
        return (
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        );
      case 'info':
      default:
        return (
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        );
    }
  };

  // 样式映射
  const getStyles = () => {
    const baseStyles = 'flex items-start p-4 rounded-lg shadow-lg border backdrop-blur-sm';
    
    switch (toast.type) {
      case 'success':
        return `${baseStyles} bg-green-50/95 border-green-200 text-green-800`;
      case 'error':
        return `${baseStyles} bg-red-50/95 border-red-200 text-red-800`;
      case 'warning':
        return `${baseStyles} bg-yellow-50/95 border-yellow-200 text-yellow-800`;
      case 'info':
      default:
        return `${baseStyles} bg-blue-50/95 border-blue-200 text-blue-800`;
    }
  };

  // 图标颜色
  const getIconColor = () => {
    switch (toast.type) {
      case 'success':
        return 'text-green-500';
      case 'error':
        return 'text-red-500';
      case 'warning':
        return 'text-yellow-500';
      case 'info':
      default:
        return 'text-blue-500';
    }
  };

  // 进度条颜色
  const getProgressColor = () => {
    switch (toast.type) {
      case 'success':
        return 'bg-green-400';
      case 'error':
        return 'bg-red-400';
      case 'warning':
        return 'bg-yellow-400';
      case 'info':
      default:
        return 'bg-blue-400';
    }
  };

  // 处理关闭
  const handleClose = () => {
    setIsVisible(false);
    setTimeout(() => onRemove(toast.id), 300);
  };

  // 进度条动画
  useEffect(() => {
    if (toast.duration && toast.duration > 0) {
      const interval = setInterval(() => {
        setProgress(prev => {
          const elapsed = Date.now() - toast.createdAt;
          const remaining = Math.max(0, 100 - (elapsed / toast.duration!) * 100);
          return remaining;
        });
      }, 50);

      return () => clearInterval(interval);
    }
  }, [toast.duration, toast.createdAt]);

  return (
    <AnimatePresence>
      {isVisible && (
        <motion.div
          initial={{ opacity: 0, x: 300, scale: 0.8 }}
          animate={{ opacity: 1, x: 0, scale: 1 }}
          exit={{ opacity: 0, x: 300, scale: 0.8 }}
          transition={{ 
            type: "spring", 
            stiffness: 300, 
            damping: 30,
            duration: 0.4
          }}
          className={`${getStyles()} relative overflow-hidden max-w-sm w-full group hover:shadow-xl transition-shadow duration-200`}
          role="alert"
          aria-live="polite"
        >
          {/* 进度条 */}
          {toast.duration && toast.duration > 0 && (
            <div className="absolute bottom-0 left-0 right-0 h-1 bg-gray-200/50">
              <motion.div
                className={`h-full ${getProgressColor()}`}
                initial={{ width: '100%' }}
                animate={{ width: `${progress}%` }}
                transition={{ duration: 0.1, ease: 'linear' }}
              />
            </div>
          )}

          {/* 图标 */}
          <div className={`flex-shrink-0 ${getIconColor()} mr-3 mt-0.5`}>
            {getIcon()}
          </div>

          {/* 内容 */}
          <div className="flex-1 min-w-0">
            {toast.title && (
              <h4 className="text-sm font-semibold mb-1 truncate">
                {toast.title}
              </h4>
            )}
            <p className="text-sm leading-relaxed break-words">
              {toast.message}
            </p>
            
            {/* 操作按钮 */}
            {toast.action && (
              <button
                onClick={toast.action.onClick}
                className="mt-2 text-xs font-medium underline hover:no-underline focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-current rounded"
              >
                {toast.action.label}
              </button>
            )}
          </div>

          {/* 关闭按钮 */}
          {toast.closable && (
            <button
              onClick={handleClose}
              className="flex-shrink-0 ml-3 text-gray-400 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-current rounded transition-colors duration-150"
              aria-label="关闭通知"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          )}
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default Toast;