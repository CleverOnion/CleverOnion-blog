import React from 'react';
import { Outlet } from 'react-router-dom';
import Header from './Header';

const Layout: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <Header />

      {/* 主要内容区域 */}
      <main className="py-8">
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