import React, { useState, useEffect, useRef } from "react";
import LatestArticles from "./LatestArticles";
import TagList from "./TagList";
import PopularArticles from "./PopularArticles";
import { useThrottle } from "../../hooks/useThrottle";

const MainContent: React.FC = () => {
  const [isTagListVisible, setIsTagListVisible] = useState(true);
  const tagListRef = useRef<HTMLDivElement>(null);

  // 创建节流的滚动处理函数
  const handleWindowScroll = useThrottle(() => {
    if (tagListRef.current) {
      const tagListRect = tagListRef.current.getBoundingClientRect();
      const tagListBottom = tagListRect.bottom;

      // 当标签列表完全滚动出视口顶部时隐藏
      setIsTagListVisible(tagListBottom > 0);
    }
  }, 100); // 每100ms最多执行一次

  useEffect(() => {
    window.addEventListener("scroll", handleWindowScroll);

    return () => {
      window.removeEventListener("scroll", handleWindowScroll);
    };
  }, [handleWindowScroll]);

  return (
    <div className="container mx-auto px-4 sm:px-6 md:px-8 lg:px-12 xl:px-16 2xl:px-50 py-8">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 md:gap-8">
        {/* 左列 - 最新文章列表 */}
        <div className="lg:col-span-2">
          <LatestArticles />
        </div>

        {/* 右列 - 标签列表和热门文章 */}
        <div className="lg:col-span-1 space-y-6">
          {/* 标签列表 - 可隐藏 */}
          <div ref={tagListRef}>
            <TagList isVisible={isTagListVisible} />
          </div>

          {/* 热门文章列表 - 固定显示 */}
          <PopularArticles />
        </div>
      </div>
    </div>
  );
};

export default MainContent;
