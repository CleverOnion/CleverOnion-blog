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
  private shouldPlay(eventName: string, _options?: PlayOptions): boolean {
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
