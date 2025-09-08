import React from 'react';
import MilkdownEditor from '../MilkdownEditor';

interface EditorContentProps {
  content: string;
  onContentChange: (content: string) => void;
}

const EditorContent: React.FC<EditorContentProps> = ({ content, onContentChange }) => {
  return (
    <div className="flex-1 overflow-auto w-full h-full">
      <div className="w-full h-full">
        <MilkdownEditor
          value={content}
          onChange={onContentChange}
          className="w-full h-full"
        />
      </div>
    </div>
  );
};

export default EditorContent;