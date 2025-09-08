import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { tagApi, type Tag, type TagWithCount } from '../../api/tags';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import { Loading, SkeletonLoading } from '../../components/ui/Loading';
import { useToast } from '../../components/ui/Toast';

const TagManagement = () => {
  const [tags, setTags] = useState<TagWithCount[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [totalCount, setTotalCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingTag, setEditingTag] = useState<Tag | null>(null);
  const [newTagName, setNewTagName] = useState('');
  const [selectedTags, setSelectedTags] = useState<Set<number>>(new Set());
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletingTag, setDeletingTag] = useState<Tag | null>(null);
  const [showBatchDeleteModal, setShowBatchDeleteModal] = useState(false);
  const toast = useToast();
  const navigate = useNavigate();

  // 加载标签列表
  const loadTags = async (page: number = 0) => {
    try {
      setLoading(true);
      const response = await tagApi.getTagsWithCount(page, pageSize);
      setTags(response.tags);
      setTotalCount(response.total);
      setCurrentPage(response.page);
      setTotalPages(response.totalPages);
    } catch (error) {
      console.error('加载标签失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 搜索标签
  const handleSearch = async () => {
    if (!searchKeyword.trim()) {
      setCurrentPage(0);
      loadTags(0);
      return;
    }
    
    try {
      setLoading(true);
      const response = await tagApi.searchTags(searchKeyword);
      setTags(response.tags.map(tag => ({ ...tag, articleCount: 0 })));
      setTotalCount(response.total);
      setCurrentPage(0);
      setTotalPages(1);
    } catch (error) {
      console.error('搜索标签失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 创建或更新标签
  const handleCreateOrUpdateTag = async () => {
    if (!newTagName.trim()) return;
    
    try {
      setIsSubmitting(true);
      if (editingTag) {
        await tagApi.updateTag(editingTag.id, { name: newTagName });
      } else {
        await tagApi.createTag({ name: newTagName });
      }
      setEditingTag(null);
      setNewTagName('');
      setShowCreateModal(false);
      loadTags(currentPage);
    } catch (error) {
      console.error(editingTag ? '更新标签失败:' : '创建标签失败:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  // 打开删除确认模态框
  const openDeleteModal = (tag: Tag) => {
    setDeletingTag(tag);
    setShowDeleteModal(true);
  };

  // 关闭删除确认模态框
  const closeDeleteModal = () => {
    setShowDeleteModal(false);
    setDeletingTag(null);
  };

  // 确认删除标签
  const handleDeleteTag = async () => {
    if (!deletingTag) return;
    
    setIsSubmitting(true);
    
    try {
      await tagApi.deleteTag(deletingTag.id);
      closeDeleteModal();
      // 如果当前页没有数据了，回到上一页
      const newPage = tags.length === 1 && currentPage > 0 ? currentPage - 1 : currentPage;
      loadTags(newPage);
    } catch (error) {
      console.error('删除标签失败:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  // 批量删除
  const handleBatchDelete = async () => {
    if (selectedTags.size === 0) return;
    setShowBatchDeleteModal(true);
  };

  // 确认批量删除
  const handleConfirmBatchDelete = async () => {
    try {
      setIsSubmitting(true);
      await Promise.all(Array.from(selectedTags).map(id => tagApi.deleteTag(id)));
      setSelectedTags(new Set());
      toast.success(`成功删除 ${selectedTags.size} 个标签`);
      loadTags(currentPage);
    } catch (error) {
      console.error('批量删除失败:', error);
      toast.error('批量删除失败，请重试');
    } finally {
      setIsSubmitting(false);
      setShowBatchDeleteModal(false);
    }
  };

  // 开始编辑标签
  const startEditTag = (tag: Tag) => {
    setEditingTag(tag);
    setNewTagName(tag.name);
    setShowCreateModal(true);
  };

  // 取消编辑
  const cancelEdit = () => {
    setEditingTag(null);
    setNewTagName('');
    setShowCreateModal(false);
  };

  // 切换标签选择
  const toggleTagSelection = (id: number) => {
    const newSelected = new Set(selectedTags);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelectedTags(newSelected);
  };

  // 全选/取消全选
  const toggleSelectAll = () => {
    if (selectedTags.size === tags.length) {
      setSelectedTags(new Set());
    } else {
      setSelectedTags(new Set(tags.map(tag => tag.id)));
    }
  };

  /**
   * 查看标签下的文章
   */
  const viewTagArticles = (tag: Tag) => {
    navigate(`/admin/articles?tagId=${tag.id}`);
  };

  useEffect(() => {
    if (!searchKeyword.trim()) {
      loadTags(currentPage);
    }
  }, [currentPage]);

  useEffect(() => {
    loadTags(0);
  }, []);

  const TagCard = ({ tag }: { tag: TagWithCount }) => (
    <div className="group relative bg-white border border-gray-100 hover:border-gray-200 rounded-xl p-6 transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5">
      <div className="absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity">
        <input
          type="checkbox"
          checked={selectedTags.has(tag.id)}
          onChange={() => toggleTagSelection(tag.id)}
          className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
        />
      </div>
      
      <div className="flex items-start justify-between mb-4">
        <div className="flex-1">
          <h3 className="text-lg font-semibold text-gray-900 mb-2">{tag.name}</h3>
          <div className="flex items-center space-x-4 text-sm text-gray-500">
             <span className="flex items-center">
               <div className="w-2 h-2 bg-blue-500 rounded-full mr-2"></div>
               {tag.articleCount} 篇文章
             </span>
           </div>
        </div>
      </div>
      
      <div className="flex items-center justify-between pt-4 border-t border-gray-50">
        <div className="flex space-x-2">
          <button
            onClick={() => startEditTag(tag)}
            className="text-xs font-medium text-gray-600 hover:text-blue-600 transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
          >
            编辑
          </button>
          <button 
            onClick={() => viewTagArticles(tag)}
            className="text-xs font-medium text-gray-600 hover:text-green-600 transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
          >
            查看文章
          </button>
        </div>
        <button
          onClick={() => openDeleteModal(tag)}
          className="text-xs font-medium text-gray-400 hover:text-red-500 transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
        >
          删除
        </button>
      </div>
    </div>
  );

  const TagListItem = ({ tag }: { tag: TagWithCount }) => (
    <div className="group flex items-center justify-between p-4 bg-white border-b border-gray-50 hover:bg-gray-50 transition-colors">
      <div className="flex items-center space-x-4">
        <input
          type="checkbox"
          checked={selectedTags.has(tag.id)}
          onChange={() => toggleTagSelection(tag.id)}
          className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
        />
        <div className="flex items-center space-x-3">
          <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
          <span className="font-medium text-gray-900">{tag.name}</span>
        </div>
      </div>
      
      <div className="flex items-center space-x-6">
         <span className="text-sm text-gray-500">{tag.articleCount} 篇文章</span>
         <div className="flex items-center space-x-2 opacity-0 group-hover:opacity-100 transition-opacity">
          <button
            onClick={() => startEditTag(tag)}
            className="text-sm text-gray-600 hover:text-blue-600 transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
          >
            编辑
          </button>
          <button 
            onClick={() => viewTagArticles(tag)}
            className="text-sm text-gray-600 hover:text-green-600 transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
          >
            查看
          </button>
          <button
            onClick={() => openDeleteModal(tag)}
            className="text-sm text-gray-400 hover:text-red-500 transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
          >
            删除
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50">


      {/* 工具栏 */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              {/* 搜索框 */}
              <div className="relative">
                <input
                  type="text"
                  placeholder="搜索标签..."
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
                    disabled={loading}
                    className="absolute right-2 top-1/2 transform -translate-y-1/2 px-2 py-1 text-xs bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? '搜索中...' : '搜索'}
                  </button>
                )}
              </div>
              
              {selectedTags.size > 0 && (
                <div className="flex items-center space-x-3">
                  <span className="text-sm text-gray-600">
                    已选择 {selectedTags.size} 个标签
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
                <span>总计 {totalCount} 个标签</span>
                <span>显示 {tags.length} 个</span>
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
              
              <Button variant="primary" onClick={() => setShowCreateModal(true)}>
                新建标签
              </Button>
              
              {viewMode === 'list' && (
                <button
                  onClick={toggleSelectAll}
                  className="text-sm text-blue-600 hover:text-blue-700 font-medium transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
                >
                  {selectedTags.size === tags.length ? '取消全选' : '全选'}
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* 主内容区 */}
      <div className="max-w-7xl mx-auto px-6 py-8">
        {loading ? (
          viewMode === 'grid' ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              <SkeletonLoading rows={4} />
            </div>
          ) : (
            <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
              <SkeletonLoading rows={6} />
            </div>
          )
        ) : tags.length === 0 ? (
          <div className="text-center py-12">
            <div className="w-24 h-24 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
              <svg className="w-12 h-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
              </svg>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              {searchKeyword ? '没有找到匹配的标签' : '还没有标签'}
            </h3>
            <p className="text-gray-500 mb-6">
              {searchKeyword ? '尝试使用其他关键词搜索' : '创建第一个标签来开始组织你的内容'}
            </p>
            {!searchKeyword && (
              <button
                  onClick={() => setShowCreateModal(true)}
                  className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-all duration-200 cursor-pointer hover:scale-105 active:scale-95"
                >
                  创建标签
                </button>
            )}
          </div>
        ) : (
          <>
            <div className={viewMode === 'grid' ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6' : 'bg-white rounded-lg border border-gray-200 overflow-hidden'}>
              {viewMode === 'grid' ? (
                tags.map((tag) => <TagCard key={tag.id} tag={tag} />)
              ) : (
                tags.map((tag) => <TagListItem key={tag.id} tag={tag} />)
              )}
            </div>
            
            {/* 分页控件 */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between bg-white rounded-lg border border-gray-200 px-6 py-4 mt-6">
                <div className="text-sm text-gray-500">
                  共 {totalCount} 个标签，第 {currentPage + 1} / {totalPages} 页
                </div>
                <div className="flex items-center space-x-2">
                  <Button
                    variant="secondary"
                    onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
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
                          onClick={() => setCurrentPage(pageNum)}
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
                    onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                    disabled={currentPage === totalPages - 1}
                  >
                    下一页
                  </Button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* 创建/编辑标签弹窗 */}
      <Modal
        isOpen={showCreateModal}
        onClose={cancelEdit}
        title={editingTag ? '编辑标签' : '创建标签'}
        size="md"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              标签名称
            </label>
            <input
              type="text"
              value={newTagName}
              onChange={(e) => setNewTagName(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
              placeholder="输入标签名称"
              autoFocus
            />
          </div>
        </div>
        
        <div className="flex flex-col sm:flex-row justify-end space-y-3 sm:space-y-0 sm:space-x-3 mt-6 pt-4 border-t border-gray-100">
           <Button 
             variant="secondary" 
             onClick={cancelEdit}
             fullWidth
             className="sm:w-auto"
           >
             取消
           </Button>
           <Button 
             variant="primary" 
             onClick={handleCreateOrUpdateTag}
             disabled={!newTagName.trim()}
             loading={isSubmitting}
             fullWidth
             className="sm:w-auto"
           >
             {editingTag ? '更新' : '创建'}
           </Button>
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
            确定要删除标签 <span className="font-medium text-gray-900">"{deletingTag?.name}"</span> 吗？
          </p>
          <p className="text-xs text-red-600">
            注意：删除标签后，该标签下的文章将失去此标签关联。
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
             onClick={handleDeleteTag}
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
            确定要删除选中的 <span className="font-medium text-gray-900">{selectedTags.size}</span> 个标签吗？
          </p>
          <p className="text-xs text-red-600">
            注意：删除标签后，这些标签下的文章将失去对应的标签关联。
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

export default TagManagement;