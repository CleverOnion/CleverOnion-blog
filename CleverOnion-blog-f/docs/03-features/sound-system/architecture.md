# 音效系统架构设计

## 🏗️ 总体架构

### 架构分层

```
┌──────────────────────────────────────────────────────────┐
│                    应用层 (Application)                    │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐         │
│  │ 设置面板 UI │  │ Toast 组件  │  │ 其他 UI组件 │         │
│  └────────────┘  └────────────┘  └────────────┘         │
└──────────────────────────────────────────────────────────┘
                        ↓ ↑
┌──────────────────────────────────────────────────────────┐
│                  配置层 (Configuration)                    │
│  ┌─────────────────────────────────────────────────┐    │
│  │           SoundConfigManager                     │    │
│  │  - 读取/保存配置                                  │    │
│  │  - 配置验证                                       │    │
│  │  - 配置合并（用户配置 + 默认配置）                 │    │
│  │  - 配置预设管理                                   │    │
│  └─────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────┘
                        ↓ ↑
┌──────────────────────────────────────────────────────────┐
│                  管理层 (Management)                       │
│  ┌─────────────────────────────────────────────────┐    │
│  │              SoundManager                        │    │
│  │  - 事件路由                                       │    │
│  │  - 权限检查（全局开关、分类开关）                  │    │
│  │  - 节流控制                                       │    │
│  │  - 优先级调度                                     │    │
│  │  - 上下文感知（标签页可见性、系统设置等）           │    │
│  └─────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────┘
                        ↓ ↑
┌──────────────────────────────────────────────────────────┐
│                   引擎层 (Engine)                          │
│  ┌─────────────────────────────────────────────────┐    │
│  │              SoundEngine                         │    │
│  │  - Web Audio API 封装                            │    │
│  │  - 音频文件加载                                   │    │
│  │  - AudioBuffer 缓存                              │    │
│  │  - 播放控制（播放/暂停/停止）                      │    │
│  │  - 音量控制                                       │    │
│  └─────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────┘
                        ↓ ↑
┌──────────────────────────────────────────────────────────┐
│                  资源层 (Resources)                        │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐         │
│  │ success.mp3│  │  error.mp3 │  │ 其他音效文件 │         │
│  └────────────┘  └────────────┘  └────────────┘         │
└──────────────────────────────────────────────────────────┘
```

---

## 📦 核心模块设计

### 1. SoundEngine（音效引擎）

**职责**：底层音频播放能力

```typescript
class SoundEngine {
  private audioContext: AudioContext;
  private bufferCache: Map<string, AudioBuffer>;
  private gainNode: GainNode;

  // 加载音频文件
  async loadSound(url: string): Promise<AudioBuffer>;

  // 播放音频
  async play(buffer: AudioBuffer, options: PlayOptions): Promise<void>;

  // 设置主音量
  setMasterVolume(volume: number): void;

  // 停止所有音效
  stopAll(): void;

  // 预加载音效
  preload(urls: string[]): Promise<void>;
}
```

**技术细节**

- 使用 **Web Audio API** 而非 `<audio>` 标签
  - 优势：低延迟、精确控制、支持多音轨混合
- 音频缓存策略
  - 使用 `Map<string, AudioBuffer>` 缓存解码后的音频
  - 避免重复加载和解码
- 音量控制
  - 通过 `GainNode` 实现全局音量
  - 支持单个音效的音量调整

### 2. SoundManager（音效管理器）

**职责**：业务逻辑和智能控制

```typescript
class SoundManager {
  private engine: SoundEngine;
  private configManager: SoundConfigManager;
  private throttleMap: Map<string, ThrottleState>;
  private playQueue: PriorityQueue<SoundTask>;

  // 播放音效（主入口）
  play(eventName: string, options?: PlayOptions): Promise<void>;

  // 检查是否应该播放
  private shouldPlay(eventName: string, config: SoundConfig): boolean;

  // 节流检查
  private checkThrottle(eventName: string, throttleMs: number): boolean;

  // 优先级调度
  private schedulePlay(task: SoundTask): void;
}
```

**核心功能**

#### 2.1 权限检查

```typescript
shouldPlay(eventName: string, config: SoundConfig): boolean {
  // 1. 检查全局开关
  if (!config.master.enabled) return false;

  // 2. 检查分类开关
  const category = this.getCategory(eventName);
  if (!config.categories[category]?.enabled) return false;

  // 3. 检查具体场景开关
  if (!config.categories[category]?.scenes[eventName]?.enabled) {
    return false;
  }

  // 4. 检查上下文
  if (!this.checkContext()) return false;

  return true;
}
```

#### 2.2 节流机制

```typescript
interface ThrottleState {
  lastPlayTime: number;
  playCount: number;
  windowStart: number;
}

checkThrottle(eventName: string, throttleMs: number): boolean {
  const now = Date.now();
  const state = this.throttleMap.get(eventName);

  if (!state || now - state.lastPlayTime > throttleMs) {
    this.throttleMap.set(eventName, {
      lastPlayTime: now,
      playCount: 1,
      windowStart: now
    });
    return true;
  }

  return false; // 被节流
}
```

**节流策略**

- **时间节流**：相同事件在 N 毫秒内只播放一次
- **频率限制**：滑动窗口内最多播放 M 次
- **去重**：快速连续的相同事件只播放一次

#### 2.3 优先级系统

```typescript
enum SoundPriority {
  Low = 0,
  Normal = 1,
  High = 2,
  Critical = 3,
}

interface SoundTask {
  eventName: string;
  priority: SoundPriority;
  timestamp: number;
  options?: PlayOptions;
}

// 优先级队列（最大堆）
class PriorityQueue<T> {
  // 高优先级先出队
  // 同优先级按时间戳（先进先出）
}
```

**优先级规则**

- Critical：立即播放，可打断低优先级
- High：优先播放
- Normal：正常排队
- Low：可被跳过

#### 2.4 上下文感知

```typescript
checkContext(): boolean {
  // 1. 检查标签页可见性
  if (document.hidden) return false;

  // 2. 检查系统设置（减少动效）
  if (this.respectReducedMotion()) {
    const prefersReduced = window.matchMedia(
      '(prefers-reduced-motion: reduce)'
    ).matches;
    if (prefersReduced) return false;
  }

  // 3. 检查用户输入状态
  if (this.isUserTyping()) return false;

  return true;
}
```

### 3. SoundConfigManager（配置管理器）

**职责**：配置的持久化和管理

```typescript
class SoundConfigManager {
  private static STORAGE_KEY = "sound-system-config";
  private config: SoundConfig;
  private defaultConfig: SoundConfig;

  // 加载配置
  load(): SoundConfig;

  // 保存配置
  save(config: SoundConfig): void;

  // 重置为默认配置
  reset(): void;

  // 导出配置（JSON）
  export(): string;

  // 导入配置
  import(json: string): void;

  // 获取预设配置
  getPreset(name: PresetName): SoundConfig;
}
```

**配置数据结构**

```typescript
interface SoundConfig {
  version: string;

  master: {
    enabled: boolean;
    volume: number; // 0-100
    respectSystemSettings: boolean;
  };

  categories: {
    [category: string]: CategoryConfig;
  };
}

interface CategoryConfig {
  enabled: boolean;
  volume: number; // 0-100（相对于主音量）
  throttle: number; // 毫秒

  scenes: {
    [eventName: string]: SceneConfig;
  };
}

interface SceneConfig {
  enabled: boolean;
  volume?: number; // 可选，覆盖分类音量
  priority?: SoundPriority;
  soundFile?: string; // 自定义音效文件
}
```

**存储策略**

- **LocalStorage**：主要存储方式
  - 优点：持久化、容量够用（5-10MB）
  - 缺点：同步 API（但配置数据小，影响可忽略）
- **SessionStorage**：会话级临时配置（未来扩展）
- **IndexedDB**：自定义音效文件存储（未来扩展）

**配置合并策略**

```typescript
load(): SoundConfig {
  const stored = localStorage.getItem(STORAGE_KEY);
  const userConfig = stored ? JSON.parse(stored) : {};

  // 深度合并：用户配置覆盖默认配置
  return deepMerge(this.defaultConfig, userConfig);
}
```

---

## 🎵 音效分类体系

### 当前实现（Phase 1）

```
API 音效（api）
├── 成功音效（api.success）
│   ├── api.success.get      [优先级: Low]
│   ├── api.success.post     [优先级: Normal]
│   ├── api.success.put      [优先级: Normal]
│   └── api.success.delete   [优先级: High]
└── 错误音效（api.error）
    ├── api.error.4xx        [优先级: High]
    ├── api.error.401        [优先级: High]
    ├── api.error.403        [优先级: High]
    ├── api.error.404        [优先级: Normal]
    └── api.error.5xx        [优先级: Critical]
```

### 扩展设计（Phase 2+）

```
UI 交互音效（interaction）
├── interaction.click.button
├── interaction.toggle.switch
├── interaction.expand.panel
└── interaction.drag.start

内容操作音效（content）
├── content.publish.article
├── content.save.draft
├── content.delete.confirm
└── content.upload.success

通知音效（notification）
├── notification.message.new
├── notification.comment.new
└── notification.system.alert
```

**命名规范**

```
<category>.<action>.<detail>

示例：
- api.success.post       (API 成功，POST 请求)
- api.error.500          (API 错误，500 状态码)
- interaction.click.button (交互，点击按钮)
- content.publish.article  (内容，发布文章)
```

---

## 🔌 集成方式

### 1. API 拦截器集成

```typescript
// src/api/index.ts
import { soundManager } from "@/utils/sound";

apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code } = response.data;
    const method = response.config.method?.toUpperCase();

    // 动态构建事件名
    const eventName = `api.success.${method.toLowerCase()}`;
    soundManager.play(eventName);

    return response;
  },
  (error: AxiosError) => {
    const status = error.response?.status;
    const eventName = `api.error.${status || "5xx"}`;
    soundManager.play(eventName, { priority: "high" });

    return Promise.reject(error);
  }
);
```

### 2. React Hook 集成

```typescript
// 未来扩展：UI 交互音效
import { useSoundEffect } from "@/hooks/useSoundEffect";

function Button() {
  const playSound = useSoundEffect();

  const handleClick = () => {
    playSound("interaction.click.button");
    // 业务逻辑...
  };

  return <button onClick={handleClick}>Click Me</button>;
}
```

### 3. 事件总线集成

```typescript
// 未来扩展：系统级事件
eventBus.on("article.published", () => {
  soundManager.play("content.publish.article", {
    priority: "high",
    volume: 0.8,
  });
});
```

---

## 🎨 默认配置

### 配置预设

#### 1. 静音模式（Silent）

```typescript
{
  master: { enabled: false, volume: 0 }
}
```

#### 2. 仅错误提示（Errors Only）

```typescript
{
  master: { enabled: true, volume: 70 },
  categories: {
    api: {
      enabled: true,
      scenes: {
        'api.success.*': { enabled: false },
        'api.error.*': { enabled: true }
      }
    }
  }
}
```

#### 3. 极简模式（Minimal）

```typescript
{
  master: { enabled: true, volume: 50 },
  categories: {
    api: {
      enabled: true,
      throttle: 2000, // 2秒节流
      scenes: {
        'api.success.get': { enabled: false },
        'api.success.post': { enabled: true },
        'api.error.*': { enabled: true }
      }
    }
  }
}
```

#### 4. 标准模式（Standard）- 默认

```typescript
{
  master: {
    enabled: true,
    volume: 70,
    respectSystemSettings: true
  },
  categories: {
    api: {
      enabled: true,
      volume: 50,
      throttle: 1000,
      scenes: {
        'api.success.get': { enabled: false, priority: 'low' },
        'api.success.post': { enabled: true, priority: 'normal' },
        'api.success.put': { enabled: true, priority: 'normal' },
        'api.success.delete': { enabled: true, priority: 'high' },
        'api.error.4xx': { enabled: true, priority: 'high' },
        'api.error.5xx': { enabled: true, priority: 'critical' }
      }
    }
  }
}
```

#### 5. 完整体验（Full Experience）

```typescript
{
  master: { enabled: true, volume: 80 },
  categories: {
    api: {
      enabled: true,
      volume: 70,
      throttle: 500,
      scenes: {
        // 所有场景启用
      }
    }
  }
}
```

---

## 🔧 技术实现要点

### 1. Web Audio API 使用

```typescript
class SoundEngine {
  private audioContext: AudioContext;

  constructor() {
    // 创建音频上下文
    this.audioContext = new (window.AudioContext ||
      (window as any).webkitAudioContext)();

    // 创建主增益节点（音量控制）
    this.gainNode = this.audioContext.createGain();
    this.gainNode.connect(this.audioContext.destination);
  }

  async loadSound(url: string): Promise<AudioBuffer> {
    const response = await fetch(url);
    const arrayBuffer = await response.arrayBuffer();
    return await this.audioContext.decodeAudioData(arrayBuffer);
  }

  async play(buffer: AudioBuffer, volume: number = 1.0): Promise<void> {
    const source = this.audioContext.createBufferSource();
    source.buffer = buffer;

    // 创建音量节点
    const gainNode = this.audioContext.createGain();
    gainNode.gain.value = volume;

    // 连接音频图
    source.connect(gainNode);
    gainNode.connect(this.gainNode);

    // 播放
    source.start(0);
  }
}
```

**优势**

- 低延迟（<10ms）
- 支持多音轨同时播放
- 精确的音量控制
- 更好的性能

### 2. 性能优化

**音效文件优化**

- 格式：MP3（兼容性好）
- 大小：< 20KB
- 采样率：44.1kHz
- 比特率：128kbps
- 时长：< 1 秒

**加载策略**

```typescript
// 应用启动时预加载高频音效
preload([
  "/assets/sounds/api/error-critical.mp3",
  "/assets/sounds/api/success.mp3",
]);

// 低频音效懒加载
lazyLoad("/assets/sounds/api/success-heavy.mp3");
```

**内存管理**

- 使用 `Map` 缓存 `AudioBuffer`
- 定期清理未使用的缓存
- 监控内存占用

### 3. 可访问性

```typescript
// 尊重系统设置
const prefersReducedMotion = window.matchMedia(
  "(prefers-reduced-motion: reduce)"
).matches;

if (prefersReducedMotion && config.master.respectSystemSettings) {
  // 降低音效播放频率或完全禁用
}
```

---

## 📊 数据流图

```
用户操作 → API 请求
            ↓
    API 响应拦截器
            ↓
  soundManager.play('api.success.post')
            ↓
    [权限检查] → 全局开关? 分类开关? 场景开关?
            ↓
    [节流检查] → 距离上次播放时间?
            ↓
    [上下文检查] → 标签页可见? 系统设置?
            ↓
    [优先级调度] → 加入播放队列
            ↓
       SoundEngine
            ↓
    [缓存检查] → AudioBuffer 已缓存?
            ↓
       播放音效
            ↓
       用户听到
```

---

## 🔮 扩展性设计

### 添加新音效类型

**1. 注册音效文件**

```typescript
// src/utils/sound/registry.ts
export const SOUND_REGISTRY = {
  api: {
    success: "/assets/sounds/api/success.mp3",
    error: "/assets/sounds/api/error.mp3",
  },
  // 新增：UI 交互音效
  interaction: {
    click: "/assets/sounds/ui/click.mp3",
    toggle: "/assets/sounds/ui/toggle.mp3",
  },
};
```

**2. 添加默认配置**

```typescript
// src/utils/sound/config.ts
const DEFAULT_CONFIG = {
  categories: {
    api: { ... },
    // 新增
    interaction: {
      enabled: true,
      volume: 30,
      throttle: 100,
      scenes: {
        'interaction.click': { enabled: true }
      }
    }
  }
};
```

**3. 在设置 UI 中添加配置项**

```typescript
<SettingCategory name="interaction" label="UI 交互音效">
  <SettingScene name="interaction.click" label="按钮点击" />
</SettingCategory>
```

**4. 在需要的地方调用**

```typescript
soundManager.play("interaction.click");
```

**无需修改核心代码！** 🎉

---

## 📝 总结

### 设计优势

1. **分层清晰**：引擎、管理、配置各司其职
2. **高扩展性**：新增音效类型无需修改核心
3. **性能优化**：节流、缓存、智能调度
4. **用户友好**：细粒度控制、本地存储
5. **开发友好**：简洁 API、易于集成

### 技术亮点

- ✅ Web Audio API 低延迟播放
- ✅ 智能节流防止过度播放
- ✅ 优先级系统保证重要提示
- ✅ 上下文感知提升用户体验
- ✅ 完全本地化的配置存储

### 扩展路径

```
Phase 1: API 音效（当前）
    ↓
Phase 2: UI 交互音效
    ↓
Phase 3: 自定义音效上传
    ↓
Phase 4: 音效主题包
```

---

**下一步**: 查看 [API 参考文档](./api-reference.md) 了解如何使用
