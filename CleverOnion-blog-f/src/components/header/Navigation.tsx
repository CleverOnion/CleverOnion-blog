import React, { useState, useRef } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { FaCss3Alt, FaReact, FaJs, FaBriefcase } from 'react-icons/fa';
import { MdAnimation, MdList } from 'react-icons/md';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuthStore } from '../../store/authStore';
import { authAPI } from '../../api/auth';

interface NavigationProps {
  isMobileMenuOpen: boolean;
  onMobileMenuClose: () => void;
}

const Navigation: React.FC<NavigationProps> = ({ isMobileMenuOpen, onMobileMenuClose }) => {
  const location = useLocation();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const { isAuthenticated, user, logout } = useAuthStore();

  const handleGitHubLogin = () => {
    authAPI.loginWithGitHub();
  };

  const handleLogout = async () => {
    try {
      await logout();
      onMobileMenuClose();
    } catch (error) {
      console.error('登出失败:', error);
    }
  };

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  const categories = [
    { name: 'CSS', icon: FaCss3Alt, path: '/category/css' },
    { name: 'React', icon: FaReact, path: '/category/react' },
    { name: 'Animation', icon: MdAnimation, path: '/category/animation' },
    { name: 'JavaScript', icon: FaJs, path: '/category/javascript' },
    { name: 'Career', icon: FaBriefcase, path: '/category/career' },
    { name: 'General', icon: MdList, path: '/category/general' }
  ];

  const navigationItems = [
    { path: '/', label: '主页' },
    { path: '/category', label: '分类', hasDropdown: true }
  ];

  return (
    <>
      {/* Desktop Navigation */}
      <nav className="hidden md:flex items-center space-x-1">
        {navigationItems.map((item) => (
          <div key={item.path} className="relative">
            {item.hasDropdown ? (
              <div
                className="relative"
                onMouseEnter={() => {
                  if (timeoutRef.current) {
                    clearTimeout(timeoutRef.current);
                    timeoutRef.current = null;
                  }
                  setIsDropdownOpen(true);
                }}
                onMouseLeave={() => {
                   timeoutRef.current = setTimeout(() => {
                     setIsDropdownOpen(false);
                   }, 70);
                 }}
              >
                <Link
                  to={item.path}
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 relative ${
                    location.pathname.startsWith(item.path)
                      ? 'text-blue-600 bg-blue-50' 
                      : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
                  }`}
                  style={{ fontFamily: "'FZDaHei', sans-serif" }}
                >
                  {item.label}
                  {location.pathname.startsWith(item.path) && (
                    <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 w-1 h-1 bg-blue-600 rounded-full"></div>
                  )}
                </Link>
                
                {/* Dropdown Menu */}
                   <AnimatePresence>
                     {isDropdownOpen && (
                       <motion.div 
                         initial={{ opacity: 0, scale: 0.95, y: -10 }}
                         animate={{ opacity: 1, scale: 1, y: 0 }}
                         exit={{ opacity: 0, scale: 0.95, y: -10 }}
                         transition={{ duration: 0.15, ease: "easeOut" }}
                         className="absolute top-full left-1/2 transform -translate-x-1/2 mt-4 w-72 bg-white rounded-xl shadow-lg border border-gray-100 p-4 z-50"
                         onMouseEnter={() => {
                           if (timeoutRef.current) {
                             clearTimeout(timeoutRef.current);
                             timeoutRef.current = null;
                           }
                         }}
                         onMouseLeave={() => {
                            timeoutRef.current = setTimeout(() => {
                              setIsDropdownOpen(false);
                            }, 70);
                          }}
                       >
                        {/* Arrow pointing up */}
                        <div className="absolute -top-2 left-1/2 transform -translate-x-1/2 w-4 h-4 bg-white border-l border-t border-gray-100 rotate-45"></div>
                        <div className="grid grid-cols-2 gap-2">
                          {categories.map((category, index) => (
                            <motion.div
                              key={category.path}
                              initial={{ opacity: 0, y: 10 }}
                              animate={{ opacity: 1, y: 0 }}
                              transition={{ duration: 0.1, delay: index * 0.03 }}
                            >
                              <Link
                                 to={category.path}
                                 className="flex items-center space-x-2 p-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                                 onClick={() => {
                                   if (timeoutRef.current) {
                                     clearTimeout(timeoutRef.current);
                                     timeoutRef.current = null;
                                   }
                                   setIsDropdownOpen(false);
                                 }}
                               >
                                 <category.icon className="text-lg text-gray-600" />
                                 <span className="text-gray-800 font-medium text-sm">{category.name}</span>
                               </Link>
                            </motion.div>
                          ))}
                        </div>
                      </motion.div>
                    )}
                   </AnimatePresence>
              </div>
            ) : (
              <Link
                to={item.path}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 relative ${
                  (item.path === '/' ? isActive('/') : location.pathname.startsWith(item.path))
                    ? 'text-blue-600 bg-blue-50' 
                    : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
                }`}
                style={{ fontFamily: "'FZDaHei', sans-serif" }}
              >
                {item.label}
                {(item.path === '/' ? isActive('/') : location.pathname.startsWith(item.path)) && (
                  <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 w-1 h-1 bg-blue-600 rounded-full"></div>
                )}
              </Link>
            )}
          </div>
        ))}
      </nav>

      {/* Mobile Navigation */}
      {isMobileMenuOpen && (
        <div className="absolute top-full left-0 right-0 md:hidden border-t border-gray-100/30 bg-white/95 backdrop-blur-sm z-40">
          <div className="px-6 py-4 space-y-2">
            {navigationItems.map((item) => (
              <Link
                key={item.path}
                to={item.path}
                className={`block px-4 py-3 rounded-lg text-base font-medium transition-all duration-200 ${
                  (item.path === '/' ? isActive('/') : location.pathname.startsWith(item.path))
                    ? 'text-blue-600 bg-blue-50' 
                    : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
                }`}
                style={{ fontFamily: "'FZDaHei', sans-serif" }}
                onClick={onMobileMenuClose}
              >
                {item.label}
              </Link>
            ))}
            
            {/* Mobile Auth Section */}
            <div className="border-t border-gray-200 pt-4 mt-4">
              {isAuthenticated && user ? (
                <div className="space-y-2">
                  <div className="flex items-center space-x-3 px-4 py-3">
                    <img 
                      src={user.avatarUrl || '/default-avatar.svg'} 
                      alt={user.name || user.githubLogin || 'User'}
                      className="w-10 h-10 rounded-full"
                    />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-gray-900 truncate">
                        {user.name || user.githubLogin}
                      </p>
                      <p className="text-sm text-gray-500 truncate">
                        {user.email}
                      </p>
                    </div>
                  </div>
                  
                  <Link
                    to="/admin"
                    className="flex items-center px-4 py-3 text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-lg transition-colors"
                    onClick={onMobileMenuClose}
                  >
                    <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    管理后台
                  </Link>
                  
                  <button
                    onClick={handleLogout}
                    className="flex items-center w-full px-4 py-3 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                  >
                    <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                    退出登录
                  </button>
                </div>
              ) : (
                <button
                  onClick={handleGitHubLogin}
                  className="flex items-center justify-center w-full px-4 py-3 bg-gray-900 hover:bg-gray-800 text-white rounded-lg transition-all duration-200 font-medium"
                >
                  <svg className="w-5 h-5 mr-3" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                  </svg>
                  GitHub登录
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Navigation;