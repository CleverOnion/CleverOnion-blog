import { useState, useCallback } from 'react';
import {
  getTopLevelCommentsWithReplies,
  getCommentReplies,
  postComment,
  deleteComment,
} from '../api/comments';
import type { Comment, CommentState } from '../types/comment';

/**
 * 评论管理 Hook（新版 v2.0.0 - 支持懒加载子评论）
 * 
 * 核心特性：
 * - 只对顶级评论分页
 * - 每个顶级评论默认显示最新3条回复
 * - 支持展开/收起所有回复
 * - 性能优化：按需加载，避免一次性加载大量数据
 * 
 * @param articleId - 文章ID
 * @returns 评论状态和操作方法
 * 
 * @example
 * ```tsx
 * const {
 *   topLevelComments,
 *   loading,
 *   loadComments,
 *   loadMoreReplies,
 *   postComment,
 * } = useCommentsV2('123');
 * 
 * useEffect(() => {
 *   loadComments(0);
 * }, [loadComments]);
 * ```
 */
export function useCommentsV2(articleId: string) {
  const [state, setState] = useState<CommentState>({
    topLevelComments: [],
    total: 0,
    page: 0,
    size: 10,
    loading: false,
    hasMore: false,
  });

  /**
   * 加载顶级评论（带最新回复）
   * 
   * @param page - 页码（默认0）
   */
  const loadComments = useCallback(
    async (page: number = 0) => {
      setState((prev) => ({ ...prev, loading: true }));

      try {
        const response = await getTopLevelCommentsWithReplies({
          articleId,
          page,
          size: state.size,
          replyLimit: 3, // 默认返回最新3条回复
        });

        if (response.success && response.data) {
          setState((prev) => ({
            ...prev,
            topLevelComments: response.data.comments,
            total: response.data.total_count,
            page,
            hasMore: response.data.has_next,
            loading: false,
          }));
        } else {
          console.error('加载评论失败:', response.message);
          setState((prev) => ({ ...prev, loading: false }));
        }
      } catch (error) {
        console.error('加载评论失败:', error);
        setState((prev) => ({ ...prev, loading: false }));
        throw error;
      }
    },
    [articleId, state.size]
  );

  /**
   * 加载某个评论的所有回复
   * 用户点击"查看全部N条回复"时调用
   * 
   * @param commentId - 评论ID
   */
  const loadMoreReplies = useCallback(async (commentId: string) => {
    try {
      const response = await getCommentReplies({
        parentId: commentId,
        page: 0,
        size: 100, // 一次性加载100条回复（如果更多可以再分页）
      });

      if (response.success && response.data) {
        setState((prev) => ({
          ...prev,
          topLevelComments: prev.topLevelComments.map((comment) =>
            comment.id === commentId
              ? {
                  ...comment,
                  replies: response.data.comments, // 使用comments字段（后端返回的）
                  repliesLoaded: true,
                  showAllReplies: true,
                }
              : comment
          ),
        }));
      } else {
        console.error('加载回复失败:', response.message);
      }
    } catch (error) {
      console.error('加载回复失败:', error);
      throw error;
    }
  }, []);

  /**
   * 折叠回复
   * 用户点击"收起回复"时调用
   * 
   * @param commentId - 评论ID
   */
  const collapseReplies = useCallback((commentId: string) => {
    setState((prev) => ({
      ...prev,
      topLevelComments: prev.topLevelComments.map((comment) =>
        comment.id === commentId
          ? { ...comment, showAllReplies: false }
          : comment
      ),
    }));
  }, []);

  /**
   * 发表评论或回复
   * 
   * @param content - 评论内容
   * @param parentId - 父评论ID（顶级评论为null）
   */
  const handlePostComment = useCallback(
    async (content: string, parentId?: string | null) => {
      try {
        const response = await postComment({
          content,
          article_id: articleId,
          parent_id: parentId,
        });

        if (response.success) {
          if (!parentId) {
            // 发表顶级评论：重新加载第一页
            await loadComments(0);
          } else {
            // 发表回复：重新加载该评论的回复
            await loadMoreReplies(parentId);
          }
        } else {
          console.error('发表评论失败:', response.message);
          throw new Error(response.message);
        }
      } catch (error) {
        console.error('发表评论失败:', error);
        throw error;
      }
    },
    [articleId, loadComments, loadMoreReplies]
  );

  /**
   * 删除评论
   * 
   * @param commentId - 评论ID
   * @param parentId - 父评论ID（如果是回复）
   */
  const handleDeleteComment = useCallback(
    async (commentId: string, parentId?: string | null) => {
      try {
        const response = await deleteComment(commentId);

        if (response.success) {
          if (!parentId) {
            // 删除顶级评论：重新加载当前页
            await loadComments(state.page);
          } else {
            // 删除回复：从列表中移除
            setState((prev) => ({
              ...prev,
              topLevelComments: prev.topLevelComments.map((comment) =>
                comment.id === parentId
                  ? {
                      ...comment,
                      reply_count: Math.max(0, (comment.reply_count || 0) - 1),
                      replies: comment.replies?.filter((r) => r.id !== commentId),
                      latest_replies: comment.latest_replies?.filter(
                        (r) => r.id !== commentId
                      ),
                    }
                  : comment
              ),
            }));
          }
        } else {
          console.error('删除评论失败:', response.message);
          throw new Error(response.message);
        }
      } catch (error) {
        console.error('删除评论失败:', error);
        throw error;
      }
    },
    [state.page, loadComments]
  );

  /**
   * 切换到下一页
   */
  const nextPage = useCallback(() => {
    if (state.hasMore && !state.loading) {
      loadComments(state.page + 1);
    }
  }, [state.hasMore, state.loading, state.page, loadComments]);

  /**
   * 切换到上一页
   */
  const prevPage = useCallback(() => {
    if (state.page > 0 && !state.loading) {
      loadComments(state.page - 1);
    }
  }, [state.page, state.loading, loadComments]);

  /**
   * 刷新当前页
   */
  const refresh = useCallback(() => {
    loadComments(state.page);
  }, [state.page, loadComments]);

  return {
    // 状态
    topLevelComments: state.topLevelComments,
    total: state.total,
    page: state.page,
    size: state.size,
    loading: state.loading,
    hasMore: state.hasMore,

    // 操作方法
    loadComments,
    loadMoreReplies,
    collapseReplies,
    postComment: handlePostComment,
    deleteComment: handleDeleteComment,
    nextPage,
    prevPage,
    refresh,
  };
}

/**
 * Hook 返回值类型（用于类型推导）
 */
export type UseCommentsV2Return = ReturnType<typeof useCommentsV2>;

