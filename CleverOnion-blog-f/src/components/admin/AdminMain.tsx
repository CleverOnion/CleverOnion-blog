import { Outlet, useLocation } from "react-router-dom";
import { AnimatePresence, motion } from "motion/react";

const AdminMain = () => {
  const location = useLocation();

  return (
    <main
      id="main-content"
      tabIndex={-1}
      className="flex-1 bg-gray-50 focus:outline-none"
    >
      <AnimatePresence mode="wait">
        <motion.div
          key={location.pathname}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{
            duration: 0.3,
            ease: "easeInOut",
          }}
          className="h-full"
        >
          <Outlet />
        </motion.div>
      </AnimatePresence>
    </main>
  );
};

export default AdminMain;
