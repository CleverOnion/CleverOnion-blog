/**
 * 音效引擎 - 负责底层音频播放
 */

// 扩展 Window 接口以支持 Safari 的 webkitAudioContext
interface WindowWithWebkit extends Window {
  webkitAudioContext?: typeof AudioContext;
}

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
      const AudioContextClass =
        window.AudioContext || (window as WindowWithWebkit).webkitAudioContext;

      if (AudioContextClass) {
        this.audioContext = new AudioContextClass();
        this.gainNode = this.audioContext.createGain();
        this.gainNode.connect(this.audioContext.destination);
      }
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
      typeof (window as WindowWithWebkit).webkitAudioContext !== "undefined"
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
