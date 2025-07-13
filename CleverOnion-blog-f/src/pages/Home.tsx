import React from 'react';
import Hero from '../components/home/Hero';
import MainContent from '../components/home/MainContent';

const Home: React.FC = () => {
  return (
    <div className="min-h-screen">
      {/* Hero 区域 */}
      <Hero />
      
      {/* 主要内容区域 */}
      <div className="bg-white bg-opacity-80">
        <MainContent />
      </div>
    </div>
  );
};

export default Home;