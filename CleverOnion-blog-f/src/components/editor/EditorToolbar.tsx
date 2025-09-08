import React from 'react';
import { FiSave, FiSend, FiArrowLeft, FiEdit3, FiArchive } from 'react-icons/fi';

interface EditorToolbarProps {
  title: string;
  articleStatus?: 'DRAFT' | 'PUBLISHED';
  isNewArticle: boolean;
  onTitleChange: (title: string) => void;
  onBack: () => void;
  onSaveDraft: () => void;
  onPublish: () => void;
  onUnpublish?: () => void;
  onUpdate?: () => void;
}

const EditorToolbar: React.FC<EditorToolbarProps> = ({
  title,
  articleStatus,
  isNewArticle,
  onTitleChange,
  onBack,
  onSaveDraft,
  onPublish,
  onUnpublish,
  onUpdate
}) => {
  
  // 根据文章状态渲染不同的按钮
  const renderActionButtons = () => {
    if (articleStatus === 'PUBLISHED') {
      // 已发布文章：显示"转为草稿"和"更新文章"按钮
      return (
        <>
          <button
            onClick={onUnpublish}
            className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
          >
            <FiArchive className="w-4 h-4 mr-2" />
            转为草稿
          </button>
          
          <button
            onClick={onUpdate}
            className="flex items-center px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors cursor-pointer"
          >
            <FiEdit3 className="w-4 h-4 mr-2" />
            更新文章
          </button>
        </>
      );
    } else {
      // 新文章或草稿：显示"保存草稿"和"发布文章"按钮
      return (
        <>
          <button
            onClick={onSaveDraft}
            className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
          >
            <FiSave className="w-4 h-4 mr-2" />
            保存草稿
          </button>
          
          <button
            onClick={onPublish}
            className="flex items-center px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors cursor-pointer"
          >
            <FiSend className="w-4 h-4 mr-2" />
            发布文章
          </button>
        </>
      );
    }
  };
  return (
    <div className="flex items-center justify-between px-6 py-3 border-b border-gray-200 bg-white flex-shrink-0">
      <div className="flex items-center space-x-4">
        <button
          onClick={onBack}
          className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer"
          title="返回文章列表"
        >
          <FiArrowLeft className="w-5 h-5" />
        </button>
        
        <input
          type="text"
          value={title}
          onChange={(e) => onTitleChange(e.target.value)}
          className="text-lg font-semibold text-gray-900 placeholder-gray-400 border-0 focus:outline-none bg-transparent"
          placeholder="无标题文档"
        />
      </div>
      
      <div className="flex items-center space-x-3">
        {renderActionButtons()}
      </div>
    </div>
  );
};

export default EditorToolbar;