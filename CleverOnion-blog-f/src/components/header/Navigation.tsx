import React from 'react';
import { Link, useLocation } from 'react-router-dom';

interface NavigationProps {
  isMobileMenuOpen: boolean;
  onMobileMenuClose: () => void;
}

const Navigation: React.FC<NavigationProps> = ({ isMobileMenuOpen, onMobileMenuClose }) => {
  const location = useLocation();

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  const navigationItems = [
    { path: '/', label: '主页' },
    { path: '/category', label: '分类' }
  ];

  return (
    <>
      {/* Desktop Navigation */}
      <nav className="hidden md:flex items-center space-x-1">
        {navigationItems.map((item) => (
          <Link
            key={item.path}
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
          </div>
        </div>
      )}
    </>
  );
};

export default Navigation;