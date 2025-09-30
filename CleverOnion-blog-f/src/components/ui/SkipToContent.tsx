import React from "react";

interface SkipToContentProps {
  targetId?: string;
  label?: string;
}

/**
 * 跳转到主内容链接
 * 提供无障碍访问支持，允许键盘用户快速跳过导航
 */
export const SkipToContent: React.FC<SkipToContentProps> = ({
  targetId = "main-content",
  label = "跳转到主内容",
}) => {
  const handleClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault();
    const target = document.getElementById(targetId);
    if (target) {
      target.focus();
      target.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  };

  return (
    <a
      href={`#${targetId}`}
      onClick={handleClick}
      className="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-50 focus:px-4 focus:py-2 focus:bg-blue-600 focus:text-white focus:rounded-lg focus:shadow-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
    >
      {label}
    </a>
  );
};

export default SkipToContent;
