import React from "react";
import { Link } from "react-router-dom";
import { motion } from "motion/react";
import { FiHome, FiArrowLeft, FiCloud, FiSearch } from "react-icons/fi";

/**
 * 404页面组件
 * 提供友好的错误提示和导航选项，保持与项目整体风格一致
 */
const NotFound: React.FC = () => {
  return (
    <div className="min-h-screen bg-gradient-to-b from-sky-300 to-white overflow-hidden relative">
      {/* 主要内容 */}
      <main
        id="main-content"
        tabIndex={-1}
        className="relative z-10 flex items-center justify-center min-h-screen px-6 focus:outline-none"
      >
        <motion.div
          className="text-center max-w-2xl mx-auto"
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: "easeOut" }}
        >
          {/* 404数字 */}
          <motion.div
            className="mb-8"
            initial={{ scale: 0.8 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
          >
            <h1 className="text-8xl md:text-9xl font-bold text-white drop-shadow-lg mb-4">
              404
            </h1>
            <motion.div
              className="flex justify-center mb-6"
              animate={{
                y: [0, -10, 0],
                rotate: [0, 5, -5, 0],
              }}
              transition={{
                duration: 3,
                repeat: Infinity,
                ease: "easeInOut",
              }}
            >
              <FiCloud className="text-6xl text-white drop-shadow-md" />
            </motion.div>
          </motion.div>

          {/* 错误信息 */}
          <motion.div
            className="mb-12"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.4 }}
          >
            <h2 className="text-2xl md:text-3xl font-semibold text-gray-800 mb-4">
              哎呀！页面飞到云端去了
            </h2>
            <p className="text-lg text-gray-600 leading-relaxed">
              您访问的页面可能已经移动、删除或者从未存在过。
              <br className="hidden md:block" />
              不过别担心，让我们帮您找到正确的方向！
            </p>
          </motion.div>

          {/* 操作按钮 */}
          <motion.div
            className="flex flex-col sm:flex-row gap-4 justify-center items-center"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.6 }}
          >
            <Link
              to="/"
              className="inline-flex items-center px-6 py-3 bg-white text-blue-600 font-semibold rounded-lg shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300 border-2 border-blue-100 hover:border-blue-300"
            >
              <FiHome className="mr-2" />
              回到首页
            </Link>

            <button
              onClick={() => window.history.back()}
              className="inline-flex items-center px-6 py-3 bg-blue-600 text-white font-semibold rounded-lg shadow-lg hover:shadow-xl hover:bg-blue-700 transform hover:scale-105 transition-all duration-300"
            >
              <FiArrowLeft className="mr-2" />
              返回上页
            </button>
          </motion.div>
        </motion.div>
      </main>
    </div>
  );
};

export default NotFound;
