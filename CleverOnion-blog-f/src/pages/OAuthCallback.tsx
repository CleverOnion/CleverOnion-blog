import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { showErrorToast, showSuccessToast } from '../store/toastStore';
import LoadingSpinner from '../components/common/LoadingSpinner';
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
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-md p-6 sm:p-8">
        {status === 'loading' && (
          <div className="text-center">
            <LoadingSpinner size="xl" className="mx-auto mb-4" />
            <h2 className="text-lg sm:text-xl font-semibold text-gray-900 mb-2">正在处理登录...</h2>
            <p className="text-sm sm:text-base text-gray-600">请稍候，我们正在验证您的GitHub账户</p>
          </div>
        )}

        {status === 'success' && (
          <div className="text-center">
            <div className="w-12 h-12 sm:w-16 sm:h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-6 h-6 sm:w-8 sm:h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-lg sm:text-xl font-semibold text-gray-900 mb-2">登录成功！</h2>
            <p className="text-sm sm:text-base text-gray-600">正在跳转到首页...</p>
            <div className="mt-4">
              <LoadingSpinner size="sm" className="mx-auto" />
            </div>
          </div>
        )}

        {status === 'error' && (
          <div className="text-center">
            <div className="w-12 h-12 sm:w-16 sm:h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-6 h-6 sm:w-8 sm:h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h2 className="text-lg sm:text-xl font-semibold text-gray-900 mb-2">登录失败</h2>
            <p className="text-sm sm:text-base text-gray-600 mb-6 break-words">{errorMessage}</p>
            <div className="space-y-3">
              <button
                onClick={handleRetry}
                className="w-full bg-blue-600 text-white py-2.5 px-4 rounded-md hover:bg-blue-700 transition-colors duration-200 text-sm sm:text-base font-medium"
              >
                返回首页
              </button>
              <button
                onClick={() => window.location.reload()}
                className="w-full bg-gray-100 text-gray-700 py-2.5 px-4 rounded-md hover:bg-gray-200 transition-colors duration-200 text-sm sm:text-base font-medium"
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