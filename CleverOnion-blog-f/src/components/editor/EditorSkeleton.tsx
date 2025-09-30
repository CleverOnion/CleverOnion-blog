import React from "react";

/**
 * 编辑器加载骨架屏
 * 镜像实际编辑器的布局，避免布局跳动
 */
export const EditorSkeleton: React.FC = () => {
  return (
    <div className="fixed inset-0 bg-white flex flex-col animate-pulse">
      {/* 工具栏骨架 */}
      <div className="flex items-center justify-between px-6 py-3 border-b border-gray-200 bg-white flex-shrink-0">
        <div className="flex items-center space-x-4 flex-1">
          {/* 返回按钮骨架 */}
          <div className="w-9 h-9 bg-gray-200 rounded-lg" />

          {/* 标题输入框骨架 */}
          <div className="h-7 bg-gray-200 rounded w-64" />
        </div>

        {/* 操作按钮骨架 */}
        <div className="flex items-center space-x-3">
          <div className="w-24 h-9 bg-gray-200 rounded-lg" />
          <div className="w-24 h-9 bg-gray-200 rounded-lg" />
        </div>
      </div>

      {/* 主内容区骨架 */}
      <div className="flex-1 flex overflow-hidden">
        {/* 编辑器内容骨架 */}
        <div className="flex-1 p-8 space-y-4">
          {/* 模拟编辑器内容行 */}
          <div className="h-6 bg-gray-200 rounded w-3/4" />
          <div className="h-6 bg-gray-200 rounded w-full" />
          <div className="h-6 bg-gray-200 rounded w-5/6" />
          <div className="h-6 bg-gray-200 rounded w-full" />
          <div className="h-6 bg-gray-200 rounded w-2/3" />

          {/* 空白间隔 */}
          <div className="h-8" />

          <div className="h-6 bg-gray-200 rounded w-4/5" />
          <div className="h-6 bg-gray-200 rounded w-full" />
          <div className="h-6 bg-gray-200 rounded w-3/4" />
        </div>

        {/* 设置面板骨架 */}
        <div className="w-80 border-l border-gray-200 bg-gray-50 p-4 space-y-4 flex-shrink-0">
          {/* 分类选择骨架 */}
          <div className="bg-white rounded-lg p-4 shadow-sm">
            <div className="h-4 bg-gray-200 rounded w-16 mb-3" />
            <div className="h-10 bg-gray-200 rounded" />
          </div>

          {/* 摘要骨架 */}
          <div className="bg-white rounded-lg p-4 shadow-sm">
            <div className="h-4 bg-gray-200 rounded w-20 mb-3" />
            <div className="h-20 bg-gray-200 rounded" />
            <div className="h-3 bg-gray-200 rounded w-24 mt-2" />
          </div>

          {/* 标签骨架 */}
          <div className="bg-white rounded-lg p-4 shadow-sm">
            <div className="h-4 bg-gray-200 rounded w-16 mb-3" />
            <div className="flex flex-wrap gap-2">
              <div className="h-8 bg-gray-200 rounded-full w-16" />
              <div className="h-8 bg-gray-200 rounded-full w-20" />
              <div className="h-8 bg-gray-200 rounded-full w-24" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EditorSkeleton;
