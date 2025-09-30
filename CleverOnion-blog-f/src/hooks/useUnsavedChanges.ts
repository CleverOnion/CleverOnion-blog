import { useEffect, useCallback, useRef, useState } from "react";
import { useBlocker } from "react-router-dom";

/**
 * 未保存更改警告 Hook
 * 提供页面刷新/关闭和路由切换时的未保存更改提示
 */
export function useUnsavedChanges(
  hasUnsavedChanges: boolean,
  message: string = "您有未保存的更改，确定要离开吗？"
) {
  const messageRef = useRef(message);
  const [isBlocking, setIsBlocking] = useState(true);

  // 更新消息引用
  useEffect(() => {
    messageRef.current = message;
  }, [message]);

  /**
   * 处理页面刷新/关闭事件
   * 使用 beforeunload 事件提示用户
   */
  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (hasUnsavedChanges) {
        e.preventDefault();
        // 现代浏览器会忽略自定义消息，显示默认提示
        e.returnValue = messageRef.current;
        return messageRef.current;
      }
    };

    window.addEventListener("beforeunload", handleBeforeUnload);

    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [hasUnsavedChanges]);

  /**
   * 处理路由切换事件
   * 使用 react-router-dom 的 useBlocker 拦截路由变化
   */
  const blocker = useBlocker(({ currentLocation, nextLocation }) => {
    // 只在有未保存更改且路由真的要改变且拦截开启时才拦截
    return (
      isBlocking &&
      hasUnsavedChanges &&
      currentLocation.pathname !== nextLocation.pathname
    );
  });

  /**
   * 临时禁用拦截器
   * 用于在保存/发布操作后安全导航
   */
  const disableBlocking = useCallback(() => {
    setIsBlocking(false);
  }, []);

  /**
   * 重新启用拦截器
   */
  const enableBlocking = useCallback(() => {
    setIsBlocking(true);
  }, []);

  return {
    blocker,
    hasUnsavedChanges,
    disableBlocking,
    enableBlocking,
  };
}
