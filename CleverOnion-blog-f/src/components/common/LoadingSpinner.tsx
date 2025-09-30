import React from "react";

interface LoadingSpinnerProps {
  size?: "sm" | "md" | "lg" | "xl";
  color?: "blue" | "white" | "gray" | "green" | "red";
  className?: string;
  fullScreen?: boolean;
  message?: string;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = "md",
  color = "blue",
  className = "",
  fullScreen = false,
  message = "加载中...",
}) => {
  const getSizeClasses = () => {
    switch (size) {
      case "sm":
        return "w-4 h-4";
      case "md":
        return "w-6 h-6";
      case "lg":
        return "w-8 h-8";
      case "xl":
        return "w-12 h-12";
      default:
        return "w-6 h-6";
    }
  };

  const getColorClasses = () => {
    switch (color) {
      case "blue":
        return "border-blue-600 border-t-transparent";
      case "white":
        return "border-white border-t-transparent";
      case "gray":
        return "border-gray-600 border-t-transparent";
      case "green":
        return "border-green-600 border-t-transparent";
      case "red":
        return "border-red-600 border-t-transparent";
      default:
        return "border-blue-600 border-t-transparent";
    }
  };

  // 全屏加载模式
  if (fullScreen) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-white">
        <div
          className={`
            animate-spin rounded-full border-2
            ${getSizeClasses()}
            ${getColorClasses()}
            mb-4
          `}
          role="status"
          aria-label="加载中"
        >
          <span className="sr-only">{message}</span>
        </div>
        <p className="text-gray-600 text-sm">{message}</p>
      </div>
    );
  }

  // 常规模式
  return (
    <div
      className={`
        animate-spin rounded-full border-2
        ${getSizeClasses()}
        ${getColorClasses()}
        ${className}
      `}
      role="status"
      aria-label="加载中"
    >
      <span className="sr-only">{message}</span>
    </div>
  );
};

export default LoadingSpinner;
