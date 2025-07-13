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
    <header className="fixed top-0 left-0 right-0 z-50 bg-white/10 backdrop-blur-sm transition-all duration-300">
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