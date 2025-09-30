import React from "react";
import { FiAlertCircle } from "react-icons/fi";

interface FormErrorMessageProps {
  /** 错误消息内容 */
  message: string;
  /** 是否显示错误 */
  show: boolean;
  /** 错误消息的唯一ID，用于 aria-describedby */
  id: string;
  /** 自定义类名 */
  className?: string;
}

/**
 * 表单错误消息组件
 * 提供无障碍访问支持和统一的错误提示样式
 */
export const FormErrorMessage: React.FC<FormErrorMessageProps> = ({
  message,
  show,
  id,
  className = "",
}) => {
  if (!show || !message) {
    return null;
  }

  return (
    <div
      id={id}
      role="alert"
      aria-live="polite"
      className={`flex items-start gap-2 mt-1.5 text-sm text-red-600 animate-in slide-in-from-top-2 ${className}`}
    >
      <FiAlertCircle
        className="w-4 h-4 flex-shrink-0 mt-0.5"
        aria-hidden="true"
      />
      <span>{message}</span>
    </div>
  );
};

export default FormErrorMessage;
