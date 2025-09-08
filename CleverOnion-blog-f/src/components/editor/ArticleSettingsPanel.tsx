import React from 'react';
import TagManager from './TagManager';
import SearchableSelect from '../ui/SearchableSelect';
import { Loading } from '../ui/Loading';

interface Category {
  id: number;
  name: string;
}

interface ArticleSettingsPanelProps {
  categoryId: number | null;
  tagNames: string[];
  categories: Category[];
  summary?: string;
  loading?: boolean;
  onCategoryChange: (categoryId: number) => void;
  onSummaryChange: (summary: string) => void;
  onAddTag: (tagName: string) => void;
  onRemoveTag: (tagName: string) => void;
}

const ArticleSettingsPanel: React.FC<ArticleSettingsPanelProps> = ({
  categoryId,
  tagNames,
  categories,
  summary: initialSummary = '',
  loading = false,
  onCategoryChange,
  onSummaryChange,
  onAddTag,
  onRemoveTag
}) => {
  const [summary, setSummary] = React.useState(initialSummary);
  
  // 当外部传入的summary发生变化时，更新本地状态
  React.useEffect(() => {
    setSummary(initialSummary);
  }, [initialSummary]);
  return (
    <div className="w-80 border-l border-gray-200 bg-gray-50 overflow-y-auto flex-shrink-0">
      <div className="p-4 space-y-4">

        {/* 分类选择 */}
        <div className="bg-white rounded-lg p-4 shadow-sm">
          <h3 className="text-sm font-semibold text-gray-900 mb-3">分类</h3>
          {loading ? (
            <div className="flex items-center justify-center py-4">
              <Loading size="sm" text="加载分类..." />
            </div>
          ) : (
            <SearchableSelect
              value={categoryId?.toString() || ''}
              onChange={(value) => {
                if (typeof value === 'string' && value) {
                  onCategoryChange(parseInt(value));
                }
              }}
              options={(categories || []).map(category => ({
                value: category.id.toString(),
                label: category.name
              }))}
              placeholder="搜索或选择分类"
              searchPlaceholder="搜索分类..."
              searchable={true}
              size="md"
              className="w-full"
            />
          )}
        </div>
        
        {/* 文章摘要 */}
        <div className="bg-white rounded-lg p-4 shadow-sm">
          <h3 className="text-sm font-semibold text-gray-900 mb-3">文章摘要</h3>
          <textarea
            value={summary}
            onChange={(e) => {
              setSummary(e.target.value);
              onSummaryChange(e.target.value);
            }}
            className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            placeholder="输入文章摘要（可选）"
            rows={3}
            maxLength={500}
          />
          <div className="text-xs text-gray-500 mt-1">
            {summary.length}/500 字符
          </div>
        </div>
        
        {/* 标签管理 */}
        <TagManager
          tagNames={tagNames}
          onAddTag={onAddTag}
          onRemoveTag={onRemoveTag}
        />
      </div>
    </div>
  );
};

export default ArticleSettingsPanel;