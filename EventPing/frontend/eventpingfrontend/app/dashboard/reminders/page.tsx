'use client';

import { Bell, Clock, CheckCircle, AlertCircle } from 'lucide-react';

export default function RemindersPage() {
  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div>
        <h1 className="text-3xl font-bold text-white mb-2">Reminders</h1>
        <p className="text-slate-400">Track and manage automated notifications for your participants</p>
      </div>

      <div className="glass-panel p-12 rounded-2xl border border-dashed border-white/20 text-center">
        <div className="w-16 h-16 bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-4 text-slate-500">
          <Bell className="w-8 h-8" />
        </div>
        <h3 className="text-lg font-medium text-white mb-2">Reminder History</h3>
        <p className="text-slate-400 mb-6">This feature is coming soon. You'll be able to see exactly when and how your participants were notified.</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 opacity-50 pointer-events-none">
        <div className="glass-panel p-6 rounded-2xl border border-white/10">
            <div className="flex items-center gap-4 mb-4">
                <div className="p-3 rounded-lg bg-emerald-500/20 text-emerald-400">
                    <CheckCircle className="w-6 h-6" />
                </div>
                <div>
                    <div className="text-2xl font-bold text-white">1,204</div>
                    <div className="text-sm text-slate-400">Sent Successfully</div>
                </div>
            </div>
        </div>
        <div className="glass-panel p-6 rounded-2xl border border-white/10">
            <div className="flex items-center gap-4 mb-4">
                <div className="p-3 rounded-lg bg-red-500/20 text-red-400">
                    <AlertCircle className="w-6 h-6" />
                </div>
                <div>
                    <div className="text-2xl font-bold text-white">0</div>
                    <div className="text-sm text-slate-400">Failed Delivery</div>
                </div>
            </div>
        </div>
      </div>
    </div>
  );
}
