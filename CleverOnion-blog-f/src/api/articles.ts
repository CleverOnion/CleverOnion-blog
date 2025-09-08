import { api } from './index';

// 文章相关的类型定义
interface Article {
  id: string;
  title: string;
  content: string;
  summary: string;
  status: 'PUBLISHED' | 'DRAFT' | 'ARCHIVED';
  category_id?: number;
  category?: {
    id: number;
    name: string;
    description?: string;
    created_at: string;
    updated_at: string;
  };
  author_id?: number;
  author?: {
    id: number;
    username: string;
    email: string;
    avatar_url?: string;
    bio?: string;
    created_at: string;
    updated_at: string;
  };
  tag_ids?: number[];
  tags?: Array<{
    id: number;
    name: string;
    created_at: string;
    updated_at: string;
  }>;
  views?: number;
  created_at: string;
  updated_at: string;
  published_at?: string;
}

interface ArticlePageResponse {
  articles: Article[];
  total_count: number;
  page: number;
  size: number;
  total_pages: number;
  has_next: boolean;
  has_previous: boolean;
}

interface ArticleQueryParams {
  page?: number;
  size?: number;
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  categoryId?: number;
  tagId?: number;
}

interface CreateArticleRequest {
  title: string;
  content: string;
  summary: string;
  category_id: number;
  tag_names: string[];
  status: 'PUBLISHED' | 'DRAFT';
}

interface UpdateArticleRequest {
  title?: string;
  content?: string;
  summary?: string;
  category_id?: number;
  tag_names?: string[];
  status?: 'PUBLISHED' | 'DRAFT' | 'ARCHIVED';
}

interface PublishArticleRequest {
  title: string;
  content: string;
  summary: string;
  category_id: number;
  tag_names: string[];
}

// 文章管理API
const articleApi = {
  // 获取已发布文章列表（分页）
  getPublishedArticles: async (params: ArticleQueryParams = {}): Promise<ArticlePageResponse> => {
    const { page = 0, size = 10, status, categoryId, tagId } = params;
    const queryParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    
    if (status !== undefined) {
      queryParams.append('status', status);
    }
    
    if (categoryId !== undefined) {
      queryParams.append('categoryId', categoryId.toString());
    }
    
    if (tagId !== undefined) {
      queryParams.append('tagId', tagId.toString());
    }
    
    const response = await api.get<{data: ArticlePageResponse}>(`/articles/published?${queryParams}`);
    return response.data.data;
  },

  // 获取所有文章列表（管理端）
  getAllArticles: async (params: ArticleQueryParams = {}): Promise<ArticlePageResponse> => {
    const { page = 0, size = 10, status, categoryId, tagId } = params;
    const queryParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    
    if (status !== undefined) {
      queryParams.append('status', status);
    }
    
    if (categoryId !== undefined) {
      queryParams.append('categoryId', categoryId.toString());
    }
    
    if (tagId !== undefined) {
      queryParams.append('tagId', tagId.toString());
    }
    
    const response = await api.get<{data: ArticlePageResponse}>(`/articles?${queryParams}`);
    return response.data.data;
  },

  // 根据ID获取文章
  getArticleById: async (id: string): Promise<Article> => {
    const response = await api.get<{data: Article}>(`/articles/${id}`);
    return response.data.data;
  },

  // 创建文章
  createArticle: async (data: CreateArticleRequest): Promise<Article> => {
    const response = await api.post<{data: Article}>('/articles', data);
    return response.data.data;
  },

  // 直接发布文章
  publishArticleDirectly: async (data: PublishArticleRequest): Promise<Article> => {
    const response = await api.post<{data: Article}>('/articles/publish', data);
    return response.data.data;
  },

  // 更新文章
  updateArticle: async (id: string, data: UpdateArticleRequest): Promise<Article> => {
    const response = await api.put<{data: Article}>(`/articles/${id}`, data);
    return response.data.data;
  },

  // 删除文章
  deleteArticle: async (id: string): Promise<void> => {
    await api.delete(`/articles/${id}`);
  },

  // 发布文章
  publishArticle: async (id: string): Promise<Article> => {
    const response = await api.post<{data: Article}>(`/articles/${id}/publish`);
    return response.data.data;
  },

  // 恢复文章为草稿
  unpublishArticle: async (id: string): Promise<Article> => {
    const response = await api.post<{data: Article}>(`/articles/${id}/revert-to-draft`);
    return response.data.data;
  },

  // 归档文章
  archiveArticle: async (id: string): Promise<Article> => {
    const response = await api.put<{data: Article}>(`/articles/${id}/archive`);
    return response.data.data;
  },

  // 搜索文章
  searchArticles: async (keyword: string, params: ArticleQueryParams = {}): Promise<ArticlePageResponse> => {
    const { page = 0, size = 10, categoryId, tagId } = params;
    const queryParams = new URLSearchParams({
      keyword,
      page: page.toString(),
      size: size.toString(),
    });
    
    if (categoryId !== undefined) {
      queryParams.append('categoryId', categoryId.toString());
    }
    
    if (tagId !== undefined) {
      queryParams.append('tagId', tagId.toString());
    }
    
    const response = await api.get<{data: ArticlePageResponse}>(`/articles/search?${queryParams}`);
    return response.data.data;
  },

  // 批量删除文章
  batchDeleteArticles: async (ids: string[]): Promise<void> => {
    await api.delete('/articles/batch', { data: { ids } });
  },

  // 批量更新文章状态
  batchUpdateArticleStatus: async (ids: string[], status: 'PUBLISHED' | 'DRAFT' | 'ARCHIVED'): Promise<void> => {
    await api.put('/articles/batch/status', { ids, status });
  }
};

// 导出类型和API
export type { 
  Article, 
  ArticlePageResponse, 
  ArticleQueryParams, 
  CreateArticleRequest, 
  UpdateArticleRequest,
  PublishArticleRequest 
};
export { articleApi };
export default articleApi;