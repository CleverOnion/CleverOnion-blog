import { create } from 'zustand';
import { authAPI, type User, type AuthResponse } from '../api/auth';
import { showErrorToast, showSuccessToast } from './toastStore';

// 认证状态接口
interface AuthState {
  // 状态
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  
  // 操作方法
  login: () => void;
  logout: () => Promise<void>;
  setUser: (user: User) => void;
  setAuthData: (authData: AuthResponse) => void;
  clearError: () => void;
  initializeAuth: () => Promise<void>;
  refreshUserInfo: () => Promise<void>;
}

// 创建认证store
export const useAuthStore = create<AuthState>((set, get) => ({
  // 初始状态
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  // GitHub登录
  login: () => {
    set({ isLoading: true, error: null });
    authAPI.loginWithGitHub();
  },

  // 用户登出
  logout: async () => {
    set({ isLoading: true, error: null });
    
    try {
      await authAPI.logout();
      set({ 
        user: null, 
        isAuthenticated: false, 
        isLoading: false,
        error: null 
      });
      showSuccessToast('已成功退出登录');
    } catch (error) {
      console.error('Logout failed:', error);
      showErrorToast('退出登录失败，请重试');
      // 即使登出失败，也清除本地状态
      set({ 
        user: null, 
        isAuthenticated: false, 
        isLoading: false,
        error: null 
      });
    }
  },

  // 设置用户信息
  setUser: (user: User) => {
    set({ 
      user, 
      isAuthenticated: true, 
      isLoading: false,
      error: null 
    });
    // 同步到localStorage
    localStorage.setItem('user', JSON.stringify(user));
  },

  // 设置认证数据
  setAuthData: (authData: AuthResponse) => {
    authAPI.setAuthData(authData);
    set({ 
      user: authData.user, 
      isAuthenticated: true, 
      isLoading: false,
      error: null 
    });
  },

  // 清除错误
  clearError: () => {
    set({ error: null });
  },

  // 初始化认证状态（应用启动时调用）
  initializeAuth: async () => {
    set({ isLoading: true, error: null });
    
    try {
      // 检查是否有存储的token
      const token = authAPI.getAccessToken();
      const storedUser = authAPI.getStoredUser();
      
      if (token && storedUser) {
        // 验证token是否有效，获取最新用户信息
        try {
          const currentUser = await authAPI.getCurrentUser();
          set({ 
            user: currentUser, 
            isAuthenticated: true, 
            isLoading: false,
            error: null 
          });
        } catch (error) {
          // token可能已过期，尝试刷新
          try {
            const authData = await authAPI.refreshToken();
            set({ 
              user: authData.user, 
              isAuthenticated: true, 
              isLoading: false,
              error: null 
            });
          } catch (refreshError) {
            // 刷新失败，清除认证状态
            console.error('Token refresh failed:', refreshError);
            authAPI.logout();
            set({ 
              user: null, 
              isAuthenticated: false, 
              isLoading: false,
              error: null 
            });
          }
        }
      } else {
        // 没有认证信息
        set({ 
          user: null, 
          isAuthenticated: false, 
          isLoading: false,
          error: null 
        });
      }
    } catch (error) {
      console.error('Auth initialization failed:', error);
      set({ 
        user: null, 
        isAuthenticated: false, 
        isLoading: false,
        error: null 
      });
    }
  },

  // 刷新用户信息
  refreshUserInfo: async () => {
    const { isAuthenticated } = get();
    
    if (!isAuthenticated) {
      return;
    }
    
    set({ isLoading: true, error: null });
    
    try {
      const user = await authAPI.getCurrentUser();
      set({ 
        user, 
        isLoading: false,
        error: null 
      });
      // 更新localStorage中的用户信息
      localStorage.setItem('user', JSON.stringify(user));
    } catch (error) {
      console.error('Failed to refresh user info:', error);
      showErrorToast('刷新用户信息失败');
      set({ 
        isLoading: false,
        error: null 
      });
    }
  },
}));

// 导出类型
export type { AuthState };