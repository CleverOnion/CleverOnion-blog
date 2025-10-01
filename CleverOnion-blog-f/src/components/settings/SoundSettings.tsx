import { useState } from "react";
import { soundManager, PresetNames } from "../../utils/sound";
import type { PresetName } from "../../utils/sound";

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

  const presets = [
    { value: PresetNames.Silent, label: "静音模式" },
    { value: PresetNames.ErrorsOnly, label: "仅错误提示" },
    { value: PresetNames.Minimal, label: "极简模式" },
    { value: PresetNames.Standard, label: "标准模式" },
    { value: PresetNames.Full, label: "完整体验" },
  ];

  return (
    <div className="sound-settings space-y-6">
      {/* 全局开关 */}
      <div className="setting-item flex items-center justify-between">
        <label
          htmlFor="sound-enabled"
          className="text-sm font-medium text-gray-700 dark:text-gray-300"
        >
          启用音效
        </label>
        <button
          id="sound-enabled"
          type="button"
          role="switch"
          aria-checked={config.master.enabled}
          onClick={() => handleToggle(!config.master.enabled)}
          className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 ${
            config.master.enabled ? "bg-blue-600" : "bg-gray-200"
          }`}
        >
          <span
            className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
              config.master.enabled ? "translate-x-6" : "translate-x-1"
            }`}
          />
        </button>
      </div>

      {/* 音量控制 */}
      <div className="setting-item space-y-2">
        <div className="flex items-center justify-between">
          <label
            htmlFor="master-volume"
            className="text-sm font-medium text-gray-700 dark:text-gray-300"
          >
            主音量
          </label>
          <span className="text-sm text-gray-500 dark:text-gray-400">
            {config.master.volume}%
          </span>
        </div>
        <input
          id="master-volume"
          type="range"
          min="0"
          max="100"
          value={config.master.volume}
          onChange={(e) => handleVolumeChange(Number(e.target.value))}
          disabled={!config.master.enabled}
          className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer disabled:cursor-not-allowed disabled:opacity-50 dark:bg-gray-700 [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-4 [&::-webkit-slider-thumb]:h-4 [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-blue-600 [&::-webkit-slider-thumb]:cursor-pointer [&::-moz-range-thumb]:w-4 [&::-moz-range-thumb]:h-4 [&::-moz-range-thumb]:rounded-full [&::-moz-range-thumb]:bg-blue-600 [&::-moz-range-thumb]:border-0 [&::-moz-range-thumb]:cursor-pointer"
        />
      </div>

      {/* 预设方案 */}
      <div className="setting-item space-y-2">
        <label
          htmlFor="sound-preset"
          className="text-sm font-medium text-gray-700 dark:text-gray-300"
        >
          预设方案
        </label>
        <select
          id="sound-preset"
          onChange={(e) => handlePresetChange(e.target.value)}
          disabled={!config.master.enabled}
          className="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
        >
          {presets.map((preset) => (
            <option key={preset.value} value={preset.value}>
              {preset.label}
            </option>
          ))}
        </select>
      </div>

      {/* API 音效设置 */}
      <div className="setting-item space-y-2">
        <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300">
          API 请求音效
        </h3>
        <div className="flex items-center justify-between pl-4">
          <span className="text-sm text-gray-600 dark:text-gray-400">
            API 音效已{config.categories.api?.enabled ? "启用" : "禁用"}
          </span>
          <button
            type="button"
            onClick={() =>
              soundManager.setCategoryEnabled(
                "api",
                !config.categories.api?.enabled
              )
            }
            disabled={!config.master.enabled}
            className="text-sm text-blue-600 hover:text-blue-700 disabled:text-gray-400 disabled:cursor-not-allowed dark:text-blue-400 dark:hover:text-blue-300"
          >
            {config.categories.api?.enabled ? "禁用" : "启用"}
          </button>
        </div>
      </div>

      {/* 帮助文本 */}
      <div className="text-xs text-gray-500 dark:text-gray-400 space-y-1">
        <p>💡 提示：</p>
        <ul className="list-disc list-inside space-y-1 pl-2">
          <li>调整音量时会播放示例音效</li>
          <li>所有设置自动保存在本地浏览器中</li>
          <li>默认情况下 GET 请求不播放音效</li>
        </ul>
      </div>
    </div>
  );
}
