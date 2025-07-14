import React, { useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';

interface ToastProps {
  message: string;
  isVisible: boolean;
  onClose: () => void;
  duration?: number;
  type?: 'error' | 'warning' | 'info' | 'success';
}

const Toast: React.FC<ToastProps> = ({ 
  message, 
  isVisible, 
  onClose, 
  duration = 5000,
  type = 'error'
}) => {
  useEffect(() => {
    if (isVisible) {
      const timer = setTimeout(() => {
        onClose();
      }, duration);
      return () => clearTimeout(timer);
    }
  }, [isVisible, duration, onClose]);

  const getTypeStyles = () => {
    switch (type) {
      case 'warning':
        return {
          bg: 'bg-white/95',
          border: 'border-amber-200',
          iconBg: 'bg-amber-100',
          icon: 'text-amber-600',
          text: 'text-gray-800',
          subtext: 'text-gray-600'
        };
      case 'info':
        return {
          bg: 'bg-white/95',
          border: 'border-blue-200',
          iconBg: 'bg-blue-100',
          icon: 'text-blue-600',
          text: 'text-gray-800',
          subtext: 'text-gray-600'
        };
      case 'success':
        return {
          bg: 'bg-white/95',
          border: 'border-green-200',
          iconBg: 'bg-green-100',
          icon: 'text-green-600',
          text: 'text-gray-800',
          subtext: 'text-gray-600'
        };
      default:
        return {
          bg: 'bg-white/95',
          border: 'border-red-200',
          iconBg: 'bg-red-100',
          icon: 'text-red-600',
          text: 'text-gray-800',
          subtext: 'text-gray-600'
        };
    }
  };

  const getIcon = () => {
    switch (type) {
      case 'warning':
        return (
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        );
      case 'info':
        return (
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        );
      case 'success':
        return (
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        );
      default:
        return (
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        );
    }
  };

  const styles = getTypeStyles();

  return (
    <AnimatePresence mode="wait">
      {isVisible && (
        <motion.div
          initial={{ x: 400, opacity: 0, scale: 0.8 }}
          animate={{ x: 0, opacity: 1, scale: 1 }}
          exit={{ x: 400, opacity: 0, scale: 0.8 }}
          transition={{ 
            type: 'spring', 
            stiffness: 300, 
            damping: 30,
            duration: 0.4
          }}
          className={`
            ${styles.bg} ${styles.border}
            backdrop-blur-sm rounded-2xl shadow-2xl
            p-4 min-w-80 max-w-md
            border-2
          `}
        >
          <div className="flex items-center gap-3">
            <div className={`${styles.iconBg} p-2 rounded-xl flex-shrink-0`}>
              <div className={styles.icon}>
                {getIcon()}
              </div>
            </div>
            
            <div className="flex-1 min-w-0 flex items-center justify-center">
              <p className={`${styles.text} text-sm font-bold leading-5`} style={{fontFamily: 'FZDaHei, sans-serif'}}>
                {message}
              </p>
            </div>
            
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors flex-shrink-0 ml-2"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </motion.button>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default Toast;