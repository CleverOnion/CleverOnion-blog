# 架构设计文档

本目录包含 CleverOnion 后端项目的架构设计和改进方案文档。

## 📁 目录结构

```
architecture/
└── cqrs/                    # CQRS架构专题
    ├── README.md            # CQRS专题说明
    ├── design/              # 设计和理论文档
    │   ├── cqrs-implementation-guide.md    # 完整实施指南
    │   ├── cqrs-query-object-guide.md      # 查询对象使用指南
    │   └── cqrs-quick-reference.md         # 快速参考手册
    │
    └── execution/           # 执行和进度文档
        ├── cqrs-implementation-tasks.md    # 详细任务清单
        ├── cqrs-progress-tracker.md        # 进度跟踪表
        └── EXECUTION_SUMMARY.md            # 执行总结
```

## 🎯 文档导航

### CQRS 架构改进

**完整的命令查询职责分离（CQRS）实施方案**

- 📖 [CQRS 专题主页](./cqrs/README.md)

#### 设计文档

- [CQRS 架构实施指南](./cqrs/design/cqrs-implementation-guide.md) - 完整的技术方案和设计
- [CQRS 快速参考手册](./cqrs/design/cqrs-quick-reference.md) - 核心概念和代码模板
- [CQRS 查询对象指南](./cqrs/design/cqrs-query-object-guide.md) - 查询对象使用说明

#### 执行文档

- [CQRS 实施任务清单](./cqrs/execution/cqrs-implementation-tasks.md) - 详细的执行任务（46个）
- [CQRS 进度跟踪表](./cqrs/execution/cqrs-progress-tracker.md) - 实时进度跟踪
- [执行总结](./cqrs/execution/EXECUTION_SUMMARY.md) - 当前执行情况

---

## 🔄 未来规划

此目录将持续扩展，计划添加：

- **事件溯源（Event Sourcing）** - 基于事件的架构模式
- **读写分离** - 数据库主从架构设计
- **微服务拆分** - 服务拆分和边界划分
- **分布式架构** - 消息队列、服务网格等

---

**返回**: [文档首页](../README.md)


