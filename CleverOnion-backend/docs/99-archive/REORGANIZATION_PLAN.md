# 文档重组计划

## 📁 新的文档结构设计

### 后端文档结构 (CleverOnion-backend/docs/)

```
docs/
├── README.md                          # 文档总览和导航
│
├── 01-getting-started/               # 快速开始
│   ├── README.md                     # 项目介绍
│   ├── setup.md                      # 环境搭建
│   └── development-guidelines.md     # 开发指南（现有）
│
├── 02-architecture/                  # 架构设计
│   ├── README.md                     # 架构总览
│   ├── overview.md                   # 整体架构（现有 architecture.md）
│   ├── cqrs/                         # CQRS架构（保留现有结构）
│   │   ├── README.md
│   │   ├── design/                   # 设计文档
│   │   └── reports/                  # 完成报告（合并 execution/）
│   └── layers.md                     # 分层架构说明
│
├── 03-features/                      # 功能模块文档
│   ├── README.md                     # 功能模块总览
│   ├── comments/                     # 评论系统
│   │   ├── README.md                 # 功能概述
│   │   ├── design.md                 # 设计方案（现有 comment-system-design.md）
│   │   ├── implementation.md         # 实现细节
│   │   ├── migration-tasks.md        # 迁移任务（现有）
│   │   └── completion-report.md      # 完成报告（现有）
│   ├── cache/                        # 缓存系统（保留现有）
│   │   ├── README.md
│   │   ├── implementation-guide.md
│   │   ├── quick-reference.md
│   │   └── verification-report.md
│   └── articles/                     # 文章系统（待补充）
│
├── 04-standards/                     # 开发规范（保留现有）
│   ├── README.md
│   ├── backend-development-standards.md
│   └── code-review-checklist.md
│
├── 05-api/                           # API文档
│   ├── README.md                     # API总览
│   ├── comments.md                   # 评论接口文档
│   ├── articles.md                   # 文章接口文档
│   └── swagger.md                    # Swagger使用指南
│
├── 06-database/                      # 数据库文档
│   ├── README.md                     # 数据库总览
│   ├── schema.md                     # 数据库表结构
│   ├── migrations.md                 # 迁移脚本
│   └── optimization.md               # 性能优化
│
├── 07-deployment/                    # 部署文档
│   ├── README.md                     # 部署指南
│   ├── production.md                 # 生产环境部署
│   └── troubleshooting.md            # 故障排查
│
└── 99-archive/                       # 归档（过时的文档）
    ├── README.md                     # 归档说明
    └── old-reports/                  # 旧的报告文档
```

### 前端文档结构 (CleverOnion-blog-f/docs/)

```
docs/
├── README.md                          # 文档总览和导航
│
├── 01-getting-started/               # 快速开始
│   ├── README.md                     # 项目介绍
│   ├── setup.md                      # 环境搭建
│   └── development-guide.md          # 开发指南
│
├── 02-architecture/                  # 架构设计
│   ├── README.md                     # 架构总览
│   ├── components.md                 # 组件架构
│   ├── state-management.md           # 状态管理
│   └── routing.md                    # 路由设计
│
├── 03-features/                      # 功能模块文档
│   ├── README.md                     # 功能总览
│   ├── comments/                     # 评论系统
│   │   ├── README.md
│   │   └── implementation.md
│   ├── sound-system/                 # 音效系统（保留现有）
│   │   ├── README.md
│   │   ├── architecture.md
│   │   ├── api-reference.md
│   │   └── implementation-guide.md
│   ├── editor/                       # 编辑器
│   │   ├── README.md
│   │   └── optimization.md           # 现有 article-editor-optimization.md
│   └── article-display/              # 文章展示
│
├── 04-accessibility/                 # 可访问性
│   ├── README.md                     # 可访问性总览
│   ├── guidelines.md                 # 可访问性指南
│   ├── implementations/              # 具体实现
│   │   ├── skip-to-content.md
│   │   ├── focus-management.md
│   │   ├── form-validation.md
│   │   └── button-loading-state.md
│   └── reports/                      # 优化报告
│       ├── improvements-summary.md
│       └── optimization-summary.md
│
├── 05-performance/                   # 性能优化
│   ├── README.md                     # 性能总览
│   ├── analysis.md                   # 性能分析（现有 frontend-optimization-analysis.md）
│   ├── implementations/              # 具体实现
│   │   ├── loading-skeleton.md
│   │   └── optimization-2.1-2.5.md
│   └── reports/                      # 优化报告
│       └── optimization-summary.md
│
├── 06-components/                    # 组件文档
│   ├── README.md                     # 组件总览
│   ├── ui-components.md              # UI组件库
│   └── custom-hooks.md               # 自定义Hooks
│
└── 99-archive/                       # 归档
    └── README.md
```

---

## 🔄 迁移映射表

### 后端文档迁移

| 现有文件                               | 新位置                                         | 操作        |
| -------------------------------------- | ---------------------------------------------- | ----------- |
| `development-guidelines.md`            | `01-getting-started/development-guidelines.md` | 移动        |
| `architecture.md`                      | `02-architecture/overview.md`                  | 重命名+移动 |
| `architecture/cqrs/*`                  | `02-architecture/cqrs/*`                       | 重组        |
| `cache/*`                              | `03-features/cache/*`                          | 移动        |
| `comment-system-design.md`             | `03-features/comments/design.md`               | 移动        |
| `comment-system-migration-tasks.md`    | `03-features/comments/migration-tasks.md`      | 移动        |
| `comment-system-upgrade-completion.md` | `03-features/comments/completion-report.md`    | 移动        |
| `comment-system-cleanup-summary.md`    | `03-features/comments/cleanup-summary.md`      | 移动        |
| `standards/*`                          | `04-standards/*`                               | 保持        |
| `features.md`                          | `03-features/README.md`                        | 重命名      |
| `issues-and-optimization.md`           | `99-archive/issues-and-optimization.md`        | 归档        |
| `STRUCTURE.md`                         | `02-architecture/structure.md`                 | 移动        |
| `WORK_SUMMARY.md`                      | `99-archive/work-summary.md`                   | 归档        |

### 前端文档迁移

| 现有文件                         | 新位置                               | 操作     |
| -------------------------------- | ------------------------------------ | -------- |
| `accessibility-*.md`             | `04-accessibility/implementations/*` | 分类移动 |
| `performance-*.md`               | `05-performance/implementations/*`   | 分类移动 |
| `article-editor-optimization.md` | `03-features/editor/optimization.md` | 移动     |
| `sound-system/*`                 | `03-features/sound-system/*`         | 保持     |
| `unsaved-changes-*.md`           | `03-features/editor/`                | 移动     |

---

## ✅ 新结构优势

1. **清晰的层次**：按功能模块分类
2. **易于查找**：数字前缀表示阅读顺序
3. **便于维护**：相关文档集中管理
4. **归档机制**：过时文档移到 archive
5. **模块化**：每个功能有自己的子目录

---

## 🚀 执行建议

1. 保留原始文档（备份）
2. 创建新目录结构
3. 移动/重命名文档
4. 创建各级 README.md 导航
5. 删除重复/过时文档
