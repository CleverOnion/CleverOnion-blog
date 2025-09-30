import React, { Suspense } from "react";
import LoadingSpinner from "./LoadingSpinner";

/**
 * Suspense 包装组件
 * 为懒加载的路由组件提供统一的加载状态
 */
export const SuspenseWrapper: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => (
  <Suspense
    fallback={<LoadingSpinner fullScreen size="lg" message="页面加载中..." />}
  >
    {children}
  </Suspense>
);

export default SuspenseWrapper;
