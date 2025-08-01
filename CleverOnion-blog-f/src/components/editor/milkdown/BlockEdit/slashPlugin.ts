import { $ctx, $prose } from '@milkdown/kit/utils';
import { slashFactory, SlashProvider } from '@milkdown/kit/plugin/slash';
import { commandsCtx } from '@milkdown/kit/core';
import {
  paragraphSchema,
  headingSchema,
  blockquoteSchema,
  codeBlockSchema,
  bulletListSchema,
  orderedListSchema,
  listItemSchema,
  hrSchema,
  setBlockTypeCommand,
  wrapInBlockTypeCommand,
  addBlockTypeCommand,
  clearTextInCurrentBlockCommand
} from '@milkdown/kit/preset/commonmark';
import type { PluginView } from '@milkdown/prose/state';
import type { EditorView } from '@milkdown/prose/view';
import type { Ctx } from '@milkdown/kit/ctx';

interface SlashMenuItem {
  id: string;
  label: string;
  description?: string;
  icon: string;
  command: (ctx: Ctx) => void;
}

interface SlashMenuItem {
  id: string;
  label: string;
  description?: string;
  icon: string;
  shortcut?: string;
  command: (ctx: Ctx) => void;
}

const SLASH_MENU_ITEMS: SlashMenuItem[] = [
  {
    id: 'text',
    label: 'Text',
    description: 'Just start typing with plain text.',
    icon: 'T',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const paragraph = paragraphSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(setBlockTypeCommand.key, { nodeType: paragraph });
    }
  },
  {
    id: 'heading1',
    label: 'Heading 1',
    description: 'Big section heading.',
    icon: 'H1',
    shortcut: '#',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const heading = headingSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(setBlockTypeCommand.key, { nodeType: heading, attrs: { level: 1 } });
    }
  },
  {
    id: 'heading2',
    label: 'Heading 2',
    description: 'Medium section heading.',
    icon: 'H2',
    shortcut: '##',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const heading = headingSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(setBlockTypeCommand.key, { nodeType: heading, attrs: { level: 2 } });
    }
  },
  {
    id: 'heading3',
    label: 'Heading 3',
    description: 'Small section heading.',
    icon: 'H3',
    shortcut: '###',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const heading = headingSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(setBlockTypeCommand.key, { nodeType: heading, attrs: { level: 3 } });
    }
  },
  {
    id: 'bulletlist',
    label: 'Bulleted list',
    description: 'Create a simple bulleted list.',
    icon: '•',
    shortcut: '-',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const bulletList = bulletListSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(wrapInBlockTypeCommand.key, { nodeType: bulletList });
    }
  },
  {
    id: 'numberedlist',
    label: 'Numbered list',
    description: 'Create a list with numbering.',
    icon: '1.',
    shortcut: '1.',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const orderedList = orderedListSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(wrapInBlockTypeCommand.key, { nodeType: orderedList });
    }
  },
  {
    id: 'todolist',
    label: 'To-do list',
    description: 'Track tasks with a to-do list.',
    icon: '☐',
    shortcut: '[]',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const listItem = listItemSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(wrapInBlockTypeCommand.key, { nodeType: listItem, attrs: { checked: false } });
    }
  },
  {
    id: 'togglelist',
    label: 'Toggle list',
    description: 'Toggles can hide and show content inside.',
    icon: '▷',
    shortcut: '>',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const blockquote = blockquoteSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(wrapInBlockTypeCommand.key, { nodeType: blockquote });
    }
  },
  {
    id: 'codeblock',
    label: 'Code',
    description: 'Capture a code snippet.',
    icon: '</>', 
    shortcut: '```',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const codeBlock = codeBlockSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(setBlockTypeCommand.key, { nodeType: codeBlock });
    }
  },
  {
    id: 'divider',
    label: 'Divider',
    description: 'Visually divide blocks.',
    icon: '---',
    shortcut: '---',
    command: (ctx) => {
      const commands = ctx.get(commandsCtx);
      const hr = hrSchema.type(ctx);
      commands.call(clearTextInCurrentBlockCommand.key);
      commands.call(addBlockTypeCommand.key, { nodeType: hr });
    }
  }
];

class SlashMenuView implements PluginView {
  private content: HTMLElement;
  private slashProvider: SlashProvider;
  private selectedIndex = 0;
  private filteredItems: SlashMenuItem[] = [];
  private filter = '';
  private keydownHandler?: (event: KeyboardEvent) => void;
  private isKeyboardNavigation = false;

  private ctx: Ctx;
  private view: EditorView;

  constructor(ctx: Ctx, view: EditorView) {
    this.ctx = ctx;
    this.view = view;
    this.content = document.createElement('div');
    this.content.className = 'slash-menu-container';
    this.content.style.display = 'none';
    
    this.slashProvider = new SlashProvider({
      content: this.content,
      shouldShow: (view) => {
        const { state } = view;
        const { selection } = state;
        const { $from } = selection;
        
        if (!selection.empty) return false;
        
        // 获取当前段落的文本内容
        const textContent = $from.parent.textContent;
        const cursorPos = $from.parentOffset;
        
        // 检查光标前的字符是否为 '/'
        if (cursorPos > 0 && textContent[cursorPos - 1] === '/') {
          // 检查 '/' 前面是否为空格或者是行首
          if (cursorPos === 1 || textContent[cursorPos - 2] === ' ') {
            this.updateFilter('');
            return true;
          }
        }
        
        return false;
      },
      trigger: '/'
    });

    this.slashProvider.onShow = () => {
      this.show();
    };

    this.slashProvider.onHide = () => {
      this.hide();
    };

    this.setupKeyboardHandlers();
    this.updateFilter('');
  }

  private updateFilter(filter: string) {
    this.filter = filter;
    this.filteredItems = SLASH_MENU_ITEMS.filter(item => 
      item.label.toLowerCase().includes(filter.toLowerCase()) ||
      (item.description?.toLowerCase() || '').includes(filter.toLowerCase())
    );
    this.selectedIndex = 0;
    this.render();
  }

  private render() {
    this.content.innerHTML = '';
    
    if (this.filteredItems.length === 0) {
      this.content.style.display = 'none';
      return;
    }

    const menu = document.createElement('div');
    menu.className = 'slash-menu';

    this.filteredItems.forEach((item, index) => {
      const menuItem = document.createElement('div');
      menuItem.className = `slash-menu-item ${index === this.selectedIndex ? 'selected' : ''}`;
      menuItem.setAttribute('data-index', index.toString());
      
      menuItem.innerHTML = `
        <div class="slash-menu-item-icon">${item.icon}</div>
        <div class="slash-menu-item-content">
          <div class="slash-menu-item-label">${item.label}</div>
          ${item.description ? `<div class="slash-menu-item-description">${item.description}</div>` : ''}
        </div>
        ${item.shortcut ? `<div class="slash-menu-item-shortcut">${item.shortcut}</div>` : ''}
      `;

      menuItem.addEventListener('mousedown', (e) => {
        e.preventDefault();
        e.stopPropagation();
        this.executeCommand(item);
      });

      menuItem.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
      });

      menuItem.addEventListener('mouseenter', () => {
        if (!this.isKeyboardNavigation) {
          this.selectedIndex = index;
          this.render();
        }
      });

      menuItem.addEventListener('mousemove', () => {
        this.isKeyboardNavigation = false;
      });

      menu.appendChild(menuItem);
    });

    this.content.appendChild(menu);
    
    // 如果是键盘导航，滚动到选中的项目
    if (this.isKeyboardNavigation) {
      this.scrollToSelectedItem();
    }
  }

  private scrollToSelectedItem() {
    const container = this.content;
    const selectedItem = this.content.querySelector('.slash-menu-item.selected');
    
    if (!container || !selectedItem) {
      return;
    }

    const containerRect = container.getBoundingClientRect();
    const itemRect = selectedItem.getBoundingClientRect();
    
    // 计算选中项相对于容器的位置
    const itemTop = itemRect.top - containerRect.top + container.scrollTop;
    const itemBottom = itemTop + itemRect.height;
    
    // 获取容器的可视区域
    const containerScrollTop = container.scrollTop;
    const containerHeight = container.clientHeight;
    const containerScrollBottom = containerScrollTop + containerHeight;
    
    // 如果选中项在可视区域上方，向上滚动
    if (itemTop < containerScrollTop) {
      container.scrollTop = itemTop;
    }
    // 如果选中项在可视区域下方，向下滚动
    else if (itemBottom > containerScrollBottom) {
      container.scrollTop = itemBottom - containerHeight;
    }
  }

  private executeCommand(item: SlashMenuItem) {
    try {
      // 删除触发斜杠命令的 '/'
      const { state, dispatch } = this.view;
      const { selection } = state;
      const { $from } = selection;
      
      // 找到 '/' 的位置并删除它
      const tr = state.tr.delete($from.pos - 1, $from.pos);
      dispatch(tr);
      
      // 确保编辑器获得焦点
      this.view.focus();
      
      // 执行命令
      setTimeout(() => {
        item.command(this.ctx);
        // 命令执行后再次确保焦点
        this.view.focus();
      }, 0);
      
      this.slashProvider.hide();
    } catch (error) {
      console.error('执行命令时出错:', error);
    }
  }

  private setupKeyboardHandlers() {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (this.content.style.display === 'none' || this.filteredItems.length === 0) {
        return;
      }

      switch (event.key) {
        case 'ArrowDown':
          event.preventDefault();
          this.isKeyboardNavigation = true;
          this.selectedIndex = (this.selectedIndex + 1) % this.filteredItems.length;
          this.render();
          break;
        case 'ArrowUp':
          event.preventDefault();
          this.isKeyboardNavigation = true;
          this.selectedIndex = (this.selectedIndex - 1 + this.filteredItems.length) % this.filteredItems.length;
          this.render();
          break;
        case 'Enter':
          event.preventDefault();
          if (this.filteredItems[this.selectedIndex]) {
            this.executeCommand(this.filteredItems[this.selectedIndex]);
          }
          break;
        case 'Escape':
          event.preventDefault();
          this.slashProvider.hide();
          break;
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    this.keydownHandler = handleKeyDown;
  }

  private show() {
    this.content.style.display = 'block';
    this.isKeyboardNavigation = false;
  }

  private hide() {
    this.content.style.display = 'none';
    this.updateFilter('');
  }

  update(view: EditorView) {
    this.slashProvider.update(view);
  }

  destroy() {
    if (this.keydownHandler) {
      document.removeEventListener('keydown', this.keydownHandler);
    }
    this.slashProvider.destroy();
    this.content.remove();
  }
}

// 创建斜杠插件
export const slashMenu = slashFactory('SLASH_MENU');

// 配置斜杠插件
export const configureSlashMenu = (ctx: Ctx) => {
  ctx.set(slashMenu.key, {
    view: (view: EditorView) => new SlashMenuView(ctx, view)
  });
};