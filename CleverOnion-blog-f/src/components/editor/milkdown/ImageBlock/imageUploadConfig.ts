import { api } from '../../../../api/index';

/**
 * 图片上传处理函数
 * @param file 要上传的文件
 * @returns 返回上传后的图片URL
 */
export const handleImageUpload = async (file: File): Promise<string> => {
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    throw new Error('请选择图片文件');
  }
  
  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    throw new Error('图片大小不能超过 5MB');
  }
  
  try {
    // 检查用户登录状态
    const token = localStorage.getItem('accessToken');
    if (!token) {
      throw new Error('请先登录后再上传图片');
    }
    
    // 创建FormData
    const formData = new FormData();
    formData.append('file', file);
    
    // 调用后端上传接口
    // 对于 FormData，需要移除默认的 Content-Type 让 axios 自动设置
    const response = await api.post('/upload/image', formData, {
      headers: {
        'Content-Type': undefined 
      }
    });
    
    if (response.data.success && response.data.data?.url) {
      console.log('图片上传成功:', response.data.data.url);
      return response.data.data.url;
    } else {
      throw new Error(response.data.message || '上传失败，请重试');
    }
  } catch (error: any) {
    console.error('图片上传失败:', error);
    
    // 处理不同类型的错误
    if (error.response?.status === 401) {
      throw new Error('登录已过期，请重新登录');
    } else if (error.response?.status === 413) {
      throw new Error('图片文件过大，请选择较小的图片');
    } else if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    } else {
      throw new Error(error.message || '上传失败，请重试');
    }
  }
};

/**
 * ImageBlock 配置选项
 */
export const imageBlockConfig = {
  imageIcon: '🖼️',
  captionIcon: '📝',
  uploadButton: '上传图片',
  confirmButton: '确认 ⏎',
  uploadPlaceholderText: '拖拽图片到此处或粘贴图片链接',
  captionPlaceholderText: '添加图片描述（可选）',
  onUpload: handleImageUpload
};