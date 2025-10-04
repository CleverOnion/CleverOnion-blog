import { api, ApiResponse } from "./index";
import type {
  Comment,
  CommentListResponse,
  CreateCommentRequest,
  GetCommentsParams,
  GetRepliesParams,
} from "../types/comment";

// ===== 导出类型（供其他模块使用） =====
export type {
  Comment,
  CommentListResponse,
  CreateCommentRequest,
  GetCommentsParams,
  GetRepliesParams,
};

// ===== 评论API接口 =====

/**
 * 获取文章的顶级评论及最新回复（新版接口 v2.0.0）
 * 支持懒加载子评论，性能更好
 *
 * @param params.articleId - 文章ID
 * @param params.page - 页码（默认0）
 * @param params.size - 每页大小（默认10）
 * @param params.replyLimit - 每个评论返回的最新回复数（默认3）
 * @returns 顶级评论列表（包含回复统计和最新回复）
 */
export const getTopLevelCommentsWithReplies = async (
  params: GetCommentsParams
): Promise<ApiResponse<CommentListResponse>> => {
  const { articleId, page = 0, size = 10, replyLimit = 3 } = params;
  const response = await api.get<ApiResponse<CommentListResponse>>(
    "/comments/top-level-with-replies",
    {
      params: { articleId, page, size, replyLimit },
    }
  );
  return response.data;
};

/**
 * 获取评论的所有回复（分页）
 *
 * @param params.parentId - 父评论ID
 * @param params.page - 页码（默认0）
 * @param params.size - 每页大小（默认50）
 * @returns 回复列表
 */
export const getCommentReplies = async (
  params: GetRepliesParams
): Promise<ApiResponse<CommentListResponse>> => {
  const { parentId, page = 0, size = 50 } = params;
  const response = await api.get<ApiResponse<CommentListResponse>>(
    "/comments/replies",
    {
      params: { parentId, page, size },
    }
  );
  return response.data;
};

/**
 * 发表评论或回复
 *
 * @param data.content - 评论内容
 * @param data.article_id - 文章ID
 * @param data.parent_id - 父评论ID（顶级评论为null）
 * @returns 新创建的评论
 */
export const postComment = async (
  data: CreateCommentRequest
): Promise<ApiResponse<Comment>> => {
  const response = await api.post<ApiResponse<Comment>>("/comments", data);
  return response.data;
};

/**
 * 删除评论
 *
 * @param commentId - 评论ID
 */
export const deleteComment = async (
  commentId: string
): Promise<ApiResponse<null>> => {
  const response = await api.delete<ApiResponse<null>>(
    `/comments/${commentId}`
  );
  return response.data;
};
