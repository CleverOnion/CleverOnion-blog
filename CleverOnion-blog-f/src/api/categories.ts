import { api, ApiResponse } from './index';

// 分类相关的类型定义
interface Category {
  id: number;
  name: string;
  icon?: string; // react-icons图标名称，如 'FaReact', 'FaCss3Alt'
}

// 带文章数量的分类类型
interface CategoryWithCount {
  id: number;
  name: string;
  icon?: string; // react-icons图标名称
  articleCount: number;
}

interface CategoryListResponse {
  categories: Category[];
  totalCount: number;
}

interface CategoryPageResponse {
  categories: Category[];
  totalCount: number;
  page: number;
  size: number;
  totalPages: number;
}

// 带文章数量的分类分页响应
interface CategoryWithCountListResponse {
  categories: CategoryWithCount[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

interface CreateCategoryRequest {
  name: string;
  icon?: string; // react-icons图标名称
}

interface UpdateCategoryRequest {
  name: string;
  icon?: string; // react-icons图标名称
}

// 分类管理API
const categoryApi = {
  // 获取所有分类
  getAllCategories: async (): Promise<CategoryListResponse> => {
    const response = await api.get<ApiResponse<CategoryListResponse>>('/categories');
    return response.data.data;
  },

  // 根据ID获取分类详情
  getCategoryById: async (id: number): Promise<Category> => {
    const response = await api.get<ApiResponse<Category>>(`/categories/${id}`);
    return response.data.data;
  },

  // 创建分类
  createCategory: async (data: CreateCategoryRequest): Promise<Category> => {
    const response = await api.post<ApiResponse<Category>>('/categories', data);
    return response.data.data;
  },

  // 更新分类
  updateCategory: async (id: number, data: UpdateCategoryRequest): Promise<Category> => {
    const response = await api.put<ApiResponse<Category>>(`/categories/${id}`, data);
    return response.data.data;
  },

  // 删除分类
  deleteCategory: async (id: number): Promise<void> => {
    await api.delete(`/categories/${id}`);
  },

  // 分页获取分类
  getCategoriesPage: async (page: number = 0, size: number = 10): Promise<CategoryPageResponse> => {
    const response = await api.get<ApiResponse<CategoryPageResponse>>('/categories/page', {
      params: { page, size }
    });
    return response.data.data;
  },

  // 分页获取分类及文章数量
  getCategoriesWithCount: async (page: number = 0, size: number = 10): Promise<CategoryWithCountListResponse> => {
    const response = await api.get<ApiResponse<CategoryWithCountListResponse>>('/categories/with-count', {
      params: { page, size }
    });
    return response.data.data;
  },

  // 搜索分类
  searchCategories: async (keyword: string): Promise<CategoryListResponse> => {
    const response = await api.get<ApiResponse<CategoryListResponse>>('/categories/search', {
      params: { keyword }
    });
    return response.data.data;
  },

  // 获取最近创建的分类
  getRecentCategories: async (limit: number = 10): Promise<CategoryListResponse> => {
    const response = await api.get<ApiResponse<CategoryListResponse>>('/categories/recent', {
      params: { limit }
    });
    return response.data.data;
  }
};

export default categoryApi;
export type { Category, CategoryWithCount, CategoryListResponse, CategoryPageResponse, CategoryWithCountListResponse, CreateCategoryRequest, UpdateCategoryRequest };