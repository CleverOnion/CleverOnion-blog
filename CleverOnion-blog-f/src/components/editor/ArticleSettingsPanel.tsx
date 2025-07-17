import React from 'react';
import TagManager from './TagManager';

interface Category {
  id: string;
  name: string;
}

interface ArticleSettingsPanelProps {
  status: 'draft' | 'published';
  categoryId: string;
  tags: string[];
  categories: Category[];
  onStatusChange: (status: 'draft' | 'published') => void;
  onCategoryChange: (categoryId: string) => void;
  onAddTag: (tag: string) => void;
  onRemoveTag: (tag: string) => void;
}

const ArticleSettingsPanel: React.FC<ArticleSettingsPanelProps> = ({
  status,
  categoryId,
  tags,
  categories,
  onStatusChange,
  onCategoryChange,
  onAddTag,
  onRemoveTag
}) => {
  return (
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
              value={status}
              onChange={(e) => onStatusChange(e.target.value as 'draft' | 'published')}
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
            value={categoryId}
            onChange={(e) => onCategoryChange(e.target.value)}
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
        <TagManager
          tags={tags}
          onAddTag={onAddTag}
          onRemoveTag={onRemoveTag}
        />
      </div>
    </div>
  );
};

export default ArticleSettingsPanel;