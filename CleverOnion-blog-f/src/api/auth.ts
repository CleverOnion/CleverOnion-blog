import { api } from './index';

// 用户信息接口
export interface User {
  id: number;
  githubId: number;
  githubLogin: string;
  avatarUrl: string;
  email: string;
  name: string;
  bio?: string;
  createdAt: string;
  updatedAt: string;
}

// 认证响应接口
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

// 认证API类
class AuthAPI {
  /**
   * GitHub登录 - 重定向到GitHub授权页面
   */
  loginWithGitHub(): void {
    window.location.href = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/auth/github`;
  }

  /**
   * 获取当前用户信息
   */
  async getCurrentUser(): Promise<User> {
    const response = await api.get<User>('/auth/me');
    return response.data;
  }

  /**
   * 刷新访问令牌
   */
  async refreshToken(): Promise<AuthResponse> {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await api.post<AuthResponse>('/auth/refresh', {
      refreshToken
    });

    // 更新本地存储的token
    localStorage.setItem('accessToken', response.data.accessToken);
    localStorage.setItem('refreshToken', response.data.refreshToken);

    return response.data;
  }

  /**
   * 用户登出
   */
  async logout(): Promise<void> {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // 清除本地存储
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  }

  /**
   * 检查是否已登录
   */
  isAuthenticated(): boolean {
    const token = localStorage.getItem('accessToken');
    return !!token;
  }

  /**
   * 获取存储的访问令牌
   */
  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  /**
   * 获取存储的刷新令牌
   */
  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  /**
   * 存储认证信息
   */
  setAuthData(authData: AuthResponse): void {
    localStorage.setItem('accessToken', authData.accessToken);
    localStorage.setItem('refreshToken', authData.refreshToken);
    localStorage.setItem('user', JSON.stringify(authData.user));
  }

  /**
   * 获取存储的用户信息
   */
  getStoredUser(): User | null {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;
    
    try {
      return JSON.parse(userStr);
    } catch (error) {
      console.error('Error parsing stored user:', error);
      return null;
    }
  }
}

// 导出单例实例
export const authAPI = new AuthAPI();

// 默认导出
export default authAPI;