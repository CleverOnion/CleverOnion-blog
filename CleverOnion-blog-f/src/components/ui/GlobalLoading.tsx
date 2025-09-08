import React from 'react';
import { useLoading } from '../../contexts/LoadingContext';
import { Loading } from './Loading';

const GlobalLoading: React.FC = () => {
  const { isLoading, loadingMessage } = useLoading();

  if (!isLoading) {
    return null;
  }

  return (
    <Loading 
      text={loadingMessage} 
      overlay={true} 
      size="lg"
    />
  );
};

export default GlobalLoading;