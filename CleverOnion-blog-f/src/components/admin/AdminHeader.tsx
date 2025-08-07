import React from 'react';
import { useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const AdminHeader: React.FC = () => {
  const { user } = useAuthStore();
  const location = useLocation();

  // 路径到页面名称的映射
  const getPageTitle = (pathname: string): string => {
    const pathMap: { [key: string]: string } = {
      '/admin': 'Dashboard',
      '/admin/users': '用户管理',
      '/admin/articles': '文章管理',
      '/admin/categories': '分类管理',
      '/admin/tags': '标签管理',
      '/admin/editor': '文章编辑器'
    };
    
    // 处理带参数的路径（如 /admin/editor/:articleId）
    if (pathname.startsWith('/admin/editor/')) {
      return '文章编辑器';
    }
    
    return pathMap[pathname] || 'Dashboard';
  };

  return (
    <header className="bg-white border-b border-gray-200 px-6 py-4 shadow-sm">
      <div className="flex justify-between items-center">
        {/* 左侧：页面标题和面包屑 */}
        <div className="flex items-center space-x-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900" style={{ fontFamily: 'Ubuntu, sans-serif' }}>
              {getPageTitle(location.pathname)}
            </h1>
            <p className="text-sm text-gray-500 mt-1">
              {new Date().toLocaleDateString('zh-CN', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                weekday: 'long'
              })}
            </p>
          </div>
        </div>

        {/* 右侧：用户信息 */}
        <div className="flex items-center space-x-4">
          <div className="text-right">
            <p className="text-sm font-medium text-gray-900">{user?.name || user?.githubLogin}</p>
            <p className="text-xs text-gray-500">管理员</p>
          </div>
          <img
            src={user?.avatarUrl || '/default-avatar.svg'}
            alt={user?.name || user?.githubLogin || 'User Avatar'}
            className="w-10 h-10 rounded-full border-2 border-gray-200 hover:border-gray-300 transition-colors object-cover"
          />
        </div>
      </div>
    </header>
  );
};

export default AdminHeader;