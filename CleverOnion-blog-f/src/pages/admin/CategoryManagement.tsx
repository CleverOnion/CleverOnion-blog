const CategoryManagement = () => {
  return (
    <div className="h-full flex flex-col">
      {/* 顶部工具栏 */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <div className="flex justify-end">
          <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
            新建分类
          </button>
        </div>
      </div>
      
      {/* 分类列表 */}
      <div className="flex-1 p-6">
        <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                分类名称
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                描述
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                文章数量
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
                <div className="flex items-center">
                  <div className="text-sm font-medium text-gray-900">前端开发</div>
                </div>
              </td>
              <td className="px-6 py-4">
                <div className="text-sm text-gray-900">关于前端开发技术的文章</div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                  15 篇
                </span>
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
                <div className="flex items-center">
                  <div className="text-sm font-medium text-gray-900">后端开发</div>
                </div>
              </td>
              <td className="px-6 py-4">
                <div className="text-sm text-gray-900">关于后端开发技术的文章</div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                  8 篇
                </span>
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
                <div className="flex items-center">
                  <div className="text-sm font-medium text-gray-900">数据库</div>
                </div>
              </td>
              <td className="px-6 py-4">
                <div className="text-sm text-gray-900">数据库设计和优化相关文章</div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                  5 篇
                </span>
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
                <div className="flex items-center">
                  <div className="text-sm font-medium text-gray-900">DevOps</div>
                </div>
              </td>
              <td className="px-6 py-4">
                <div className="text-sm text-gray-900">运维和部署相关技术文章</div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-purple-100 text-purple-800">
                  3 篇
                </span>
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
        </div>
      </div>
      
      {/* 新建分类模态框占位 */}
      <div className="mt-6 p-4 bg-gray-50 rounded-lg">
        <p className="text-gray-600 text-center">
          新建/编辑分类的模态框将在这里显示
        </p>
      </div>
    </div>
  );
};

export default CategoryManagement;