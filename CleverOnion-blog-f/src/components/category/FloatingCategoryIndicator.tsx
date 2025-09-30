import React from "react";
import { Category as CategoryType } from "../../api/categories";
import * as Icons from "react-icons/fa";
import * as MdIcons from "react-icons/md";
import { MdList } from "react-icons/md";
import { motion, AnimatePresence } from "framer-motion";

interface FloatingCategoryIndicatorProps {
  isVisible: boolean;
  category: CategoryType | null;
}

const FloatingCategoryIndicator: React.FC<FloatingCategoryIndicatorProps> =
  React.memo(({ isVisible, category }) => {
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

    const IconComponent = getIconComponent(category?.icon);

    // 根据分类名称生成颜色
    const getCategoryColor = (categoryName?: string) => {
      if (!categoryName) return "text-gray-500";

      const colors = [
        "text-blue-500",
        "text-cyan-500",
        "text-yellow-500",
        "text-green-500",
        "text-purple-500",
        "text-pink-500",
        "text-indigo-500",
        "text-red-500",
        "text-orange-500",
      ];

      // 基于分类名称生成一致的颜色
      const hash = categoryName.split("").reduce((a, b) => {
        a = (a << 5) - a + b.charCodeAt(0);
        return a & a;
      }, 0);

      return colors[Math.abs(hash) % colors.length];
    };

    return (
      <AnimatePresence>
        {isVisible && (
          <motion.div
            initial={{ opacity: 0, x: 100, y: -20 }}
            animate={{ opacity: 1, x: 0, y: 0 }}
            exit={{ opacity: 0, x: 100, y: -20 }}
            transition={{ duration: 0.3, ease: "easeOut" }}
            className="fixed top-4 right-4 z-50"
          >
            <div className="bg-white rounded-xl shadow-lg border border-gray-200 px-6 py-4 flex items-center space-x-4 backdrop-blur-sm bg-white/95">
              <IconComponent
                className={`text-3xl ${getCategoryColor(category?.name)}`}
              />
              <div>
                <p className="text-base font-semibold text-gray-900">
                  {category?.name || "未知分类"}
                </p>
                <p className="text-sm text-gray-500">当前分类</p>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    );
  });

FloatingCategoryIndicator.displayName = "FloatingCategoryIndicator";

export default FloatingCategoryIndicator;
