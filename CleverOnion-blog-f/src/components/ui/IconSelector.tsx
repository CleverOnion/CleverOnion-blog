import React, { useState, useRef, useEffect } from "react";
import {
  getIconsByCategory,
  renderIcon,
  getAvailableIcons,
} from "../../utils/iconUtils";
import Modal from "./Modal";

interface IconSelectorProps {
  value?: string;
  onChange: (iconName: string) => void;
  placeholder?: string;
  className?: string;
}

/**
 * 图标选择器组件
 * 提供分类图标选择功能，支持按分类浏览和搜索
 * 采用模态框设计，提供更好的用户体验
 */
const IconSelector: React.FC<IconSelectorProps> = ({
  value,
  onChange,
  placeholder = "选择图标",
  className = "",
}) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<string>("all");
  const searchInputRef = useRef<HTMLInputElement>(null);

  const iconsByCategory = getIconsByCategory();
  const allIcons = getAvailableIcons();

  // 获取当前显示的图标列表
  const getDisplayIcons = () => {
    let icons: string[] = [];

    if (selectedCategory === "all") {
      icons = allIcons;
    } else {
      icons =
        iconsByCategory[selectedCategory as keyof typeof iconsByCategory] || [];
    }

    // 如果有搜索词，进行智能过滤
    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase();
      icons = icons.filter((icon) => {
        const iconLower = icon.toLowerCase();

        // 1. 精确匹配优先
        if (iconLower === searchLower) return true;

        // 2. 开头匹配
        if (iconLower.startsWith(searchLower)) return true;

        // 3. 包含匹配
        if (iconLower.includes(searchLower)) return true;

        // 4. 去掉前缀后匹配 (如 FaReact -> react)
        const withoutPrefix = iconLower.replace(
          /^(fa|md|si|fi|bs|ai|di|go|gr|hi|im|io|ri|tb|ti|vsc|wi)/,
          ""
        );
        if (withoutPrefix.includes(searchLower)) return true;

        // 5. 模糊匹配关键词
        const keywords = {
          react: ["react", "jsx"],
          vue: ["vue", "vuejs"],
          angular: ["angular", "ng"],
          node: ["node", "nodejs", "js"],
          python: ["python", "py"],
          java: ["java"],
          css: ["css", "style"],
          html: ["html", "markup"],
          js: ["javascript", "js", "node"],
          ts: ["typescript", "ts"],
          mobile: ["mobile", "phone", "android", "ios"],
          database: ["database", "db", "sql", "mongo", "mysql", "postgres"],
          cloud: ["cloud", "aws", "azure", "gcp"],
          git: ["git", "github", "gitlab", "version"],
          design: ["design", "figma", "sketch", "adobe"],
          tool: ["tool", "build", "webpack", "vite"],
        };

        for (const [key, values] of Object.entries(keywords)) {
          if (
            values.some((v) => searchLower.includes(v)) &&
            iconLower.includes(key)
          ) {
            return true;
          }
        }

        return false;
      });

      // 按匹配优先级排序
      icons.sort((a, b) => {
        const aLower = a.toLowerCase();
        const bLower = b.toLowerCase();

        // 精确匹配最优先
        if (aLower === searchLower) return -1;
        if (bLower === searchLower) return 1;

        // 开头匹配次优先
        if (aLower.startsWith(searchLower) && !bLower.startsWith(searchLower))
          return -1;
        if (bLower.startsWith(searchLower) && !aLower.startsWith(searchLower))
          return 1;

        // 其他按字母顺序
        return a.localeCompare(b);
      });
    }

    return icons;
  };

  // 分类标签映射
  const categoryLabels = {
    all: "全部图标",
    frontend: "前端开发",
    backend: "后端开发",
    mobile: "移动开发",
    tools: "开发工具",
    design: "设计工具",
    business: "商务办公",
    general: "通用图标",
  };

  // 处理图标选择
  const handleIconSelect = (iconName: string) => {
    onChange(iconName);
    setIsModalOpen(false);
    setSearchTerm("");
    setSelectedCategory("all");
  };

  // 处理模态框打开
  const handleOpenModal = () => {
    setIsModalOpen(true);
    setSearchTerm("");
    setSelectedCategory("all");
  };

  // 处理模态框关闭
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSearchTerm("");
    setSelectedCategory("all");
  };

  // 当模态框打开时聚焦搜索框
  useEffect(() => {
    if (isModalOpen && searchInputRef.current) {
      // 延迟聚焦，确保模态框完全渲染
      const timer = setTimeout(() => {
        searchInputRef.current?.focus();
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [isModalOpen]);

  const displayIcons = getDisplayIcons();

  return (
    <>
      {/* 选择器触发按钮 */}
      <div className={`relative ${className}`}>
        <button
          type="button"
          onClick={handleOpenModal}
          className="w-full flex items-center justify-between px-3 py-2 border border-gray-300 rounded-lg bg-white hover:border-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
        >
          <div className="flex items-center space-x-2">
            {value ? (
              <>
                {renderIcon(value, "w-4 h-4 text-gray-600")}
                <span className="text-sm text-gray-700">{value}</span>
              </>
            ) : (
              <span className="text-sm text-gray-500">{placeholder}</span>
            )}
          </div>
          <svg
            className="w-4 h-4 text-gray-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 9l-7 7-7-7"
            />
          </svg>
        </button>
      </div>

      {/* 图标选择模态框 */}
      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        title="选择图标"
        size="xl"
      >
        <div className="flex flex-col h-full">
          {/* 搜索栏 */}
          <div className="flex-shrink-0 p-6 border-b border-gray-200">
            <div className="relative">
              <input
                ref={searchInputRef}
                type="text"
                placeholder="搜索图标名称..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-10 py-3 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              <svg
                className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
              {searchTerm && (
                <button
                  type="button"
                  onClick={() => setSearchTerm("")}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 hover:text-gray-600"
                >
                  <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M6 18L18 6M6 6l12 12"
                    />
                  </svg>
                </button>
              )}
            </div>

            {/* 搜索建议 */}
            {!searchTerm && (
              <div className="mt-3">
                <p className="text-xs text-gray-500 mb-2">热门搜索：</p>
                <div className="flex flex-wrap gap-2">
                  {[
                    "react",
                    "vue",
                    "node",
                    "css",
                    "mobile",
                    "database",
                    "git",
                    "design",
                  ].map((suggestion) => (
                    <button
                      key={suggestion}
                      type="button"
                      onClick={() => setSearchTerm(suggestion)}
                      className="px-2 py-1 text-xs text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
                    >
                      {suggestion}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* 分类导航 */}
          <div className="flex-shrink-0 px-6 py-4 border-b border-gray-200 bg-gray-50">
            <div className="flex flex-wrap gap-2">
              {Object.entries(categoryLabels).map(([key, label]) => (
                <button
                  key={key}
                  type="button"
                  onClick={() => setSelectedCategory(key)}
                  className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors ${
                    selectedCategory === key
                      ? "bg-blue-600 text-white shadow-sm"
                      : "bg-white text-gray-700 border border-gray-300 hover:bg-gray-50"
                  }`}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>

          {/* 图标网格区域 */}
          <div className="flex-1 overflow-y-auto p-6">
            {displayIcons.length > 0 ? (
              <>
                {/* 图标统计信息 */}
                <div className="mb-4 text-sm text-gray-600">
                  {searchTerm ? (
                    <span>
                      搜索 "{searchTerm}" 找到 {displayIcons.length} 个图标
                    </span>
                  ) : (
                    <span>
                      {
                        categoryLabels[
                          selectedCategory as keyof typeof categoryLabels
                        ]
                      }{" "}
                      - {displayIcons.length} 个图标
                    </span>
                  )}
                </div>

                {/* 图标网格 */}
                <div className="grid grid-cols-6 sm:grid-cols-8 md:grid-cols-10 lg:grid-cols-12 xl:grid-cols-14 2xl:grid-cols-16 gap-3">
                  {displayIcons.map((iconName) => (
                    <button
                      key={iconName}
                      type="button"
                      onClick={() => handleIconSelect(iconName)}
                      className={`group relative p-3 rounded-lg border-2 transition-all duration-200 hover:scale-105 ${
                        value === iconName
                          ? "border-blue-500 bg-blue-50 shadow-md"
                          : "border-gray-200 hover:border-gray-300 hover:bg-gray-50"
                      }`}
                      title={iconName}
                    >
                      {renderIcon(
                        iconName,
                        `w-6 h-6 ${
                          value === iconName
                            ? "text-blue-600"
                            : "text-gray-600 group-hover:text-gray-800"
                        }`
                      )}

                      {/* 选中状态指示器 */}
                      {value === iconName && (
                        <div className="absolute -top-1 -right-1 w-4 h-4 bg-blue-500 rounded-full flex items-center justify-center">
                          <svg
                            className="w-2.5 h-2.5 text-white"
                            fill="currentColor"
                            viewBox="0 0 20 20"
                          >
                            <path
                              fillRule="evenodd"
                              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                              clipRule="evenodd"
                            />
                          </svg>
                        </div>
                      )}

                      {/* 图标名称提示 */}
                      <div className="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-2 py-1 bg-gray-900 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap z-10">
                        {iconName}
                      </div>
                    </button>
                  ))}
                </div>
              </>
            ) : (
              <div className="flex flex-col items-center justify-center py-16 text-gray-500">
                <svg
                  className="w-16 h-16 mb-4 text-gray-300"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={1}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  没有找到匹配的图标
                </h3>
                <p className="text-sm text-gray-500 text-center max-w-sm">
                  {searchTerm ? (
                    <>尝试使用不同的关键词搜索，或者选择其他分类查看更多图标</>
                  ) : (
                    <>当前分类下暂无图标，请选择其他分类</>
                  )}
                </p>
              </div>
            )}
          </div>

          {/* 底部操作栏 */}
          <div className="flex-shrink-0 px-6 py-4 border-t border-gray-200 bg-gray-50 flex items-center justify-between">
            <div className="flex items-center space-x-4">
              {value && (
                <button
                  type="button"
                  onClick={() => handleIconSelect("")}
                  className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors"
                >
                  清除选择
                </button>
              )}
            </div>

            <div className="flex items-center space-x-3">
              <button
                type="button"
                onClick={handleCloseModal}
                className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors"
              >
                取消
              </button>
              {value && (
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-6 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
                >
                  确认选择
                </button>
              )}
            </div>
          </div>
        </div>
      </Modal>
    </>
  );
};

export default IconSelector;
