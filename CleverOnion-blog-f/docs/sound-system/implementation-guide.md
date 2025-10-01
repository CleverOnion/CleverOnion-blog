# 实现指南

本文档提供音效系统的完整实现步骤，从零开始构建整个系统。

---

## 📌 使用说明

### 状态标记说明

本文档使用以下标记来跟踪实现进度：

- **⬜ 未开始**：任务尚未开始
- **◻️ 进行中**：任务正在进行
- **✅ 已完成**：任务已完成

### 如何使用

1. **开始任务时**：将步骤标题的 ⬜ 更改为 ◻️，并更新状态行
2. **完成任务时**：将步骤标题的 ◻️ 更改为 ✅，并更新状态行和顶部进度追踪的 checkbox
3. **跟踪总体进度**：查看顶部的进度追踪列表

**示例**：

```markdown
### 步骤 1: 创建类型定义 ✅

> **状态**: ⬜ 未开始 | ◻️ 进行中 | ✅ 已完成
```

---

## 📋 实现步骤概览

### 进度追踪

**Phase 1: 基础架构**

- [x] 1. 创建类型定义
- [x] 2. 实现 SoundEngine（音效引擎）
- [x] 3. 实现 SoundConfigManager（配置管理）
- [x] 4. 实现 SoundManager（音效管理）
- [x] 5. 创建入口文件

**Phase 2: 集成应用**

- [x] 6. 集成到 API 拦截器
- [x] 7. 应用初始化
- [x] 8. 创建用户设置 UI

**Phase 3: 测试优化**

- [ ] 9. 功能测试
- [ ] 10. 性能优化

---

### 实现路线图

```
Phase 1: 基础架构
├── 1. 创建类型定义
├── 2. 实现 SoundEngine（音效引擎）
├── 3. 实现 SoundConfigManager（配置管理）
├── 4. 实现 SoundManager（音效管理）
└── 5. 创建入口文件

Phase 2: 集成应用
├── 6. 集成到 API 拦截器
├── 7. 应用初始化
└── 8. 创建用户设置 UI

Phase 3: 测试优化
├── 9. 功能测试
└── 10. 性能优化
```

---

## Phase 1: 基础架构

### 步骤 1: 创建类型定义 ✅

> **状态**: ⬜ 未开始 | ◻️ 进行中 | ✅ **已完成**

创建 `src/utils/sound/types.ts`：

```typescript
/**
 * 音效系统类型定义
 */

// 音效优先级
export enum SoundPriority {
  Low = 0, // 低优先级
  Normal = 1, // 普通优先级
  High = 2, // 高优先级
  Critical = 3, // 关键优先级
}

// 预设配置名称
export enum PresetName {
  Silent = "silent",
  ErrorsOnly = "errors-only",
  Minimal = "minimal",
  Standard = "standard",
  Full = "full",
}

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
```

---

### 步骤 2: 实现 SoundEngine（音效引擎） ✅

> **状态**: ⬜ 未开始 | ◻️ 进行中 | ✅ **已完成**

创建 `src/utils/sound/SoundEngine.ts`：

```typescript
/**
 * 音效引擎 - 负责底层音频播放
 */

export class SoundEngine {
  private audioContext: AudioContext | null = null;
  private gainNode: GainNode | null = null;
  private bufferCache: Map<string, AudioBuffer> = new Map();
  private stats = {
    cacheHits: 0,
    cacheMisses: 0,
  };

  /**
   * 初始化音频上下文
   */
  init(): void {
    if (this.audioContext) return;

    try {
      this.audioContext = new (window.AudioContext ||
        (window as any).webkitAudioContext)();

      this.gainNode = this.audioContext.createGain();
      this.gainNode.connect(this.audioContext.destination);
    } catch (error) {
      console.warn("音频上下文初始化失败:", error);
    }
  }

  /**
   * 检查是否支持
   */
  isSupported(): boolean {
    return (
      typeof AudioContext !== "undefined" ||
      typeof (window as any).webkitAudioContext !== "undefined"
    );
  }

  /**
   * 加载音频文件
   */
  async loadSound(url: string): Promise<AudioBuffer | null> {
    // 检查缓存
    if (this.bufferCache.has(url)) {
      this.stats.cacheHits++;
      return this.bufferCache.get(url)!;
    }

    this.stats.cacheMisses++;

    if (!this.audioContext) {
      this.init();
    }

    if (!this.audioContext) {
      console.warn("音频上下文不可用");
      return null;
    }

    try {
      const response = await fetch(url);
      const arrayBuffer = await response.arrayBuffer();
      const audioBuffer = await this.audioContext.decodeAudioData(arrayBuffer);

      // 缓存
      this.bufferCache.set(url, audioBuffer);

      return audioBuffer;
    } catch (error) {
      console.warn(`音频加载失败: ${url}`, error);
      return null;
    }
  }

  /**
   * 播放音频
   */
  async play(buffer: AudioBuffer, volume: number = 1.0): Promise<void> {
    if (!this.audioContext || !this.gainNode) {
      this.init();
    }

    if (!this.audioContext || !this.gainNode) {
      return;
    }

    try {
      // 恢复音频上下文（浏览器可能会暂停）
      if (this.audioContext.state === "suspended") {
        await this.audioContext.resume();
      }

      const source = this.audioContext.createBufferSource();
      source.buffer = buffer;

      // 创建音量节点
      const gainNode = this.audioContext.createGain();
      gainNode.gain.value = Math.max(0, Math.min(1, volume));

      // 连接音频图
      source.connect(gainNode);
      gainNode.connect(this.gainNode);

      // 播放
      source.start(0);
    } catch (error) {
      console.warn("音频播放失败:", error);
    }
  }

  /**
   * 设置主音量
   */
  setMasterVolume(volume: number): void {
    if (this.gainNode) {
      this.gainNode.gain.value = Math.max(0, Math.min(1, volume));
    }
  }

  /**
   * 预加载音效
   */
  async preload(urls: string[]): Promise<void> {
    await Promise.all(urls.map((url) => this.loadSound(url)));
  }

  /**
   * 清除缓存
   */
  clearCache(): void {
    this.bufferCache.clear();
  }

  /**
   * 获取统计信息
   */
  getStats() {
    return {
      cacheHits: this.stats.cacheHits,
      cacheMisses: this.stats.cacheMisses,
      cacheSize: this.bufferCache.size,
    };
  }
}
```

---

### 步骤 3: 实现 SoundConfigManager（配置管理） ✅

> **状态**: ⬜ 未开始 | ◻️ 进行中 | ✅ **已完成**

创建 `src/utils/sound/SoundConfigManager.ts`：

```typescript
/**
 * 音效配置管理器
 */

import type { SoundConfig, PresetName } from "./types";

export class SoundConfigManager {
  private static readonly STORAGE_KEY = "sound-system-config";
  private static readonly VERSION = "1.0.0";

  /**
   * 获取默认配置
   */
  static getDefaultConfig(): SoundConfig {
    return {
      version: this.VERSION,
      master: {
        enabled: true,
        volume: 70,
        respectSystemSettings: true,
      },
      categories: {
        api: {
          enabled: true,
          volume: 50,
          throttle: 1000,
          scenes: {
            "api.success.get": {
              enabled: false,
              priority: 0, // Low
            },
            "api.success.post": {
              enabled: true,
              priority: 1, // Normal
            },
            "api.success.put": {
              enabled: true,
              priority: 1, // Normal
            },
            "api.success.patch": {
              enabled: true,
              priority: 1, // Normal
            },
            "api.success.delete": {
              enabled: true,
              priority: 2, // High
            },
            "api.error.400": {
              enabled: true,
              priority: 1, // Normal
            },
            "api.error.401": {
              enabled: true,
              priority: 2, // High
            },
            "api.error.403": {
              enabled: true,
              priority: 2, // High
            },
            "api.error.404": {
              enabled: true,
              priority: 1, // Normal
            },
            "api.error.4xx": {
              enabled: true,
              priority: 2, // High
            },
            "api.error.500": {
              enabled: true,
              priority: 3, // Critical
            },
            "api.error.503": {
              enabled: true,
              priority: 3, // Critical
            },
            "api.error.5xx": {
              enabled: true,
              priority: 3, // Critical
            },
            "api.error.timeout": {
              enabled: true,
              priority: 2, // High
            },
            "api.error.network": {
              enabled: true,
              priority: 3, // Critical
            },
          },
        },
      },
    };
  }

  /**
   * 加载配置
   */
  static load(): SoundConfig {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (!stored) {
        return this.getDefaultConfig();
      }

      const userConfig = JSON.parse(stored);
      return this.merge(this.getDefaultConfig(), userConfig);
    } catch (error) {
      console.warn("配置加载失败，使用默认配置:", error);
      return this.getDefaultConfig();
    }
  }

  /**
   * 保存配置
   */
  static save(config: SoundConfig): void {
    try {
      config.version = this.VERSION;
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(config));
    } catch (error) {
      console.warn("配置保存失败:", error);
    }
  }

  /**
   * 重置配置
   */
  static reset(): SoundConfig {
    const defaultConfig = this.getDefaultConfig();
    this.save(defaultConfig);
    return defaultConfig;
  }

  /**
   * 导出配置
   */
  static export(config: SoundConfig): string {
    return JSON.stringify(config, null, 2);
  }

  /**
   * 导入配置
   */
  static import(json: string): SoundConfig {
    try {
      const imported = JSON.parse(json);
      const merged = this.merge(this.getDefaultConfig(), imported);
      this.save(merged);
      return merged;
    } catch (error) {
      console.warn("配置导入失败:", error);
      return this.getDefaultConfig();
    }
  }

  /**
   * 获取预设配置
   */
  static getPreset(name: PresetName): SoundConfig {
    const base = this.getDefaultConfig();

    switch (name) {
      case "silent":
        return {
          ...base,
          master: { ...base.master, enabled: false, volume: 0 },
        };

      case "errors-only":
        return {
          ...base,
          categories: {
            api: {
              ...base.categories.api,
              scenes: {
                ...base.categories.api.scenes,
                "api.success.get": {
                  ...base.categories.api.scenes["api.success.get"],
                  enabled: false,
                },
                "api.success.post": {
                  ...base.categories.api.scenes["api.success.post"],
                  enabled: false,
                },
                "api.success.put": {
                  ...base.categories.api.scenes["api.success.put"],
                  enabled: false,
                },
                "api.success.patch": {
                  ...base.categories.api.scenes["api.success.patch"],
                  enabled: false,
                },
                "api.success.delete": {
                  ...base.categories.api.scenes["api.success.delete"],
                  enabled: false,
                },
              },
            },
          },
        };

      case "minimal":
        return {
          ...base,
          master: { ...base.master, volume: 50 },
          categories: {
            api: {
              ...base.categories.api,
              throttle: 2000,
              scenes: {
                ...base.categories.api.scenes,
                "api.success.get": {
                  ...base.categories.api.scenes["api.success.get"],
                  enabled: false,
                },
              },
            },
          },
        };

      case "full":
        return {
          ...base,
          master: { ...base.master, volume: 80 },
          categories: {
            api: {
              ...base.categories.api,
              volume: 70,
              throttle: 500,
              scenes: {
                ...base.categories.api.scenes,
                "api.success.get": {
                  ...base.categories.api.scenes["api.success.get"],
                  enabled: true,
                },
              },
            },
          },
        };

      case "standard":
      default:
        return base;
    }
  }

  /**
   * 深度合并配置
   */
  private static merge(target: any, source: any): any {
    const result = { ...target };

    for (const key in source) {
      if (
        source[key] &&
        typeof source[key] === "object" &&
        !Array.isArray(source[key])
      ) {
        result[key] = this.merge(target[key] || {}, source[key]);
      } else {
        result[key] = source[key];
      }
    }

    return result;
  }
}
```

---

### 步骤 4: 实现 SoundManager（音效管理） ✅

> **状态**: ⬜ 未开始 | ◻️ 进行中 | ✅ **已完成**

创建 `src/utils/sound/SoundManager.ts`：

```typescript
/**
 * 音效管理器 - 主入口
 */

import { SoundEngine } from "./SoundEngine";
import { SoundConfigManager } from "./SoundConfigManager";
import type {
  SoundConfig,
  PlayOptions,
  PresetName,
  ThrottleState,
  SoundStats,
} from "./types";

export class SoundManager {
  private engine: SoundEngine;
  private config: SoundConfig;
  private throttleMap: Map<string, ThrottleState> = new Map();
  private stats = {
    totalPlayed: 0,
    throttled: 0,
  };
  private debugMode = false;
  private soundRegistry: Record<string, Record<string, string>> = {};

  constructor() {
    this.engine = new SoundEngine();
    this.config = SoundConfigManager.load();
  }

  /**
   * 初始化
   */
  init(): void {
    this.engine.init();
    this.log("音效系统已初始化");
  }

  /**
   * 注册音效文件
   */
  registerSounds(registry: Record<string, Record<string, string>>): void {
    this.soundRegistry = registry;
  }

  /**
   * 播放音效
   */
  async play(eventName: string, options?: PlayOptions): Promise<void> {
    // 1. 权限检查
    if (!this.shouldPlay(eventName, options)) {
      return;
    }

    // 2. 节流检查
    if (!options?.force && !this.checkThrottle(eventName)) {
      this.stats.throttled++;
      this.log(`音效被节流: ${eventName}`);
      return;
    }

    // 3. 获取音效文件路径
    const soundFile = this.getSoundFile(eventName);
    if (!soundFile) {
      this.log(`未找到音效文件: ${eventName}`);
      return;
    }

    // 4. 计算音量
    const volume = this.calculateVolume(eventName, options);

    // 5. 加载并播放
    try {
      const buffer = await this.engine.loadSound(soundFile);
      if (buffer) {
        if (options?.delay) {
          setTimeout(() => this.engine.play(buffer, volume), options.delay);
        } else {
          await this.engine.play(buffer, volume);
        }

        this.stats.totalPlayed++;
        this.log(`播放音效: ${eventName}, 音量: ${volume.toFixed(2)}`);
      }
    } catch (error) {
      console.warn(`音效播放失败: ${eventName}`, error);
    }
  }

  /**
   * 检查是否应该播放
   */
  private shouldPlay(eventName: string, options?: PlayOptions): boolean {
    // 全局开关
    if (!this.config.master.enabled) {
      return false;
    }

    // 获取分类
    const category = this.getCategory(eventName);
    if (!category) {
      return false;
    }

    // 分类开关
    const categoryConfig = this.config.categories[category];
    if (!categoryConfig?.enabled) {
      return false;
    }

    // 场景开关
    const sceneConfig = categoryConfig.scenes[eventName];
    if (sceneConfig && !sceneConfig.enabled) {
      return false;
    }

    // 上下文检查
    if (!this.checkContext()) {
      return false;
    }

    return true;
  }

  /**
   * 节流检查
   */
  private checkThrottle(eventName: string): boolean {
    const category = this.getCategory(eventName);
    if (!category) return true;

    const categoryConfig = this.config.categories[category];
    const throttleMs = categoryConfig?.throttle || 1000;

    const now = Date.now();
    const state = this.throttleMap.get(eventName);

    if (!state || now - state.lastPlayTime > throttleMs) {
      this.throttleMap.set(eventName, {
        lastPlayTime: now,
        playCount: 1,
      });
      return true;
    }

    return false;
  }

  /**
   * 上下文检查
   */
  private checkContext(): boolean {
    // 检查标签页可见性
    if (document.hidden) {
      return false;
    }

    // 检查系统设置
    if (this.config.master.respectSystemSettings) {
      const prefersReduced = window.matchMedia(
        "(prefers-reduced-motion: reduce)"
      ).matches;
      if (prefersReduced) {
        return false;
      }
    }

    return true;
  }

  /**
   * 计算音量
   */
  private calculateVolume(eventName: string, options?: PlayOptions): number {
    let volume = this.config.master.volume / 100;

    const category = this.getCategory(eventName);
    if (category) {
      const categoryConfig = this.config.categories[category];
      volume *= (categoryConfig?.volume || 100) / 100;

      const sceneConfig = categoryConfig?.scenes[eventName];
      if (sceneConfig?.volume !== undefined) {
        volume *= sceneConfig.volume / 100;
      }
    }

    if (options?.volume !== undefined) {
      volume *= options.volume;
    }

    return Math.max(0, Math.min(1, volume));
  }

  /**
   * 获取音效文件路径
   */
  private getSoundFile(eventName: string): string | null {
    const category = this.getCategory(eventName);
    if (!category) return null;

    // 检查自定义音效
    const categoryConfig = this.config.categories[category];
    const sceneConfig = categoryConfig?.scenes[eventName];
    if (sceneConfig?.soundFile) {
      return sceneConfig.soundFile;
    }

    // 从注册表获取
    const action = eventName.split(".")[1]; // 如 'success' 或 'error'
    return this.soundRegistry[category]?.[action] || null;
  }

  /**
   * 获取分类
   */
  private getCategory(eventName: string): string | null {
    const parts = eventName.split(".");
    return parts[0] || null;
  }

  /**
   * 设置主音量
   */
  setMasterVolume(volume: number): void {
    this.config.master.volume = Math.max(0, Math.min(100, volume));
    this.engine.setMasterVolume(volume / 100);
    SoundConfigManager.save(this.config);
  }

  /**
   * 设置全局开关
   */
  setEnabled(enabled: boolean): void {
    this.config.master.enabled = enabled;
    SoundConfigManager.save(this.config);
  }

  /**
   * 设置分类开关
   */
  setCategoryEnabled(category: string, enabled: boolean): void {
    if (this.config.categories[category]) {
      this.config.categories[category].enabled = enabled;
      SoundConfigManager.save(this.config);
    }
  }

  /**
   * 设置场景开关
   */
  setSceneEnabled(eventName: string, enabled: boolean): void {
    const category = this.getCategory(eventName);
    if (category && this.config.categories[category]) {
      const scene = this.config.categories[category].scenes[eventName];
      if (scene) {
        scene.enabled = enabled;
        SoundConfigManager.save(this.config);
      }
    }
  }

  /**
   * 获取配置
   */
  getConfig(): SoundConfig {
    return { ...this.config };
  }

  /**
   * 更新配置
   */
  updateConfig(partial: Partial<SoundConfig>): void {
    this.config = { ...this.config, ...partial };
    SoundConfigManager.save(this.config);
  }

  /**
   * 重置配置
   */
  resetConfig(): void {
    this.config = SoundConfigManager.reset();
  }

  /**
   * 加载预设
   */
  loadPreset(name: PresetName): void {
    this.config = SoundConfigManager.getPreset(name);
    SoundConfigManager.save(this.config);
  }

  /**
   * 导出配置
   */
  exportConfig(): string {
    return SoundConfigManager.export(this.config);
  }

  /**
   * 导入配置
   */
  importConfig(json: string): void {
    this.config = SoundConfigManager.import(json);
  }

  /**
   * 预加载音效
   */
  async preload(eventNames: string[]): Promise<void> {
    const urls: string[] = [];
    eventNames.forEach((eventName) => {
      const url = this.getSoundFile(eventName);
      if (url) urls.push(url);
    });

    await this.engine.preload(urls);
  }

  /**
   * 检查支持
   */
  isSupported(): boolean {
    return this.engine.isSupported();
  }

  /**
   * 设置调试模式
   */
  setDebugMode(enabled: boolean): void {
    this.debugMode = enabled;
  }

  /**
   * 获取统计信息
   */
  getStats(): SoundStats {
    const engineStats = this.engine.getStats();
    return {
      totalPlayed: this.stats.totalPlayed,
      throttled: this.stats.throttled,
      cacheHits: engineStats.cacheHits,
      cacheMisses: engineStats.cacheMisses,
    };
  }

  /**
   * 日志
   */
  private log(message: string): void {
    if (this.debugMode) {
      console.log(`[SoundManager] ${message}`);
    }
  }
}
```

---

### 步骤 5: 创建入口文件 ✅

> **状态**: ⬜ 未开始 | ◻️ 进行中 | ✅ **已完成**

创建 `src/utils/sound/index.ts`：

```typescript
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
```

---

## Phase 2: 集成应用

### 步骤 6: 集成到 API 拦截器 ✅

> **状态**: ⬜ 未开始 | ◻️ 进行中 | ✅ **已完成**

修改 `src/api/index.ts`：

```typescript
import axios from "axios";
import type {
  AxiosInstance,
  InternalAxiosRequestConfig,
  AxiosResponse,
  AxiosError,
} from "axios";
import { soundManager } from "@/utils/sound"; // 导入音效管理器

// ... 其他代码 ...

// 响应拦截器
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // 成功响应
    const method = response.config.method?.toLowerCase() || "get";
    const eventName = `api.success.${method}`;
    soundManager.play(eventName);

    return response;
  },
  (error: AxiosError) => {
    // 错误响应
    let eventName = "api.error.network";

    if (error.response?.status) {
      const status = error.response.status;
      eventName = `api.error.${status}`;

      // 优先级设置
      const priority = status >= 500 ? "critical" : "high";
      soundManager.play(eventName, { priority });
    } else if (error.code === "ECONNABORTED") {
      soundManager.play("api.error.timeout", { priority: "high" });
    } else {
      soundManager.play("api.error.network", { priority: "critical" });
    }

    return Promise.reject(error);
  }
);
```

---

### 步骤 7: 应用初始化 ✅

> **状态**: ⬜ 未开始 | ◻️ 进行中 | ✅ **已完成**

修改 `src/main.tsx`：

```typescript
import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { soundManager } from "@/utils/sound";

// 初始化音效系统
document.addEventListener(
  "click",
  () => {
    soundManager.init();
  },
  { once: true }
);

// 预加载常用音效
soundManager.preload(["api.success.post", "api.error.500"]);

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

---

### 步骤 8: 创建用户设置 UI ✅

> **状态**: ⬜ 未开始 | ◻️ 进行中 | ✅ **已完成**

创建 `src/components/settings/SoundSettings.tsx`：

```typescript
import React, { useState } from "react";
import { soundManager, PresetName } from "@/utils/sound";

export function SoundSettings() {
  const [config, setConfig] = useState(soundManager.getConfig());

  const handleToggle = (enabled: boolean) => {
    soundManager.setEnabled(enabled);
    setConfig(soundManager.getConfig());
  };

  const handleVolumeChange = (volume: number) => {
    soundManager.setMasterVolume(volume);
    setConfig(soundManager.getConfig());

    // 播放示例音效
    soundManager.play("api.success.post", { force: true });
  };

  const handlePresetChange = (preset: string) => {
    soundManager.loadPreset(preset as PresetName);
    setConfig(soundManager.getConfig());

    // 播放示例音效
    soundManager.play("api.success.post", { force: true });
  };

  return (
    <div className="sound-settings">
      <h2>音效设置</h2>

      <div className="setting-item">
        <label>
          <input
            type="checkbox"
            checked={config.master.enabled}
            onChange={(e) => handleToggle(e.target.checked)}
          />
          启用音效
        </label>
      </div>

      <div className="setting-item">
        <label>
          音量: {config.master.volume}%
          <input
            type="range"
            min="0"
            max="100"
            value={config.master.volume}
            onChange={(e) => handleVolumeChange(Number(e.target.value))}
            disabled={!config.master.enabled}
          />
        </label>
      </div>

      <div className="setting-item">
        <label>
          预设方案:
          <select onChange={(e) => handlePresetChange(e.target.value)}>
            <option value="silent">静音</option>
            <option value="errors-only">仅错误</option>
            <option value="minimal">极简</option>
            <option value="standard">标准</option>
            <option value="full">完整</option>
          </select>
        </label>
      </div>
    </div>
  );
}
```

---

## Phase 3: 测试优化

### 步骤 9: 功能测试 ⬜

> **状态**: ⬜ 未开始 | ◻️ 进行中 | ✅ 已完成

创建测试文件 `src/utils/sound/__tests__/SoundManager.test.ts`：

```typescript
import { describe, it, expect, beforeEach } from "vitest";
import { SoundManager } from "../SoundManager";

describe("SoundManager", () => {
  let manager: SoundManager;

  beforeEach(() => {
    manager = new SoundManager();
  });

  it("应该初始化", () => {
    expect(manager).toBeDefined();
    expect(manager.isSupported()).toBe(true);
  });

  it("应该加载默认配置", () => {
    const config = manager.getConfig();
    expect(config.master.enabled).toBe(true);
    expect(config.master.volume).toBe(70);
  });

  it("应该设置主音量", () => {
    manager.setMasterVolume(50);
    const config = manager.getConfig();
    expect(config.master.volume).toBe(50);
  });

  it("应该加载预设", () => {
    manager.loadPreset("silent");
    const config = manager.getConfig();
    expect(config.master.enabled).toBe(false);
  });
});
```

---

### 步骤 10: 性能优化 ⬜

> **状态**: ⬜ 未开始 | ◻️ 进行中 | ✅ 已完成

**优化清单**

1. **音效文件优化**

   - 使用音频编辑工具（如 Audacity）压缩文件
   - 目标大小：< 20KB
   - 格式：MP3（128kbps）

2. **预加载策略**

   ```typescript
   // 应用启动时预加载高频音效
   soundManager.preload(["api.success.post", "api.error.500"]);
   ```

3. **节流配置**

   ```typescript
   // API 分类默认 1 秒节流
   throttle: 1000;
   ```

4. **内存管理**
   ```typescript
   // 定期清理未使用的缓存（可选）
   setInterval(() => {
     if (soundManager.getStats().cacheSize > 20) {
       soundManager.clearCache();
     }
   }, 60000); // 每分钟检查一次
   ```

---

## 🔮 扩展新音效类型

### 示例：添加 UI 交互音效

**1. 添加音效文件**

```
src/assets/sounds/ui/
├── click.mp3
├── toggle.mp3
└── swipe.mp3
```

**2. 注册音效**

```typescript
// src/utils/sound/index.ts
const SOUND_REGISTRY = {
  api: { ... },
  interaction: {
    click: '/src/assets/sounds/ui/click.mp3',
    toggle: '/src/assets/sounds/ui/toggle.mp3',
    swipe: '/src/assets/sounds/ui/swipe.mp3'
  }
};
```

**3. 添加默认配置**

```typescript
// src/utils/sound/SoundConfigManager.ts
categories: {
  api: { ... },
  interaction: {
    enabled: true,
    volume: 30,
    throttle: 100,
    scenes: {
      'interaction.click': { enabled: true, priority: 0 },
      'interaction.toggle': { enabled: true, priority: 1 }
    }
  }
}
```

**4. 在组件中使用**

```typescript
import { soundManager } from "@/utils/sound";

function Button({ onClick, children }) {
  const handleClick = (e) => {
    soundManager.play("interaction.click");
    onClick?.(e);
  };

  return <button onClick={handleClick}>{children}</button>;
}
```

**完成！** 🎉

---

## 📊 总体进度概览

| Phase       | 步骤                       | 状态 | 关键检查点                     |
| ----------- | -------------------------- | ---- | ------------------------------ |
| **Phase 1** | 1. 创建类型定义            | ✅   | 类型定义文件创建完成           |
| **Phase 1** | 2. 实现 SoundEngine        | ✅   | 音频播放、缓存功能正常         |
| **Phase 1** | 3. 实现 SoundConfigManager | ✅   | 配置加载/保存/预设功能正常     |
| **Phase 1** | 4. 实现 SoundManager       | ✅   | 播放、节流、权限检查功能正常   |
| **Phase 1** | 5. 创建入口文件            | ✅   | 导出正确，音效注册完成         |
| **Phase 2** | 6. 集成到 API 拦截器       | ✅   | API 请求成功/失败时播放音效    |
| **Phase 2** | 7. 应用初始化              | ✅   | 音效系统正确初始化，预加载完成 |
| **Phase 2** | 8. 创建用户设置 UI         | ✅   | 设置面板功能完整，配置可保存   |
| **Phase 3** | 9. 功能测试                | ⬜   | 所有测试通过                   |
| **Phase 3** | 10. 性能优化               | ⬜   | 音效文件优化，内存管理良好     |

**完成度**: 8 / 10 (80%) 🎉🎉

---

## 📝 质量检查清单

实现完成后进行以下检查：

### 功能完整性

- [ ] 所有类型定义完整且正确
- [ ] SoundEngine 正常工作（加载、播放、缓存）
- [ ] SoundConfigManager 能保存/加载配置
- [ ] SoundManager 核心功能正常（播放、节流、优先级）
- [ ] API 拦截器已集成并正常触发音效
- [ ] 音效文件已添加且路径正确
- [ ] 用户设置 UI 已创建且功能完整
- [ ] 应用初始化正确（音频上下文、预加载）

### 系统功能

- [ ] 节流机制生效（防止音效过于频繁）
- [ ] 优先级系统工作（高优先级优先播放）
- [ ] 上下文感知正常（标签页隐藏时不播放）
- [ ] 配置持久化正常（LocalStorage 存储）
- [ ] 预设切换功能正常（5 种预设可用）

### 性能与优化

- [ ] 性能优化完成（音效文件 < 20KB）
- [ ] 内存管理良好（缓存合理）
- [ ] 预加载策略有效（常用音效预加载）
- [ ] 无内存泄漏

### 用户体验

- [ ] 音效音量合理（默认配置舒适）
- [ ] 节流间隔合适（不会太吵）
- [ ] 设置面板易用（调整方便）
- [ ] 错误处理完善（音效加载失败不影响功能）

### 文档

- [ ] 代码注释完整
- [ ] 类型定义准确
- [ ] 使用示例清晰
- [ ] 实现进度已更新

---

## 🐛 常见问题

### 1. 音效无法播放

**原因**: 浏览器要求用户交互后才能播放音频

**解决**:

```typescript
// 在用户首次点击时初始化
document.addEventListener(
  "click",
  () => {
    soundManager.init();
  },
  { once: true }
);
```

### 2. 音效延迟

**原因**: 音频文件未预加载

**解决**:

```typescript
// 应用启动时预加载
soundManager.preload(["api.success.post", "api.error.500"]);
```

### 3. 音效过于频繁

**原因**: 节流间隔太短

**解决**:

```typescript
// 增加节流间隔
soundManager.updateConfig({
  categories: {
    api: { throttle: 2000 }, // 2 秒
  },
});
```

---

## 🔗 相关资源

- [Web Audio API 文档](https://developer.mozilla.org/zh-CN/docs/Web/API/Web_Audio_API)
- [系统架构](./architecture.md)
- [API 参考](./api-reference.md)
- [配置说明](./configuration.md)

---

**最后更新**: 2025-10-01
