import React from 'react';
import { AdminSidebar, AdminHeader, AdminMain } from './admin';

const AdminLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* 侧边栏 */}
      <AdminSidebar />

      {/* 主要内容区域 */}
      <div className="flex-1 flex flex-col">
        {/* 顶部导航栏 */}
        <AdminHeader />

        {/* 主要内容 */}
        <AdminMain />
      </div>
    </div>
  );
};

export default AdminLayout;