import React, { useState, useRef } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { FaCss3Alt, FaReact, FaJs, FaBriefcase } from 'react-icons/fa';
import { MdAnimation, MdList } from 'react-icons/md';
import { motion, AnimatePresence } from 'framer-motion';

interface NavigationProps {
  isMobileMenuOpen: boolean;
  onMobileMenuClose: () => void;
}

const Navigation: React.FC<NavigationProps> = ({ isMobileMenuOpen, onMobileMenuClose }) => {
  const location = useLocation();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

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
          </div>
        </div>
      )}
    </>
  );
};

export default Navigation;