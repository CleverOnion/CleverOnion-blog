import React from 'react';
import { useToast } from './ToastContext';
import Button from '../Button';

/**
 * Toast使用示例组件
 * 展示如何在组件中使用Toast通知系统
 */
const ToastExample: React.FC = () => {
  const { success, error, warning, info, addToast } = useToast();

  const handleSuccess = () => {
    success('操作成功！', {
      title: '成功',
      duration: 3000
    });
  };

  const handleError = () => {
    error('操作失败，请重试', {
      title: '错误',
      duration: 5000
    });
  };

  const handleWarning = () => {
    warning('请注意检查输入内容', {
      title: '警告',
      duration: 4000
    });
  };

  const handleInfo = () => {
    info('这是一条信息提示', {
      title: '提示',
      duration: 3000
    });
  };

  const handleCustomToast = () => {
    addToast('自定义Toast消息', {
      type: 'success',
      title: '自定义',
      duration: 0, // 不自动关闭
      action: {
        label: '查看详情',
        onClick: () => {
          console.log('查看详情被点击');
        }
      }
    });
  };

  const handleLongMessage = () => {
    success(
      '这是一条很长的消息，用来测试Toast组件在处理长文本时的表现。它应该能够正确地换行显示，并保持良好的视觉效果。',
      {
        title: '长消息测试',
        duration: 6000
      }
    );
  };

  const handleMultipleToasts = () => {
    success('第一条消息');
    setTimeout(() => error('第二条消息'), 500);
    setTimeout(() => warning('第三条消息'), 1000);
    setTimeout(() => info('第四条消息'), 1500);
  };

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <h2 className="text-2xl font-bold mb-6">Toast 通知系统示例</h2>
      
      <div className="grid grid-cols-2 gap-4 mb-6">
        <Button onClick={handleSuccess} variant="primary">
          成功通知
        </Button>
        
        <Button onClick={handleError} variant="danger">
          错误通知
        </Button>
        
        <Button onClick={handleWarning} variant="secondary">
          警告通知
        </Button>
        
        <Button onClick={handleInfo} variant="ghost">
          信息通知
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-4">
        <Button onClick={handleCustomToast} variant="primary" fullWidth>
          自定义Toast（带操作按钮）
        </Button>
        
        <Button onClick={handleLongMessage} variant="secondary" fullWidth>
          长消息测试
        </Button>
        
        <Button onClick={handleMultipleToasts} variant="ghost" fullWidth>
          多个Toast测试
        </Button>
      </div>

      <div className="mt-8 p-4 bg-gray-50 rounded-lg">
        <h3 className="text-lg font-semibold mb-2">使用方法：</h3>
        <pre className="text-sm text-gray-700 overflow-x-auto">
{`// 1. 导入useToast Hook
import { useToast } from './components/ui/Toast';

// 2. 在组件中使用
const { success, error, warning, info } = useToast();

// 3. 调用相应方法
success('操作成功！');
error('操作失败！');
warning('警告信息');
info('提示信息');

// 4. 自定义选项
success('消息', {
  title: '标题',
  duration: 3000,
  action: {
    label: '操作',
    onClick: () => console.log('点击')
  }
});`}
        </pre>
      </div>
    </div>
  );
};

export default ToastExample;