import { Link, useLocation } from 'react-router-dom';

interface MenuItem {
  path: string;
  label: string;
  icon: React.ReactNode;
}

interface MenuSection {
  title: string;
  items: MenuItem[];
}

const AdminSidebar = () => {
  const location = useLocation();

  const menuSections: MenuSection[] = [
    {
      title: "Overview",
      items: [
        { 
          path: '/admin', 
          label: '仪表盘', 
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          )
        },
      ]
    },
    {
      title: "Content Management",
      items: [
        { 
          path: '/admin/articles', 
          label: '文章管理', 
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          )
        },
        { 
          path: '/admin/categories', 
          label: '分类管理', 
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
          )
        },
        { 
          path: '/admin/tags', 
          label: '标签管理', 
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            </svg>
          )
        },
        { 
          path: '/admin/editor', 
          label: '发布文章', 
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
          )
        },
      ]
    },
    {
      title: "User Management",
      items: [
        { 
          path: '/admin/users', 
          label: '用户管理', 
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
            </svg>
          )
        },
      ]
    }
  ];

  return (
    <aside className="w-64 bg-white shadow-lg border-r border-gray-200">
      {/* 头部 */}
      <div className="p-6 border-b border-gray-100">
        <div className="flex items-center space-x-3 group">
          <div className="w-10 h-10 rounded-lg flex items-center justify-center cursor-pointer relative">
            <img 
              src="/src/assets/header/logo/onion.svg" 
              alt="CleverOnion Logo" 
              className="w-8 h-8 transition-all duration-500 group-hover:scale-125 group-hover:rotate-12 drop-shadow-lg animate-bounce"
              style={{
                filter: 'drop-shadow(0 4px 8px rgba(203, 113, 215, 0.3))',
                animationDuration: '3s',
                animationIterationCount: 'infinite',
                animationTimingFunction: 'ease-in-out'
              }}
            />
            {/* 装饰性光晕效果 */}
            <div className="absolute inset-0 rounded-lg bg-gradient-to-r from-purple-400 to-pink-400 opacity-0 group-hover:opacity-20 transition-opacity duration-300 blur-sm"></div>
          </div>
          <div className="transition-all duration-300">
            <h2 className="text-lg font-semibold text-gray-900 group-hover:text-purple-600 transition-colors duration-300">CleverOnion</h2>
            <p className="text-xs text-gray-500 group-hover:text-purple-400 transition-colors duration-300">Admin Panel</p>
          </div>
        </div>
      </div>

      {/* 导航菜单 */}
      <nav className="flex-1 py-4">
        {menuSections.map((section, sectionIndex) => (
          <div key={section.title} className={sectionIndex > 0 ? 'mt-6' : ''}>
            <div className="px-6 mb-2">
               <h3 className="text-xs font-semibold uppercase tracking-wider" style={{ color: 'var(--color-pink-500)' }}>
                 {section.title}
               </h3>
             </div>
            <div className="space-y-1">
              {section.items.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.path}
                    to={item.path}
                    className={`group flex items-center px-6 py-2.5 text-sm font-medium transition-all duration-200 ${
                      isActive
                        ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-600'
                        : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                    }`}
                  >
                    <span className={`mr-3 transition-colors duration-200 ${
                      isActive ? 'text-blue-600' : 'text-gray-400 group-hover:text-gray-600'
                    }`}>
                      {item.icon}
                    </span>
                    <span className="truncate">{item.label}</span>
                    {isActive && (
                      <span className="ml-auto w-1.5 h-1.5 bg-blue-600 rounded-full"></span>
                    )}
                  </Link>
                );
              })}
            </div>
          </div>
        ))}
      </nav>


    </aside>
  );
};

export default AdminSidebar;