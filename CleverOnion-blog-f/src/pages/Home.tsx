import React from "react";
import Hero from "../components/home/Hero";
import MainContent from "../components/home/MainContent";

const Home: React.FC = () => {
  return (
    <div className="min-h-screen">
      {/* 隐藏的主标题，用于正确的标题层级 */}
      <h1 className="sr-only">CleverOnion's Blog - 首页</h1>

      {/* Hero 区域 */}
      <Hero />

      {/* 主要内容区域 */}
      <main
        id="main-content"
        tabIndex={-1}
        className="bg-white bg-opacity-80 focus:outline-none"
      >
        <MainContent />
      </main>
    </div>
  );
};

export default Home;
