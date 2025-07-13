import React from 'react';

const Hero: React.FC = () => {
  return (
    <section className="relative h-96 bg-gradient-to-b from-sky-300 to-white overflow-hidden">
      {/* 分层云朵背景 - 从下往上的大云朵局部 */}
      <div className="absolute inset-0">
        <svg className="absolute inset-0 w-full h-full" viewBox="0 0 1200 400" preserveAspectRatio="xMidYMid slice">
          {/* 内层云朵 - 白色+更多蓝色，从底部开始的大云朵局部 */}
          <g fill="rgb(200, 230, 250)">
            {/* 左侧大云朵的顶部局部 */}
            <circle cx="100" cy="280" r="120" />
            <circle cx="250" cy="260" r="100" />
            <circle cx="400" cy="300" r="90" />
            <circle cx="520" cy="280" r="80" />
            
            {/* 右侧大云朵的顶部局部 */}
            <circle cx="700" cy="260" r="110" />
            <circle cx="850" cy="240" r="95" />
            <circle cx="1000" cy="280" r="85" />
            <circle cx="1150" cy="260" r="75" />
            
            {/* 中央大云朵的顶部局部 */}
            <circle cx="550" cy="220" r="90" />
            <circle cx="650" cy="200" r="80" />
          </g>
          
          {/* 中层云朵 - 白色+较少蓝色，从底部开始的更大云朵局部 */}
          <g fill="rgb(230, 245, 255)">
            {/* 左侧更大云朵的中部局部 */}
            <circle cx="50" cy="340" r="140" />
            <circle cx="200" cy="320" r="130" />
            <circle cx="350" cy="360" r="120" />
            <circle cx="500" cy="340" r="110" />
            <circle cx="620" cy="320" r="100" />
            
            {/* 右侧更大云朵的中部局部 */}
            <circle cx="650" cy="320" r="135" />
            <circle cx="800" cy="300" r="125" />
            <circle cx="950" cy="340" r="115" />
            <circle cx="1100" cy="320" r="105" />
            <circle cx="1200" cy="300" r="95" />
          </g>
          
          {/* 外层云朵 - 纯白色，从底部开始的巨大云朵局部 */}
          <g fill="rgb(255, 255, 255)">
            {/* 横跨整个底部的巨大云朵局部 */}
            <circle cx="0" cy="420" r="180" />
            <circle cx="150" cy="400" r="170" />
            <circle cx="300" cy="440" r="160" />
            <circle cx="450" cy="420" r="150" />
            <circle cx="600" cy="400" r="140" />
            <circle cx="750" cy="440" r="160" />
            <circle cx="900" cy="420" r="150" />
            <circle cx="1050" cy="400" r="140" />
            <circle cx="1200" cy="440" r="130" />
          </g>
        </svg>
      </div>
      

    </section>
  );
};

export default Hero;