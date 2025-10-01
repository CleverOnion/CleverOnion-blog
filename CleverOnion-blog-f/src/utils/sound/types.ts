/**
 * 音效系统类型定义
 */

// 音效优先级
export type SoundPriority =
  (typeof SoundPriorityValues)[keyof typeof SoundPriorityValues];

export const SoundPriorityValues = {
  Low: 0,
  Normal: 1,
  High: 2,
  Critical: 3,
} as const;

// 预设配置名称
export type PresetName = (typeof PresetNames)[keyof typeof PresetNames];

export const PresetNames = {
  Silent: "silent",
  ErrorsOnly: "errors-only",
  Minimal: "minimal",
  Standard: "standard",
  Full: "full",
} as const;

// 播放选项
export interface PlayOptions {
  volume?: number; // 音量 0.0-1.0
  priority?: SoundPriority; // 优先级
  force?: boolean; // 强制播放，忽略节流
  delay?: number; // 延迟播放（毫秒）
}

// 场景配置
export interface SceneConfig {
  enabled: boolean;
  volume?: number;
  priority?: SoundPriority;
  soundFile?: string;
}

// 分类配置
export interface CategoryConfig {
  enabled: boolean;
  volume: number;
  throttle: number;
  scenes: Record<string, SceneConfig>;
}

// 完整配置
export interface SoundConfig {
  version: string;
  master: {
    enabled: boolean;
    volume: number;
    respectSystemSettings: boolean;
  };
  categories: Record<string, CategoryConfig>;
}

// 统计信息
export interface SoundStats {
  totalPlayed: number;
  throttled: number;
  cacheHits: number;
  cacheMisses: number;
}

// 节流状态
export interface ThrottleState {
  lastPlayTime: number;
  playCount: number;
}
