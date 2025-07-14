import React from 'react';
import { useToastStore } from '../../store/toastStore';
import Toast from './Toast';

const ToastManager: React.FC = () => {
  const { toasts, removeToast } = useToastStore();

  return (
    <div className="fixed top-4 right-4 z-50 space-y-2">
      {toasts.map((toast) => (
        <Toast
          key={toast.id}
          message={toast.message}
          isVisible={true}
          onClose={() => removeToast(toast.id)}
          duration={toast.duration}
          type={toast.type}
        />
      ))}
    </div>
  );
};

export default ToastManager;