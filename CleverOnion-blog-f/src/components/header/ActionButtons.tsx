import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { GitHubOAuth, AuthUtils, AuthAPI, type UserInfo } from "../../api/auth";
import { useToast } from "../ui/Toast";
import { soundManager, PresetNames } from "../../utils/sound";
import { FaVolumeMute, FaVolumeDown, FaVolumeUp } from "react-icons/fa";
import { MdWarning } from "react-icons/md";

interface ActionButtonsProps {
  isMobileMenuOpen: boolean;
  onMobileMenuToggle: () => void;
}

const ActionButtons: React.FC<ActionButtonsProps> = ({
  isMobileMenuOpen,
  onMobileMenuToggle,
}) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [showSoundMenu, setShowSoundMenu] = useState(false);
  const [soundConfig, setSoundConfig] = useState(soundManager.getConfig());
  const toast = useToast();
  const soundMenuRef = React.useRef<HTMLDivElement>(null);

  const buttonClass =
    "p-2.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all duration-200 cursor-pointer hover:scale-105";

  // 检查登录状态
  useEffect(() => {
    // 检查管理员状态
    const checkAdminStatus = async () => {
      if (!isLoggedIn) {
        setIsAdmin(false);
        return;
      }

      try {
        const adminStatus = await AuthAPI.checkAdminStatus();
        console.log("🔍 管理员权限检查结果:", adminStatus);
        setIsAdmin(adminStatus.isAdmin);
      } catch (error) {
        console.error("检查管理员权限失败:", error);
        setIsAdmin(false);
      }
    };
    const checkAuthStatus = () => {
      const token = AuthUtils.getAccessToken();
      const user = AuthUtils.getUserInfo();
      const isTokenValid = AuthUtils.isTokenValid();

      console.log("🔍 检查认证状态:", {
        hasToken: !!token,
        hasUser: !!user,
        isTokenValid,
        user: user,
      });

      if (token && user && isTokenValid) {
        console.log("✅ 用户已登录:", user);
        setIsLoggedIn(true);
        setUserInfo(user);
      } else {
        console.log("❌ 用户未登录或token无效");
        setIsLoggedIn(false);
        setUserInfo(null);
        setIsAdmin(false);
        // 不在这里调用clearLoginInfo，避免无限递归
      }
    };

    checkAuthStatus();

    // 当登录状态变化时检查管理员权限
    if (isLoggedIn) {
      checkAdminStatus();
    }

    // 监听存储变化
    const handleStorageChange = () => {
      checkAuthStatus();
    };

    // 监听storage事件（跨窗口）和自定义事件（同窗口）
    window.addEventListener("storage", handleStorageChange);
    window.addEventListener("authStatusChanged", handleStorageChange);

    return () => {
      window.removeEventListener("storage", handleStorageChange);
      window.removeEventListener("authStatusChanged", handleStorageChange);
    };
  }, [isLoggedIn]);

  // 点击外部关闭音效菜单
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        soundMenuRef.current &&
        !soundMenuRef.current.contains(event.target as Node)
      ) {
        setShowSoundMenu(false);
      }
    };

    if (showSoundMenu) {
      document.addEventListener("mousedown", handleClickOutside);
      return () => {
        document.removeEventListener("mousedown", handleClickOutside);
      };
    }
  }, [showSoundMenu]);

  // 处理GitHub登录
  const handleGitHubLogin = async () => {
    try {
      setIsLoggingIn(true);
      await GitHubOAuth.initiateLogin();
    } catch (error: any) {
      console.error("GitHub登录失败:", error);
      toast.error(error.message || "登录失败，请重试");
    } finally {
      setIsLoggingIn(false);
    }
  };

  // 处理登出
  const handleLogout = async () => {
    try {
      await GitHubOAuth.logout();
      setIsLoggedIn(false);
      setUserInfo(null);
      setShowUserMenu(false);
      // 可选：刷新页面或重定向
      window.location.reload();
    } catch (error: any) {
      console.error("登出失败:", error);
      // 即使登出失败，也清除本地状态
      setIsLoggedIn(false);
      setUserInfo(null);
      setShowUserMenu(false);
    }
  };

  return (
    <div className="flex items-center space-x-1 sm:space-x-2">
      {/* 音效设置下拉菜单 */}
      <div className="relative" ref={soundMenuRef}>
        <button
          onClick={() => setShowSoundMenu(!showSoundMenu)}
          className={buttonClass}
          aria-label="音效设置"
          title="音效设置"
        >
          <svg
            className="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            {soundConfig.master.enabled ? (
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z"
              />
            ) : (
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15zM17 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2"
              />
            )}
          </svg>
        </button>

        {/* 音效设置下拉框 */}
        <AnimatePresence>
          {showSoundMenu && (
            <motion.div
              initial={{ opacity: 0, y: -5 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -5 }}
              transition={{ duration: 0.15 }}
              className="absolute right-0 mt-2 w-72 bg-white rounded-lg shadow-lg border border-gray-200 py-3 z-50"
            >
              {/* 标题 */}
              <div className="px-4 pb-3 border-b border-gray-100">
                <h3 className="text-sm font-semibold text-gray-900">
                  音效设置
                </h3>
              </div>

              <div className="px-4 py-3 space-y-4">
                {/* 全局开关 */}
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-700">启用音效</span>
                  <button
                    type="button"
                    role="switch"
                    aria-checked={soundConfig.master.enabled}
                    onClick={() => {
                      soundManager.setEnabled(!soundConfig.master.enabled);
                      setSoundConfig(soundManager.getConfig());
                    }}
                    className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 ${
                      soundConfig.master.enabled ? "bg-blue-600" : "bg-gray-200"
                    }`}
                  >
                    <span
                      className={`inline-block h-3 w-3 transform rounded-full bg-white transition-transform ${
                        soundConfig.master.enabled
                          ? "translate-x-5"
                          : "translate-x-1"
                      }`}
                    />
                  </button>
                </div>

                {/* 音量控制 */}
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <label
                      htmlFor="volume-slider"
                      className="text-sm text-gray-700"
                    >
                      音量
                    </label>
                    <span className="text-sm text-gray-500">
                      {soundConfig.master.volume}%
                    </span>
                  </div>
                  <input
                    id="volume-slider"
                    type="range"
                    min="0"
                    max="100"
                    value={soundConfig.master.volume}
                    onChange={(e) => {
                      const volume = Number(e.target.value);
                      soundManager.setMasterVolume(volume);
                      setSoundConfig(soundManager.getConfig());
                      if (volume > 0) {
                        soundManager.play("api.success.post", { force: true });
                      }
                    }}
                    disabled={!soundConfig.master.enabled}
                    className="w-full h-1.5 bg-gray-200 rounded-lg appearance-none cursor-pointer disabled:cursor-not-allowed disabled:opacity-50"
                  />
                </div>

                {/* 预设方案 */}
                <div className="space-y-2">
                  <label className="text-sm text-gray-700">快速预设</label>
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      onClick={() => {
                        soundManager.loadPreset(PresetNames.Silent);
                        setSoundConfig(soundManager.getConfig());
                      }}
                      className="flex items-center justify-center gap-1.5 px-3 py-1.5 text-xs bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
                    >
                      <FaVolumeMute className="w-3 h-3" />
                      <span>静音</span>
                    </button>
                    <button
                      onClick={() => {
                        soundManager.loadPreset(PresetNames.ErrorsOnly);
                        setSoundConfig(soundManager.getConfig());
                      }}
                      className="flex items-center justify-center gap-1.5 px-3 py-1.5 text-xs bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
                    >
                      <MdWarning className="w-3.5 h-3.5" />
                      <span>仅错误</span>
                    </button>
                    <button
                      onClick={() => {
                        soundManager.loadPreset(PresetNames.Minimal);
                        setSoundConfig(soundManager.getConfig());
                        soundManager.play("api.success.post", { force: true });
                      }}
                      className="flex items-center justify-center gap-1.5 px-3 py-1.5 text-xs bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
                    >
                      <FaVolumeDown className="w-3 h-3" />
                      <span>极简</span>
                    </button>
                    <button
                      onClick={() => {
                        soundManager.loadPreset(PresetNames.Full);
                        setSoundConfig(soundManager.getConfig());
                        soundManager.play("api.success.post", { force: true });
                      }}
                      className="flex items-center justify-center gap-1.5 px-3 py-1.5 text-xs bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
                    >
                      <FaVolumeUp className="w-3 h-3" />
                      <span>完整</span>
                    </button>
                  </div>
                </div>
              </div>

              {/* 底部提示 */}
              <div className="px-4 pt-3 border-t border-gray-100">
                <p className="text-xs text-gray-500">设置自动保存在本地</p>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* GitHub登录/用户信息 */}
      {isLoggedIn && userInfo ? (
        <div className="relative">
          <button
            onClick={() => setShowUserMenu(!showUserMenu)}
            className="flex items-center space-x-2 p-2 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
            aria-label="用户菜单"
          >
            <img
              src={userInfo.avatarUrl || "/default-avatar.svg"}
              alt={userInfo.username}
              className="w-8 h-8 sm:w-9 sm:h-9 rounded-full border border-gray-300"
            />
            <span className="hidden sm:block text-sm font-medium text-gray-700 max-w-20 truncate">
              {userInfo.username}
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
                    {userInfo.username}
                  </p>
                  <p className="text-xs text-gray-500 truncate">
                    ID: {userInfo.gitHubId}
                  </p>
                </div>

                {isAdmin && (
                  <button
                    onClick={() => {
                      window.open("/admin", "_blank");
                      setShowUserMenu(false);
                    }}
                    className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-blue-50 hover:text-blue-700 transition-colors flex items-center space-x-2 cursor-pointer"
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
                        d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
                      />
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                      />
                    </svg>
                    <span>管理后台</span>
                  </button>
                )}

                <button
                  onClick={handleLogout}
                  className="w-full text-left px-3 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors flex items-center space-x-2 cursor-pointer"
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
        <button
          onClick={handleGitHubLogin}
          disabled={isLoggingIn}
          className="flex items-center space-x-2 px-3 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 hover:scale-105 transition-all duration-200 ease-in-out cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100 disabled:hover:bg-gray-900"
          aria-label="GitHub登录"
        >
          {isLoggingIn ? (
            <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
          ) : (
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z" />
            </svg>
          )}
          <span className="hidden sm:block text-sm font-medium">
            {isLoggingIn ? "登录中..." : "GitHub登录"}
          </span>
        </button>
      )}

      {/* Mobile menu button */}
      <button
        className={`md:hidden ${buttonClass}`}
        onClick={onMobileMenuToggle}
        aria-label="菜单"
      >
        <svg
          className="w-5 h-5"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d={
              isMobileMenuOpen
                ? "M6 18L18 6M6 6l12 12"
                : "M4 6h16M4 12h16M4 18h16"
            }
          />
        </svg>
      </button>
    </div>
  );
};

export default ActionButtons;
