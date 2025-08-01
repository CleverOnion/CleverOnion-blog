import { $ctx } from '@milkdown/kit/utils';
import { blockPlugin, blockSpec, blockService, blockServiceInstance } from '@milkdown/plugin-block';
import { slashMenu, configureSlashMenu } from './slashPlugin';
import type { BlockEditConfig } from './types';

/**
 * BlockEdit配置的默认值
 */
const defaultConfig: Required<BlockEditConfig> = {
  enableDrag: true,
  showAddButton: true,
  offset: 8
};

/**
 * 创建BlockEdit配置的上下文键
 */
export const blockEditConfigCtx = $ctx(defaultConfig, 'blockEditConfig');

/**
 * 创建BlockEdit插件工厂
 */
export const blockEditFactory = (config: BlockEditConfig = {}) => {
  const mergedConfig = { ...defaultConfig, ...config };
  
  // 创建带有合并配置的上下文键
  const configCtx = $ctx(mergedConfig, 'blockEditConfig');
  
  return {
    configCtx,
    plugins: [blockService, blockServiceInstance, blockPlugin, ...slashMenu],
    configureSlashMenu
  };
};

/**
 * 默认的BlockEdit插件实例
 */
export const blockEdit = blockEditFactory();