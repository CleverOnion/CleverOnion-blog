# 配置系统说明

## 📋 配置概览

音效系统的配置完全存储在用户浏览器的 LocalStorage 中，遵循以下原则：

- ✅ **本地存储**：所有配置仅保存在用户本地，不上传服务器
- ✅ **隐私保护**：不会跨设备同步，不会被追踪
- ✅ **用户控制**：用户可以随时修改、导出、导入或重置配置
- ✅ **向后兼容**：配置结构支持版本管理，未来升级不会丢失设置

---

## 🗂️ 配置结构

### 完整配置示例

```json
{
  "version": "1.0.0",
  "master": {
    "enabled": true,
    "volume": 70,
    "respectSystemSettings": true
  },
  "categories": {
    "api": {
      "enabled": true,
      "volume": 50,
      "throttle": 1000,
      "scenes": {
        "api.success.get": {
          "enabled": false,
          "priority": "low"
        },
        "api.success.post": {
          "enabled": true,
          "volume": 80,
          "priority": "normal"
        },
        "api.success.put": {
          "enabled": true,
          "priority": "normal"
        },
        "api.success.delete": {
          "enabled": true,
          "priority": "high"
        },
        "api.error.4xx": {
          "enabled": true,
          "priority": "high"
        },
        "api.error.5xx": {
          "enabled": true,
          "priority": "critical",
          "soundFile": "/assets/sounds/api/error-critical.mp3"
        }
      }
    }
  }
}
```

---

## 🎛️ 配置层级

### 1. Master（主配置）

控制全局音效行为。

```typescript
{
  master: {
    enabled: boolean,              // 全局总开关
    volume: number,                // 主音量 0-100
    respectSystemSettings: boolean // 是否尊重系统减少动效设置
  }
}
```

#### enabled（全局开关）

- **类型**: `boolean`
- **默认值**: `true`
- **说明**: 控制所有音效的总开关
- **影响**: 关闭后，任何音效都不会播放

```typescript
// 关闭所有音效
soundManager.setEnabled(false);
```

#### volume（主音量）

- **类型**: `number`
- **范围**: 0-100
- **默认值**: `70`
- **说明**: 全局主音量，影响所有音效
- **计算**: 最终音量 = 主音量 × 分类音量 × 场景音量

```typescript
// 设置主音量为 80%
soundManager.setMasterVolume(80);
```

#### respectSystemSettings（尊重系统设置）

- **类型**: `boolean`
- **默认值**: `true`
- **说明**: 是否遵循系统的"减少动效"设置
- **影响**: 当用户系统设置为减少动效时，音效会被禁用或降低频率

```typescript
// 检查系统设置
const prefersReduced = window.matchMedia(
  "(prefers-reduced-motion: reduce)"
).matches;

if (prefersReduced && config.master.respectSystemSettings) {
  // 禁用或减少音效播放
}
```

---

### 2. Categories（分类配置）

按音效分类进行配置。

```typescript
{
  categories: {
    [category: string]: {
      enabled: boolean,       // 分类开关
      volume: number,         // 分类音量 0-100
      throttle: number,       // 节流间隔（毫秒）
      scenes: { ... }         // 场景配置
    }
  }
}
```

#### enabled（分类开关）

- **类型**: `boolean`
- **默认值**: `true`（API 分类）
- **说明**: 控制该分类下所有音效的开关
- **优先级**: 低于 `master.enabled`

```typescript
// 关闭 API 音效
soundManager.setCategoryEnabled("api", false);
```

#### volume（分类音量）

- **类型**: `number`
- **范围**: 0-100
- **默认值**: `50`（API 分类）
- **说明**: 该分类的相对音量
- **计算**: 与主音量相乘得到实际音量

```typescript
// 设置 API 音效音量为 60%
soundManager.updateConfig({
  categories: {
    api: { volume: 60 },
  },
});
```

#### throttle（节流间隔）

- **类型**: `number`
- **单位**: 毫秒
- **默认值**: `1000`（API 分类）
- **说明**: 同一类型音效的最小播放间隔
- **用途**: 防止音效过于频繁

```typescript
// 设置节流间隔为 2 秒
soundManager.updateConfig({
  categories: {
    api: { throttle: 2000 },
  },
});
```

**节流示例**

```
时间轴: 0ms ----> 500ms ----> 1000ms ----> 1500ms
事件:   播放✅      被拦截❌     播放✅       被拦截❌
```

---

### 3. Scenes（场景配置）

最细粒度的配置，针对具体音效事件。

```typescript
{
  scenes: {
    [eventName: string]: {
      enabled: boolean,           // 场景开关
      volume?: number,            // 场景音量（可选，覆盖分类音量）
      priority?: SoundPriority,   // 优先级
      soundFile?: string          // 自定义音效文件
    }
  }
}
```

#### enabled（场景开关）

- **类型**: `boolean`
- **默认值**: 按场景而异
- **说明**: 控制该具体场景的音效开关
- **优先级**: 最高优先级的开关

```typescript
// 关闭 GET 请求的成功音效
soundManager.setSceneEnabled("api.success.get", false);
```

#### volume（场景音量）

- **类型**: `number | undefined`
- **范围**: 0-100
- **默认值**: `undefined`（使用分类音量）
- **说明**: 覆盖分类音量，针对该场景特别调整

```typescript
// POST 请求成功时音量调大
soundManager.updateConfig({
  categories: {
    api: {
      scenes: {
        "api.success.post": {
          volume: 90, // 90%
        },
      },
    },
  },
});
```

#### priority（优先级）

- **类型**: `SoundPriority | undefined`
- **枚举值**: `'low'` | `'normal'` | `'high'` | `'critical'`
- **默认值**: `'normal'`
- **说明**: 音效播放优先级，高优先级可打断低优先级

```typescript
// 5xx 错误使用最高优先级
soundManager.updateConfig({
  categories: {
    api: {
      scenes: {
        "api.error.5xx": {
          priority: "critical",
        },
      },
    },
  },
});
```

#### soundFile（自定义音效）

- **类型**: `string | undefined`
- **默认值**: `undefined`（使用默认音效文件）
- **说明**: 指定自定义音效文件路径
- **用途**: 用户上传或选择不同的音效

```typescript
// 使用自定义音效
soundManager.updateConfig({
  categories: {
    api: {
      scenes: {
        "api.success.post": {
          soundFile: "/assets/sounds/custom/celebration.mp3",
        },
      },
    },
  },
});
```

---

## 🎨 预设配置

系统提供多种预设配置，用户可快速切换。

### 1. Silent（静音模式）

完全关闭所有音效。

```json
{
  "master": {
    "enabled": false,
    "volume": 0
  }
}
```

**使用场景**

- 需要安静的工作环境
- 公共场所使用
- 演示或录屏时

```typescript
soundManager.loadPreset("silent");
```

---

### 2. Errors Only（仅错误提示）

只播放错误/警告音效，成功音效静音。

```json
{
  "master": { "enabled": true, "volume": 70 },
  "categories": {
    "api": {
      "enabled": true,
      "scenes": {
        "api.success.get": { "enabled": false },
        "api.success.post": { "enabled": false },
        "api.success.put": { "enabled": false },
        "api.success.delete": { "enabled": false },
        "api.error.4xx": { "enabled": true },
        "api.error.5xx": { "enabled": true }
      }
    }
  }
}
```

**使用场景**

- 关注错误提示
- 减少音效干扰
- 读取密集的应用场景

```typescript
soundManager.loadPreset("errors-only");
```

---

### 3. Minimal（极简模式）

只播放重要操作的音效，节流间隔加长。

```json
{
  "master": { "enabled": true, "volume": 50 },
  "categories": {
    "api": {
      "enabled": true,
      "throttle": 2000,
      "scenes": {
        "api.success.get": { "enabled": false },
        "api.success.post": { "enabled": true },
        "api.success.put": { "enabled": true },
        "api.success.delete": { "enabled": true, "priority": "high" },
        "api.error.4xx": { "enabled": true, "priority": "high" },
        "api.error.5xx": { "enabled": true, "priority": "critical" }
      }
    }
  }
}
```

**使用场景**

- 想要音效但不要太频繁
- 平衡体验和安静
- 日常使用推荐

```typescript
soundManager.loadPreset("minimal");
```

---

### 4. Standard（标准模式）⭐ 默认

平衡的音效配置，适合大多数用户。

```json
{
  "master": {
    "enabled": true,
    "volume": 70,
    "respectSystemSettings": true
  },
  "categories": {
    "api": {
      "enabled": true,
      "volume": 50,
      "throttle": 1000,
      "scenes": {
        "api.success.get": { "enabled": false, "priority": "low" },
        "api.success.post": { "enabled": true, "priority": "normal" },
        "api.success.put": { "enabled": true, "priority": "normal" },
        "api.success.delete": { "enabled": true, "priority": "high" },
        "api.error.4xx": { "enabled": true, "priority": "high" },
        "api.error.5xx": { "enabled": true, "priority": "critical" }
      }
    }
  }
}
```

**使用场景**

- 首次使用
- 不确定如何配置
- 默认推荐

```typescript
soundManager.loadPreset("standard");
```

---

### 5. Full（完整体验）

启用所有音效，节流间隔最短。

```json
{
  "master": { "enabled": true, "volume": 80 },
  "categories": {
    "api": {
      "enabled": true,
      "volume": 70,
      "throttle": 500,
      "scenes": {
        "api.success.get": { "enabled": true },
        "api.success.post": { "enabled": true },
        "api.success.put": { "enabled": true },
        "api.success.delete": { "enabled": true },
        "api.error.4xx": { "enabled": true },
        "api.error.5xx": { "enabled": true }
      }
    }
  }
}
```

**使用场景**

- 喜欢丰富的音效反馈
- 测试音效系统
- 展示应用功能

```typescript
soundManager.loadPreset("full");
```

---

## 💾 存储机制

### LocalStorage 存储

**存储键名**: `sound-system-config`

**存储数据**

```typescript
localStorage.setItem("sound-system-config", JSON.stringify(config));
```

**读取数据**

```typescript
const stored = localStorage.getItem("sound-system-config");
const config = stored ? JSON.parse(stored) : defaultConfig;
```

**存储容量**

- LocalStorage 通常限制 5-10MB
- 音效配置 < 10KB
- 足够存储完整配置

---

### 配置合并策略

**默认配置 + 用户配置 = 最终配置**

```typescript
function loadConfig(): SoundConfig {
  const defaultConfig = getDefaultConfig();
  const stored = localStorage.getItem("sound-system-config");
  const userConfig = stored ? JSON.parse(stored) : {};

  // 深度合并
  return deepMerge(defaultConfig, userConfig);
}
```

**合并规则**

1. 用户配置覆盖默认配置
2. 未配置的项使用默认值
3. 保证向后兼容性

**示例**

```typescript
// 默认配置
const defaultConfig = {
  master: { enabled: true, volume: 70 },
  categories: {
    api: { enabled: true, volume: 50 },
  },
};

// 用户配置（只改了音量）
const userConfig = {
  master: { volume: 80 },
};

// 合并结果
const finalConfig = {
  master: { enabled: true, volume: 80 }, // volume 被覆盖
  categories: {
    api: { enabled: true, volume: 50 }, // 保持默认
  },
};
```

---

### 配置版本管理

**版本号**: `config.version`

```typescript
interface SoundConfig {
  version: string; // 如 "1.0.0"
  // ...
}
```

**版本升级**

```typescript
function migrateConfig(config: any): SoundConfig {
  const version = config.version || "0.0.0";

  if (version < "1.0.0") {
    // 迁移逻辑
    config = migrateFrom_0_to_1(config);
  }

  if (version < "2.0.0") {
    // 未来的迁移
    config = migrateFrom_1_to_2(config);
  }

  config.version = CURRENT_VERSION;
  return config;
}
```

---

## 📤 导出/导入配置

### 导出配置

```typescript
const json = soundManager.exportConfig();

// 下载为文件
const blob = new Blob([json], { type: "application/json" });
const url = URL.createObjectURL(blob);
const a = document.createElement("a");
a.href = url;
a.download = "sound-config.json";
a.click();
```

### 导入配置

```typescript
// 从文件读取
const file = event.target.files[0];
const reader = new FileReader();
reader.onload = (e) => {
  const json = e.target.result as string;
  soundManager.importConfig(json);
};
reader.readAsText(file);
```

### 分享配置

用户可以导出配置并分享给其他用户：

```json
// sound-config.json
{
  "version": "1.0.0",
  "master": { ... },
  "categories": { ... }
}
```

---

## 🔒 隐私与安全

### 隐私保护

✅ **本地存储**

- 所有配置仅保存在用户浏览器
- 不会发送到服务器
- 不会被第三方访问

✅ **不跨设备同步**

- 每个设备独立配置
- 不会通过云端同步

✅ **用户完全控制**

- 可以随时修改配置
- 可以随时清除配置
- 可以导出备份

### 清除配置

```typescript
// 方式1: 重置为默认
soundManager.resetConfig();

// 方式2: 完全删除
localStorage.removeItem("sound-system-config");

// 方式3: 清除所有本地存储（谨慎）
localStorage.clear();
```

---

## 🛠️ 高级配置

### 自定义节流策略

```typescript
soundManager.updateConfig({
  categories: {
    api: {
      throttle: 3000, // 3 秒节流
      scenes: {
        "api.success.post": {
          throttle: 500, // 覆盖分类节流，单独为 500ms
        },
      },
    },
  },
});
```

### 音量分层控制

```typescript
// 主音量 70% × 分类音量 50% × 场景音量 80% = 最终 28%
soundManager.updateConfig({
  master: { volume: 70 },
  categories: {
    api: {
      volume: 50,
      scenes: {
        "api.success.post": {
          volume: 80,
        },
      },
    },
  },
});
```

### 优先级配置

```typescript
soundManager.updateConfig({
  categories: {
    api: {
      scenes: {
        "api.error.5xx": { priority: "critical" }, // 最高优先级
        "api.error.4xx": { priority: "high" }, // 高优先级
        "api.success.post": { priority: "normal" }, // 普通优先级
        "api.success.get": { priority: "low" }, // 低优先级
      },
    },
  },
});
```

---

## 📊 配置最佳实践

### 推荐配置

**1. 办公环境**

```typescript
soundManager.loadPreset("minimal"); // 极简模式
soundManager.setMasterVolume(40); // 降低音量
```

**2. 个人使用**

```typescript
soundManager.loadPreset("standard"); // 标准模式
```

**3. 演示/展示**

```typescript
soundManager.loadPreset("full"); // 完整体验
soundManager.setMasterVolume(60); // 适中音量
```

**4. 开发/测试**

```typescript
soundManager.loadPreset("full"); // 完整体验
soundManager.setDebugMode(true); // 启用调试
```

### 性能优化配置

```typescript
soundManager.updateConfig({
  categories: {
    api: {
      throttle: 2000, // 增加节流间隔
      scenes: {
        "api.success.get": { enabled: false }, // 关闭高频操作音效
      },
    },
  },
});
```

---

## 📝 配置检查清单

在部署前检查配置：

- [ ] 全局开关默认开启
- [ ] 主音量设置合理（50-70）
- [ ] 高频操作音效已关闭或节流（如 GET 请求）
- [ ] 错误音效优先级设置正确
- [ ] 尊重系统设置已启用
- [ ] 配置版本号正确
- [ ] 默认预设为 Standard
- [ ] 所有音效文件路径正确

---

## 🔗 相关文档

- [系统架构](./architecture.md)
- [API 参考](./api-reference.md)
- [实现指南](./implementation-guide.md)

---

**最后更新**: 2025-10-01
