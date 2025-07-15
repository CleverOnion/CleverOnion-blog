import React, { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { authAPI } from '../api/auth';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  requireAdmin?: boolean;
  redirectTo?: string;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  requireAuth = true,
  requireAdmin = false,
  redirectTo = '/' 
}) => {
  const { isAuthenticated, isLoading } = useAuthStore();
  const [isAdmin, setIsAdmin] = useState(false);
  const [isCheckingAdmin, setIsCheckingAdmin] = useState(requireAdmin); // 如果需要管理员权限，初始状态为检查中
  const location = useLocation();

  // 检查管理员权限
  useEffect(() => {
    const checkAdminStatus = async () => {
      // 调试信息（生产环境可移除）
      if (import.meta.env.DEV) {
        console.log('ProtectedRoute - 检查管理员权限:', {
          requireAdmin,
          isAuthenticated,
          isLoading,
          shouldCheck: requireAdmin && isAuthenticated && !isLoading
        });
      }
      
      if (!requireAdmin) {
        // 如果不需要管理员权限，重置状态
        setIsAdmin(false);
        setIsCheckingAdmin(false);
        return;
      }
      
      if (!isAuthenticated || isLoading) {
        // 如果未认证或正在加载，重置管理员状态但保持检查状态
        setIsAdmin(false);
        return;
      }
      
      // 需要管理员权限且用户已认证且认证加载完成
      setIsCheckingAdmin(true);
      try {
        const adminStatus = await authAPI.checkAdminStatus();
        if (import.meta.env.DEV) {
           console.log('ProtectedRoute - 管理员权限检查结果:', adminStatus);
         }
        setIsAdmin(adminStatus);
      } catch (error) {
        console.error('检查管理员权限失败:', error);
        setIsAdmin(false);
      } finally {
        setIsCheckingAdmin(false);
      }
    };

    checkAdminStatus();
  }, [requireAdmin, isAuthenticated, isLoading]);

  // 如果正在加载认证状态或管理员权限，显示加载界面
  if (isLoading || (requireAdmin && isAuthenticated && isCheckingAdmin)) {
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

  // 如果需要管理员权限但用户不是管理员，重定向到首页
  // 注意：只有在管理员权限检查完成后才进行判断
  if (requireAdmin && isAuthenticated && !isCheckingAdmin && !isAdmin) {
    if (import.meta.env.DEV) {
      console.log('ProtectedRoute - 重定向到首页，用户不是管理员');
    }
    return <Navigate to="/" replace />;
  }

  // 如果不需要认证或用户已登录且权限满足，渲染子组件
  return <>{children}</>;
};

export default ProtectedRoute;