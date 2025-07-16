import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';

// API 基础配置
const API_BASE_URL = 'http://localhost:8080';
const REQUEST_TIMEOUT = 10000; // 10秒超时

// 扩展 InternalAxiosRequestConfig 类型以支持 _retry 属性
interface ExtendedAxiosRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

// 创建 axios 实例
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: REQUEST_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('accessToken');
    
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // 打印请求信息（开发环境）
    if (import.meta.env.DEV) {
      console.log('🚀 API Request:', {
        method: config.method?.toUpperCase(),
        url: config.url,
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
    
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as ExtendedAxiosRequestConfig;
    
    // 处理 401 未授权错误
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        // 尝试刷新 token
        const refreshToken = localStorage.getItem('refreshToken');
        
        if (refreshToken) {
          const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
            refreshToken,
          });
          
          const { accessToken } = response.data;
          localStorage.setItem('accessToken', accessToken);
          
          // 重新发送原始请求
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          }
          
          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        // 刷新失败，清除本地存储并跳转到首页
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        
        // 跳转到首页，用户可以重新进行GitHub登录
        window.location.href = '/';
        
        return Promise.reject(refreshError);
      }
    }
    
    // 打印错误信息
    console.error('❌ API Error:', {
      status: error.response?.status,
      message: error.message,
      url: error.config?.url,
      data: error.response?.data,
    });
    
    return Promise.reject(error);
  }
);

// 通用 API 响应类型
export interface ApiResponse<T = any> {
  data: T;
  message?: string;
}

export interface ApiError {
  error: string;
  message?: string;
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