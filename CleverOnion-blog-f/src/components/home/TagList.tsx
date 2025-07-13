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
    { id: 1, name: "React", count: 15 },
    { id: 2, name: "TypeScript", count: 12 },
    { id: 3, name: "Node.js", count: 8 },
    { id: 4, name: "Vue.js", count: 6 },
    { id: 5, name: "JavaScript", count: 20 },
    { id: 6, name: "CSS", count: 10 },
    { id: 7, name: "HTML", count: 5 },
    { id: 8, name: "Python", count: 7 },
    { id: 9, name: "Java", count: 4 },
    { id: 10, name: "Go", count: 3 }
  ];

  const displayTags = tags.length > 0 ? tags : mockTags;

  return (
    <div 
      className={`bg-white rounded-lg shadow-sm p-6 transition-all duration-300 ${
        isVisible ? 'opacity-100 transform translate-y-0' : 'opacity-0 transform -translate-y-4'
      }`}
      style={{ display: isVisible ? 'block' : 'none' }}
    >
      <h3 className="text-lg font-semibold text-gray-900 mb-4">热门标签</h3>
      
      <div className="flex flex-wrap gap-2">
        {displayTags.map((tag) => (
          <button
            key={tag.id}
            className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-gray-100 text-gray-700 hover:bg-blue-100 hover:text-blue-800 transition-colors cursor-pointer"
          >
            <span>{tag.name}</span>
            <span className="ml-1 text-xs text-gray-500">({tag.count})</span>
          </button>
        ))}
      </div>
      
      <div className="mt-4 text-center">
        <button className="text-blue-600 hover:text-blue-800 text-sm font-medium">
          查看所有标签 →
        </button>
      </div>
    </div>
  );
};

export default TagList;