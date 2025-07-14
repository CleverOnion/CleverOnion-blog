import { create } from 'zustand';

export interface Toast {
  id: string;
  message: string;
  type: 'error' | 'warning' | 'info' | 'success';
  duration?: number;
}

interface ToastState {
  toasts: Toast[];
  addToast: (toast: Omit<Toast, 'id'>) => void;
  removeToast: (id: string) => void;
  clearAllToasts: () => void;
}

export const useToastStore = create<ToastState>((set) => ({
  toasts: [],
  
  addToast: (toast) => {
    const id = Date.now().toString() + Math.random().toString(36).substr(2, 9);
    const newToast: Toast = {
      ...toast,
      id,
      duration: toast.duration || 5000
    };
    
    set((state) => ({
      toasts: [...state.toasts, newToast]
    }));
    
    // 自动移除toast
    setTimeout(() => {
      set((state) => ({
        toasts: state.toasts.filter(t => t.id !== id)
      }));
    }, newToast.duration);
  },
  
  removeToast: (id) => {
    set((state) => ({
      toasts: state.toasts.filter(toast => toast.id !== id)
    }));
  },
  
  clearAllToasts: () => {
    set({ toasts: [] });
  }
}));

// 便捷方法
export const showErrorToast = (message: string, duration?: number) => {
  useToastStore.getState().addToast({ message, type: 'error', duration });
};

export const showWarningToast = (message: string, duration?: number) => {
  useToastStore.getState().addToast({ message, type: 'warning', duration });
};

export const showInfoToast = (message: string, duration?: number) => {
  useToastStore.getState().addToast({ message, type: 'info', duration });
};

export const showSuccessToast = (message: string, duration?: number) => {
  useToastStore.getState().addToast({ message, type: 'success', duration });
};