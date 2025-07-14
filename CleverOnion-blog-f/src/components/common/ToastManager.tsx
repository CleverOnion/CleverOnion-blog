import React from 'react';
import { AnimatePresence, motion } from 'motion/react';
import { useToastStore } from '../../store/toastStore';
import Toast from './Toast';

const ToastManager: React.FC = () => {
  const { toasts, removeToast } = useToastStore();

  return (
    <div className="fixed top-20 right-4 z-50 space-y-3">
      <AnimatePresence>
        {toasts.map((toast, index) => (
          <motion.div
            key={toast.id}
            initial={{ opacity: 0, y: -20, scale: 0.9 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -20, scale: 0.9 }}
            transition={{
              type: 'spring',
              stiffness: 400,
              damping: 25,
              delay: index * 0.1
            }}
          >
            <Toast
              message={toast.message}
              isVisible={true}
              onClose={() => removeToast(toast.id)}
              duration={toast.duration}
              type={toast.type}
            />
          </motion.div>
        ))}
      </AnimatePresence>
    </div>
  );
};

export default ToastManager;