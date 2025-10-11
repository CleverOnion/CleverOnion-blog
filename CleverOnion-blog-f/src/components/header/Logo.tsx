import React, { useState } from "react";
import { Link } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import ScallionIcon from "../../assets/header/logo/scallion.svg";
import SheepIcon from "../../assets/header/logo/sheep.svg";
import OnionIcon from "../../assets/header/logo/onion.svg";

const Logo: React.FC = () => {
  const [isActivated, setIsActivated] = useState(false);
  const [isClicked, setIsClicked] = useState(false);
  const [isFinalForm, setIsFinalForm] = useState(false);

  const handleClick = (e: React.MouseEvent) => {
    // 只在需要触发动画时阻止默认导航
    if (isActivated && !isClicked) {
      e.preventDefault();
      setIsClicked(true);
      // 延迟触发最终形态
      setTimeout(() => {
        setIsFinalForm(true);
      }, 1500);
    }
    // 否则让Link正常导航到首页
  };

  return (
    <div className="flex-shrink-0">
      <Link
        to="/"
        className="relative flex items-center justify-center h-12 px-4 group"
        onMouseEnter={() => setIsActivated(true)}
        onClick={handleClick}
      >
        <div className="relative flex items-center whitespace-nowrap">
          {/* 最终形态的洋葱图标 */}
          <motion.div
            className="flex items-center justify-center"
            animate={{
              width: isFinalForm ? 40 : 0,
              opacity: isFinalForm ? 1 : 0,
              scale: isFinalForm ? 1 : 0,
              marginRight: isFinalForm ? 8 : 0,
            }}
            transition={{
              type: "spring",
              stiffness: 200,
              damping: 30,
              delay: isFinalForm ? 0.8 : 0,
            }}
            style={{ overflow: "visible" }}
          >
            <motion.img
              src={OnionIcon}
              alt="CleverOnion Logo"
              className="w-8 h-8"
              animate={{
                rotate: isFinalForm ? [0, 360] : 0,
              }}
              transition={{
                duration: 1.5,
                ease: "easeInOut",
              }}
            />
          </motion.div>

          {/* Clever 文字 */}
          <motion.span
            className="text-xl font-bold bg-gradient-to-r from-blue-900 to-blue-700 bg-clip-text text-transparent"
            style={{ fontFamily: "'Ubuntu', sans-serif" }}
            animate={{
              x: isFinalForm ? 0 : isActivated ? -8 : 0,
              opacity: isFinalForm ? 1 : 1,
            }}
            transition={{
              type: "spring",
              stiffness: 300,
              damping: 30,
              delay: isFinalForm ? 0.3 : 0,
            }}
          >
            Clever
          </motion.span>

          {/* 中间的图标容器 */}
          <motion.div
            className="flex flex-col items-center justify-center"
            animate={{
              width: isFinalForm ? 0 : isActivated ? 32 : 0,
              opacity: isFinalForm ? 0 : isActivated ? 1 : 0,
              scale: isFinalForm ? 0 : isActivated ? 1 : 0,
              marginLeft: isFinalForm ? 0 : isActivated ? 4 : 0,
              marginRight: isFinalForm ? 0 : isActivated ? 4 : 0,
            }}
            transition={{
              type: "spring",
              stiffness: 400,
              damping: 25,
              duration: 0.6,
            }}
            style={{ overflow: "visible" }}
          >
            <AnimatePresence mode="wait">
              {isClicked ? (
                <>
                  {/* 合成光晕效果 */}
                  <motion.div
                    className="absolute inset-0 rounded-full"
                    style={{
                      background:
                        "radial-gradient(circle, rgba(255,165,0,0.3) 0%, rgba(255,140,0,0.2) 50%, transparent 70%)",
                    }}
                    initial={{ opacity: 0, scale: 0.5 }}
                    animate={{
                      opacity: [0, 0.6, 0],
                      scale: [0.5, 1.5, 2],
                    }}
                    transition={{
                      duration: 0.6,
                      ease: "easeInOut",
                    }}
                  />

                  {/* 洋葱图标 */}
                  <motion.img
                    key="onion"
                    src={OnionIcon}
                    alt="洋葱图标"
                    aria-hidden="true"
                    className="w-6 h-6"
                    initial={{ opacity: 0, scale: 0, rotate: 0, x: 0 }}
                    animate={{
                      opacity: isFinalForm ? 0 : [0, 0.8, 1],
                      scale: isFinalForm ? 0 : [0, 1.2, 1],
                      rotate: isFinalForm ? 0 : [0, 10, -10, 0],
                      x: isFinalForm ? -60 : 0,
                    }}
                    exit={{
                      opacity: 0,
                      scale: 0,
                      x: -60,
                      transition: { duration: 1.2, ease: "easeInOut" },
                    }}
                    transition={{
                      duration: isFinalForm ? 1.2 : 0.8,
                      ease: "easeInOut",
                      delay: isFinalForm ? 0.3 : 0.2,
                    }}
                  />
                </>
              ) : (
                <motion.div
                  key="icons"
                  className="flex flex-col items-center relative"
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{
                    opacity: 0,
                    scale: 0.8,
                    transition: { duration: 0.4 },
                  }}
                  transition={{ duration: 0.3 }}
                >
                  {/* 上方的葱图标 */}
                  <motion.img
                    src={ScallionIcon}
                    alt="大葱图标"
                    aria-hidden="true"
                    className="w-5 h-5 mb-0.5"
                    initial={{ y: -5, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    exit={{
                      y: -2,
                      x: 0,
                      scale: 0.8,
                      opacity: 0.5,
                      transition: { duration: 0.3 },
                    }}
                    transition={{ delay: 0.1, duration: 0.3 }}
                  />
                  {/* 下方的羊图标 */}
                  <motion.img
                    src={SheepIcon}
                    alt="羊图标"
                    aria-hidden="true"
                    className="w-5 h-5"
                    initial={{ y: 5, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    exit={{
                      y: 2,
                      x: 0,
                      scale: 0.8,
                      opacity: 0.5,
                      transition: { duration: 0.3, delay: 0.1 },
                    }}
                    transition={{ delay: 0.2, duration: 0.3 }}
                  />

                  {/* 合成效果的光晕 */}
                  <motion.div
                    className="absolute inset-0 rounded-full bg-gradient-to-r from-yellow-200 to-orange-200 opacity-0"
                    animate={{
                      opacity: isClicked ? [0, 0.6, 0] : 0,
                      scale: isClicked ? [0.5, 1.5, 2] : 0.5,
                    }}
                    transition={{
                      duration: 0.6,
                      ease: "easeOut",
                    }}
                  />
                </motion.div>
              )}
            </AnimatePresence>
          </motion.div>

          {/* Onion 文字 */}
          <motion.span
            className="text-xl font-bold bg-gradient-to-r from-blue-900 to-blue-700 bg-clip-text text-transparent"
            style={{ fontFamily: "'Ubuntu', sans-serif" }}
            animate={{
              x: isFinalForm ? 0 : isActivated ? 8 : 0,
              opacity: isFinalForm ? 1 : 1,
            }}
            transition={{
              type: "spring",
              stiffness: 300,
              damping: 30,
              delay: isFinalForm ? 1.2 : 0,
            }}
          >
            Onion
          </motion.span>
        </div>
      </Link>
    </div>
  );
};

export default Logo;
