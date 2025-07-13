const Home = () => {
  return (
    <div>
      <h1 className="text-3xl font-bold text-gray-900 mb-8">欢迎来到 CleverOnion Blog</h1>
      
      {/* 文章列表区域 */}
      <div className="grid gap-6">
        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-xl font-semibold mb-4">最新文章</h2>
          <p className="text-gray-600">这里将显示最新发布的文章列表...</p>
        </div>
        
        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-xl font-semibold mb-4">热门分类</h2>
          <p className="text-gray-600">这里将显示热门分类...</p>
        </div>
        
        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-xl font-semibold mb-4">热门标签</h2>
          <p className="text-gray-600">这里将显示热门标签...</p>
        </div>
      </div>
    </div>
  );
};

export default Home;