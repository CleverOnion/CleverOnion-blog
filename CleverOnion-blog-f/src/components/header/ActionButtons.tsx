import React, { useEffect, useState, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuthStore } from '../../store/authStore';
import { authAPI } from '../../api/auth';
import { showErrorToast } from '../../store/toastStore';

interface ActionButtonsProps {
  isMobileMenuOpen: boolean;
  onMobileMenuToggle: () => void;
}

const ActionButtons: React.FC<ActionButtonsProps> = ({ isMobileMenuOpen, onMobileMenuToggle }) => {
  const { isAuthenticated, user, logout, isLoading } = useAuthStore();
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  
  const buttonClass = "p-2.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all duration-200";

  // 清理定时器
  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  const handleGitHubLogin = () => {
    try {
      authAPI.loginWithGitHub();
    } catch (error) {
      showErrorToast('启动GitHub登录失败，请重试');
    }
  };

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await logout();
      setIsUserMenuOpen(false);
    } catch (error) {
      console.error('登出失败:', error);
    } finally {
      setIsLoggingOut(false);
    }
  };


  return (
    <div className="flex items-center space-x-1 sm:space-x-2">
        {/* Search Icon - 在小屏幕上隐藏 */}
        <button className={`${buttonClass} hidden sm:flex`} aria-label="搜索">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </button>

        {/* Sound Icon - 在小屏幕上隐藏 */}
        <button className={`${buttonClass} hidden md:flex`} aria-label="音频">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M9 12a3 3 0 106 0v-1a3 3 0 00-6 0v1z" />
          </svg>
        </button>

        {/* Theme Toggle */}
        <button className={buttonClass} aria-label="切换主题">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
          </svg>
        </button>

        {/* RSS Icon - 在小屏幕上隐藏 */}
        <button className={`${buttonClass} hidden sm:flex`} aria-label="RSS订阅">
          <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
            <path d="M6.503 20.752c0 1.794-1.456 3.248-3.251 3.248S0 22.546 0 20.752s1.456-3.248 3.252-3.248 3.251 1.454 3.251 3.248zM1.677 6.082v4.15c6.988 0 12.65 5.662 12.65 12.65h4.15c0-9.271-7.529-16.8-16.8-16.8zM1.677.014v4.151C14.727 4.165 25.836 15.274 25.85 28.324H30C29.986 13.19 14.811-0.001 1.677.014z"/>
          </svg>
        </button>

        {/* GitHub Login / User Menu */}
        {isAuthenticated && user ? (
          <div 
            className="relative"
            onMouseEnter={() => {
              if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
                timeoutRef.current = null;
              }
              setIsUserMenuOpen(true);
            }}
            onMouseLeave={() => {
              timeoutRef.current = setTimeout(() => {
                setIsUserMenuOpen(false);
              }, 70);
            }}
          >
            <div className="flex items-center space-x-1 sm:space-x-2 p-1.5 sm:p-2 rounded-lg hover:bg-blue-50 transition-all duration-200 cursor-pointer">
              <img 
                src={user.avatarUrl || '/default-avatar.svg'} 
                alt={user.name || user.githubLogin || 'User'}
                className="w-9 h-9 sm:w-10 sm:h-10 rounded-full border-2 border-black hover:border-gray-700 transition-colors"
              />
            </div>

            {/* User Dropdown Menu */}
            <AnimatePresence>
              {isUserMenuOpen && (
                <motion.div 
                  initial={{ opacity: 0, scale: 0.95, y: -10 }}
                  animate={{ opacity: 1, scale: 1, y: 0 }}
                  exit={{ opacity: 0, scale: 0.95, y: -10 }}
                  transition={{ duration: 0.15, ease: "easeOut" }}
                  className="absolute left-1/2 transform -translate-x-1/2 mt-4 w-56 sm:w-64 bg-white rounded-xl shadow-lg border border-gray-100 py-2 z-50"
                  onMouseEnter={() => {
                    if (timeoutRef.current) {
                      clearTimeout(timeoutRef.current);
                      timeoutRef.current = null;
                    }
                  }}
                  onMouseLeave={() => {
                    timeoutRef.current = setTimeout(() => {
                      setIsUserMenuOpen(false);
                    }, 70);
                  }}
                >
                  {/* Arrow pointing up */}
                  <div className="absolute -top-2 left-1/2 transform -translate-x-1/2 w-4 h-4 bg-white border-l border-t border-gray-100 rotate-45"></div>
                  
                  <motion.div 
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.1, delay: 0.03 }}
                    className="px-3 sm:px-4 py-3 border-b border-gray-100"
                  >
                    <div className="flex items-center space-x-2 sm:space-x-3">
                      <img 
                        src={user.avatarUrl || '/default-avatar.svg'} 
                        alt={user.name || user.githubLogin || 'User'}
                        className="w-8 h-8 sm:w-10 sm:h-10 rounded-full flex-shrink-0"
                      />
                      <div className="flex-1 min-w-0">
                        <p className="text-xs sm:text-sm font-medium text-gray-900 truncate">
                          {user.name || user.githubLogin}
                        </p>
                        <p className="text-xs sm:text-sm text-gray-500 truncate">
                          {user.email}
                        </p>
                      </div>
                    </div>
                  </motion.div>
                  
                  <div className="py-1">
                    <motion.div
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.1, delay: 0.06 }}
                    >
                      <a 
                        href="/admin" 
                        className="flex items-center px-3 sm:px-4 py-2 text-xs sm:text-sm text-gray-700 hover:bg-gray-50 rounded-lg mx-2 transition-colors duration-200"
                        onClick={() => {
                          if (timeoutRef.current) {
                            clearTimeout(timeoutRef.current);
                            timeoutRef.current = null;
                          }
                          setIsUserMenuOpen(false);
                        }}
                      >
                        <svg className="w-3 h-3 sm:w-4 sm:h-4 mr-2 sm:mr-3 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        </svg>
                        管理后台
                      </a>
                    </motion.div>
                    
                    <motion.div
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.1, delay: 0.09 }}
                    >
                      <button 
                        onClick={handleLogout}
                        disabled={isLoggingOut}
                        className="flex items-center w-full px-3 sm:px-4 py-2 text-xs sm:text-sm text-red-600 hover:bg-red-50 rounded-lg mx-2 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        {isLoggingOut ? (
                          <div className="w-3 h-3 sm:w-4 sm:h-4 mr-2 sm:mr-3 animate-spin rounded-full border-2 border-red-600 border-t-transparent flex-shrink-0"></div>
                        ) : (
                          <svg className="w-3 h-3 sm:w-4 sm:h-4 mr-2 sm:mr-3 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                          </svg>
                        )}
                        {isLoggingOut ? '退出中...' : '退出登录'}
                      </button>
                    </motion.div>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
        </div>
      ) : (
        <button 
          onClick={handleGitHubLogin}
          disabled={isLoading}
          className="flex items-center space-x-1 sm:space-x-2 px-2 sm:px-3 py-1.5 sm:py-2 bg-gray-900 hover:bg-gray-800 disabled:bg-gray-600 disabled:cursor-not-allowed text-white rounded-lg transition-all duration-200 text-xs sm:text-sm font-medium"
          aria-label="GitHub登录"
        >
          {isLoading ? (
            <div className="w-3 h-3 sm:w-4 sm:h-4 animate-spin rounded-full border-2 border-white border-t-transparent"></div>
          ) : (
            <svg className="w-3 h-3 sm:w-4 sm:h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
          )}
          <span className="hidden sm:inline">{isLoading ? '登录中...' : 'GitHub登录'}</span>
        </button>
      )}

        {/* Mobile menu button */}
        <button 
          className={`md:hidden ${buttonClass}`}
          onClick={onMobileMenuToggle}
          aria-label="菜单"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={isMobileMenuOpen ? "M6 18L18 6M6 6l12 12" : "M4 6h16M4 12h16M4 18h16"} />
          </svg>
        </button>
    </div>
  );
};

export default ActionButtons;