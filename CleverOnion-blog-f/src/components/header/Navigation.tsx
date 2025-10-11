import React, { useState, useRef, useEffect } from "react";
import { Link, useLocation } from "react-router-dom";
import { FaCss3Alt, FaReact, FaJs, FaBriefcase } from "react-icons/fa";
import { MdAnimation, MdList } from "react-icons/md";
import { motion, AnimatePresence } from "framer-motion";
import categoryApi, { Category } from "../../api/categories";
import * as Icons from "react-icons/fa";
import * as MdIcons from "react-icons/md";

interface NavigationProps {
  isMobileMenuOpen: boolean;
  onMobileMenuClose: () => void;
}

const Navigation: React.FC<NavigationProps> = ({
  isMobileMenuOpen,
  onMobileMenuClose,
}) => {
  const location = useLocation();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isMobileCategoryOpen, setIsMobileCategoryOpen] = useState(false);
  const [categories, setCategories] = useState<Category[]>([]);
  const [categoriesLoading, setCategoriesLoading] = useState(true);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  // 获取图标组件
  const getIconComponent = (iconName?: string) => {
    if (!iconName) return MdList; // 默认图标

    // 尝试从 react-icons/fa 获取图标
    const FaIcon = (Icons as any)[iconName];
    if (FaIcon) return FaIcon;

    // 尝试从 react-icons/md 获取图标
    const MdIcon = (MdIcons as any)[iconName];
    if (MdIcon) return MdIcon;

    // 如果找不到图标，返回默认图标
    return MdList;
  };

  // 加载分类数据
  useEffect(() => {
    const loadCategories = async () => {
      try {
        setCategoriesLoading(true);
        const response = await categoryApi.getAllCategories();
        setCategories(response.categories);
      } catch (error) {
        console.error("加载分类失败:", error);
        // 如果API调用失败，使用默认分类数据
        setCategories([
          { id: 1, name: "CSS", icon: "FaCss3Alt" },
          { id: 2, name: "React", icon: "FaReact" },
          { id: 3, name: "Animation", icon: "MdAnimation" },
          { id: 4, name: "JavaScript", icon: "FaJs" },
          { id: 5, name: "Career", icon: "FaBriefcase" },
          { id: 6, name: "General", icon: "MdList" },
        ]);
      } finally {
        setCategoriesLoading(false);
      }
    };

    loadCategories();
  }, []);

  // 当移动端菜单关闭时，重置分类展开状态
  useEffect(() => {
    if (!isMobileMenuOpen) {
      setIsMobileCategoryOpen(false);
    }
  }, [isMobileMenuOpen]);

  const navigationItems = [
    { path: "/", label: "主页" },
    { path: "/category", label: "分类", hasDropdown: true },
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
                <button
                  type="button"
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 relative cursor-pointer ${
                    location.pathname.startsWith(item.path)
                      ? "text-blue-600 bg-blue-50"
                      : "text-gray-700 hover:text-blue-600 hover:bg-gray-50"
                  }`}
                  style={{ fontFamily: "'FZDaHei', sans-serif" }}
                  aria-haspopup="true"
                  aria-expanded={isDropdownOpen}
                >
                  {item.label}
                  {location.pathname.startsWith(item.path) && (
                    <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 w-1 h-1 bg-blue-600 rounded-full"></div>
                  )}
                </button>

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
                        {categoriesLoading
                          ? // 加载状态
                            Array.from({ length: 6 }).map((_, index) => (
                              <div
                                key={index}
                                className="flex items-center space-x-2 p-2"
                              >
                                <div className="w-4 h-4 bg-gray-200 rounded animate-pulse"></div>
                                <div className="h-4 bg-gray-200 rounded flex-1 animate-pulse"></div>
                              </div>
                            ))
                          : categories.map((category, index) => {
                              const IconComponent = getIconComponent(
                                category.icon
                              );
                              return (
                                <motion.div
                                  key={category.id}
                                  initial={{ opacity: 0, y: 10 }}
                                  animate={{ opacity: 1, y: 0 }}
                                  transition={{
                                    duration: 0.1,
                                    delay: index * 0.03,
                                  }}
                                >
                                  <Link
                                    to={`/category/${category.id}`}
                                    className="flex items-center space-x-2 p-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                                    onClick={() => {
                                      if (timeoutRef.current) {
                                        clearTimeout(timeoutRef.current);
                                        timeoutRef.current = null;
                                      }
                                      setIsDropdownOpen(false);
                                    }}
                                  >
                                    <IconComponent className="text-lg text-gray-600" />
                                    <span className="text-gray-800 font-medium text-sm">
                                      {category.name}
                                    </span>
                                  </Link>
                                </motion.div>
                              );
                            })}
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            ) : (
              <Link
                to={item.path}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 relative ${
                  (
                    item.path === "/"
                      ? isActive("/")
                      : location.pathname.startsWith(item.path)
                  )
                    ? "text-blue-600 bg-blue-50"
                    : "text-gray-700 hover:text-blue-600 hover:bg-gray-50"
                }`}
                style={{ fontFamily: "'FZDaHei', sans-serif" }}
              >
                {item.label}
                {(item.path === "/"
                  ? isActive("/")
                  : location.pathname.startsWith(item.path)) && (
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
              <div key={item.path}>
                {item.hasDropdown ? (
                  <div>
                    <button
                      type="button"
                      onClick={() =>
                        setIsMobileCategoryOpen(!isMobileCategoryOpen)
                      }
                      className={`w-full flex items-center justify-between px-4 py-3 rounded-lg text-base font-medium transition-all duration-200 ${
                        location.pathname.startsWith(item.path)
                          ? "text-blue-600 bg-blue-50"
                          : "text-gray-700 hover:text-blue-600 hover:bg-gray-50"
                      }`}
                      style={{ fontFamily: "'FZDaHei', sans-serif" }}
                      aria-expanded={isMobileCategoryOpen}
                    >
                      <span>{item.label}</span>
                      <svg
                        className={`w-5 h-5 transition-transform duration-200 ${
                          isMobileCategoryOpen ? "rotate-180" : ""
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

                    {/* Mobile Category List */}
                    <AnimatePresence>
                      {isMobileCategoryOpen && (
                        <motion.div
                          initial={{ opacity: 0, height: 0 }}
                          animate={{ opacity: 1, height: "auto" }}
                          exit={{ opacity: 0, height: 0 }}
                          transition={{ duration: 0.2 }}
                          className="overflow-hidden"
                        >
                          <div className="pl-4 pr-2 pt-2 space-y-1">
                            {categoriesLoading
                              ? Array.from({ length: 6 }).map((_, index) => (
                                  <div
                                    key={index}
                                    className="flex items-center space-x-3 px-4 py-2"
                                  >
                                    <div className="w-5 h-5 bg-gray-200 rounded animate-pulse"></div>
                                    <div className="h-4 bg-gray-200 rounded flex-1 animate-pulse"></div>
                                  </div>
                                ))
                              : categories.map((category) => {
                                  const IconComponent = getIconComponent(
                                    category.icon
                                  );
                                  return (
                                    <Link
                                      key={category.id}
                                      to={`/category/${category.id}`}
                                      className="flex items-center space-x-3 px-4 py-2 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                                      onClick={() => {
                                        setIsMobileCategoryOpen(false);
                                        onMobileMenuClose();
                                      }}
                                    >
                                      <IconComponent className="text-lg text-gray-600 flex-shrink-0" />
                                      <span className="text-gray-800 font-medium text-sm">
                                        {category.name}
                                      </span>
                                    </Link>
                                  );
                                })}
                          </div>
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </div>
                ) : (
                  <Link
                    to={item.path}
                    className={`block px-4 py-3 rounded-lg text-base font-medium transition-all duration-200 ${
                      (
                        item.path === "/"
                          ? isActive("/")
                          : location.pathname.startsWith(item.path)
                      )
                        ? "text-blue-600 bg-blue-50"
                        : "text-gray-700 hover:text-blue-600 hover:bg-gray-50"
                    }`}
                    style={{ fontFamily: "'FZDaHei', sans-serif" }}
                    onClick={onMobileMenuClose}
                  >
                    {item.label}
                  </Link>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </>
  );
};

export default Navigation;
