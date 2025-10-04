// MilkdownEditor.tsx
import React from "react";
import { Milkdown, MilkdownProvider, useEditor } from "@milkdown/react";
import {
  Editor,
  rootCtx,
  defaultValueCtx,
  editorViewCtx,
  parserCtx,
} from "@milkdown/kit/core";
import { commonmark } from "@milkdown/kit/preset/commonmark";
import { nord } from "@milkdown/theme-nord";
import { listener, listenerCtx } from "@milkdown/kit/plugin/listener";
import { gfm } from "@milkdown/kit/preset/gfm";
import { clipboard } from "@milkdown/kit/plugin/clipboard";
import { history } from "@milkdown/kit/plugin/history";
import { cursor } from "@milkdown/kit/plugin/cursor";
import { prism, prismConfig } from "@milkdown/plugin-prism";
import { indent } from "@milkdown/kit/plugin/indent";
import { configurePrismLanguages } from "./editor/milkdown/prismLanguages";
import { tooltip } from "./editor/milkdown/tooltipConfig";
import { TooltipView } from "./editor/milkdown/Tooltip";
import { blockEdit } from "./editor/milkdown/BlockEdit";
import { BlockHandleView } from "./editor/milkdown/BlockEdit";
import { blockSpec } from "@milkdown/plugin-block";
import {
  usePluginViewFactory,
  ProsemirrorAdapterProvider,
} from "@prosemirror-adapter/react";
import {
  configureCodeMirror,
  codeBlockComponent,
} from "./editor/milkdown/CodeMirror";
import { codeMirrorLanguages } from "./editor/milkdown/CodeMirror/languages";
import {
  configureImageBlock,
  imageBlockComponent,
} from "./editor/milkdown/ImageBlock";
import {
  imageInlineComponent,
  inlineImageConfig,
} from "@milkdown/kit/component/image-inline";
import { imageBlockConfig } from "./editor/milkdown/ImageBlock/imageUploadConfig";

import "@milkdown/crepe/theme/common/style.css";
import "./editor/milkdown/BlockEdit/SlashMenu.css";
import "./editor/milkdown/CodeMirror/CodeMirror.css";
import "./editor/milkdown/ImageBlock/ImageBlock.css";

interface MilkdownEditorProps {
  value: string;
  onChange: (value: string) => void;
  className?: string;
}

export const MilkdownEditor: React.FC<MilkdownEditorProps> = ({
  value,
  onChange,
  className = "",
}) => {
  const pluginViewFactory = usePluginViewFactory();
  const currentValueRef = React.useRef(value);
  const isUpdatingRef = React.useRef(false);

  const { get } = useEditor((root) =>
    Editor.make()
      .config(nord)
      .config((ctx) => {
        ctx.set(rootCtx, root);
        ctx.set(defaultValueCtx, value);
        ctx.get(listenerCtx).markdownUpdated((_, markdown) => {
          if (!isUpdatingRef.current && markdown !== currentValueRef.current) {
            currentValueRef.current = markdown;
            onChange(markdown);
          }
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
          configureRefractor: () => {
            configurePrismLanguages();
          },
        });
      })
      .use(prism)
      .use(indent)
      .config((ctx) => {
        ctx.set(tooltip.key, {
          view: pluginViewFactory({
            component: TooltipView,
          }),
        });
      })
      .use(tooltip)
      .use(blockEdit.configCtx)
      .config(blockEdit.configureSlashMenu)
      .use(blockEdit.plugins)
      .use(blockSpec)
      .config((ctx) => {
        ctx.set(blockSpec.key, {
          view: pluginViewFactory({
            component: BlockHandleView,
          }),
        });
      })
      .config((ctx) => {
        configureCodeMirror(ctx, {
          languages: codeMirrorLanguages,
          searchPlaceholder: "搜索编程语言",
          copyText: "复制代码",
          noResultText: "未找到匹配的语言",
          onCopy: (text: string) => {
            navigator.clipboard
              .writeText(text)
              .then(() => {
                console.log("代码已复制到剪贴板");
              })
              .catch((err) => {
                console.error("复制失败:", err);
              });
          },
        });
      })
      .use(codeBlockComponent)
      .config((ctx) => {
        configureImageBlock(ctx, imageBlockConfig);
      })
      .use(imageBlockComponent)
      .config((ctx) => {
        ctx.update(inlineImageConfig.key, (defaultConfig) => ({
          ...defaultConfig,
          onUpload: imageBlockConfig.onUpload, // 使用相同的上传逻辑
        }));
      })
      .use(imageInlineComponent)
  );

  // 当value变化时，更新编辑器内容
  React.useEffect(() => {
    if (get && value !== currentValueRef.current) {
      const editor = get();
      if (editor) {
        // 使用setTimeout避免在React渲染期间同步更新
        setTimeout(() => {
          isUpdatingRef.current = true;
          editor.action((ctx) => {
            const view = ctx.get(editorViewCtx);
            const parser = ctx.get(parserCtx);
            const doc = parser(value);
            if (doc) {
              const tr = view.state.tr.replaceWith(
                0,
                view.state.doc.content.size,
                doc.content
              );
              view.dispatch(tr);
              currentValueRef.current = value;
            }
          });
          // 延迟重置标志，确保编辑器更新完成
          setTimeout(() => {
            isUpdatingRef.current = false;
          }, 10);
        }, 0);
      }
    }
  }, [value, get]);

  return (
    <div className={`h-full w-full ${className}`}>
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
