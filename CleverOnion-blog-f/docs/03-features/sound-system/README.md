# 通用音效系统

## 📖 概述

这是一个为 CleverOnion Blog 前端应用设计的**通用音效系统**，提供灵活、可扩展、高度可定制的音效管理方案。

### 设计目标

- ✅ **高扩展性**：核心架构支持多种音效类型，当前聚焦 API 音效，未来可轻松扩展至 UI 交互、通知等场景
- ✅ **用户友好**：完全本地化的用户设置，细粒度控制，避免音效过于频繁
- ✅ **性能优先**：智能节流、预加载、内存管理，不影响应用性能
- ✅ **开发友好**：简洁的 API，易于集成到现有代码中

### 当前功能范围

**Phase 1（当前实现）**

- ✅ API 请求音效系统
  - 成功音效（可按 HTTP 方法区分）
  - 错误音效（可按状态码区分）
  - 智能节流机制
- ✅ 用户设置面板
  - 总开关 + 音量控制
  - 分类细粒度配置
  - 本地存储持久化
- ✅ 基础音效管理
  - 音效预加载
  - 优先级系统
  - 性能优化

**Phase 2（预留扩展）**

- 🔄 UI 交互音效（点击、切换、拖拽等）
- 🔄 内容操作音效（发布、保存、删除等）
- 🔄 实时通知音效（新消息、评论等）
- 🔄 自定义音效上传

## 📁 文档结构

```
docs/sound-system/
├── README.md                   # 本文档（概览）
├── architecture.md             # 系统架构设计
├── api-reference.md            # API 参考文档
├── configuration.md            # 配置系统说明
├── implementation-guide.md     # 实现指南（含进度追踪）
└── IMPLEMENTATION_STATUS.md    # 实现状态总览 ⭐
```

## 🚀 快速开始

### 基本用法

```typescript
import { soundManager } from "@/utils/sound";

// 播放 API 成功音效
soundManager.play("api.success.post");

// 播放 API 错误音效
soundManager.play("api.error.500");

// 带自定义参数播放
soundManager.play("api.success.get", { volume: 0.5 });
```

### 用户设置

用户可以通过设置面板调整音效行为：

- 全局开关
- 音量调节（0-100%）
- 按分类/场景细粒度控制
- 节流间隔调整

所有设置保存在浏览器 LocalStorage，**不会上传到服务端**。

## 🎯 核心特性

### 1. 智能节流

防止音效过于频繁播放：

- **时间节流**：相同类型音效在指定时间内只播放一次
- **频率限制**：滑动窗口内限制播放次数
- **去重机制**：短时间内相同事件只播放一次

### 2. 优先级系统

```
Critical（关键）> High（高）> Normal（普通）> Low（低）
```

- 高优先级音效可以打断低优先级
- 防止重要提示被淹没

### 3. 上下文感知

- 检测标签页可见性（后台标签页不播放）
- 尊重系统设置（`prefers-reduced-motion`）
- 检测输入状态（用户输入时降低音效）

### 4. 本地存储策略

```typescript
// 存储位置
localStorage.setItem('sound-system-config', JSON.stringify(config));

// 数据结构
{
  master: { enabled: true, volume: 70 },
  categories: {
    api: {
      enabled: true,
      volume: 50,
      throttle: 1000,
      scenes: { ... }
    }
  }
}
```

**隐私保护**

- ✅ 所有配置仅存储在用户本地浏览器
- ✅ 不会上传到服务端
- ✅ 不会跨设备同步
- ✅ 用户可随时清除

## 📊 架构概览

```
┌─────────────────────────────────────┐
│     用户设置 UI (Settings Panel)      │  ← 本地 LocalStorage
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│   音效管理器 (Sound Manager)          │  ← 配置、节流、优先级
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│   音效引擎 (Sound Engine)             │  ← Web Audio API
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│   事件触发 (API Interceptor 等)       │  ← 各模块集成
└─────────────────────────────────────┘
```

详见 [architecture.md](./architecture.md)

## 🔧 技术栈

- **TypeScript**：类型安全
- **Web Audio API**：高性能音频播放
- **LocalStorage**：配置持久化
- **React Context**：全局状态管理
- **Custom Events**：事件通信

## 📚 更多文档

- [系统架构设计](./architecture.md) - 深入了解系统设计
- [API 参考](./api-reference.md) - 完整的 API 文档
- [配置说明](./configuration.md) - 配置系统详解
- [实现指南](./implementation-guide.md) - 分步实现说明（含进度追踪）
- [**实现状态总览**](./IMPLEMENTATION_STATUS.md) ⭐ - 当前实现进度和完成情况

### 实现进度追踪

**当前状态**: ✅ **80% 完成** (8/10)

- ✅ Phase 1: 基础架构（5/5 完成）
- ✅ Phase 2: 集成应用（3/3 完成）
- ⬜ Phase 3: 测试优化（0/2 待开始）

详见 [实现状态文档](./IMPLEMENTATION_STATUS.md) 和 [实现指南](./implementation-guide.md)

## 🤝 贡献指南

### 添加新的音效类型

系统设计为高度可扩展，添加新类型只需：

1. 在 `SoundCategory` 枚举中添加新分类
2. 在 `SOUND_REGISTRY` 中注册音效文件
3. 在配置界面添加对应的设置项
4. 在需要的地方调用 `soundManager.play()`

详见 [implementation-guide.md](./implementation-guide.md#扩展新音效类型)

## 📝 版本历史

### v1.0.0 (Current)

- ✅ API 音效系统基础架构
- ✅ 配置管理和本地存储
- ✅ 智能节流机制
- ✅ 用户设置面板

### v2.0.0 (Planned)

- 🔄 UI 交互音效
- 🔄 自定义音效上传
- 🔄 音效主题包

## 📄 许可证

本系统作为 CleverOnion Blog 项目的一部分，遵循项目整体许可证。

---

**维护者**: CleverOnion Development Team  
**最后更新**: 2025-10-01
