import React from "react";
import { Outlet, useLocation } from "react-router-dom";
import { AnimatePresence, motion } from "motion/react";
import Header from "./Header";
import Footer from "./Footer";

const Layout: React.FC = () => {
  const location = useLocation();

  return (
    <div className="min-h-screen">
      {/* Header - 透明固定在顶部 */}
      <Header />

      {/* 主要内容区域 */}
      <main>
        <AnimatePresence mode="wait">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{
              duration: 0.3,
              ease: "easeInOut",
            }}
          >
            <Outlet />
          </motion.div>
        </AnimatePresence>
      </main>

      {/* 底部 */}
      <Footer />
    </div>
  );
};

export default Layout;
