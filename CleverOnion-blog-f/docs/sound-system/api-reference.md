# API 参考文档

## 📚 核心 API

### SoundManager

音效系统的主入口，提供播放和配置管理功能。

#### 导入

```typescript
import { soundManager } from "@/utils/sound";
```

---

### 方法

#### `play(eventName, options?)`

播放指定的音效。

**参数**

- `eventName: string` - 音效事件名称（必需）
- `options?: PlayOptions` - 播放选项（可选）

**返回值**

- `Promise<void>` - 播放完成的 Promise

**示例**

```typescript
// 基本用法
soundManager.play("api.success.post");

// 带选项
soundManager.play("api.error.500", {
  volume: 0.8,
  priority: "high",
});

// 异步等待
await soundManager.play("api.success.get");
```

**PlayOptions 接口**

```typescript
interface PlayOptions {
  volume?: number; // 0.0 - 1.0，覆盖配置的音量
  priority?: SoundPriority; // 播放优先级
  force?: boolean; // 强制播放，忽略节流
  delay?: number; // 延迟播放（毫秒）
}
```

**SoundPriority 枚举**

```typescript
enum SoundPriority {
  Low = 0, // 低优先级，可能被跳过
  Normal = 1, // 普通优先级（默认）
  High = 2, // 高优先级，优先播放
  Critical = 3, // 关键优先级，立即播放
}
```

---

#### `setMasterVolume(volume)`

设置主音量。

**参数**

- `volume: number` - 音量值（0-100）

**示例**

```typescript
soundManager.setMasterVolume(70); // 设置为 70%
```

---

#### `setEnabled(enabled)`

设置全局开关。

**参数**

- `enabled: boolean` - 是否启用音效

**示例**

```typescript
soundManager.setEnabled(false); // 关闭所有音效
soundManager.setEnabled(true); // 开启音效
```

---

#### `setCategoryEnabled(category, enabled)`

设置分类开关。

**参数**

- `category: string` - 分类名称（如 'api'）
- `enabled: boolean` - 是否启用

**示例**

```typescript
soundManager.setCategoryEnabled("api", false); // 关闭 API 音效
```

---

#### `setSceneEnabled(eventName, enabled)`

设置具体场景开关。

**参数**

- `eventName: string` - 事件名称
- `enabled: boolean` - 是否启用

**示例**

```typescript
soundManager.setSceneEnabled("api.success.get", false); // 关闭 GET 成功音效
```

---

#### `getConfig()`

获取当前配置。

**返回值**

- `SoundConfig` - 当前音效配置对象

**示例**

```typescript
const config = soundManager.getConfig();
console.log(config.master.volume); // 70
```

---

#### `updateConfig(config)`

更新配置（部分更新）。

**参数**

- `config: Partial<SoundConfig>` - 部分配置对象

**示例**

```typescript
soundManager.updateConfig({
  master: {
    volume: 80,
  },
  categories: {
    api: {
      throttle: 2000,
    },
  },
});
```

---

#### `resetConfig()`

重置为默认配置。

**示例**

```typescript
soundManager.resetConfig();
```

---

#### `loadPreset(presetName)`

加载预设配置。

**参数**

- `presetName: PresetName` - 预设名称

**PresetName 枚举**

```typescript
enum PresetName {
  Silent = "silent", // 静音模式
  ErrorsOnly = "errors-only", // 仅错误提示
  Minimal = "minimal", // 极简模式
  Standard = "standard", // 标准模式（默认）
  Full = "full", // 完整体验
}
```

**示例**

```typescript
soundManager.loadPreset("errors-only"); // 只播放错误音效
soundManager.loadPreset("silent"); // 完全静音
```

---

#### `exportConfig()`

导出配置为 JSON 字符串。

**返回值**

- `string` - JSON 格式的配置

**示例**

```typescript
const json = soundManager.exportConfig();
localStorage.setItem("my-backup", json);
```

---

#### `importConfig(json)`

从 JSON 字符串导入配置。

**参数**

- `json: string` - JSON 格式的配置

**示例**

```typescript
const json = localStorage.getItem("my-backup");
if (json) {
  soundManager.importConfig(json);
}
```

---

#### `preload(eventNames)`

预加载音效文件。

**参数**

- `eventNames: string[]` - 事件名称数组

**返回值**

- `Promise<void>` - 预加载完成的 Promise

**示例**

```typescript
// 应用启动时预加载常用音效
await soundManager.preload(["api.success.post", "api.error.500"]);
```

---

### 配置对象

#### SoundConfig

```typescript
interface SoundConfig {
  version: string; // 配置版本

  master: {
    enabled: boolean; // 全局开关
    volume: number; // 主音量 0-100
    respectSystemSettings: boolean; // 尊重系统减少动效设置
  };

  categories: {
    [category: string]: CategoryConfig;
  };
}
```

#### CategoryConfig

```typescript
interface CategoryConfig {
  enabled: boolean; // 分类开关
  volume: number; // 分类音量 0-100（相对于主音量）
  throttle: number; // 节流间隔（毫秒）

  scenes: {
    [eventName: string]: SceneConfig;
  };
}
```

#### SceneConfig

```typescript
interface SceneConfig {
  enabled: boolean; // 场景开关
  volume?: number; // 场景音量（覆盖分类音量）
  priority?: SoundPriority; // 优先级
  soundFile?: string; // 自定义音效文件路径
}
```

---

## 🎵 事件名称列表

### API 音效

#### 成功音效

| 事件名称             | 描述            | 默认优先级 | 默认启用 |
| -------------------- | --------------- | ---------- | -------- |
| `api.success.get`    | GET 请求成功    | Low        | ❌ 否    |
| `api.success.post`   | POST 请求成功   | Normal     | ✅ 是    |
| `api.success.put`    | PUT 请求成功    | Normal     | ✅ 是    |
| `api.success.patch`  | PATCH 请求成功  | Normal     | ✅ 是    |
| `api.success.delete` | DELETE 请求成功 | High       | ✅ 是    |

#### 错误音效

| 事件名称            | 描述           | 默认优先级 | 默认启用 |
| ------------------- | -------------- | ---------- | -------- |
| `api.error.400`     | 400 错误请求   | Normal     | ✅ 是    |
| `api.error.401`     | 401 未授权     | High       | ✅ 是    |
| `api.error.403`     | 403 禁止访问   | High       | ✅ 是    |
| `api.error.404`     | 404 未找到     | Normal     | ✅ 是    |
| `api.error.4xx`     | 其他 4xx 错误  | High       | ✅ 是    |
| `api.error.500`     | 500 服务器错误 | Critical   | ✅ 是    |
| `api.error.503`     | 503 服务不可用 | Critical   | ✅ 是    |
| `api.error.5xx`     | 其他 5xx 错误  | Critical   | ✅ 是    |
| `api.error.timeout` | 请求超时       | High       | ✅ 是    |
| `api.error.network` | 网络错误       | Critical   | ✅ 是    |

---

## 🔧 实用工具

### useSoundEffect Hook

React Hook，用于在组件中播放音效。

```typescript
import { useSoundEffect } from "@/hooks/useSoundEffect";

function MyComponent() {
  const playSound = useSoundEffect();

  const handleClick = () => {
    playSound("interaction.click.button");
  };

  return <button onClick={handleClick}>Click Me</button>;
}
```

**Hook 实现**

```typescript
import { useCallback } from "react";
import { soundManager } from "@/utils/sound";

export function useSoundEffect() {
  return useCallback((eventName: string, options?: PlayOptions) => {
    soundManager.play(eventName, options);
  }, []);
}
```

---

### withSound HOC

高阶组件，为组件添加音效功能。

```typescript
import { withSound } from "@/utils/sound/hoc";

const Button = ({ onClick, ...props }) => (
  <button onClick={onClick} {...props} />
);

const SoundButton = withSound(Button, {
  onClick: "interaction.click.button",
});

// 使用
<SoundButton onClick={handleClick}>Click Me</SoundButton>;
```

---

### SoundTrigger 组件

声明式音效触发组件。

```typescript
import { SoundTrigger } from "@/components/sound";

<SoundTrigger event="api.success.post">
  {(play) => (
    <button
      onClick={() => {
        // 业务逻辑
        play(); // 播放音效
      }}
    >
      Submit
    </button>
  )}
</SoundTrigger>;
```

---

## 📊 事件监听

### 监听配置变化

```typescript
soundManager.on("config-changed", (config: SoundConfig) => {
  console.log("配置已更新:", config);
});
```

### 监听音效播放

```typescript
soundManager.on("sound-played", (eventName: string) => {
  console.log("播放音效:", eventName);
});
```

### 监听音效被节流

```typescript
soundManager.on("sound-throttled", (eventName: string) => {
  console.log("音效被节流:", eventName);
});
```

---

## 🎯 使用场景示例

### 场景 1: API 请求集成

```typescript
// src/api/index.ts
import { soundManager } from "@/utils/sound";

apiClient.interceptors.response.use(
  (response) => {
    const method = response.config.method?.toLowerCase();
    soundManager.play(`api.success.${method}`);
    return response;
  },
  (error) => {
    const status = error.response?.status;

    if (status) {
      soundManager.play(`api.error.${status}`, {
        priority: status >= 500 ? "critical" : "high",
      });
    } else if (error.code === "ECONNABORTED") {
      soundManager.play("api.error.timeout", { priority: "high" });
    } else {
      soundManager.play("api.error.network", { priority: "critical" });
    }

    return Promise.reject(error);
  }
);
```

### 场景 2: 表单提交

```typescript
const handleSubmit = async (data: FormData) => {
  try {
    await submitForm(data);
    // API 拦截器会自动播放 api.success.post
  } catch (error) {
    // API 拦截器会自动播放 api.error.xxx
    console.error(error);
  }
};
```

### 场景 3: 用户设置面板

```typescript
function SoundSettings() {
  const [config, setConfig] = useState(soundManager.getConfig());

  const handleVolumeChange = (volume: number) => {
    soundManager.setMasterVolume(volume);
    setConfig(soundManager.getConfig());

    // 播放示例音效
    soundManager.play("api.success.post", { force: true });
  };

  const handleToggle = (enabled: boolean) => {
    soundManager.setEnabled(enabled);
    setConfig(soundManager.getConfig());
  };

  return (
    <div>
      <Switch
        checked={config.master.enabled}
        onChange={handleToggle}
        label="启用音效"
      />

      <Slider
        value={config.master.volume}
        onChange={handleVolumeChange}
        min={0}
        max={100}
        label="音量"
      />
    </div>
  );
}
```

### 场景 4: 预设切换

```typescript
function SoundPresets() {
  const presets = [
    { name: "silent", label: "静音" },
    { name: "errors-only", label: "仅错误" },
    { name: "minimal", label: "极简" },
    { name: "standard", label: "标准" },
    { name: "full", label: "完整" },
  ];

  const handlePresetChange = (preset: string) => {
    soundManager.loadPreset(preset);

    // 播放示例音效
    soundManager.play("api.success.post", { force: true });
  };

  return (
    <Select onChange={handlePresetChange}>
      {presets.map((p) => (
        <option key={p.name} value={p.name}>
          {p.label}
        </option>
      ))}
    </Select>
  );
}
```

---

## ⚠️ 注意事项

### 1. 浏览器兼容性

```typescript
// 检查音效支持
if (!soundManager.isSupported()) {
  console.warn("当前浏览器不支持音效功能");
}
```

### 2. 用户交互要求

Web Audio API 要求用户交互后才能播放音效（浏览器限制）。

```typescript
// 在用户首次交互时初始化音频上下文
document.addEventListener(
  "click",
  () => {
    soundManager.init(); // 初始化音频上下文
  },
  { once: true }
);
```

### 3. 性能考虑

```typescript
// ❌ 避免在循环中播放音效
for (let i = 0; i < 100; i++) {
  soundManager.play("api.success.get"); // 会被节流
}

// ✅ 使用节流或只在关键时刻播放
soundManager.play("api.success.post"); // 只播放一次
```

### 4. 音效文件大小

- 建议单个音效文件 < 20KB
- 使用 MP3 格式（兼容性好）
- 音效时长 < 1 秒

---

## 🐛 调试

### 启用调试模式

```typescript
soundManager.setDebugMode(true);

// 控制台会输出：
// [SoundManager] Playing: api.success.post
// [SoundManager] Throttled: api.success.get
// [SoundManager] Config updated: { ... }
```

### 查看统计信息

```typescript
const stats = soundManager.getStats();
console.log(stats);
// {
//   totalPlayed: 42,
//   throttled: 15,
//   cacheHits: 38,
//   cacheMisses: 4
// }
```

---

## 📝 类型定义

### 完整类型定义文件

```typescript
// src/utils/sound/types.ts

export enum SoundPriority {
  Low = 0,
  Normal = 1,
  High = 2,
  Critical = 3,
}

export enum PresetName {
  Silent = "silent",
  ErrorsOnly = "errors-only",
  Minimal = "minimal",
  Standard = "standard",
  Full = "full",
}

export interface PlayOptions {
  volume?: number;
  priority?: SoundPriority;
  force?: boolean;
  delay?: number;
}

export interface SceneConfig {
  enabled: boolean;
  volume?: number;
  priority?: SoundPriority;
  soundFile?: string;
}

export interface CategoryConfig {
  enabled: boolean;
  volume: number;
  throttle: number;
  scenes: Record<string, SceneConfig>;
}

export interface SoundConfig {
  version: string;
  master: {
    enabled: boolean;
    volume: number;
    respectSystemSettings: boolean;
  };
  categories: Record<string, CategoryConfig>;
}

export interface SoundStats {
  totalPlayed: number;
  throttled: number;
  cacheHits: number;
  cacheMisses: number;
}

export interface ISoundManager {
  play(eventName: string, options?: PlayOptions): Promise<void>;
  setMasterVolume(volume: number): void;
  setEnabled(enabled: boolean): void;
  setCategoryEnabled(category: string, enabled: boolean): void;
  setSceneEnabled(eventName: string, enabled: boolean): void;
  getConfig(): SoundConfig;
  updateConfig(config: Partial<SoundConfig>): void;
  resetConfig(): void;
  loadPreset(presetName: PresetName): void;
  exportConfig(): string;
  importConfig(json: string): void;
  preload(eventNames: string[]): Promise<void>;
  isSupported(): boolean;
  init(): void;
  setDebugMode(enabled: boolean): void;
  getStats(): SoundStats;
  on(event: string, callback: Function): void;
  off(event: string, callback: Function): void;
}
```

---

## 🔗 相关文档

- [系统架构](./architecture.md)
- [配置说明](./configuration.md)
- [实现指南](./implementation-guide.md)

---

**最后更新**: 2025-10-01
