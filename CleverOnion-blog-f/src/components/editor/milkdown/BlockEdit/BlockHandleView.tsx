import React, { useRef, useEffect, useCallback } from 'react';
import { useInstance } from '@milkdown/react';
import { usePluginViewContext } from '@prosemirror-adapter/react';
import { BlockProvider } from '@milkdown/plugin-block';
import { blockEdit } from './blockEdit';
import type { BlockEditConfig, ActiveBlockInfo } from './types';
import { RxDragHandleDots2 } from 'react-icons/rx';
import { IoAdd } from 'react-icons/io5';
import { callCommand } from '@milkdown/kit/utils';
import { paragraphSchema } from '@milkdown/kit/preset/commonmark';
import { editorViewCtx } from '@milkdown/kit/core';
import './BlockEdit.css';

/**
 * 块句柄视图组件
 */
export const BlockHandleView: React.FC = () => {
  const ref = useRef<HTMLDivElement>(null);
  const blockProvider = useRef<BlockProvider | undefined>(undefined);
  const [loading, get] = useInstance();
  const { view, prevState } = usePluginViewContext();

  // 获取配置
  const getConfig = useCallback((): BlockEditConfig => {
    if (loading) return {};
    try {
      return get().ctx.get(blockEdit.configCtx.key) || {};
    } catch {
      return {};
    }
  }, [loading, get]);

  // 执行命令的辅助函数
  const action = useCallback((fn: (ctx: any) => void) => {
    if (loading) return;
    get().action(fn);
  }, [loading, get]);

  // 处理添加新段落
  const handleAddBlock = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    action((ctx) => {
      const { tr } = ctx.get(editorViewCtx).state;
      const { selection } = tr;
      const paragraph = paragraphSchema.type(ctx).create();
      const newTr = tr.insert(selection.to, paragraph);
      ctx.get(editorViewCtx).dispatch(newTr);
    });
  }, [action]);

  // 初始化BlockProvider
  useEffect(() => {
    const div = ref.current;
    if (loading || !div) {
      return;
    }

    const config = getConfig();
    
    const ctx = get().ctx;
    
    blockProvider.current = new BlockProvider({
      ctx,
      content: div,
      getOffset: () => config.offset || 8
    });

    return () => {
      blockProvider.current?.destroy();
    };
  }, [loading, getConfig]);

  // 更新BlockProvider
  useEffect(() => {
    if (blockProvider.current && view && prevState) {
      blockProvider.current.update();
    }
  }, [view, prevState]);

  const config = getConfig();

  return (
    <div
      ref={ref}
      className="block-handle-container flex items-center gap-1 bg-white border border-gray-200 rounded-lg shadow-sm p-1"
    >
      {/* 拖拽句柄 */}
      {config.enableDrag !== false && (
        <button
          className="drag-handle-button flex items-center justify-center w-6 h-6 text-gray-400 rounded"
          draggable
          title="拖拽移动块"
          onMouseDown={(e) => {
            // 阻止默认行为，让ProseMirror处理拖拽
            e.preventDefault();
          }}
        >
          <RxDragHandleDots2 size={14} />
        </button>
      )}
      
      {/* 添加按钮 */}
      {config.showAddButton !== false && (
        <button
          className="add-button flex items-center justify-center w-6 h-6 text-gray-400 rounded"
          onClick={handleAddBlock}
          title="添加新段落"
        >
          <IoAdd size={14} />
        </button>
      )}
    </div>
  );
};