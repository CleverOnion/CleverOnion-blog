/**
 * 音效配置管理器
 */

import type { SoundConfig, PresetName } from "./types";
import { SoundPriorityValues } from "./types";

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
              priority: SoundPriorityValues.Low,
            },
            "api.success.post": {
              enabled: true,
              priority: SoundPriorityValues.Normal,
            },
            "api.success.put": {
              enabled: true,
              priority: SoundPriorityValues.Normal,
            },
            "api.success.patch": {
              enabled: true,
              priority: SoundPriorityValues.Normal,
            },
            "api.success.delete": {
              enabled: true,
              priority: SoundPriorityValues.High,
            },
            "api.error.400": {
              enabled: true,
              priority: SoundPriorityValues.Normal,
            },
            "api.error.401": {
              enabled: true,
              priority: SoundPriorityValues.High,
            },
            "api.error.403": {
              enabled: true,
              priority: SoundPriorityValues.High,
            },
            "api.error.404": {
              enabled: true,
              priority: SoundPriorityValues.Normal,
            },
            "api.error.4xx": {
              enabled: true,
              priority: SoundPriorityValues.High,
            },
            "api.error.500": {
              enabled: true,
              priority: SoundPriorityValues.Critical,
            },
            "api.error.503": {
              enabled: true,
              priority: SoundPriorityValues.Critical,
            },
            "api.error.5xx": {
              enabled: true,
              priority: SoundPriorityValues.Critical,
            },
            "api.error.timeout": {
              enabled: true,
              priority: SoundPriorityValues.High,
            },
            "api.error.network": {
              enabled: true,
              priority: SoundPriorityValues.Critical,
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
  private static merge(
    target: SoundConfig,
    source: Partial<SoundConfig>
  ): SoundConfig {
    const result = { ...target } as SoundConfig;

    for (const key in source) {
      const sourceValue = source[key as keyof SoundConfig];
      const targetValue = target[key as keyof SoundConfig];

      if (
        sourceValue &&
        typeof sourceValue === "object" &&
        !Array.isArray(sourceValue) &&
        targetValue &&
        typeof targetValue === "object"
      ) {
        (result as unknown as Record<string, unknown>)[key] = this.merge(
          targetValue as unknown as SoundConfig,
          sourceValue as unknown as Partial<SoundConfig>
        );
      } else if (sourceValue !== undefined) {
        (result as unknown as Record<string, unknown>)[key] = sourceValue;
      }
    }

    return result;
  }
}
