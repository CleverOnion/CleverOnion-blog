import { useParams } from 'react-router-dom';
import ArticleHero from '../components/article/ArticleHero';

const Article = () => {
  const { articleId } = useParams();

  // 模拟文章数据，实际应该从API获取
  const articleData = {
    title: 'React Hooks 深度解析：从入门到精通',
    author: 'CleverOnion',
    publishDate: '2024-01-15',
    readTime: '8 分钟',
    views: 1234,
    tags: ['React', 'Hooks', '前端开发', 'JavaScript']
  };

  return (
    <div className="bg-white">
      {/* 文章Hero区域 */}
      <ArticleHero
        title={articleData.title}
        author={articleData.author}
        publishDate={articleData.publishDate}
        readTime={articleData.readTime}
        views={articleData.views}
        tags={articleData.tags}
      />

      {/* 文章内容区域 */}
      <div className="max-w-4xl mx-auto px-4 py-12">
        {/* 文章内容 */}
        <article className="bg-white rounded-lg shadow-sm p-8 mb-12">
          <div className="prose max-w-none">
            <p className="text-gray-700 leading-relaxed mb-6 text-lg">
              这里是文章的正文内容。在实际应用中，这里会显示从后端获取的 Markdown 渲染后的 HTML 内容。
            </p>
            <p className="text-gray-700 leading-relaxed mb-6 text-lg">
              文章内容支持 Markdown 格式，包括标题、列表、代码块、图片等各种元素。通过精心设计的排版和样式，为读者提供最佳的阅读体验。
            </p>
            <h2 className="text-3xl font-semibold text-gray-900 mt-8 mb-6">深入理解 React Hooks</h2>
            <p className="text-gray-700 leading-relaxed mb-6 text-lg">
              React Hooks 是 React 16.8 引入的新特性，它让你在不编写 class 的情况下使用 state 以及其他的 React 特性。这一革命性的变化彻底改变了我们编写 React 组件的方式。
            </p>
            <h3 className="text-2xl font-semibold text-gray-900 mt-6 mb-4">useState Hook</h3>
            <p className="text-gray-700 leading-relaxed mb-6 text-lg">
              useState 是最基础也是最常用的 Hook，它让函数组件能够拥有自己的状态。通过简洁的 API 设计，我们可以轻松地管理组件的状态变化。
            </p>
          </div>
        </article>

        {/* 评论区域 */}
        <section className="bg-white rounded-lg shadow-sm p-8">
          <h3 className="text-2xl font-semibold text-gray-900 mb-8">评论区</h3>
          
          {/* 发表评论表单 */}
          <div className="mb-8">
            <textarea 
              className="w-full p-4 border border-gray-300 rounded-lg resize-none focus:ring-2 focus:ring-pink-500 focus:border-transparent"
              rows={4}
              placeholder="写下你的评论..."
            ></textarea>
            <button className="mt-3 px-6 py-3 bg-pink-500 text-white rounded-lg hover:bg-pink-600 transition-colors duration-200 font-medium">
              发表评论
            </button>
          </div>
          
          {/* 评论列表 */}
          <div className="space-y-6">
            <div className="border-b border-gray-100 pb-6">
              <div className="flex items-center mb-3">
                <div className="w-10 h-10 bg-gradient-to-br from-pink-400 to-purple-500 rounded-full flex items-center justify-center text-white font-medium mr-3">
                  用
                </div>
                <div>
                  <span className="font-medium text-gray-900">用户名</span>
                  <span className="ml-3 text-sm text-gray-500">2024-01-01 10:00</span>
                </div>
              </div>
              <p className="text-gray-700 leading-relaxed ml-13">这是一条示例评论内容，感谢作者的精彩分享！这篇文章对我理解 React Hooks 很有帮助。</p>
            </div>
            
            <div className="border-b border-gray-100 pb-6">
              <div className="flex items-center mb-3">
                <div className="w-10 h-10 bg-gradient-to-br from-blue-400 to-cyan-500 rounded-full flex items-center justify-center text-white font-medium mr-3">
                  另
                </div>
                <div>
                  <span className="font-medium text-gray-900">另一个用户</span>
                  <span className="ml-3 text-sm text-gray-500">2024-01-01 11:00</span>
                </div>
              </div>
              <p className="text-gray-700 leading-relaxed ml-13">非常详细的教程，期待更多关于 React 的深度文章！</p>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
};

export default Article;