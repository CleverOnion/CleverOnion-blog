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
    <header className="bg-gray-100 px-6 py-4">
      <div className="flex justify-between items-center">
        {/* 左侧：页面标题 */}
        <div className="flex flex-col items-start">
          <h1 className="text-2xl font-bold text-gray-900" style={{ fontFamily: 'Ubuntu, sans-serif' }}>{getPageTitle(location.pathname)}</h1>
          <p className="text-sm text-gray-500 mt-1">
            {new Date().toLocaleDateString('zh-CN', {
              year: 'numeric',
              month: 'long',
              day: 'numeric',
              weekday: 'long'
            })}
          </p>
        </div>

        {/* 右侧：用户头像 */}
        <div>
          <img
            src={user?.avatarUrl || '/default-avatar.svg'}
            alt={user?.name || user?.githubLogin || 'User Avatar'}
            className="w-12 h-12 rounded-full border-2 border-gray-300 hover:border-gray-400 transition-colors object-cover"
          />
        </div>
      </div>
    </header>
  );
};

export default AdminHeader;