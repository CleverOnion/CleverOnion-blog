import { api } from './index';
import type { AxiosResponse } from 'axios';

// 鉴权相关的类型定义
export interface AuthResponse {
  code: number;
  message: string;
  data: any;
  timestamp: number;
}

export interface LoginResponse {
  accessToken: string;
  expiresIn: number;
  tokenType: string;
  userInfo: UserInfo;
  loginTime: string;
}

export interface UserInfo {
  id: number;
  gitHubId: number;
  username: string;
  avatarUrl: string;
  createdAt: string;
}

export interface GitHubCallbackRequest {
  code: string;
  state?: string;
}

// 鉴权API类
export class AuthAPI {
  /**
   * 获取GitHub OAuth2授权URL
   * @param state 可选的状态参数，用于防止CSRF攻击
   * @returns Promise<string> 授权URL
   */
  static async getGitHubAuthUrl(state?: string): Promise<string> {
    try {
      const url = state ? `/auth/login/github?state=${encodeURIComponent(state)}` : '/auth/login/github';
      const response: AxiosResponse<AuthResponse> = await api.get(url);
      
      if (response.data.code === 200) {
        return response.data.data;
      } else {
        throw new Error(response.data.message || '获取授权URL失败');
      }
    } catch (error: any) {
      console.error('获取GitHub授权URL失败:', error);
      throw new Error(error.response?.data?.message || error.message || '获取授权URL失败');
    }
  }

  /**
   * GitHub OAuth2回调处理
   * @param callbackData 包含授权码和状态参数的对象
   * @returns Promise<LoginResponse> 登录响应数据
   */
  static async handleGitHubCallback(callbackData: GitHubCallbackRequest): Promise<LoginResponse> {
    try {
      const response: AxiosResponse<AuthResponse> = await api.post('/auth/callback/github', callbackData);
      
      if (response.data.code === 200) {
        const backendData = response.data.data;
        
        // 转换后端数据格式为前端期望的格式
        const loginResponse: LoginResponse = {
          accessToken: backendData.access_token,
          expiresIn: backendData.expires_in,
          tokenType: backendData.token_type,
          userInfo: {
            id: backendData.user_info.id,
            gitHubId: backendData.user_info.github_id,
            username: backendData.user_info.username,
            avatarUrl: backendData.user_info.avatar_url,
            createdAt: backendData.user_info.created_at
          },
          loginTime: backendData.login_time
        };
        
        console.log('🔄 数据格式转换完成:', loginResponse);
        return loginResponse;
      } else {
        throw new Error(response.data.message || 'GitHub授权失败');
      }
    } catch (error: any) {
      console.error('GitHub回调处理失败:', error);
      throw new Error(error.response?.data?.message || error.message || 'GitHub授权失败');
    }
  }



  /**
   * 检查登录状态
   * @param userId 用户ID
   * @returns Promise<boolean> 是否已登录
   */
  static async checkLoginStatus(userId: string): Promise<boolean> {
    try {
      const response: AxiosResponse<AuthResponse> = await api.get(`/auth/status?userId=${encodeURIComponent(userId)}`);
      
      if (response.data.code === 200) {
        return response.data.data;
      } else {
        return false;
      }
    } catch (error: any) {
      console.error('检查登录状态失败:', error);
      return false;
    }
  }

  /**
   * 获取当前用户信息
   * @returns Promise<{user: UserInfo}> 用户信息
   */
  static async getUserInfo(): Promise<{user: UserInfo}> {
    try {
      // 首先从本地存储获取用户信息
      const localUserInfo = AuthUtils.getUserInfo();
      if (localUserInfo) {
        return { user: localUserInfo };
      } else {
        throw new Error('用户未登录');
      }
    } catch (error: any) {
      console.error('获取用户信息失败:', error);
      throw error;
    }
  }

  /**
   * 检查当前用户是否为管理员
   * @returns Promise<{isAdmin: boolean}> 是否为管理员
   */
  static async checkAdminStatus(): Promise<{isAdmin: boolean}> {
    try {
      const response: AxiosResponse<AuthResponse> = await api.get('/admin/check');
      
      if (response.data.code === 200) {
        return { isAdmin: response.data.data };
      } else {
        return { isAdmin: false };
      }
    } catch (error: any) {
      console.error('检查管理员权限失败:', error);
      return { isAdmin: false };
    }
  }

  /**
   * 用户登出
   * @returns Promise<void>
   */
  static async logout(): Promise<void> {
    try {
      // 清除本地存储的登录信息
      AuthUtils.clearLoginInfoWithEvent();
    } catch (error: any) {
      console.error('登出失败:', error);
      throw error;
    }
  }
}

// 鉴权工具函数
export class AuthUtils {
  private static readonly ACCESS_TOKEN_KEY = 'access_token';
  private static readonly TOKEN_EXPIRES_KEY = 'token_expires_at';
  private static readonly USER_INFO_KEY = 'user_info';
  private static readonly GITHUB_STATE_KEY = 'github_oauth_state';

  /**
   * 保存登录信息到本地存储
   * @param loginData 登录响应数据
   */
  static saveLoginInfo(loginData: LoginResponse): void {
    try {
      const expiresAt = Date.now() + (loginData.expiresIn * 1000);
      
      console.log('💾 开始保存登录信息:', {
        accessToken: loginData.accessToken?.substring(0, 10) + '...',
        expiresIn: loginData.expiresIn,
        expiresAt: new Date(expiresAt).toLocaleString(),
        userInfo: loginData.userInfo
      });
      
      localStorage.setItem(this.ACCESS_TOKEN_KEY, loginData.accessToken);
      localStorage.setItem(this.TOKEN_EXPIRES_KEY, expiresAt.toString());
      localStorage.setItem(this.USER_INFO_KEY, JSON.stringify(loginData.userInfo));
      
      console.log('✅ 登录信息保存成功');
      
      // 触发自定义事件，通知其他组件状态已更新
      window.dispatchEvent(new Event('authStatusChanged'));
      
      console.log('📢 已触发authStatusChanged事件');
    } catch (error) {
      console.error('❌ 保存登录信息失败:', error);
    }
  }

  /**
   * 获取访问令牌
   * @returns string | null
   */
  static getAccessToken(): string | null {
    try {
      return localStorage.getItem(this.ACCESS_TOKEN_KEY);
    } catch (error) {
      console.error('获取访问令牌失败:', error);
      return null;
    }
  }

  /**
   * 获取用户信息
   * @returns UserInfo | null
   */
  static getUserInfo(): UserInfo | null {
    try {
      const userInfoStr = localStorage.getItem(this.USER_INFO_KEY);
      if (!userInfoStr || userInfoStr === 'undefined' || userInfoStr === 'null') {
        return null;
      }
      return JSON.parse(userInfoStr);
    } catch (error) {
      console.error('获取用户信息失败:', error);
      // 清除无效的数据
      localStorage.removeItem(this.USER_INFO_KEY);
      return null;
    }
  }

  /**
   * 检查令牌是否有效
   * @returns boolean
   */
  static isTokenValid(): boolean {
    try {
      const token = this.getAccessToken();
      const expiresAt = localStorage.getItem(this.TOKEN_EXPIRES_KEY);
      
      if (!token || !expiresAt || expiresAt === 'undefined' || expiresAt === 'null') {
        return false;
      }
      
      const expiresAtNum = parseInt(expiresAt);
      if (isNaN(expiresAtNum)) {
        // 清除无效的过期时间数据
        localStorage.removeItem(this.TOKEN_EXPIRES_KEY);
        return false;
      }
      
      return Date.now() < expiresAtNum;
    } catch (error) {
      console.error('检查令牌有效性失败:', error);
      return false;
    }
  }

  /**
   * 清除登录信息
   */
  static clearLoginInfo(): void {
    try {
      localStorage.removeItem(this.ACCESS_TOKEN_KEY);
      localStorage.removeItem(this.TOKEN_EXPIRES_KEY);
      localStorage.removeItem(this.USER_INFO_KEY);
    } catch (error) {
      console.error('清除登录信息失败:', error);
    }
  }

  /**
   * 清除登录信息并触发状态更新事件
   */
  static clearLoginInfoWithEvent(): void {
    this.clearLoginInfo();
    // 触发自定义事件，通知其他组件状态已更新
    window.dispatchEvent(new Event('authStatusChanged'));
  }

  /**
   * 生成随机状态值
   * @returns string
   */
  static generateRandomState(): string {
    return Math.random().toString(36).substring(2, 15) + 
           Math.random().toString(36).substring(2, 15);
  }

  /**
   * 保存GitHub OAuth状态参数
   * @param state 状态参数
   */
  static saveGitHubState(state: string): void {
    try {
      sessionStorage.setItem(this.GITHUB_STATE_KEY, state);
    } catch (error) {
      console.error('保存GitHub状态参数失败:', error);
    }
  }

  /**
   * 获取并清除GitHub OAuth状态参数
   * @returns string | null
   */
  static getAndClearGitHubState(): string | null {
    try {
      const state = sessionStorage.getItem(this.GITHUB_STATE_KEY);
      if (state) {
        sessionStorage.removeItem(this.GITHUB_STATE_KEY);
      }
      return state;
    } catch (error) {
      console.error('获取GitHub状态参数失败:', error);
      return null;
    }
  }

  /**
   * 验证状态参数
   * @param receivedState 接收到的状态参数
   * @param savedState 保存的状态参数
   * @returns boolean
   */
  static validateState(receivedState: string | null, savedState: string | null): boolean {
    return receivedState === savedState && receivedState !== null;
  }
}

// GitHub OAuth2集成流程类
export class GitHubOAuth {
  /**
   * 发起GitHub登录
   * @returns Promise<void>
   */
  static async initiateLogin(): Promise<void> {
    try {
      const state = AuthUtils.generateRandomState();
      
      // 获取授权URL
      const authUrl = await AuthAPI.getGitHubAuthUrl(state);
      
      // 保存状态参数
      AuthUtils.saveGitHubState(state);
      
      // 重定向到GitHub授权页面
      window.location.href = authUrl;
    } catch (error) {
      console.error('发起GitHub登录失败:', error);
      throw error;
    }
  }

  /**
   * 处理GitHub授权回调
   * @param code 授权码
   * @param state 状态参数
   * @returns Promise<LoginResponse>
   */
  static async handleCallback(code: string, state?: string): Promise<LoginResponse> {
    try {
      // 验证状态参数
      const savedState = AuthUtils.getAndClearGitHubState();
      if (!AuthUtils.validateState(state || null, savedState)) {
        throw new Error('状态参数验证失败，可能存在CSRF攻击');
      }

      // 调用后端回调接口
      const loginData = await AuthAPI.handleGitHubCallback({ code, state });
      
      console.log('🔍 从后端获取的登录数据:', loginData);
      
      // 不在这里保存登录信息，让调用方决定何时保存
      // AuthUtils.saveLoginInfo(loginData);
      
      return loginData;
    } catch (error) {
      console.error('处理GitHub回调失败:', error);
      throw error;
    }
  }

  /**
   * 用户登出
   * @returns Promise<void>
   */
  static async logout(): Promise<void> {
    try {
      const userInfo = AuthUtils.getUserInfo();
      if (userInfo) {
        await AuthAPI.logout(userInfo.id.toString());
      }
      
      // 清除本地存储并触发状态更新
      AuthUtils.clearLoginInfoWithEvent();
    } catch (error) {
      console.error('登出失败:', error);
      // 即使后端登出失败，也要清除本地存储
      AuthUtils.clearLoginInfoWithEvent();
      throw error;
    }
  }
}

// 导出默认对象
export default {
  AuthAPI,
  AuthUtils,
  GitHubOAuth,
};