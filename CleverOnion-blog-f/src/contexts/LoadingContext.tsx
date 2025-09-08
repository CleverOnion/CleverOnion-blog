import React, { createContext, useContext, useState, ReactNode } from 'react';

interface LoadingContextType {
  isLoading: boolean;
  loadingMessage: string;
  setLoading: (loading: boolean, message?: string) => void;
  withLoading: <T>(promise: Promise<T>, message?: string) => Promise<T>;
}

const LoadingContext = createContext<LoadingContextType | undefined>(undefined);

interface LoadingProviderProps {
  children: ReactNode;
}

export const LoadingProvider: React.FC<LoadingProviderProps> = ({ children }) => {
  const [isLoading, setIsLoading] = useState(false);
  const [loadingMessage, setLoadingMessage] = useState('加载中...');

  const setLoading = (loading: boolean, message: string = '加载中...') => {
    setIsLoading(loading);
    setLoadingMessage(message);
  };

  const withLoading = async <T,>(promise: Promise<T>, message: string = '加载中...'): Promise<T> => {
    try {
      setLoading(true, message);
      const result = await promise;
      return result;
    } finally {
      setLoading(false);
    }
  };

  const value: LoadingContextType = {
    isLoading,
    loadingMessage,
    setLoading,
    withLoading,
  };

  return (
    <LoadingContext.Provider value={value}>
      {children}
    </LoadingContext.Provider>
  );
};

export const useLoading = (): LoadingContextType => {
  const context = useContext(LoadingContext);
  if (context === undefined) {
    throw new Error('useLoading must be used within a LoadingProvider');
  }
  return context;
};

export default LoadingContext;