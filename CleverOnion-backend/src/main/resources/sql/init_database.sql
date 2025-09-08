-- CleverOnion Blog 数据库初始化脚本
-- 版本: V1
-- 描述: 创建用户、分类、标签、文章、评论等核心表

-- 1. 创建用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    github_id BIGINT NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 为 github_id 创建唯一索引
CREATE UNIQUE INDEX idx_users_github_id ON users(github_id);

-- 2. 创建分类表
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    icon VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 为分类名称创建唯一索引
CREATE UNIQUE INDEX idx_categories_name ON categories(name);

-- 3. 创建标签表
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 为标签名称创建唯一索引
CREATE UNIQUE INDEX idx_tags_name ON tags(name);

-- 4. 创建文章表
CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    summary TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    category_id BIGINT,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    CONSTRAINT fk_articles_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_articles_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_articles_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

-- 为文章表创建索引
CREATE INDEX idx_articles_status ON articles(status);
CREATE INDEX idx_articles_category_id ON articles(category_id);
CREATE INDEX idx_articles_author_id ON articles(author_id);
CREATE INDEX idx_articles_published_at ON articles(published_at);
CREATE INDEX idx_articles_created_at ON articles(created_at);

-- 为文章表创建复合索引，优化分页查询性能
CREATE INDEX idx_articles_status_published_at ON articles(status, published_at DESC);
CREATE INDEX idx_articles_status_created_at ON articles(status, created_at DESC);
CREATE INDEX idx_articles_category_status_published_at ON articles(category_id, status, published_at DESC);
CREATE INDEX idx_articles_author_status_published_at ON articles(author_id, status, published_at DESC);

-- 5. 创建文章标签关联表
CREATE TABLE article_tags (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    CONSTRAINT fk_article_tags_article FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    CONSTRAINT fk_article_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- 为文章标签关联表创建索引
CREATE INDEX idx_article_tags_article_id ON article_tags(article_id);
CREATE INDEX idx_article_tags_tag_id ON article_tags(tag_id);

-- 6. 创建评论表
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    article_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);

-- 为评论表创建索引
CREATE INDEX idx_comments_article_id ON comments(article_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_id);
CREATE INDEX idx_comments_created_at ON comments(created_at);

-- 7. 创建触发器函数，用于自动更新 updated_at 字段
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 8. 为各表创建更新时间触发器
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tags_updated_at BEFORE UPDATE ON tags
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_articles_updated_at BEFORE UPDATE ON articles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_comments_updated_at BEFORE UPDATE ON comments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 9. 插入初始数据
-- 插入默认分类
INSERT INTO categories (name) VALUES 
    ('技术分享'),
    ('生活随笔'),
    ('学习笔记'),
    ('项目总结');

-- 插入常用标签
INSERT INTO tags (name) VALUES 
    ('Java'),
    ('Spring Boot'),
    ('PostgreSQL'),
    ('Redis'),
    ('前端'),
    ('后端'),
    ('数据库'),
    ('架构设计'),
    ('DDD'),
    ('微服务');

-- 添加注释
COMMENT ON TABLE users IS '用户表，存储通过GitHub登录的用户信息';
COMMENT ON TABLE categories IS '文章分类表';
COMMENT ON TABLE tags IS '文章标签表';

COMMENT ON COLUMN categories.icon IS '分类图标，可以是图标类名或图标URL';
COMMENT ON TABLE articles IS '文章表，存储博客文章内容';
COMMENT ON TABLE article_tags IS '文章标签关联表，多对多关系';
COMMENT ON TABLE comments IS '评论表，支持层级回复';

COMMENT ON COLUMN users.github_id IS 'GitHub用户唯一ID';
COMMENT ON COLUMN users.username IS 'GitHub用户名';
COMMENT ON COLUMN users.avatar_url IS 'GitHub头像链接';
COMMENT ON COLUMN articles.status IS '文章状态：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档';
COMMENT ON COLUMN articles.content IS '文章正文，Markdown格式';
COMMENT ON COLUMN comments.parent_id IS '父评论ID，用于层级回复，NULL表示顶级评论';