import React, { useState, useRef, useCallback } from 'react';
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
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  const [article, setArticle] = useState<Article>({
    title: '',
    content: '',
    categoryId: '',
    tags: [],
    status: 'draft'
  });

  const [isUploading, setIsUploading] = useState(false);
  
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
  
  const handleImageUpload = useCallback(async (file: File) => {
    setIsUploading(true);
    try {
      // TODO: 实现图片上传到后端
      const formData = new FormData();
      formData.append('image', file);
      
      // 模拟上传
      await new Promise(resolve => setTimeout(resolve, 1000));
      const imageUrl = URL.createObjectURL(file);
      
      // 插入图片到编辑器
      const imageMarkdown = `![${file.name}](${imageUrl})`;
      const newContent = article.content + '\n\n' + imageMarkdown;
      setArticle(prev => ({ ...prev, content: newContent }));
    } catch (error) {
      console.error('图片上传失败:', error);
    } finally {
      setIsUploading(false);
    }
  }, [article.content]);
  
  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file && file.type.startsWith('image/')) {
      handleImageUpload(file);
    }
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
        onImageUpload={() => fileInputRef.current?.click()}
        isUploading={isUploading}
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
      
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        onChange={handleFileSelect}
        className="hidden"
      />
    </div>
  );
};

export default ArticleEditor;