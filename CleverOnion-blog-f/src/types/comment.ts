/**
 * 评论系统类型定义
 * @since 2.0.0
 */

/**
 * 用户信息（评论中的用户）
 */
export interface CommentUser {
  id?: string | null;
  github_id: number;
  username: string;
  avatar_url: string;
}

/**
 * 评论数据类型
 * 支持新旧版本评论系统
 */
export interface Comment {
  id: string;
  content: string;
  article_id: string;
  user_id?: number; // 兼容旧版
  user: CommentUser; // 新版使用嵌套用户对象
  parent_id: string | null;
  is_top_level: boolean;
  published_at: string;

  // ===== 新增字段（用于新版评论系统 v2.0.0） =====

  /** 回复总数（仅顶级评论有效） */
  reply_count?: number;

  /** 最新的几条回复（仅顶级评论有效，默认3条） */
  latest_replies?: Comment[];

  // ===== 前端状态字段 =====

  /** 前端加载的所有回复（用户点击"查看全部回复"后加载） */
  replies?: Comment[];

  /** 是否已加载所有回复 */
  repliesLoaded?: boolean;

  /** 是否展示所有回复（true: 显示replies, false: 显示latest_replies） */
  showAllReplies?: boolean;

  // ===== 兼容旧版字段 =====

  /** 旧版扩展字段（兼容） */
  author?: {
    id: number;
    username: string;
    avatar?: string;
  };
}

/**
 * 创建评论请求参数
 * 注意：user_id 由后端从登录态获取，无需前端传递
 */
export interface CreateCommentRequest {
  content: string;
  article_id: string;
  parent_id?: string | null;
}

/**
 * 评论列表响应
 */
export interface CommentListResponse {
  comments: Comment[];
  total_count: number;
  page: number;
  size: number;
  has_next: boolean;
  has_previous: boolean;
}

/**
 * 回复列表响应（与评论列表结构相同，但语义更清晰）
 */
export interface ReplyListResponse {
  replies: Comment[];
  total_count: number;
  page: number;
  size: number;
  has_next: boolean;
  has_previous: boolean;
}

/**
 * 评论列表状态（用于Hook状态管理）
 */
export interface CommentState {
  /** 顶级评论列表 */
  topLevelComments: Comment[];

  /** 顶级评论总数 */
  total: number;

  /** 当前页码 */
  page: number;

  /** 每页大小 */
  size: number;

  /** 是否正在加载 */
  loading: boolean;

  /** 是否有更多数据 */
  hasMore: boolean;
}

/**
 * 查询评论列表参数
 */
export interface GetCommentsParams {
  articleId: string;
  page?: number;
  size?: number;
  replyLimit?: number; // 新增：每个评论返回的最新回复数
}

/**
 * 查询回复列表参数
 */
export interface GetRepliesParams {
  parentId: string;
  page?: number;
  size?: number;
}

/**
 * 评论操作类型（用于优化更新）
 */
export type CommentActionType =
  | "create"
  | "delete"
  | "load_more_replies"
  | "collapse_replies";

/**
 * 评论操作事件
 */
export interface CommentAction {
  type: CommentActionType;
  commentId?: string;
  parentId?: string | null;
  payload?: any;
}
