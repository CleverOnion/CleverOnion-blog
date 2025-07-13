import { useParams } from 'react-router-dom';

const Category = () => {
  const { categoryId } = useParams();

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          分类：{categoryId}
        </h1>
        <p className="text-gray-600">浏览该分类下的所有文章</p>
      </div>
      
      {/* 文章列表 */}
      <div className="bg-white rounded-lg shadow-sm">
        <div className="p-6">
          <h2 className="text-xl font-semibold mb-4">文章列表</h2>
          <div className="space-y-4">
            <div className="border-b pb-4">
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                示例文章标题 1
              </h3>
              <p className="text-gray-600 mb-2">
                这里是文章摘要内容...
              </p>
              <div className="flex items-center text-sm text-gray-500">
                <span>2024-01-01</span>
                <span className="mx-2">•</span>
                <span>作者名称</span>
                <span className="mx-2">•</span>
                <span>100 次浏览</span>
              </div>
            </div>
            
            <div className="border-b pb-4">
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                示例文章标题 2
              </h3>
              <p className="text-gray-600 mb-2">
                这里是文章摘要内容...
              </p>
              <div className="flex items-center text-sm text-gray-500">
                <span>2024-01-01</span>
                <span className="mx-2">•</span>
                <span>作者名称</span>
                <span className="mx-2">•</span>
                <span>100 次浏览</span>
              </div>
            </div>
          </div>
          
          {/* 分页 */}
          <div className="mt-6 flex justify-center">
            <p className="text-gray-600">分页组件将在这里...</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Category;