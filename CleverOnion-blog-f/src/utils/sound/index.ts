/**
 * 音效系统入口
 */

import { SoundManager } from "./SoundManager";
import successSound from "../../assets/mp3/success.mp3";
import failSound from "../../assets/mp3/fail.mp3";

// 音效文件注册表
const SOUND_REGISTRY = {
  api: {
    success: successSound,
    error: failSound,
  },
};

// 创建全局实例
export const soundManager = new SoundManager();
soundManager.registerSounds(SOUND_REGISTRY);

// 导出类型
export * from "./types";
export { SoundManager } from "./SoundManager";
