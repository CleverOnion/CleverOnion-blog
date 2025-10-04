import React, { useState, useEffect, useRef } from "react";
import { useLocation } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { AuthAPI } from "../../api/auth";

const AdminHeader: React.FC = () => {
  const location = useLocation();
  const [userInfo, setUserInfo] = useState<any>(null);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  // 获取用户信息
  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const userResponse = await AuthAPI.getUserInfo();
        setUserInfo(userResponse.user);
      } catch (error) {
        console.error("获取用户信息失败:", error);
        setUserInfo(null);
      }
    };

    fetchUserInfo();
  }, []);

  // 处理登出
  const handleLogout = async () => {
    try {
      await AuthAPI.logout();
      setUserInfo(null);
      setShowUserMenu(false);
      // 重定向到首页
      window.location.href = "/";
    } catch (error) {
      console.error("登出失败:", error);
    }
  };

  // 点击外部关闭菜单
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setShowUserMenu(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  // 路径到页面名称的映射
  const getPageTitle = (pathname: string): string => {
    const pathMap: { [key: string]: string } = {
      "/admin": "Dashboard",
      "/admin/users": "用户管理",
      "/admin/articles": "文章管理",
      "/admin/categories": "分类管理",
      "/admin/tags": "标签管理",
      "/admin/editor": "文章编辑器",
    };

    // 处理带参数的路径（如 /admin/editor/:articleId）
    if (pathname.startsWith("/admin/editor/")) {
      return "文章编辑器";
    }

    return pathMap[pathname] || "Dashboard";
  };

  return (
    <header className="bg-white border-b border-gray-200 px-6 py-4 shadow-sm">
      <div className="flex justify-between items-center">
        {/* 左侧：页面标题和面包屑 */}
        <div className="flex items-center space-x-4">
          <div>
            <h1
              className="text-2xl font-bold text-gray-900"
              style={{ fontFamily: "Ubuntu, sans-serif" }}
            >
              {getPageTitle(location.pathname)}
            </h1>
            <p className="text-sm text-gray-500 mt-1">
              {new Date().toLocaleDateString("zh-CN", {
                year: "numeric",
                month: "long",
                day: "numeric",
                weekday: "long",
              })}
            </p>
          </div>
        </div>

        {/* 右侧：用户信息 */}
        <div className="flex items-center space-x-4">
          {userInfo ? (
            <div className="relative" ref={menuRef}>
              <button
                onClick={() => setShowUserMenu(!showUserMenu)}
                className="flex items-center space-x-2 p-2 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
                aria-label="用户菜单"
              >
                <img
                  src={userInfo.avatarUrl || "/default-avatar.svg"}
                  alt={
                    userInfo.username || userInfo.name || userInfo.githubLogin
                  }
                  className="w-8 h-8 sm:w-9 sm:h-9 rounded-full border border-gray-300"
                />
                <span className="hidden sm:block text-sm font-medium text-gray-700 max-w-20 truncate">
                  {userInfo.username || userInfo.name || userInfo.githubLogin}
                </span>
                <svg
                  className={`w-4 h-4 text-gray-500 transition-transform ${
                    showUserMenu ? "rotate-180" : ""
                  }`}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </button>

              {/* 用户下拉菜单 */}
              <AnimatePresence>
                {showUserMenu && (
                  <motion.div
                    initial={{ opacity: 0, y: -5 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -5 }}
                    transition={{ duration: 0.15 }}
                    className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50"
                  >
                    <div className="px-3 py-2 border-b border-gray-100">
                      <p className="text-sm font-medium text-gray-900 truncate">
                        {userInfo.username ||
                          userInfo.name ||
                          userInfo.githubLogin}
                      </p>
                      <p className="text-xs text-gray-500 truncate">
                        ID: {userInfo.gitHubId}
                      </p>
                    </div>

                    <button
                      onClick={() => {
                        window.open("/", "_blank");
                        setShowUserMenu(false);
                      }}
                      className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-blue-50 hover:text-blue-700 transition-colors flex items-center space-x-2"
                    >
                      <svg
                        className="w-4 h-4 text-blue-600"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
                        />
                      </svg>
                      <span>返回前台</span>
                    </button>

                    <button
                      onClick={handleLogout}
                      className="w-full text-left px-3 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors flex items-center space-x-2"
                    >
                      <svg
                        className="w-4 h-4"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                        />
                      </svg>
                      <span>退出登录</span>
                    </button>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          ) : (
            <div className="flex items-center space-x-4">
              <div className="text-right">
                <p className="text-sm font-medium text-gray-900">未登录</p>
                <p className="text-xs text-gray-500">请先登录</p>
              </div>
              <img
                src="/default-avatar.svg"
                alt="Default Avatar"
                className="w-10 h-10 rounded-full border-2 border-gray-200 object-cover"
              />
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default AdminHeader;
