-- 文章示例数据插入脚本
-- 注意：执行前请确保已有用户、分类和标签数据

-- 插入示例文章数据
INSERT INTO articles (title, content, summary, status, category_id, author_id, created_at, updated_at, published_at) VALUES 
-- 1. Spring Boot 微服务架构实践
('Spring Boot 微服务架构实践', 
'# Spring Boot 微服务架构实践

## 概述

在现代软件开发中，微服务架构已经成为构建大型分布式系统的主流方式。Spring Boot 作为 Java 生态系统中最受欢迎的框架之一，为微服务开发提供了强大的支持。

## 微服务架构的优势

1. **独立部署**：每个服务可以独立部署和扩展
2. **技术多样性**：不同服务可以使用不同的技术栈
3. **故障隔离**：单个服务的故障不会影响整个系统
4. **团队自治**：不同团队可以独立开发和维护各自的服务

## Spring Boot 在微服务中的应用

### 1. 服务注册与发现

使用 Spring Cloud Netflix Eureka 实现服务注册与发现：

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### 2. 配置管理

使用 Spring Cloud Config 进行集中配置管理，支持配置的动态刷新。

### 3. 负载均衡

通过 Spring Cloud LoadBalancer 实现客户端负载均衡。

## 最佳实践

1. **数据库分离**：每个微服务应该有自己的数据库
2. **API 网关**：使用 Spring Cloud Gateway 作为统一入口
3. **监控和日志**：集成 Micrometer 和 ELK 栈进行监控
4. **容错处理**：使用 Resilience4j 实现断路器模式

## 总结

Spring Boot 为微服务架构提供了完整的解决方案，通过合理的架构设计和最佳实践，可以构建出高可用、可扩展的分布式系统。', 
'Spring Boot 微服务架构的实践指南，包括服务注册发现、配置管理、负载均衡等核心概念和最佳实践。', 
'PUBLISHED', 1, 1, NOW(), NOW(), NOW()),

-- 2. PostgreSQL 高级查询优化技巧
('PostgreSQL 高级查询优化技巧', 
'# PostgreSQL 高级查询优化技巧

## 前言

PostgreSQL 是一个功能强大的开源关系型数据库，在处理复杂查询时，合理的优化策略可以显著提升性能。

## 索引优化

### 1. B-tree 索引

最常用的索引类型，适用于等值查询和范围查询：

```sql
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_article_created_at ON articles(created_at);
```

### 2. 复合索引

对于多列查询，复合索引可以显著提升性能：

```sql
CREATE INDEX idx_article_status_category ON articles(status, category_id);
```

### 3. 部分索引

只对满足特定条件的行建立索引：

```sql
CREATE INDEX idx_published_articles ON articles(created_at) 
WHERE status = ''PUBLISHED'';
```

## 查询优化

### 1. 使用 EXPLAIN ANALYZE

分析查询执行计划：

```sql
EXPLAIN ANALYZE SELECT * FROM articles 
WHERE status = ''PUBLISHED'' 
ORDER BY created_at DESC 
LIMIT 10;
```

### 2. 避免 SELECT *

只查询需要的列：

```sql
-- 不推荐
SELECT * FROM articles;

-- 推荐
SELECT id, title, summary FROM articles;
```

### 3. 合理使用 JOIN

选择合适的 JOIN 类型和顺序：

```sql
SELECT a.title, c.name
FROM articles a
INNER JOIN categories c ON a.category_id = c.id
WHERE a.status = ''PUBLISHED'';
```

## 配置优化

### 1. 内存配置

```
shared_buffers = 256MB
work_mem = 4MB
maintenance_work_mem = 64MB
```

### 2. 连接配置

```
max_connections = 100
max_prepared_transactions = 0
```

## 监控和维护

### 1. 定期 VACUUM

```sql
VACUUM ANALYZE articles;
```

### 2. 统计信息更新

```sql
ANALYZE articles;
```

## 总结

PostgreSQL 查询优化是一个系统性工程，需要从索引设计、查询编写、配置调优等多个方面综合考虑。', 
'PostgreSQL 数据库查询优化的高级技巧，包括索引优化、查询优化、配置调优等实用方法。', 
'PUBLISHED', 1, 1, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

-- 3. Redis 缓存设计模式与最佳实践
('Redis 缓存设计模式与最佳实践', 
'# Redis 缓存设计模式与最佳实践

## 引言

Redis 作为高性能的内存数据库，在现代应用架构中扮演着重要的缓存角色。合理的缓存设计模式可以显著提升应用性能。

## 常见缓存模式

### 1. Cache-Aside 模式

应用程序直接管理缓存：

```java
public Article getArticle(Long id) {
    // 先查缓存
    Article article = redisTemplate.opsForValue().get("article:" + id);
    if (article != null) {
        return article;
    }
    
    // 缓存未命中，查数据库
    article = articleRepository.findById(id);
    if (article != null) {
        // 写入缓存
        redisTemplate.opsForValue().set("article:" + id, article, Duration.ofHours(1));
    }
    return article;
}
```

### 2. Write-Through 模式

写操作同时更新缓存和数据库。

### 3. Write-Behind 模式

先写缓存，异步写数据库。

## 缓存策略

### 1. 过期策略

- **TTL 设置**：为不同类型的数据设置合适的过期时间
- **随机过期**：避免缓存雪崩

### 2. 淘汰策略

- **LRU**：最近最少使用
- **LFU**：最不经常使用
- **Random**：随机淘汰

## 性能优化

### 1. 连接池配置

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

### 2. 序列化优化

使用高效的序列化方式，如 Kryo 或 Protobuf。

### 3. 批量操作

使用 Pipeline 或 Lua 脚本进行批量操作。

## 常见问题及解决方案

### 1. 缓存穿透

对于不存在的数据，缓存空值或使用布隆过滤器。

### 2. 缓存击穿

使用互斥锁或设置热点数据永不过期。

### 3. 缓存雪崩

设置随机过期时间，使用多级缓存。

## 监控和运维

### 1. 关键指标

- 命中率
- 响应时间
- 内存使用率
- 连接数

### 2. 告警设置

设置合理的告警阈值，及时发现问题。

## 总结

Redis 缓存设计需要根据业务场景选择合适的模式和策略，同时要考虑数据一致性、性能和可用性的平衡。', 
'Redis 缓存设计模式与最佳实践，包括常见缓存模式、缓存策略、性能优化和常见问题解决方案。', 
'PUBLISHED', 1, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

-- 4. DDD 领域驱动设计实战
('DDD 领域驱动设计实战', 
'# DDD 领域驱动设计实战

## 什么是 DDD

领域驱动设计（Domain-Driven Design）是一种软件开发方法论，强调将业务领域的复杂性作为软件设计的核心。

## 核心概念

### 1. 领域模型

领域模型是对业务领域的抽象表示：

```java
@Entity
public class Article {
    private ArticleId id;
    private String title;
    private String content;
    private ArticleStatus status;
    private CategoryId categoryId;
    private AuthorId authorId;
    
    public void publish() {
        if (this.status == ArticleStatus.DRAFT) {
            this.status = ArticleStatus.PUBLISHED;
            this.publishedAt = LocalDateTime.now();
            // 发布领域事件
            DomainEventPublisher.publish(new ArticlePublishedEvent(this.id));
        }
    }
}
```

### 2. 聚合根

聚合根是聚合的唯一入口，负责维护聚合内部的一致性。

### 3. 值对象

值对象是不可变的，通过属性值来标识：

```java
public class ArticleId {
    private final Long value;
    
    public ArticleId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Article ID must be positive");
        }
        this.value = value;
    }
}
```

### 4. 领域服务

当业务逻辑不属于任何特定实体时，使用领域服务：

```java
@Service
public class ArticlePublishService {
    public void publishArticle(Article article, User author) {
        // 检查发布权限
        if (!author.canPublishArticle()) {
            throw new InsufficientPermissionException();
        }
        
        // 执行发布逻辑
        article.publish();
    }
}
```

## 分层架构

### 1. 表现层（Presentation）

处理用户界面和 HTTP 请求。

### 2. 应用层（Application）

协调领域对象完成业务用例。

### 3. 领域层（Domain）

包含业务逻辑和规则。

### 4. 基础设施层（Infrastructure）

提供技术实现，如数据持久化。

## 最佳实践

### 1. 统一语言

团队成员使用相同的业务术语。

### 2. 限界上下文

将大型系统分解为多个限界上下文。

### 3. 事件驱动

使用领域事件实现松耦合。

### 4. 测试策略

重点测试领域逻辑，保证业务规则的正确性。

## 总结

DDD 帮助我们构建更贴近业务的软件模型，通过合理的分层和抽象，提高代码的可维护性和可扩展性。', 
'DDD 领域驱动设计的实战指南，包括核心概念、分层架构、最佳实践等内容。', 
'PUBLISHED', 3, 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),

-- 5. 我的编程学习心得
('我的编程学习心得', 
'# 我的编程学习心得

## 学习历程

回顾我的编程学习之路，从最初的困惑到现在的相对熟练，这个过程充满了挑战和收获。

## 学习方法

### 1. 理论与实践结合

- **看书学理论**：系统学习编程基础知识
- **动手写代码**：通过实际项目巩固理论知识
- **解决实际问题**：在工作中应用所学技能

### 2. 持续学习

技术更新很快，需要保持学习的习惯：

- 关注技术博客和社区
- 参加技术会议和培训
- 阅读开源项目代码
- 尝试新的技术和工具

### 3. 总结反思

定期总结学习成果和经验教训：

- 写技术博客
- 整理学习笔记
- 分享给团队成员
- 参与技术讨论

## 遇到的挑战

### 1. 技术选择困难

面对众多的技术栈，如何选择适合的技术是一个挑战。

**解决方案**：
- 了解项目需求
- 考虑团队技能
- 评估技术成熟度
- 权衡学习成本

### 2. 调试复杂问题

复杂的 bug 往往需要大量时间和精力去解决。

**解决方案**：
- 系统性分析问题
- 使用调试工具
- 查阅文档和资料
- 寻求他人帮助

### 3. 保持代码质量

在项目压力下，容易忽视代码质量。

**解决方案**：
- 制定编码规范
- 进行代码审查
- 编写单元测试
- 重构优化代码

## 收获与感悟

### 1. 技术能力提升

- 掌握了多种编程语言
- 熟悉了常用的开发框架
- 具备了系统设计能力
- 提高了问题解决能力

### 2. 软技能发展

- 沟通协作能力
- 项目管理能力
- 学习能力
- 抗压能力

### 3. 职业发展

编程技能为我的职业发展提供了强有力的支撑。

## 给新手的建议

1. **打好基础**：重视基础知识的学习
2. **多动手**：理论学习要结合实践
3. **不怕犯错**：从错误中学习和成长
4. **保持耐心**：编程学习是一个长期过程
5. **寻找导师**：有经验的人可以提供宝贵指导

## 未来规划

- 深入学习架构设计
- 关注新兴技术趋势
- 提升团队协作能力
- 分享知识和经验

## 总结

编程学习是一个持续的过程，需要保持好奇心和学习热情。通过不断的实践和总结，我们可以在这条路上走得更远。', 
'分享个人编程学习的心得体会，包括学习方法、遇到的挑战、收获感悟以及给新手的建议。', 
'PUBLISHED', 2, 1, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),

-- 6. 前端性能优化实践（草稿）
('前端性能优化实践', 
'# 前端性能优化实践

## 概述

前端性能优化是提升用户体验的重要手段，本文将分享一些实用的优化技巧和最佳实践。

## 加载性能优化

### 1. 资源压缩

- JavaScript 和 CSS 文件压缩
- 图片压缩和格式优化
- Gzip 压缩

### 2. 缓存策略

- 浏览器缓存
- CDN 缓存
- Service Worker 缓存

### 3. 代码分割

- 路由级别的代码分割
- 组件级别的懒加载
- 第三方库的分离

## 运行时性能优化

### 1. 减少重排重绘

- 避免频繁的 DOM 操作
- 使用 CSS3 动画
- 合理使用 transform

### 2. 内存管理

- 及时清理事件监听器
- 避免内存泄漏
- 合理使用闭包

## 总结

前端性能优化需要从多个维度考虑，持续监控和优化是关键。', 
'前端性能优化的实践经验，包括加载性能和运行时性能的优化技巧。', 
'DRAFT', 1, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NULL),

-- 7. 微服务架构设计思考（草稿）
('微服务架构设计思考', 
'# 微服务架构设计思考

## 背景

随着业务的发展，单体应用逐渐暴露出一些问题，微服务架构成为了一个可行的解决方案。

## 微服务拆分原则

### 1. 业务边界

按照业务领域进行拆分，确保每个服务有明确的职责。

### 2. 数据独立

每个微服务应该有自己的数据库，避免数据耦合。

### 3. 团队结构

考虑团队的组织结构，确保服务边界与团队边界一致。

## 技术选型

### 1. 服务注册与发现

- Eureka
- Consul
- Nacos

### 2. 配置管理

- Spring Cloud Config
- Apollo
- Nacos Config

### 3. 服务网关

- Spring Cloud Gateway
- Zuul
- Kong

## 挑战与解决方案

### 1. 分布式事务

- Saga 模式
- TCC 模式
- 事件驱动架构

### 2. 服务监控

- 链路追踪
- 指标监控
- 日志聚合

### 3. 部署复杂性

- 容器化部署
- CI/CD 流水线
- 蓝绿部署

## 总结

微服务架构不是银弹，需要根据实际情况权衡利弊。', 
'微服务架构设计的思考和实践，包括拆分原则、技术选型、挑战与解决方案。', 
'DRAFT', 4, 1, NOW(), NOW(), NULL);

-- 插入文章标签关联数据
INSERT INTO article_tags (article_id, tag_id, created_at) VALUES 
-- Spring Boot 微服务架构实践 (文章ID需要根据实际插入后的ID调整)
(1, 1, NOW()),  -- Java
(1, 2, NOW()),  -- Spring Boot
(1, 6, NOW()),  -- 后端
(1, 9, NOW()),  -- DDD

-- PostgreSQL 高级查询优化技巧
(2, 3, NOW()),  -- PostgreSQL
(2, 7, NOW()),  -- 数据库

-- Redis 缓存设计模式与最佳实践
(3, 4, NOW()),  -- Redis
(3, 6, NOW()),  -- 后端

-- DDD 领域驱动设计实战
(4, 1, NOW()),  -- Java
(4, 9, NOW()),  -- DDD
(4, 6, NOW()),  -- 后端

-- 我的编程学习心得
(5, 1, NOW()),  -- Java

-- 前端性能优化实践（草稿）
(6, 5, NOW()),  -- 前端

-- 微服务架构设计思考（草稿）
(7, 1, NOW()),  -- Java
(7, 2, NOW()),  -- Spring Boot
(7, 6, NOW());  -- 后端

-- 查询插入的数据
SELECT 
    a.id,
    a.title,
    a.status,
    c.name as category_name,
    a.created_at
FROM articles a
LEFT JOIN categories c ON a.category_id = c.id
ORDER BY a.created_at DESC;