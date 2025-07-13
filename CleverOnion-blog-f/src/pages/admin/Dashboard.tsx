const AdminDashboard = () => {
  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">仪表盘</h1>
      
      {/* 统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <div className="p-2 bg-blue-100 rounded-lg">
              <span className="text-2xl">📝</span>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">总文章数</p>
              <p className="text-2xl font-semibold text-gray-900">128</p>
            </div>
          </div>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <div className="p-2 bg-green-100 rounded-lg">
              <span className="text-2xl">👥</span>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">总用户数</p>
              <p className="text-2xl font-semibold text-gray-900">45</p>
            </div>
          </div>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <div className="p-2 bg-yellow-100 rounded-lg">
              <span className="text-2xl">💬</span>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">总评论数</p>
              <p className="text-2xl font-semibold text-gray-900">324</p>
            </div>
          </div>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <div className="p-2 bg-purple-100 rounded-lg">
              <span className="text-2xl">👁️</span>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">总浏览量</p>
              <p className="text-2xl font-semibold text-gray-900">12.5K</p>
            </div>
          </div>
        </div>
      </div>
      
      {/* 最近活动 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">最新文章</h2>
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-gray-900">示例文章标题 1</span>
              <span className="text-sm text-gray-500">2024-01-01</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-900">示例文章标题 2</span>
              <span className="text-sm text-gray-500">2024-01-01</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-900">示例文章标题 3</span>
              <span className="text-sm text-gray-500">2024-01-01</span>
            </div>
          </div>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">最新评论</h2>
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-gray-900">用户A 发表了评论</span>
              <span className="text-sm text-gray-500">2小时前</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-900">用户B 发表了评论</span>
              <span className="text-sm text-gray-500">4小时前</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-900">用户C 发表了评论</span>
              <span className="text-sm text-gray-500">6小时前</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;