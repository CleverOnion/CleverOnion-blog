import React, { useState } from "react";
import { FiHelpCircle } from "react-icons/fi";

interface Shortcut {
  keys: string[];
  description: string;
}

interface KeyboardShortcutsHelpProps {
  shortcuts: Shortcut[];
  className?: string;
}

/**
 * 键盘快捷键帮助组件
 * 显示可用的键盘快捷键列表
 */
export const KeyboardShortcutsHelp: React.FC<KeyboardShortcutsHelpProps> = ({
  shortcuts,
  className = "",
}) => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className={`relative ${className}`}>
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-1"
        aria-label="显示键盘快捷键"
        title="键盘快捷键"
      >
        <FiHelpCircle className="w-5 h-5" aria-hidden="true" />
      </button>

      {isOpen && (
        <>
          {/* 遮罩层 */}
          <div
            className="fixed inset-0 z-40"
            onClick={() => setIsOpen(false)}
            aria-hidden="true"
          />

          {/* 快捷键列表 */}
          <div
            className="absolute right-0 top-full mt-2 w-64 bg-white rounded-lg shadow-lg border border-gray-200 z-50 animate-in slide-in-from-top-2"
            role="dialog"
            aria-label="键盘快捷键列表"
          >
            <div className="p-4">
              <h3 className="text-sm font-semibold text-gray-900 mb-3">
                键盘快捷键
              </h3>
              <div className="space-y-2">
                {shortcuts.map((shortcut, index) => (
                  <div
                    key={index}
                    className="flex items-center justify-between text-sm"
                  >
                    <span className="text-gray-600">
                      {shortcut.description}
                    </span>
                    <div className="flex items-center gap-1">
                      {shortcut.keys.map((key, keyIndex) => (
                        <React.Fragment key={keyIndex}>
                          {keyIndex > 0 && (
                            <span className="text-gray-400 text-xs">+</span>
                          )}
                          <kbd className="px-2 py-1 text-xs font-semibold text-gray-700 bg-gray-100 border border-gray-300 rounded">
                            {key}
                          </kbd>
                        </React.Fragment>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default KeyboardShortcutsHelp;
