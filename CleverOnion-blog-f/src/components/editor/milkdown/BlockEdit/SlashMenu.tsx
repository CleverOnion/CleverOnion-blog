import React, { useEffect, useRef, useState } from 'react';
import { useInstance } from '@milkdown/react';
import { SlashProvider } from '@milkdown/plugin-slash';
import { callCommand } from '@milkdown/kit/utils';
import { commandsCtx } from '@milkdown/kit/core';
import {
  paragraphSchema,
  headingSchema,
  blockquoteSchema,
  codeBlockSchema,
  bulletListSchema,
  orderedListSchema,
  setBlockTypeCommand,
  wrapInBlockTypeCommand,
  clearTextInCurrentBlockCommand
} from '@milkdown/kit/preset/commonmark';
import './SlashMenu.css';

interface SlashMenuItem {
  id: string;
  label: string;
  description: string;
  icon: string;
  command: (ctx: any) => void;
}

const SLASH_MENU_ITEMS: SlashMenuItem[] = [
  {
    id: 'paragraph',
    label: '段落',
    description: '普通文本段落',
    icon: '¶',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const paragraph = paragraphSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(setBlockTypeCommand.key, { nodeType: paragraph });
    }
  },
  {
    id: 'heading1',
    label: '标题 1',
    description: '大标题',
    icon: 'H1',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const heading = headingSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(setBlockTypeCommand.key, { nodeType: heading, attrs: { level: 1 } });
    }
  },
  {
    id: 'heading2',
    label: '标题 2',
    description: '中标题',
    icon: 'H2',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const heading = headingSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(setBlockTypeCommand.key, { nodeType: heading, attrs: { level: 2 } });
    }
  },
  {
    id: 'heading3',
    label: '标题 3',
    description: '小标题',
    icon: 'H3',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const heading = headingSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(setBlockTypeCommand.key, { nodeType: heading, attrs: { level: 3 } });
    }
  },
  {
    id: 'blockquote',
    label: '引用',
    description: '引用文本',
    icon: '"',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const blockquote = blockquoteSchema.type(ctx);
      commands.call(wrapInBlockTypeCommand.key, { nodeType: blockquote });
    }
  },
  {
    id: 'codeblock',
    label: '代码块',
    description: '代码片段',
    icon: '</>',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const codeBlock = codeBlockSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(setBlockTypeCommand.key, { nodeType: codeBlock });
    }
  },
  {
    id: 'bulletlist',
    label: '无序列表',
    description: '项目符号列表',
    icon: '•',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const bulletList = bulletListSchema.type(ctx);
      commands.call(wrapInBlockTypeCommand.key, { nodeType: bulletList });
    }
  },
  {
    id: 'orderedlist',
    label: '有序列表',
    description: '数字编号列表',
    icon: '1.',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const orderedList = orderedListSchema.type(ctx);
      commands.call(wrapInBlockTypeCommand.key, { nodeType: orderedList });
    }
  }
];

export const SlashMenu: React.FC = () => {
  const [loading, get] = useInstance();
  const [visible, setVisible] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [filter, setFilter] = useState('');
  const menuRef = useRef<HTMLDivElement>(null);
  const slashProviderRef = useRef<SlashProvider | null>(null);

  const filteredItems = SLASH_MENU_ITEMS.filter(item => 
    item.label.toLowerCase().includes(filter.toLowerCase()) ||
    item.description.toLowerCase().includes(filter.toLowerCase())
  );

  useEffect(() => {
    if (loading) return;

    const ctx = get();
    if (!ctx) return;

    const menuElement = document.createElement('div');
    menuElement.className = 'slash-menu-container';
    
    const slashProvider = new SlashProvider({
      content: menuElement,
      shouldShow: (view) => {
        const { state } = view;
        const { selection } = state;
        const { $from } = selection;
        
        // 检查当前位置的文本内容
        const textBefore = $from.parent.textBetween(
          Math.max(0, $from.parentOffset - 10),
          $from.parentOffset
        );
        
        const shouldShow = textBefore.endsWith('/');
        
        if (shouldShow) {
          // 提取过滤文本（斜杠后的内容）
          const slashIndex = textBefore.lastIndexOf('/');
          const filterText = textBefore.slice(slashIndex + 1);
          setFilter(filterText);
          setSelectedIndex(0);
        }
        
        setVisible(shouldShow);
        return shouldShow;
      },
      trigger: '/'
    });

    slashProvider.onShow = () => {
      setVisible(true);
    };

    slashProvider.onHide = () => {
      setVisible(false);
      setFilter('');
      setSelectedIndex(0);
    };

    slashProviderRef.current = slashProvider;

    return () => {
      slashProvider.destroy();
    };
  }, [loading, get]);

  const handleItemClick = (item: SlashMenuItem) => {
    if (loading) return;
    
    const ctx = get();
    if (!ctx) return;

    try {
      item.command(ctx);
      slashProviderRef.current?.hide();
    } catch (error) {
      console.error('执行命令时出错:', error);
    }
  };

  const handleKeyDown = (event: KeyboardEvent) => {
    if (!visible || filteredItems.length === 0) return;

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        setSelectedIndex(prev => (prev + 1) % filteredItems.length);
        break;
      case 'ArrowUp':
        event.preventDefault();
        setSelectedIndex(prev => (prev - 1 + filteredItems.length) % filteredItems.length);
        break;
      case 'Enter':
        event.preventDefault();
        if (filteredItems[selectedIndex]) {
          handleItemClick(filteredItems[selectedIndex]);
        }
        break;
      case 'Escape':
        event.preventDefault();
        slashProviderRef.current?.hide();
        break;
    }
  };

  useEffect(() => {
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [visible, filteredItems, selectedIndex]);

  if (!visible || filteredItems.length === 0) {
    return null;
  }

  return (
    <div ref={menuRef} className="slash-menu-container">
      <div className="slash-menu">
        {filteredItems.map((item, index) => (
          <div
            key={item.id}
            className={`slash-menu-item ${index === selectedIndex ? 'selected' : ''}`}
            onClick={() => handleItemClick(item)}
            onMouseEnter={() => setSelectedIndex(index)}
          >
            <div className="slash-menu-item-icon">{item.icon}</div>
            <div className="slash-menu-item-content">
              <div className="slash-menu-item-label">{item.label}</div>
              <div className="slash-menu-item-description">{item.description}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};