import React from 'react';
import { Link } from 'react-router-dom';

const Logo: React.FC = () => {
  return (
    <div className="flex-shrink-0">
      <Link to="/" className="flex items-center space-x-3 group">
        <div className="w-9 h-9 bg-gradient-to-br from-blue-600 via-purple-600 to-blue-700 rounded-xl flex items-center justify-center shadow-lg group-hover:shadow-xl transition-all duration-300 group-hover:scale-105">
          <span className="text-white font-bold text-lg">C</span>
        </div>
        <span className="text-xl font-bold bg-gradient-to-r from-gray-800 to-gray-600 bg-clip-text text-transparent">CleverOnion</span>
      </Link>
    </div>
  );
};

export default Logo;