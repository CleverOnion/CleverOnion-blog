import React, { useState, useEffect, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import EditorToolbar from "../../components/editor/EditorToolbar";
import EditorContent from "../../components/editor/EditorContent";
import ArticleSettingsPanel from "../../components/editor/ArticleSettingsPanel";
import { articleApi } from "../../api/articles";
import categoryApi, { type Category } from "../../api/categories";
import { useLoading } from "../../hooks/useLoading";
import { useToast } from "../../components/ui/Toast";
import { useFormValidation } from "../../hooks/useFormValidation";
import { articleValidationRules } from "../../utils/validation";
import { useUnsavedChanges } from "../../hooks/useUnsavedChanges";
import { useKeyboardShortcuts } from "../../hooks/useKeyboardShortcuts";
import Modal from "../../components/ui/Modal";
import SkipToContent from "../../components/ui/SkipToContent";
import EditorSkeleton from "../../components/editor/EditorSkeleton";

interface Article {
  id?: string;
  title: string;
  content: string;
  summary?: string;
  category_id: number | null;
  tag_names: string[];
  tag_ids: number[];
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  [key: string]: any;
}

const ArticleEditor = () => {
  const { articleId } = useParams();
  const navigate = useNavigate();
  const { setLoading } = useLoading();
  const toast = useToast();
  const isEdit = !!articleId;
  const [article, setArticle] = useState<Article>({
    title: "",
    content: "",
    summary: "",
    category_id: null,
    tag_names: [],
    tag_ids: [],
    status: "DRAFT",
  });
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLocalLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [originalArticle, setOriginalArticle] = useState<Article | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);

  // 表单验证
  const {
    validationState,
    updateFieldValidation,
    markFieldAsTouched,
    validateAllFields,
    focusFirstError,
    registerField,
  } = useFormValidation(article, {
    title: articleValidationRules.title as any,
    content: articleValidationRules.content as any,
    category_id: articleValidationRules.category_id as any,
  });

  // 检测未保存的更改
  const hasUnsavedChanges = useMemo(() => {
    if (!originalArticle) return false;

    return (
      article.title !== originalArticle.title ||
      article.content !== originalArticle.content ||
      article.summary !== originalArticle.summary ||
      article.category_id !== originalArticle.category_id ||
      JSON.stringify(article.tag_names) !==
        JSON.stringify(originalArticle.tag_names)
    );
  }, [article, originalArticle]);

  // 未保存更改警告
  const { blocker, disableBlocking } = useUnsavedChanges(
    hasUnsavedChanges,
    "您有未保存的更改。如果离开此页面，您的更改将会丢失。"
  );

  // 键盘快捷键
  useKeyboardShortcuts([
    {
      key: "s",
      ctrlOrCmd: true,
      handler: () => {
        if (!saving) {
          handleSaveDraft();
        }
      },
      description: "保存草稿",
    },
    {
      key: "Enter",
      ctrlOrCmd: true,
      handler: () => {
        if (!saving) {
          if (article.status === "PUBLISHED") {
            handleUpdate();
          } else {
            handlePublish();
          }
        }
      },
      description: "发布/更新文章",
    },
  ]);

  // 加载分类数据
  const loadCategories = async () => {
    try {
      setLocalLoading(true);
      const response = await categoryApi.getAllCategories();
      setCategories(response.categories || []);
    } catch (error) {
      console.error("加载分类失败:", error);
      setCategories([]); // 设置为空数组以防止undefined错误
      toast.error("加载分类失败，请刷新页面重试");
    } finally {
      setLocalLoading(false);
    }
  };

  // 加载文章数据（编辑模式）
  const loadArticle = async (id: string) => {
    try {
      setLoading(true, "加载文章数据...");
      const articleData = await articleApi.getArticleById(id);
      const loadedArticle = {
        id: articleData.id,
        title: articleData.title,
        content: articleData.content,
        summary: articleData.summary,
        category_id: articleData.category?.id || null,
        tag_names: articleData.tags?.map((tag: any) => tag.name) || [],
        tag_ids: articleData.tags?.map((tag: any) => tag.id) || [],
        status: articleData.status,
      };
      setArticle(loadedArticle);
      // 保存原始数据用于检测更改
      setOriginalArticle(loadedArticle);
    } catch (error) {
      console.error("加载文章失败:", error);
      toast.error("加载文章失败，请返回重试");
    } finally {
      setLoading(false);
    }
  };

  const handleSaveDraft = async () => {
    // 防止在数据还未加载完成时保存
    if (isInitializing || loading) {
      toast.warning("数据正在加载中，请稍后重试");
      return;
    }

    // 验证必填字段
    const isValid = validateAllFields(article);

    if (!isValid) {
      focusFirstError();
      toast.warning("请检查表单错误");
      return;
    }

    try {
      setSaving(true);
      const articleData = {
        title: article.title,
        content: article.content,
        summary: article.summary || "",
        category_id: article.category_id!,
        tag_names: article.tag_names,
        status: "DRAFT" as const,
      };

      // 如果文章已经有ID，更新草稿
      if (articleId) {
        await articleApi.updateArticle(articleId, articleData);
        toast.success("草稿保存成功！");
        // 更新本地状态和原始数据
        setArticle({ ...article, status: "DRAFT" });
        setOriginalArticle({ ...article, status: "DRAFT" });
      } else {
        const newArticle = await articleApi.createArticle(articleData);
        // 使用服务器返回的完整文章数据更新状态，包括status
        const updatedArticle = {
          ...article,
          ...newArticle,
          tag_names: article.tag_names, // 保留本地的tag_names
          status: "DRAFT" as const, // 明确设置为草稿状态
        };
        setArticle(updatedArticle);
        setOriginalArticle(updatedArticle);
        toast.success("草稿创建成功！");

        // 禁用拦截器后再导航，避免触发"未保存的更改"提示
        disableBlocking();
        // 延迟导航，确保状态完全同步
        setTimeout(() => {
          navigate(`/admin/articles/edit/${newArticle.id}`, { replace: true });
        }, 100);
      }
    } catch (error) {
      console.error("保存草稿失败:", error);
      toast.error("保存失败，请重试");
    } finally {
      setSaving(false);
    }
  };

  const handlePublish = async () => {
    // 防止在数据还未加载完成时发布
    if (isInitializing || loading) {
      toast.warning("数据正在加载中，请稍后重试");
      return;
    }

    // 验证所有字段（包括分类）
    const isValid = validateAllFields(article);

    if (!isValid) {
      focusFirstError();
      toast.warning("请检查表单错误");
      return;
    }

    try {
      setSaving(true);

      // 如果文章已经有ID（已保存的草稿或已发布的文章）
      if (articleId) {
        // 如果是草稿，调用发布接口；否则调用更新接口
        if (article.status === "DRAFT") {
          await articleApi.publishArticle(articleId);
          toast.success("文章发布成功！");
          // 更新原始数据，防止触发未保存警告
          setArticle({ ...article, status: "PUBLISHED" });
          setOriginalArticle({ ...article, status: "PUBLISHED" });
        } else {
          // 已发布的文章，更新内容
          const articleData = {
            title: article.title,
            content: article.content,
            summary: article.summary || "",
            category_id: article.category_id!,
            tag_names: article.tag_names,
            status: "PUBLISHED" as const,
          };
          await articleApi.updateArticle(articleId, articleData);
          toast.success("文章更新成功！");
          // 更新原始数据，防止触发未保存警告
          setArticle({ ...article, ...articleData });
          setOriginalArticle({ ...article, ...articleData });
        }
      } else {
        // 全新文章，直接发布
        const articleData = {
          title: article.title,
          content: article.content,
          summary: article.summary || "",
          category_id: article.category_id!,
          tag_names: article.tag_names,
        };
        const publishedArticle = await articleApi.publishArticleDirectly(
          articleData
        );
        toast.success("文章发布成功！");
        // 更新本地状态
        setArticle({ ...article, ...publishedArticle, status: "PUBLISHED" });
        setOriginalArticle({
          ...article,
          ...publishedArticle,
          status: "PUBLISHED",
        });
      }

      // 禁用拦截器后再导航
      disableBlocking();
      // 延迟导航，确保状态完全同步
      setTimeout(() => {
        navigate("/admin/articles");
      }, 100);
    } catch (error) {
      console.error("发布文章失败:", error);
      toast.error("发布失败，请重试");
    } finally {
      setSaving(false);
    }
  };

  // 转为草稿
  const handleUnpublish = async () => {
    if (!articleId) return;

    try {
      setSaving(true);
      await articleApi.unpublishArticle(articleId);
      setArticle((prev) => ({ ...prev, status: "DRAFT" }));
      toast.success("文章已转为草稿！");
      // 更新原始数据，防止触发未保存警告
      setOriginalArticle((prev) =>
        prev ? { ...prev, status: "DRAFT" } : null
      );
    } catch (error) {
      console.error("转为草稿失败:", error);
      toast.error("操作失败，请重试");
    } finally {
      setSaving(false);
    }
  };

  // 更新已发布文章
  const handleUpdate = async () => {
    // 防止在数据还未加载完成时更新
    if (isInitializing || loading) {
      toast.warning("数据正在加载中，请稍后重试");
      return;
    }

    // 验证所有字段
    const isValid = validateAllFields(article);

    if (!isValid) {
      focusFirstError();
      toast.warning("请检查表单错误");
      return;
    }

    if (!articleId) return;

    try {
      setSaving(true);
      const articleData = {
        title: article.title,
        content: article.content,
        summary: article.summary || "",
        category_id: article.category_id!,
        tag_names: article.tag_names,
        status: "PUBLISHED" as const,
      };

      await articleApi.updateArticle(articleId, articleData);
      toast.success("文章更新成功！");
      // 更新原始数据，防止触发未保存警告
      setOriginalArticle({ ...article, ...articleData });
    } catch (error) {
      console.error("更新文章失败:", error);
      toast.error("更新失败，请重试");
    } finally {
      setSaving(false);
    }
  };

  const handleAddTag = (tagName: string) => {
    setArticle((prev) => ({
      ...prev,
      tag_names: [...prev.tag_names, tagName],
    }));
  };

  const handleRemoveTag = (tagName: string) => {
    setArticle((prev) => ({
      ...prev,
      tag_names: prev.tag_names.filter((name) => name !== tagName),
    }));
  };

  // 初始化数据
  useEffect(() => {
    const initialize = async () => {
      setIsInitializing(true);

      // 并行加载分类和文章数据
      if (isEdit && articleId) {
        await Promise.all([loadCategories(), loadArticle(articleId)]);
      } else {
        await loadCategories();
        // 新文章，设置初始原始状态
        setOriginalArticle({
          title: "",
          content: "",
          summary: "",
          category_id: null,
          tag_names: [],
          tag_ids: [],
          status: "DRAFT",
        });
      }

      setIsInitializing(false);
    };

    initialize();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [articleId, isEdit]);

  // 页面加载后自动聚焦到标题输入框
  useEffect(() => {
    // 只在初始化完成后聚焦
    if (!isInitializing) {
      const timer = setTimeout(() => {
        const titleInput = document.querySelector<HTMLInputElement>(
          'input[aria-label="文章标题"]'
        );
        if (titleInput) {
          titleInput.focus();
        }
      }, 200);

      return () => clearTimeout(timer);
    }
  }, [isInitializing]);

  // 处理字段变化和验证
  const handleTitleChange = (title: string) => {
    setArticle((prev) => ({ ...prev, title }));
    updateFieldValidation("title", title, { dirty: true });
  };

  const handleTitleBlur = () => {
    markFieldAsTouched("title");
  };

  const handleTitleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    // 按 Enter 键时聚焦到编辑器
    if (e.key === "Enter" && !e.shiftKey && !e.ctrlKey && !e.metaKey) {
      e.preventDefault();
      // 查找 Milkdown 编辑器并聚焦
      const editorContainer = document.querySelector(".milkdown");
      if (editorContainer) {
        const editableElement = editorContainer.querySelector<HTMLElement>(
          '[contenteditable="true"]'
        );
        if (editableElement) {
          editableElement.focus();
        }
      }
    }
  };

  const handleCategoryChange = (categoryId: number) => {
    setArticle((prev) => ({ ...prev, category_id: categoryId }));
    updateFieldValidation("category_id", categoryId, {
      dirty: true,
      touched: true,
    });
  };

  // 显示骨架屏
  if (isInitializing) {
    return <EditorSkeleton />;
  }

  return (
    <>
      <SkipToContent targetId="editor-main-content" label="跳转到编辑器内容" />
      <div className="fixed inset-0 bg-white flex flex-col">
        <EditorToolbar
          title={article.title}
          articleStatus={article.status as "PUBLISHED" | "DRAFT" | undefined}
          isNewArticle={!isEdit}
          onTitleChange={handleTitleChange}
          onTitleBlur={handleTitleBlur}
          onTitleKeyDown={handleTitleKeyDown}
          onBack={() => navigate("/admin/articles")}
          onSaveDraft={handleSaveDraft}
          onPublish={handlePublish}
          onUnpublish={handleUnpublish}
          onUpdate={handleUpdate}
          titleError={validationState.title?.message}
          showTitleError={
            validationState.title?.isTouched && !validationState.title?.isValid
          }
          registerTitleRef={(ref) => registerField("title", ref)}
          saving={saving}
          hasUnsavedChanges={hasUnsavedChanges}
        />

        <div className="flex-1 flex overflow-hidden w-full">
          <div className="flex-1 w-full">
            <EditorContent
              content={article.content}
              onContentChange={(content) => {
                setArticle((prev) => ({ ...prev, content }));
                updateFieldValidation("content", content, { dirty: true });
              }}
            />
          </div>

          <ArticleSettingsPanel
            categoryId={article.category_id}
            tagNames={article.tag_names}
            categories={categories}
            summary={article.summary}
            loading={loading}
            onCategoryChange={handleCategoryChange}
            onSummaryChange={(summary) =>
              setArticle((prev) => ({ ...prev, summary }))
            }
            onAddTag={handleAddTag}
            onRemoveTag={handleRemoveTag}
            categoryError={validationState.category_id?.message}
            showCategoryError={
              validationState.category_id?.isTouched &&
              !validationState.category_id?.isValid
            }
            registerCategoryRef={(ref) => registerField("category_id", ref)}
          />
        </div>
      </div>

      {/* 未保存更改确认对话框 */}
      <Modal
        isOpen={blocker.state === "blocked"}
        onClose={() => blocker.reset?.()}
        title="未保存的更改"
        size="sm"
        closeOnOverlayClick={false}
      >
        <div className="space-y-4">
          <p className="text-gray-600">
            您有未保存的更改。如果离开此页面，您的更改将会丢失。
          </p>
          <div className="flex justify-end space-x-3">
            <button
              onClick={() => blocker.reset?.()}
              className="px-4 py-2 text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors cursor-pointer"
              autoFocus
            >
              继续编辑
            </button>
            <button
              onClick={() => blocker.proceed?.()}
              className="px-4 py-2 text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors cursor-pointer"
            >
              离开
            </button>
          </div>
        </div>
      </Modal>
    </>
  );
};

export default ArticleEditor;
