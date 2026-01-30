'use client';

import { useEffect, useState } from 'react';
import { X, CheckCircle2, AlertCircle, Info } from 'lucide-react';
import { ShinyButton } from './shiny-button';

export type ModalType = 'success' | 'error' | 'info';

interface CustomModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  message: React.ReactNode;
  type?: ModalType;
  primaryAction?: {
    label: string;
    onClick: () => void;
  };
  secondaryAction?: {
    label: string;
    onClick: () => void;
  };
}

export function CustomModal({
  isOpen,
  onClose,
  title,
  message,
  type = 'info',
  primaryAction,
  secondaryAction,
}: CustomModalProps) {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setIsVisible(true);
      document.body.style.overflow = 'hidden';
    } else {
      const timer = setTimeout(() => setIsVisible(false), 300);
      document.body.style.overflow = 'unset';
      return () => clearTimeout(timer);
    }
  }, [isOpen]);

  if (!isVisible && !isOpen) return null;

  const icons = {
    success: <CheckCircle2 className="w-12 h-12 text-emerald-400" />,
    error: <AlertCircle className="w-12 h-12 text-red-400" />,
    info: <Info className="w-12 h-12 text-blue-400" />,
  };

  const colors = {
    success: 'from-emerald-500/20 to-teal-500/20 border-emerald-500/30',
    error: 'from-red-500/20 to-orange-500/20 border-red-500/30',
    info: 'from-blue-500/20 to-indigo-500/20 border-blue-500/30',
  };

  return (
    <div className={`fixed inset-0 z-50 flex items-center justify-center p-4 transition-opacity duration-300 ${isOpen ? 'opacity-100' : 'opacity-0'}`}>
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-slate-950/80 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Modal Content */}
      <div className={`relative w-full max-w-md transform transition-all duration-300 ${isOpen ? 'scale-100 translate-y-0' : 'scale-95 translate-y-4'}`}>
        <div className={`glass-panel p-1 rounded-2xl border ${colors[type].split(' ').pop()} overflow-hidden`}>
          <div className={`bg-gradient-to-b ${colors[type]} p-6 rounded-xl`}>
            
            <button 
              onClick={onClose}
              className="absolute top-4 right-4 text-slate-400 hover:text-white transition-colors p-1"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex flex-col items-center text-center">
              <div className="mb-4 p-3 bg-white/5 rounded-full ring-1 ring-white/10 shadow-xl">
                {icons[type]}
              </div>

              <h3 className="text-2xl font-bold text-white mb-2">{title}</h3>
              <div className="text-slate-300 mb-8 leading-relaxed">
                {message}
              </div>

              <div className="flex gap-3 w-full justify-center">
                {secondaryAction && (
                  <button
                    onClick={secondaryAction.onClick}
                    className="px-5 py-2.5 rounded-xl text-sm font-medium text-slate-300 hover:text-white hover:bg-white/5 border border-transparent hover:border-white/10 transition-all"
                  >
                    {secondaryAction.label}
                  </button>
                )}
                
                {primaryAction ? (
                    <button
                        onClick={primaryAction.onClick}
                        className="px-6 py-2.5 bg-gradient-to-r from-indigo-500 to-cyan-500 hover:from-indigo-400 hover:to-cyan-400 text-white rounded-xl shadow-lg shadow-indigo-500/20 font-medium transition-all transform hover:scale-105"
                    >
                        {primaryAction.label} 
                    </button>
                ) : (
                  <button
                    onClick={onClose}
                    className="px-8 py-2.5 bg-white/10 hover:bg-white/20 text-white rounded-xl border border-white/10 transition-all font-medium"
                  >
                    Close
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
