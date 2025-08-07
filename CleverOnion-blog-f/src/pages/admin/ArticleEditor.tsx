import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import EditorToolbar from '../../components/editor/EditorToolbar';
import EditorContent from '../../components/editor/EditorContent';
import ArticleSettingsPanel from '../../components/editor/ArticleSettingsPanel';

interface Article {
  id?: number;
  title: string;
  content: string;
  categoryId: string;
  tags: string[];
  status: 'draft' | 'published';
}

const ArticleEditor = () => {
  const { articleId } = useParams();
  const navigate = useNavigate();
  const isEdit = !!articleId;
  const [article, setArticle] = useState<Article>({
    title: '',
    content: '',
    categoryId: '',
    tags: [],
    status: 'draft'
  });
  
  const categories = [
    { id: '1', name: '前端开发' },
    { id: '2', name: '后端开发' },
    { id: '3', name: '数据库' },
    { id: '4', name: 'DevOps' },
    { id: '5', name: '人工智能' },
    { id: '6', name: '移动开发' }
  ];
  
  const handleSaveDraft = async () => {
    // TODO: 实现保存草稿功能
    console.log('保存草稿:', article);
  };
  
  const handlePublish = async () => {
    // TODO: 实现发布功能
    console.log('发布文章:', { ...article, status: 'published' });
  };
  

  
  const handleAddTag = (tag: string) => {
    setArticle(prev => ({
      ...prev,
      tags: [...prev.tags, tag]
    }));
  };
  
  const handleRemoveTag = (tagToRemove: string) => {
    setArticle(prev => ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }));
  };
  

  
  return (
    <div className="fixed inset-0 bg-white flex flex-col">
      <EditorToolbar
        title={article.title}
        onTitleChange={(title) => setArticle(prev => ({ ...prev, title }))}
        onBack={() => navigate('/admin/articles')}
        onSaveDraft={handleSaveDraft}
        onPublish={handlePublish}
      />
      
      <div className="flex-1 flex overflow-hidden">
        <EditorContent
          content={article.content}
          onContentChange={(content) => setArticle(prev => ({ ...prev, content }))}
        />
        
        <ArticleSettingsPanel
          status={article.status}
          categoryId={article.categoryId}
          tags={article.tags}
          categories={categories}
          onStatusChange={(status) => setArticle(prev => ({ ...prev, status }))}
          onCategoryChange={(categoryId) => setArticle(prev => ({ ...prev, categoryId }))}
          onAddTag={handleAddTag}
          onRemoveTag={handleRemoveTag}
        />
      </div>
      

    </div>
  );
};

export default ArticleEditor;