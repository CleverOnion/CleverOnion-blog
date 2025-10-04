import React, { useState, useEffect } from "react";
import { tagApi, TagWithCount } from "../../api/tags";

interface TagListProps {
  isVisible?: boolean;
}

const TagList: React.FC<TagListProps> = React.memo(({ isVisible = true }) => {
  const [tags, setTags] = useState<TagWithCount[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 加载热门标签
  const loadPopularTags = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await tagApi.getTagsWithCount(0, 10);
      setTags(response.tags);
    } catch (error) {
      console.error("加载热门标签失败:", error);
      setError("加载标签失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPopularTags();
  }, []);

  return (
    <div
      className={`transition-all duration-300 ${
        isVisible
          ? "opacity-100 transform translate-y-0"
          : "opacity-0 transform -translate-y-4"
      }`}
      style={{ display: isVisible ? "block" : "none" }}
    >
      <div className="text-center mb-8">
        <h2 className="text-pink-500 font-semibold text-2xl uppercase tracking-wider mb-6">
          热门标签
        </h2>
      </div>

      {loading ? (
        <div className="flex flex-wrap gap-3 justify-center">
          {Array.from({ length: 8 }).map((_, index) => (
            <div
              key={index}
              className="px-6 py-3 rounded-full bg-gray-200 animate-pulse"
              style={{ width: "80px", height: "44px" }}
            />
          ))}
        </div>
      ) : error ? (
        <div className="text-center py-4">
          <p className="text-red-500 text-sm">{error}</p>
          <button
            onClick={loadPopularTags}
            className="mt-2 px-4 py-2 text-sm text-blue-600 hover:text-blue-700 transition-colors"
          >
            重试
          </button>
        </div>
      ) : tags.length === 0 ? (
        <div className="text-center py-4">
          <p className="text-gray-500 text-sm">暂无热门标签</p>
        </div>
      ) : (
        <div className="flex flex-wrap gap-3 justify-center">
          {tags.map((tag) => (
            <button
              key={tag.id}
              className="px-6 py-3 rounded-full text-gray-700 bg-sky-100 hover:bg-sky-200 transition-colors cursor-pointer font-medium"
              title={`${tag.articleCount} 篇文章`}
            >
              {tag.name}
            </button>
          ))}
        </div>
      )}
    </div>
  );
});

TagList.displayName = "TagList";

export default TagList;
