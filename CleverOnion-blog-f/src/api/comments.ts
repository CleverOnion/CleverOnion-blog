import { api, ApiResponse } from './index';

// 评论数据类型定义
export interface Comment {
  id: string;
  content: string;
  article_id: string;
  user_id: number;
  parent_id: string | null;
  is_top_level: boolean;
  published_at: string;
  // 后端返回的用户信息
  user?: {
    id: number;
    github_id: number;
    username: string;
    avatar_url: string;
  };
  // 扩展字段，用于前端显示
  author?: {
    id: number;
    username: string;
    avatar?: string;
  };
  replies?: Comment[];
}

// 创建评论请求参数
export interface CreateCommentRequest {
  content: string;
  article_id: string;
  user_id: number;
  parent_id?: string;
}

// 评论列表响应
export interface CommentListResponse {
  comments: Comment[];
  total_count: number;
  page: number;
  size: number;
  has_next: boolean;
  has_previous: boolean;
}

// 查询评论列表参数
export interface GetCommentsParams {
  articleId: string;
  page?: number;
  size?: number;
}

// 评论API接口
export const commentApi = {
  // 获取文章评论列表
  getComments: async (params: GetCommentsParams): Promise<ApiResponse<CommentListResponse>> => {
    const { articleId, page = 0, size = 10 } = params;
    const response = await api.get<ApiResponse<CommentListResponse>>(
      `/comments?articleId=${articleId}&page=${page}&size=${size}`
    );
    return response.data;
  },

  // 发表评论
  createComment: async (data: CreateCommentRequest): Promise<ApiResponse<Comment>> => {
    const response = await api.post<ApiResponse<Comment>>('/comments', data);
    return response.data;
  },

  // 删除评论
  deleteComment: async (commentId: string): Promise<ApiResponse<null>> => {
    const response = await api.delete<ApiResponse<null>>(`/comments/${commentId}`);
    return response.data;
  },
};

export default commentApi;