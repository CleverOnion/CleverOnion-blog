import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { showErrorToast, showSuccessToast } from '../store/toastStore';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { authAPI } from '../api/auth';
import type { AuthResponse } from '../api/auth';

const OAuthCallback: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { setAuthData } = useAuthStore();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [errorMessage, setErrorMessage] = useState<string>('');

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // 检查是否是初始登录请求（从登录按钮跳转过来）
        const action = searchParams.get('action');
        if (action === 'login') {
          // 立即重定向到GitHub授权页面
          authAPI.loginWithGitHub();
          return;
        }

        // 检查是否有错误参数
        const error = searchParams.get('error');
        const errorDescription = searchParams.get('error_description');
        
        if (error) {
          setStatus('error');
          const message = errorDescription || error;
          setErrorMessage(message);
          showErrorToast(`GitHub登录失败: ${message}`);
          return;
        }

        // 获取认证数据
        const accessToken = searchParams.get('access_token');
        const refreshToken = searchParams.get('refresh_token');
        const userDataStr = searchParams.get('user');

        if (!accessToken || !refreshToken || !userDataStr) {
          setStatus('error');
          const message = '认证数据不完整，请重新登录';
          setErrorMessage(message);
          showErrorToast(message);
          return;
        }

        // 解析用户数据
        let userData;
        try {
          userData = JSON.parse(decodeURIComponent(userDataStr));
        } catch (parseError) {
          setStatus('error');
          const message = '用户数据格式错误';
          setErrorMessage(message);
          showErrorToast(message);
          return;
        }

        // 构造认证响应数据
        const authData: AuthResponse = {
          accessToken,
          refreshToken,
          user: userData
        };

        // 更新认证状态
        setAuthData(authData);
        setStatus('success');
        showSuccessToast('登录成功！正在跳转...');

        // 延迟跳转，让用户看到成功提示
        setTimeout(() => {
          navigate('/', { replace: true });
        }, 1500);

      } catch (error) {
        console.error('OAuth callback error:', error);
        setStatus('error');
        const message = '登录过程中发生未知错误，请重试';
        setErrorMessage(message);
        showErrorToast(message);
      }
    };

    handleCallback();
  }, [searchParams, setAuthData, navigate]);

  // 错误状态处理
  const handleRetry = () => {
    navigate('/', { replace: true });
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-sky-300 to-white px-4 relative overflow-hidden">
      {/* 云朵背景装饰 */}
      <div className="absolute inset-0">
        <svg className="absolute inset-0 w-full h-full" viewBox="0 0 1200 800" preserveAspectRatio="xMidYMid slice">
          <g fill="rgb(200, 230, 250)" opacity="0.6">
            <circle cx="200" cy="150" r="80" />
            <circle cx="350" cy="120" r="60" />
            <circle cx="800" cy="180" r="70" />
            <circle cx="1000" cy="140" r="50" />
          </g>
          <g fill="rgb(230, 245, 255)" opacity="0.8">
            <circle cx="100" cy="600" r="120" />
            <circle cx="300" cy="650" r="100" />
            <circle cx="900" cy="620" r="110" />
            <circle cx="1100" cy="680" r="90" />
          </g>
          <g fill="rgb(255, 255, 255)" opacity="0.9">
            <circle cx="0" cy="750" r="150" />
            <circle cx="200" cy="780" r="130" />
            <circle cx="600" cy="760" r="140" />
            <circle cx="1000" cy="790" r="120" />
            <circle cx="1200" cy="770" r="110" />
          </g>
        </svg>
      </div>
      
      <div className="max-w-md w-full bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl border border-white/20 p-6 sm:p-8 relative z-10">
        {status === 'loading' && (
          <div className="text-center">
            <div className="flex justify-center mb-6">
              <LoadingSpinner size="xl" color="blue" />
            </div>
            <h2 className="text-2xl font-bold text-gray-800 mb-3 tracking-wide">
              {searchParams.get('action') === 'login' ? '正在跳转到GitHub...' : '正在处理登录...'}
            </h2>
            <p className="text-gray-600 text-lg">
              {searchParams.get('action') === 'login' 
                ? '即将跳转到GitHub授权页面，请稍候...' 
                : '请稍候，我们正在验证您的身份'
              }
            </p>
          </div>
        )}

        {status === 'success' && (
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-gradient-to-br from-green-400 to-green-600 mb-6 shadow-lg">
              <svg className="h-8 w-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-800 mb-3 tracking-wide">登录成功！</h2>
            <p className="text-gray-600 text-lg mb-4">欢迎回来，正在跳转到主页...</p>
            <div className="flex justify-center">
              <LoadingSpinner size="md" color="green" />
            </div>
          </div>
        )}

        {status === 'error' && (
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-gradient-to-br from-red-400 to-red-600 mb-6 shadow-lg">
              <svg className="h-8 w-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-800 mb-3 tracking-wide">登录失败</h2>
            <p className="text-gray-600 text-lg mb-6 break-words">{errorMessage}</p>
            <div className="space-y-3">
              <button
                onClick={handleRetry}
                className="w-full bg-gradient-to-r from-blue-500 to-blue-600 text-white py-3 px-6 rounded-xl font-semibold hover:from-blue-600 hover:to-blue-700 focus:outline-none focus:ring-4 focus:ring-blue-300 focus:ring-opacity-50 transition-all duration-300 transform hover:scale-105 shadow-lg hover:shadow-xl"
              >
                返回首页
              </button>
              <button
                onClick={() => window.location.reload()}
                className="w-full bg-gradient-to-r from-gray-100 to-gray-200 text-gray-700 py-3 px-6 rounded-xl font-semibold hover:from-gray-200 hover:to-gray-300 focus:outline-none focus:ring-4 focus:ring-gray-300 focus:ring-opacity-50 transition-all duration-300 transform hover:scale-105 shadow-lg hover:shadow-xl"
              >
                重新尝试
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default OAuthCallback;