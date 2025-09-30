import React, { useState, useEffect } from "react";
import { motion } from "motion/react";
import { FaReply, FaTrash } from "react-icons/fa";
import { commentApi, Comment, CreateCommentRequest } from "../../api/comments";
import { AuthUtils, UserInfo } from "../../api/auth";
import { useToast } from "../ui/Toast";
import Modal from "../ui/Modal";
import Button from "../ui/Button";

interface CommentSectionProps {
  articleId: string;
}

interface CommentWithAuthor extends Comment {
  author: {
    id: number;
    username: string;
    avatar?: string;
  };
  timeAgo: string;
  replies?: CommentWithAuthor[];
}

const CommentSection: React.FC<CommentSectionProps> = ({ articleId }) => {
  const { error, warning, success } = useToast();
  const [comments, setComments] = useState<CommentWithAuthor[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [newComment, setNewComment] = useState("");
  const [replyingTo, setReplyingTo] = useState<string | null>(null);
  const [replyContent, setReplyContent] = useState("");
  const [currentUser, setCurrentUser] = useState<UserInfo | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [totalCount, setTotalCount] = useState(0);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletingComment, setDeletingComment] = useState<string | null>(null);

  // 获取当前用户信息
  useEffect(() => {
    const userInfo = AuthUtils.getUserInfo();
    setCurrentUser(userInfo);
  }, []);

  // 加载评论列表
  const loadComments = async (pageNum: number = 0, append: boolean = false) => {
    try {
      setLoading(!append);
      const response = await commentApi.getComments({
        articleId,
        page: pageNum,
        size: 10,
      });

      if (response.code === 200) {
        const commentsWithAuthor = response.data.comments.map(transformComment);
        const organizedComments = organizeComments(commentsWithAuthor);

        if (append) {
          setComments((prev) => [...prev, ...organizedComments]);
        } else {
          setComments(organizedComments);
        }

        setTotalCount(response.data.total_count);
        setHasMore(response.data.has_next);
        setPage(pageNum);
      }
    } catch (error) {
      console.error("加载评论失败:", error);
    } finally {
      setLoading(false);
    }
  };

  // 初始加载评论
  useEffect(() => {
    if (articleId) {
      loadComments();
    }
  }, [articleId]);

  // 转换评论数据格式
  const transformComment = (comment: Comment): CommentWithAuthor => {
    return {
      ...comment,
      author: comment.user
        ? {
            id: comment.user.id,
            username: comment.user.username,
            avatar: comment.user.avatar_url,
          }
        : {
            id: comment.user_id,
            username: "匿名用户",
            avatar: "/default-avatar.svg",
          },
      timeAgo: formatTimeAgo(comment.published_at),
      replies: [],
    };
  };

  // 组织评论层级结构 - 保持完整逻辑层级，视觉上限制为2级显示
  const organizeComments = (
    flatComments: CommentWithAuthor[]
  ): CommentWithAuthor[] => {
    const commentMap = new Map<string, CommentWithAuthor>();
    const topLevelComments: CommentWithAuthor[] = [];

    // 先创建所有评论的映射
    flatComments.forEach((comment) => {
      commentMap.set(comment.id, { ...comment, replies: [] });
    });

    // 组织完整的层级关系
    flatComments.forEach((comment) => {
      const commentWithReplies = commentMap.get(comment.id)!;

      if (comment.parent_id && commentMap.has(comment.parent_id)) {
        const parent = commentMap.get(comment.parent_id)!;
        parent.replies = parent.replies || [];
        parent.replies.push(commentWithReplies);
      } else {
        topLevelComments.push(commentWithReplies);
      }
    });

    return topLevelComments;
  };

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

  // 查找被回复的用户信息（用于深层回复）
  const findReplyTarget = (
    comment: CommentWithAuthor,
    allComments: CommentWithAuthor[]
  ): { id: number; username: string; avatar?: string } | null => {
    if (!comment.parent_id) return null;

    const findCommentById = (
      comments: CommentWithAuthor[],
      id: string
    ): CommentWithAuthor | null => {
      for (const c of comments) {
        if (c.id === id) return c;
        if (c.replies) {
          const found = findCommentById(c.replies, id);
          if (found) return found;
        }
      }
      return null;
    };

    const parent = findCommentById(allComments, comment.parent_id);
    return parent ? parent.author : null;
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
      const commentData: CreateCommentRequest = {
        content: newComment.trim(),
        article_id: articleId,
        user_id: currentUser.id,
      };

      const response = await commentApi.createComment(commentData);

      if (response.code === 200) {
        setNewComment("");
        success("评论发表成功！", { title: "操作成功" });
        // 重新加载评论列表
        await loadComments();
      }
    } catch (error) {
      console.error("发表评论失败:", error);
      error?.("发表评论失败，请稍后重试", { title: "操作失败" });
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
      const replyData: CreateCommentRequest = {
        content: replyContent.trim(),
        article_id: articleId,
        user_id: currentUser.id,
        parent_id: parentId,
      };

      const response = await commentApi.createComment(replyData);

      if (response.code === 200) {
        setReplyContent("");
        setReplyingTo(null);
        success("回复发表成功！", { title: "操作成功" });
        // 重新加载评论列表
        await loadComments();
      }
    } catch (error) {
      console.error("回复评论失败:", error);
      error("回复评论失败，请稍后重试", { title: "操作失败" });
    } finally {
      setSubmitting(false);
    }
  };

  // 打开删除确认Modal
  const handleDeleteComment = (commentId: string) => {
    if (!currentUser) {
      warning("请先登录", { title: "需要登录" });
      return;
    }

    setDeletingComment(commentId);
    setShowDeleteModal(true);
  };

  // 确认删除评论
  const handleConfirmDelete = async () => {
    if (!deletingComment) return;

    try {
      const response = await commentApi.deleteComment(deletingComment);

      if (response.code === 200) {
        success("评论删除成功！", { title: "操作成功" });
        // 重新加载评论列表
        await loadComments();
      }
    } catch (error) {
      console.error("删除评论失败:", error);
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

  // 加载更多评论
  const handleLoadMore = () => {
    if (hasMore && !loading) {
      loadComments(page + 1, true);
    }
  };

  const renderComment = (comment: CommentWithAuthor, isReply = false) => (
    <div
      key={comment.id}
      className={`${
        isReply
          ? "ml-6 mt-4 pl-4 border-l-2 border-blue-100 bg-gray-50/30"
          : "border-b border-gray-100 last:border-b-0 px-4"
      } py-4 ${isReply ? "rounded-r-lg" : ""}`}
    >
      <div className="flex items-start space-x-3">
        {/* 头像 */}
        <img
          src={comment.author.avatar || "/default-avatar.svg"}
          alt={comment.author.username}
          loading="lazy"
          decoding="async"
          className={`${
            isReply ? "w-7 h-7" : "w-8 h-8"
          } rounded-full flex-shrink-0 object-cover ring-2 ring-white shadow-sm`}
        />

        {/* 评论内容 */}
        <div className="flex-1 min-w-0">
          {/* 用户信息和时间 */}
          <div className="flex items-center space-x-2 mb-1">
            <span
              className={`font-semibold text-gray-900 ${
                isReply ? "text-xs" : "text-sm"
              }`}
            >
              {comment.author.username}
            </span>
            {/* 回复目标显示 - 对于回复显示被回复的用户 */}
            {isReply &&
              comment.parent_id &&
              (() => {
                const replyTarget = findReplyTarget(comment, comments);
                return replyTarget ? (
                  <>
                    <span className="text-xs text-gray-400">回复</span>
                    <span className="text-xs font-medium text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">
                      @{replyTarget.username}
                    </span>
                  </>
                ) : null;
              })()}
            <span className="text-xs text-gray-500">·</span>
            <span className="text-xs text-gray-500">{comment.timeAgo}</span>
            {/* 删除按钮 - 只有评论作者可以删除 */}
            {currentUser && currentUser.id === comment.author.id && (
              <button
                onClick={() => handleDeleteComment(comment.id)}
                className="ml-auto text-xs text-gray-400 hover:text-red-500 transition-colors p-1 rounded hover:bg-red-50"
                title="删除评论"
              >
                <FaTrash className="w-3 h-3" />
              </button>
            )}
          </div>

          {/* 评论文本 */}
          <p
            className={`text-gray-800 ${
              isReply ? "text-xs" : "text-sm"
            } leading-relaxed mb-3 text-left`}
          >
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
              >
                <FaReply className="w-3 h-3" />
                <span>回复</span>
              </button>
            )}

            {/* 回复数量 */}
            {comment.replies && comment.replies.length > 0 && (
              <span className="text-xs text-gray-500 font-medium bg-gray-100 px-2 py-1 rounded-full">
                {comment.replies.length} 条回复
              </span>
            )}
          </div>

          {/* 回复表单 */}
          {replyingTo === comment.id && (
            <div className="mt-4 p-4 bg-white rounded-lg border border-gray-200 shadow-sm">
              <div className="mb-2">
                <span className="text-xs text-gray-500">回复 </span>
                <span className="text-xs font-medium text-blue-600">
                  @{comment.author.username}
                </span>
              </div>
              <label htmlFor={`reply-${comment.id}`} className="sr-only">
                回复 {comment.author.username}
              </label>
              <textarea
                id={`reply-${comment.id}`}
                value={replyContent}
                onChange={(e) => setReplyContent(e.target.value)}
                className="w-full p-3 border border-gray-200 rounded-lg resize-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all text-sm"
                rows={3}
                placeholder={`回复 ${comment.author.username}...`}
                maxLength={1000}
                aria-label={`回复 ${comment.author.username}`}
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

          {/* 回复列表 - 扁平化显示所有回复 */}
          {comment.replies && comment.replies.length > 0 && (
            <div className="mt-4 space-y-0">
              {(() => {
                // 递归收集所有回复，扁平化显示
                const collectAllReplies = (
                  replies: CommentWithAuthor[]
                ): CommentWithAuthor[] => {
                  const result: CommentWithAuthor[] = [];
                  replies.forEach((reply) => {
                    result.push(reply);
                    if (reply.replies && reply.replies.length > 0) {
                      result.push(...collectAllReplies(reply.replies));
                    }
                  });
                  return result;
                };

                const allReplies = collectAllReplies(comment.replies);

                return allReplies.map((reply) => (
                  <div
                    key={reply.id}
                    className="ml-6 mt-4 pl-4 border-l-2 border-blue-100 bg-gray-50/30 py-4 rounded-r-lg"
                  >
                    <div className="flex items-start space-x-3">
                      {/* 头像 */}
                      <img
                        src={reply.author.avatar || "/default-avatar.svg"}
                        alt={reply.author.username}
                        loading="lazy"
                        decoding="async"
                        className="w-7 h-7 rounded-full flex-shrink-0 object-cover ring-2 ring-white shadow-sm"
                      />

                      {/* 评论内容 */}
                      <div className="flex-1 min-w-0">
                        {/* 用户信息和时间 */}
                        <div className="flex items-center space-x-2 mb-1">
                          <span className="font-semibold text-gray-900 text-xs">
                            {reply.author.username}
                          </span>
                          {/* 回复目标显示 - 对于回复显示被回复的用户 */}
                          {reply.parent_id &&
                            (() => {
                              const replyTarget = findReplyTarget(
                                reply,
                                comments
                              );
                              return replyTarget ? (
                                <>
                                  <span className="text-xs text-gray-400">
                                    回复
                                  </span>
                                  <span className="text-xs font-medium text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">
                                    @{replyTarget.username}
                                  </span>
                                </>
                              ) : null;
                            })()}
                          <span className="text-xs text-gray-500">·</span>
                          <span className="text-xs text-gray-500">
                            {reply.timeAgo}
                          </span>
                          {/* 删除按钮 - 只有评论作者可以删除 */}
                          {currentUser &&
                            currentUser.id === reply.author.id && (
                              <button
                                onClick={() => handleDeleteComment(reply.id)}
                                className="ml-auto text-xs text-gray-400 hover:text-red-500 transition-colors p-1 rounded hover:bg-red-50"
                                title="删除评论"
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
                          {/* 回复按钮 */}
                          {currentUser && (
                            <button
                              onClick={() =>
                                setReplyingTo(
                                  replyingTo === reply.id ? null : reply.id
                                )
                              }
                              className="flex items-center space-x-1 text-xs text-gray-500 hover:text-blue-600 transition-colors font-medium hover:bg-blue-50 px-2 py-1 rounded-md"
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
                              <span className="text-xs text-gray-500">
                                回复{" "}
                              </span>
                              <span className="text-xs font-medium text-blue-600">
                                @{reply.author.username}
                              </span>
                            </div>
                            <label
                              htmlFor={`reply-nested-${reply.id}`}
                              className="sr-only"
                            >
                              回复 {reply.author.username}
                            </label>
                            <textarea
                              id={`reply-nested-${reply.id}`}
                              value={replyContent}
                              onChange={(e) => setReplyContent(e.target.value)}
                              className="w-full p-3 border border-gray-200 rounded-lg resize-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all text-sm"
                              rows={3}
                              placeholder={`回复 ${reply.author.username}...`}
                              maxLength={1000}
                              aria-label={`回复 ${reply.author.username}`}
                              aria-describedby={`reply-nested-char-count-${reply.id}`}
                            />
                            <div className="flex justify-between items-center mt-3">
                              <span
                                id={`reply-nested-char-count-${reply.id}`}
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
                                  onClick={() => handleSubmitReply(reply.id)}
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
                ));
              })()}
            </div>
          )}
        </div>
      </div>
    </div>
  );

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
            {loading && comments.length === 0
              ? "正在加载评论..."
              : `${totalCount} 条评论`}
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
                aria-label="评论内容"
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
        {loading && comments.length === 0 ? (
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
        ) : comments.length > 0 ? (
          <>
            <div className="divide-y divide-gray-100">
              {comments.map((comment) => renderComment(comment))}
            </div>

            {/* 加载更多按钮 */}
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
