# 评论系统优化迁移任务清单

> **迁移策略**：渐进式迁移，新旧方案并存，平滑过渡，不影响现有功能和样式

## 📋 任务状态说明

- ⬜ 待开始
- 🔄 进行中
- ✅ 已完成
- ⏸️ 暂停/阻塞
- ❌ 已取消

---

## Phase 1: 后端基础设施准备（1-2 天）

### 1.1 DTO 层扩展

#### Task 1.1.1: 扩展 CommentResponse DTO

**状态**: ✅  
**优先级**: 🔴 高  
**预计时间**: 30 分钟

**任务描述**:

- 在 `CommentResponse.java` 中添加新字段 `replyCount` 和 `latestReplies`
- 添加相应的 getter/setter 方法
- 更新 toString 方法
- **注意**: 新字段设为可选，不影响现有接口

**验收标准**:

- [x] 编译通过，无错误
- [x] 新字段可以为 null，向后兼容
- [x] 更新了 Swagger 文档注解

**文件路径**:

```
CleverOnion-backend/src/main/java/com/cleveronion/blog/presentation/api/dto/comment/CommentResponse.java
```

**实现代码**:

```java
@Schema(description = "回复总数（仅顶级评论有效）")
@JsonProperty("reply_count")
private Integer replyCount;

@Schema(description = "最新的几条回复（仅顶级评论有效，默认3条）")
@JsonProperty("latest_replies")
private List<CommentResponse> latestReplies;
```

---

#### Task 1.1.2: 创建带回复信息的评论包装类

**状态**: ✅  
**优先级**: 🔴 高  
**预计时间**: 20 分钟

**任务描述**:

- 创建 `CommentWithRepliesDTO` 类用于内部传递
- 封装评论对象、回复数量和最新回复列表

**验收标准**:

- [x] 类创建完成
- [x] 包含必要的构造函数
- [x] 添加 Javadoc 注释

**文件路径**:

```
CleverOnion-backend/src/main/java/com/cleveronion/blog/application/comment/dto/CommentWithRepliesDTO.java (新建)
```

**实现代码**:

```java
package com.cleveronion.blog.application.comment.dto;

import com.cleveronion.blog.domain.comment.aggregate.CommentAggregate;
import java.util.List;

/**
 * 评论及其回复信息DTO
 * 用于在 Service 层传递评论及其相关回复信息
 */
public class CommentWithRepliesDTO {
    private final CommentAggregate comment;
    private final long replyCount;
    private final List<CommentAggregate> latestReplies;

    public CommentWithRepliesDTO(CommentAggregate comment, long replyCount, List<CommentAggregate> latestReplies) {
        this.comment = comment;
        this.replyCount = replyCount;
        this.latestReplies = latestReplies;
    }

    // Getters
    public CommentAggregate getComment() { return comment; }
    public long getReplyCount() { return replyCount; }
    public List<CommentAggregate> getLatestReplies() { return latestReplies; }
}
```

---

### 1.2 Repository 层扩展

#### Task 1.2.1: 验证现有查询方法

**状态**: ✅  
**优先级**: 🟡 中  
**预计时间**: 15 分钟

**任务描述**:

- 检查 `CommentRepository` 接口是否已有以下方法：
  - `findTopLevelCommentsByArticleId(ArticleId, int page, int size)`
  - `findByParentId(CommentId, int page, int size)`
  - `countRepliesByParentId(CommentId)`
- 如缺少则添加

**验收标准**:

- [x] 所有需要的查询方法都已存在
- [x] 方法签名正确
- [x] 添加了方法文档注释

**文件路径**:

```
CleverOnion-backend/src/main/java/com/cleveronion/blog/domain/comment/repository/CommentRepository.java
```

---

#### Task 1.2.2: 添加批量查询回复数方法（可选优化）

**状态**: ✅  
**优先级**: 🟢 低（优化项）  
**预计时间**: 1 小时

**任务描述**:

- 在 `CommentRepository` 中添加批量查询方法，避免 N+1 问题
- 实现 `countRepliesByParentIds(List<CommentId>)` 方法

**验收标准**:

- [x] 方法实现完成
- [x] 使用一次数据库查询获取多个评论的回复数
- [x] 添加单元测试（可后续补充）

**文件路径**:

```
CleverOnion-backend/src/main/java/com/cleveronion/blog/domain/comment/repository/CommentRepository.java
CleverOnion-backend/src/main/java/com/cleveronion/blog/infrastructure/comment/persistence/repository/CommentRepositoryImpl.java
```

**SQL 参考**:

```sql
SELECT parent_id, COUNT(*) as reply_count
FROM comments
WHERE parent_id IN (:parentIds)
GROUP BY parent_id
```

---

### 1.3 Service 层扩展

#### Task 1.3.1: 添加查询顶级评论及回复的方法

**状态**: ✅  
**优先级**: 🔴 高  
**预计时间**: 1 小时

**任务描述**:

- 在 `CommentQueryService` 中添加新方法：
  ```java
  public List<CommentWithRepliesDTO> findTopLevelCommentsWithLatestReplies(
      ArticleId articleId, int page, int size, int replyLimit)
  ```
- 实现逻辑：
  1. 查询顶级评论（分页）
  2. 对每个顶级评论，查询回复数量
  3. 对每个顶级评论，查询最新的 N 条回复（默认 3 条）
  4. 组装成 `CommentWithRepliesDTO` 返回

**验收标准**:

- [x] 方法实现完成
- [x] 添加参数校验
- [x] 添加日志记录
- [x] 添加 Javadoc 注释
- [x] replyLimit 参数支持自定义

**文件路径**:

```
CleverOnion-backend/src/main/java/com/cleveronion/blog/application/comment/service/CommentQueryService.java
```

**实现提示**:

```java
public List<CommentWithRepliesDTO> findTopLevelCommentsWithLatestReplies(
    ArticleId articleId, int page, int size, int replyLimit) {

    // 参数校验
    if (articleId == null) {
        throw new IllegalArgumentException("文章ID不能为空");
    }
    // ... 其他校验

    logger.debug("查询顶级评论及最新回复，文章ID: {}, 页码: {}, 回复数: {}",
        articleId.getValue(), page, replyLimit);

    // 1. 查询顶级评论
    List<CommentAggregate> topComments =
        commentRepository.findTopLevelCommentsByArticleId(articleId, page, size);

    // 2. 为每个顶级评论查询回复信息
    return topComments.stream().map(comment -> {
        CommentId commentId = comment.getId();

        // 统计回复数
        long replyCount = commentRepository.countRepliesByParentId(commentId);

        // 获取最新N条回复
        List<CommentAggregate> latestReplies =
            commentRepository.findByParentId(commentId, 0, replyLimit);

        return new CommentWithRepliesDTO(comment, replyCount, latestReplies);
    }).collect(Collectors.toList());
}
```

---

### 1.4 Controller 层扩展

#### Task 1.4.1: 创建新的顶级评论查询接口（保留旧接口）

**状态**: ✅  
**优先级**: 🔴 高  
**预计时间**: 45 分钟

**任务描述**:

- 在 `CommentController` 中添加新接口：
  ```
  GET /api/comments/top-level-with-replies
  ```
- **重要**: 不修改现有 `/api/comments` 接口，保持向后兼容
- 新接口返回带 `reply_count` 和 `latest_replies` 的数据

**验收标准**:

- [x] 新接口实现完成
- [x] 旧接口 `/api/comments` 仍然正常工作
- [x] 添加 Swagger 文档注释
- [x] 添加请求日志

**文件路径**:

```
CleverOnion-backend/src/main/java/com/cleveronion/blog/presentation/api/controller/CommentController.java
```

**实现代码**:

```java
/**
 * 查询文章的顶级评论列表（带回复信息）
 * 新版评论接口，支持懒加载子评论
 *
 * @param articleId 文章ID
 * @param page 页码（从0开始）
 * @param size 每页大小
 * @param replyLimit 每个评论返回的最新回复数（默认3）
 * @return 顶级评论列表（包含回复统计和最新回复）
 */
@GetMapping("/top-level-with-replies")
public Result<CommentListResponse> getTopLevelCommentsWithReplies(
        @RequestParam Long articleId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "3") int replyLimit) {

    logger.debug("查询文章顶级评论（带回复），文章ID: {}, 页码: {}, 每页: {}, 回复数: {}",
        articleId, page, size, replyLimit);

    // 执行查询
    ArticleId articleIdVO = new ArticleId(articleId.toString());
    List<CommentWithRepliesDTO> commentsWithReplies =
        commentQueryService.findTopLevelCommentsWithLatestReplies(articleIdVO, page, size, replyLimit);

    // 转换为响应DTO
    List<CommentResponse> commentResponses = commentsWithReplies.stream()
        .map(this::convertToCommentResponseWithReplies)
        .collect(Collectors.toList());

    // 获取顶级评论总数
    long totalComments = commentQueryService.countTopLevelByArticleId(articleIdVO);

    CommentListResponse response = new CommentListResponse(
        commentResponses,
        totalComments,
        page,
        size
    );

    return Result.success(response);
}

/**
 * 转换带回复信息的评论
 */
private CommentResponse convertToCommentResponseWithReplies(CommentWithRepliesDTO dto) {
    // 转换主评论
    CommentResponse response = convertToCommentResponse(dto.getComment());

    // 添加回复统计
    response.setReplyCount((int) dto.getReplyCount());

    // 添加最新回复
    if (!dto.getLatestReplies().isEmpty()) {
        List<CommentResponse> latestReplies = dto.getLatestReplies().stream()
            .map(this::convertToCommentResponse)
            .collect(Collectors.toList());
        response.setLatestReplies(latestReplies);
    }

    return response;
}
```

---

#### Task 1.4.2: 验证回复查询接口

**状态**: ✅  
**优先级**: 🟡 中  
**预计时间**: 15 分钟

**任务描述**:

- 验证 `/api/comments/replies` 接口是否正常工作
- 确认接口支持分页参数
- 测试返回数据格式

**验收标准**:

- [x] 接口正常返回数据
- [x] 分页功能正常
- [x] 数据格式符合预期

---

### 1.5 单元测试

#### Task 1.5.1: 编写 Service 层单元测试

**状态**: ✅  
**优先级**: 🟡 中  
**预计时间**: 1 小时

**任务描述**:

- 为 `findTopLevelCommentsWithLatestReplies` 方法编写单元测试
- 测试场景：
  1. 正常查询（有回复）
  2. 正常查询（无回复）
  3. 空列表场景
  4. 参数校验测试

**验收标准**:

- [x] 测试覆盖率 > 80%
- [x] 所有测试通过
- [x] Mock 依赖正确

**文件路径**:

```
CleverOnion-backend/src/test/java/com/cleveronion/blog/application/comment/service/CommentQueryServiceTest.java
```

---

#### Task 1.5.2: 编写 Controller 层集成测试

**状态**: ✅  
**优先级**: 🟡 中  
**预计时间**: 1 小时

**任务描述**:

- 为新接口编写集成测试
- 使用 `@WebMvcTest` 或 `@SpringBootTest`
- 测试完整的请求-响应流程

**验收标准**:

- [x] 测试覆盖主要场景
- [x] HTTP 状态码正确
- [x] 响应格式正确

---

## Phase 2: 前端基础设施准备（1 天）

### 2.1 类型定义

#### Task 2.1.1: 扩展评论类型定义

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 20 分钟

**任务描述**:

- 在前端项目中更新 Comment 接口定义
- 添加新字段：`reply_count`, `latest_replies`
- **注意**: 不删除或修改现有字段，只添加新字段

**验收标准**:

- [ ] TypeScript 类型检查通过
- [ ] 新字段标记为可选（`?`）
- [ ] 添加 JSDoc 注释

**文件路径**:

```
CleverOnion-blog-f/src/types/comment.ts (如不存在则创建)
```

**实现代码**:

```typescript
/**
 * 评论数据类型
 */
export interface Comment {
  id: string;
  content: string;
  article_id: string;
  user: {
    id?: string | null;
    github_id: number;
    username: string;
    avatar_url: string;
  };
  parent_id: string | null;
  is_top_level: boolean;
  published_at: string;

  // 新增字段（用于新版评论系统）
  /** 回复总数（仅顶级评论有效） */
  reply_count?: number;

  /** 最新的几条回复（仅顶级评论有效，默认3条） */
  latest_replies?: Comment[];

  // 前端状态字段
  /** 前端加载的所有回复 */
  replies?: Comment[];

  /** 是否已加载所有回复 */
  repliesLoaded?: boolean;

  /** 是否展示所有回复 */
  showAllReplies?: boolean;
}

/**
 * 评论列表状态
 */
export interface CommentState {
  topLevelComments: Comment[];
  total: number;
  page: number;
  size: number;
  loading: boolean;
  hasMore: boolean;
}

/**
 * 评论API响应
 */
export interface CommentListResponse {
  comments: Comment[];
  total_count: number;
  page: number;
  size: number;
  has_next: boolean;
  has_previous: boolean;
}
```

---

### 2.2 API 层

#### Task 2.2.1: 添加新的评论 API 方法

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 30 分钟

**任务描述**:

- 在 `comments.ts` API 文件中添加新方法
- **保留**现有的 `getComments` 方法
- 添加新方法：`getTopLevelCommentsWithReplies`

**验收标准**:

- [ ] 新方法实现完成
- [ ] 旧方法保持不变
- [ ] 类型定义正确
- [ ] 错误处理完善

**文件路径**:

```
CleverOnion-blog-f/src/api/comments.ts
```

**实现代码**:

```typescript
import api from "./index";
import type { Comment, CommentListResponse } from "@/types/comment";

/**
 * 获取文章的所有评论（旧版接口，保留）
 */
export const getComments = async (
  articleId: string,
  page: number = 0,
  size: number = 10
) => {
  const response = await api.get<CommentListResponse>("/comments", {
    params: { articleId, page, size },
  });
  return response.data;
};

/**
 * 获取文章的顶级评论及最新回复（新版接口）
 * @param articleId 文章ID
 * @param page 页码
 * @param size 每页大小
 * @param replyLimit 每个评论返回的最新回复数（默认3）
 */
export const getTopLevelCommentsWithReplies = async (
  articleId: string,
  page: number = 0,
  size: number = 10,
  replyLimit: number = 3
) => {
  const response = await api.get<CommentListResponse>(
    "/comments/top-level-with-replies",
    {
      params: { articleId, page, size, replyLimit },
    }
  );
  return response.data;
};

/**
 * 获取评论的所有回复
 */
export const getCommentReplies = async (
  parentId: string,
  page: number = 0,
  size: number = 50
) => {
  const response = await api.get<CommentListResponse>("/comments/replies", {
    params: { parentId, page, size },
  });
  return response.data;
};

/**
 * 发表评论
 */
export const postComment = async (
  articleId: string,
  content: string,
  parentId?: string | null
) => {
  const response = await api.post<Comment>("/comments", {
    content,
    article_id: articleId,
    parent_id: parentId,
  });
  return response.data;
};

/**
 * 删除评论
 */
export const deleteComment = async (commentId: string) => {
  await api.delete(`/comments/${commentId}`);
};
```

---

### 2.3 自定义 Hook

#### Task 2.3.1: 创建新的评论管理 Hook

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 2 小时

**任务描述**:

- 创建 `useCommentsV2.ts`（新版 Hook，不影响旧版）
- 实现评论懒加载逻辑
- 包含：加载评论、加载更多回复、折叠回复、发表评论、删除评论等功能

**验收标准**:

- [ ] Hook 实现完成
- [ ] 类型定义正确
- [ ] 错误处理完善
- [ ] 添加 loading 状态管理
- [ ] 添加 JSDoc 注释

**文件路径**:

```
CleverOnion-blog-f/src/hooks/useCommentsV2.ts (新建)
```

**实现代码**:

```typescript
import { useState, useCallback } from "react";
import {
  getTopLevelCommentsWithReplies,
  getCommentReplies,
  postComment as postCommentAPI,
  deleteComment as deleteCommentAPI,
} from "@/api/comments";
import type { Comment, CommentState } from "@/types/comment";

/**
 * 评论管理 Hook（新版 - 支持懒加载）
 * @param articleId 文章ID
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
   */
  const loadComments = useCallback(
    async (page: number = 0) => {
      setState((prev) => ({ ...prev, loading: true }));

      try {
        const data = await getTopLevelCommentsWithReplies(
          articleId,
          page,
          state.size
        );

        setState((prev) => ({
          ...prev,
          topLevelComments: data.comments,
          total: data.total_count,
          page,
          hasMore: data.has_next,
          loading: false,
        }));
      } catch (error) {
        console.error("加载评论失败:", error);
        setState((prev) => ({ ...prev, loading: false }));
        throw error;
      }
    },
    [articleId, state.size]
  );

  /**
   * 加载某个评论的所有回复
   */
  const loadMoreReplies = useCallback(async (commentId: string) => {
    try {
      const data = await getCommentReplies(commentId, 0, 100);

      setState((prev) => ({
        ...prev,
        topLevelComments: prev.topLevelComments.map((comment) =>
          comment.id === commentId
            ? {
                ...comment,
                replies: data.replies,
                repliesLoaded: true,
                showAllReplies: true,
              }
            : comment
        ),
      }));
    } catch (error) {
      console.error("加载回复失败:", error);
      throw error;
    }
  }, []);

  /**
   * 折叠回复
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
   * 发表评论
   */
  const postComment = useCallback(
    async (content: string, parentId?: string | null) => {
      try {
        await postCommentAPI(articleId, content, parentId);

        if (!parentId) {
          // 发表顶级评论：重新加载第一页
          await loadComments(0);
        } else {
          // 发表回复：重新加载该评论的回复
          await loadMoreReplies(parentId);
        }
      } catch (error) {
        console.error("发表评论失败:", error);
        throw error;
      }
    },
    [articleId, loadComments, loadMoreReplies]
  );

  /**
   * 删除评论
   */
  const deleteComment = useCallback(
    async (commentId: string, parentId?: string | null) => {
      try {
        await deleteCommentAPI(commentId);

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
      } catch (error) {
        console.error("删除评论失败:", error);
        throw error;
      }
    },
    [state.page, loadComments]
  );

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

---

## Phase 3: 前端组件开发（2-3 天）

### 3.1 创建新版评论组件（不修改现有组件）

#### Task 3.1.1: 创建评论区容器组件 V2

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 1 小时

**任务描述**:

- 创建 `CommentSectionV2.tsx` 组件
- **不修改**现有的评论组件
- 使用 `useCommentsV2` Hook
- **样式复用**：使用现有的评论区样式类名，不创建新样式

**验收标准**:

- [ ] 组件创建完成
- [ ] 集成 useCommentsV2 Hook
- [ ] 复用现有样式
- [ ] 添加 PropTypes/TypeScript 类型
- [ ] 添加组件文档注释

**文件路径**:

```
CleverOnion-blog-f/src/components/comment/CommentSectionV2.tsx (新建)
```

**实现提示**:

```typescript
import React, { useEffect } from 'react';
import { useCommentsV2 } from '@/hooks/useCommentsV2';
import CommentListV2 from './CommentListV2';
import CommentEditor from './CommentEditor'; // 复用现有编辑器

interface CommentSectionV2Props {
  articleId: string;
}

/**
 * 评论区组件 V2（支持懒加载子评论）
 * 复用现有样式，不修改现有组件
 */
export default function CommentSectionV2({ articleId }: CommentSectionV2Props) {
  const {
    topLevelComments,
    total,
    page,
    loading,
    hasMore,
    loadComments,
    loadMoreReplies,
    collapseReplies,
    postComment,
    deleteComment,
  } = useCommentsV2(articleId);

  useEffect(() => {
    loadComments(0);
  }, [loadComments]);

  return (
    <div className="comment-section">  {/* 复用现有类名 */}
      <div className="comment-header">
        <h3>评论 ({total})</h3>
      </div>

      <CommentEditor
        onSubmit={(content) => postComment(content)}
        placeholder="写下你的评论..."
      />

      <CommentListV2
        comments={topLevelComments}
        loading={loading}
        onLoadMoreReplies={loadMoreReplies}
        onCollapseReplies={collapseReplies}
        onPostReply={postComment}
        onDelete={deleteComment}
      />

      {hasMore && (
        <button
          className="load-more-btn"  {/* 复用现有类名 */}
          onClick={() => loadComments(page + 1)}
          disabled={loading}
        >
          加载更多评论
        </button>
      )}
    </div>
  );
}
```

---

#### Task 3.1.2: 创建评论列表组件 V2

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 1.5 小时

**任务描述**:

- 创建 `CommentListV2.tsx` 组件
- 渲染顶级评论列表
- **样式复用**：使用现有评论列表样式

**验收标准**:

- [ ] 组件创建完成
- [ ] 正确渲染评论列表
- [ ] 复用现有样式
- [ ] 处理空列表状态

**文件路径**:

```
CleverOnion-blog-f/src/components/comment/CommentListV2.tsx (新建)
```

---

#### Task 3.1.3: 创建评论项组件 V2

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 2 小时

**任务描述**:

- 创建 `CommentItemV2.tsx` 组件
- 显示单个顶级评论
- 显示最新 3 条回复
- 显示"查看全部 N 条回复"按钮（当回复数>3 时）
- 显示"收起回复"按钮（当展开所有回复时）
- **样式复用**：完全使用现有的评论样式类名

**验收标准**:

- [ ] 组件创建完成
- [ ] 正确显示评论内容
- [ ] 正确显示回复列表
- [ ] 懒加载按钮功能正常
- [ ] 复用所有现有样式类名
- [ ] 添加骨架屏（加载状态）

**文件路径**:

```
CleverOnion-blog-f/src/components/comment/CommentItemV2.tsx (新建)
```

**实现提示**:

```typescript
import React, { useState } from "react";
import type { Comment } from "@/types/comment";
import ReplyList from "./ReplyList"; // 复用现有回复列表组件

interface CommentItemV2Props {
  comment: Comment;
  onLoadMoreReplies: (commentId: string) => Promise<void>;
  onCollapseReplies: (commentId: string) => void;
  onReply: (content: string, parentId: string) => Promise<void>;
  onDelete: (commentId: string, parentId: string | null) => Promise<void>;
}

export default function CommentItemV2({
  comment,
  onLoadMoreReplies,
  onCollapseReplies,
  onReply,
  onDelete,
}: CommentItemV2Props) {
  const [loadingReplies, setLoadingReplies] = useState(false);
  const replyCount = comment.reply_count || 0;

  // 确定显示哪些回复
  const showingReplies = comment.showAllReplies
    ? comment.replies
    : comment.latest_replies;

  const handleLoadMore = async () => {
    setLoadingReplies(true);
    try {
      await onLoadMoreReplies(comment.id);
    } finally {
      setLoadingReplies(false);
    }
  };

  return (
    <div className="comment-item">
      {" "}
      {/* 复用现有类名 */}
      <div className="comment-avatar">
        <img src={comment.user.avatar_url} alt={comment.user.username} />
      </div>
      <div className="comment-body">
        <div className="comment-header">
          <span className="comment-author">{comment.user.username}</span>
          <span className="comment-time">{comment.published_at}</span>
        </div>

        <div className="comment-content">{comment.content}</div>

        <div className="comment-actions">
          <button
            onClick={() => {
              /* 显示回复框 */
            }}
          >
            回复
          </button>
          <button onClick={() => onDelete(comment.id, null)}>删除</button>
        </div>

        {/* 回复区域 */}
        {showingReplies && showingReplies.length > 0 && (
          <div className="comment-replies">
            {" "}
            {/* 复用现有类名 */}
            <ReplyList
              replies={showingReplies}
              onReply={onReply}
              onDelete={onDelete}
            />
            {/* 加载更多回复按钮 */}
            {!comment.showAllReplies && replyCount > 3 && (
              <button
                className="load-more-replies-btn"
                onClick={handleLoadMore}
                disabled={loadingReplies}
              >
                {loadingReplies ? "加载中..." : `查看全部 ${replyCount} 条回复`}
              </button>
            )}
            {/* 折叠回复按钮 */}
            {comment.showAllReplies && (
              <button
                className="collapse-replies-btn"
                onClick={() => onCollapseReplies(comment.id)}
              >
                收起回复
              </button>
            )}
          </div>
        )}

        {/* 回复入口（当没有显示回复但有回复数时） */}
        {(!showingReplies || showingReplies.length === 0) && replyCount > 0 && (
          <button className="show-replies-btn" onClick={handleLoadMore}>
            {replyCount} 条回复
          </button>
        )}
      </div>
    </div>
  );
}
```

---

#### Task 3.1.4: 验证现有子组件可复用性

**状态**: ⬜  
**优先级**: 🟡 中  
**预计时间**: 30 分钟

**任务描述**:

- 检查现有的子组件是否可以直接复用：
  - CommentEditor（评论编辑器）
  - CommentHeader（评论头部）
  - ReplyList（回复列表）
  - ReplyItem（单条回复）
- 如果不能复用，评估是否需要轻微调整

**验收标准**:

- [ ] 列出可复用的组件清单
- [ ] 列出需要调整的组件清单
- [ ] 记录调整方案

---

## Phase 4: 功能开关与灰度发布（0.5 天）

### 4.1 功能开关

#### Task 4.1.1: 添加功能开关配置

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 30 分钟

**任务描述**:

- 创建功能开关配置文件或环境变量
- 控制是否启用新版评论系统
- 默认使用旧版，手动切换到新版

**验收标准**:

- [ ] 配置文件创建完成
- [ ] 可以通过配置切换新旧版本
- [ ] 默认使用旧版本

**文件路径**:

```
CleverOnion-blog-f/src/config/features.ts (新建)
```

**实现代码**:

```typescript
/**
 * 功能开关配置
 */
export const features = {
  // 是否启用新版评论系统（懒加载子评论）
  useNewCommentSystem:
    import.meta.env.VITE_USE_NEW_COMMENT_SYSTEM === "true" || false,
};
```

**.env 文件**:

```
# 评论系统版本控制（默认false，使用旧版）
VITE_USE_NEW_COMMENT_SYSTEM=false
```

---

#### Task 4.1.2: 在文章详情页添加版本切换逻辑

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 20 分钟

**任务描述**:

- 在文章详情页根据功能开关渲染不同版本的评论组件
- 保持 API 调用一致

**验收标准**:

- [ ] 可以通过配置切换评论组件版本
- [ ] 切换流畅，无报错
- [ ] 样式保持一致

**文件路径**:

```
CleverOnion-blog-f/src/pages/Article.tsx
```

**实现代码**:

```typescript
import CommentSection from "@/components/comment/CommentSection"; // 旧版
import CommentSectionV2 from "@/components/comment/CommentSectionV2"; // 新版
import { features } from "@/config/features";

// 在文章详情页组件中
export default function Article() {
  // ... 其他代码

  const CommentComponent = features.useNewCommentSystem
    ? CommentSectionV2
    : CommentSection;

  return (
    <div className="article-page">
      {/* 文章内容 */}

      {/* 评论区 */}
      <CommentComponent articleId={articleId} />
    </div>
  );
}
```

---

### 4.2 灰度测试

#### Task 4.2.1: 本地测试新版评论系统

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 1 小时

**任务描述**:

- 将 `VITE_USE_NEW_COMMENT_SYSTEM` 设为 `true`
- 测试新版评论系统的所有功能
- 测试场景：
  1. 加载顶级评论
  2. 查看最新 3 条回复
  3. 展开所有回复
  4. 收起回复
  5. 发表评论
  6. 发表回复
  7. 删除评论
  8. 分页功能

**验收标准**:

- [ ] 所有功能正常工作
- [ ] 样式与旧版一致
- [ ] 无控制台错误
- [ ] 网络请求正常

---

#### Task 4.2.2: 性能对比测试

**状态**: ⬜  
**优先级**: 🟡 中  
**预计时间**: 1 小时

**任务描述**:

- 对比新旧版本的性能指标：
  - 首次加载时间
  - 网络请求数量
  - 数据传输量
  - 页面渲染时间
- 使用 Chrome DevTools 进行测量

**验收标准**:

- [ ] 完成性能测试报告
- [ ] 新版性能不低于旧版
- [ ] 记录优化建议

---

## Phase 5: 数据库优化（可选，0.5 天）

### 5.1 索引优化

#### Task 5.1.1: 添加复合索引

**状态**: ⬜  
**优先级**: 🟢 低（优化项）  
**预计时间**: 30 分钟

**任务描述**:

- 在 comments 表上添加复合索引
- 优化查询性能

**验收标准**:

- [ ] 索引创建成功
- [ ] 查询性能有提升
- [ ] 不影响写入性能

**SQL 脚本**:

```sql
-- 复合索引：文章ID + 父评论ID + 创建时间
CREATE INDEX idx_article_parent_created
ON comments(article_id, parent_id, created_at);

-- 分析索引效果
EXPLAIN SELECT * FROM comments
WHERE article_id = 13 AND parent_id IS NULL
ORDER BY created_at ASC;
```

---

### 5.2 查询优化

#### Task 5.2.1: 分析慢查询

**状态**: ⬜  
**优先级**: 🟢 低（优化项）  
**预计时间**: 1 小时

**任务描述**:

- 开启慢查询日志
- 分析评论相关的慢查询
- 优化查询语句

**验收标准**:

- [ ] 识别出慢查询
- [ ] 提供优化方案
- [ ] 实施优化

---

## Phase 6: 完整测试与上线（1 天）

### 6.1 测试

#### Task 6.1.1: 功能测试清单

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 2 小时

**测试清单**:

- [ ] 顶级评论加载（空列表）
- [ ] 顶级评论加载（有数据）
- [ ] 顶级评论分页
- [ ] 查看最新 3 条回复
- [ ] 展开所有回复（<10 条）
- [ ] 展开所有回复（>10 条，测试分页）
- [ ] 收起回复
- [ ] 发表顶级评论
- [ ] 发表回复
- [ ] 删除顶级评论（级联删除子评论）
- [ ] 删除子评论
- [ ] 评论数量统计正确
- [ ] 回复数量统计正确
- [ ] 权限控制（仅作者可删除）

---

#### Task 6.1.2: 兼容性测试

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 1 小时

**测试清单**:

- [ ] Chrome（最新版）
- [ ] Firefox（最新版）
- [ ] Safari（最新版）
- [ ] Edge（最新版）
- [ ] 移动端 Chrome
- [ ] 移动端 Safari

---

#### Task 6.1.3: 边界情况测试

**状态**: ⬜  
**优先级**: 🟡 中  
**预计时间**: 1 小时

**测试清单**:

- [ ] 评论内容为空
- [ ] 评论内容超长
- [ ] 评论内容包含特殊字符
- [ ] 评论内容包含 HTML/脚本标签
- [ ] 网络异常处理
- [ ] 并发请求处理
- [ ] 快速连续点击按钮

---

### 6.2 文档

#### Task 6.2.1: 更新 API 文档

**状态**: ⬜  
**优先级**: 🟡 中  
**预计时间**: 30 分钟

**任务描述**:

- 更新 Swagger 文档
- 标注新旧接口
- 说明迁移指南

**验收标准**:

- [ ] API 文档更新完成
- [ ] 新接口有完整描述
- [ ] 示例代码正确

---

#### Task 6.2.2: 编写用户操作指南（可选）

**状态**: ⬜  
**优先级**: 🟢 低  
**预计时间**: 30 分钟

**任务描述**:

- 如果有用户可见的变化，编写操作指南
- 说明新功能的使用方法

---

### 6.3 上线部署

#### Task 6.3.1: 后端部署

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 30 分钟

**部署步骤**:

1. [ ] 备份数据库
2. [ ] 部署新版后端代码
3. [ ] 运行数据库迁移脚本（如有）
4. [ ] 验证新接口可用性
5. [ ] 验证旧接口仍正常工作

---

#### Task 6.3.2: 前端部署（灰度）

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 30 分钟

**部署步骤**:

1. [ ] 确认功能开关为 `false`（使用旧版）
2. [ ] 部署前端代码
3. [ ] 验证旧版评论系统正常
4. [ ] 手动测试切换到新版
5. [ ] 验证新版评论系统正常

---

#### Task 6.3.3: 全量切换

**状态**: ⬜  
**优先级**: 🔴 高  
**预计时间**: 15 分钟

**切换步骤**:

1. [ ] 观察灰度期间的错误日志
2. [ ] 修复发现的问题（如有）
3. [ ] 将功能开关设为 `true`
4. [ ] 重新部署前端
5. [ ] 监控错误率和性能指标
6. [ ] 准备回滚方案（如需要）

---

## Phase 7: 清理与优化（1 天，可选）

### 7.1 代码清理

#### Task 7.1.1: 移除旧版接口（可选，谨慎）

**状态**: ⬜  
**优先级**: 🟢 低  
**预计时间**: 1 小时

**任务描述**:

- 在确认新版稳定运行 1-2 周后
- 考虑移除旧版接口和代码
- 或保留旧版接口作为备用

**验收标准**:

- [ ] 评估移除风险
- [ ] 确认无依赖
- [ ] 完成移除（或决定保留）

---

#### Task 7.1.2: 组件重命名

**状态**: ⬜  
**优先级**: 🟢 低  
**预计时间**: 30 分钟

**任务描述**:

- 将 `CommentSectionV2` 重命名为 `CommentSection`
- 将旧版组件重命名为 `CommentSectionLegacy`
- 或直接删除旧版组件

**验收标准**:

- [ ] 组件重命名完成
- [ ] 所有引用已更新
- [ ] 测试通过

---

### 7.2 性能优化

#### Task 7.2.1: 添加缓存策略

**状态**: ⬜  
**优先级**: 🟡 中  
**预计时间**: 2 小时

**任务描述**:

- 后端添加 Redis 缓存
- 缓存热门文章的评论列表
- 设置合理的过期时间

**验收标准**:

- [ ] 缓存实现完成
- [ ] 缓存命中率 > 50%
- [ ] 缓存失效策略正确

---

#### Task 7.2.2: 前端优化

**状态**: ⬜  
**优先级**: 🟡 中  
**预计时间**: 2 小时

**优化项**:

- [ ] 图片懒加载
- [ ] 虚拟滚动（评论很多时）
- [ ] 防抖/节流优化
- [ ] 代码分割

---

## 📊 进度跟踪

### 总体进度

| Phase    | 任务数 | 已完成 | 进行中 | 待开始 | 完成率  |
| -------- | ------ | ------ | ------ | ------ | ------- |
| Phase 1  | 12     | 7      | 0      | 5      | 58%     |
| Phase 2  | 3      | 0      | 0      | 3      | 0%      |
| Phase 3  | 4      | 0      | 0      | 4      | 0%      |
| Phase 4  | 3      | 0      | 0      | 3      | 0%      |
| Phase 5  | 2      | 0      | 0      | 2      | 0%      |
| Phase 6  | 7      | 0      | 0      | 7      | 0%      |
| Phase 7  | 4      | 0      | 0      | 4      | 0%      |
| **总计** | **35** | **7**  | **0**  | **28** | **20%** |

### 关键里程碑

- [x] M1: 后端新接口开发完成（Phase 1 完成）🎉
- [ ] M2: 前端新组件开发完成（Phase 3 完成）
- [ ] M3: 功能开关实现（Phase 4 完成）
- [ ] M4: 测试通过（Phase 6.1 完成）
- [ ] M5: 成功上线并稳定运行

---

## 🚨 风险与注意事项

### 高风险项

1. **数据一致性**: 确保新旧接口返回的数据一致
2. **性能问题**: N+1 查询可能导致性能下降，需要优化
3. **样式兼容**: 新组件必须完全复用现有样式，避免视觉差异

### 回滚方案

1. **后端回滚**: 保留旧接口，可随时切回
2. **前端回滚**: 通过功能开关立即切回旧版
3. **数据库回滚**: 新增索引可保留，不影响原有功能

### 注意事项

1. ⚠️ **不要删除旧代码**: 在确认新版稳定前，保留所有旧版代码
2. ⚠️ **不要修改现有样式**: 新组件完全复用现有 CSS 类名
3. ⚠️ **充分测试**: 每个阶段完成后都要进行充分测试
4. ⚠️ **监控指标**: 上线后密切关注错误率和性能指标
5. ⚠️ **灰度发布**: 先小范围测试，确认无问题后再全量

---

## 📝 更新日志

| 日期       | 更新内容                                                         | 更新人      |
| ---------- | ---------------------------------------------------------------- | ----------- |
| 2025-10-04 | 创建任务文档                                                     | CleverOnion |
| 2025-10-04 | 完成 Task 1.1.1 和 1.1.2 - DTO 层扩展完成                        | CleverOnion |
| 2025-10-04 | 完成 Task 1.2.1 和 1.2.2 - Repository 层扩展完成（含性能优化）   | CleverOnion |
| 2025-10-04 | 完成 Task 1.3.1 - Service 层核心业务逻辑实现完成                 | CleverOnion |
| 2025-10-04 | 完成 Task 1.4.1 和 1.4.2 - Controller 层接口实现完成             | CleverOnion |
| 2025-10-04 | 完成 Task 1.5.1 和 1.5.2 - 单元测试和集成测试完成（Phase 1完成）🎉 | CleverOnion |

---

**文档版本**: v1.0  
**最后更新**: 2025-10-04  
**文档所有者**: CleverOnion Team
