import React from 'react';
import { CheckCircle2, AlertCircle, X } from 'lucide-react';
import { useAccounting } from '../context/AccountingContext';

export const AlertBanner: React.FC = () => {
  const { notification, errorMessage, setNotification, setErrorMessage } = useAccounting();

  if (!notification && !errorMessage) return null;

  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col space-y-2 max-w-md">
      {notification && (
        <div className="flex items-center justify-between p-4 bg-emerald-600 text-white rounded-lg shadow-lg">
          <div className="flex items-center space-x-3">
            <CheckCircle2 className="w-5 h-5 shrink-0" />
            <span className="text-sm font-medium">{notification}</span>
          </div>
          <button
            onClick={() => setNotification(null)}
            className="ml-3 hover:text-emerald-200"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {errorMessage && (
        <div className="flex items-center justify-between p-4 bg-rose-600 text-white rounded-lg shadow-lg">
          <div className="flex items-center space-x-3">
            <AlertCircle className="w-5 h-5 shrink-0" />
            <span className="text-sm font-medium">{errorMessage}</span>
          </div>
          <button
            onClick={() => setErrorMessage(null)}
            className="ml-3 hover:text-rose-200"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}
    </div>
  );
};
