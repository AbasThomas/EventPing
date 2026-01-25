'use client';

import { useState, useEffect } from 'react';
import { apiFetch } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';

export default function ProfilePage() {
  const { user, login } = useAuth();
  const [formData, setFormData] = useState({
    fullName: '',
    phoneNumber: '',
    email: '',
  });
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (user) {
      setFormData({
        fullName: user.fullName || '',
        phoneNumber: '', // Phone number might not be in basic user object depending on context
        email: user.email || '',
      });
      // Fetch full profile to get phone number if needed
      apiFetch(`/users/${user.id}`).then(data => {
        setFormData(prev => ({ ...prev, phoneNumber: data.phoneNumber, fullName: data.fullName }));
      });
    }
  }, [user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setMessage('');

    try {
      if (!user) return;

      const updatedUser = await apiFetch(`/users/${user.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          email: formData.email,
          fullName: formData.fullName,
          phoneNumber: formData.phoneNumber,
        }),
      });

      // Update context
      // We need token to update context login, retrieving it from localStorage
      const token = localStorage.getItem('token');
      if (token) {
        login(token, updatedUser);
      }
      
      setMessage('Profile updated successfully');
    } catch (err: any) {
      setMessage(err.message || 'Failed to update profile');
    } finally {
      setIsLoading(false);
    }
  };

  if (!user) return null;

  return (
    <div className="min-h-screen bg-background p-8 md:p-24">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold text-foreground mb-8">Profile Settings</h1>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium leading-6 text-foreground">
              Email Address
            </label>
            <div className="mt-2">
              <input
                type="email"
                disabled
                className="block w-full rounded-md border-0 py-1.5 text-gray-500 bg-gray-800 shadow-sm ring-1 ring-inset ring-gray-700 sm:text-sm sm:leading-6 px-3 cursor-not-allowed"
                value={formData.email}
              />
              <p className="mt-1 text-xs text-gray-500">Email cannot be changed</p>
            </div>
          </div>

          <div>
            <label htmlFor="fullName" className="block text-sm font-medium leading-6 text-foreground">
              Full Name
            </label>
            <div className="mt-2">
              <input
                type="text"
                name="fullName"
                id="fullName"
                required
                className="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6 px-3"
                value={formData.fullName}
                onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
              />
            </div>
          </div>

          <div>
            <label htmlFor="phoneNumber" className="block text-sm font-medium leading-6 text-foreground">
              Phone Number
            </label>
            <div className="mt-2">
              <input
                type="tel"
                name="phoneNumber"
                id="phoneNumber"
                required
                className="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6 px-3"
                value={formData.phoneNumber}
                onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
              />
            </div>
          </div>

          {message && (
            <div className={`text-sm ${message.includes('success') ? 'text-green-500' : 'text-red-500'}`}>
              {message}
            </div>
          )}

          <div className="flex items-center justify-end gap-x-6">
            <button
              type="submit"
              disabled={isLoading}
              className="rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:opacity-50"
            >
              {isLoading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
