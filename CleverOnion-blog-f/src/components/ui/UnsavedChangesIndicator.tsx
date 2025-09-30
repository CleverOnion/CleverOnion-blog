import React from "react";
import { FiCircle } from "react-icons/fi";

interface UnsavedChangesIndicatorProps {
  hasUnsavedChanges: boolean;
  className?: string;
}

/**
 * 未保存更改指示器
 * 在工具栏显示未保存状态
 */
export const UnsavedChangesIndicator: React.FC<
  UnsavedChangesIndicatorProps
> = ({ hasUnsavedChanges, className = "" }) => {
  if (!hasUnsavedChanges) {
    return null;
  }

  return (
    <div
      className={`flex items-center gap-2 text-sm text-gray-600 ${className}`}
      role="status"
      aria-live="polite"
      aria-label="有未保存的更改"
    >
      <FiCircle
        className="w-2 h-2 fill-yellow-500 text-yellow-500 animate-pulse"
        aria-hidden="true"
      />
      <span>未保存</span>
    </div>
  );
};

export default UnsavedChangesIndicator;
