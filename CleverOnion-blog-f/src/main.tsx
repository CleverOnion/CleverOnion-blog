import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { preloadAudioFiles } from './utils/audioUtils'

// 预加载音效文件
preloadAudioFiles();

createRoot(document.getElementById('root')!).render(
  <App />
)
