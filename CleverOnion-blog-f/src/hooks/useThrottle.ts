import { useCallback, useRef } from "react";

/**
 * useThrottle Hook
 * 节流函数，限制函数在指定时间内最多执行一次
 *
 * @param callback - 要节流的回调函数
 * @param delay - 节流延迟时间（毫秒）
 * @returns 节流后的函数
 */
export const useThrottle = <T extends (...args: any[]) => any>(
  callback: T,
  delay: number
): ((...args: Parameters<T>) => void) => {
  const timeoutRef = useRef<number | null>(null);
  const lastRunRef = useRef(Date.now());

  return useCallback(
    (...args: Parameters<T>) => {
      const now = Date.now();
      const timeSinceLastRun = now - lastRunRef.current;

      if (timeSinceLastRun >= delay) {
        callback(...args);
        lastRunRef.current = now;
      } else {
        if (timeoutRef.current !== null) {
          clearTimeout(timeoutRef.current);
        }
        timeoutRef.current = window.setTimeout(() => {
          callback(...args);
          lastRunRef.current = Date.now();
        }, delay - timeSinceLastRun);
      }
    },
    [callback, delay]
  );
};

export default useThrottle;
