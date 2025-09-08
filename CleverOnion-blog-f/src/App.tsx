import { RouterProvider } from 'react-router-dom';
import { router } from './router';
import { useEffect } from 'react';

import { LoadingProvider } from './contexts/LoadingContext';
import { ToastProvider, ToastContainer, useToast } from './components/ui/Toast';
import GlobalLoading from './components/ui/GlobalLoading';
import './App.css';

// 全局事件监听器组件
function GlobalEventListener() {
  const { error } = useToast();

  useEffect(() => {
    // 监听登录toast事件
    const handleLoginToast = (event: CustomEvent) => {
      const { message } = event.detail;
      error(message, {
        title: '需要登录',
        duration: 5000
      });
    };

    window.addEventListener('show-login-toast', handleLoginToast as EventListener);

    return () => {
      window.removeEventListener('show-login-toast', handleLoginToast as EventListener);
    };
  }, [error]);

  return null;
}

function App() {

  return (
    <LoadingProvider>
      <ToastProvider>
        <GlobalEventListener />
        <RouterProvider router={router} />

        <GlobalLoading />
        <ToastContainer position="bottom-right" maxToasts={5} />
      </ToastProvider>
    </LoadingProvider>
  );
}

export default App
