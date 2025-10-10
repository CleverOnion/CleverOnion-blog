import React, { useRef, useState } from "react";
import { FiUpload, FiAlertTriangle } from "react-icons/fi";
import Modal from "../ui/Modal";

interface MarkdownImporterProps {
  onImport: (content: string, filename: string) => void;
  hasContent: boolean;
  disabled?: boolean;
}

const MarkdownImporter: React.FC<MarkdownImporterProps> = ({
  onImport,
  hasContent,
  disabled = false,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [pendingFile, setPendingFile] = useState<{
    content: string;
    filename: string;
  } | null>(null);

  const handleFileSelect = async (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // 验证文件类型
    if (!file.name.endsWith(".md") && !file.name.endsWith(".markdown")) {
      alert("请选择 Markdown 文件（.md 或 .markdown）");
      return;
    }

    // 读取文件内容
    try {
      const content = await readFileContent(file);

      // 如果编辑器已有内容，显示确认对话框
      if (hasContent) {
        setPendingFile({ content, filename: file.name });
        setShowConfirmDialog(true);
      } else {
        // 直接导入
        onImport(content, file.name);
      }
    } catch (error) {
      console.error("读取文件失败:", error);
      alert("读取文件失败，请重试");
    } finally {
      // 重置 input，允许选择相同文件
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  const readFileContent = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = (e) => {
        const content = e.target?.result as string;
        resolve(content);
      };

      reader.onerror = () => {
        reject(new Error("文件读取失败"));
      };

      reader.readAsText(file, "UTF-8");
    });
  };

  const handleConfirmImport = () => {
    if (pendingFile) {
      onImport(pendingFile.content, pendingFile.filename);
      setPendingFile(null);
      setShowConfirmDialog(false);
    }
  };

  const handleCancelImport = () => {
    setPendingFile(null);
    setShowConfirmDialog(false);
  };

  const handleButtonClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <>
      <input
        ref={fileInputRef}
        type="file"
        accept=".md,.markdown"
        onChange={handleFileSelect}
        className="hidden"
        aria-label="选择 Markdown 文件"
      />

      <button
        onClick={handleButtonClick}
        disabled={disabled}
        className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-1"
        title="导入 Markdown 文件"
        aria-label="导入 Markdown 文件"
      >
        <FiUpload className="w-4 h-4 mr-2" aria-hidden="true" />
        <span className="hidden sm:inline">导入 MD</span>
      </button>

      {/* 确认对话框 */}
      <Modal
        isOpen={showConfirmDialog}
        onClose={handleCancelImport}
        title="确认导入文件"
        size="sm"
        closeOnOverlayClick={false}
      >
        <div className="space-y-4">
          <div className="flex items-start space-x-3">
            <div className="flex-shrink-0">
              <FiAlertTriangle
                className="w-6 h-6 text-yellow-500"
                aria-hidden="true"
              />
            </div>
            <div className="flex-1">
              <p className="text-gray-700 font-medium mb-2">
                当前编辑器中已有内容
              </p>
              <p className="text-gray-600 text-sm mb-3">
                导入文件将会
                <strong className="text-red-600">替换所有现有内容</strong>
                （包括标题和正文）。 此操作无法撤销。
              </p>
              {pendingFile && (
                <div className="bg-gray-50 rounded p-3 text-sm">
                  <p className="text-gray-700">
                    <span className="font-medium">文件名：</span>
                    {pendingFile.filename}
                  </p>
                  <p className="text-gray-700 mt-1">
                    <span className="font-medium">大小：</span>
                    {(pendingFile.content.length / 1024).toFixed(2)} KB
                  </p>
                </div>
              )}
            </div>
          </div>

          <div className="flex justify-end space-x-3 pt-2">
            <button
              onClick={handleCancelImport}
              className="px-4 py-2 text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-1"
              autoFocus
            >
              取消
            </button>
            <button
              onClick={handleConfirmImport}
              className="px-4 py-2 text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors cursor-pointer focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-1"
            >
              确认替换
            </button>
          </div>
        </div>
      </Modal>
    </>
  );
};

export default MarkdownImporter;
