import { Ctx } from '@milkdown/kit/ctx';
import { Node } from '@milkdown/prose/model';

/**
 * BlockEdit配置接口
 */
export interface BlockEditConfig {
  /** 是否启用拖拽功能 */
  enableDrag?: boolean;
  /** 是否显示添加按钮 */
  showAddButton?: boolean;
  /** 自定义偏移量 */
  offset?: number;
}

/**
 * 活动块信息
 */
export interface ActiveBlockInfo {
  /** 当前节点 */
  node: Node;
  /** 节点位置 */
  pos: number;
  /** DOM元素 */
  el: HTMLElement;
}

/**
 * 块句柄组件属性
 */
export interface BlockHandleProps {
  /** Milkdown上下文 */
  ctx: Ctx;
  /** 配置选项 */
  config?: BlockEditConfig;
}