import React from 'react';

const AdminTest: React.FC = () => {
  // 模拟用户数据（用于保持UI样式）
  const mockUser = {
    id: "1",
    githubId: "123456",
    githubLogin: "admin",
    name: "管理员",
    email: "admin@example.com"
  };

  const mockAdminStatus = true;
  const mockIsAuthenticated = true;
  const mockIsLoading = false;
  const mockError = null;

  const handleCheckAdmin = () => {
    console.log('管理员权限检查功能已移除');
  };

  const handleLogin = () => {
    console.log('GitHub登录功能已移除');
  };

  if (mockIsLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p>正在加载认证状态...</p>
        </div>
      </div>
    );
  }

  if (!mockIsAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold mb-4">请先登录</h1>
          <button 
            onClick={handleLogin}
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
          {mockUser && (
            <div className="space-y-2">
              <p><strong>用户ID:</strong> {mockUser.id}</p>
              <p><strong>GitHub ID:</strong> {mockUser.githubId}</p>
              <p><strong>GitHub 用户名:</strong> {mockUser.githubLogin}</p>
              <p><strong>姓名:</strong> {mockUser.name || '未设置'}</p>
              <p><strong>邮箱:</strong> {mockUser.email || '未设置'}</p>
            </div>
          )}
        </div>

        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">管理员权限检查</h2>
          
          <div className="flex items-center space-x-4 mb-4">
            <button
              onClick={handleCheckAdmin}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
            >
              重新检查管理员权限
            </button>
          </div>

          {mockError && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {mockError}
            </div>
          )}

          {mockAdminStatus !== null && (
            <div className={`border px-4 py-3 rounded ${
              mockAdminStatus 
                ? 'bg-green-100 border-green-400 text-green-700' 
                : 'bg-yellow-100 border-yellow-400 text-yellow-700'
            }`}>
              <strong>管理员状态:</strong> {mockAdminStatus ? '是管理员' : '不是管理员'}
            </div>
          )}
        </div>

        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4">调试信息</h2>
          <div className="space-y-2 text-sm">
            <p><strong>认证状态:</strong> {mockIsAuthenticated ? '已登录' : '未登录'}</p>
            <p><strong>加载状态:</strong> {mockIsLoading ? '加载中' : '加载完成'}</p>
            <p><strong>Access Token:</strong> 存在（模拟）</p>
            <p><strong>当前时间:</strong> {new Date().toLocaleString()}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminTest;