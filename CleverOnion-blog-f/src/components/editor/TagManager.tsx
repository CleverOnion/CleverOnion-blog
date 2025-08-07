import React, { useState } from 'react';
import { FiPlus, FiX } from 'react-icons/fi';

interface TagManagerProps {
  tags: string[];
  onAddTag: (tag: string) => void;
  onRemoveTag: (tag: string) => void;
}

const TagManager: React.FC<TagManagerProps> = ({ tags, onAddTag, onRemoveTag }) => {
  const [tagInput, setTagInput] = useState('');

  const handleAddTag = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && tagInput.trim()) {
      e.preventDefault();
      if (!tags.includes(tagInput.trim())) {
        onAddTag(tagInput.trim());
      }
      setTagInput('');
    }
  };

  const handleAddTagClick = () => {
    if (tagInput.trim() && !tags.includes(tagInput.trim())) {
      onAddTag(tagInput.trim());
      setTagInput('');
    }
  };

  return (
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
            onClick={handleAddTagClick}
            className="px-3 py-2 bg-blue-600 text-white rounded-r-md hover:bg-blue-700 transition-colors cursor-pointer"
          >
            <FiPlus className="w-4 h-4" />
          </button>
        </div>
        
        {tags.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {tags.map((tag, index) => (
              <span
                key={index}
                className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
              >
                {tag}
                <button
                  onClick={() => onRemoveTag(tag)}
                  className="ml-1.5 text-blue-600 hover:text-blue-800 cursor-pointer"
                >
                  <FiX className="w-3 h-3" />
                </button>
              </span>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default TagManager;