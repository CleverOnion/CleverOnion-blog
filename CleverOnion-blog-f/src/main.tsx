import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.tsx";
import { soundManager } from "./utils/sound";

// 初始化音效系统（在用户首次交互时）
document.addEventListener(
  "click",
  () => {
    soundManager.init();
  },
  { once: true }
);

// 预加载常用音效
soundManager.preload(["api.success.post", "api.error.500"]);

createRoot(document.getElementById("root")!).render(<App />);
