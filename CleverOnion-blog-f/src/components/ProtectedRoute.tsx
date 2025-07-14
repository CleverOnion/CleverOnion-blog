import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  redirectTo?: string;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  requireAuth = true, 
  redirectTo = '/' 
}) => {
  const { isAuthenticated, isLoading } = useAuthStore();
  const location = useLocation();

  // 如果正在加载认证状态，显示加载界面
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  // 如果需要认证但用户未登录，重定向到指定页面
  if (requireAuth && !isAuthenticated) {
    return <Navigate to={redirectTo} state={{ from: location }} replace />;
  }

  // 如果不需要认证或用户已登录，渲染子组件
  return <>{children}</>;
};

export default ProtectedRoute;