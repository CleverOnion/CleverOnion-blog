import { imageBlockComponent, imageBlockConfig } from '@milkdown/kit/component/image-block';
import type { Ctx } from '@milkdown/kit/ctx';

/**
 * ImageBlock 配置选项
 */
export interface ImageBlockOptions {
  /** 空图片块占位符图标 */
  imageIcon?: string;
  /** 标题切换按钮图标 */
  captionIcon?: string;
  /** 上传按钮文本 */
  uploadButton?: string;
  /** 确认按钮文本 */
  confirmButton?: string;
  /** 图片块占位符文本 */
  uploadPlaceholderText?: string;
  /** 标题输入占位符文本 */
  captionPlaceholderText?: string;
  /** 图片上传处理函数 */
  onUpload?: (file: File) => Promise<string>;
  /** 图片URL代理函数 */
  proxyDomURL?: (url: string) => Promise<string> | string;
}

/**
 * 配置 ImageBlock 组件
 */
export function configureImageBlock(ctx: Ctx, options: ImageBlockOptions = {}) {
  const {
    imageIcon = '🌌',
    captionIcon = '💬',
    uploadButton = '上传文件',
    confirmButton = '确认 ⏎',
    uploadPlaceholderText = '或粘贴图片链接...',
    captionPlaceholderText = '图片标题',
    onUpload = async (file: File) => {
      // 默认使用 URL.createObjectURL 创建本地预览
      return Promise.resolve(URL.createObjectURL(file));
    },
    proxyDomURL
  } = options;

  ctx.update(imageBlockConfig.key, (defaultConfig) => ({
    ...defaultConfig,
    imageIcon,
    captionIcon,
    uploadButton,
    confirmButton,
    uploadPlaceholderText,
    captionPlaceholderText,
    onUpload,
    proxyDomURL
  }));
}

// 导出组件和配置
export { imageBlockComponent, imageBlockConfig };
export default imageBlockComponent;