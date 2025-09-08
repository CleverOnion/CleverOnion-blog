import { api, ApiResponse } from './index';

// 标签相关的类型定义
interface Tag {
  id: number;
  name: string;
}

interface TagWithCount {
  id: number;
  name: string;
  articleCount: number;
}

interface TagListResponse {
  tags: Tag[];
  total: number;
}

interface TagPageResponse {
  tags: TagWithCount[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

interface CreateTagRequest {
  name: string;
}

interface UpdateTagRequest {
  name: string;
}

interface TagCountResponse {
  count: number;
}

// 标签管理API
const tagApi = {
  // 获取所有标签
  getAllTags: async (): Promise<TagListResponse> => {
    const response = await api.get<ApiResponse<TagListResponse>>('/tags');
    return response.data.data;
  },

  // 根据ID获取标签详情
  getTagById: async (id: number): Promise<Tag> => {
    const response = await api.get<ApiResponse<Tag>>(`/tags/${id}`);
    return response.data.data;
  },

  // 创建标签
  createTag: async (data: CreateTagRequest): Promise<Tag> => {
    const response = await api.post<ApiResponse<Tag>>('/tags', data);
    return response.data.data;
  },

  // 更新标签
  updateTag: async (id: number, data: UpdateTagRequest): Promise<Tag> => {
    const response = await api.put<ApiResponse<Tag>>(`/tags/${id}`, data);
    return response.data.data;
  },

  // 删除标签
  deleteTag: async (id: number): Promise<void> => {
    await api.delete(`/tags/${id}`);
  },

  // 搜索标签
  searchTags: async (keyword: string): Promise<TagListResponse> => {
    const response = await api.get<ApiResponse<TagListResponse>>('/tags/search', {
      params: { keyword }
    });
    return response.data.data;
  },

  // 获取最近创建的标签
  getRecentTags: async (limit: number = 10): Promise<TagListResponse> => {
    const response = await api.get<ApiResponse<TagListResponse>>('/tags/recent', {
      params: { limit }
    });
    return response.data.data;
  },

  // 获取标签总数
  getTagCount: async (): Promise<TagCountResponse> => {
    const response = await api.get<ApiResponse<TagCountResponse>>('/tags/count');
    return response.data.data;
  },

  // 分页查询标签及文章数量
  getTagsWithCount: async (page: number = 0, size: number = 10): Promise<TagPageResponse> => {
    const response = await api.get<ApiResponse<TagPageResponse>>(`/tags/page-with-count?page=${page}&size=${size}`);
    return response.data.data;
  }
};

// 导出类型和API
export type { Tag, TagWithCount, TagListResponse, TagPageResponse, CreateTagRequest, UpdateTagRequest, TagCountResponse };
export { tagApi };
export default tagApi;
