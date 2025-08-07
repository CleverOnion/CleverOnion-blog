const TagManagement = () => {
  return (
    <div className="h-full flex flex-col">
      {/* 顶部工具栏 */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <div className="flex justify-end items-center space-x-3">
          <input
            type="text"
            placeholder="搜索标签..."
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
            新建标签
          </button>
        </div>
      </div>
      
      {/* 主要内容区域 */}
      <div className="flex-1 p-6 overflow-y-auto">
        {/* 标签统计 */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <div className="p-2 bg-blue-100 rounded-lg">
              <span className="text-2xl">🏷️</span>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">总标签数</p>
              <p className="text-2xl font-semibold text-gray-900">24</p>
            </div>
          </div>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <div className="p-2 bg-green-100 rounded-lg">
              <span className="text-2xl">📈</span>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">使用中标签</p>
              <p className="text-2xl font-semibold text-gray-900">18</p>
            </div>
          </div>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <div className="p-2 bg-yellow-100 rounded-lg">
              <span className="text-2xl">🔥</span>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">热门标签</p>
              <p className="text-2xl font-semibold text-gray-900">React</p>
            </div>
          </div>
        </div>
      </div>
      
      {/* 标签列表 */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                标签名称
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                颜色
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                使用次数
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                创建时间
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                操作
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            <tr>
              <td className="px-6 py-4 whitespace-nowrap">
                <span className="px-3 py-1 text-sm font-medium bg-blue-100 text-blue-800 rounded-full">
                  React
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="flex items-center">
                  <div className="w-4 h-4 bg-blue-500 rounded mr-2"></div>
                  <span className="text-sm text-gray-900">#3B82F6</span>
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                12
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                2024-01-01
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button className="text-indigo-600 hover:text-indigo-900 mr-3">编辑</button>
                <button className="text-green-600 hover:text-green-900 mr-3">查看文章</button>
                <button className="text-red-600 hover:text-red-900">删除</button>
              </td>
            </tr>
            
            <tr>
              <td className="px-6 py-4 whitespace-nowrap">
                <span className="px-3 py-1 text-sm font-medium bg-green-100 text-green-800 rounded-full">
                  JavaScript
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="flex items-center">
                  <div className="w-4 h-4 bg-green-500 rounded mr-2"></div>
                  <span className="text-sm text-gray-900">#10B981</span>
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                18
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                2024-01-01
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button className="text-indigo-600 hover:text-indigo-900 mr-3">编辑</button>
                <button className="text-green-600 hover:text-green-900 mr-3">查看文章</button>
                <button className="text-red-600 hover:text-red-900">删除</button>
              </td>
            </tr>
            
            <tr>
              <td className="px-6 py-4 whitespace-nowrap">
                <span className="px-3 py-1 text-sm font-medium bg-purple-100 text-purple-800 rounded-full">
                  TypeScript
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="flex items-center">
                  <div className="w-4 h-4 bg-purple-500 rounded mr-2"></div>
                  <span className="text-sm text-gray-900">#8B5CF6</span>
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                8
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                2024-01-02
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button className="text-indigo-600 hover:text-indigo-900 mr-3">编辑</button>
                <button className="text-green-600 hover:text-green-900 mr-3">查看文章</button>
                <button className="text-red-600 hover:text-red-900">删除</button>
              </td>
            </tr>
            
            <tr>
              <td className="px-6 py-4 whitespace-nowrap">
                <span className="px-3 py-1 text-sm font-medium bg-yellow-100 text-yellow-800 rounded-full">
                  Vue.js
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="flex items-center">
                  <div className="w-4 h-4 bg-yellow-500 rounded mr-2"></div>
                  <span className="text-sm text-gray-900">#F59E0B</span>
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                6
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                2024-01-03
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button className="text-indigo-600 hover:text-indigo-900 mr-3">编辑</button>
                <button className="text-green-600 hover:text-green-900 mr-3">查看文章</button>
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
    </div>
  );
};

export default TagManagement;