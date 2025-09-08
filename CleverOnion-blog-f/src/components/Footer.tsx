import React from 'react';
import { FiGithub, FiMail, FiArrowUp } from 'react-icons/fi';

const Footer: React.FC = () => {
  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-blue-50 border-t border-blue-200">
      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
          {/* 左侧：网站信息和社交链接 */}
          <div className="flex flex-col md:flex-row items-center space-y-3 md:space-y-0 md:space-x-6">
            <div className="text-center md:text-left">
              <h3 className="text-lg font-semibold text-gray-900 font-ubuntu">
                CleverOnion Blog
              </h3>
              <p className="text-sm text-gray-500 mt-1">
                分享技术见解，记录成长历程
              </p>
            </div>
            
            {/* 社交媒体链接 */}
            <div className="flex space-x-3">
              <a
                href="https://github.com/CleverOnion"
                target="_blank"
                rel="noopener noreferrer"
                className="p-2 text-gray-500 hover:text-gray-700 transition-colors duration-200"
                title="GitHub"
              >
                <FiGithub className="w-4 h-4" />
              </a>
              <a
                href="mailto:2951698265@qq.com"
                className="p-2 text-gray-500 hover:text-green-500 transition-colors duration-200"
                title="Email"
              >
                <FiMail className="w-4 h-4" />
              </a>
            </div>
          </div>

          {/* 右侧：版权信息和回到顶部 */}
          <div className="flex flex-col md:flex-row items-center space-y-2 md:space-y-0 md:space-x-4">
            <span className="text-sm text-gray-500">
              © {currentYear} CleverOnion Blog
            </span>
            <button
              onClick={scrollToTop}
              className="flex items-center space-x-1 text-sm text-gray-500 hover:text-gray-700 transition-colors duration-200"
              title="回到顶部"
            >
              <FiArrowUp className="w-3 h-3" />
              <span>顶部</span>
            </button>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;