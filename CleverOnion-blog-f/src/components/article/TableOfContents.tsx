import React from 'react';
import { motion } from 'motion/react';

interface TOCItem {
  id: string;
  title: string;
  level: number;
}

interface TableOfContentsProps {
  tableOfContents: TOCItem[];
  onSectionClick: (id: string) => void;
}

const TableOfContents: React.FC<TableOfContentsProps> = ({
  tableOfContents,
  onSectionClick
}) => {
  return (
    <motion.aside 
      className="w-80 sticky top-24 h-fit"
      initial={{ opacity: 0, x: 30 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.8, delay: 0.3, ease: "easeOut" }}
    >
      <div className="bg-white/80 backdrop-blur-sm rounded-lg p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">目录</h3>
        <nav className="space-y-2">
          {tableOfContents.map((item, index) => (
            <motion.button
              key={`toc-${item.id || index}-${index}`}
              onClick={() => onSectionClick(item.id)}
              className={`block w-full text-left py-2 px-3 rounded-md transition-all duration-200 ${
                item.level === 3 ? 'ml-4 text-sm' : 'text-sm'
              } text-gray-600 hover:bg-sky-50/50 hover:text-sky-600`}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ 
                duration: 0.4, 
                delay: 0.5 + index * 0.1, 
                ease: "easeOut" 
              }}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
            >
              {item.title}
            </motion.button>
          ))}
        </nav>
      </div>
    </motion.aside>
  );
};

export default TableOfContents;