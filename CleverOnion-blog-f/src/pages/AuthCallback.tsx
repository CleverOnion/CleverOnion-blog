import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { AuthUtils, GitHubOAuth } from "../api/auth";

interface AuthCallbackProps {
  onSuccess?: () => void;
  onError?: (error: string) => void;
}

const AuthCallback: React.FC<AuthCallbackProps> = ({ onSuccess, onError }) => {
  const [status, setStatus] = useState<"loading" | "success" | "error">(
    "loading"
  );
  const [message, setMessage] = useState<string>("正在处理GitHub授权...");

  useEffect(() => {
    const handleAuthCallback = async () => {
      try {
        // 从URL参数中获取授权码和状态参数
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get("code");
        const state = urlParams.get("state");
        const error = urlParams.get("error");
        const errorDescription = urlParams.get("error_description");

        // 检查是否有错误参数
        if (error) {
          const errorMsg = errorDescription || `GitHub授权失败: ${error}`;
          setStatus("error");
          setMessage(errorMsg);
          onError?.(errorMsg);
          return;
        }

        // 检查是否有授权码
        if (!code) {
          const errorMsg = "未收到GitHub授权码";
          setStatus("error");
          setMessage(errorMsg);
          onError?.(errorMsg);
          return;
        }

        // 处理GitHub回调
        setMessage("正在验证授权信息...");
        const loginData = await GitHubOAuth.handleCallback(
          code,
          state || undefined
        );

        console.log("🎉 GitHub登录成功，获取到数据:", loginData);
        console.log("📋 数据类型检查:", {
          loginData: typeof loginData,
          accessToken: typeof loginData?.accessToken,
          userInfo: typeof loginData?.userInfo,
          expiresIn: typeof loginData?.expiresIn,
          hasAccessToken: !!loginData?.accessToken,
          hasUserInfo: !!loginData?.userInfo,
          accessTokenValue: loginData?.accessToken,
          userInfoValue: loginData?.userInfo,
        });

        // 检查数据完整性
        if (!loginData || !loginData.accessToken || !loginData.userInfo) {
          throw new Error("后端返回的登录数据不完整");
        }

        // 保存登录信息
        AuthUtils.saveLoginInfo(loginData);

        console.log("💾 登录信息已保存到localStorage");
        console.log("🔍 验证保存的数据:", {
          token: AuthUtils.getAccessToken(),
          user: AuthUtils.getUserInfo(),
          isValid: AuthUtils.isTokenValid(),
        });

        setStatus("success");
        setMessage("登录成功！正在跳转...");

        // 调用成功回调
        onSuccess?.();

        // 延迟跳转，让用户看到成功消息
        setTimeout(() => {
          // 清除URL参数
          window.history.replaceState(
            {},
            document.title,
            window.location.pathname
          );

          // 手动触发自定义事件，通知其他组件状态已更新
          window.dispatchEvent(new Event("authStatusChanged"));

          console.log("📢 已触发authStatusChanged事件");

          // 跳转到主页
          window.location.href = "/";
        }, 1500);
      } catch (error: any) {
        console.error("GitHub授权回调处理失败:", error);
        const errorMsg = error.message || "授权处理失败，请重试";
        setStatus("error");
        setMessage(errorMsg);
        onError?.(errorMsg);
      }
    };

    handleAuthCallback();
  }, [onSuccess, onError]);

  const handleRetry = () => {
    // 重新发起GitHub登录
    GitHubOAuth.initiateLogin().catch((error) => {
      console.error("重新发起登录失败:", error);
      setMessage("重新发起登录失败，请刷新页面重试");
    });
  };

  const handleGoHome = () => {
    window.location.href = "/";
  };

  return (
    <div className="min-h-screen relative bg-gradient-to-b from-sky-300 to-white overflow-hidden">
      {/* 主要内容 */}
      <main
        id="main-content"
        tabIndex={-1}
        className="relative z-10 min-h-screen flex items-center justify-center px-4 focus:outline-none"
      >
        <motion.div
          className="max-w-md w-full space-y-8"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, ease: "easeOut" }}
        >
          <div className="text-center">
            <motion.h1
              className="text-3xl font-bold text-gray-800 mb-2"
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              style={{ fontFamily: "Comic Sans MS, cursive, sans-serif" }}
            >
              GitHub 授权处理
            </motion.h1>
            <motion.p
              className="text-gray-600"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.6, delay: 0.4 }}
              style={{ fontFamily: "Comic Sans MS, cursive, sans-serif" }}
            >
              正在为您完成登录流程
            </motion.p>
          </div>

          <motion.div
            className="bg-white/90 backdrop-blur-sm shadow-lg rounded-2xl p-8 border-2 border-gray-200"
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5, delay: 0.3 }}
          >
            <div className="text-center">
              {status === "loading" && (
                <motion.div
                  className="space-y-6"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ duration: 0.4 }}
                >
                  <div className="relative">
                    <div className="animate-spin rounded-full h-16 w-16 border-4 border-sky-200 border-t-sky-500 mx-auto"></div>
                    <div className="absolute inset-0 rounded-full h-16 w-16 border-4 border-transparent border-t-sky-300 mx-auto animate-ping"></div>
                  </div>
                  <p
                    className="text-gray-600 font-medium"
                    style={{ fontFamily: "Comic Sans MS, cursive, sans-serif" }}
                  >
                    {message}
                  </p>
                </motion.div>
              )}

              {status === "success" && (
                <motion.div
                  className="space-y-6"
                  initial={{ opacity: 0, scale: 0.8 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ duration: 0.5, ease: "easeOut" }}
                >
                  <motion.div
                    className="relative"
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    transition={{
                      duration: 0.6,
                      delay: 0.2,
                      type: "spring",
                      stiffness: 200,
                    }}
                  >
                    <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
                      <svg
                        className="w-8 h-8 text-green-600"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={3}
                          d="M5 13l4 4L19 7"
                        />
                      </svg>
                    </div>
                    <div className="absolute inset-0 w-16 h-16 bg-green-200 rounded-full mx-auto animate-ping opacity-20"></div>
                  </motion.div>
                  <motion.p
                    className="text-green-700 font-semibold text-lg"
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.4, delay: 0.4 }}
                    style={{ fontFamily: "Comic Sans MS, cursive, sans-serif" }}
                  >
                    {message}
                  </motion.p>
                </motion.div>
              )}

              {status === "error" && (
                <motion.div
                  className="space-y-6"
                  initial={{ opacity: 0, scale: 0.8 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ duration: 0.5, ease: "easeOut" }}
                >
                  <motion.div
                    className="relative"
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    transition={{
                      duration: 0.6,
                      delay: 0.2,
                      type: "spring",
                      stiffness: 200,
                    }}
                  >
                    <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto">
                      <svg
                        className="w-8 h-8 text-red-600"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={3}
                          d="M6 18L18 6M6 6l12 12"
                        />
                      </svg>
                    </div>
                    <div className="absolute inset-0 w-16 h-16 bg-red-200 rounded-full mx-auto animate-ping opacity-20"></div>
                  </motion.div>
                  <motion.p
                    className="text-red-700 font-semibold text-lg"
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.4, delay: 0.4 }}
                    style={{ fontFamily: "Comic Sans MS, cursive, sans-serif" }}
                  >
                    {message}
                  </motion.p>
                  <motion.div
                    className="flex flex-col sm:flex-row gap-3 justify-center"
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.4, delay: 0.6 }}
                  >
                    <button
                      onClick={handleRetry}
                      className="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-300 transition-all duration-300 font-medium shadow-md"
                      style={{
                        fontFamily: "Comic Sans MS, cursive, sans-serif",
                      }}
                    >
                      重新登录
                    </button>
                    <button
                      onClick={handleGoHome}
                      className="px-6 py-3 bg-gray-500 text-white rounded-lg hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-300 transition-all duration-300 font-medium shadow-md"
                      style={{
                        fontFamily: "Comic Sans MS, cursive, sans-serif",
                      }}
                    >
                      返回首页
                    </button>
                  </motion.div>
                </motion.div>
              )}
            </div>
          </motion.div>

          <motion.div
            className="text-center"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.6, delay: 0.8 }}
          >
            <p
              className="text-sm text-gray-600 bg-white/70 backdrop-blur-sm rounded-lg px-4 py-2 inline-block border border-gray-300"
              style={{ fontFamily: "Comic Sans MS, cursive, sans-serif" }}
            >
              如果页面长时间无响应，请刷新页面重试
            </p>
          </motion.div>
        </motion.div>
      </main>
    </div>
  );
};

export default AuthCallback;
