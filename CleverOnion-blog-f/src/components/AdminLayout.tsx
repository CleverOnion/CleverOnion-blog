import { Outlet, Link, useLocation } from 'react-router-dom';

const AdminLayout = () => {
  const location = useLocation();

  const menuItems = [
    { path: '/admin', label: '仪表盘', icon: '📊' },
    { path: '/admin/users', label: '用户管理', icon: '👥' },
    { path: '/admin/articles', label: '文章管理', icon: '📝' },
    { path: '/admin/categories', label: '分类管理', icon: '📂' },
    { path: '/admin/tags', label: '标签管理', icon: '🏷️' },
    { path: '/admin/editor', label: '发布文章', icon: '✍️' },
  ];

  return (
    <div className="min-h-screen bg-gray-100 flex">
      {/* 侧边栏 */}
      <aside className="w-64 bg-white shadow-lg">
        <div className="p-6">
          <h2 className="text-xl font-bold text-gray-800">管理后台</h2>
        </div>
        <nav className="mt-6">
          {menuItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`flex items-center px-6 py-3 text-gray-700 hover:bg-gray-50 hover:text-gray-900 ${
                location.pathname === item.path ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-700' : ''
              }`}
            >
              <span className="mr-3">{item.icon}</span>
              {item.label}
            </Link>
          ))}
        </nav>
      </aside>

      {/* 主要内容区域 */}
      <div className="flex-1 flex flex-col">
        {/* 顶部导航栏 */}
        <header className="bg-white shadow-sm border-b">
          <div className="px-6 py-4">
            <div className="flex justify-between items-center">
              <h1 className="text-2xl font-semibold text-gray-800">管理面板</h1>
              <div className="flex items-center space-x-4">
                <Link to="/" className="text-gray-600 hover:text-gray-900">
                  返回前台
                </Link>
                <button className="text-gray-600 hover:text-gray-900">
                  退出登录
                </button>
              </div>
            </div>
          </div>
        </header>

        {/* 主要内容 */}
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;