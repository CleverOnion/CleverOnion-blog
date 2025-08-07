import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  FiGrid, 
  FiList, 
  FiSearch, 
  FiPlus, 
  FiEdit3, 
  FiEye, 
  FiX, 
  FiMaximize2, 
  FiMinimize2,
  FiCalendar,
  FiTag
} from 'react-icons/fi';

interface Article {
  id: number;
  title: string;
  excerpt: string;
  content: string;
  author: {
    name: string;
    avatar: string;
  };
  category: string;
  tags: string[];
  status: 'published' | 'draft';
  views: number;
  publishedAt: string;
  updatedAt: string;
}

const mockArticles: Article[] = [
  {
    id: 1,
    title: 'React 18 新特性深度解析',
    excerpt: '深入了解 React 18 带来的并发特性、自动批处理、Suspense 改进等重要更新...',
    content: '# React 18 新特性深度解析\n\n## 并发特性\n\nReact 18 引入了并发特性，这是 React 历史上最重要的更新之一。并发特性允许 React 在渲染过程中暂停、恢复或放弃工作，从而提供更好的用户体验。\n\n### 自动批处理\n\n在 React 18 中，所有更新都会自动批处理，包括 Promise、setTimeout 和原生事件处理程序中的更新。这意味着更少的重新渲染和更好的性能。\n\n```javascript\nfunction App() {\n  const [count, setCount] = useState(0);\n  const [flag, setFlag] = useState(false);\n\n  function handleClick() {\n    // React 18 会自动批处理这些更新\n    setCount(c => c + 1);\n    setFlag(f => !f);\n  }\n\n  return (\n    <div>\n      <button onClick={handleClick}>Next</button>\n      <h1 style={{ color: flag ? "blue" : "black" }}>{count}</h1>\n    </div>\n  );\n}\n```\n\n### Suspense 改进\n\nReact 18 对 Suspense 进行了重大改进，现在支持服务端渲染和并发特性。',
    author: {
      name: '张三',
      avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=40&h=40&fit=crop&crop=face'
    },
    category: '前端开发',
    tags: ['React', 'JavaScript', '前端'],
    status: 'published',
    views: 2847,
    publishedAt: '2024-01-15',
    updatedAt: '2024-01-16'
  },
  {
    id: 2,
    title: 'TypeScript 5.0 新特性一览',
    excerpt: 'TypeScript 5.0 带来了装饰器、const 类型参数等激动人心的新特性...',
    content: '# TypeScript 5.0 新特性一览\n\n## 装饰器支持\n\nTypeScript 5.0 正式支持了 ECMAScript 装饰器提案，这是一个期待已久的特性。\n\n```typescript\nfunction logged(target: any, propertyKey: string, descriptor: PropertyDescriptor) {\n  const original = descriptor.value;\n  descriptor.value = function(...args: any[]) {\n    console.log(`Calling ${propertyKey} with`, args);\n    return original.apply(this, args);\n  };\n}\n\nclass Calculator {\n  @logged\n  add(a: number, b: number) {\n    return a + b;\n  }\n}\n```',
    author: {
      name: '李四',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=40&h=40&fit=crop&crop=face'
    },
    category: '编程语言',
    tags: ['TypeScript', 'JavaScript'],
    status: 'draft',
    views: 0,
    publishedAt: '2024-01-14',
    updatedAt: '2024-01-14'
  },
  {
    id: 3,
    title: 'Vue 3 Composition API 最佳实践',
    excerpt: '探索 Vue 3 Composition API 的最佳实践，包括响应式设计、组合函数等...',
    content: '# Vue 3 Composition API 最佳实践\n\n## 响应式基础\n\nComposition API 提供了更灵活的响应式系统。\n\n```javascript\nimport { ref, reactive, computed } from "vue";\n\nexport default {\n  setup() {\n    const count = ref(0);\n    const state = reactive({\n      name: "Vue 3",\n      version: "3.0"\n    });\n\n    const doubleCount = computed(() => count.value * 2);\n\n    function increment() {\n      count.value++;\n    }\n\n    return {\n      count,\n      state,\n      doubleCount,\n      increment\n    };\n  }\n};\n```',
    author: {
      name: '王五',
      avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=40&h=40&fit=crop&crop=face'
    },
    category: '前端开发',
    tags: ['Vue', 'JavaScript', 'Composition API'],
    status: 'published',
    views: 1523,
    publishedAt: '2024-01-13',
    updatedAt: '2024-01-13'
  },
  {
    id: 4,
    title: 'Node.js 性能优化指南',
    excerpt: '全面的 Node.js 性能优化指南，涵盖内存管理、异步处理、缓存策略等...',
    content: '# Node.js 性能优化指南\n\n## 内存管理\n\n### 避免内存泄漏\n\n```javascript\n// 错误示例\nconst cache = {};\nfunction addToCache(key, value) {\n  cache[key] = value; // 可能导致内存泄漏\n}\n\n// 正确示例\nconst cache = new Map();\nfunction addToCache(key, value) {\n  if (cache.size > 1000) {\n    const firstKey = cache.keys().next().value;\n    cache.delete(firstKey);\n  }\n  cache.set(key, value);\n}\n```',
    author: {
      name: '赵六',
      avatar: 'https://images.unsplash.com/photo-1519244703995-f4e0f30006d5?w=40&h=40&fit=crop&crop=face'
    },
    category: '后端开发',
    tags: ['Node.js', 'JavaScript', '性能优化'],
    status: 'published',
    views: 3241,
    publishedAt: '2024-01-12',
    updatedAt: '2024-01-12'
  }
];

type ViewMode = 'grid' | 'list';

const ArticleManagement = () => {
  const [articles] = useState<Article[]>(mockArticles);
  const [viewMode, setViewMode] = useState<ViewMode>('grid');
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const navigate = useNavigate();

  const filteredArticles = articles.filter(article => {
    const matchesSearch = article.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         article.excerpt.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = !statusFilter || article.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleEditArticle = (article: Article) => {
    setSelectedArticle(article);
    setIsFullscreen(false);
  };

  const handleClosePreview = () => {
    setSelectedArticle(null);
    setIsFullscreen(false);
  };

  const toggleFullscreen = () => {
    setIsFullscreen(!isFullscreen);
  };

  const handleNewArticle = () => {
    navigate('/admin/articles/new');
  };

  const renderGridView = () => (
      <div className={`grid gap-6 ${
        selectedArticle && !isFullscreen 
          ? 'grid-cols-1 lg:grid-cols-2' 
          : 'grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4'
      }`}>
        {filteredArticles.map((article) => (
          <div key={article.id} className="group relative bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 hover:-translate-y-1 border border-gray-100/50">
            {/* 卡片顶部装饰条 */}
            <div className={`h-1 w-full ${
              article.status === 'published' 
                ? 'bg-gradient-to-r from-emerald-400 to-emerald-600' 
                : 'bg-gradient-to-r from-amber-400 to-amber-600'
            }`} />
            
            {/* 主要内容区域 */}
            <div className="p-5">
              {/* 状态标签 */}
              <div className="flex justify-between items-start mb-4">
                <div className={`inline-flex items-center px-3 py-1 rounded-lg text-xs font-semibold tracking-wide uppercase ${
                  article.status === 'published' 
                    ? 'bg-emerald-50 text-emerald-700 border border-emerald-200/50' 
                    : 'bg-amber-50 text-amber-700 border border-amber-200/50'
                }`}>
                  {article.status === 'published' ? 'Published' : 'Draft'}
                </div>
                
                {/* 浏览量 */}
                <div className="flex items-center space-x-1 text-gray-500">
                  <FiEye className="w-3.5 h-3.5" />
                  <span className="text-xs font-medium">{article.views.toLocaleString()}</span>
                </div>
              </div>
              
              {/* 文章标题 */}
              <h3 className="text-lg font-bold text-gray-900 mb-3 line-clamp-2 leading-tight group-hover:text-blue-600 transition-colors duration-300">
                {article.title}
              </h3>
              
              {/* 文章摘要 */}
              <p className="text-gray-600 text-sm leading-relaxed mb-4 line-clamp-2">
                {article.excerpt}
              </p>
              
              {/* 标签区域 */}
              <div className="flex flex-wrap gap-1.5 mb-4">
                {article.tags.slice(0, 2).map((tag, index) => (
                  <span key={index} className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-100">
                    #{tag}
                  </span>
                ))}
                {article.tags.length > 2 && (
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-600">+{article.tags.length - 2}</span>
                )}
              </div>
            </div>
            
            {/* 底部信息栏 */}
            <div className="px-5 pb-4 pt-0">
              <div className="flex items-center justify-between pt-3 border-t border-gray-100">
                {/* 作者信息 */}
                <div className="flex items-center space-x-2">
                  <img 
                    src={article.author.avatar} 
                    alt={article.author.name} 
                    className="w-6 h-6 rounded-full object-cover ring-1 ring-gray-200" 
                  />
                  <div>
                    <p className="text-xs font-medium text-gray-900">{article.author.name}</p>
                    <p className="text-xs text-gray-500">{article.publishedAt}</p>
                  </div>
                </div>
                
                {/* 编辑按钮 */}
                <button 
                  onClick={() => handleEditArticle(article)}
                  className="opacity-0 group-hover:opacity-100 transition-all duration-300 p-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg shadow-md hover:shadow-lg transform hover:scale-105"
                  title="编辑文章"
                >
                  <FiEdit3 className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
            
            {/* 悬停时的背景效果 */}
            <div className="absolute inset-0 bg-gradient-to-br from-blue-50/0 to-purple-50/0 group-hover:from-blue-50/20 group-hover:to-purple-50/20 transition-all duration-300 pointer-events-none" />
          </div>
        ))}
      </div>
    );

  const renderListView = () => (
    <div className="bg-white rounded-lg border border-gray-100">
      <div className="overflow-hidden">
        {filteredArticles.map((article, index) => (
          <div key={article.id} className={`flex items-center p-4 hover:bg-gray-50 transition-colors ${
            index !== filteredArticles.length - 1 ? 'border-b border-gray-100' : ''
          }`}>
            {/* 状态指示器 */}
            <div className="flex-shrink-0 mr-4">
              <div className={`w-2 h-2 rounded-full ${
                article.status === 'published' ? 'bg-green-400' : 'bg-amber-400'
              }`} />
            </div>
            
            {/* 文章信息 */}
            <div className="flex-1 min-w-0">
              <div className="flex items-start justify-between">
                <div className="flex-1 min-w-0 mr-4">
                  <h3 className="font-semibold text-gray-900 truncate hover:text-blue-600 transition-colors cursor-pointer">
                    {article.title}
                  </h3>
                  <p className="text-sm text-gray-600 mt-1 line-clamp-1">
                    {article.excerpt}
                  </p>
                  <div className="flex items-center mt-2 text-xs text-gray-500 space-x-4">
                    <div className="flex items-center">
                      <img src={article.author.avatar} alt={article.author.name} className="w-4 h-4 rounded-full mr-1" />
                      {article.author.name}
                    </div>
                    <div className="flex items-center">
                      <FiCalendar className="w-3 h-3 mr-1" />
                      {article.publishedAt}
                    </div>
                    <div className="flex items-center">
                      <FiEye className="w-3 h-3 mr-1" />
                      {article.views.toLocaleString()}
                    </div>
                  </div>
                </div>
                
                {/* 标签 */}
                <div className="flex flex-wrap gap-1 mr-4">
                  {article.tags.slice(0, 3).map((tag, index) => (
                    <span key={index} className="inline-flex items-center px-2 py-0.5 rounded text-xs bg-gray-100 text-gray-600">
                      {tag}
                    </span>
                  ))}
                </div>
                
                {/* 操作按钮 */}
                <div className="flex items-center">
                  <button 
                    onClick={() => handleEditArticle(article)}
                    className="p-2 text-blue-600 bg-white hover:bg-blue-50 rounded-lg shadow-sm hover:shadow-md transition-all duration-200 border border-blue-100 hover:border-blue-200"
                    title="编辑文章"
                  >
                    <FiEdit3 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );

  return (
    <div className="h-full flex flex-col">
      {/* 顶部工具栏 */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-1 bg-gray-100 rounded-lg p-1">
              <button
                onClick={() => setViewMode('grid')}
                className={`p-2 rounded-md transition-colors ${
                  viewMode === 'grid' 
                    ? 'bg-white text-blue-600 shadow-sm' 
                    : 'text-gray-600 hover:text-gray-900'
                }`}
                title="卡片视图"
              >
                <FiGrid className="w-4 h-4" />
              </button>
              <button
                onClick={() => setViewMode('list')}
                className={`p-2 rounded-md transition-colors ${
                  viewMode === 'list' 
                    ? 'bg-white text-blue-600 shadow-sm' 
                    : 'text-gray-600 hover:text-gray-900'
                }`}
                title="列表视图"
              >
                <FiList className="w-4 h-4" />
              </button>
            </div>
          </div>
          
          <div className="flex items-center space-x-3">
            {/* 搜索框 */}
            <div className="relative">
              <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                type="text"
                placeholder="搜索文章..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm w-64"
              />
            </div>
            
            {/* 状态筛选 */}
            <select 
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
            >
              <option value="">全部状态</option>
              <option value="published">已发布</option>
              <option value="draft">草稿</option>
            </select>
            
            {/* 新建按钮 */}
            <button 
              onClick={handleNewArticle}
              className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
            >
              <FiPlus className="w-4 h-4 mr-2" />
              新建文章
            </button>
          </div>
        </div>
      </div>
      
      {/* 主内容区 */}
      <div className="flex-1 flex overflow-hidden">
        {/* 文章列表区域 */}
        <div className={`${selectedArticle && !isFullscreen ? 'w-1/2' : 'w-full'} transition-all duration-300 overflow-y-auto`}>
          <div className="p-6">
            {viewMode === 'grid' ? renderGridView() : renderListView()}
            
            {/* 分页 */}
            <div className="mt-8 flex justify-center">
              <div className="flex items-center space-x-2">
                <button className="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
                  上一页
                </button>
                <button className="px-3 py-2 text-sm font-medium text-white bg-blue-600 border border-blue-600 rounded-lg">
                  1
                </button>
                <button className="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
                  2
                </button>
                <button className="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
                  3
                </button>
                <button className="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
                  下一页
                </button>
              </div>
            </div>
          </div>
        </div>
        
        {/* 文章预览区域 */}
        {selectedArticle && (
          <div className={`${isFullscreen ? 'w-full' : 'w-1/2'} transition-all duration-300 border-l border-gray-200 bg-white flex flex-col`}>
            {/* 预览头部 */}
            <div className="flex items-center justify-between p-4 border-b border-gray-200 bg-gray-50">
              <div className="flex items-center space-x-3">
                <h2 className="font-semibold text-gray-900">文章预览</h2>
                <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                  selectedArticle.status === 'published' 
                    ? 'bg-green-50 text-green-700 border border-green-200' 
                    : 'bg-amber-50 text-amber-700 border border-amber-200'
                }`}>
                  {selectedArticle.status === 'published' ? '已发布' : '草稿'}
                </span>
              </div>
              <div className="flex items-center space-x-2">
                <button 
                  onClick={() => navigate(`/admin/articles/edit/${selectedArticle.id}`)}
                  className="flex items-center px-3 py-1.5 text-sm font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-md transition-colors"
                >
                  <FiEdit3 className="w-3 h-3 mr-1" />
                  编辑
                </button>
                <button 
                  onClick={toggleFullscreen}
                  className="p-1.5 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-md transition-colors"
                  title={isFullscreen ? '退出全屏' : '全屏显示'}
                >
                  {isFullscreen ? <FiMinimize2 className="w-4 h-4" /> : <FiMaximize2 className="w-4 h-4" />}
                </button>
                <button 
                  onClick={handleClosePreview}
                  className="p-1.5 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-md transition-colors"
                  title="关闭预览"
                >
                  <FiX className="w-4 h-4" />
                </button>
              </div>
            </div>
            
            {/* 文章内容 */}
            <div className="flex-1 overflow-y-auto">
              <div className="p-6">
                {/* 文章头部信息 */}
                <div className="mb-8">
                  <h1 className="text-2xl font-bold text-gray-900 mb-4">{selectedArticle.title}</h1>
                  
                  <div className="flex items-center space-x-6 text-sm text-gray-600 mb-4">
                    <div className="flex items-center">
                      <img src={selectedArticle.author.avatar} alt={selectedArticle.author.name} className="w-6 h-6 rounded-full mr-2" />
                      <span>{selectedArticle.author.name}</span>
                    </div>
                    <div className="flex items-center">
                      <FiCalendar className="w-4 h-4 mr-1" />
                      <span>{selectedArticle.publishedAt}</span>
                    </div>
                    <div className="flex items-center">
                      <FiEye className="w-4 h-4 mr-1" />
                      <span>{selectedArticle.views.toLocaleString()} 次浏览</span>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-2 mb-6">
                    <span className="text-sm text-gray-500">分类:</span>
                    <span className="inline-flex items-center px-2 py-1 rounded-md text-sm bg-gray-100 text-gray-700">
                      {selectedArticle.category}
                    </span>
                  </div>
                  
                  <div className="flex flex-wrap gap-2">
                    {selectedArticle.tags.map((tag, index) => (
                      <span key={index} className="inline-flex items-center px-2 py-1 rounded-md text-sm bg-blue-50 text-blue-700 border border-blue-200">
                        <FiTag className="w-3 h-3 mr-1" />
                        {tag}
                      </span>
                    ))}
                  </div>
                </div>
                
                {/* 文章正文 */}
                <div className="prose prose-sm max-w-none">
                  <div className="whitespace-pre-wrap text-gray-700 leading-relaxed">
                    {selectedArticle.content}
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ArticleManagement;