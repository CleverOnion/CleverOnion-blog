import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
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
  FiTag,
  FiTrash2,
  FiFilter
} from 'react-icons/fi';
import { articleApi, type Article, type ArticleQueryParams } from '../../api/articles';
import { tagApi, type TagWithCount } from '../../api/tags';
import { Loading, SkeletonLoading } from '../../components/ui/Loading';
import Select from '../../components/ui/Select';
import { Modal, useToast } from '../../components/ui';

// 简单的Button组件
const Button: React.FC<{
  children: React.ReactNode;
  variant?: 'default' | 'outline';
  size?: 'sm' | 'md';
  onClick?: () => void;
  className?: string;
}> = ({ children, variant = 'default', size = 'md', onClick, className = '' }) => {
  const baseClasses = 'inline-flex items-center justify-center font-medium rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 cursor-pointer';
  
  const variantClasses = {
    default: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500',
    outline: 'border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 focus:ring-blue-500'
  };
  
  const sizeClasses = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm'
  };
  
  return (
    <button
      onClick={onClick}
      className={`${baseClasses} ${variantClasses[variant]} ${sizeClasses[size]} ${className}`}
    >
      {children}
    </button>
  );
};

// 分类和标签的模拟数据
interface Category {
  id: number;
  name: string;
}

const mockCategories: Category[] = [
  { id: 1, name: '前端开发' },
  { id: 2, name: '后端开发' },
  { id: 3, name: '数据库' },
  { id: 4, name: 'DevOps' }
];

type ViewMode = 'grid' | 'list';

const ArticleManagement = () => {
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState<ViewMode>('grid');
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalCount, setTotalCount] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [categoryFilter, setCategoryFilter] = useState<number | undefined>(undefined);
  const [tagFilter, setTagFilter] = useState<number | undefined>(undefined);
  const [categories, setCategories] = useState<Category[]>(mockCategories);
  const [tags, setTags] = useState<TagWithCount[]>([]);
  const [selectedArticles, setSelectedArticles] = useState<Set<string>>(new Set());
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<{ type: 'single' | 'batch'; id?: string; count?: number }>({ type: 'single' });
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const toast = useToast();

  // 加载文章列表
  const loadArticles = async (page: number = 0) => {
    try {
      setLoading(true);
      const params: ArticleQueryParams = {
        page,
        size: pageSize,
        status: statusFilter || undefined,
        categoryId: categoryFilter,
        tagId: tagFilter
      };
      
      const response = await articleApi.getAllArticles(params);
      setArticles(response.articles);
      setTotalCount(response.total_count);
      setCurrentPage(response.page);
      setTotalPages(response.total_pages);
    } catch (error) {
      console.error('加载文章失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 加载标签列表
  const loadTags = async () => {
    try {
      const response = await tagApi.getTagsWithCount(0, 100);
      setTags(response.tags);
    } catch (error) {
      console.error('加载标签失败:', error);
    }
  };

  // 搜索文章
  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      setCurrentPage(0);
      loadArticles(0);
      return;
    }
    
    try {
      setLoading(true);
      const params: ArticleQueryParams = {
        page: 0,
        size: pageSize,
        categoryId: categoryFilter,
        tagId: tagFilter
      };
      
      const response = await articleApi.searchArticles(searchTerm, params);
      setArticles(response.articles);
      setTotalCount(response.total_count);
      setCurrentPage(0);
      setTotalPages(response.total_pages);
    } catch (error) {
      console.error('搜索文章失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 删除文章
  const handleDeleteArticle = async (id: string) => {
    setDeleteTarget({ type: 'single', id });
    setShowDeleteModal(true);
  };

  // 确认删除
  const handleConfirmDelete = async () => {
    try {
      if (deleteTarget.type === 'single' && deleteTarget.id) {
        await articleApi.deleteArticle(deleteTarget.id);
        const newPage = articles.length === 1 && currentPage > 0 ? currentPage - 1 : currentPage;
        await loadArticles(newPage);
        toast.success('文章删除成功');
      } else if (deleteTarget.type === 'batch') {
        await articleApi.batchDeleteArticles(Array.from(selectedArticles));
        setSelectedArticles(new Set());
        await loadArticles(currentPage);
        toast.success(`成功删除 ${deleteTarget.count} 篇文章`);
      }
    } catch (error) {
      console.error('删除文章失败:', error);
      toast.error('删除失败，请重试');
    } finally {
      setShowDeleteModal(false);
    }
  };

  // 批量删除文章
  const handleBatchDelete = async () => {
    if (selectedArticles.size === 0) return;
    setDeleteTarget({ type: 'batch', count: selectedArticles.size });
    setShowDeleteModal(true);
  };

  // 继续处理批量删除的剩余逻辑
  const handleBatchDeleteContinue = async () => {
    try {
      await articleApi.batchDeleteArticles(Array.from(selectedArticles));
      setSelectedArticles(new Set());
      await loadArticles(currentPage);
    } catch (error) {
      console.error('批量删除失败:', error);
    }
  };

  // 更新文章状态
  const handleUpdateStatus = async (id: string, status: 'PUBLISHED' | 'DRAFT' | 'ARCHIVED') => {
    try {
      await articleApi.updateArticle(id, { status });
      await loadArticles(currentPage);
    } catch (error) {
      console.error('更新文章状态失败:', error);
    }
  };

  // 编辑文章
  const handleEditArticle = (article: Article) => {
    navigate(`/admin/articles/edit/${article.id}`);
  };

  // 预览文章
  const handlePreviewArticle = (article: Article) => {
    window.open(`/article/${article.id}`, '_blank');
  };

  useEffect(() => {
    if (!searchTerm.trim()) {
      loadArticles(currentPage);
    }
  }, [currentPage, statusFilter, categoryFilter, tagFilter]);

  // 处理URL参数
  useEffect(() => {
    const categoryIdParam = searchParams.get('categoryId');
    const tagIdParam = searchParams.get('tagId');
    
    if (categoryIdParam) {
      setCategoryFilter(Number(categoryIdParam));
    }
    
    if (tagIdParam) {
      setTagFilter(Number(tagIdParam));
    }
  }, [searchParams]);

  useEffect(() => {
    loadArticles(0);
    loadTags();
  }, []);

  // API已经处理了所有筛选，直接使用返回的文章列表
  const filteredArticles = articles;

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
            {/* 选择框 */}
            <div className="absolute top-3 left-3 z-10">
              <input
                type="checkbox"
                checked={selectedArticles.has(article.id)}
                onChange={(e) => {
                  const newSelected = new Set(selectedArticles);
                  if (e.target.checked) {
                    newSelected.add(article.id);
                  } else {
                    newSelected.delete(article.id);
                  }
                  setSelectedArticles(newSelected);
                }}
                className="w-4 h-4 text-blue-600 bg-white border-gray-300 rounded focus:ring-blue-500 focus:ring-2"
              />
            </div>
            {/* 卡片顶部装饰条 */}
            <div className={`h-1 w-full ${
              article.status === 'PUBLISHED' 
                ? 'bg-gradient-to-r from-emerald-400 to-emerald-600' 
                : 'bg-gradient-to-r from-amber-400 to-amber-600'
            }`} />
            
            {/* 主要内容区域 */}
            <div className="p-5">
              {/* 状态标签和分类 */}
              <div className="flex justify-between items-start mb-4">
                <div className="flex items-center gap-2 flex-wrap">
                  <div className={`inline-flex items-center px-3 py-1 rounded-lg text-xs font-semibold tracking-wide uppercase ${
                    article.status === 'PUBLISHED' 
                      ? 'bg-emerald-50 text-emerald-700 border border-emerald-200/50' 
                      : article.status === 'ARCHIVED'
                      ? 'bg-gray-50 text-gray-700 border border-gray-200/50'
                      : 'bg-amber-50 text-amber-700 border border-amber-200/50'
                  }`}>
                    {article.status === 'PUBLISHED' ? 'Published' : article.status === 'ARCHIVED' ? 'Archived' : 'Draft'}
                  </div>
                  
                  {/* 分类显示 */}
                  {article.category && (
                    <span className="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium bg-purple-50 text-purple-700 border border-purple-200/50">
                      <FiTag className="w-3 h-3 mr-1" />
                      {typeof article.category === 'string' ? article.category : article.category?.name || ''}
                    </span>
                  )}
                </div>
                
                {/* 浏览量 */}
                <div className="flex items-center space-x-1 text-gray-500">
                  <FiEye className="w-3.5 h-3.5" />
                  <span className="text-xs font-medium">{(article.views || 0).toLocaleString()}</span>
                </div>
              </div>
              
              {/* 文章标题 */}
              <h3 className="text-lg font-bold text-gray-900 mb-3 line-clamp-2 leading-tight group-hover:text-blue-600 transition-colors duration-300">
                {article.title}
              </h3>
              
              {/* 文章摘要 */}
              <p className="text-gray-600 text-sm leading-relaxed mb-4 line-clamp-2">
                {article.excerpt || article.summary || '暂无摘要'}
              </p>
              
              {/* 标签区域 */}
              <div className="mb-4">
                <div className="flex flex-wrap gap-1.5">
                  {(article.tags || []).slice(0, 2).map((tag, index) => (
                    <span key={index} className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-100">
                      #{typeof tag === 'string' ? tag : tag.name || ''}
                    </span>
                  ))}
                  {(article.tags || []).length > 2 && (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-600">+{(article.tags || []).length - 2}</span>
                  )}
                </div>
              </div>
            </div>
            
            {/* 底部信息栏 */}
            <div className="px-5 pb-4 pt-0">
              <div className="flex items-center justify-between pt-3 border-t border-gray-100">
                {/* 作者信息 */}
                <div className="flex items-center space-x-2">
                  <img 
                    src={article.author?.avatar_url || article.author?.avatar || '/default-avatar.svg'} 
                    alt={article.author?.username || '未知作者'} 
                    className="w-6 h-6 rounded-full object-cover ring-1 ring-gray-200" 
                  />
                  <div>
                    <p className="text-xs font-medium text-gray-900">{article.author?.username || '未知作者'}</p>
                    <p className="text-xs text-gray-500">{article.published_at}</p>
                  </div>
                </div>
                
                {/* 操作按钮 */}
                <div className="opacity-0 group-hover:opacity-100 transition-all duration-300 flex items-center space-x-2">
                  <button 
                     onClick={() => handlePreviewArticle(article)}
                     className="p-2 text-gray-600 bg-white hover:bg-gray-50 rounded-lg shadow-sm hover:shadow-md transition-all duration-200 border border-gray-200 cursor-pointer"
                     title="预览文章"
                   >
                     <FiEye className="w-3.5 h-3.5" />
                   </button>
                  <button 
                    onClick={() => handleEditArticle(article)}
                    className="p-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg shadow-md hover:shadow-lg transform hover:scale-105 cursor-pointer"
                    title="编辑文章"
                  >
                    <FiEdit3 className="w-3.5 h-3.5" />
                  </button>
                  <button 
                    onClick={() => handleDeleteArticle(article.id)}
                    className="p-2 text-red-600 bg-white hover:bg-red-50 rounded-lg shadow-sm hover:shadow-md transition-all duration-200 border border-red-100 hover:border-red-200 cursor-pointer"
                    title="删除文章"
                  >
                    <FiTrash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
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
            {/* 选择框 */}
            <div className="flex-shrink-0 mr-4">
              <input
                type="checkbox"
                checked={selectedArticles.has(article.id)}
                onChange={(e) => {
                  const newSelected = new Set(selectedArticles);
                  if (e.target.checked) {
                    newSelected.add(article.id);
                  } else {
                    newSelected.delete(article.id);
                  }
                  setSelectedArticles(newSelected);
                }}
                className="w-4 h-4 text-blue-600 bg-white border-gray-300 rounded focus:ring-blue-500 focus:ring-2"
              />
            </div>
            {/* 状态指示器 */}
            <div className="flex-shrink-0 mr-4">
              <div className={`w-2 h-2 rounded-full ${
                article.status === 'PUBLISHED' ? 'bg-green-400' : article.status === 'ARCHIVED' ? 'bg-gray-400' : 'bg-amber-400'
              }`} />
            </div>
            
            {/* 文章信息 */}
            <div className="flex-1 min-w-0">
              <div className="flex items-start justify-between">
                <div className="flex-1 min-w-0 mr-4">
                  <div className="flex items-center gap-2 mb-1">
                    <h3 className="font-semibold text-gray-900 truncate hover:text-blue-600 transition-colors cursor-pointer flex-1">
                      {article.title}
                    </h3>
                    {/* 分类显示 */}
                    {article.category && (
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-purple-50 text-purple-700 border border-purple-200/50 flex-shrink-0">
                        <FiTag className="w-3 h-3 mr-1" />
                        {typeof article.category === 'string' ? article.category : article.category?.name || ''}
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-gray-600 mt-1 line-clamp-1">
                    {article.excerpt || article.summary || '暂无摘要'}
                  </p>
                  <div className="flex items-center mt-2 text-xs text-gray-500 space-x-4">
                    <div className="flex items-center">
                      <img src={article.author?.avatar_url || article.author?.avatar || '/default-avatar.svg'} alt={article.author?.username || '未知作者'} className="w-4 h-4 rounded-full mr-1" />
                      {article.author?.username || '未知作者'}
                    </div>
                    <div className="flex items-center">
                      <FiCalendar className="w-3 h-3 mr-1" />
                      {article.publishedAt || article.published_at || '未发布'}
                    </div>
                    <div className="flex items-center">
                      <FiEye className="w-3 h-3 mr-1" />
                      {(article.views || 0).toLocaleString()}
                    </div>
                  </div>
                </div>
                
                {/* 标签 */}
                <div className="flex flex-wrap gap-1 mr-4">
                  {(article.tags || []).slice(0, 3).map((tag, index) => (
                    <span key={index} className="inline-flex items-center px-2 py-0.5 rounded text-xs bg-gray-100 text-gray-600">
                      {typeof tag === 'string' ? tag : tag.name || ''}
                    </span>
                  ))}
                </div>
                
                {/* 操作按钮 */}
                 <div className="flex items-center space-x-2">
                   <button 
                     onClick={() => handlePreviewArticle(article)}
                     className="p-2 text-gray-600 bg-white hover:bg-gray-50 rounded-lg shadow-sm hover:shadow-md transition-all duration-200 border border-gray-200 cursor-pointer"
                     title="预览文章"
                   >
                     <FiEye className="w-4 h-4" />
                   </button>
                   <button 
                     onClick={() => handleEditArticle(article)}
                     className="p-2 text-blue-600 bg-white hover:bg-blue-50 rounded-lg shadow-sm hover:shadow-md transition-all duration-200 border border-blue-100 hover:border-blue-200 cursor-pointer"
                     title="编辑文章"
                   >
                     <FiEdit3 className="w-4 h-4" />
                   </button>
                   <button 
                     onClick={() => handleDeleteArticle(article.id)}
                     className="p-2 text-red-600 bg-white hover:bg-red-50 rounded-lg shadow-sm hover:shadow-md transition-all duration-200 border border-red-100 hover:border-red-200 cursor-pointer"
                     title="删除文章"
                   >
                     <FiTrash2 className="w-4 h-4" />
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
                className={`p-2 rounded-md transition-colors cursor-pointer ${
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
                className={`p-2 rounded-md transition-colors cursor-pointer ${
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
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                className="pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm w-64"
              />
              {searchTerm && (
                <button
                  onClick={handleSearch}
                  disabled={loading}
                  className="absolute right-2 top-1/2 transform -translate-y-1/2 px-2 py-1 text-xs bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? '搜索中...' : '搜索'}
                </button>
              )}
            </div>
            
            {/* 分类筛选 */}
            <Select
              value={categoryFilter?.toString() || ''}
              onChange={(value) => setCategoryFilter(value ? Number(value) : undefined)}
              options={[
                { value: '', label: '全部分类' },
                ...categories.map(category => ({
                  value: category.id.toString(),
                  label: category.name
                }))
              ]}
              placeholder="全部分类"
              className="w-32"
            />
            
            {/* 标签筛选 */}
            <Select
              value={tagFilter?.toString() || ''}
              onChange={(value) => setTagFilter(value ? Number(value) : undefined)}
              options={[
                { value: '', label: '全部标签' },
                ...tags.map(tag => ({
                  value: tag.id.toString(),
                  label: `${tag.name} (${tag.articleCount})`
                }))
              ]}
              placeholder="全部标签"
              className="w-36"
            />
            
            {/* 状态筛选 */}
            <Select
              value={statusFilter}
              onChange={(value) => setStatusFilter(value)}
              options={[
                { value: '', label: '全部状态' },
                { value: 'PUBLISHED', label: '已发布' },
                { value: 'DRAFT', label: '草稿' },
                { value: 'ARCHIVED', label: '已归档' }
              ]}
              placeholder="全部状态"
              className="w-28"
            />
            
            {/* 新建按钮 */}
            <button 
              onClick={handleNewArticle}
              className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium cursor-pointer"
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
            {loading ? (
              viewMode === 'grid' ? (
                <div className={`grid gap-6 ${
                  selectedArticle && !isFullscreen 
                    ? 'grid-cols-1 lg:grid-cols-2' 
                    : 'grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4'
                }`}>
                  <SkeletonLoading rows={6} />
                </div>
              ) : (
                <SkeletonLoading rows={8} />
              )
            ) : filteredArticles.length === 0 ? (
              <div className="text-center py-12">
                <div className="text-gray-400 text-lg mb-2">暂无文章</div>
                <p className="text-gray-500 text-sm">点击右上角的"新建文章"按钮开始创作</p>
              </div>
            ) : (
              viewMode === 'grid' ? renderGridView() : renderListView()
            )}
            
            {/* 批量操作栏 */}
            {selectedArticles.size > 0 && (
              <div className="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4">
                    <span className="text-sm font-medium text-blue-900">
                      已选择 {selectedArticles.size} 篇文章
                    </span>
                    <button
                      onClick={() => setSelectedArticles(new Set())}
                      className="text-sm text-blue-600 hover:text-blue-800 transition-colors"
                    >
                      取消选择
                    </button>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleBatchDelete()}
                      className="text-red-600 border-red-200 hover:bg-red-50"
                    >
                      <FiTrash2 className="w-4 h-4 mr-1" />
                      批量删除
                    </Button>
                  </div>
                </div>
              </div>
            )}

            {/* 分页 */}
            {totalPages > 1 && (
              <div className="mt-8 flex justify-center">
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => loadArticles(currentPage - 1)}
                    disabled={currentPage === 0}
                    className="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    上一页
                  </button>
                  
                  {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                    const pageNum = Math.max(0, Math.min(currentPage - 2, totalPages - 5)) + i;
                    return (
                      <button
                        key={pageNum}
                        onClick={() => loadArticles(pageNum)}
                        className={`px-3 py-2 text-sm font-medium rounded-lg transition-colors cursor-pointer ${
                          pageNum === currentPage
                            ? 'text-white bg-blue-600 border border-blue-600'
                            : 'text-gray-700 bg-white border border-gray-200 hover:bg-gray-50'
                        }`}
                      >
                        {pageNum + 1}
                      </button>
                    );
                  })}
                  
                  <button
                    onClick={() => loadArticles(currentPage + 1)}
                    disabled={currentPage >= totalPages - 1}
                    className="px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    下一页
                  </button>
                </div>
                
                <div className="ml-6 flex items-center text-sm text-gray-600">
                  <span>共 {totalCount} 篇文章，第 {currentPage + 1} / {totalPages} 页</span>
                </div>
              </div>
            )}
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
                  selectedArticle.status === 'PUBLISHED' 
                    ? 'bg-green-50 text-green-700 border border-green-200' 
                    : 'bg-amber-50 text-amber-700 border border-amber-200'
                }`}>
                  {selectedArticle.status === 'PUBLISHED' ? '已发布' : '草稿'}
                </span>
              </div>
              <div className="flex items-center space-x-2">
                <button 
                  onClick={() => navigate(`/admin/articles/edit/${selectedArticle.id}`)}
                  className="flex items-center px-3 py-1.5 text-sm font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-md transition-colors cursor-pointer"
                >
                  <FiEdit3 className="w-3 h-3 mr-1" />
                  编辑
                </button>
                <button 
                  onClick={toggleFullscreen}
                  className="p-1.5 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-md transition-colors cursor-pointer"
                  title={isFullscreen ? '退出全屏' : '全屏显示'}
                >
                  {isFullscreen ? <FiMinimize2 className="w-4 h-4" /> : <FiMaximize2 className="w-4 h-4" />}
                </button>
                <button 
                  onClick={handleClosePreview}
                  className="p-1.5 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-md transition-colors cursor-pointer"
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
                      <img src={selectedArticle.author?.avatar_url || selectedArticle.author?.avatar || '/default-avatar.svg'} alt={selectedArticle.author?.username || '未知作者'} className="w-6 h-6 rounded-full mr-2" />
                      <span>{selectedArticle.author?.username || '未知作者'}</span>
                    </div>
                    <div className="flex items-center">
                      <FiCalendar className="w-4 h-4 mr-1" />
                      <span>{selectedArticle.publishedAt || selectedArticle.published_at || '未发布'}</span>
                    </div>
                    <div className="flex items-center">
                      <FiEye className="w-4 h-4 mr-1" />
                      <span>{(selectedArticle.views || 0).toLocaleString()} 次浏览</span>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-2 mb-6">
                    <span className="text-sm text-gray-500">分类:</span>
                    <span className="inline-flex items-center px-2 py-1 rounded-md text-sm bg-gray-100 text-gray-700">
                      {selectedArticle.category?.name || selectedArticle.category || '未分类'}
                    </span>
                  </div>
                  
                  <div className="flex flex-wrap gap-2">
                    {(selectedArticle.tags || []).map((tag, index) => (
                      <span key={index} className="inline-flex items-center px-2 py-1 rounded-md text-sm bg-blue-50 text-blue-700 border border-blue-200">
                        <FiTag className="w-3 h-3 mr-1" />
                        {tag.name || tag}
                      </span>
                    ))}
                  </div>
                </div>
                
                {/* 文章正文 */}
                <div className="prose prose-blog">
                  <div className="whitespace-pre-wrap text-gray-700 leading-relaxed">
                    {selectedArticle.content}
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* 删除确认Modal */}
      <Modal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        title="确认删除"
        size="sm"
      >
        <div className="space-y-4">
          <p className="text-gray-600">
            {deleteTarget.type === 'single' 
              ? '确定要删除这篇文章吗？此操作不可撤销。'
              : `确定要删除选中的 ${deleteTarget.count} 篇文章吗？此操作不可撤销。`
            }
          </p>
          <div className="flex justify-end space-x-3">
            <button
              onClick={() => setShowDeleteModal(false)}
              className="px-4 py-2 text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
            >
              取消
            </button>
            <button
              onClick={handleConfirmDelete}
              className="px-4 py-2 text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors"
            >
              确认删除
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default ArticleManagement;