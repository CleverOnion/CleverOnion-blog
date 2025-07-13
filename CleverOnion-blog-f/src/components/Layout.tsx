import React from 'react';
import { Outlet } from 'react-router-dom';
import Header from './Header';

const Layout: React.FC = () => {
  return (
    <div className="min-h-screen">
      {/* Header - 透明固定在顶部 */}
      <Header />

      {/* 主要内容区域 */}
      <main>
        <Outlet />
      </main>

      {/* 底部 */}
      <footer className="bg-white border-t mt-auto">
        <div className="py-6">
          <p className="text-center text-gray-600">
            © 2024 CleverOnion Blog. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
};

export default Layout;