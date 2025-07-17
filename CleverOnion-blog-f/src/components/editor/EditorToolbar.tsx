import React from 'react';
import { FiSave, FiSend, FiImage, FiArrowLeft } from 'react-icons/fi';

interface EditorToolbarProps {
  title: string;
  onTitleChange: (title: string) => void;
  onBack: () => void;
  onSaveDraft: () => void;
  onPublish: () => void;
  onImageUpload: () => void;
  isUploading: boolean;
}

const EditorToolbar: React.FC<EditorToolbarProps> = ({
  title,
  onTitleChange,
  onBack,
  onSaveDraft,
  onPublish,
  onImageUpload,
  isUploading
}) => {
  return (
    <div className="flex items-center justify-between px-6 py-3 border-b border-gray-200 bg-white flex-shrink-0">
      <div className="flex items-center space-x-4">
        <button
          onClick={onBack}
          className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
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
        <button
          onClick={onImageUpload}
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
          onClick={onSaveDraft}
          className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
        >
          <FiSave className="w-4 h-4 mr-2" />
          保存草稿
        </button>
        
        <button
          onClick={onPublish}
          className="flex items-center px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors"
        >
          <FiSend className="w-4 h-4 mr-2" />
          发布文章
        </button>
      </div>
    </div>
  );
};

export default EditorToolbar;