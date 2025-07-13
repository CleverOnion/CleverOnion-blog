import React, { useState } from 'react';
import Logo from './header/Logo';
import Navigation from './header/Navigation';
import ActionButtons from './header/ActionButtons';

const Header: React.FC = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const handleMobileMenuToggle = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const handleMobileMenuClose = () => {
    setIsMobileMenuOpen(false);
  };

  return (
    <header className="relative bg-white/40 backdrop-blur-lg border-b border-gray-100/30 sticky top-0 z-50 transition-all duration-300">
      <div className="max-w-7xl mx-auto px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <Logo />
          <Navigation 
            isMobileMenuOpen={isMobileMenuOpen} 
            onMobileMenuClose={handleMobileMenuClose} 
          />
          <ActionButtons 
            isMobileMenuOpen={isMobileMenuOpen} 
            onMobileMenuToggle={handleMobileMenuToggle} 
          />
        </div>
      </div>
    </header>
  );
};

export default Header;