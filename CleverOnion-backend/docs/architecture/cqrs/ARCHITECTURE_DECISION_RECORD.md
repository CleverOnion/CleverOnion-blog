# CQRS 架构决策记录（ADR）

## 📅 创建日期：2025-10-01

---

## 决策 1：在 Controller 层缓存 Response 对象

**日期**: 2025-10-01  
**状态**: ✅ 已采纳并实施  
**决策者**: 开发团队

### 背景

在 CQRS 架构实施过程中，遇到 Redis 缓存反序列化值对象（CategoryId、TagId 等）失败的问题：

```
SerializationException: Cannot construct instance of CategoryId
```

### 考虑的方案

#### 方案 A: 在值对象中添加 Jackson 注解 ❌

```java
@JsonCreator
public CategoryId(@JsonProperty("value") Long value) { ... }
```

**优点**: 快速解决序列化问题  
**缺点**:

- ❌ 领域层包含技术依赖
- ❌ 违反 DDD 原则
- ❌ 值对象不纯粹

#### 方案 B: 在 Controller 层缓存 Response 对象 ✅

```java
@GetMapping("/{id}")
@Cacheable(value = "category-responses", key = "#id")
public Result<CategoryResponse> getCategoryById(@PathVariable Long id) { ... }
```

**优点**:

- ✅ 领域层保持纯粹
- ✅ 符合 DDD 分层原则
- ✅ Response 对象序列化无问题
- ✅ 更灵活的缓存策略

**缺点**:

- ⚠️ 需要移除 51 个@Cacheable 注解
- ⚠️ 需要重新在 Controller 添加缓存

### 决策

**采用方案 B**: 在 Controller 层缓存 Response 对象

**理由**:

1. **符合 DDD 原则**: 领域层不应包含技术实现细节
2. **架构更优雅**: 分层清晰，技术与业务分离
3. **长期收益**: 更易维护和扩展

### 影响

**立即影响**:

- 移除了 4 个 QueryService 中的 51 个@Cacheable 注解
- 移除了值对象中的 Jackson 注解
- QueryService 恢复为纯业务逻辑

**后续工作**:

- 需要在 Controller 层按需添加缓存
- 参考缓存策略指南文档
- 优先为高频端点添加缓存

### 实施

**已完成**:

- ✅ 移除 CategoryQueryService 的 13 个@Cacheable
- ✅ 移除 TagQueryService 的 21 个@Cacheable
- ✅ 移除 CommentQueryService 的 9 个@Cacheable
- ✅ 移除 UserQueryService 的 8 个@Cacheable
- ✅ 创建 CACHE_STRATEGY_GUIDE.md 文档

**待实施**:

- ⬜ 在 Controller 关键端点添加@Cacheable（按需）

---

## 决策 2：所有事件必须继承 DomainEvent 基类

**日期**: 2025-10-01  
**状态**: ✅ 已采纳并实施

### 背景

项目中已存在 DomainEvent 基类，但部分事件（如 TagDeletedEvent）没有继承。

### 决策

**所有领域事件必须继承 DomainEvent 基类**

**理由**:

1. 统一事件机制
2. 自动生成事件 ID 和时间戳
3. 便于事件追踪和审计

### 实施

- ✅ CategoryCreatedEvent
- ✅ CategoryUpdatedEvent
- ✅ CategoryDeletedEvent
- ✅ TagCreatedEvent
- ✅ TagUpdatedEvent
- ✅ TagDeletedEvent（修正）
- ✅ CommentCreatedEvent
- ✅ CommentDeletedEvent

---

## 决策 3：User 模块采用简化版 CQRS

**日期**: 2025-10-01  
**状态**: ✅ 已采纳并实施

### 背景

User 模块命令操作很少（只有 GitHub 同步），不需要复杂的 CQRS 实现。

### 决策

**User 模块采用简化版 CQRS**

**特点**:

- 只有 1 个命令方法
- 不发布领域事件（可选）
- 重点优化查询性能

**理由**:

- 避免过度设计
- 符合实际需求
- 保持架构简洁

---

## 决策 4：CommandService 保留@CacheEvict 注解

**日期**: 2025-10-01  
**状态**: ✅ 已采纳并实施

### 背景

虽然 QueryService 移除了@Cacheable，但 CommandService 的@CacheEvict 是否保留？

### 决策

**CommandService 保留@CacheEvict 注解**

**理由**:

1. @CacheEvict 是应用层的技术实现，不是领域层
2. 命令服务本身就是应用层
3. 自动失效缓存更可靠

**示例**:

```java
@Service
@Transactional
public class CategoryCommandService {

    @CacheEvict(value = {"category-responses", "category-list-responses"}, allEntries = true)
    public CategoryAggregate createCategory(CreateCategoryCommand command) {
        // ...
    }
}
```

**注意**: 缓存区域名称需要对应 Controller 层的缓存

---

## 📝 决策记录模板

### 决策 X：[决策标题]

**日期**: YYYY-MM-DD  
**状态**: ⬜ 待决策 / 🟡 讨论中 / ✅ 已采纳 / ❌ 已拒绝

#### 背景

[描述问题或需求]

#### 考虑的方案

- 方案 A: [描述]
- 方案 B: [描述]

#### 决策

[选择的方案和理由]

#### 影响

[对系统的影响]

#### 实施

[实施步骤和进度]

---

**文档版本**: v1.0  
**最后更新**: 2025-10-01  
**维护者**: 架构团队

**相关文档**:

- [CACHE_STRATEGY_GUIDE.md](./CACHE_STRATEGY_GUIDE.md)
- [FINAL_COMPLETION_REPORT.md](./FINAL_COMPLETION_REPORT.md)
