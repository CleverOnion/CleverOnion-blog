import React, { useState, useEffect } from "react";
import { motion } from "motion/react";
import { FaReply, FaTrash } from "react-icons/fa";
import { useCommentsV2 } from "../../hooks/useCommentsV2";
import { AuthUtils, UserInfo } from "../../api/auth";
import { useToast } from "../ui/Toast";
import Modal from "../ui/Modal";
import Button from "../ui/Button";
import type { Comment, CommentUser } from "../../types/comment";

interface CommentSectionProps {
  articleId: string;
}

interface CommentWithAuthor extends Comment {
  timeAgo: string;
}

/**
 * 评论区组件（支持懒加载子评论）
 *
 * 核心特性：
 * - 只对顶级评论分页
 * - 每个顶级评论默认显示最新3条回复
 * - 支持展开/收起所有回复
 * - 性能优化：批量查询，避免N+1问题
 *
 * @since 2.0.0
 */
const CommentSection: React.FC<CommentSectionProps> = ({ articleId }) => {
  const { error, warning, success } = useToast();
  const [newComment, setNewComment] = useState("");
  const [replyingTo, setReplyingTo] = useState<string | null>(null);
  const [replyContent, setReplyContent] = useState("");
  const [currentUser, setCurrentUser] = useState<UserInfo | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletingComment, setDeletingComment] = useState<{
    id: string;
    parentId: string | null;
  } | null>(null);

  // 使用新版评论Hook
  const {
    topLevelComments,
    total,
    page,
    loading,
    hasMore,
    loadComments,
    loadMoreReplies,
    collapseReplies,
    postComment: hookPostComment,
    deleteComment: hookDeleteComment,
  } = useCommentsV2(articleId);

  // 获取当前用户信息
  useEffect(() => {
    const userInfo = AuthUtils.getUserInfo();
    setCurrentUser(userInfo);
  }, []);

  // 初始加载评论
  useEffect(() => {
    if (articleId) {
      loadComments(0);
    }
  }, [articleId, loadComments]);

  // 格式化时间
  const formatTimeAgo = (dateString: string): string => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffInSeconds < 60) return "刚刚";
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}分钟前`;
    if (diffInSeconds < 86400)
      return `${Math.floor(diffInSeconds / 3600)}小时前`;
    if (diffInSeconds < 2592000)
      return `${Math.floor(diffInSeconds / 86400)}天前`;
    if (diffInSeconds < 31536000)
      return `${Math.floor(diffInSeconds / 2592000)}个月前`;
    return `${Math.floor(diffInSeconds / 31536000)}年前`;
  };

  // 转换评论数据
  const transformComment = (comment: Comment): CommentWithAuthor => {
    return {
      ...comment,
      timeAgo: formatTimeAgo(comment.published_at),
    };
  };

  // 查找回复目标用户
  const findReplyTarget = (
    reply: Comment,
    parentComment: Comment
  ): CommentUser | null => {
    if (!reply.parent_id) return null;

    // 如果回复的是顶级评论，返回顶级评论作者
    if (reply.parent_id === parentComment.id) {
      return parentComment.user;
    }

    // 在顶级评论的所有回复中查找父回复
    const allReplies = parentComment.showAllReplies
      ? parentComment.replies
      : parentComment.latest_replies;

    if (!allReplies) return null;

    const parentReply = allReplies.find((r) => r.id === reply.parent_id);
    return parentReply ? parentReply.user : null;
  };

  // 发表评论
  const handleSubmitComment = async () => {
    if (!currentUser) {
      warning("请先登录后再发表评论", { title: "需要登录" });
      return;
    }

    if (!newComment.trim()) {
      warning("评论内容不能为空", { title: "输入提示" });
      return;
    }

    try {
      setSubmitting(true);
      await hookPostComment(newComment.trim());
      setNewComment("");
      success("评论发表成功！", { title: "操作成功" });
    } catch (err) {
      console.error("发表评论失败:", err);
      error("发表评论失败，请稍后重试", { title: "操作失败" });
    } finally {
      setSubmitting(false);
    }
  };

  // 回复评论
  const handleSubmitReply = async (parentId: string) => {
    if (!currentUser) {
      warning("请先登录后再回复评论", { title: "需要登录" });
      return;
    }

    if (!replyContent.trim()) {
      warning("回复内容不能为空", { title: "输入提示" });
      return;
    }

    try {
      setSubmitting(true);
      await hookPostComment(replyContent.trim(), parentId);
      setReplyContent("");
      setReplyingTo(null);
      success("回复发表成功！", { title: "操作成功" });
    } catch (err) {
      console.error("回复评论失败:", err);
      error("回复评论失败，请稍后重试", { title: "操作失败" });
    } finally {
      setSubmitting(false);
    }
  };

  // 打开删除确认Modal
  const handleDeleteComment = (commentId: string, parentId: string | null) => {
    if (!currentUser) {
      warning("请先登录", { title: "需要登录" });
      return;
    }

    setDeletingComment({ id: commentId, parentId });
    setShowDeleteModal(true);
  };

  // 确认删除评论
  const handleConfirmDelete = async () => {
    if (!deletingComment) return;

    try {
      await hookDeleteComment(deletingComment.id, deletingComment.parentId);
      success("评论删除成功！", { title: "操作成功" });
    } catch (err) {
      console.error("删除评论失败:", err);
      error("删除评论失败，请稍后重试", { title: "操作失败" });
    } finally {
      setShowDeleteModal(false);
      setDeletingComment(null);
    }
  };

  // 关闭删除确认Modal
  const handleCloseDeleteModal = () => {
    setShowDeleteModal(false);
    setDeletingComment(null);
  };

  // 加载更多顶级评论
  const handleLoadMore = () => {
    if (hasMore && !loading) {
      loadComments(page + 1);
    }
  };

  // 展开某评论的所有回复
  const handleLoadAllReplies = async (commentId: string) => {
    try {
      await loadMoreReplies(commentId);
    } catch (err) {
      console.error("加载回复失败:", err);
      error("加载回复失败，请稍后重试", { title: "操作失败" });
    }
  };

  // 渲染单条回复
  const renderReply = (reply: Comment, parentComment: Comment) => {
    const replyWithTime = transformComment(reply);
    const replyTarget = findReplyTarget(reply, parentComment);

    return (
      <div
        key={reply.id}
        className="ml-6 mt-4 pl-4 border-l-2 border-blue-100 bg-gray-50/30 py-4 rounded-r-lg"
      >
        <div className="flex items-start space-x-3">
          {/* 头像 */}
          <img
            src={reply.user.avatar_url || "/default-avatar.svg"}
            alt={reply.user.username}
            loading="lazy"
            decoding="async"
            className="w-7 h-7 rounded-full flex-shrink-0 object-cover ring-2 ring-white shadow-sm"
          />

          {/* 评论内容 */}
          <div className="flex-1 min-w-0">
            {/* 用户信息和时间 */}
            <div className="flex items-center space-x-2 mb-1">
              <span className="font-semibold text-gray-900 text-xs">
                {reply.user.username}
              </span>
              {/* 回复目标显示 */}
              {replyTarget && (
                <>
                  <span className="text-xs text-gray-400">回复</span>
                  <span className="text-xs font-medium text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">
                    @{replyTarget.username}
                  </span>
                </>
              )}
              <span className="text-xs text-gray-500">·</span>
              <span className="text-xs text-gray-500">
                {replyWithTime.timeAgo}
              </span>
              {/* 删除按钮 */}
              {currentUser && currentUser.id === reply.user.github_id && (
                <button
                  onClick={() =>
                    handleDeleteComment(reply.id, parentComment.id)
                  }
                  className="ml-auto text-xs text-gray-400 hover:text-red-500 transition-colors p-1 rounded hover:bg-red-50"
                  title="删除评论"
                  aria-label="删除评论"
                >
                  <FaTrash className="w-3 h-3" />
                </button>
              )}
            </div>

            {/* 评论文本 */}
            <p className="text-gray-800 text-xs leading-relaxed mb-3 text-left">
              {reply.content}
            </p>

            {/* 操作按钮 */}
            <div className="flex items-center space-x-4">
              {currentUser && (
                <button
                  onClick={() =>
                    setReplyingTo(replyingTo === reply.id ? null : reply.id)
                  }
                  className="flex items-center space-x-1 text-xs text-gray-500 hover:text-blue-600 transition-colors font-medium hover:bg-blue-50 px-2 py-1 rounded-md"
                  aria-label={`回复 ${reply.user.username}`}
                >
                  <FaReply className="w-3 h-3" />
                  <span>回复</span>
                </button>
              )}
            </div>

            {/* 回复表单 */}
            {replyingTo === reply.id && (
              <div className="mt-4 p-4 bg-white rounded-lg border border-gray-200 shadow-sm">
                <div className="mb-2">
                  <span className="text-xs text-gray-500">回复 </span>
                  <span className="text-xs font-medium text-blue-600">
                    @{reply.user.username}
                  </span>
                </div>
                <label htmlFor={`reply-${reply.id}`} className="sr-only">
                  回复 {reply.user.username}
                </label>
                <textarea
                  id={`reply-${reply.id}`}
                  value={replyContent}
                  onChange={(e) => setReplyContent(e.target.value)}
                  className="w-full p-3 border border-gray-200 rounded-lg resize-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all text-sm"
                  rows={3}
                  placeholder={`回复 ${reply.user.username}...`}
                  maxLength={1000}
                  aria-describedby={`reply-char-count-${reply.id}`}
                />
                <div className="flex justify-between items-center mt-3">
                  <span
                    id={`reply-char-count-${reply.id}`}
                    className="text-xs text-gray-500"
                    aria-live="polite"
                  >
                    {replyContent.length}/1000
                  </span>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => {
                        setReplyingTo(null);
                        setReplyContent("");
                      }}
                      className="px-3 py-1.5 text-xs text-gray-600 hover:text-gray-800 transition-colors hover:bg-gray-100 rounded-md"
                    >
                      取消
                    </button>
                    <button
                      onClick={() => handleSubmitReply(parentComment.id)}
                      disabled={submitting || !replyContent.trim()}
                      className="px-4 py-1.5 bg-blue-600 text-white text-xs rounded-md hover:bg-blue-700 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
                    >
                      {submitting ? "发送中..." : "发表回复"}
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  // 渲染顶级评论
  const renderComment = (comment: Comment) => {
    const commentWithTime = transformComment(comment);
    const replyCount = comment.reply_count || 0;

    // 确定显示哪些回复（展开状态显示全部，否则显示最新3条）
    const showingReplies = comment.showAllReplies
      ? comment.replies
      : comment.latest_replies;

    return (
      <div
        key={comment.id}
        className="border-b border-gray-100 last:border-b-0 px-4 py-4"
      >
        <div className="flex items-start space-x-3">
          {/* 头像 */}
          <img
            src={comment.user.avatar_url || "/default-avatar.svg"}
            alt={comment.user.username}
            loading="lazy"
            decoding="async"
            className="w-8 h-8 rounded-full flex-shrink-0 object-cover ring-2 ring-white shadow-sm"
          />

          {/* 评论内容 */}
          <div className="flex-1 min-w-0">
            {/* 用户信息和时间 */}
            <div className="flex items-center space-x-2 mb-1">
              <span className="font-semibold text-gray-900 text-sm">
                {comment.user.username}
              </span>
              <span className="text-xs text-gray-500">·</span>
              <span className="text-xs text-gray-500">
                {commentWithTime.timeAgo}
              </span>
              {/* 删除按钮 */}
              {currentUser && currentUser.id === comment.user.github_id && (
                <button
                  onClick={() => handleDeleteComment(comment.id, null)}
                  className="ml-auto text-xs text-gray-400 hover:text-red-500 transition-colors p-1 rounded hover:bg-red-50"
                  title="删除评论"
                  aria-label="删除评论"
                >
                  <FaTrash className="w-3 h-3" />
                </button>
              )}
            </div>

            {/* 评论文本 */}
            <p className="text-gray-800 text-sm leading-relaxed mb-3 text-left">
              {comment.content}
            </p>

            {/* 操作按钮 */}
            <div className="flex items-center space-x-4">
              {/* 回复按钮 */}
              {currentUser && (
                <button
                  onClick={() =>
                    setReplyingTo(replyingTo === comment.id ? null : comment.id)
                  }
                  className="flex items-center space-x-1 text-xs text-gray-500 hover:text-blue-600 transition-colors font-medium hover:bg-blue-50 px-2 py-1 rounded-md"
                  aria-label={`回复 ${comment.user.username}`}
                >
                  <FaReply className="w-3 h-3" />
                  <span>回复</span>
                </button>
              )}

              {/* 回复数量 */}
              {replyCount > 0 && (
                <span className="text-xs text-gray-500 font-medium bg-gray-100 px-2 py-1 rounded-full">
                  {replyCount} 条回复
                </span>
              )}
            </div>

            {/* 回复表单 */}
            {replyingTo === comment.id && (
              <div className="mt-4 p-4 bg-white rounded-lg border border-gray-200 shadow-sm">
                <div className="mb-2">
                  <span className="text-xs text-gray-500">回复 </span>
                  <span className="text-xs font-medium text-blue-600">
                    @{comment.user.username}
                  </span>
                </div>
                <label htmlFor={`reply-${comment.id}`} className="sr-only">
                  回复 {comment.user.username}
                </label>
                <textarea
                  id={`reply-${comment.id}`}
                  value={replyContent}
                  onChange={(e) => setReplyContent(e.target.value)}
                  className="w-full p-3 border border-gray-200 rounded-lg resize-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all text-sm"
                  rows={3}
                  placeholder={`回复 ${comment.user.username}...`}
                  maxLength={1000}
                  aria-describedby={`reply-char-count-${comment.id}`}
                />
                <div className="flex justify-between items-center mt-3">
                  <span
                    id={`reply-char-count-${comment.id}`}
                    className="text-xs text-gray-500"
                    aria-live="polite"
                  >
                    {replyContent.length}/1000
                  </span>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => {
                        setReplyingTo(null);
                        setReplyContent("");
                      }}
                      className="px-3 py-1.5 text-xs text-gray-600 hover:text-gray-800 transition-colors hover:bg-gray-100 rounded-md"
                    >
                      取消
                    </button>
                    <button
                      onClick={() => handleSubmitReply(comment.id)}
                      disabled={submitting || !replyContent.trim()}
                      className="px-4 py-1.5 bg-blue-600 text-white text-xs rounded-md hover:bg-blue-700 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
                    >
                      {submitting ? "发送中..." : "发表回复"}
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* 回复列表区域 */}
            {showingReplies && showingReplies.length > 0 && (
              <div className="mt-4 space-y-0">
                {showingReplies.map((reply) => renderReply(reply, comment))}

                {/* 加载更多回复按钮 */}
                {!comment.showAllReplies && replyCount > 3 && (
                  <div className="ml-6 mt-4">
                    <button
                      onClick={() => handleLoadAllReplies(comment.id)}
                      className="text-sm text-blue-600 hover:text-blue-800 font-medium hover:bg-blue-50 px-3 py-1.5 rounded-md transition-colors"
                      aria-label={`查看全部 ${replyCount} 条回复`}
                    >
                      查看全部 {replyCount} 条回复 →
                    </button>
                  </div>
                )}

                {/* 收起回复按钮 */}
                {comment.showAllReplies && (
                  <div className="ml-6 mt-4">
                    <button
                      onClick={() => collapseReplies(comment.id)}
                      className="text-sm text-gray-600 hover:text-gray-800 font-medium hover:bg-gray-100 px-3 py-1.5 rounded-md transition-colors"
                      aria-label="收起回复"
                    >
                      收起回复 ↑
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* 回复入口提示（当没有显示回复但有回复数时） */}
            {(!showingReplies || showingReplies.length === 0) &&
              replyCount > 0 && (
                <div className="mt-4 ml-6">
                  <button
                    onClick={() => handleLoadAllReplies(comment.id)}
                    className="text-sm text-blue-600 hover:text-blue-800 font-medium hover:bg-blue-50 px-3 py-1.5 rounded-md transition-colors"
                    aria-label={`查看 ${replyCount} 条回复`}
                  >
                    {replyCount} 条回复 →
                  </button>
                </div>
              )}
          </div>
        </div>
      </div>
    );
  };

  return (
    <motion.section
      className="max-w-4xl mx-auto"
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.8, delay: 0.6, ease: "easeOut" }}
    >
      <div className="mb-12">
        <div className="text-center mb-8">
          <h2 className="text-pink-500 font-semibold text-lg uppercase tracking-wider mb-2">
            评论
          </h2>
          <p className="text-gray-600 text-sm">
            {loading && topLevelComments.length === 0
              ? "正在加载评论..."
              : `${total} 条评论`}
          </p>
        </div>
      </div>

      {/* 评论表单 */}
      {currentUser ? (
        <div className="mb-8 bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-start space-x-3">
            <img
              src={currentUser.avatarUrl || "/default-avatar.svg"}
              alt={currentUser.username}
              className="w-8 h-8 rounded-full flex-shrink-0 object-cover"
            />
            <div className="flex-1">
              <label htmlFor="new-comment" className="sr-only">
                评论内容
              </label>
              <textarea
                id="new-comment"
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                className="w-full p-3 border border-gray-200 rounded-md resize-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500 transition-all text-sm"
                rows={3}
                placeholder="写下你的评论..."
                maxLength={1000}
                aria-describedby="comment-char-count"
              />
              <div className="flex justify-between items-center mt-3">
                <span
                  id="comment-char-count"
                  className="text-xs text-gray-500"
                  aria-live="polite"
                >
                  {newComment.length}/1000
                </span>
                <button
                  onClick={handleSubmitComment}
                  disabled={submitting || !newComment.trim()}
                  className="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {submitting ? "发表中..." : "发表评论"}
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <div className="mb-8 bg-white rounded-lg border border-gray-200 p-4">
          <p className="text-gray-600 mb-3 text-sm">请登录后发表评论</p>
          <button
            onClick={() => (window.location.href = "/auth/login")}
            className="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 transition-colors font-medium"
          >
            登录
          </button>
        </div>
      )}

      {/* 评论列表 */}
      <div className="bg-white rounded-lg border border-gray-200">
        {loading && topLevelComments.length === 0 ? (
          <div className="p-4">
            <div className="animate-pulse space-y-4">
              {[...Array(3)].map((_, index) => (
                <div
                  key={index}
                  className="flex items-start space-x-3 py-4 border-b border-gray-100 last:border-b-0"
                >
                  <div className="w-8 h-8 bg-gray-200 rounded-full flex-shrink-0"></div>
                  <div className="flex-1">
                    <div className="h-3 bg-gray-200 rounded mb-2 w-1/4"></div>
                    <div className="h-3 bg-gray-200 rounded mb-2 w-full"></div>
                    <div className="h-3 bg-gray-200 rounded w-3/4"></div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : topLevelComments.length > 0 ? (
          <>
            <div className="divide-y divide-gray-100">
              {topLevelComments.map((comment) => renderComment(comment))}
            </div>

            {/* 加载更多顶级评论按钮 */}
            {hasMore && (
              <div className="p-4 border-t border-gray-200 text-center">
                <button
                  onClick={handleLoadMore}
                  disabled={loading}
                  className="px-4 py-2 bg-gray-100 text-gray-700 text-sm rounded-md hover:bg-gray-200 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? "加载中..." : "加载更多评论"}
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="p-8 text-gray-500 text-center">
            <p className="text-base mb-1">暂无评论</p>
            <p className="text-sm">成为第一个发表评论的人吧！</p>
          </div>
        )}
      </div>

      {/* 删除确认Modal */}
      <Modal
        isOpen={showDeleteModal}
        onClose={handleCloseDeleteModal}
        title="确认删除"
        size="sm"
      >
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            确定要删除这条评论吗？删除后无法恢复。
          </p>

          <div className="flex flex-col sm:flex-row justify-end space-y-3 sm:space-y-0 sm:space-x-3 mt-6 pt-4 border-t border-gray-100">
            <Button
              variant="secondary"
              onClick={handleCloseDeleteModal}
              fullWidth
              className="sm:w-auto"
            >
              取消
            </Button>
            <Button
              variant="danger"
              onClick={handleConfirmDelete}
              fullWidth
              className="sm:w-auto"
            >
              确认删除
            </Button>
          </div>
        </div>
      </Modal>
    </motion.section>
  );
};

export default CommentSection;
