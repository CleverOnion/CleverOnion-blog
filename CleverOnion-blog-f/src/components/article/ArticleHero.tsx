import React from "react";
import { FaUser, FaCalendarAlt } from "react-icons/fa";

interface ArticleHeroProps {
  title: string;
  author: string;
  publishDate: string;
  tags: string[];
  backgroundColor?: string;
}

const ArticleHero: React.FC<ArticleHeroProps> = React.memo(
  ({
    title,
    author,
    publishDate,
    tags,
    backgroundColor = "bg-gradient-to-br from-yellow-400 via-orange-500 to-red-600",
  }) => {
    return (
      <section
        className={`relative h-80 sm:h-96 md:h-[28rem] lg:h-[32rem] xl:h-[36rem] ${backgroundColor} overflow-hidden`}
      >
        {/* 分层云朵背景 */}
        <div className="absolute inset-0 overflow-hidden" aria-hidden="true">
          {/* 使用多个云朵层确保在各种分辨率下都能覆盖 */}
          <svg
            className="absolute bottom-0 left-0 right-0 w-full"
            viewBox="0 0 1440 320"
            preserveAspectRatio="none"
            aria-hidden="true"
            style={{ minHeight: "150px" }}
          >
            {/* 云朵形状使用path创建更自然的波浪 */}
            <path
              fill="#ffffff"
              fillOpacity="1"
              d="M0,192L48,197.3C96,203,192,213,288,197.3C384,181,480,139,576,133.3C672,128,768,160,864,181.3C960,203,1056,213,1152,197.3C1248,181,1344,139,1392,117.3L1440,96L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"
            ></path>
          </svg>

          {/* 第二层云朵，增加层次感 */}
          <svg
            className="absolute bottom-0 left-0 right-0 w-full"
            viewBox="0 0 1440 200"
            preserveAspectRatio="none"
            aria-hidden="true"
            style={{ minHeight: "100px" }}
          >
            <path
              fill="#ffffff"
              fillOpacity="0.8"
              d="M0,96L60,106.7C120,117,240,139,360,133.3C480,128,600,96,720,90.7C840,85,960,107,1080,117.3C1200,128,1320,128,1380,128L1440,128L1440,200L1380,200C1320,200,1200,200,1080,200C960,200,840,200,720,200C600,200,480,200,360,200C240,200,120,200,60,200L0,200Z"
            ></path>
          </svg>
        </div>

        {/* 内容区域 */}
        <div className="relative z-10 h-full flex items-center justify-center px-4 sm:px-6 md:px-8">
          <div className="text-center max-w-4xl mx-auto">
            {/* 文章标题 */}
            <h1 className="text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-bold text-black mb-4 sm:mb-5 md:mb-6 leading-tight drop-shadow-lg">
              {title}
            </h1>

            {/* 文章元信息 */}
            <div className="flex flex-wrap items-center justify-center gap-3 sm:gap-4 md:gap-6 mb-4 sm:mb-5 md:mb-6 text-black/80">
              <div className="flex items-center gap-1.5 sm:gap-2">
                <FaUser className="text-sm sm:text-base md:text-lg" />
                <span className="text-sm sm:text-base md:text-lg font-medium">
                  {author}
                </span>
              </div>
              <div className="flex items-center gap-1.5 sm:gap-2">
                <FaCalendarAlt className="text-sm sm:text-base md:text-lg" />
                <span className="text-sm sm:text-base md:text-lg">
                  {publishDate}
                </span>
              </div>
            </div>

            {/* 标签 */}
            <div className="flex flex-wrap justify-center gap-2 sm:gap-2.5 md:gap-3">
              {tags.map((tag, index) => (
                <span
                  key={`tag-${tag}-${index}`}
                  className="px-3 py-1.5 sm:px-4 sm:py-2 bg-white/30 backdrop-blur-sm text-black rounded-full text-xs sm:text-sm font-medium border border-black/20 hover:bg-white/40 transition-colors duration-200"
                >
                  {tag}
                </span>
              ))}
            </div>
          </div>
        </div>

        {/* 底部渐变遮罩 */}
        <div className="absolute bottom-0 left-0 right-0 h-16 sm:h-20 bg-gradient-to-t from-white/20 to-transparent"></div>
      </section>
    );
  }
);

ArticleHero.displayName = "ArticleHero";

export default ArticleHero;
