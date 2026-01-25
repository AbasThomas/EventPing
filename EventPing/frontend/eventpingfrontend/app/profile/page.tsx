'use client';

import { useState, useEffect } from 'react';
import { apiFetch } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { PageLayout } from '@/components/layout/PageLayout';
import { User, Mail, Phone, Loader2, Save, CheckCircle, AlertCircle } from 'lucide-react';

export default function ProfilePage() {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phoneNumber: '',
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (user) {
      fetchProfile();
    }
  }, [user]);

  const fetchProfile = async () => {
    try {
      const data = await apiFetch(`/users/${user?.id}`);
      setFormData({
        fullName: data.fullName || '',
        email: data.email || '',
        phoneNumber: data.phoneNumber || '',
      });
    } catch (err: any) {
      console.error(err);
      setError('Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setMessage('');
    setError('');

    try {
      await apiFetch(`/users/${user?.id}`, {
        method: 'PUT',
        body: JSON.stringify(formData),
      });
      setMessage('Profile updated successfully');
    } catch (err: any) {
      setError(err.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <PageLayout>
        <div className="flex min-h-[60vh] items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-indigo-500" />
        </div>
      </PageLayout>
    );
  }

  return (
    <PageLayout>
      <div className="max-w-2xl mx-auto px-4 py-12 sm:px-6 lg:px-8">
        <div className="bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-2xl p-8 shadow-2xl">
          <div className="flex items-center gap-4 mb-8">
            <div className="w-16 h-16 bg-indigo-500/20 rounded-full flex items-center justify-center border border-indigo-500/30">
              <User className="h-8 w-8 text-indigo-400" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-white">Your Profile</h1>
              <p className="text-slate-400">Manage your account information</p>
            </div>
          </div>

          {message && (
            <div className="mb-6 p-3 bg-green-500/10 border border-green-500/20 rounded-lg flex items-center gap-2 text-green-400">
              <CheckCircle className="h-5 w-5" />
              {message}
            </div>
          )}

          {error && (
            <div className="mb-6 p-3 bg-red-500/10 border border-red-500/20 rounded-lg flex items-center gap-2 text-red-400">
              <AlertCircle className="h-5 w-5" />
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Full Name
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <User className="h-5 w-5 text-slate-500" />
                </div>
                <input
                  type="text"
                  value={formData.fullName}
                  onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  className="block w-full pl-10 pr-3 py-2.5 bg-slate-800/50 border border-slate-700 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                  placeholder="John Doe"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Email Address
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-5 w-5 text-slate-500" />
                </div>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="block w-full pl-10 pr-3 py-2.5 bg-slate-800/50 border border-slate-700 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                  placeholder="you@example.com"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Phone Number
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Phone className="h-5 w-5 text-slate-500" />
                </div>
                <input
                  type="text"
                  value={formData.phoneNumber}
                  onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                  className="block w-full pl-10 pr-3 py-2.5 bg-slate-800/50 border border-slate-700 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500/50 transition-all"
                  placeholder="+1 (555) 000-0000"
                  required
                />
              </div>
            </div>

            <div className="pt-4">
              <button
                type="submit"
                disabled={saving}
                className="w-full flex items-center justify-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-medium py-2.5 px-4 rounded-lg transition-all shadow-[0_0_20px_-5px_rgba(99,102,241,0.5)] hover:shadow-[0_0_25px_-5px_rgba(99,102,241,0.6)] disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {saving ? (
                  <>
                    <Loader2 className="h-5 w-5 animate-spin" />
                    Saving Changes...
                  </>
                ) : (
                  <>
                    <Save className="h-5 w-5" />
                    Save Changes
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </PageLayout>
  );
}
