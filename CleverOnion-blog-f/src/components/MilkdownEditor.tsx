// MilkdownEditor.tsx
import React from 'react';
import { Milkdown, MilkdownProvider, useEditor } from '@milkdown/react';
import { Editor, rootCtx } from '@milkdown/kit/core';
import { commonmark } from '@milkdown/kit/preset/commonmark';
import { nord } from '@milkdown/theme-nord';
import { listener, listenerCtx } from '@milkdown/kit/plugin/listener';
import { gfm } from '@milkdown/kit/preset/gfm'
import { clipboard } from '@milkdown/kit/plugin/clipboard'

import "@milkdown/crepe/theme/common/style.css";

interface MilkdownEditorProps {
  value: string;
  onChange: (value: string) => void;
  className?: string;
}

export const MilkdownEditor: React.FC<MilkdownEditorProps> = ({ value, onChange, className = '' }) => {
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
      <MilkdownEditor {...props} />
    </MilkdownProvider>
  );
};

export default MilkdownEditorWrapper;