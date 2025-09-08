import { api } from './index';

// 用户数据类型定义
export interface User {
  id: number;
  github_id: number;
  username: string;
  avatar_url: string;
}

// 用户列表响应类型
export interface UserListResponse {
  users: User[];
  totalCount: number;
  currentPage: number;
  pageSize: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

// 分页参数类型
export interface UserListParams {
  page?: number;
  size?: number;
}

// API响应基础类型
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

/**
 * 用户模块API接口
 */
export const userApi = {
  /**
   * 分页获取用户列表
   * @param params 分页参数
   * @returns 用户列表数据
   */
  async getUserList(params: UserListParams = {}): Promise<UserListResponse> {
    const { page = 0, size = 10 } = params;
    const response = await api.get<ApiResponse<UserListResponse>>('/users', {
      params: { page, size }
    });
    
    if (!response.data.success) {
      throw new Error(response.data.message || '获取用户列表失败');
    }
    
    return response.data.data;
  },

  /**
   * 获取用户详情
   * @param id 用户ID
   * @returns 用户详情数据
   */
  async getUserDetail(id: number): Promise<User> {
    const response = await api.get<ApiResponse<User>>(`/users/${id}`);
    
    if (!response.data.success) {
      throw new Error(response.data.message || '获取用户详情失败');
    }
    
    return response.data.data;
  },

  /**
   * 获取用户总数
   * @returns 用户总数
   */
  async getUserCount(): Promise<number> {
    const response = await api.get<ApiResponse<number>>('/users/count');
    
    if (!response.data.success) {
      throw new Error(response.data.message || '获取用户总数失败');
    }
    
    return response.data.data;
  }
};

export default userApi;