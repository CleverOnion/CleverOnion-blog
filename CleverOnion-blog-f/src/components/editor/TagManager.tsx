import React, { useState, useEffect, useRef } from 'react';
import { FiX } from 'react-icons/fi';
import { tagApi } from '../../api/tags';

interface Tag {
  id: number;
  name: string;
}

interface TagManagerProps {
  tagNames: string[];
  onAddTag: (tagName: string) => void;
  onRemoveTag: (tagName: string) => void;
}

const TagManager: React.FC<TagManagerProps> = ({ tagNames, onAddTag, onRemoveTag }) => {
  const [tags, setTags] = useState<Tag[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [inputValue, setInputValue] = useState('');
  const [suggestions, setSuggestions] = useState<Tag[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [selectedSuggestionIndex, setSelectedSuggestionIndex] = useState(-1);
  const inputRef = useRef<HTMLInputElement>(null);
  const suggestionsRef = useRef<HTMLDivElement>(null);

  // 加载所有标签
  useEffect(() => {
    const loadTags = async () => {
      try {
        setIsLoading(true);
        const response = await tagApi.getAllTags();
        setTags(response.tags || []);
      } catch (error) {
        console.error('加载标签失败:', error);
        // 当API失败时，提供一些示例标签用于演示
        setTags([
          { id: 1, name: 'React' },
          { id: 2, name: 'JavaScript' },
          { id: 3, name: 'TypeScript' },
          { id: 4, name: 'Vue' },
          { id: 5, name: 'Angular' },
          { id: 6, name: 'Node.js' },
          { id: 7, name: 'CSS' },
          { id: 8, name: 'HTML' },
          { id: 9, name: '前端开发' },
          { id: 10, name: '后端开发' }
        ]);
      } finally {
        setIsLoading(false);
      }
    };
    loadTags();
  }, []);

  // 处理输入变化
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setInputValue(value);
    
    if (value.trim()) {
      // 过滤匹配的标签
      const filtered = tags.filter(tag => 
        tag.name.toLowerCase().includes(value.toLowerCase()) &&
        !tagNames.includes(tag.name)
      );
      setSuggestions(filtered);
      setShowSuggestions(true);
      setSelectedSuggestionIndex(-1);
    } else {
      setSuggestions([]);
      setShowSuggestions(false);
      setSelectedSuggestionIndex(-1);
    }
  };

  // 处理键盘事件
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      if (selectedSuggestionIndex >= 0 && suggestions[selectedSuggestionIndex]) {
        // 选择当前高亮的建议
        handleSuggestionClick(suggestions[selectedSuggestionIndex]);
      } else {
        // 添加输入的标签
        handleAddTag();
      }
    } else if (e.key === 'Escape') {
      setShowSuggestions(false);
      setSelectedSuggestionIndex(-1);
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      if (showSuggestions && suggestions.length > 0) {
        setSelectedSuggestionIndex(prev => 
          prev < suggestions.length - 1 ? prev + 1 : 0
        );
      }
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      if (showSuggestions && suggestions.length > 0) {
        setSelectedSuggestionIndex(prev => 
          prev > 0 ? prev - 1 : suggestions.length - 1
        );
      }
    }
  };

  // 处理标签添加
  const handleAddTag = () => {
    const tagName = inputValue.trim();
    if (!tagName) return;

    // 检查是否已经添加过该标签
    if (!tagNames.includes(tagName)) {
      onAddTag(tagName);
    }
    
    // 清空输入框
    setInputValue('');
    setSuggestions([]);
    setShowSuggestions(false);
    setSelectedSuggestionIndex(-1);
  };

  // 处理建议点击
  const handleSuggestionClick = (tag: Tag) => {
    if (!tagNames.includes(tag.name)) {
      onAddTag(tag.name);
    }
    setInputValue('');
    setSuggestions([]);
    setShowSuggestions(false);
    setSelectedSuggestionIndex(-1);
  };

  // 点击外部关闭建议
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        inputRef.current &&
        !inputRef.current.contains(event.target as Node) &&
        suggestionsRef.current &&
        !suggestionsRef.current.contains(event.target as Node)
      ) {
         setShowSuggestions(false);
         setSelectedSuggestionIndex(-1);
       }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  return (
    <div className="space-y-4">
      {/* 已选标签显示 */}
      {tagNames.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {tagNames.map(tagName => (
            <span
              key={tagName}
              className="inline-flex items-center gap-2 px-3 py-1.5 bg-blue-50 text-blue-700 text-sm font-medium rounded-lg border border-blue-200/50"
            >
              {tagName}
              <button
                type="button"
                onClick={() => onRemoveTag(tagName)}
                className="hover:bg-blue-100 rounded p-0.5 transition-colors"
              >
                <FiX className="w-3.5 h-3.5" />
              </button>
            </span>
          ))}
        </div>
      )}

      {/* 标签输入框 */}
      <div className="relative">
        <input
          ref={inputRef}
          type="text"
          value={inputValue}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
          placeholder="输入标签名称，按回车添加"
          className="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
          disabled={isLoading}
        />
        
        {/* 建议下拉列表 */}
        {showSuggestions && suggestions.length > 0 && (
          <div
             ref={suggestionsRef}
             className="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg max-h-48 overflow-y-auto"
           >
             {suggestions.map((tag, index) => (
               <button
                 key={tag.id}
                 type="button"
                 onClick={() => handleSuggestionClick(tag)}
                 className={`w-full px-4 py-2.5 text-left focus:outline-none transition-colors duration-150 first:rounded-t-lg last:rounded-b-lg ${
                   index === selectedSuggestionIndex
                     ? 'bg-blue-50 text-blue-700'
                     : 'hover:bg-gray-50 text-gray-900'
                 }`}
               >
                 <span>{tag.name}</span>
               </button>
             ))}
           </div>
        )}
        
        {/* 加载状态 */}
        {isLoading && (
          <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
            <div className="animate-spin rounded-full h-4 w-4 border-2 border-blue-500 border-t-transparent"></div>
          </div>
        )}
      </div>
    </div>
  );
};

export default TagManager;