import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
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
          setErrorMessage(errorDescription || error);
          return;
        }

        // 获取认证数据
        const accessToken = searchParams.get('access_token');
        const refreshToken = searchParams.get('refresh_token');
        const userDataStr = searchParams.get('user');

        if (!accessToken || !refreshToken || !userDataStr) {
          setStatus('error');
          setErrorMessage('Missing authentication data in callback');
          return;
        }

        // 解析用户数据
        let userData;
        try {
          userData = JSON.parse(decodeURIComponent(userDataStr));
        } catch (parseError) {
          setStatus('error');
          setErrorMessage('Invalid user data format');
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

        // 延迟跳转，让用户看到成功提示
        setTimeout(() => {
          navigate('/', { replace: true });
        }, 1500);

      } catch (error) {
        console.error('OAuth callback error:', error);
        setStatus('error');
        setErrorMessage('An unexpected error occurred during authentication');
      }
    };

    handleCallback();
  }, [searchParams, setAuthData, navigate]);

  // 错误状态处理
  const handleRetry = () => {
    navigate('/', { replace: true });
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full bg-white rounded-lg shadow-md p-8">
        {status === 'loading' && (
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
            <h2 className="text-xl font-semibold text-gray-900 mb-2">正在处理登录...</h2>
            <p className="text-gray-600">请稍候，我们正在验证您的GitHub账户</p>
          </div>
        )}

        {status === 'success' && (
          <div className="text-center">
            <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-xl font-semibold text-gray-900 mb-2">登录成功！</h2>
            <p className="text-gray-600">正在跳转到首页...</p>
          </div>
        )}

        {status === 'error' && (
          <div className="text-center">
            <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h2 className="text-xl font-semibold text-gray-900 mb-2">登录失败</h2>
            <p className="text-gray-600 mb-4">{errorMessage}</p>
            <button
              onClick={handleRetry}
              className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors duration-200"
            >
              返回首页
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default OAuthCallback;