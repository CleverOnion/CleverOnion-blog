import React, { useState, useRef, useEffect } from 'react';
import { FiChevronDown, FiCheck } from 'react-icons/fi';

interface Option {
  value: string;
  label: string;
}

interface SelectProps {
  value: string;
  onChange: (value: string) => void;
  options: Option[];
  placeholder?: string;
  className?: string;
  disabled?: boolean;
}

const Select: React.FC<SelectProps> = ({
  value,
  onChange,
  options,
  placeholder = '请选择',
  className = '',
  disabled = false
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [focusedIndex, setFocusedIndex] = useState(-1);
  const selectRef = useRef<HTMLDivElement>(null);
  const optionsRef = useRef<HTMLDivElement>(null);

  const selectedOption = options.find(option => option.value === value);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (selectRef.current && !selectRef.current.contains(event.target as Node)) {
        setIsOpen(false);
        setFocusedIndex(-1);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (isOpen && focusedIndex >= 0 && optionsRef.current) {
      const focusedElement = optionsRef.current.children[focusedIndex] as HTMLElement;
      if (focusedElement) {
        focusedElement.scrollIntoView({ block: 'nearest' });
      }
    }
  }, [focusedIndex, isOpen]);

  const handleKeyDown = (event: React.KeyboardEvent) => {
    if (disabled) return;

    switch (event.key) {
      case 'Enter':
      case ' ':
        event.preventDefault();
        if (isOpen && focusedIndex >= 0) {
          onChange(options[focusedIndex].value);
          setIsOpen(false);
          setFocusedIndex(-1);
        } else {
          setIsOpen(true);
        }
        break;
      case 'Escape':
        setIsOpen(false);
        setFocusedIndex(-1);
        break;
      case 'ArrowDown':
        event.preventDefault();
        if (!isOpen) {
          setIsOpen(true);
        } else {
          setFocusedIndex(prev => 
            prev < options.length - 1 ? prev + 1 : 0
          );
        }
        break;
      case 'ArrowUp':
        event.preventDefault();
        if (isOpen) {
          setFocusedIndex(prev => 
            prev > 0 ? prev - 1 : options.length - 1
          );
        }
        break;
    }
  };

  const handleOptionClick = (optionValue: string) => {
    onChange(optionValue);
    setIsOpen(false);
    setFocusedIndex(-1);
  };

  return (
    <div 
      ref={selectRef}
      className={`relative ${className}`}
    >
      <button
        type="button"
        onClick={() => !disabled && setIsOpen(!isOpen)}
        onKeyDown={handleKeyDown}
        disabled={disabled}
        className={`
          w-full px-4 py-3.5 text-left bg-white/90 backdrop-blur-sm border border-gray-200/50 rounded-2xl
          transition-all duration-300 ease-out shadow-apple cursor-pointer
          hover:bg-white hover:border-gray-300/70 hover:shadow-apple-lg hover:-translate-y-0.5
          focus:outline-none focus:bg-white focus:border-blue-400/50 focus:shadow-apple-lg focus:-translate-y-0.5
          disabled:bg-gray-50/50 disabled:text-gray-400 disabled:cursor-not-allowed
          ${isOpen ? 'bg-white border-blue-400/50 shadow-apple-lg -translate-y-0.5 scale-[1.01]' : ''}
          ${disabled ? 'opacity-50' : ''}
        `}
        aria-haspopup="listbox"
        aria-expanded={isOpen}
      >
        <div className="flex items-center justify-between">
          <span className={`text-sm font-normal tracking-wide leading-relaxed ${
            selectedOption ? 'text-gray-900' : 'text-gray-400'
          }`}>
            {selectedOption ? selectedOption.label : placeholder}
          </span>
          <FiChevronDown 
            className={`w-4 h-4 text-gray-400 transition-all duration-300 ease-out pointer-events-none ${
              isOpen ? 'rotate-180 text-blue-500' : ''
            }`}
          />
        </div>
      </button>

      {isOpen && (
        <div className="absolute z-50 w-full mt-3 bg-white/95 backdrop-blur-xl border border-gray-200/50 rounded-2xl shadow-apple-lg overflow-hidden slide-in-from-top-2 cursor-default">
          <div 
            ref={optionsRef}
            className="max-h-64 overflow-y-auto py-2 scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-transparent"
            role="listbox"
          >
            {options.map((option, index) => {
              const isSelected = option.value === value;
              const isFocused = index === focusedIndex;
              
              return (
                <div
                  key={option.value}
                  onClick={() => handleOptionClick(option.value)}
                  className={`
                    px-4 py-3.5 cursor-pointer transition-all duration-200 ease-out select-none
                    flex items-center justify-between
                    ${isFocused ? 'bg-blue-50/70 scale-[0.97] mx-2 rounded-xl shadow-sm' : ''}
                    ${isSelected ? 'bg-blue-50/70 text-blue-600 mx-2 rounded-xl shadow-sm' : 'text-gray-700'}
                    hover:bg-blue-50/50 hover:scale-[0.97] hover:mx-2 hover:rounded-xl hover:shadow-sm active:scale-[0.95]
                  `}
                  role="option"
                  aria-selected={isSelected}
                >
                  <span className={`text-sm font-normal tracking-wide leading-relaxed ${
                    isSelected ? 'font-medium text-blue-600' : 'text-gray-700'
                  }`}>
                    {option.label}
                  </span>
                  {isSelected && (
                    <FiCheck className="w-4 h-4 text-blue-500 zoom-in-50" />
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export default Select;