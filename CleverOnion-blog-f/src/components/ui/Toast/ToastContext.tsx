import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { Toast, ToastOptions, ToastContextType, ToastType } from './types';

const ToastContext = createContext<ToastContextType | undefined>(undefined);

interface ToastProviderProps {
  children: ReactNode;
  defaultDuration?: number;
  maxToasts?: number;
}

export const ToastProvider: React.FC<ToastProviderProps> = ({ 
  children, 
  defaultDuration = 4000,
  maxToasts = 5 
}) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  // 生成唯一ID
  const generateId = useCallback(() => {
    return `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }, []);

  // 添加Toast
  const addToast = useCallback((message: string, options?: ToastOptions): string => {
    const id = generateId();
    const newToast: Toast = {
      id,
      type: options?.type || 'info',
      title: options?.title,
      message,
      duration: options?.duration ?? defaultDuration,
      action: options?.action,
      closable: options?.closable ?? true,
      createdAt: Date.now()
    };

    setToasts(prevToasts => {
      const updatedToasts = [newToast, ...prevToasts];
      // 限制最大Toast数量
      return updatedToasts.slice(0, maxToasts);
    });

    // 自动移除Toast（如果设置了duration）
    if (newToast.duration && newToast.duration > 0) {
      setTimeout(() => {
        removeToast(id);
      }, newToast.duration);
    }

    return id;
  }, [generateId, defaultDuration, maxToasts]);

  // 移除Toast
  const removeToast = useCallback((id: string) => {
    setToasts(prevToasts => prevToasts.filter(toast => toast.id !== id));
  }, []);

  // 清除所有Toast
  const clearAllToasts = useCallback(() => {
    setToasts([]);
  }, []);

  // 便捷方法
  const success = useCallback((message: string, options?: Omit<ToastOptions, 'type'>) => {
    return addToast(message, { ...options, type: 'success' });
  }, [addToast]);

  const error = useCallback((message: string, options?: Omit<ToastOptions, 'type'>) => {
    return addToast(message, { ...options, type: 'error' });
  }, [addToast]);

  const warning = useCallback((message: string, options?: Omit<ToastOptions, 'type'>) => {
    return addToast(message, { ...options, type: 'warning' });
  }, [addToast]);

  const info = useCallback((message: string, options?: Omit<ToastOptions, 'type'>) => {
    return addToast(message, { ...options, type: 'info' });
  }, [addToast]);

  const value: ToastContextType = {
    toasts,
    addToast,
    removeToast,
    clearAllToasts,
    success,
    error,
    warning,
    info
  };

  return (
    <ToastContext.Provider value={value}>
      {children}
    </ToastContext.Provider>
  );
};

export const useToast = (): ToastContextType => {
  const context = useContext(ToastContext);
  if (context === undefined) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

export default ToastContext;