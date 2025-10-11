import React, { useEffect, useState } from "react";
import { AdminSidebar, AdminHeader, AdminMain } from "./admin";
import { AuthUtils, AuthAPI } from "../api/auth";
import { useNavigate } from "react-router-dom";

const AdminLayout: React.FC = () => {
  const [isAuthorized, setIsAuthorized] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const checkAuth = async () => {
      try {
        // 检查是否已登录
        const token = AuthUtils.getAccessToken();
        const isTokenValid = AuthUtils.isTokenValid();

        if (!token || !isTokenValid) {
          console.warn("🔒 未登录，重定向到首页");
          navigate("/", { replace: true });
          return;
        }

        // 检查是否是管理员
        const adminStatus = await AuthAPI.checkAdminStatus();

        if (!adminStatus.isAdmin) {
          console.warn("⛔ 非管理员，无权访问后台");
          alert("您没有管理员权限，无法访问后台管理系统");
          navigate("/", { replace: true });
          return;
        }

        console.log("✅ 管理员身份验证通过");
        setIsAuthorized(true);
      } catch (error) {
        console.error("❌ 管理员权限验证失败:", error);
        alert("验证失败，请重新登录");
        navigate("/", { replace: true });
      } finally {
        setIsLoading(false);
      }
    };

    checkAuth();
  }, [navigate]);

  // 加载中状态
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-gray-600">正在验证权限...</p>
        </div>
      </div>
    );
  }

  // 未授权则不渲染内容
  if (!isAuthorized) {
    return null;
  }

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
