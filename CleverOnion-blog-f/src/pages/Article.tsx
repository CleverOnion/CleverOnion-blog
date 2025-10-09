import React, { useState, useEffect, useRef, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Header from "../components/Header";
import ArticleHero from "../components/article/ArticleHero";
import ArticleContent from "../components/article/ArticleContent";
import TableOfContents from "../components/article/TableOfContents";
import CommentSection from "../components/article/CommentSection";
import { articleApi, Article as ArticleType } from "../api/articles";
import { AuthAPI } from "../api/auth";

interface TOCItem {
  id: string;
  title: string;
  level: number;
}

// 预设的背景颜色方案
const BACKGROUND_COLORS = [
  "bg-gradient-to-br from-orange-400 via-orange-500 to-red-500", // 橙红渐变
  "bg-gradient-to-br from-blue-400 via-indigo-500 to-purple-600", // 蓝紫渐变
  "bg-gradient-to-br from-green-400 via-teal-500 to-cyan-600", // 绿青渐变
  "bg-gradient-to-br from-pink-400 via-rose-500 to-red-600", // 粉红渐变
  "bg-gradient-to-br from-yellow-400 via-amber-500 to-orange-600", // 黄橙渐变
  "bg-gradient-to-br from-purple-400 via-violet-500 to-indigo-600", // 紫蓝渐变
];

const Article: React.FC = () => {
  const { articleId: id } = useParams<{ articleId: string }>();
  const navigate = useNavigate();
  const [article, setArticle] = useState<ArticleType | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [tableOfContents, setTableOfContents] = useState<TOCItem[]>([]);
  const articleRef = useRef<HTMLElement>(null);

  // 随机选择一个背景色，使用 useMemo 确保在组件生命周期内保持一致
  const randomBackground = useMemo(() => {
    return BACKGROUND_COLORS[
      Math.floor(Math.random() * BACKGROUND_COLORS.length)
    ];
  }, []);

  // 从Markdown内容中提取目录
  const extractTableOfContents = (content: string): TOCItem[] => {
    const headingRegex = /^(#{1,6})\s+(.+)$/gm;
    const headings: TOCItem[] = [];
    let match;

    while ((match = headingRegex.exec(content)) !== null) {
      const level = match[1].length;
      const title = match[2].trim();
      const id = title
        .toLowerCase()
        .replace(/[^\w\s-]/g, "")
        .replace(/\s+/g, "-")
        .replace(/-+/g, "-")
        .replace(/^-|-$/g, "");

      headings.push({ id, title, level });
    }

    return headings;
  };

  // 获取文章数据
  useEffect(() => {
    const fetchArticle = async () => {
      if (!id) {
        setError("文章ID不存在");
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const articleData = await articleApi.getArticleById(id);

        // 检查文章状态，如果是草稿状态需要验证管理员权限
        if (articleData.status === "DRAFT") {
          try {
            const adminResponse = await AuthAPI.checkAdminStatus();
            if (!adminResponse.isAdmin) {
              // 非管理员访问草稿文章，跳转到404页面
              navigate("/404", { replace: true });
              return;
            }
          } catch (error) {
            // 权限检查失败，跳转到404页面
            console.error("权限检查失败:", error);
            navigate("/404", { replace: true });
            return;
          }
        }

        setArticle(articleData);

        // 从文章内容中提取目录
        if (articleData.content) {
          const toc = extractTableOfContents(articleData.content);
          setTableOfContents(toc);
        }

        setError(null);
      } catch (err) {
        console.error("获取文章失败:", err);
        setError("获取文章失败，请稍后重试");
      } finally {
        setLoading(false);
      }
    };

    fetchArticle();
  }, [id, navigate]);

  // 点击目录项滚动到对应章节
  const scrollToSection = (id: string) => {
    const element = document.getElementById(id);
    if (element) {
      const headerHeight = 80; // Header 高度
      const elementPosition = element.offsetTop - headerHeight;
      window.scrollTo({
        top: elementPosition,
        behavior: "smooth",
      });
    }
  };

  // 加载状态
  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">加载中...</p>
        </div>
      </div>
    );
  }

  // 错误状态
  if (error || !article) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">⚠️</div>
          <h2 className="text-2xl font-bold text-gray-800 mb-2">
            文章加载失败
          </h2>
          <p className="text-gray-600 mb-4">{error || "文章不存在"}</p>
          <button
            onClick={() => window.history.back()}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            返回上一页
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      <Header />

      <ArticleHero
        title={article.title}
        author={article.author?.username || "未知作者"}
        publishDate={
          article.published_at
            ? new Date(article.published_at).toLocaleDateString("zh-CN")
            : new Date(article.created_at).toLocaleDateString("zh-CN")
        }
        tags={article.tags?.map((tag) => tag.name) || []}
        backgroundColor={randomBackground}
      />

      <main id="main-content" tabIndex={-1} className="focus:outline-none">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 md:px-8 py-8 md:py-12">
          <div className="flex flex-col lg:flex-row gap-6 lg:gap-8">
            <ArticleContent ref={articleRef} content={article.content} />

            {/* 目录在移动端隐藏，大屏显示 */}
            <div className="hidden lg:block">
              <TableOfContents
                tableOfContents={tableOfContents}
                onSectionClick={scrollToSection}
              />
            </div>
          </div>
        </div>

        {/* 评论区分隔线 */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 md:px-8">
          <div className="border-t border-gray-200 my-8 md:my-12"></div>
        </div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 md:px-8 pb-8 md:pb-12">
          <CommentSection articleId={id || ""} />
        </div>
      </main>
    </div>
  );
};

export default Article;
