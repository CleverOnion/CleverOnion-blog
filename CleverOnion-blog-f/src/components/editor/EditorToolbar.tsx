import React, { useRef, useEffect } from "react";
import {
  FiSave,
  FiSend,
  FiArrowLeft,
  FiEdit3,
  FiArchive,
} from "react-icons/fi";
import FormErrorMessage from "../ui/FormErrorMessage";
import UnsavedChangesIndicator from "../ui/UnsavedChangesIndicator";
import KeyboardShortcutsHelp from "../ui/KeyboardShortcutsHelp";

interface EditorToolbarProps {
  title: string;
  articleStatus?: "DRAFT" | "PUBLISHED";
  isNewArticle: boolean;
  onTitleChange: (title: string) => void;
  onTitleBlur?: () => void;
  onTitleKeyDown?: (e: React.KeyboardEvent<HTMLInputElement>) => void;
  onBack: () => void;
  onSaveDraft: () => void;
  onPublish: () => void;
  onUnpublish?: () => void;
  onUpdate?: () => void;
  titleError?: string;
  showTitleError?: boolean;
  registerTitleRef?: (ref: HTMLInputElement | null) => void;
  saving?: boolean;
  hasUnsavedChanges?: boolean;
}

const EditorToolbar: React.FC<EditorToolbarProps> = ({
  title,
  articleStatus,
  onTitleChange,
  onTitleBlur,
  onTitleKeyDown,
  onBack,
  onSaveDraft,
  onPublish,
  onUnpublish,
  onUpdate,
  titleError,
  showTitleError = false,
  registerTitleRef,
  saving = false,
  hasUnsavedChanges = false,
}) => {
  const titleInputRef = useRef<HTMLInputElement>(null);

  // 注册输入框引用
  useEffect(() => {
    if (registerTitleRef && titleInputRef.current) {
      registerTitleRef(titleInputRef.current);
    }
  }, [registerTitleRef]);

  // 根据文章状态渲染不同的按钮
  const renderActionButtons = () => {
    if (articleStatus === "PUBLISHED") {
      // 已发布文章：显示"转为草稿"和"更新文章"按钮
      return (
        <>
          <button
            onClick={onUnpublish}
            disabled={saving}
            className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            aria-label="转为草稿"
          >
            {saving ? (
              <div className="w-4 h-4 mr-2 border-2 border-current border-t-transparent rounded-full animate-spin" />
            ) : (
              <FiArchive className="w-4 h-4 mr-2" aria-hidden="true" />
            )}
            转为草稿
          </button>

          <button
            onClick={onUpdate}
            disabled={saving}
            className="flex items-center px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            aria-label={saving ? "正在更新文章..." : "更新文章"}
          >
            {saving ? (
              <div className="w-4 h-4 mr-2 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <FiEdit3 className="w-4 h-4 mr-2" aria-hidden="true" />
            )}
            更新文章
          </button>
        </>
      );
    } else {
      // 新文章或草稿：显示"保存草稿"和"发布文章"按钮
      return (
        <>
          <button
            onClick={onSaveDraft}
            disabled={saving}
            className="flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            aria-label={saving ? "正在保存草稿..." : "保存草稿"}
          >
            {saving ? (
              <div className="w-4 h-4 mr-2 border-2 border-current border-t-transparent rounded-full animate-spin" />
            ) : (
              <FiSave className="w-4 h-4 mr-2" aria-hidden="true" />
            )}
            保存草稿
          </button>

          <button
            onClick={onPublish}
            disabled={saving}
            className="flex items-center px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            aria-label={saving ? "正在发布文章..." : "发布文章"}
          >
            {saving ? (
              <div className="w-4 h-4 mr-2 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <FiSend className="w-4 h-4 mr-2" aria-hidden="true" />
            )}
            发布文章
          </button>
        </>
      );
    }
  };

  return (
    <header
      className="flex flex-col border-b border-gray-200 bg-white flex-shrink-0"
      role="banner"
      aria-label="文章编辑工具栏"
    >
      <div className="flex items-center justify-between px-6 py-3">
        <div className="flex items-center space-x-4 flex-1 min-w-0">
          <button
            onClick={onBack}
            className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-1"
            title="返回文章列表"
            aria-label="返回文章列表"
          >
            <FiArrowLeft className="w-5 h-5" aria-hidden="true" />
          </button>

          <div className="flex-1 min-w-0">
            <input
              ref={titleInputRef}
              type="text"
              value={title}
              onChange={(e) => onTitleChange(e.target.value)}
              onBlur={onTitleBlur}
              onKeyDown={onTitleKeyDown}
              className={`w-full text-lg font-semibold text-gray-900 placeholder-gray-400 border-0 focus:outline-none focus:ring-0 bg-transparent ${
                showTitleError && titleError ? "text-red-600" : ""
              }`}
              placeholder="无标题文档"
              aria-label="文章标题"
              aria-invalid={showTitleError && !!titleError}
              aria-describedby={
                showTitleError && titleError ? "title-error" : undefined
              }
              autoComplete="title"
            />
          </div>
        </div>

        <div className="flex items-center space-x-3 ml-4">
          <UnsavedChangesIndicator hasUnsavedChanges={hasUnsavedChanges} />
          <KeyboardShortcutsHelp
            shortcuts={[
              { keys: ["Ctrl", "S"], description: "保存草稿" },
              {
                keys: ["Ctrl", "Enter"],
                description:
                  articleStatus === "PUBLISHED" ? "更新文章" : "发布文章",
              },
              { keys: ["Enter"], description: "标题 → 编辑器" },
            ]}
          />
          {renderActionButtons()}
        </div>
      </div>

      {/* 标题错误提示 */}
      {showTitleError && titleError && (
        <div className="px-6 pb-3">
          <FormErrorMessage
            id="title-error"
            message={titleError}
            show={showTitleError}
          />
        </div>
      )}
    </header>
  );
};

export default EditorToolbar;
