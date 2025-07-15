import React, { useEffect, useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { authAPI } from '../api/auth';

const AdminTest: React.FC = () => {
  const { user, isAuthenticated, isLoading } = useAuthStore();
  const [adminStatus, setAdminStatus] = useState<boolean | null>(null);
  const [isChecking, setIsChecking] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const checkAdminStatus = async () => {
    if (!isAuthenticated) {
      setError('用户未登录');
      return;
    }

    setIsChecking(true);
    setError(null);
    
    try {
      const result = await authAPI.checkAdminStatus();
      setAdminStatus(result);
      console.log('管理员状态检查结果:', result);
    } catch (err: any) {
      setError(`检查失败: ${err.message}`);
      console.error('管理员状态检查失败:', err);
    } finally {
      setIsChecking(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated && !isLoading) {
      checkAdminStatus();
    }
  }, [isAuthenticated, isLoading]);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p>正在加载认证状态...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold mb-4">请先登录</h1>
          <button 
            onClick={() => authAPI.loginWithGitHub()}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
          >
            GitHub 登录
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <h1 className="text-3xl font-bold mb-8 text-center">管理员权限测试页面</h1>
        
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">用户信息</h2>
          {user && (
            <div className="space-y-2">
              <p><strong>用户ID:</strong> {user.id}</p>
              <p><strong>GitHub ID:</strong> {user.githubId}</p>
              <p><strong>GitHub 用户名:</strong> {user.githubLogin}</p>
              <p><strong>姓名:</strong> {user.name || '未设置'}</p>
              <p><strong>邮箱:</strong> {user.email || '未设置'}</p>
            </div>
          )}
        </div>

        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">管理员权限检查</h2>
          
          <div className="flex items-center space-x-4 mb-4">
            <button
              onClick={checkAdminStatus}
              disabled={isChecking}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
              {isChecking ? '检查中...' : '重新检查管理员权限'}
            </button>
          </div>

          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          {adminStatus !== null && (
            <div className={`border px-4 py-3 rounded ${
              adminStatus 
                ? 'bg-green-100 border-green-400 text-green-700' 
                : 'bg-yellow-100 border-yellow-400 text-yellow-700'
            }`}>
              <strong>管理员状态:</strong> {adminStatus ? '是管理员' : '不是管理员'}
            </div>
          )}
        </div>

        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4">调试信息</h2>
          <div className="space-y-2 text-sm">
            <p><strong>认证状态:</strong> {isAuthenticated ? '已登录' : '未登录'}</p>
            <p><strong>加载状态:</strong> {isLoading ? '加载中' : '加载完成'}</p>
            <p><strong>Access Token:</strong> {authAPI.getAccessToken() ? '存在' : '不存在'}</p>
            <p><strong>当前时间:</strong> {new Date().toLocaleString()}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminTest;