// MilkdownEditor.tsx
import React from 'react';
import { Milkdown, MilkdownProvider, useEditor } from '@milkdown/react';
import { Editor, rootCtx, defaultValueCtx } from '@milkdown/kit/core';
import { commonmark } from '@milkdown/kit/preset/commonmark';
import { nord } from '@milkdown/theme-nord';
import { listener, listenerCtx } from '@milkdown/kit/plugin/listener';
import { gfm } from '@milkdown/kit/preset/gfm'
import { clipboard } from '@milkdown/kit/plugin/clipboard'
import { history } from '@milkdown/kit/plugin/history'
import { cursor } from '@milkdown/kit/plugin/cursor'
import { prism, prismConfig } from '@milkdown/plugin-prism'
import { indent } from '@milkdown/kit/plugin/indent'
import css from 'refractor/css'
import javascript from 'refractor/javascript'
import jsx from 'refractor/jsx'
import markdown from 'refractor/markdown'
import tsx from 'refractor/tsx'
import typescript from 'refractor/typescript'
import { tooltip, TooltipView } from './editor/milkdown/ToolTip';
import { usePluginViewFactory, ProsemirrorAdapterProvider } from '@prosemirror-adapter/react';


import "@milkdown/crepe/theme/common/style.css";

interface MilkdownEditorProps {
  value: string;
  onChange: (value: string) => void;
  className?: string;
}

export const MilkdownEditor: React.FC<MilkdownEditorProps> = ({ value, onChange, className = '' }) => {
  const pluginViewFactory = usePluginViewFactory();
  
  useEditor((root) =>
    Editor.make()
      .config(nord)
      .config((ctx) => {
        ctx.set(rootCtx, root);
        ctx.get(listenerCtx).markdownUpdated((_, markdown) => {
          onChange(markdown);
        });
      })
      .use(commonmark)
      .use(listener)
      .use(gfm)
      .use(clipboard)
      .use(history)
      .use(cursor)
      .config((ctx) => {
        ctx.set(prismConfig.key, {
          configureRefractor: (refractor) => {
            refractor.register(markdown)
            refractor.register(css)
            refractor.register(javascript)
            refractor.register(typescript)
            refractor.register(jsx)
            refractor.register(tsx)
          },
        })
      })
      .use(prism)
      .use(indent)
      .config(ctx => {
        ctx.set(tooltip.key, {
          view: pluginViewFactory({
            component: TooltipView,
          })
        })
      })
      .use(tooltip)
  );

  return (
    <div className={className}>
      <Milkdown />
    </div>
  );
};

export const MilkdownEditorWrapper: React.FC<MilkdownEditorProps> = (props) => {
  return (
    <MilkdownProvider>
      <ProsemirrorAdapterProvider>
        <MilkdownEditor {...props} />
      </ProsemirrorAdapterProvider>
    </MilkdownProvider>
  );
};

export default MilkdownEditorWrapper;