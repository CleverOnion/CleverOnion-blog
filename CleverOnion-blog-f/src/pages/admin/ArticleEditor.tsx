import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import EditorToolbar from '../../components/editor/EditorToolbar';
import EditorContent from '../../components/editor/EditorContent';
import ArticleSettingsPanel from '../../components/editor/ArticleSettingsPanel';
import { articleApi, type PublishArticleRequest } from '../../api/articles';
import categoryApi, { type Category } from '../../api/categories';
import { useLoading } from '../../contexts/LoadingContext';
import { useToast } from '../../components/ui/Toast';

interface Article {
  id?: string;
  title: string;
  content: string;
  summary?: string;
  category_id: number | null;
  tag_names: string[];
  tag_ids: number[];
  status: 'DRAFT' | 'PUBLISHED';
}

const ArticleEditor = () => {
  const { articleId } = useParams();
  const navigate = useNavigate();
  const { setLoading } = useLoading();
  const toast = useToast();
  const isEdit = !!articleId;
  const [article, setArticle] = useState<Article>({
    title: '',
    content: '',
    summary: '',
    category_id: null,
    tag_names: [],
    tag_ids: [],
    status: 'DRAFT'
  });
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLocalLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  
  // 加载分类数据
  const loadCategories = async () => {
    try {
      setLocalLoading(true);
      const response = await categoryApi.getAllCategories();
      setCategories(response.categories || []);
    } catch (error) {
      console.error('加载分类失败:', error);
      setCategories([]); // 设置为空数组以防止undefined错误
    } finally {
      setLocalLoading(false);
    }
  };

  // 加载文章数据（编辑模式）
  const loadArticle = async (id: string) => {
    try {
      setLoading(true, '加载文章数据...');
      const articleData = await articleApi.getArticleById(id);
      setArticle({
        id: articleData.id,
        title: articleData.title,
        content: articleData.content,
        summary: articleData.summary,
        category_id: articleData.category?.id || null,
        tag_names: articleData.tags?.map((tag: any) => tag.name) || [],
        tag_ids: articleData.tags?.map((tag: any) => tag.id) || [],
        status: articleData.status
      });
    } catch (error) {
      console.error('加载文章失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveDraft = async () => {
    if (!article.title.trim()) {
      toast.warning('请输入文章标题');
      return;
    }
    
    if (!article.content.trim()) {
      toast.warning('请输入文章内容');
      return;
    }

    try {
      setSaving(true);
      const articleData = {
        title: article.title,
        content: article.content,
        summary: article.summary || '',
        category_id: article.category_id!,
        tag_names: article.tag_names,
        status: 'DRAFT' as const
      };

      if (isEdit && articleId) {
        await articleApi.updateArticle(articleId, articleData);
        toast.success('草稿保存成功！');
      } else {
        const newArticle = await articleApi.createArticle(articleData);
        setArticle(prev => ({ ...prev, id: newArticle.id }));
        // 更新URL为编辑模式
        navigate(`/admin/articles/edit/${newArticle.id}`, { replace: true });
        toast.success('草稿创建成功！');
      }
    } catch (error) {
      console.error('保存草稿失败:', error);
      toast.error('保存失败，请重试');
    } finally {
      setSaving(false);
    }
  };
  
  const handlePublish = async () => {
    if (!article.title.trim()) {
      toast.warning('请输入文章标题');
      return;
    }
    
    if (!article.content.trim()) {
      toast.warning('请输入文章内容');
      return;
    }

    if (!article.category_id) {
      toast.warning('请选择文章分类');
      return;
    }

    try {
      setSaving(true);
      
      if (isEdit && articleId) {
        // 如果是草稿，调用发布接口
        if (article.status === 'DRAFT') {
          await articleApi.publishArticle(articleId);
          toast.success('文章发布成功！');
        } else {
          // 如果是新文章，调用创建并发布接口
          const articleData = {
            title: article.title,
            content: article.content,
            summary: article.summary || '',
            category_id: article.category_id,
            tag_names: article.tag_names,
            status: 'PUBLISHED' as const
          };
          await articleApi.updateArticle(articleId, articleData);
          toast.success('文章发布成功！');
        }
      } else {
        // 新文章直接发布
        const articleData = {
          title: article.title,
          content: article.content,
          summary: article.summary || '',
          category_id: article.category_id,
          tag_names: article.tag_names
        };
        await articleApi.publishArticleDirectly(articleData);
        toast.success('文章发布成功！');
      }
      
      navigate('/admin/articles');
    } catch (error) {
      console.error('发布文章失败:', error);
      toast.error('发布失败，请重试');
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
      setArticle(prev => ({ ...prev, status: 'DRAFT' }));
      toast.success('文章已转为草稿！');
    } catch (error) {
      console.error('转为草稿失败:', error);
      toast.error('操作失败，请重试');
    } finally {
      setSaving(false);
    }
  };
  
  // 更新已发布文章
  const handleUpdate = async () => {
    if (!article.title.trim()) {
      toast.warning('请输入文章标题');
      return;
    }
    
    if (!article.content.trim()) {
      toast.warning('请输入文章内容');
      return;
    }

    if (!article.category_id) {
      toast.warning('请选择文章分类');
      return;
    }
    
    if (!articleId) return;
    
    try {
      setSaving(true);
      const articleData = {
        title: article.title,
        content: article.content,
        summary: article.summary || '',
        category_id: article.category_id,
        tag_names: article.tag_names,
        status: 'PUBLISHED' as const
      };
      
      await articleApi.updateArticle(articleId, articleData);
      toast.success('文章更新成功！');
    } catch (error) {
      console.error('更新文章失败:', error);
      toast.error('更新失败，请重试');
    } finally {
      setSaving(false);
    }
  };
  

  const handleAddTag = (tagName: string) => {
    setArticle(prev => ({
      ...prev,
      tag_names: [...prev.tag_names, tagName]
    }));
  };
  
  const handleRemoveTag = (tagName: string) => {
    setArticle(prev => ({
      ...prev,
      tag_names: prev.tag_names.filter(name => name !== tagName)
    }));
  };

  // 初始化数据
  useEffect(() => {
    loadCategories();
    if (isEdit && articleId) {
      loadArticle(articleId);
    }
  }, [articleId, isEdit]);
  

  
  return (
    <div className="fixed inset-0 bg-white flex flex-col">
      <EditorToolbar
        title={article.title}
        articleStatus={article.status}
        isNewArticle={!isEdit}
        onTitleChange={(title) => setArticle(prev => ({ ...prev, title }))}
        onBack={() => navigate('/admin/articles')}
        onSaveDraft={handleSaveDraft}
        onPublish={handlePublish}
        onUnpublish={handleUnpublish}
        onUpdate={handleUpdate}
      />
      
      <div className="flex-1 flex overflow-hidden w-full">
        <div className="flex-1 w-full">
          <EditorContent
            content={article.content}
            onContentChange={(content) => setArticle(prev => ({ ...prev, content }))}
          />
        </div>
        
        <ArticleSettingsPanel
          categoryId={article.category_id}
          tagNames={article.tag_names}
          categories={categories}
          summary={article.summary}
          loading={loading}
          onCategoryChange={(categoryId) => setArticle(prev => ({ ...prev, category_id: categoryId }))}
          onSummaryChange={(summary) => setArticle(prev => ({ ...prev, summary }))}
          onAddTag={handleAddTag}
          onRemoveTag={handleRemoveTag}
        />
      </div>
      

    </div>
  );
};

export default ArticleEditor;