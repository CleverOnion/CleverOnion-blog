import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { FaCss3Alt, FaReact, FaJs, FaBriefcase } from 'react-icons/fa';
import { MdAnimation, MdList } from 'react-icons/md';
import { motion, AnimatePresence } from 'framer-motion';

interface FloatingCategoryIndicatorProps {
  isVisible: boolean;
}

const FloatingCategoryIndicator: React.FC<FloatingCategoryIndicatorProps> = ({ isVisible }) => {
  const { categoryId } = useParams();

  // 分类图标映射
  const categoryIcons: { [key: string]: { icon: React.ComponentType<any>, name: string, color: string } } = {
    'css': { icon: FaCss3Alt, name: 'CSS', color: 'text-blue-500' },
    'react': { icon: FaReact, name: 'React', color: 'text-cyan-500' },
    'javascript': { icon: FaJs, name: 'JavaScript', color: 'text-yellow-500' },
    'career': { icon: FaBriefcase, name: '职场', color: 'text-gray-600' },
    'animation': { icon: MdAnimation, name: '动画', color: 'text-purple-500' },
    'other': { icon: MdList, name: '其他', color: 'text-green-500' }
  };

  const currentCategory = categoryIcons[categoryId || 'other'] || categoryIcons['other'];
  const IconComponent = currentCategory.icon;

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
            <IconComponent className={`text-3xl ${currentCategory.color}`} />
            <div>
              <p className="text-base font-semibold text-gray-900">{currentCategory.name}</p>
              <p className="text-sm text-gray-500">当前分类</p>
            </div>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default FloatingCategoryIndicator;