import React from 'react';
import TagManager from './TagManager';
import Select from '../ui/Select';

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
            <Select
              value={status}
              onChange={(value) => onStatusChange(value as 'draft' | 'published')}
              options={[
                { value: 'draft', label: '草稿' },
                { value: 'published', label: '已发布' }
              ]}
              placeholder="选择状态"
            />
          </div>
        </div>
        
        {/* 分类选择 */}
        <div className="bg-white rounded-lg p-4 shadow-sm">
          <h3 className="text-sm font-semibold text-gray-900 mb-3">分类</h3>
          <Select
            value={categoryId}
            onChange={onCategoryChange}
            options={[
              { value: '', label: '选择分类' },
              ...categories.map(category => ({
                value: category.id,
                label: category.name
              }))
            ]}
            placeholder="选择分类"
          />
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