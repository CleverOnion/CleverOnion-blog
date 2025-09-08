-- CleverOnion Blog 测试数据脚本
-- 版本: V2
-- 描述: 插入测试用户、文章、评论等数据，仅用于开发和测试环境

-- 注意：此脚本仅在开发和测试环境中执行，生产环境请勿使用

-- 1. 插入测试用户
INSERT INTO users (github_id, username, avatar_url) VALUES 
    (12345678, 'testuser1', 'https://avatars.githubusercontent.com/u/12345678?v=4'),
    (87654321, 'developer', 'https://avatars.githubusercontent.com/u/87654321?v=4'),
    (11111111, 'blogger', 'https://avatars.githubusercontent.com/u/11111111?v=4');

-- 2. 插入更多分类
INSERT INTO categories (name, icon) VALUES 
    ('开源项目', 'fab fa-github'),
    ('算法与数据结构', 'fas fa-sitemap'),
    ('系统设计', 'fas fa-cogs'),
    ('工具推荐', 'fas fa-tools');

-- 3. 插入更多标签
INSERT INTO tags (name) VALUES 
    ('Vue.js'),
    ('React'),
    ('Docker'),
    ('Kubernetes'),
    ('MySQL'),
    ('MongoDB'),
    ('缓存'),
    ('消息队列'),
    ('分布式'),
    ('性能优化'),
    ('代码重构'),
    ('单元测试'),
    ('CI/CD'),
    ('DevOps'),
    ('Linux');

-- 4. 插入测试文章
INSERT INTO articles (title, content, summary, status, category_id, author_id, published_at) VALUES 
    (
        'Spring Boot 项目搭建指南',
        '# Spring Boot 项目搭建指南\n\n## 1. 环境准备\n\n在开始之前，请确保你的开发环境已经安装了以下工具：\n\n- JDK 17 或更高版本\n- Maven 3.6+ 或 Gradle 7+\n- IDE（推荐 IntelliJ IDEA 或 Eclipse）\n\n## 2. 创建项目\n\n使用 Spring Initializr 创建项目...\n\n## 3. 添加依赖\n\n在 pom.xml 中添加必要的依赖...\n\n## 4. 配置文件\n\n配置 application.yml 文件...\n\n## 总结\n\n通过以上步骤，我们成功搭建了一个基础的 Spring Boot 项目。',
        '本文详细介绍了如何从零开始搭建一个 Spring Boot 项目，包括环境准备、项目创建、依赖配置等关键步骤。',
        'PUBLISHED',
        (SELECT id FROM categories WHERE name = '技术分享'),
        (SELECT id FROM users WHERE username = 'testuser1'),
        CURRENT_TIMESTAMP - INTERVAL '7 days'
    ),
    (
        'PostgreSQL 性能优化实践',
        '# PostgreSQL 性能优化实践\n\n## 前言\n\nPostgreSQL 是一个功能强大的开源关系型数据库，但在高并发场景下，如何优化其性能是一个重要话题。\n\n## 1. 索引优化\n\n### 1.1 B-Tree 索引\n\nB-Tree 索引是最常用的索引类型...\n\n### 1.2 复合索引\n\n在多列查询时，复合索引能显著提升性能...\n\n## 2. 查询优化\n\n### 2.1 EXPLAIN 分析\n\n使用 EXPLAIN 命令分析查询计划...\n\n### 2.2 避免全表扫描\n\n通过合理的 WHERE 条件避免全表扫描...\n\n## 3. 配置优化\n\n### 3.1 内存配置\n\n合理配置 shared_buffers、work_mem 等参数...\n\n### 3.2 连接池\n\n使用连接池减少连接开销...\n\n## 总结\n\n通过索引优化、查询优化和配置优化，可以显著提升 PostgreSQL 的性能。',
        '深入探讨 PostgreSQL 数据库的性能优化策略，包括索引优化、查询优化和配置调优等方面的实践经验。',
        'PUBLISHED',
        (SELECT id FROM categories WHERE name = '技术分享'),
        (SELECT id FROM users WHERE username = 'developer'),
        CURRENT_TIMESTAMP - INTERVAL '3 days'
    ),
    (
        '我的编程学习之路',
        '# 我的编程学习之路\n\n## 初识编程\n\n还记得第一次接触编程是在大学的计算机基础课上...\n\n## 语言选择\n\n从 C 语言开始，到后来的 Java、Python...\n\n## 项目实践\n\n理论学习固然重要，但项目实践更能提升编程能力...\n\n## 持续学习\n\n技术更新很快，保持学习的心态很重要...\n\n## 感悟\n\n编程不仅仅是写代码，更是一种思维方式...',
        '分享个人的编程学习经历，从初学者到现在的成长历程，以及一些学习心得和感悟。',
        'PUBLISHED',
        (SELECT id FROM categories WHERE name = '生活随笔'),
        (SELECT id FROM users WHERE username = 'blogger'),
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    ),
    (
        'Redis 缓存设计模式',
        '# Redis 缓存设计模式\n\n## 缓存模式概述\n\n在高并发系统中，缓存是提升性能的重要手段...\n\n## 1. Cache-Aside 模式\n\n这是最常用的缓存模式...\n\n## 2. Write-Through 模式\n\n写入时同时更新缓存和数据库...\n\n## 3. Write-Behind 模式\n\n异步写入数据库，提升写入性能...\n\n## 缓存一致性\n\n如何保证缓存和数据库的一致性是一个重要问题...',
        '详细介绍 Redis 缓存的几种设计模式，以及如何在实际项目中选择合适的缓存策略。',
        'DRAFT',
        (SELECT id FROM categories WHERE name = '技术分享'),
        (SELECT id FROM users WHERE username = 'developer'),
        NULL
    );

-- 5. 为文章添加标签关联
INSERT INTO article_tags (article_id, tag_id) VALUES 
    -- Spring Boot 项目搭建指南
    ((SELECT id FROM articles WHERE title = 'Spring Boot 项目搭建指南'), (SELECT id FROM tags WHERE name = 'Java')),
    ((SELECT id FROM articles WHERE title = 'Spring Boot 项目搭建指南'), (SELECT id FROM tags WHERE name = 'Spring Boot')),
    ((SELECT id FROM articles WHERE title = 'Spring Boot 项目搭建指南'), (SELECT id FROM tags WHERE name = '后端')),
    
    -- PostgreSQL 性能优化实践
    ((SELECT id FROM articles WHERE title = 'PostgreSQL 性能优化实践'), (SELECT id FROM tags WHERE name = 'PostgreSQL')),
    ((SELECT id FROM articles WHERE title = 'PostgreSQL 性能优化实践'), (SELECT id FROM tags WHERE name = '数据库')),
    ((SELECT id FROM articles WHERE title = 'PostgreSQL 性能优化实践'), (SELECT id FROM tags WHERE name = '性能优化')),
    
    -- 我的编程学习之路
    ((SELECT id FROM articles WHERE title = '我的编程学习之路'), (SELECT id FROM tags WHERE name = 'Java')),
    
    -- Redis 缓存设计模式
    ((SELECT id FROM articles WHERE title = 'Redis 缓存设计模式'), (SELECT id FROM tags WHERE name = 'Redis')),
    ((SELECT id FROM articles WHERE title = 'Redis 缓存设计模式'), (SELECT id FROM tags WHERE name = '缓存')),
    ((SELECT id FROM articles WHERE title = 'Redis 缓存设计模式'), (SELECT id FROM tags WHERE name = '架构设计'));

-- 6. 插入测试评论
INSERT INTO comments (content, article_id, user_id, parent_id) VALUES 
    (
        '非常详细的教程，对新手很友好！',
        (SELECT id FROM articles WHERE title = 'Spring Boot 项目搭建指南'),
        (SELECT id FROM users WHERE username = 'developer'),
        NULL
    ),
    (
        '感谢分享，正好在学习 Spring Boot',
        (SELECT id FROM articles WHERE title = 'Spring Boot 项目搭建指南'),
        (SELECT id FROM users WHERE username = 'blogger'),
        NULL
    ),
    (
        '同感，项目实践确实很重要',
        (SELECT id FROM articles WHERE title = '我的编程学习之路'),
        (SELECT id FROM users WHERE username = 'testuser1'),
        NULL
    ),
    (
        '谢谢鼓励！一起加油💪',
        (SELECT id FROM articles WHERE title = '我的编程学习之路'),
        (SELECT id FROM users WHERE username = 'blogger'),
        (SELECT id FROM comments WHERE content = '同感，项目实践确实很重要')
    ),
    (
        '这些优化技巧很实用，已收藏',
        (SELECT id FROM articles WHERE title = 'PostgreSQL 性能优化实践'),
        (SELECT id FROM users WHERE username = 'testuser1'),
        NULL
    );

-- 添加注释
COMMENT ON COLUMN articles.content IS '文章内容使用 Markdown 格式编写';
COMMENT ON COLUMN comments.parent_id IS '父评论ID，NULL表示顶级评论，非NULL表示回复评论';