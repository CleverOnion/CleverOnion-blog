# 架构设计

## 📖 文档列表

- [**overview.md**](./overview.md) - 整体架构设计
- [**structure.md**](./structure.md) - 项目结构说明
- [**cqrs/**](./cqrs/) - CQRS 架构详细文档

---

## 🏗️ 架构概览

CleverOnion 博客系统采用现代化的后端架构设计：

### 核心架构模式

1. **CQRS（命令查询职责分离）**

   - Command：处理写操作（创建、更新、删除）
   - Query：处理读操作（查询）
   - 职责分离，优化性能

2. **DDD（领域驱动设计）**

   - Aggregate：聚合根（用户、文章、评论等）
   - Value Object：值对象（ID、时间等）
   - Domain Event：领域事件

3. **分层架构**
   - Presentation（表示层）：Controller、DTO
   - Application（应用层）：Service、Command
   - Domain（领域层）：Aggregate、Repository 接口
   - Infrastructure（基础设施层）：Repository 实现、数据库

### 技术栈

- Spring Boot 3.x
- Sa-Token（认证）
- MySQL + Redis
- JPA + MyBatis

---

## 📚 详细文档

- [CQRS 架构设计](./cqrs/) - 完整的 CQRS 实现指南
- [整体架构](./overview.md) - 系统架构详解
- [项目结构](./structure.md) - 目录结构说明

---

**最后更新**：2025-10-04
