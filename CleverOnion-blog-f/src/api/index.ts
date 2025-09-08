import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { playNotificationSound } from '../utils/audioUtils';

// API 基础配置
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const API_PATH = import.meta.env.VITE_API_PATH || '/api';
const REQUEST_TIMEOUT = 10000; // 10秒超时

// 创建 axios 实例
const apiClient: AxiosInstance = axios.create({
  baseURL: `${API_BASE_URL}${API_PATH}`,
  timeout: REQUEST_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 自动添加认证Token
    const token = localStorage.getItem('access_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // 打印请求信息（开发环境）
    if (import.meta.env.DEV) {
      console.log('🚀 API Request:', {
        method: config.method?.toUpperCase(),
        url: config.url,
        baseURL: config.baseURL,
        headers: config.headers,
        data: config.data,
      });
    }
    
    return config;
  },
  (error: AxiosError) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // 打印响应信息（开发环境）
    if (import.meta.env.DEV) {
      console.log('✅ API Response:', {
        status: response.status,
        url: response.config.url,
        data: response.data,
      });
    }
    
    // 检查后端返回的统一响应格式
    if (response.data && typeof response.data === 'object' && 'code' in response.data) {
      const { code, message } = response.data;
      
      // 播放通知音效
      playNotificationSound(code);
      
      // 处理401未授权错误
      if (code === 401) {
        // 通过事件方式处理toast显示
        const event = new CustomEvent('show-login-toast', {
          detail: { message: message || '请先登录' }
        });
        window.dispatchEvent(event);
        
        // Token过期或无效，清除本地存储
        localStorage.removeItem('access_token');
        localStorage.removeItem('token_expires_at');
        localStorage.removeItem('user_info');
        
        return Promise.reject(new Error(message || '请先登录'));
      }
    }
    
    return response;
  },
  (error: AxiosError) => {
    // 打印错误信息
    console.error('❌ API Error:', {
      status: error.response?.status,
      message: error.message,
      url: error.config?.url,
      data: error.response?.data,
    });
    
    // 检查后端返回的统一响应格式
    if (error.response?.data && typeof error.response.data === 'object' && 'code' in error.response.data) {
      const { code, message } = error.response.data as { code: number; message: string };
      
      // 播放通知音效
      playNotificationSound(code);
      
      // 处理401未授权错误
      if (code === 401) {
        // 通过事件方式处理toast显示
        const event = new CustomEvent('show-login-toast', {
          detail: { message: message || '请先登录' }
        });
        window.dispatchEvent(event);
        
        // Token过期或无效，清除本地存储
        localStorage.removeItem('access_token');
        localStorage.removeItem('token_expires_at');
        localStorage.removeItem('user_info');
        
        return Promise.reject(new Error(message || '请先登录'));
      }
    }
    
    // 处理HTTP状态码401（兼容旧的处理方式）
    if (error.response?.status === 401) {
      // Token过期或无效，清除本地存储
      localStorage.removeItem('access_token');
      localStorage.removeItem('token_expires_at');
      localStorage.removeItem('user_info');
      
      // 如果不是登录相关的接口，重定向到登录页
      const currentPath = window.location.pathname;
      if (!currentPath.includes('/auth/') && !currentPath.includes('/login')) {
        console.warn('🔒 Token已过期，重定向到登录页');
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);

// 通用 API 响应类型（符合后端统一响应格式）
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
  success: boolean;
}

export interface ApiError {
  code: number;
  message: string;
  data: null;
  timestamp: number;
}

export interface PaginationResponse<T = any> {
  data: T[];
  pagination: {
    currentPage: number;
    pageSize: number;
    totalCount: number;
    totalPages: number;
  };
}

// 通用请求方法
export const api = {
  // GET 请求
  get: <T = any>(url: string, config?: InternalAxiosRequestConfig): Promise<AxiosResponse<T>> => {
    return apiClient.get(url, config);
  },
  
  // POST 请求
  post: <T = any>(url: string, data?: any, config?: InternalAxiosRequestConfig): Promise<AxiosResponse<T>> => {
    return apiClient.post(url, data, config);
  },
  
  // PUT 请求
  put: <T = any>(url: string, data?: any, config?: InternalAxiosRequestConfig): Promise<AxiosResponse<T>> => {
    return apiClient.put(url, data, config);
  },
  
  // DELETE 请求
  delete: <T = any>(url: string, config?: InternalAxiosRequestConfig): Promise<AxiosResponse<T>> => {
    return apiClient.delete(url, config);
  },
  
  // PATCH 请求
  patch: <T = any>(url: string, data?: any, config?: InternalAxiosRequestConfig): Promise<AxiosResponse<T>> => {
    return apiClient.patch(url, data, config);
  },
};

// 文件上传专用方法
export const uploadFile = (
  url: string, 
  file: File, 
  onProgress?: (progress: number) => void
): Promise<AxiosResponse> => {
  const formData = new FormData();
  formData.append('file', file);
  
  return apiClient.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        onProgress(progress);
      }
    },
  });
};

export default apiClient;