-- 1. 文章状态枚举类型
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'article_status') THEN
CREATE TYPE article_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
ELSE
        -- 如果已存在但缺少 ARCHIVED，可以用 ALTER 添加
        -- 这里只做检查，不做改动，生产可用 ALTER TYPE 添加
        RAISE NOTICE 'article_status type exists, 请确认是否包含 ARCHIVED';
END IF;
END$$;

-- 2. 用户表，存 GitHub 登录用户
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     github_id BIGINT NOT NULL UNIQUE,
                                     github_login VARCHAR(100) NOT NULL,
                                     avatar_url TEXT,
                                     created_at TIMESTAMPTZ DEFAULT NOW()
);


-- 4. 文章表
CREATE TABLE IF NOT EXISTS articles (
                                        id BIGSERIAL PRIMARY KEY,
                                        author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                        title VARCHAR(255) NOT NULL,
                                        content_md TEXT NOT NULL,
                                        content_html TEXT,
                                        status article_status NOT NULL DEFAULT 'DRAFT',
                                        view_count INT NOT NULL DEFAULT 0,
                                        created_at TIMESTAMPTZ DEFAULT NOW(),
                                        updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. 标签表
CREATE TABLE IF NOT EXISTS tags (
                                    id BIGSERIAL PRIMARY KEY,
                                    name VARCHAR(50) NOT NULL UNIQUE
);

-- 6. 文章-标签多对多关联表
CREATE TABLE IF NOT EXISTS article_tags (
                                            article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
                                            tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
                                            PRIMARY KEY(article_id, tag_id)
);

-- 7. 评论表，支持多层级 parent_id
CREATE TABLE IF NOT EXISTS comments (
                                        id BIGSERIAL PRIMARY KEY,
                                        article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
                                        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                        content TEXT NOT NULL,
                                        parent_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
                                        created_at TIMESTAMPTZ DEFAULT NOW()
);
