import React from 'react';

interface Tag {
  id: number;
  name: string;
  count: number;
}

interface TagListProps {
  tags?: Tag[];
  isVisible?: boolean;
}

const TagList: React.FC<TagListProps> = ({ tags = [], isVisible = true }) => {
  // 模拟数据
  const mockTags: Tag[] = [
    { id: 1, name: "CSS", count: 15 },
    { id: 2, name: "React", count: 12 },
    { id: 3, name: "Animation", count: 8 },
    { id: 4, name: "Career", count: 6 },
    { id: 5, name: "JavaScript", count: 20 },
    { id: 6, name: "SVG", count: 10 },
    { id: 7, name: "Next.js", count: 5 },
    { id: 8, name: "General", count: 7 }
  ];

  const displayTags = tags.length > 0 ? tags : mockTags;

  return (
    <div 
      className={`transition-all duration-300 ${
        isVisible ? 'opacity-100 transform translate-y-0' : 'opacity-0 transform -translate-y-4'
      }`}
      style={{ display: isVisible ? 'block' : 'none' }}
    >
      <div className="text-center mb-8">
        <h3 className="text-pink-500 font-semibold text-lg uppercase tracking-wider mb-6">BROWSE BY TAGS</h3>
      </div>
      
      <div className="flex flex-wrap gap-3 justify-center">
        {displayTags.map((tag) => (
          <button
            key={tag.id}
            className="px-6 py-3 rounded-full text-gray-700 bg-sky-100 hover:bg-sky-200 transition-colors cursor-pointer font-medium"
          >
            {tag.name}
          </button>
        ))}
      </div>
    </div>
  );
};

export default TagList;