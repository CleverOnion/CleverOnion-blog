import React from "react";
import { createPortal } from "react-dom";
import { AnimatePresence, motion } from "motion/react";
import { useToast } from "./useToast";
import Toast from "./Toast";
import { ToastContainerProps } from "./types";

/**
 * ToastContainer组件 - Toast容器
 * 负责管理Toast的显示位置和堆叠效果
 */
const ToastContainer: React.FC<ToastContainerProps> = ({
  position = "bottom-right",
  maxToasts = 5,
}) => {
  const { toasts, removeToast } = useToast();

  // 获取位置样式
  const getPositionStyles = () => {
    const baseStyles = "fixed z-50 pointer-events-none";

    switch (position) {
      case "top-right":
        return `${baseStyles} top-4 right-4`;
      case "top-left":
        return `${baseStyles} top-4 left-4`;
      case "top-center":
        return `${baseStyles} top-4 left-1/2 transform -translate-x-1/2`;
      case "bottom-left":
        return `${baseStyles} bottom-4 left-4`;
      case "bottom-center":
        return `${baseStyles} bottom-4 left-1/2 transform -translate-x-1/2`;
      case "bottom-right":
      default:
        return `${baseStyles} bottom-4 right-4`;
    }
  };

  // 获取堆叠方向
  const getStackDirection = () => {
    return position.includes("top") ? "flex-col" : "flex-col-reverse";
  };

  // 限制显示的Toast数量
  const visibleToasts = toasts.slice(0, maxToasts);

  // 如果没有Toast，不渲染容器
  if (visibleToasts.length === 0) {
    return null;
  }

  // 容器动画变体
  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
        delayChildren: 0.1,
      },
    },
  };

  // Toast项动画变体
  const itemVariants = {
    hidden: {
      opacity: 0,
      x: position.includes("right")
        ? 300
        : position.includes("left")
        ? -300
        : 0,
      y: position.includes("top") ? -50 : position.includes("bottom") ? 50 : 0,
      scale: 0.8,
    },
    visible: {
      opacity: 1,
      x: 0,
      y: 0,
      scale: 1,
      transition: {
        type: "spring" as const,
        stiffness: 300,
        damping: 30,
      },
    },
    exit: {
      opacity: 0,
      x: position.includes("right")
        ? 300
        : position.includes("left")
        ? -300
        : 0,
      y: position.includes("top") ? -50 : position.includes("bottom") ? 50 : 0,
      scale: 0.8,
      transition: {
        duration: 0.3,
        ease: "easeInOut" as const,
      },
    },
  };

  const containerContent = (
    <motion.div
      className={`${getPositionStyles()} flex ${getStackDirection()} gap-3 max-w-sm w-full`}
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      exit="hidden"
    >
      <AnimatePresence mode="popLayout">
        {visibleToasts.map((toast, index) => (
          <motion.div
            key={toast.id}
            variants={itemVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
            layout
            className="pointer-events-auto"
            style={{
              zIndex: visibleToasts.length - index, // 确保新的Toast在上层
            }}
            whileHover={{
              scale: 1.02,
              transition: { duration: 0.2 },
            }}
          >
            <Toast toast={toast} onRemove={removeToast} />
          </motion.div>
        ))}
      </AnimatePresence>
    </motion.div>
  );

  // 使用Portal渲染到body
  return createPortal(containerContent, document.body);
};

export default ToastContainer;
