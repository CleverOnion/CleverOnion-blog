import React from "react";
import MilkdownEditor from "../MilkdownEditor";

interface EditorContentProps {
  content: string;
  onContentChange: (content: string) => void;
}

const EditorContent: React.FC<EditorContentProps> = ({
  content,
  onContentChange,
}) => {
  return (
    <main
      id="editor-main-content"
      className="flex-1 overflow-auto w-full h-full"
      role="main"
      aria-label="文章内容编辑器"
      tabIndex={-1}
    >
      <div className="w-full h-full">
        <MilkdownEditor
          value={content}
          onChange={onContentChange}
          className="w-full h-full"
        />
      </div>
    </main>
  );
};

export default EditorContent;
