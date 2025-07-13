const ArticleManagement = () => {
  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900">文章管理</h1>
        <div className="flex space-x-3">
          <select className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
            <option value="">全部状态</option>
            <option value="published">已发布</option>
            <option value="draft">草稿</option>
          </select>
          <input
            type="text"
            placeholder="搜索文章..."
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
            搜索
          </button>
          <button className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700">
            新建文章
          </button>
        </div>
      </div>
      
      {/* 文章列表 */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                文章信息
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                作者
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                分类/标签
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                状态
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                浏览量
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                发布时间
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                操作
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            <tr>
              <td className="px-6 py-4">
                <div>
                  <div className="text-sm font-medium text-gray-900">React 入门指南</div>
                  <div className="text-sm text-gray-500">这是一篇关于 React 基础知识的文章...</div>
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="flex items-center">
                  <div className="flex-shrink-0 h-8 w-8">
                    <img className="h-8 w-8 rounded-full" src="https://via.placeholder.com/32" alt="" />
                  </div>
                  <div className="ml-3">
                    <div className="text-sm font-medium text-gray-900">张三</div>
                  </div>
                </div>
              </td>
              <td className="px-6 py-4">
                <div className="text-sm text-gray-900">前端开发</div>
                <div className="flex flex-wrap gap-1 mt-1">
                  <span className="px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded">React</span>
                  <span className="px-2 py-1 text-xs bg-green-100 text-green-800 rounded">JavaScript</span>
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                  已发布
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                1,234
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                2024-01-01
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button className="text-indigo-600 hover:text-indigo-900 mr-3">编辑</button>
                <button className="text-green-600 hover:text-green-900 mr-3">查看</button>
                <button className="text-red-600 hover:text-red-900">删除</button>
              </td>
            </tr>
            
            <tr>
              <td className="px-6 py-4">
                <div>
                  <div className="text-sm font-medium text-gray-900">Vue.js 进阶技巧</div>
                  <div className="text-sm text-gray-500">深入了解 Vue.js 的高级特性和最佳实践...</div>
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="flex items-center">
                  <div className="flex-shrink-0 h-8 w-8">
                    <img className="h-8 w-8 rounded-full" src="https://via.placeholder.com/32" alt="" />
                  </div>
                  <div className="ml-3">
                    <div className="text-sm font-medium text-gray-900">李四</div>
                  </div>
                </div>
              </td>
              <td className="px-6 py-4">
                <div className="text-sm text-gray-900">前端开发</div>
                <div className="flex flex-wrap gap-1 mt-1">
                  <span className="px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded">Vue</span>
                  <span className="px-2 py-1 text-xs bg-green-100 text-green-800 rounded">JavaScript</span>
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                  草稿
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                0
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                2024-01-02
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button className="text-indigo-600 hover:text-indigo-900 mr-3">编辑</button>
                <button className="text-green-600 hover:text-green-900 mr-3">发布</button>
                <button className="text-red-600 hover:text-red-900">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        
        {/* 分页 */}
        <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200">
          <div className="flex-1 flex justify-between">
            <button className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
              上一页
            </button>
            <button className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
              下一页
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ArticleManagement;