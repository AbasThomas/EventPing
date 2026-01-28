'use client';

import { Settings, User, Bell, Shield, Mail } from 'lucide-react';
import { useAuth } from '@/lib/auth-context';

export default function SettingsPage() {
  const { user } = useAuth();

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div>
        <h1 className="text-3xl font-bold text-white mb-2">Settings</h1>
        <p className="text-slate-400">Manage your account preferences and system configuration</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1 space-y-2">
            {[
                { name: 'Profile Information', icon: User, active: true },
                { name: 'Notifications', icon: Bell },
                { name: 'Security & Privacy', icon: Shield },
                { name: 'Email Settings', icon: Mail }
            ].map(item => (
                <button 
                    key={item.name}
                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${
                        item.active 
                        ? 'bg-indigo-500/10 text-indigo-400 border border-indigo-500/20' 
                        : 'text-slate-400 hover:text-white hover:bg-white/5'
                    }`}
                >
                    <item.icon className="w-5 h-5" />
                    <span className="font-medium">{item.name}</span>
                </button>
            ))}
        </div>

        <div className="lg:col-span-2 space-y-6">
            <div className="glass-panel p-6 rounded-2xl border border-white/10">
                <h3 className="text-xl font-bold text-white mb-6">Profile Details</h3>
                <div className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="space-y-2">
                            <label className="text-sm font-medium text-slate-400 ml-1">Full Name</label>
                            <input 
                                type="text" 
                                defaultValue={user?.fullName || ''}
                                className="w-full px-4 py-2.5 bg-slate-900 border border-white/10 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-sm font-medium text-slate-400 ml-1">Email Address</label>
                            <input 
                                type="email" 
                                defaultValue={user?.email || ''}
                                disabled
                                className="w-full px-4 py-2.5 bg-slate-900/50 border border-white/10 rounded-xl text-slate-500 cursor-not-allowed"
                            />
                        </div>
                    </div>
                </div>
                <div className="mt-8 pt-6 border-t border-white/5 flex justify-end">
                    <button className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white font-medium rounded-xl transition-all shadow-lg shadow-indigo-500/20">
                        Save Changes
                    </button>
                </div>
            </div>
        </div>
      </div>
    </div>
  );
}
