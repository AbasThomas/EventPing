'use client';

import { useState } from 'react';
import { useAuth } from '@/lib/auth-context';
import { apiFetch } from '@/lib/api';
import Link from 'next/link';
import { Mail, Lock, User, Loader2, ArrowRight, Phone } from 'lucide-react';
import { CountrySelector } from '@/components/ui/CountrySelector';
import { ShinyButton } from '@/components/ui/shiny-button';
import { CustomModal, ModalType } from '@/components/ui/CustomModal';

export default function RegisterPage() {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [countryCode, setCountryCode] = useState('+1');
  const [password, setPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { login } = useAuth();

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [modalConfig, setModalConfig] = useState<{
    title: string;
    message: string;
    type: ModalType;
    primaryAction?: { label: string; onClick: () => void };
  }>({ title: '', message: '', type: 'info' });

  const showModal = (title: string, message: string, type: ModalType, action?: { label: string; onClick: () => void }) => {
    setModalConfig({ title, message, type, primaryAction: action });
    setModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      // Combine country code and phone number
      const fullPhoneNumber = `${countryCode}${phoneNumber.replace(/^0+/, '')}`;

      // First register
      await apiFetch('/users/register', {
        method: 'POST',
        body: JSON.stringify({ fullName, email, phoneNumber: fullPhoneNumber, password }),
      });

      // Then auto-login
      const loginData = await apiFetch('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });

      if (loginData.token && loginData.token.accessToken) {
        showModal(
          'Account Created!',
          'Redirecting you to the dashboard...',
          'success',
          { label: 'Go to Dashboard', onClick: () => {
             login(loginData.token.accessToken, loginData.user);
          }}
        );
        // Add a small delay for the user to see the modal before auto-redirecting if they don't click
        setTimeout(() => {
           login(loginData.token.accessToken, loginData.user);
        }, 2000);
      } else {
        // If auto-login fails, redirect to login page
        window.location.href = '/auth/login';
      }
    } catch (err: any) {
      showModal('Registration Failed', err.message || 'Failed to create account', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <div className="glass-panel p-8 rounded-2xl shadow-xl backdrop-blur-xl border border-white/10">
        <div className="mb-8 text-center">
          <h1 className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 to-cyan-400 mb-2">
            Create Account
          </h1>
          <p className="text-slate-400">Join EventPing to start managing events</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300 ml-1">Full Name</label>
            <div className="relative group">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                <User className="h-5 w-5" />
              </div>
              <input
                type="text"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
                className="block w-full pl-10 pr-3 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                placeholder="John Doe"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300 ml-1">Email Address</label>
            <div className="relative group">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                <Mail className="h-5 w-5" />
              </div>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="block w-full pl-10 pr-3 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                placeholder="you@example.com"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300 ml-1">Phone Number</label>
            <div className="flex gap-2">
              <CountrySelector 
                onSelect={(c) => setCountryCode(c.dialCode)} 
                selectedDialCode={countryCode}
              />
              <div className="relative group flex-1">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                  <Phone className="h-5 w-5" />
                </div>
                <input
                  type="tel"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value.replace(/\D/g, ''))}
                  required
                  className="block w-full pl-10 pr-3 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                  placeholder="1234567890"
                />
              </div>
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300 ml-1">Password</label>
            <div className="relative group">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                <Lock className="h-5 w-5" />
              </div>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={6}
                className="block w-full pl-10 pr-3 py-3 bg-slate-900/50 border border-slate-700/50 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                placeholder="••••••••"
              />
            </div>
          </div>

          <ShinyButton
            type="submit"
            disabled={isSubmitting}
            className="w-full !p-0" // Reset padding as ShinyButton adds it, but we want full width here mostly, or let it handle it. ShinyButton is inline-block by default.
            // Actually ShinyButton internal div is inline-block. We need to style it.
            // The ShinyButton prop className applies to the wrapping div.
            // We'll trust ShinyButton's default for now or pass w-full.
            // If ShinyButton is inline-block, w-full on it will make the container full width.
          >
            <div className="flex items-center justify-center gap-2 w-full px-8 py-1">
              {isSubmitting ? (
                <Loader2 className="h-5 w-5 animate-spin" />
              ) : (
                <>
                  Create Account
                  <ArrowRight className="h-4 w-4 group-hover:translate-x-1 transition-transform" />
                </>
              )}
            </div>
          </ShinyButton>
        </form>

        <div className="mt-8 text-center text-sm text-slate-400">
          Already have an account?{' '}
          <Link href="/auth/login" className="text-indigo-400 hover:text-indigo-300 font-medium transition-colors">
            Sign in instead
          </Link>
        </div>
      </div>

      <CustomModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={modalConfig.title}
        message={modalConfig.message}
        type={modalConfig.type}
        primaryAction={modalConfig.primaryAction}
      />
    </>
  );
}
