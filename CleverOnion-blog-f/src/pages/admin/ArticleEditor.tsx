import React, { useState, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import { 
  FiSave, 
  FiSend, 
  FiImage, 
  FiX, 
  FiPlus,
  FiArrowLeft
} from 'react-icons/fi';

interface Article {
  id?: number;
  title: string;
  content: string;
  categoryId: string;
  tags: string[];
  status: 'draft' | 'published';
}

type EditorMode = 'edit' | 'preview' | 'split';

const ArticleEditor = () => {
  const { articleId } = useParams();
  const navigate = useNavigate();
  const isEdit = !!articleId;
  const fileInputRef = useRef<HTMLInputElement>(null);
  const contentTextareaRef = useRef<HTMLTextAreaElement>(null);
  
  const [article, setArticle] = useState<Article>({
    title: '',
    content: '',
    categoryId: '',
    tags: [],
    status: 'draft'
  });
  
  const [editorMode, setEditorMode] = useState<EditorMode>('edit');
  const [tagInput, setTagInput] = useState('');
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
      const textarea = contentTextareaRef.current;
      if (textarea) {
        const start = textarea.selectionStart;
        const end = textarea.selectionEnd;
        const imageMarkdown = `![${file.name}](${imageUrl})`;
        const newContent = article.content.substring(0, start) + imageMarkdown + article.content.substring(end);
        setArticle(prev => ({ ...prev, content: newContent }));
        
        // 设置光标位置
        setTimeout(() => {
          textarea.focus();
          textarea.setSelectionRange(start + imageMarkdown.length, start + imageMarkdown.length);
        }, 0);
      }
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
  
  const handleAddTag = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && tagInput.trim()) {
      e.preventDefault();
      if (!article.tags.includes(tagInput.trim())) {
        setArticle(prev => ({
          ...prev,
          tags: [...prev.tags, tagInput.trim()]
        }));
      }
      setTagInput('');
    }
  };
  
  const handleRemoveTag = (tagToRemove: string) => {
    setArticle(prev => ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }));
  };
  
  const renderEditor = () => (
    <textarea
      ref={contentTextareaRef}
      value={article.content}
      onChange={(e) => setArticle(prev => ({ ...prev, content: e.target.value }))}
      className="w-full h-full p-6 border-0 resize-none focus:outline-none font-mono text-base leading-relaxed bg-white"
      placeholder="# 开始写作...\n\n在这里输入你的 Markdown 内容。\n\n## 提示\n- 使用 **粗体** 和 *斜体*\n- 添加 [链接](https://example.com)\n- 插入代码块：\n\n```javascript\nconsole.log('Hello World');\n```"
    />
  );
  
  const renderPreview = () => (
    <div className="w-full h-full overflow-y-auto p-6 bg-white">
      <div className="prose prose-lg max-w-none">
        <ReactMarkdown>{article.content || '# 预览\n\n开始写作以查看预览效果...'}</ReactMarkdown>
      </div>
    </div>
  );
  
  const renderSplitView = () => (
    <div className="w-full h-full flex">
      <div className="w-1/2 border-r border-gray-200">
        {renderEditor()}
      </div>
      <div className="w-1/2">
        {renderPreview()}
      </div>
    </div>
  );
  
  const renderContent = () => {
    switch (editorMode) {
      case 'preview':
        return renderPreview();
      case 'split':
        return renderSplitView();
      default:
        return renderEditor();
    }
  };
  
  return (
    <div className="fixed inset-0 bg-white flex flex-col">
      {/* 顶部工具栏 */}
      <div className="flex items-center justify-between px-6 py-3 border-b border-gray-200 bg-white flex-shrink-0">
        <div className="flex items-center space-x-4">
          <button
            onClick={() => navigate('/admin/articles')}
            className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
            title="返回文章列表"
          >
            <FiArrowLeft className="w-5 h-5" />
          </button>
          
          <input
            type="text"
            value={article.title}
            onChange={(e) => setArticle(prev => ({ ...prev, title: e.target.value }))}
            className="text-lg font-semibold text-gray-900 placeholder-gray-400 border-0 focus:outline-none bg-transparent"
            placeholder="无标题文档"
          />
        </div>
        
        <div className="flex items-center space-x-3">
          {/* 编辑器模式切换 */}
          <div className="flex items-center bg-gray-100 rounded-lg p-1">
            <button
              onClick={() => setEditorMode('edit')}
              className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
                editorMode === 'edit'
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              编辑
            </button>
            <button
              onClick={() => setEditorMode('preview')}
              className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
                editorMode === 'preview'
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              预览
            </button>
            <button
              onClick={() => setEditorMode('split')}
              className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
                editorMode === 'split'
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              分屏
            </button>
          </div>
          
          {/* 工具按钮 */}
          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={isUploading}
            className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50"
            title="插入图片"
          >
            {isUploading ? (
              <div className="w-4 h-4 border-2 border-gray-300 border-t-blue-600 rounded-full animate-spin mr-2" />
            ) : (
              <FiImage className="w-4 h-4 mr-2" />
            )}
            图片
          </button>
          
          <div className="h-6 w-px bg-gray-300" />
          
          <button
            onClick={handleSaveDraft}
            className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <FiSave className="w-4 h-4 mr-2" />
            保存草稿
          </button>
          
          <button
            onClick={handlePublish}
            className="flex items-center px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors"
          >
            <FiSend className="w-4 h-4 mr-2" />
            发布文章
          </button>
        </div>
      </div>
      
      {/* 主编辑区域 */}
      <div className="flex-1 flex overflow-hidden">
        {/* 编辑器内容区域 */}
        <div className="flex-1 overflow-hidden">
          {renderContent()}
        </div>
        
        {/* 右侧设置面板 - 仅在编辑模式下显示 */}
        {editorMode === 'edit' && (
          <div className="w-80 border-l border-gray-200 bg-gray-50 overflow-y-auto flex-shrink-0">
            <div className="p-4 space-y-4">
              {/* 发布设置 */}
              <div className="bg-white rounded-lg p-4 shadow-sm">
                <h3 className="text-sm font-semibold text-gray-900 mb-3">发布设置</h3>
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    状态
                  </label>
                  <select
                    value={article.status}
                    onChange={(e) => setArticle(prev => ({ ...prev, status: e.target.value as 'draft' | 'published' }))}
                    className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="draft">草稿</option>
                    <option value="published">已发布</option>
                  </select>
                </div>
              </div>
              
              {/* 分类选择 */}
              <div className="bg-white rounded-lg p-4 shadow-sm">
                <h3 className="text-sm font-semibold text-gray-900 mb-3">分类</h3>
                <select
                  value={article.categoryId}
                  onChange={(e) => setArticle(prev => ({ ...prev, categoryId: e.target.value }))}
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">选择分类</option>
                  {categories.map(category => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>
              
              {/* 标签管理 */}
              <div className="bg-white rounded-lg p-4 shadow-sm">
                <h3 className="text-sm font-semibold text-gray-900 mb-3">标签</h3>
                <div className="space-y-3">
                  <div className="flex">
                    <input
                      type="text"
                      value={tagInput}
                      onChange={(e) => setTagInput(e.target.value)}
                      onKeyDown={handleAddTag}
                      className="flex-1 px-3 py-2 text-sm border border-gray-300 rounded-l-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="输入标签名称"
                    />
                    <button
                      onClick={() => {
                        if (tagInput.trim() && !article.tags.includes(tagInput.trim())) {
                          setArticle(prev => ({
                            ...prev,
                            tags: [...prev.tags, tagInput.trim()]
                          }));
                          setTagInput('');
                        }
                      }}
                      className="px-3 py-2 bg-blue-600 text-white rounded-r-md hover:bg-blue-700 transition-colors"
                    >
                      <FiPlus className="w-4 h-4" />
                    </button>
                  </div>
                  
                  {article.tags.length > 0 && (
                    <div className="flex flex-wrap gap-2">
                      {article.tags.map((tag, index) => (
                        <span
                          key={index}
                          className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                        >
                          {tag}
                          <button
                            onClick={() => handleRemoveTag(tag)}
                            className="ml-1.5 text-blue-600 hover:text-blue-800"
                          >
                            <FiX className="w-3 h-3" />
                          </button>
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
      
      {/* 隐藏的文件输入 */}
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