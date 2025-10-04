# 评论系统优化设计方案

## 一、设计理念

### 1.1 核心原则

- **顶级评论分页**：只对顶级评论（parent_id = null）进行分页，保证每页显示固定数量的完整评论单元
- **子评论懒加载**：每个顶级评论默认显示前 N 条子评论，点击"查看更多回复"按钮加载剩余子评论
- **性能优化**：避免一次性加载大量数据，按需加载
- **用户体验**：保证评论的完整性和上下文连贯性

### 1.2 业界参考

- **知乎**：顶级评论分页，每个评论下默认显示 3 条回复
- **B 站**：顶级评论分页，每个评论下默认显示 3 条回复，支持展开更多
- **微博**：类似设计，顶级评论分页 + 回复懒加载
- **GitHub**：支持评论展开/折叠，嵌套回复

## 二、后端接口设计

### 2.1 获取顶级评论列表（带统计信息）

```
GET /api/comments/top-level?articleId={articleId}&page={page}&size={size}
```

**请求参数：**

```json
{
  "articleId": 13, // 文章ID
  "page": 0, // 页码（从0开始）
  "size": 10 // 每页数量
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "comments": [
      {
        "id": "1",
        "content": "这是一条顶级评论",
        "article_id": "13",
        "user": {
          "id": null,
          "github_id": 152043550,
          "username": "CleverOnion",
          "avatar_url": "https://avatars.githubusercontent.com/u/152043550?v=4"
        },
        "parent_id": null,
        "is_top_level": true,
        "published_at": "2025-09-30 21:31:02",
        "reply_count": 5, // 子评论总数（新增）
        "latest_replies": [
          // 最新的3条子评论（新增）
          {
            "id": "2",
            "content": "这是一条回复",
            "article_id": "13",
            "user": {
              "github_id": 152043550,
              "username": "CleverOnion",
              "avatar_url": "https://avatars.githubusercontent.com/u/152043550?v=4"
            },
            "parent_id": "1",
            "is_top_level": false,
            "published_at": "2025-09-30 21:32:15"
          }
          // ... 最多3条
        ]
      }
    ],
    "total_count": 5, // 顶级评论总数
    "page": 0,
    "size": 10,
    "has_next": false,
    "has_previous": false
  },
  "timestamp": 1759583378474,
  "success": true
}
```

**关键改进点：**

1. `reply_count`：显示该评论的回复总数
2. `latest_replies`：默认返回最新的 3 条子评论
3. 只对顶级评论分页，保证每页显示完整的评论单元

---

### 2.2 获取评论的所有回复（分页）

```
GET /api/comments/replies?parentId={parentId}&page={page}&size={size}
```

**请求参数：**

```json
{
  "parentId": 1, // 父评论ID
  "page": 0, // 页码（从0开始）
  "size": 20 // 每页数量（建议20-50）
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "replies": [
      {
        "id": "2",
        "content": "这是一条回复",
        "article_id": "13",
        "user": {
          "github_id": 152043550,
          "username": "CleverOnion",
          "avatar_url": "https://avatars.githubusercontent.com/u/152043550?v=4"
        },
        "parent_id": "1",
        "is_top_level": false,
        "published_at": "2025-09-30 21:32:15"
      }
      // ... 更多回复
    ],
    "total_count": 25,
    "page": 0,
    "size": 20,
    "has_next": true,
    "has_previous": false
  },
  "timestamp": 1759583378474,
  "success": true
}
```

---

### 2.3 发表评论/回复

```
POST /api/comments
```

**请求体：**

```json
{
  "content": "评论内容",
  "article_id": "13",
  "parent_id": null // 顶级评论为null，回复填写父评论ID
}
```

**响应：**返回新创建的评论对象

---

### 2.4 删除评论

```
DELETE /api/comments/{commentId}
```

**业务规则：**

- 删除顶级评论时，同时删除所有子评论（级联删除）
- 删除子评论时，只删除该条评论
- 只有评论作者或管理员可以删除

---

### 2.5 获取评论总数统计

```
GET /api/comments/count?articleId={articleId}
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "total_comments": 35, // 总评论数（包括所有层级）
    "top_level_count": 10 // 顶级评论数
  }
}
```

## 三、后端实现要点

### 3.1 DTO 扩展

**CommentResponse 新增字段：**

```java
public class CommentResponse {
    // ... 原有字段

    @Schema(description = "回复总数")
    @JsonProperty("reply_count")
    private Integer replyCount;

    @Schema(description = "最新的几条回复（默认3条）")
    @JsonProperty("latest_replies")
    private List<CommentResponse> latestReplies;
}
```

### 3.2 Service 层优化

**CommentQueryService 新增方法：**

```java
/**
 * 获取顶级评论及其最新回复
 */
public List<CommentWithRepliesDTO> findTopLevelCommentsWithLatestReplies(
    ArticleId articleId, int page, int size, int replyLimit) {

    // 1. 查询顶级评论（分页）
    List<CommentAggregate> topComments =
        commentRepository.findTopLevelCommentsByArticleId(articleId, page, size);

    // 2. 批量查询每个顶级评论的回复数和最新回复
    return topComments.stream().map(comment -> {
        CommentId commentId = comment.getId();

        // 统计回复数
        long replyCount = commentRepository.countRepliesByParentId(commentId);

        // 获取最新的N条回复
        List<CommentAggregate> latestReplies =
            commentRepository.findByParentId(commentId, 0, replyLimit);

        return new CommentWithRepliesDTO(comment, replyCount, latestReplies);
    }).collect(Collectors.toList());
}
```

### 3.3 性能优化建议

1. **批量查询优化**：

   - 使用 `@BatchSize` 注解或手动批量查询，避免 N+1 问题
   - 一次性查询所有顶级评论的回复数

2. **缓存策略**：

   ```java
   @Cacheable(value = "comments", key = "'article_' + #articleId + '_page_' + #page")
   public List<CommentWithRepliesDTO> findTopLevelCommentsWithLatestReplies(...)
   ```

3. **数据库索引**：

   ```sql
   -- 文章ID索引（已有）
   CREATE INDEX idx_article_id ON comments(article_id);

   -- 父评论ID索引（已有）
   CREATE INDEX idx_parent_id ON comments(parent_id);

   -- 复合索引：文章ID + 父评论ID + 创建时间
   CREATE INDEX idx_article_parent_created ON comments(article_id, parent_id, created_at);
   ```

## 四、前端实现方案

### 4.1 数据结构设计

```typescript
// 评论数据类型
interface Comment {
  id: string;
  content: string;
  article_id: string;
  user: {
    github_id: number;
    username: string;
    avatar_url: string;
  };
  parent_id: string | null;
  is_top_level: boolean;
  published_at: string;
  reply_count?: number; // 顶级评论才有
  latest_replies?: Comment[]; // 顶级评论才有
  replies?: Comment[]; // 前端加载的所有回复
  repliesLoaded?: boolean; // 是否已加载所有回复
  showAllReplies?: boolean; // 是否展示所有回复
}

// 评论列表状态
interface CommentState {
  topLevelComments: Comment[]; // 顶级评论列表
  total: number; // 顶级评论总数
  page: number; // 当前页
  size: number; // 每页大小
  loading: boolean; // 加载状态
  hasMore: boolean; // 是否有更多
}
```

### 4.2 组件结构

```
CommentSection (评论区容器)
├── CommentList (评论列表)
│   ├── CommentItem (单个顶级评论)
│   │   ├── CommentHeader (评论头部：用户信息、时间)
│   │   ├── CommentContent (评论内容)
│   │   ├── CommentActions (操作按钮：回复、删除)
│   │   └── ReplyList (回复列表)
│   │       ├── ReplyItem (单条回复)
│   │       │   ├── ReplyHeader
│   │       │   ├── ReplyContent
│   │       │   └── ReplyActions
│   │       └── LoadMoreReplies (加载更多回复按钮)
│   └── Pagination (分页组件)
└── CommentEditor (评论编辑器)
```

### 4.3 核心逻辑实现

```typescript
// hooks/useComments.ts
export function useComments(articleId: string) {
  const [state, setState] = useState<CommentState>({
    topLevelComments: [],
    total: 0,
    page: 0,
    size: 10,
    loading: false,
    hasMore: false,
  });

  // 1. 加载顶级评论
  const loadComments = async (page: number) => {
    setState((prev) => ({ ...prev, loading: true }));

    try {
      const response = await api.get("/comments/top-level", {
        params: { articleId, page, size: state.size },
      });

      setState((prev) => ({
        ...prev,
        topLevelComments: response.data.comments,
        total: response.data.total_count,
        page,
        hasMore: response.data.has_next,
        loading: false,
      }));
    } catch (error) {
      console.error("加载评论失败:", error);
      setState((prev) => ({ ...prev, loading: false }));
    }
  };

  // 2. 加载某个评论的所有回复
  const loadMoreReplies = async (commentId: string) => {
    try {
      const response = await api.get("/comments/replies", {
        params: { parentId: commentId, page: 0, size: 100 },
      });

      // 更新对应评论的回复列表
      setState((prev) => ({
        ...prev,
        topLevelComments: prev.topLevelComments.map((comment) =>
          comment.id === commentId
            ? {
                ...comment,
                replies: response.data.replies,
                repliesLoaded: true,
                showAllReplies: true,
              }
            : comment
        ),
      }));
    } catch (error) {
      console.error("加载回复失败:", error);
    }
  };

  // 3. 折叠回复
  const collapseReplies = (commentId: string) => {
    setState((prev) => ({
      ...prev,
      topLevelComments: prev.topLevelComments.map((comment) =>
        comment.id === commentId
          ? { ...comment, showAllReplies: false }
          : comment
      ),
    }));
  };

  // 4. 发表评论
  const postComment = async (content: string, parentId: string | null) => {
    try {
      const response = await api.post("/comments", {
        content,
        article_id: articleId,
        parent_id: parentId,
      });

      if (parentId === null) {
        // 发表顶级评论：重新加载第一页
        loadComments(0);
      } else {
        // 发表回复：更新对应评论的回复列表
        // 可以选择重新加载该评论的回复，或者直接插入到列表中
        loadMoreReplies(parentId);
      }

      return response.data;
    } catch (error) {
      console.error("发表评论失败:", error);
      throw error;
    }
  };

  // 5. 删除评论
  const deleteComment = async (commentId: string, parentId: string | null) => {
    try {
      await api.delete(`/comments/${commentId}`);

      if (parentId === null) {
        // 删除顶级评论：重新加载当前页
        loadComments(state.page);
      } else {
        // 删除回复：从列表中移除
        setState((prev) => ({
          ...prev,
          topLevelComments: prev.topLevelComments.map((comment) =>
            comment.id === parentId
              ? {
                  ...comment,
                  reply_count: (comment.reply_count || 0) - 1,
                  replies: comment.replies?.filter((r) => r.id !== commentId),
                  latest_replies: comment.latest_replies?.filter(
                    (r) => r.id !== commentId
                  ),
                }
              : comment
          ),
        }));
      }
    } catch (error) {
      console.error("删除评论失败:", error);
      throw error;
    }
  };

  return {
    ...state,
    loadComments,
    loadMoreReplies,
    collapseReplies,
    postComment,
    deleteComment,
  };
}
```

### 4.4 UI 交互设计

#### 顶级评论展示

```tsx
function CommentItem({ comment, onLoadMore, onCollapse }: CommentItemProps) {
  const replyCount = comment.reply_count || 0;
  const showingReplies = comment.showAllReplies
    ? comment.replies
    : comment.latest_replies;

  return (
    <div className="comment-item">
      <CommentHeader user={comment.user} time={comment.published_at} />
      <CommentContent content={comment.content} />
      <CommentActions
        commentId={comment.id}
        onReply={() => {
          /* 显示回复框 */
        }}
      />

      {/* 回复列表 */}
      {showingReplies && showingReplies.length > 0 && (
        <div className="replies">
          {showingReplies.map((reply) => (
            <ReplyItem key={reply.id} reply={reply} />
          ))}

          {/* 加载更多回复按钮 */}
          {!comment.showAllReplies && replyCount > 3 && (
            <button
              className="load-more-replies"
              onClick={() => onLoadMore(comment.id)}
            >
              查看全部 {replyCount} 条回复
            </button>
          )}

          {/* 折叠回复按钮 */}
          {comment.showAllReplies && (
            <button
              className="collapse-replies"
              onClick={() => onCollapse(comment.id)}
            >
              收起回复
            </button>
          )}
        </div>
      )}

      {/* 回复入口提示 */}
      {!showingReplies?.length && replyCount > 0 && (
        <button className="show-replies" onClick={() => onLoadMore(comment.id)}>
          {replyCount} 条回复
        </button>
      )}
    </div>
  );
}
```

#### 样式参考（Tailwind CSS）

```tsx
<div className="space-y-6">
  {/* 顶级评论 */}
  <article className="bg-white rounded-lg p-6 shadow-sm">
    <div className="flex items-start gap-4">
      <img
        src={user.avatar_url}
        alt={user.username}
        className="w-10 h-10 rounded-full"
      />
      <div className="flex-1">
        <div className="flex items-center gap-2 mb-2">
          <span className="font-medium">{user.username}</span>
          <span className="text-sm text-gray-500">{publishedAt}</span>
        </div>
        <p className="text-gray-800 mb-3">{content}</p>
        <button className="text-sm text-blue-600 hover:text-blue-800">
          回复
        </button>
      </div>
    </div>

    {/* 回复区域 */}
    <div className="ml-14 mt-4 space-y-3 border-l-2 border-gray-100 pl-4">
      {replies.map((reply) => (
        <div key={reply.id} className="flex items-start gap-3">
          <img src={reply.user.avatar_url} className="w-8 h-8 rounded-full" />
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-sm font-medium">{reply.user.username}</span>
              <span className="text-xs text-gray-500">
                {reply.published_at}
              </span>
            </div>
            <p className="text-sm text-gray-700">{reply.content}</p>
          </div>
        </div>
      ))}

      {/* 查看更多按钮 */}
      <button className="text-sm text-blue-600 hover:text-blue-800 font-medium">
        查看全部 25 条回复 →
      </button>
    </div>
  </article>
</div>
```

## 五、性能优化策略

### 5.1 前端优化

1. **虚拟滚动**：评论数量超过 100 条时使用虚拟滚动

   ```bash
   npm install react-window
   ```

2. **图片懒加载**：头像使用懒加载

   ```tsx
   <img loading="lazy" src={avatar_url} />
   ```

3. **乐观更新**：发表评论时立即显示，无需等待服务器响应

   ```typescript
   const optimisticComment = {
     id: `temp-${Date.now()}`,
     content,
     user: currentUser,
     published_at: new Date().toISOString(),
     // ...
   };

   setState((prev) => ({
     ...prev,
     comments: [optimisticComment, ...prev.comments],
   }));

   // 后台发送请求
   api.post("/comments", data).then((realComment) => {
     // 替换临时评论
     setState((prev) => ({
       ...prev,
       comments: prev.comments.map((c) =>
         c.id === optimisticComment.id ? realComment : c
       ),
     }));
   });
   ```

4. **防抖/节流**：滚动加载使用节流，输入框使用防抖

### 5.2 后端优化

1. **缓存策略**：

   - Redis 缓存热门文章的评论列表（5 分钟）
   - 评论数统计使用计数器（实时更新）

2. **数据库优化**：

   - 使用复合索引
   - 避免 N+1 查询
   - 分页查询使用游标而非 offset（评论很多时）

3. **CDN 缓存**：用户头像使用 CDN

## 六、用户体验细节

### 6.1 交互反馈

- ✅ 发表评论后：显示成功提示，清空输入框
- ✅ 删除评论前：二次确认对话框
- ✅ 加载中状态：显示骨架屏或加载动画
- ✅ 错误处理：友好的错误提示

### 6.2 边界情况处理

- 无评论时：显示空状态提示"暂无评论，快来抢沙发吧"
- 评论被删除：显示"该评论已被删除"占位
- 用户未登录：回复按钮显示"登录后可回复"
- 评论已关闭：显示"评论已关闭"

### 6.3 可访问性（Accessibility）

- 使用语义化 HTML 标签
- 添加 ARIA 标签
- 键盘导航支持（Tab、Enter、Esc）
- 屏幕阅读器支持

## 七、对比总结

| 特性       | 旧方案             | 新方案               |
| ---------- | ------------------ | -------------------- |
| 分页方式   | 所有评论平铺分页   | 仅顶级评论分页       |
| 子评论加载 | 随机分页，可能跨页 | 每个顶级评论独立加载 |
| 用户体验   | 评论上下文可能断裂 | 保持评论完整性       |
| 性能       | 一次性加载所有数据 | 按需懒加载           |
| 扩展性     | 评论多时性能差     | 良好，支持大量评论   |
| 复杂度     | 简单               | 中等，但更合理       |

## 八、实施步骤

### Phase 1: 后端改造（1-2 天）

1. ✅ 修改 CommentResponse DTO，添加 reply_count 和 latest_replies
2. ✅ 实现 findTopLevelCommentsWithLatestReplies 方法
3. ✅ 修改 Controller 返回新结构
4. ✅ 添加数据库索引
5. ✅ 编写单元测试

### Phase 2: 前端改造（2-3 天）

1. ✅ 创建新的评论组件结构
2. ✅ 实现 useComments hook
3. ✅ 实现懒加载逻辑
4. ✅ 优化 UI 样式
5. ✅ 添加交互动画

### Phase 3: 优化与测试（1-2 天）

1. ✅ 添加缓存策略
2. ✅ 性能测试与优化
3. ✅ 边界情况测试
4. ✅ 可访问性测试
5. ✅ 移动端适配

## 九、参考资源

- [知乎评论系统设计](https://zhuanlan.zhihu.com/p/123456789)
- [React 评论组件最佳实践](https://react.dev/learn/thinking-in-react)
- [Web 性能优化指南](https://web.dev/performance/)
- [WAI-ARIA 最佳实践](https://www.w3.org/WAI/ARIA/apg/)

---

**文档版本**：v1.0  
**最后更新**：2025-10-04  
**作者**：CleverOnion Team
