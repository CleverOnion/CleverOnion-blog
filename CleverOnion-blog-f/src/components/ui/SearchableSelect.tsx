import React, { useState, useRef, useEffect, useCallback } from 'react';
import { FiChevronDown, FiCheck, FiSearch, FiX, FiPlus } from 'react-icons/fi';

interface Option {
  value: string;
  label: string;
  disabled?: boolean;
}

interface SearchableSelectProps {
  // 基础属性
  value?: string | string[];
  onChange: (value: string | string[]) => void;
  options: Option[];
  placeholder?: string;
  searchPlaceholder?: string;
  className?: string;
  disabled?: boolean;
  
  // 搜索相关
  searchable?: boolean;
  onSearch?: (query: string) => void;
  loading?: boolean;
  
  // 多选模式
  multiple?: boolean;
  maxSelections?: number;
  
  // 创建新选项
  allowCreate?: boolean;
  onCreate?: (value: string) => void;
  createText?: string;
  
  // 样式定制
  size?: 'sm' | 'md' | 'lg';
  variant?: 'default' | 'minimal';
  
  // 回调函数
  onFocus?: () => void;
  onBlur?: () => void;
}

const SearchableSelect: React.FC<SearchableSelectProps> = ({
  value,
  onChange,
  options,
  placeholder = '请选择',
  searchPlaceholder = '搜索...',
  className = '',
  disabled = false,
  searchable = true,
  onSearch,
  loading = false,
  multiple = false,
  maxSelections,
  allowCreate = false,
  onCreate,
  createText = '创建',
  size = 'md',
  variant = 'default',
  onFocus,
  onBlur
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [focusedIndex, setFocusedIndex] = useState(-1);
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  
  const containerRef = useRef<HTMLDivElement>(null);
  const searchInputRef = useRef<HTMLInputElement>(null);
  const optionsRef = useRef<HTMLDivElement>(null);
  
  // 处理值的类型转换
  const selectedValues = multiple 
    ? (Array.isArray(value) ? value : value ? [value] : [])
    : (Array.isArray(value) ? value[0] || '' : value || '');
    
  const selectedOptions = multiple
    ? options.filter(option => (selectedValues as string[]).includes(option.value))
    : options.find(option => option.value === selectedValues);

  // 过滤选项
  const filteredOptions = options.filter(option => 
    option.label.toLowerCase().includes(searchQuery.toLowerCase()) ||
    option.value.toLowerCase().includes(searchQuery.toLowerCase())
  );
  
  // 是否显示创建选项
  const showCreateOption = allowCreate && 
    searchQuery.trim() && 
    !filteredOptions.some(option => 
      option.label.toLowerCase() === searchQuery.toLowerCase() ||
      option.value.toLowerCase() === searchQuery.toLowerCase()
    );

  // 尺寸样式
  const sizeClasses = {
    sm: 'px-3 py-2 text-sm',
    md: 'px-4 py-3.5 text-sm',
    lg: 'px-5 py-4 text-base'
  };

  // 点击外部关闭
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
        setFocusedIndex(-1);
        setSearchQuery('');
        onBlur?.();
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [onBlur]);

  // 搜索处理
  useEffect(() => {
    if (onSearch && searchQuery) {
      const timeoutId = setTimeout(() => {
        onSearch(searchQuery);
      }, 300);
      return () => clearTimeout(timeoutId);
    }
  }, [searchQuery, onSearch]);

  // 键盘导航
  const handleKeyDown = useCallback((event: React.KeyboardEvent) => {
    if (disabled) return;

    const totalOptions = filteredOptions.length + (showCreateOption ? 1 : 0);

    switch (event.key) {
      case 'Enter':
        event.preventDefault();
        if (isOpen && focusedIndex >= 0) {
          if (focusedIndex < filteredOptions.length) {
            handleOptionSelect(filteredOptions[focusedIndex].value);
          } else if (showCreateOption) {
            handleCreate();
          }
        } else if (!isOpen) {
          setIsOpen(true);
          onFocus?.();
        }
        break;
      case 'Escape':
        setIsOpen(false);
        setFocusedIndex(-1);
        setSearchQuery('');
        onBlur?.();
        break;
      case 'ArrowDown':
        event.preventDefault();
        if (!isOpen) {
          setIsOpen(true);
          onFocus?.();
        } else {
          setFocusedIndex(prev => 
            prev < totalOptions - 1 ? prev + 1 : 0
          );
        }
        break;
      case 'ArrowUp':
        event.preventDefault();
        if (isOpen) {
          setFocusedIndex(prev => 
            prev > 0 ? prev - 1 : totalOptions - 1
          );
        }
        break;
      case 'Backspace':
        if (multiple && !searchQuery && (selectedValues as string[]).length > 0) {
          event.preventDefault();
          const newValues = [...(selectedValues as string[])];
          newValues.pop();
          onChange(newValues);
        }
        break;
    }
  }, [disabled, isOpen, focusedIndex, filteredOptions, showCreateOption, searchQuery, selectedValues, multiple, onChange, onFocus, onBlur]);

  // 选择选项
  const handleOptionSelect = (optionValue: string) => {
    if (multiple) {
      const currentValues = selectedValues as string[];
      const newValues = currentValues.includes(optionValue)
        ? currentValues.filter(v => v !== optionValue)
        : maxSelections && currentValues.length >= maxSelections
          ? currentValues
          : [...currentValues, optionValue];
      onChange(newValues);
    } else {
      onChange(optionValue);
      setIsOpen(false);
      setSearchQuery('');
    }
    setFocusedIndex(-1);
  };

  // 创建新选项
  const handleCreate = () => {
    if (onCreate && searchQuery.trim()) {
      onCreate(searchQuery.trim());
      setSearchQuery('');
      setFocusedIndex(-1);
    }
  };

  // 移除选中项
  const handleRemoveSelection = (valueToRemove: string, event: React.MouseEvent) => {
    event.stopPropagation();
    if (multiple) {
      const newValues = (selectedValues as string[]).filter(v => v !== valueToRemove);
      onChange(newValues);
    }
  };

  // 打开下拉框
  const handleOpen = () => {
    if (disabled) return;
    setIsOpen(true);
    onFocus?.();
    if (searchable && searchInputRef.current) {
      setTimeout(() => searchInputRef.current?.focus(), 100);
    }
  };

  return (
    <div 
      ref={containerRef}
      className={`relative ${className}`}
    >
      {/* 主输入区域 */}
      <div
        onClick={handleOpen}
        onKeyDown={handleKeyDown}
        tabIndex={disabled ? -1 : 0}
        className={`
          w-full ${sizeClasses[size]} text-left bg-white border border-gray-200 rounded-lg
          transition-all duration-200 ease-out cursor-pointer
          hover:border-gray-300 hover:shadow-sm
          focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20
          disabled:bg-gray-50 disabled:text-gray-400 disabled:cursor-not-allowed disabled:border-gray-200
          ${isOpen ? 'border-blue-500 ring-2 ring-blue-500/20' : ''}
          ${disabled ? 'opacity-60' : ''}
          ${variant === 'minimal' ? 'border-0 shadow-none bg-gray-50' : ''}
        `}
        role="combobox"
        aria-expanded={isOpen}
        aria-haspopup="listbox"
      >
        <div className="flex items-center justify-between">
          <div className="flex-1 flex items-center gap-2 min-w-0">
            {/* 多选标签显示 */}
            {multiple && (selectedValues as string[]).length > 0 ? (
              <div className="flex flex-wrap gap-1.5 flex-1">
                {(selectedValues as string[]).map(val => {
                  const option = options.find(opt => opt.value === val);
                  return (
                    <span
                      key={val}
                      className="inline-flex items-center gap-1 px-2.5 py-1 bg-blue-50 text-blue-700 text-xs font-medium rounded-lg border border-blue-200/50"
                    >
                      <span className="truncate max-w-24">{option?.label || val}</span>
                      <button
                        type="button"
                        onClick={(e) => handleRemoveSelection(val, e)}
                        className="flex-shrink-0 hover:bg-blue-100 rounded p-0.5 transition-colors"
                      >
                        <FiX className="w-3 h-3" />
                      </button>
                    </span>
                  );
                })}
              </div>
            ) : (
              /* 单选或空状态显示 */
              <span className={`truncate ${
                (!multiple && selectedOptions) || (multiple && (selectedValues as string[]).length > 0)
                  ? 'text-gray-900' 
                  : 'text-gray-500'
              }`}>
                {!multiple && selectedOptions 
                  ? (selectedOptions as Option).label
                  : multiple && (selectedValues as string[]).length > 0
                    ? `已选择 ${(selectedValues as string[]).length} 项`
                    : placeholder
                }
              </span>
            )}
          </div>
          
          {/* 右侧图标 */}
          <div className="flex items-center gap-2 flex-shrink-0">
            {loading && (
              <div className="w-4 h-4 border-2 border-blue-500/30 border-t-blue-500 rounded-full animate-spin" />
            )}
            <FiChevronDown 
              className={`w-4 h-4 text-gray-400 transition-transform duration-200 pointer-events-none ${
                isOpen ? 'rotate-180' : ''
              }`}
            />
          </div>
        </div>
      </div>

      {/* 下拉选项 */}
      {isOpen && (
        <div className="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg overflow-hidden animate-in slide-in-from-top-2 duration-200">
          {/* 搜索框 */}
          {searchable && (
            <div className="p-3 border-b border-gray-100">
              <div className="relative">
                <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                  ref={searchInputRef}
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  onFocus={() => setIsSearchFocused(true)}
                  onBlur={() => setIsSearchFocused(false)}
                  onKeyDown={handleKeyDown}
                  placeholder={searchPlaceholder}
                  className={`
                    w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-lg
                    text-sm placeholder-gray-400 transition-all duration-200
                    focus:outline-none focus:bg-white focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20
                    ${isSearchFocused ? 'bg-white border-blue-500 ring-2 ring-blue-500/20' : ''}
                  `}
                />
              </div>
            </div>
          )}
          
          {/* 选项列表 */}
          <div 
            ref={optionsRef}
            className="max-h-60 overflow-y-auto py-1"
            role="listbox"
          >
            {filteredOptions.length === 0 && !showCreateOption ? (
              <div className="px-4 py-8 text-center text-gray-400 text-sm">
                {searchQuery ? '未找到匹配项' : '暂无选项'}
              </div>
            ) : (
              <>
                {filteredOptions.map((option, index) => {
                  const isSelected = multiple 
                    ? (selectedValues as string[]).includes(option.value)
                    : selectedValues === option.value;
                  const isFocused = index === focusedIndex;
                  
                  return (
                    <div
                      key={option.value}
                      onClick={() => !option.disabled && handleOptionSelect(option.value)}
                      className={`
                        px-4 py-2.5 cursor-pointer transition-colors duration-150 select-none
                        flex items-center justify-between
                        ${option.disabled ? 'opacity-50 cursor-not-allowed' : ''}
                        ${isFocused ? 'bg-gray-50' : ''}
                        ${isSelected ? 'bg-blue-50 text-blue-600' : 'text-gray-700'}
                        ${!option.disabled ? 'hover:bg-gray-50' : ''}
                      `}
                      role="option"
                      aria-selected={isSelected}
                      aria-disabled={option.disabled}
                    >
                      <span className={`text-sm ${
                        isSelected ? 'font-medium text-blue-600' : 'text-gray-700'
                      }`}>
                        {option.label}
                      </span>
                      {isSelected && (
                        <FiCheck className="w-4 h-4 text-blue-500" />
                      )}
                    </div>
                  );
                })}
                
                {/* 创建新选项 */}
                {showCreateOption && (
                  <div
                    onClick={handleCreate}
                    className={`
                      px-4 py-2.5 cursor-pointer transition-colors duration-150 select-none
                      flex items-center gap-3 border-t border-gray-100 mt-1
                      ${focusedIndex === filteredOptions.length ? 'bg-green-50' : ''}
                      hover:bg-green-50
                    `}
                  >
                    <FiPlus className="w-4 h-4 text-green-500" />
                    <span className="text-sm font-medium text-green-600">
                      {createText} "{searchQuery}"
                    </span>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default SearchableSelect;