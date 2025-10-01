/**
 * 音效系统入口
 */

import { SoundManager } from "./SoundManager";

// 音效文件注册表
const SOUND_REGISTRY = {
  api: {
    success: "/src/assets/mp3/success.mp3",
    error: "/src/assets/mp3/fail.mp3",
  },
};

// 创建全局实例
export const soundManager = new SoundManager();
soundManager.registerSounds(SOUND_REGISTRY);

// 导出类型
export * from "./types";
export { SoundManager } from "./SoundManager";
