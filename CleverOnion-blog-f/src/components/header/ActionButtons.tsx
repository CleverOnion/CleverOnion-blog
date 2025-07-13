import React from 'react';

interface ActionButtonsProps {
  isMobileMenuOpen: boolean;
  onMobileMenuToggle: () => void;
}

const ActionButtons: React.FC<ActionButtonsProps> = ({ isMobileMenuOpen, onMobileMenuToggle }) => {
  const buttonClass = "p-2.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all duration-200";

  return (
    <div className="flex items-center space-x-2">
      {/* Search Icon */}
      <button className={buttonClass} aria-label="搜索">
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
      </button>

      {/* Sound Icon */}
      <button className={buttonClass} aria-label="音频">
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M9 12a3 3 0 106 0v-1a3 3 0 00-6 0v1z" />
        </svg>
      </button>

      {/* Theme Toggle */}
      <button className={buttonClass} aria-label="切换主题">
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
        </svg>
      </button>

      {/* RSS Icon */}
      <button className={buttonClass} aria-label="RSS订阅">
        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
          <path d="M6.503 20.752c0 1.794-1.456 3.248-3.251 3.248S0 22.546 0 20.752s1.456-3.248 3.252-3.248 3.251 1.454 3.251 3.248zM1.677 6.082v4.15c6.988 0 12.65 5.662 12.65 12.65h4.15c0-9.271-7.529-16.8-16.8-16.8zM1.677.014v4.151C14.727 4.165 25.836 15.274 25.85 28.324H30C29.986 13.19 14.811-0.001 1.677.014z"/>
        </svg>
      </button>

      {/* Mobile menu button */}
      <button 
        className={`md:hidden ${buttonClass}`}
        onClick={onMobileMenuToggle}
        aria-label="菜单"
      >
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={isMobileMenuOpen ? "M6 18L18 6M6 6l12 12" : "M4 6h16M4 12h16M4 18h16"} />
        </svg>
      </button>
    </div>
  );
};

export default ActionButtons;