import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiEdit2 } from 'react-icons/fi';
import categoryApi, { type Category, type CategoryWithCount, type CategoryPageResponse, type CategoryWithCountListResponse } from '../../api/categories';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import { Loading, SkeletonLoading } from '../../components/ui/Loading';
import { useToast } from '../../components/ui/Toast';

import IconSelector from '../../components/ui/IconSelector';
import { renderIcon } from '../../utils/iconUtils';

/**
 * 分类管理页面组件
 * 提供分类的增删改查功能，包括分页、搜索等
 */
const CategoryManagement = () => {
  const navigate = useNavigate();

  
  // 分类列表状态
  const [categories, setCategories] = useState<CategoryWithCount[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalCount, setTotalCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  
  // 搜索状态
  const [searchKeyword, setSearchKeyword] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  
  // 模态框状态
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletingCategory, setDeletingCategory] = useState<Category | null>(null);
  
  // 表单状态
  const [categoryName, setCategoryName] = useState('');
  const [categoryIcon, setCategoryIcon] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // 视图和选择状态
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [selectedCategories, setSelectedCategories] = useState<Set<number>>(new Set());
  const [showBatchDeleteModal, setShowBatchDeleteModal] = useState(false);
  const toast = useToast();
  
  /**
   * 加载分类列表
   */
  const loadCategories = async (page: number = currentPage, keyword: string = searchKeyword) => {
    try {
      setLoading(true);
      
      if (keyword.trim()) {
        // 搜索分类（搜索时不显示文章数量）
        const searchResult = await categoryApi.searchCategories(keyword.trim());
        const categoriesWithCount = searchResult.categories.map(cat => ({
          ...cat,
          articleCount: 0 // 搜索结果暂不显示文章数量
        }));
        setCategories(categoriesWithCount);
        setTotalCount(searchResult.totalCount);
        setTotalPages(1);
        setCurrentPage(0);
      } else {
        // 分页获取分类及文章数量
        const response = await categoryApi.getCategoriesWithCount(page, pageSize);
        setCategories(response.categories);
        setTotalCount(response.total);
        setTotalPages(response.totalPages || Math.ceil(response.total / pageSize));
        setCurrentPage(response.page);
      }
    } catch (error) {
      console.error('加载分类列表失败:', error);
      console.error('加载分类列表失败');
    } finally {
      setLoading(false);
    }
  };
  
  /**
   * 处理搜索
   */
  const handleSearch = async () => {
    if (isSearching) return;
    
    setIsSearching(true);
    setCurrentPage(0);
    await loadCategories(0, searchKeyword);
    setIsSearching(false);
  };
  

  
  /**
   * 切换分类选择
   */
  const toggleCategorySelection = (id: number) => {
    const newSelected = new Set(selectedCategories);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelectedCategories(newSelected);
  };
  
  /**
   * 全选/取消全选
   */
  const toggleSelectAll = () => {
    if (selectedCategories.size === categories.length) {
      setSelectedCategories(new Set());
    } else {
      setSelectedCategories(new Set(categories.map(category => category.id)));
    }
  };
  
  /**
   * 批量删除
   */
  const handleBatchDelete = async () => {
    if (selectedCategories.size === 0) return;
    setShowBatchDeleteModal(true);
  };

  /**
   * 确认批量删除
   */
  const handleConfirmBatchDelete = async () => {
    try {
      setIsSubmitting(true);
      await Promise.all(Array.from(selectedCategories).map(id => categoryApi.deleteCategory(id)));
      setSelectedCategories(new Set());
      toast.success(`成功删除 ${selectedCategories.size} 个分类`);
      loadCategories(currentPage);
    } catch (error) {
      console.error('批量删除失败:', error);
      toast.error('批量删除失败，请重试');
    } finally {
      setIsSubmitting(false);
      setShowBatchDeleteModal(false);
    }
  };
  
  /**
   * 处理分页
   */
  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < totalPages && newPage !== currentPage) {
      setCurrentPage(newPage);
      loadCategories(newPage, searchKeyword);
    }
  };
  
  /**
   * 打开创建分类模态框
   */
  const openCreateModal = () => {
    setCategoryName('');
    setCategoryIcon('');
    setEditingCategory(null);
    setShowCreateModal(true);
  };
  
  /**
   * 打开编辑分类模态框
   */
  const openEditModal = (category: Category) => {
    setCategoryName(category.name);
    setCategoryIcon(category.icon || '');
    setEditingCategory(category);
    setShowCreateModal(true);
  };
  
  /**
   * 关闭创建/编辑模态框
   */
  const closeCreateModal = () => {
    setShowCreateModal(false);
    setEditingCategory(null);
    setCategoryName('');
    setCategoryIcon('');
  };
  
  /**
   * 提交分类表单
   */
  const handleSubmitCategory = async () => {
    if (!categoryName.trim()) {
      console.error('分类名称不能为空');
      return;
    }
    
    if (categoryName.trim().length > 50) {
      console.error('分类名称不能超过50个字符');
      return;
    }
    
    setIsSubmitting(true);
    
    try {
      if (editingCategory) {
        // 更新分类
        await categoryApi.updateCategory(editingCategory.id, { 
          name: categoryName.trim(),
          icon: categoryIcon || undefined
        });
        console.log('分类更新成功');
      } else {
        // 创建分类
        await categoryApi.createCategory({ 
          name: categoryName.trim(),
          icon: categoryIcon || undefined
        });
        console.log('分类创建成功');
      }
      
      closeCreateModal();
      await loadCategories(currentPage, searchKeyword);
    } catch (error) {
      console.error('保存分类失败:', error);
      console.error(editingCategory ? '分类更新失败' : '分类创建失败');
    } finally {
      setIsSubmitting(false);
    }
  };
  
  /**
   * 打开删除确认模态框
   */
  const openDeleteModal = (category: Category) => {
    setDeletingCategory(category);
    setShowDeleteModal(true);
  };
  
  /**
   * 关闭删除确认模态框
   */
  const closeDeleteModal = () => {
    setShowDeleteModal(false);
    setDeletingCategory(null);
  };
  
  /**
   * 确认删除分类
   */
  const handleDeleteCategory = async () => {
    if (!deletingCategory) return;
    
    setIsSubmitting(true);
    
    try {
      await categoryApi.deleteCategory(deletingCategory.id);
      console.log('分类删除成功');
      closeDeleteModal();
      
      // 如果当前页没有数据了，回到上一页
      const newTotalCount = totalCount - 1;
      const newTotalPages = Math.ceil(newTotalCount / pageSize);
      const targetPage = currentPage >= newTotalPages ? Math.max(0, newTotalPages - 1) : currentPage;
      
      await loadCategories(targetPage, searchKeyword);
    } catch (error) {
      console.error('删除分类失败:', error);
      console.error('删除分类失败');
    } finally {
      setIsSubmitting(false);
    }
  };
  
  /**
   * 查看分类下的文章
   */
  const viewCategoryArticles = (category: Category) => {
    navigate(`/admin/articles?categoryId=${category.id}`);
  };
  
  // 组件挂载时加载数据
  useEffect(() => {
    loadCategories();
  }, []);
  
  return (
    <div className="h-full flex flex-col">
      {/* 工具栏 */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              {/* 搜索框 */}
              <div className="relative">
                <input
                  type="text"
                  placeholder="搜索分类..."
                  value={searchKeyword}
                  onChange={(e) => setSearchKeyword(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                  className="w-80 pl-10 pr-16 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                />
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg className="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                </div>
                {searchKeyword && (
                  <button
                    onClick={handleSearch}
                    disabled={isSearching}
                    className="absolute right-2 top-1/2 transform -translate-y-1/2 px-2 py-1 text-xs bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {isSearching ? '搜索中...' : '搜索'}
                  </button>
                )}
              </div>
              
              {selectedCategories.size > 0 && (
                <div className="flex items-center space-x-3">
                  <span className="text-sm text-gray-600">
                    已选择 {selectedCategories.size} 个分类
                  </span>
                  <button
                    onClick={handleBatchDelete}
                    className="text-sm text-red-600 hover:text-red-700 font-medium transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
                  >
                    批量删除
                  </button>
                </div>
              )}
            </div>
            
            <div className="flex items-center space-x-4">
              {/* 统计信息 */}
              <div className="flex items-center space-x-6 text-sm text-gray-500">
                <span>总计 {totalCount} 个分类</span>
                <span>显示 {categories.length} 个</span>
              </div>
              
              {/* 视图切换 */}
              <div className="flex items-center bg-gray-100 rounded-lg p-1">
                <button
                  onClick={() => setViewMode('grid')}
                  className={`px-3 py-1.5 text-sm font-medium rounded-md transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95 ${
                    viewMode === 'grid'
                      ? 'bg-white text-gray-900 shadow-sm'
                      : 'text-gray-600 hover:text-gray-900'
                  }`}
                >
                  网格
                </button>
                <button
                  onClick={() => setViewMode('list')}
                  className={`px-3 py-1.5 text-sm font-medium rounded-md transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95 ${
                    viewMode === 'list'
                      ? 'bg-white text-gray-900 shadow-sm'
                      : 'text-gray-600 hover:text-gray-900'
                  }`}
                >
                  列表
                </button>
              </div>
              
              <Button variant="primary" onClick={openCreateModal}>
                新建分类
              </Button>
              
              {viewMode === 'list' && (
                <button
                  onClick={toggleSelectAll}
                  className="text-sm text-blue-600 hover:text-blue-700 font-medium transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
                >
                  {selectedCategories.size === categories.length ? '取消全选' : '全选'}
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
      
      {/* 主内容区 */}
      <div className="flex-1 p-6">
        {loading ? (
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <SkeletonLoading rows={6} />
          </div>
        ) : categories.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-12 text-center">
            <div className="w-24 h-24 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
              <svg className="w-12 h-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
              </svg>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              {searchKeyword ? '没有找到匹配的分类' : '还没有分类'}
            </h3>
            <p className="text-gray-500 mb-6">
              {searchKeyword ? '尝试使用其他关键词搜索' : '创建第一个分类来开始组织你的内容'}
            </p>
            {!searchKeyword && (
              <button
                onClick={openCreateModal}
                className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
              >
                创建分类
              </button>
            )}
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow overflow-hidden">
            {viewMode === 'grid' ? (
              /* 网格视图 */
              <div className="p-6">
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                  {categories.map((category) => (
                    <div key={category.id} className="group relative bg-white border border-gray-100 hover:border-gray-200 rounded-xl p-6 transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5">
                      <div className="absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity">
                        <input
                          type="checkbox"
                          checked={selectedCategories.has(category.id)}
                          onChange={() => toggleCategorySelection(category.id)}
                          className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                        />
                      </div>
                      
                      <div className="flex items-start justify-between mb-4">
                        <div className="flex-1">
                          <div className="flex items-center space-x-2 mb-2">
                            {category.icon && (
                              <div className="text-xl">
                                {renderIcon(category.icon)}
                              </div>
                            )}
                            <h3 className="text-lg font-semibold text-gray-900 group-hover:text-blue-600 transition-colors">
                              {category.name}
                            </h3>
                          </div>
                          <div className="flex items-center justify-end">
                            <div className="flex items-center space-x-1 text-sm text-blue-600 bg-blue-50 px-2 py-1 rounded-full">
                              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                              </svg>
                              <span className="font-medium">{category.articleCount}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                      
                      <div className="flex items-center justify-between pt-4 border-t border-gray-50">
                        <div className="flex space-x-2">
                          <button
                            onClick={() => openEditModal(category)}
                            className="text-xs font-medium text-gray-600 hover:text-blue-600 transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
                          >
                            编辑
                          </button>
                          <button 
                            onClick={() => viewCategoryArticles(category)}
                            className="text-xs font-medium text-gray-600 hover:text-green-600 transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
                          >
                            查看文章
                          </button>
                        </div>
                        <button
                          onClick={() => openDeleteModal(category)}
                          className="text-xs font-medium text-gray-400 hover:text-red-500 transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
                        >
                          删除
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              /* 列表视图 */
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        <input
                          type="checkbox"
                          checked={selectedCategories.size === categories.length && categories.length > 0}
                          onChange={toggleSelectAll}
                          className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                        />
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        分类名称
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        文章数量
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                        操作
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {categories.map((category) => (
                      <tr key={category.id} className="hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <input
                            type="checkbox"
                            checked={selectedCategories.has(category.id)}
                            onChange={() => toggleCategorySelection(category.id)}
                            className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                          />
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center space-x-2">
                            {category.icon && (
                              <div className="text-lg">
                                {renderIcon(category.icon)}
                              </div>
                            )}
                            <div className="text-sm font-medium text-gray-900">{category.name}</div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center space-x-1 text-sm text-blue-600">
                            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                            <span className="font-medium">{category.articleCount}</span>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <div className="flex items-center justify-end space-x-2">
                            <button
                              onClick={() => openEditModal(category)}
                              className="text-blue-600 hover:text-blue-900 transition-colors"
                            >
                              编辑
                            </button>
                            <button
                              onClick={() => viewCategoryArticles(category)}
                              className="text-green-600 hover:text-green-900 transition-colors"
                            >
                              查看
                            </button>
                            <button
                              onClick={() => openDeleteModal(category)}
                              className="text-red-600 hover:text-red-900 transition-colors"
                            >
                              删除
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
            
            {/* 分页控件 */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between bg-white rounded-lg border border-gray-200 px-6 py-4 mt-6">
                <div className="text-sm text-gray-500">
                  共 {totalCount} 个分类，第 {currentPage + 1} / {totalPages} 页
                </div>
                <div className="flex items-center space-x-2">
                  <Button
                    variant="secondary"
                    onClick={() => handlePageChange(Math.max(0, currentPage - 1))}
                    disabled={currentPage === 0}
                  >
                    上一页
                  </Button>
                  <div className="flex items-center space-x-1">
                    {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                      const pageNum = Math.max(0, Math.min(totalPages - 5, currentPage - 2)) + i;
                      return (
                        <button
                          key={pageNum}
                          onClick={() => handlePageChange(pageNum)}
                          className={`px-3 py-1 text-sm rounded-md transition-colors ${
                            pageNum === currentPage
                              ? 'bg-blue-600 text-white'
                              : 'text-gray-500 hover:bg-gray-100'
                          }`}
                        >
                          {pageNum + 1}
                        </button>
                      );
                    })}
                  </div>
                  <Button
                    variant="secondary"
                    onClick={() => handlePageChange(Math.min(totalPages - 1, currentPage + 1))}
                    disabled={currentPage === totalPages - 1}
                  >
                    下一页
                  </Button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
      
      {/* 创建/编辑分类模态框 */}
      <Modal
        isOpen={showCreateModal}
        onClose={closeCreateModal}
        title={editingCategory ? '编辑分类' : '新建分类'}
        size="md"
      >
        <div className="space-y-4">
          <div>
            <label htmlFor="categoryName" className="block text-sm font-medium text-gray-700 mb-2">
              分类名称
            </label>
            <input
              id="categoryName"
              type="text"
              value={categoryName}
              onChange={(e) => setCategoryName(e.target.value)}
              placeholder="请输入分类名称"
              maxLength={50}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              autoFocus
            />
            <p className="mt-1 text-xs text-gray-500">
              {categoryName.length}/50 字符
            </p>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              分类图标
            </label>
            <IconSelector
              value={categoryIcon}
              onChange={setCategoryIcon}
            />
          </div>
          
        <div className="flex flex-col sm:flex-row justify-end space-y-3 sm:space-y-0 sm:space-x-3 mt-6 pt-4 border-t border-gray-100">
           <Button 
             variant="secondary" 
             onClick={closeCreateModal}
             disabled={isSubmitting}
             fullWidth
             className="sm:w-auto"
           >
             取消
           </Button>
           <Button 
             variant="primary" 
             onClick={handleSubmitCategory}
             disabled={!categoryName.trim()}
             loading={isSubmitting}
             fullWidth
             className="sm:w-auto"
           >
             {editingCategory ? '更新' : '创建'}
           </Button>
         </div>
        </div>
      </Modal>
      
      {/* 删除确认模态框 */}
      <Modal
        isOpen={showDeleteModal}
        onClose={closeDeleteModal}
        title="确认删除"
        size="sm"
      >
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            确定要删除分类 <span className="font-medium text-gray-900">"{deletingCategory?.name}"</span> 吗？
          </p>
          <p className="text-xs text-red-600">
            注意：删除分类后，该分类下的文章将需要重新分配分类。
          </p>
          
        <div className="flex flex-col sm:flex-row justify-end space-y-3 sm:space-y-0 sm:space-x-3 mt-6 pt-4 border-t border-gray-100">
           <Button 
             variant="secondary" 
             onClick={closeDeleteModal}
             disabled={isSubmitting}
             fullWidth
             className="sm:w-auto"
           >
             取消
           </Button>
           <Button 
             variant="danger" 
             onClick={handleDeleteCategory}
             loading={isSubmitting}
             fullWidth
             className="sm:w-auto"
           >
             确认删除
           </Button>
         </div>
        </div>
      </Modal>

      {/* 批量删除确认Modal */}
      <Modal
        isOpen={showBatchDeleteModal}
        onClose={() => setShowBatchDeleteModal(false)}
        title="确认批量删除"
        size="sm"
      >
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            确定要删除选中的 <span className="font-medium text-gray-900">{selectedCategories.size}</span> 个分类吗？
          </p>
          <p className="text-xs text-red-600">
            注意：删除分类后，这些分类下的文章将需要重新分配分类。
          </p>
          
        <div className="flex flex-col sm:flex-row justify-end space-y-3 sm:space-y-0 sm:space-x-3 mt-6 pt-4 border-t border-gray-100">
           <Button 
             variant="secondary" 
             onClick={() => setShowBatchDeleteModal(false)}
             disabled={isSubmitting}
             fullWidth
             className="sm:w-auto"
           >
             取消
           </Button>
           <Button 
             variant="danger" 
             onClick={handleConfirmBatchDelete}
             loading={isSubmitting}
             fullWidth
             className="sm:w-auto"
           >
             确认删除
           </Button>
         </div>
        </div>
      </Modal>
    </div>
  );
};

export default CategoryManagement;