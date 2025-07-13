import { useParams } from 'react-router-dom';

const Article = () => {
  const { articleId } = useParams();

  return (
    <div className="max-w-4xl mx-auto">
      {/* 文章头部 */}
      <header className="mb-8">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">
          文章标题 (ID: {articleId})
        </h1>
        <div className="flex items-center text-gray-600 mb-4">
          <span>发布时间：2024-01-01</span>
          <span className="mx-2">•</span>
          <span>作者：示例作者</span>
          <span className="mx-2">•</span>
          <span>浏览量：100</span>
        </div>
        <div className="flex flex-wrap gap-2">
          <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm">
            示例标签1
          </span>
          <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm">
            示例标签2
          </span>
        </div>
      </header>

      {/* 文章内容 */}
      <article className="bg-white rounded-lg shadow-sm p-8 mb-8">
        <div className="prose max-w-none">
          <p className="text-gray-700 leading-relaxed mb-4">
            这里是文章的正文内容。在实际应用中，这里会显示从后端获取的 Markdown 渲染后的 HTML 内容。
          </p>
          <p className="text-gray-700 leading-relaxed mb-4">
            文章内容支持 Markdown 格式，包括标题、列表、代码块、图片等各种元素。
          </p>
          <h2 className="text-2xl font-semibold text-gray-900 mt-6 mb-4">示例小标题</h2>
          <p className="text-gray-700 leading-relaxed">
            更多文章内容...
          </p>
        </div>
      </article>

      {/* 评论区域 */}
      <section className="bg-white rounded-lg shadow-sm p-6">
        <h3 className="text-xl font-semibold text-gray-900 mb-6">评论区</h3>
        
        {/* 发表评论表单 */}
        <div className="mb-6">
          <textarea 
            className="w-full p-3 border border-gray-300 rounded-lg resize-none"
            rows={4}
            placeholder="写下你的评论..."
          ></textarea>
          <button className="mt-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
            发表评论
          </button>
        </div>
        
        {/* 评论列表 */}
        <div className="space-y-4">
          <div className="border-b pb-4">
            <div className="flex items-center mb-2">
              <span className="font-medium text-gray-900">用户名</span>
              <span className="ml-2 text-sm text-gray-500">2024-01-01 10:00</span>
            </div>
            <p className="text-gray-700">这是一条示例评论内容...</p>
          </div>
          
          <div className="border-b pb-4">
            <div className="flex items-center mb-2">
              <span className="font-medium text-gray-900">另一个用户</span>
              <span className="ml-2 text-sm text-gray-500">2024-01-01 11:00</span>
            </div>
            <p className="text-gray-700">这是另一条示例评论内容...</p>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Article;