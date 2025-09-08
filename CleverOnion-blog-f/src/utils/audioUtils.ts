/**
 * 音效播放工具函数
 * 根据后端返回的code值播放对应的音效
 */

// 音效文件路径
const AUDIO_FILES = {
  success: '/src/assets/mp3/success.mp3',
  fail: '/src/assets/mp3/fail.mp3'
} as const;

/**
 * 播放音效
 * @param code 后端返回的状态码
 */
export const playNotificationSound = (code: number): void => {
  try {
    // 根据code值选择音效文件
    const audioFile = code === 200 ? AUDIO_FILES.success : AUDIO_FILES.fail;
    
    // 创建Audio对象并播放
    const audio = new Audio(audioFile);
    audio.volume = 0.5; // 设置音量为50%
    
    // 播放音效
    audio.play().catch((error) => {
      console.warn('音效播放失败:', error);
      // 音效播放失败不应该影响正常功能，只记录警告
    });
  } catch (error) {
    console.warn('音效播放出错:', error);
    // 音效播放出错不应该影响正常功能，只记录警告
  }
};

/**
 * 播放成功音效
 */
export const playSuccessSound = (): void => {
  playNotificationSound(200);
};

/**
 * 播放失败音效
 */
export const playFailSound = (): void => {
  playNotificationSound(400); // 使用非200的code
};

/**
 * 检查浏览器是否支持音频播放
 * @returns 是否支持音频播放
 */
export const isAudioSupported = (): boolean => {
  return typeof Audio !== 'undefined';
};

/**
 * 预加载音效文件
 * 在应用启动时调用，提前加载音效文件以减少播放延迟
 */
export const preloadAudioFiles = (): void => {
  if (!isAudioSupported()) {
    return;
  }

  try {
    // 预加载成功音效
    const successAudio = new Audio(AUDIO_FILES.success);
    successAudio.preload = 'auto';
    
    // 预加载失败音效
    const failAudio = new Audio(AUDIO_FILES.fail);
    failAudio.preload = 'auto';
  } catch (error) {
    console.warn('音效文件预加载失败:', error);
  }
};