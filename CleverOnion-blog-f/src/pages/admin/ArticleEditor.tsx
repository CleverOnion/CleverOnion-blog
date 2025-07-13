import { useParams } from 'react-router-dom';

const ArticleEditor = () => {
  const { articleId } = useParams();
  const isEdit = !!articleId;

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900">
          {isEdit ? `编辑文章 (ID: ${articleId})` : '发布新文章'}
        </h1>
        <div className="flex space-x-3">
          <button className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50">
            保存草稿
          </button>
          <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
            预览
          </button>
          <button className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700">
            发布文章
          </button>
        </div>
      </div>
      
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 主要编辑区域 */}
        <div className="lg:col-span-2 space-y-6">
          {/* 文章标题 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              文章标题
            </label>
            <input
              type="text"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="请输入文章标题..."
              defaultValue={isEdit ? '示例文章标题' : ''}
            />
          </div>
          
          {/* 文章摘要 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              文章摘要
            </label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
              rows={3}
              placeholder="请输入文章摘要..."
              defaultValue={isEdit ? '这是一篇示例文章的摘要内容...' : ''}
            ></textarea>
          </div>
          
          {/* Markdown 编辑器 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex justify-between items-center mb-4">
              <label className="block text-sm font-medium text-gray-700">
                文章内容 (Markdown)
              </label>
              <div className="flex space-x-2">
                <button className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200">
                  编辑
                </button>
                <button className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200">
                  预览
                </button>
                <button className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200">
                  分屏
                </button>
              </div>
            </div>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none font-mono"
              rows={20}
              placeholder="请输入 Markdown 内容...\n\n# 标题\n\n这里是正文内容..."
              defaultValue={isEdit ? '# 示例文章\n\n这是一篇示例文章的内容...\n\n## 小标题\n\n更多内容...' : ''}
            ></textarea>
          </div>
        </div>
        
        {/* 侧边栏设置 */}
        <div className="space-y-6">
          {/* 发布设置 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">发布设置</h3>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  文章状态
                </label>
                <select className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
                  <option value="draft">草稿</option>
                  <option value="published">已发布</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  发布时间
                </label>
                <input
                  type="datetime-local"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
          </div>
          
          {/* 分类设置 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">分类</h3>
            <select className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
              <option value="">选择分类</option>
              <option value="frontend">前端开发</option>
              <option value="backend">后端开发</option>
              <option value="database">数据库</option>
              <option value="devops">DevOps</option>
            </select>
          </div>
          
          {/* 标签设置 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">标签</h3>
            <input
              type="text"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 mb-3"
              placeholder="输入标签，按回车添加"
            />
            <div className="flex flex-wrap gap-2">
              <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm flex items-center">
                React
                <button className="ml-2 text-blue-600 hover:text-blue-800">
                  ×
                </button>
              </span>
              <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm flex items-center">
                JavaScript
                <button className="ml-2 text-green-600 hover:text-green-800">
                  ×
                </button>
              </span>
            </div>
          </div>
          
          {/* 特色图片 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">特色图片</h3>
            <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
              <div className="text-gray-400 mb-2">
                <svg className="mx-auto h-12 w-12" stroke="currentColor" fill="none" viewBox="0 0 48 48">
                  <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </div>
              <p className="text-sm text-gray-600 mb-2">点击上传或拖拽图片到此处</p>
              <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm">
                选择图片
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ArticleEditor;